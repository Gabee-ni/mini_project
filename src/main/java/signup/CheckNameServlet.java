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

@WebServlet("/checkName")
public class CheckNameServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String S_CHECKED_NAME = "signupCheckedName";
    public static final String S_NAME_OK      = "signupNameOk";

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9가-힣_\\-\\.]+$");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");

        String name = request.getParameter("name");
        if (name != null) name = name.trim();

        boolean available = false;
        String message;

        try {
            if (name == null || name.isEmpty()) {
                message = "닉네임을 입력하세요.";
            } else if (!NAME_PATTERN.matcher(name).matches()) {
                message = "닉네임 형식이 올바르지 않습니다.";
            } else {
                UsersDAO usersDAO = new UsersDAO();

                if (usersDAO.existsName(name)) {
                    message = "이미 사용 중인 닉네임입니다.";
                } else {
                    available = true;
                    message = "사용 가능한 닉네임입니다.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "서버 오류";
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(S_CHECKED_NAME, name);
        session.setAttribute(S_NAME_OK, available);

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
