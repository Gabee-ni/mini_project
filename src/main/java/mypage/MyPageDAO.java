package mypage;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 마이페이지 전용 DAO (JNDI/DataSource 방식)
 * - DB에는 IMG 컬럼에 "웹 경로 문자열"만 저장
 * - 실제 파일 저장은 Servlet에서 처리 (webapp/assets/profile)
 *
 * JNDI: java:comp/env/jdbc/omok
 */
public class MyPageDAO {

    private final DataSource dataFactory;

    public MyPageDAO() {
        try {
            Context ctx = new InitialContext();
            Context envContext = (Context) ctx.lookup("java:comp/env");
            dataFactory = (DataSource) envContext.lookup("jdbc/omok"); // ✅ 너 기존 JNDI 유지
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 닉네임(=NAME) 중복 체크 (본인 USER_ID 제외)
     * @return true = 중복 있음, false = 사용 가능
     */
    public boolean existsName(String name, Long myUserId) {
        String sql = "SELECT 1 FROM USERS WHERE NAME = ? AND USER_ID <> ?";

        try (Connection conn = dataFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setLong(2, myUserId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 마이페이지 저장(업데이트)
     * - NAME(닉네임), STONE_STYLE, IMG(웹 경로 문자열) 업데이트
     * @return true = 성공(1행 이상 수정), false = 실패(0행 수정)
     */
    public boolean updateProfile(Long userId, String name, int stoneStyle, String imgPath) {
        String sql = "UPDATE USERS SET NAME = ?, STONE_STYLE = ?, IMG = ? WHERE USER_ID = ?";

        try (Connection conn = dataFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, stoneStyle);
            ps.setString(3, imgPath);
            ps.setLong(4, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
