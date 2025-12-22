package mypage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * MyPage 공통 유틸 (최종본)
 * - 문자열 trim
 * - int 파싱
 * - 닉네임 저장 가능 여부(중복체크 상태) 검증
 * - 프로필 이미지 저장(assets/profile) + 경로 반환
 * - 세션 갱신
 *
 * ※ 서블릿을 가볍게 만들기 위해 "부가 기능"을 여기에 몰아넣는 버전
 */
public final class MyPageUtil {

    private MyPageUtil() {}

    // =========================
    // 1) 기본 유틸
    // =========================
    public static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    public static int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    // =========================
    // 2) 닉네임 저장 가능 여부(서버 최종 검증)
    // =========================
    /**
     * 저장 시 서버에서 최종 검증:
     * - 현재 닉네임 그대로면 OK
     * - 바뀐 닉네임이면:
     *   session[checkedKey] == inputName AND session[okKey] == true 여야 OK
     */
    public static boolean canSaveNickname(
            HttpSession session,
            String inputName,
            String currentName,
            String checkedKey,
            String okKey
    ) {
        if (session == null) return false;
        if (inputName == null) return false;

        // 현재 닉네임 그대로면 중복체크 없이 OK
        if (currentName != null && currentName.equals(inputName)) return true;

        Object checked = session.getAttribute(checkedKey);
        Object okObj = session.getAttribute(okKey);

        boolean ok = (okObj instanceof Boolean) && ((Boolean) okObj);
        return ok && checked != null && inputName.equals(checked.toString());
    }

    // =========================
    // 3) 프로필 이미지 업로드/저장
    // =========================
    /**
     * 프로필 파일이 있으면 저장하고(assets/profile), 없으면 oldImg 그대로 반환
     *
     * @param filePart   request.getPart("profileFile")
     * @param ctx        getServletContext()
     * @param oldImg     세션에 있던 기존 이미지 웹 경로
     * @param defaultImg 기본 이미지 웹 경로
     * @param webDir     "/assets/profile" (웹 경로 == 저장 폴더)
     * @return 새 이미지 웹 경로 (예: "/assets/profile/uuid.png") 또는 fallback(old/default)
     *
     * @throws IllegalArgumentException 이미지가 아니거나 확장자 불가
     * @throws IllegalStateException    getRealPath가 null인 서버 설정
     * @throws IOException              실제 파일 쓰기 실패
     */
    public static String storeProfileImageIfPresent(
            Part filePart,
            ServletContext ctx,
            String oldImg,
            String defaultImg,
            String webDir
    ) throws IOException {

        String fallback = (oldImg == null || oldImg.isBlank()) ? defaultImg : oldImg;

        // 업로드 없음 -> 기존 유지
        if (filePart == null || filePart.getSize() <= 0) return fallback;

        // content-type 1차 방어
        if (!isImagePart(filePart)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // 파일명/확장자 체크
        String submitted = extractSubmittedFileName(filePart);
        String ext = safeImageExt(submitted);
        if (ext == null) {
            throw new IllegalArgumentException("지원하지 않는 이미지 확장자입니다. (png/jpg/jpeg/gif/webp)");
        }

        // 저장 폴더 실경로
        String saveDirRealPath = (ctx != null) ? ctx.getRealPath(webDir) : null;
        if (saveDirRealPath == null) {
            throw new IllegalStateException("서버 저장 경로를 찾을 수 없습니다. (getRealPath=null)");
        }

        File dir = new File(saveDirRealPath);
        if (!dir.exists()) dir.mkdirs();

        // 저장 파일명 생성
        String saveFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File target = new File(dir, saveFileName);

        // 실제 저장
        filePart.write(target.getAbsolutePath());

        // DB에는 웹 경로로 저장
        return webDir + "/" + saveFileName;
    }

    private static boolean isImagePart(Part p) {
        if (p == null) return false;
        String ct = p.getContentType();
        return ct != null && ct.toLowerCase().startsWith("image/");
    }

    private static String extractSubmittedFileName(Part part) {
        // content-disposition: form-data; name="profileFile"; filename="a.png"
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

    private static String safeImageExt(String filename) {
        if (filename == null) return null;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return null;

        String ext = filename.substring(dot).toLowerCase(); // ".png"
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

    // =========================
    // 4) 세션 갱신
    // =========================
    public static void updateUserSession(HttpSession session, String name, int stoneStyle, String imgPath) {
        if (session == null) return;
        session.setAttribute("userName", name);
        session.setAttribute("userStoneStyle", stoneStyle);
        session.setAttribute("userImg", imgPath);
    }
}
