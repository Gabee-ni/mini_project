package signup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import domain.User;
import login.UsersVO;
import util.Enum;

@WebServlet("/signup")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class SignupServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_IMG_PATH = "/assets/profile/default.jpeg";
    private static final int DEFAULT_STONE = 1;
    private static final int DEFAULT_SCORE = 0;

    private final SignupService signupService = new SignupService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");

        String email = trim(request.getParameter("email"));
        String pwd   = request.getParameter("pwd");
        String name  = trim(request.getParameter("name"));

        String stoneParam = trim(request.getParameter("stoneStyle"));
        int stone = parseIntOrDefault(stoneParam, DEFAULT_STONE);

        // ✅✅✅ [추가] 가입 직전에 "중복체크 세션값" 최종 검증 (mypage 방식)
        {
            HttpSession s = request.getSession(false);

            String checkedEmail = (s != null) ? (String) s.getAttribute(CheckEmailServlet.S_CHECKED_EMAIL) : null;
            Boolean emailOkObj  = (s != null) ? (Boolean) s.getAttribute(CheckEmailServlet.S_EMAIL_OK) : null;
            boolean emailOk = (emailOkObj != null && emailOkObj);

            String checkedName  = (s != null) ? (String) s.getAttribute(CheckNameServlet.S_CHECKED_NAME) : null;
            Boolean nameOkObj   = (s != null) ? (Boolean) s.getAttribute(CheckNameServlet.S_NAME_OK) : null;
            boolean nameOk = (nameOkObj != null && nameOkObj);

            if (email == null || !emailOk || checkedEmail == null || !email.equals(checkedEmail)) {
                request.setAttribute("serverEmailMsg", "이메일 중복체크를 완료하세요.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
                return;
            }
            if (name == null || !nameOk || checkedName == null || !name.equals(checkedName)) {
                request.setAttribute("serverNameMsg", "닉네임 중복체크를 완료하세요.");
                request.getRequestDispatcher("/signup.jsp").forward(request, response);
                return;
            }
        }

        // ✅ 이미지 업로드(선택)
        String imgPath = DEFAULT_IMG_PATH;
        Part imgPart = request.getPart("img");
        if (imgPart != null && imgPart.getSize() > 0) {
            imgPath = saveProfileImage(imgPart);
        }

        UsersVO vo = new UsersVO();
        vo.setEmail(email);
        vo.setName(name);
        vo.setImg(imgPath);
        vo.setStoneStyle(stone);
        vo.setScore(DEFAULT_SCORE);

        Enum code = signupService.register(vo, pwd);

        if (code != Enum.OK) {
            forwardFail(request, response, code);
            return;
        }

        // 성공: 자동 로그인 세션(user 1개)
        HttpSession session = request.getSession(true);

        User su = new User();
        su.setUserId(String.valueOf(vo.getId()));
        su.setEmail(vo.getEmail());
        su.setNickname(vo.getName());

        String avatar = vo.getImg();
        if (avatar == null || avatar.trim().isEmpty()) {
            avatar = "/img/default-avatar.jpg";
        }
        su.setAvatar(avatar);
        su.setImg(avatar);

        su.setStone_style(vo.getStoneStyle());
        su.setScore(vo.getScore());
        su.setIs_guest("N");

        session.setAttribute("user", su);

        response.sendRedirect(request.getContextPath() + "/roomList.jsp");
    }

    private void forwardFail(HttpServletRequest request, HttpServletResponse response, Enum code)
            throws ServletException, IOException {
        request.setAttribute("emailValue", request.getParameter("email"));
        request.setAttribute("nameValue", request.getParameter("name"));

        String c = code.name();
        if (c.startsWith("EMAIL")) request.setAttribute("serverEmailMsg", c);
        else if (c.startsWith("PASSWORD")) request.setAttribute("serverPwdMsg", c);
        else if (c.startsWith("NAME")) request.setAttribute("serverNameMsg", c);
        else request.setAttribute("serverCommonMsg", c);

        request.getRequestDispatcher("/signup.jsp").forward(request, response);
    }

    private String saveProfileImage(Part imgPart) throws IOException {
        Set<String> allowedExt = new HashSet<>();
        allowedExt.add("png");
        allowedExt.add("jpg");
        allowedExt.add("jpeg");
        allowedExt.add("webp");
        allowedExt.add("jfif");

        String submitted = imgPart.getSubmittedFileName();
        String filename = Paths.get(submitted).getFileName().toString();

        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length() - 1) {
            ext = filename.substring(dot + 1).toLowerCase();
        }
        if (!allowedExt.contains(ext)) return DEFAULT_IMG_PATH;

        String newName = UUID.randomUUID() + "." + ext;

        String realDir = getServletContext().getRealPath("/assets/profile");
        File dir = new File(realDir);
        if (!dir.exists()) dir.mkdirs();

        imgPart.write(new File(dir, newName).getAbsolutePath());
        return "/assets/profile/" + newName;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return def; }
    }
}
