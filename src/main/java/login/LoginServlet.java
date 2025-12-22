package login;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import domain.User;
import util.Enum;
import util.ValidationUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");

        String email = trim(request.getParameter("email"));
        String pwd = trim(request.getParameter("pwd"));

        Enum result = ValidationUtil.validateLogin(email, pwd);
        if (result != Enum.OK) {
            response.sendRedirect("login.jsp?error=" + result.name());
            return;
        }

        try {
            UsersVO user = authService.authenticate(email, pwd);

            if (user == null) {
                forwardFail(request, response, "이메일 또는 비밀번호가 올바르지 않습니다.");
                return;
            }

            // 세션 고정 방지: 기존 세션 제거 후 새 세션
            HttpSession old = request.getSession(false);
            if (old != null) old.invalidate();

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30 * 60);

            // 세션에는 user 객체 1개만 저장 (pwd는 세팅하지 않아서 null)
            User su = new User();
            su.setUserId(String.valueOf(user.getId()));
            su.setEmail(user.getEmail());
            su.setNickname(user.getName());

            String avatar = user.getImg();
            if (avatar == null || avatar.trim().isEmpty()) {
                avatar = "/img/default-avatar.jpg";
            }
            su.setAvatar(avatar);
            su.setImg(avatar);

            su.setStone_style(user.getStoneStyle());
            su.setScore(user.getScore());
            su.setIs_guest("N");

            session.setAttribute("user", su);

            // ✅ 로그인 성공하면 roomList.jsp로 이동
            redirectSuccess(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            forwardFail(request, response, "서버 오류(DB/쿼리)를 확인하세요.");
        }
    }

    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private void forwardFail(HttpServletRequest request, HttpServletResponse response, String msg)
            throws ServletException, IOException {

        request.setAttribute("error", msg);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    // ✅✅✅ 변경: 결과 페이지 대신 roomList.jsp로 리다이렉트
    private void redirectSuccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/roomList.jsp");
    }
}
