package signup;

import login.UsersDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/checkEmail")
public class CheckEmailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // ✅ 회원가입 중복체크 세션키
    public static final String S_CHECKED_EMAIL = "signupCheckedEmail";
    public static final String S_EMAIL_OK      = "signupEmailOk";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        String email = request.getParameter("email");
        if (email != null) email = email.trim();

        boolean available = false;
        String message;

        try {
            if (email == null || email.isEmpty()) {
                message = "이메일을 입력하세요.";
            } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                message = "이메일 형식(@ 포함)이 올바르지 않습니다.";
            } else {
                // ✅ 여기서 생성(서블릿 로딩 실패 방지)
                UsersDAO usersDAO = new UsersDAO();

                if (usersDAO.existsEmail(email)) {
                    message = "이미 사용 중인 이메일입니다.";
                } else {
                    available = true;
                    message = "사용 가능한 이메일입니다.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "서버 오류";
        }

        // ✅✅✅ 세션에 체크 결과 저장 (mypage 방식)
        HttpSession session = request.getSession(true);
        session.setAttribute(S_CHECKED_EMAIL, email);
        session.setAttribute(S_EMAIL_OK, available);

        try (PrintWriter out = response.getWriter()) {
            out.print("{\"available\":" + available +
                    ",\"message\":\"" + esc(message) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
