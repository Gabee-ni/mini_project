package domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import lombok.Data;
@Data
public class GameRoom {

    private final String roomId;

    private final Map<Session, User> users = new HashMap<>();

    private Player p1, p2;
    private Player blackPlayer, whitePlayer;
    private Player currentPlayer;

    private final OmokRule rule;

    private final int timeLimit;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask;
    private boolean gameOver = false; //(채원) : 게임이 끝났는지 확인
    public boolean isGameOver() { //채원 :확인. getterSetter만들기엔 너무 아까워서 메서드 하나 생성
        return gameOver;
    }
    
    private int winnerStone = 0; // 0=아직, 1=흑 승, 2=백 승
    public int getWinnerStone() { return winnerStone; }

    
    // ✅ 메인 생성자: roomId + timeLimit 둘 다 받도록 통합
    public GameRoom(String roomId, int timeLimit) {
        this.roomId = roomId;
        this.rule = new OmokRule();
        this.timeLimit = timeLimit;
    }

    // (호환용) 기존 GameRoom::new(roomId) 방어
    public GameRoom(String roomId) {
        this(roomId, 30);
    }

    // (호환용) OmokSocket에서 new GameRoom(timeLimit) 방어
    public GameRoom(int timeLimit) {
        this(null, timeLimit);
    }

    public int getTimeLimit() { return timeLimit; }

    public void addUser(Session session, User user) {
        users.put(session, user);
    }

    public void removeUser(Session session) {
        users.remove(session);
    }

    public Map<Session, User> getUsers() {
        return users;
    }

    // ✅ users만 보고 삭제되던 문제 방지
    public boolean isEmpty() {
        return users.isEmpty() && p1 == null && p2 == null;
    }

    public synchronized void enterUser(Player player) {
        addUser(player.getSession(), null);

        if (p1 == null) {
            p1 = player;
            p1.sendMessage("INFO:상대방을 기다리는 중... (제한시간: " + timeLimit + "초)");
            return;
        }

        if (p2 == null) {
            p2 = player;
            startGame();
            return;
        }

        player.sendMessage("ERROR:방이 꽉 찼습니다.");
        try { player.getSession().close(); } catch(Exception e) {}
    }

    public synchronized void exitUser(Session session) {
        removeUser(session);

        if (timerTask != null) timerTask.cancel(true);

        Player survivor = null;

        if (p1 != null && p1.getSession() == session) {
            survivor = p2;
            p1 = null;
        } else if (p2 != null && p2.getSession() == session) {
            survivor = p1;
            p2 = null;
        }

        if (survivor != null) { //상대 나감
            survivor.sendMessage("WIN:OPPONENT_LEFT");
            winnerStone = survivor.getStone(); 
            gameOver=true; //(3) 채원 구현:: 상대방 나감 
        }
    }

    private void startGame() {
        if (p1 == null || p2 == null) return;

        Random random = new Random();
        if (random.nextBoolean()) {
            blackPlayer = p1; whitePlayer = p2;
        } else {
            blackPlayer = p2; whitePlayer = p1;
        }

        String p1Name = (String) p1.getSession().getUserProperties().get("name");
        String p2Name = (String) p2.getSession().getUserProperties().get("name");

        p1.sendMessage("OPPNAME:" + p2Name);
        p2.sendMessage("OPPNAME:" + p1Name);

        blackPlayer.setStone(1);
        whitePlayer.setStone(2);
        
        //가빈 돌 이미지 설정
        applyStoneImage(blackPlayer);
        applyStoneImage(whitePlayer);

        blackPlayer.sendMessage("START:BLACK");
        whitePlayer.sendMessage("START:WHITE");

        startTimer(blackPlayer);
    }

    private void startTimer(Player turnPlayer) {
        currentPlayer = turnPlayer;

        if (timerTask != null && !timerTask.isDone()) {
            timerTask.cancel(true);
        }

        broadcast("TIMER:" + timeLimit);

        timerTask = scheduler.schedule(() -> handleTimeOut(turnPlayer), timeLimit, TimeUnit.SECONDS);
    }

    private void handleTimeOut(Player loser) { //시간 초과
        String winnerColor = (loser == blackPlayer) ? "WHITE" : "BLACK";
        broadcast("WIN:" + winnerColor + " (시간초과)");
        winnerStone = (loser == blackPlayer) ? 2 : 1;
        gameOver=true; //(2) 채원 구현:: 시간 초과인 경우 
    }

    public void processMove(String msg, Session senderSession) {
        if (blackPlayer == null || whitePlayer == null) return;

        Player sender = (senderSession == blackPlayer.getSession()) ? blackPlayer : whitePlayer;

        if (sender != currentPlayer) {
            sender.sendMessage("INFO:지금은 상대방 차례입니다.");
            return;
        }

        try {
            String[] parts = msg.split(",");
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int myColor = sender.getStone();

            int result = rule.putStone(x, y, myColor);
            //가빈 
            String img = sender.getImg();

            
            if (result == 0) {
            	broadcast("PUT:" + x + "," + y + "," + myColor + "," + img);	//가빈 myColor 뒤에 +부터 추가
                Player nextPlayer = (currentPlayer == blackPlayer) ? whitePlayer : blackPlayer;
                startTimer(nextPlayer);

            } else if (result == 100) { //오목승리 
                if (timerTask != null) timerTask.cancel(true);
                broadcast("PUT:" + x + "," + y + "," + myColor + "," + img); //가빈 myColor 뒤에 +부터 추가
                broadcast("WIN:" + (myColor == 1 ? "BLACK" : "WHITE"));
                
                winnerStone = (myColor == 1 ? 1 : 2);
                gameOver=true; //(1) 채원 구현:: 오목 승리  

            } else if (result == -1) {
                sender.sendMessage("INFO:이미 돌이 있거나 범위를 벗어났습니다.");
            } else if (result == -2) {
                sender.sendMessage("INFO:니 차례가 아닙니다.");
            }

        } catch (Exception e) {
            // 메시지가 좌표 형식이 아니면 무시(확장 가능)
        }
    }

    public void broadcast(String msg) {
        if (p1 != null) p1.sendMessage(msg);
        if (p2 != null) p2.sendMessage(msg);
    }
    
    //게임 리셋 : 재대결 (채원)
    public synchronized void resetGame() {
        rule.reset();                 // 오목판 초기화
        currentPlayer = blackPlayer;  // 흑 선공
        startTimer(blackPlayer);
        gameOver = false;
        winnerStone = 0;
        broadcast("RESET");
        
        //가빈 재대결 시 오목 이미지 변경
        applyStoneImage(blackPlayer);
        applyStoneImage(whitePlayer);
    }
    
  //가빈 돌 이미지 정하기 함수
    public void applyStoneImage(Player player) {
	    int styleIdx = player.getStoneStyle() - 1;
	    
	    //게스트일 경우 돌 스타일이 없으니까 -1이 됨
	    if (styleIdx < 0 || styleIdx >= Image.STONE_IMAGE_LIST.size()) {
	        styleIdx = 0;
	    }
	    
	    String[] images = Image.STONE_IMAGE_LIST.get(styleIdx);

	    String img = (player.getStone() == 1)
	            ? images[0]   // black
	            : images[1];  // white

	    player.setImg(img);
	}
    
   
}

