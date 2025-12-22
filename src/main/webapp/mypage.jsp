<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="domain.User" %>
<%
    // ✅ 공통 파일명(너 프로젝트에 맞게 파일명만 유지)
    final String COMMON_CSS = "omok-ui.css";
    final String COMMON_JS  = "omok-ui.js";

    // ✅ 세션에서 user 객체 1개로 읽기
    User u = null;
    Object obj = session.getAttribute("user");
    if (obj instanceof User) u = (User) obj;

    if (u == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    // ✅ user 객체에서 화면에 쓸 값 추출
    String userId = u.getUserId();                       // String
    String userName = u.getNickname();                   // 닉네임
    Integer userStoneStyle = u.getStone_style();         // int or Integer (도메인에 따라 자동 boxing)
    String userImg = u.getAvatar();                      // 이미지 경로

    if (userStoneStyle == null) userStoneStyle = 1;
    if (userName == null) userName = "";
    if (userImg == null || userImg.trim().isEmpty()) userImg = "/omok/assets/profile/default.jpg";

    String imgSrc = userImg;
    if (!(imgSrc.startsWith("http://") || imgSrc.startsWith("https://"))) {
        imgSrc = request.getContextPath() + imgSrc;
    }

    // ====== (중복체크 결과) 서블릿이 forward로 다시 보내줄 때 쓰는 값 ======
    String checkedName = (String) request.getAttribute("checkedName");
    Boolean nameOkObj = (Boolean) request.getAttribute("nameOk");
    String msg = (String) request.getAttribute("msg");
    boolean nameOk = (nameOkObj != null) ? nameOkObj.booleanValue() : false;

    // ====== 화면에 표시할 현재 값들(중복체크 후에도 입력 유지) ======
    String currentName = request.getParameter("name");
    if (currentName == null || currentName.trim().isEmpty()) currentName = userName;

    // 돌 선택도 중복체크 후 유지되게: request 파라미터 우선
    int currentStone = userStoneStyle;
    try {
        String stoneParam = request.getParameter("stoneStyle");
        if (stoneParam != null && !stoneParam.isBlank()) currentStone = Integer.parseInt(stoneParam);
    } catch (Exception ignore) {}

    // 저장 버튼 활성 조건
    boolean isSameAsCurrent = userName.equals(currentName);
    boolean canSave = isSameAsCurrent || (nameOk && checkedName != null && checkedName.equals(currentName));

    String msgClass = "";
    if (msg != null) msgClass = nameOk ? "ok" : "bad";

    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MyPage</title>

<!-- ✅ 공통 CSS 1개 -->
<link rel="stylesheet" href="<%= ctx %>/assets/css/<%= COMMON_CSS %>">

<style>
  .check-msg.ok { color:#2e7d32; font-weight:900; }
  .check-msg.bad{ color:#c62828; font-weight:900; }
  .stone img{ width:56px; height:56px; opacity:.9; }
</style>
</head>

<body>
<div class="bg-screen">
  <div class="dim-layer"></div>

  <a href="#" class="back-btn" onclick="closeMyPage(); return false;">×</a>

  <div class="paper-panel">
    <div class="card">
      <div class="section-title">MY PAGE</div>

      <!-- ✅ 한 폼으로 통합: checkName/save 둘 다 이 폼으로 처리 -->
      <form id="mypageForm" method="post" action="<%= ctx %>/mypage" enctype="multipart/form-data">

        <input type="hidden" name="stoneStyle" id="stoneStyle" value="<%= currentStone %>">

        <!-- 프로필 -->
        <div class="profile-wrap">
          <img id="profilePreview" class="profile-img" src="<%= imgSrc %>" alt="profile">
          <button type="button" class="btn btn-chip" onclick="triggerFile()">사진 변경</button>
        </div>
        <input type="file" name="profileFile" id="profileFile" accept="image/*" style="display:none;">

        <!-- 닉네임 + 중복체크 -->
        <div class="form-row">
          <input class="input" type="text" name="name" id="nameInput"
                 value="<%= currentName %>" maxlength="20" autocomplete="off">
          <button class="btn btn-chip" type="submit" name="action" value="checkName">중복체크</button>
        </div>
        <div class="check-msg <%= msgClass %>"><%= (msg != null) ? msg : "" %></div>

        <!-- 돌 선택 -->
        <div style="margin-top:22px;">
          <div class="section-title" style="font-size:24px; margin-bottom:14px;">돌 선택</div>

          <div class="stone-grid">
            <div class="stone <%= (currentStone==1) ? "is-selected" : "" %>" data-stone="1">
              <img src="<%= ctx %>/img/stone/black/1.png" alt="s1">
            </div>
            <div class="stone <%= (currentStone==2) ? "is-selected" : "" %>" data-stone="2">
              <img src="<%= ctx %>/img/stone/black/2.png" alt="s2">
            </div>
            <div class="stone <%= (currentStone==3) ? "is-selected" : "" %>" data-stone="3">
              <img src="<%= ctx %>/img/stone/black/3.png" alt="s3">
            </div>
            <div class="stone <%= (currentStone==4) ? "is-selected" : "" %>" data-stone="4">
              <img src="<%= ctx %>/img/stone/black/4.png" alt="s4">
            </div>
          </div>
        </div>

        <!-- 저장 -->
        <button class="btn btn-green" id="saveBtn"
                type="submit" name="action" value="save" <%= canSave ? "" : "disabled" %>>
          저장하기
        </button>

      </form>
    </div>
  </div>
</div>

<!-- ✅ 공통 JS 1개 -->
<script src="<%= ctx %>/assets/js/<%= COMMON_JS %>"></script>

<script>
  function triggerFile(){
    var f = document.getElementById("profileFile");
    if (f) f.click();
  }

  function closeMyPage(){
    try{ if(window.opener){ window.close(); return; } }catch(e){}
    var el = document.querySelector(".bg-screen");
    if (el) el.style.display = "none";
    else history.back();
  }

  // 닉네임 바꾸면 저장 잠금(중복체크 다시 누르게)
  (function(){
    var nameInput = document.getElementById("nameInput");
    var saveBtn = document.getElementById("saveBtn");
    if (!nameInput || !saveBtn) return;
    nameInput.addEventListener("input", function(){ saveBtn.disabled = true; });
  })();
</script>

</body>
</html>
