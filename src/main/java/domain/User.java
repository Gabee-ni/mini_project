package domain;

import java.util.Objects;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String nickname;
    private String avatar;
    private String pwd;
    private String is_guest;
    private int score;
    private int stone_style;
    private String img;
    private String email; // ->이건 뭐죠..? (호연: 이게 실제 로그인한 id가 된대!)
    
    // getter / setter ����

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    
    
}
