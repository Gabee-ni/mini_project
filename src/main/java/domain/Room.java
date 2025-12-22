package domain;

import lombok.Data;

@Data
public class Room {
    private String roomId;
    private String title;
    private User blackPlayer;
    private User whitePlayer;
    private String mode;        //30s, 60s 異붽�
    private String[][] board; // 15x15
    private String currentTurn;
    private Boolean gameStatus; //게임중 - true , 대기중 false
    
    
}
