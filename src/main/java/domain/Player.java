package domain;

import java.io.IOException;

import javax.websocket.Session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    
    // 1. 여기가 핵심! String이 아니라 Session 객체를 담아야 합니다.
    private Session session; 
    
    private String userId;
    private String nickname;
    private String avatar;
    private int score;
    private int stone;   // BLACK:1 / WHITE:2
    private int stoneStyle; // 돌 모양 등
    private String img; //가빈 수정

    // 2. 생성자 (입장할 때 필수 정보만 받아서 생성)
    public Player(Session session, String nickname) {
        this.session = session;
        this.nickname = nickname;
    }
    
    // 3. 편의 메서드: 이 플레이어에게 메시지 보내기
    // (이제 session.getBasicRemote()... 안 써도 됨!)
    public void sendMessage(String msg) {
        try {
            this.session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}