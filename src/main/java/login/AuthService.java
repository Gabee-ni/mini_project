package login;

import util.PasswordUtil;

public class AuthService {

    private final UsersDAO usersDAO = new UsersDAO();

    // 성공: UsersVO 리턴 / 실패: null
    public UsersVO authenticate(String email, String plainPwd) throws Exception {
        UsersVO user = usersDAO.findByEmail(email);
        if (user == null) return null;

        boolean ok = PasswordUtil.verifyPassword(plainPwd, user.getPwd());
        return ok ? user : null;
    }
}
