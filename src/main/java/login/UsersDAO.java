package login;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class UsersDAO {

    private DataSource dataFactory;

    public UsersDAO() {
        try {
            Context ctx = new InitialContext();
            Context envContext = (Context) ctx.lookup("java:comp/env");
            dataFactory = (DataSource) envContext.lookup("jdbc/omok"); // ✅ 네 기존 JNDI 유지
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================
    // 로그인: email로 유저 조회
    // =========================
    public UsersVO findByEmail(String email) throws Exception {
        String sql =
            "SELECT USER_ID, EMAIL, PWD, NAME, IMG, STONE_STYLE, SCORE " +
            "  FROM USERS " +
            " WHERE EMAIL = ?";

        try (Connection conn = dataFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                UsersVO vo = new UsersVO();
                vo.setId(rs.getLong("USER_ID"));
                vo.setEmail(rs.getString("EMAIL"));
                vo.setPwd(rs.getString("PWD"));
                vo.setName(rs.getString("NAME"));
                vo.setImg(rs.getString("IMG"));

                // ✅ stone/score는 int로 저장
                vo.setStoneStyle(rs.getInt("STONE_STYLE"));
                vo.setScore(rs.getInt("SCORE"));
                return vo;
            }
        }
    }

    // =========================
    // 중복 체크
    // =========================
    public boolean existsEmail(String email) throws Exception {
        String sql = "SELECT 1 FROM USERS WHERE EMAIL = ?";
        try (Connection conn = dataFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsName(String name) throws Exception {
        String sql = "SELECT 1 FROM USERS WHERE NAME = ?";
        try (Connection conn = dataFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =========================
    // 회원가입 INSERT (시퀀스 사용)
    // - PWD는 해시(pwdHash) 그대로 저장
    // - IMG는 경로 문자열 저장
    // - STONE_STYLE / SCORE는 VO에서 int로 들어온 값 그대로 저장
    // =========================
    public long insertUser(UsersVO vo) throws Exception {
        final String SEQ = "USER_SEQ";

        String insertSql =
            "INSERT INTO USERS (USER_ID, EMAIL, PWD, NAME, IMG, STONE_STYLE, SCORE, IS_GUEST) " +
            "VALUES (" + SEQ + ".NEXTVAL, ?, ?, ?, ?, ?, ?, 'N')";

        String currvalSql = "SELECT " + SEQ + ".CURRVAL FROM dual";

        try (Connection conn = dataFactory.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, vo.getEmail());
                ps.setString(2, vo.getPwd());
                ps.setString(3, vo.getName());
                ps.setString(4, vo.getImg());

                // ✅ int 그대로
                ps.setInt(5, vo.getStoneStyle());
                ps.setInt(6, vo.getScore());

                int r = ps.executeUpdate();
                System.out.println("insert result = " + r);

                conn.commit();   // ★ 이거 없으면 DB에 안 남음

            }

            long newId;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(currvalSql)) {
                rs.next();
                newId = rs.getLong(1);
            }

            conn.commit();
            return newId;

        } catch (SQLException e) {
            throw e;
        }
    }
}
