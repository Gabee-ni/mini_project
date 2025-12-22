package mypage;

import java.io.File;
import java.io.IOException;
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

@WebServlet("/mypage")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class MyPageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MyPageDAO dao = new MyPageDAO();

    private static final String S_CHECKED_NAME = "mypageCheckedName";
    private static final String S_NAME_OK = "mypageNameOk";

    private static final String DEFAULT_IMG = "/assets/profile/default.png";
    private static final String HOME_PATH = "/roomList.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // ✅✅✅ 세션 읽기(수정): 캐스팅 안전하게
        HttpSession session = request.getSession(false);
        User loginUser = null;
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof User) loginUser = (User) obj;
        }

        // ✅ 로그인 유저만 마이페이지 허용 (게스트 차단)
        if (loginUser == null || isGuest(loginUser)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 현재 닉네임은 검증된 상태로 초기화
        String currentName = safe(loginUser.getNickname());
        session.setAttribute(S_CHECKED_NAME, currentName);
        session.setAttribute(S_NAME_OK, Boolean.TRUE);

        request.setAttribute("checkedName", currentName);
        request.setAttribute("nameOk", Boolean.TRUE);
        request.setAttribute("msg", null);

        request.getRequestDispatcher("/mypage.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = trim(request.getParameter("action"));
        if (action == null) {
            doGet(request, response);
            return;
        }

        switch (action) {
            case "checkName":
                handleCheckName(request, response);
                break;
            case "save":
                handleSave(request, response);
                break;
            default:
                doGet(request, response);
        }
    }

    private void handleCheckName(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ✅✅✅ 세션 읽기(수정): 캐스팅 안전하게
        HttpSession session = request.getSession(false);
        User loginUser = null;
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof User) loginUser = (User) obj;
        }

        if (loginUser == null || isGuest(loginUser)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        long userId = Long.parseLong(loginUser.getUserId()); // 로그인 유저만 들어오므로 안전

        String inputName = trim(request.getParameter("name"));
        if (inputName == null) {
            request.setAttribute("checkedName", "");
            request.setAttribute("nameOk", Boolean.FALSE);
            request.setAttribute("msg", "닉네임을 입력하세요.");
            request.getRequestDispatcher("/mypage.jsp").forward(request, response);
            return;
        }

        String currentName = safe(loginUser.getNickname());
        if (currentName.equals(inputName)) {
            session.setAttribute(S_CHECKED_NAME, inputName);
            session.setAttribute(S_NAME_OK, Boolean.TRUE);

            request.setAttribute("checkedName", inputName);
            request.setAttribute("nameOk", Boolean.TRUE);
            request.setAttribute("msg", "현재 닉네임입니다. 사용 가능합니다.");
            request.getRequestDispatcher("/mypage.jsp").forward(request, response);
            return;
        }

        boolean duplicated = dao.existsName(inputName, userId);
        boolean ok = !duplicated;

        session.setAttribute(S_CHECKED_NAME, inputName);
        session.setAttribute(S_NAME_OK, ok);

        request.setAttribute("checkedName", inputName);
        request.setAttribute("nameOk", ok);
        request.setAttribute("msg", ok ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.");
        request.getRequestDispatcher("/mypage.jsp").forward(request, response);
    }

    private void handleSave(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ✅✅✅ 세션 읽기(수정): 캐스팅 안전하게
        HttpSession session = request.getSession(false);
        User loginUser = null;
        if (session != null) {
            Object obj = session.getAttribute("user");
            if (obj instanceof User) loginUser = (User) obj;
        }

        if (loginUser == null || isGuest(loginUser)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        long userId = Long.parseLong(loginUser.getUserId());

        String inputName = trim(request.getParameter("name"));
        int stoneStyle = parseIntOrDefault(request.getParameter("stoneStyle"), 1);

        if (inputName == null) {
            failForward(request, response, "닉네임을 입력하세요.");
            return;
        }

        // ✅ 중복체크 검증(서버 최종)
        String checkedName = (String) session.getAttribute(S_CHECKED_NAME);
        Boolean okObj = (Boolean) session.getAttribute(S_NAME_OK);
        boolean nameOk = (okObj != null && okObj);

        String currentName = safe(loginUser.getNickname());
        boolean isSameAsCurrent = currentName.equals(inputName);

        if (!isSameAsCurrent) {
            if (!nameOk || checkedName == null || !checkedName.equals(inputName)) {
                failForward(request, response, "닉네임 중복체크를 다시 해주세요.");
                return;
            }
            if (dao.existsName(inputName, userId)) {
                session.setAttribute(S_NAME_OK, Boolean.FALSE);
                failForward(request, response, "이미 사용 중인 닉네임입니다. 다시 중복체크 해주세요.");
                return;
            }
        }

        // ✅ 이미지 업로드 (없으면 기존 유지)
        String oldImg = loginUser.getAvatar();
        if (oldImg == null || oldImg.isBlank()) oldImg = DEFAULT_IMG;

        String newImgPath = oldImg;

        Part filePart = null;
        try { filePart = request.getPart("profileFile"); } catch (Exception ignore) {}

        if (filePart != null && filePart.getSize() > 0) {
            String contentType = filePart.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                failForward(request, response, "이미지 파일만 업로드 가능합니다.");
                return;
            }

            String submitted = getSubmittedFileName(filePart);
            String ext = getSafeExt(submitted);
            if (ext == null) {
                failForward(request, response, "지원하지 않는 이미지 확장자입니다. (png/jpg/jpeg/gif/webp)");
                return;
            }

            String saveDirRealPath = getServletContext().getRealPath("/assets/profile");
            if (saveDirRealPath == null) {
                failForward(request, response, "서버 저장 경로를 찾을 수 없습니다. (getRealPath=null)");
                return;
            }

            File dir = new File(saveDirRealPath);
            if (!dir.exists()) dir.mkdirs();

            String saveFileName = UUID.randomUUID().toString().replace("-", "") + ext;
            File target = new File(dir, saveFileName);
            filePart.write(target.getAbsolutePath());

            newImgPath = "/assets/profile/" + saveFileName;
        }

        // ✅ DB 업데이트
        boolean updated = dao.updateProfile(userId, inputName, stoneStyle, newImgPath);
        if (!updated) {
            failForward(request, response, "DB 저장에 실패했습니다. (update 0 row)");
            return;
        }

        // ✅ 세션의 user 객체만 갱신 (낱개 세션키 없음)
        loginUser.setNickname(inputName);
        loginUser.setStone_style(stoneStyle);
        loginUser.setAvatar(newImgPath);
        loginUser.setImg(newImgPath);

        // ✅✅✅ 세션에 다시 넣기(수정): 안전하게 반영
        session.setAttribute("user", loginUser);

        // 중복체크 상태도 최신화
        session.setAttribute(S_CHECKED_NAME, inputName);
        session.setAttribute(S_NAME_OK, Boolean.TRUE);

        goHomeAndClose(response, request.getContextPath() + HOME_PATH);
    }

    private boolean isGuest(User u) {
        if (u == null) return true;
        String nick = u.getNickname();
        if (nick != null && nick.startsWith("게스트")) return true;
        String g = u.getIs_guest();
        return (g != null && (g.equalsIgnoreCase("Y") || g.equalsIgnoreCase("true")));
    }

    private void failForward(HttpServletRequest request, HttpServletResponse response, String msg)
            throws ServletException, IOException {

        request.setAttribute("msg", msg);

        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute("checkedName", session.getAttribute(S_CHECKED_NAME));
            request.setAttribute("nameOk", session.getAttribute(S_NAME_OK));
        }

        request.getRequestDispatcher("/mypage.jsp").forward(request, response);
    }

    private void goHomeAndClose(HttpServletResponse response, String homeUrl) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        String safeUrl = homeUrl.replace("\"", "");
        response.getWriter().write(
                "<!doctype html><html><head><meta charset='UTF-8'></head><body>" +
                "<script>" +
                "try{ if(window.opener){ window.opener.location.href=\"" + safeUrl + "\"; window.close(); }" +
                "else{ location.href=\"" + safeUrl + "\"; } }catch(e){ location.href=\"" + safeUrl + "\"; }" +
                "</script>" +
                "</body></html>"
        );
    }

    private String safe(String s) { return (s == null) ? "" : s; }

    private String trim(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename=")) {
                String name = token.substring("filename=".length()).trim().replace("\"", "");
                int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return (slash >= 0) ? name.substring(slash + 1) : name;
            }
        }
        return null;
    }

    private String getSafeExt(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;
        String ext = filename.substring(dot).toLowerCase();
        switch (ext) {
            case ".png":
            case ".jpg":
            case ".jpeg":
            case ".gif":
            case ".webp":
                return ext;
            default:
                return null;
        }
    }
}
