package login;

import lombok.Data;

@Data
public class UsersVO {
    private Long id;
    private String email;
    private String pwd; // DB에서 읽어온 해시(pwd)
    private String name;
    private String img;
    
    
    private int stoneStyle;
    private int score;
}
