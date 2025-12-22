package signup;

import login.UsersDAO;
import login.UsersVO;
import util.Enum;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;

public class SignupService {

    private final UsersDAO usersDAO = new UsersDAO();

    public Enum register(UsersVO vo, String plainPwd) {
        // 입력 검증
        Enum v = ValidationUtil.validateSignup(vo.getEmail(), plainPwd, vo.getName(), vo.getStoneStyle());
        if (v != Enum.OK) return v;

        try {
            if (usersDAO.existsEmail(vo.getEmail().trim())) return Enum.EMAIL_DUPLICATE;
            if (usersDAO.existsName(vo.getName().trim()))   return Enum.NAME_DUPLICATE;

            vo.setPwd(PasswordUtil.hashPassword(plainPwd));

            long newId = usersDAO.insertUser(vo);
            vo.setId(newId);

            return Enum.OK;

        } catch (Exception e) {
            e.printStackTrace();   // ★ 이거 반드시
            if (e instanceof SQLException) {
                SQLException se = (SQLException) e;
                System.out.println("SQL ErrorCode = " + se.getErrorCode());
                System.out.println("SQL State = " + se.getSQLState());
            }
            return Enum.UNKNOWN_ERROR;
        }
    }
}
