package domain;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import chat.ChatService;
import dao.UserDAO;

@ServerEndpoint(
    value = "/omok/{roomId}",
    configurator = HttpSessionConfigurator.class
)
public class OmokSocket {

    // 방(게임) 관리
    private static final Map<String, GameRoom> rooms =
        Collections.synchronizedMap(new HashMap<>());

    @OnOpen
    public void onOpen(
        Session session,
        @PathParam("roomId") String roomId,
        EndpointConfig config
    ) {

        // 1) 시간 제한 (?time=30)
        int timeLimit = 30;
        String queryString = session.getQueryString();
        if (queryString != null) {
            try {
                for (String param : queryString.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && "time".equals(pair[0])) {
                        timeLimit = Integer.parseInt(pair[1]);
                    }
                }
            } catch (Exception ignore) {}
        }

        // 2) HttpSession에서 User 가져오기 (없으면 생성)
        HttpSession httpSession =
            (HttpSession) config.getUserProperties().get("httpSession");

        User user = null;
        if (httpSession != null) {
            user = (User) httpSession.getAttribute("user");
        }

        if (user == null) {
            user = new User();
          //  user.setUserId(UUID.randomUUID().toString());
           //
            user.setNickname("게스트-" + user.getUserId().substring(0, 4));
            user.setAvatar("/img/default-avatar.jpg");
            if (httpSession != null) {
                httpSession.setAttribute("user", user);
            }
        } else {
            if (user.getAvatar() == null) user.setAvatar("/img/default-avatar.jpg");
            if (user.getNickname() == null) user.setNickname("게스트");
        }

        // 3) Player 생성 + session userProperties에 name 저장 (GameRoom에서 사용)
        String nickname = user.getNickname();
        Player newPlayer = new Player(session, nickname);
        newPlayer.setUserId(user.getUserId());
        newPlayer.setAvatar(user.getAvatar());
        //돌 이미지 때문에 추가한 코드 - 가빈
        newPlayer.setStoneStyle(user.getStone_style());

        session.getUserProperties().put("name", nickname);
        session.getUserProperties().put("roomId", roomId);
        session.getUserProperties().put("player", newPlayer);

        try {
            session.getBasicRemote().sendText("MYNAME:" + nickname);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4) 방 입장 (방 없으면 생성)
        final int finalTime = timeLimit;
        GameRoom room = rooms.computeIfAbsent(
        	    roomId,
        	    k -> new GameRoom(roomId, finalTime)
        	);
        room.enterUser(newPlayer);

        System.out.println("입장: " + nickname + " (방: " + roomId + ", 시간: " + room.getTimeLimit() + "초)");
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        String roomId = (String) session.getUserProperties().get("roomId");
        Player player = (Player) session.getUserProperties().get("player");

        if (roomId == null) return;
        GameRoom room = rooms.get(roomId);
        if (room == null) return;

        // ✅ 1️⃣ JSON 메시지 먼저 처리
        if (msg.startsWith("{")) {
            JsonObject json = JsonParser.parseString(msg).getAsJsonObject();
            String type = json.get("type").getAsString();

            // 🔁 재대결
            if ("REMATCH".equals(type)) {
                room.resetGame();
                return;
            }
         // ✅ 결과 요청 (추가!)
            if ("REQUEST_RESULT".equals(type)) {
                generateGameResult(room);
                return;
            }
            // 💬 채팅
            if ("CHAT".equals(type)) {
                ChatService chatService = new ChatService();
                chatService.handle(msg, player, room);
                return;
            }
        }

        // 2. 실행 
        room.processMove(msg, session);
        if (room.isGameOver()) {
            generateGameResult(room);
        }
    }
    
    private void safeSend(Player p, String text) {
        try {
            if (p != null && p.getSession() != null && p.getSession().isOpen()) {
                p.getSession().getBasicRemote().sendText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateGameResult(GameRoom room) {//채원 : gameResult 객체 생성
        try {
            Player black = room.getBlackPlayer();
            Player white = room.getWhitePlayer();

            int winnerStone = room.getWinnerStone(); 

            GameResult r1 = buildResult(
                black,
                room,
                winnerStone == 1
            );

            GameResult r2 = buildResult(
                white,
                room,
                winnerStone == 2
            );

            JsonObject res = new JsonObject();
            res.addProperty("type", "GAME_RESULT");
            res.add("results",
                new Gson().toJsonTree(new GameResult[]{ r1, r2 })
            );

            black.getSession().getBasicRemote().sendText(res.toString());
            white.getSession().getBasicRemote().sendText(res.toString());

            room.setGameOver(false);
            room.setWinnerStone(0); // 다음 게임 대비
            safeSend(black, res.toString());
            safeSend(white, res.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//채원 : 구현 
    private GameResult buildResult(Player p, GameRoom room, boolean win) {

        // 🔥 USER_ID 없으면 DB 접근 금지
        if (p.getUserId() == null) {
            return new GameResult(
                p.getNickname(),
                true,
                null,
                null,
                win
            );
        }

        Integer beforeScore = null;
        int plusScore = 0;

        try {
            UserDAO dao = new UserDAO();
            beforeScore = dao.selectScore(p.getUserId());

            if (win) {
                plusScore = room.getTimeLimit() <= 30 ? 3 : 6;
                dao.updateScore(p.getUserId(), plusScore);
            }
        } catch (Exception e) {
            System.out.println("⚠️ DB 점수 처리 실패");
        }

        return new GameResult(
            p.getNickname(),
            false,
            beforeScore,
            plusScore,
            win
        );
    }

	@OnClose
    public void onClose(Session session) {
        String roomId = (String) session.getUserProperties().get("roomId");
        if (roomId == null) return;

        GameRoom room = rooms.get(roomId);
        if (room != null) {
            room.exitUser(session);
        }

        System.out.println("연결 종료");
    }
  //gameResult 생성 승패 관련 : (채원)
    /*
     * 누가 이겼는지 확인
     * play->user로 꺼내기
     * userDAO에서 db조회
     * db에 없으면 ->guest (guest=true)
     * 있으면 -> beforescore =기존 점수
     * 	plusScore(승리시 얻는 점수 )= 모드고르기 ? 3/ 6 :+0
     * gameResult 생성자 대입
     * json으로 만들어서 js로 보내기 
     * 
     * 
     * */
    
}

