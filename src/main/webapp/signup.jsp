<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
  // 서버에서 forwardFail로 내려준 메시지가 있으면 초기 메시지로 보여주기
  String initEmailMsg = (String) request.getAttribute("serverEmailMsg");
  String initNameMsg  = (String) request.getAttribute("serverNameMsg");

  String emailMsgText = (initEmailMsg != null) ? initEmailMsg : "중복체크 필요";
  String nameMsgText  = (initNameMsg  != null) ? initNameMsg  : "중복체크 필요";
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Sign Up</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/omok-ui.css">
  <style>
    /* 공통 CSS 건드리기 싫어서 signup.jsp에서만 최소 보정 */
    .check-msg.need { color:#777; font-weight:700; }
    .check-msg.ok   { color:green; font-weight:900; }
    .check-msg.bad  { color:red; font-weight:900; }
  </style>
</head>
<body>

  <div class="bg-screen" style="background-image:url('<%=request.getContextPath()%>/assets/img/bg/start.png');">
    <div class="dim-layer"></div>
    <a class="back-btn" href="<%=request.getContextPath()%>/login.jsp">‹</a>

    <div class="paper-panel">

      <form id="signupForm" action="<%=request.getContextPath()%>/signup"
            method="post" enctype="multipart/form-data">

        <div class="signup-grid">

          <!-- LEFT -->
          <section>
            <div class="profile-wrap">
              <img id="profilePreview" class="profile-img"
                   src="<%=request.getContextPath()%>/assets/profile/default.png" alt="profile">
              <label class="btn btn-chip" for="profileFile">사진 변경</label>
              <input id="profileFile" type="file" name="img" accept="image/*" hidden>
            </div>

            <div class="form-row">
              <input class="input" type="text" id="email" name="email" placeholder="이메일"
                     value="<%= (request.getParameter("email") != null ? request.getParameter("email") : "") %>">
              <button class="btn btn-chip" type="button" id="btnCheckEmail">중복체크</button>
            </div>
            <div class="check-msg <%= (initEmailMsg!=null ? "bad" : "need") %>" id="emailMsg"><%= emailMsgText %></div>

            <div class="form-row">
              <input class="input" type="text" id="name" name="name" placeholder="닉네임"
                     value="<%= (request.getParameter("name") != null ? request.getParameter("name") : "") %>">
              <button class="btn btn-chip" type="button" id="btnCheckName">중복체크</button>
            </div>
            <div class="check-msg <%= (initNameMsg!=null ? "bad" : "need") %>" id="nameMsg"><%= nameMsgText %></div>

            <div style="margin-top:14px;">
              <input class="input" type="password" id="pwd" name="pwd" placeholder="비밀번호">
            </div>

          </section>

          <!-- RIGHT -->
          <section>
            <div class="section-title">돌을 선택해주세요</div>

            <input type="hidden" id="stoneStyle" name="stoneStyle" value="1">

            <div class="stone-grid">
              <div class="stone is-selected" data-stone="1">1</div>
              <div class="stone" data-stone="2">2</div>
              <div class="stone" data-stone="3">3</div>
              <div class="stone" data-stone="4">4</div>
            </div>

            <div style="display:flex; justify-content:center;">
              <button class="btn btn-green signup-submit" type="submit" id="btnSubmit">SIGN UP</button>
            </div>
          </section>

        </div>
      </form>
    </div>

    <script src="<%=request.getContextPath()%>/assets/js/omok-ui.js"></script>

    <script>
      (() => {
        const ctx = "<%=request.getContextPath()%>";

        const emailInput = document.getElementById("email");
        const nameInput  = document.getElementById("name");
        const pwdInput   = document.getElementById("pwd");
        const emailMsg   = document.getElementById("emailMsg");
        const nameMsg    = document.getElementById("nameMsg");
        const form       = document.getElementById("signupForm");

        let emailOk = false;
        let nameOk  = false;

        function setMsg(el, state, msg) {
          el.textContent = msg || "";
          el.classList.remove("need","ok","bad");
          el.classList.add(state); // need / ok / bad
        }

        async function checkEmail() {
          const v = emailInput.value.trim();
          if(!v){
            emailOk = false;
            setMsg(emailMsg, "bad", "이메일을 입력하세요.");
            return;
          }

          setMsg(emailMsg, "need", "검사중...");
          try {
            const res = await fetch(ctx + "/checkEmail?email=" + encodeURIComponent(v), { cache: "no-store" });
            const data = await res.json();

            emailOk = !!data.available;
            if (emailOk) setMsg(emailMsg, "ok", "중복체크 완료");
            else setMsg(emailMsg, "bad", data.message || "이미 사용 중인 이메일입니다.");
          } catch (e) {
            emailOk = false;
            setMsg(emailMsg, "bad", "서버 오류(중복체크 실패)");
          }
        }

        async function checkName() {
          const v = nameInput.value.trim();
          if(!v){
            nameOk = false;
            setMsg(nameMsg, "bad", "닉네임을 입력하세요.");
            return;
          }

          setMsg(nameMsg, "need", "검사중...");
          try {
            const res = await fetch(ctx + "/checkName?name=" + encodeURIComponent(v), { cache: "no-store" });
            const data = await res.json();

            nameOk = !!data.available;
            if (nameOk) setMsg(nameMsg, "ok", "중복체크 완료");
            else setMsg(nameMsg, "bad", data.message || "이미 사용 중인 닉네임입니다.");
          } catch (e) {
            nameOk = false;
            setMsg(nameMsg, "bad", "서버 오류(중복체크 실패)");
          }
        }

        document.getElementById("btnCheckEmail").addEventListener("click", checkEmail);
        document.getElementById("btnCheckName").addEventListener("click", checkName);

        emailInput.addEventListener("input", () => {
          emailOk = false;
          setMsg(emailMsg, "need", "중복체크 필요");
        });

        nameInput.addEventListener("input", () => {
          nameOk = false;
          setMsg(nameMsg, "need", "중복체크 필요");
        });

        form.addEventListener("submit", (e) => {
          const emailV = emailInput.value.trim();
          const nameV  = nameInput.value.trim();
          const pwdV   = pwdInput.value;

          if(!emailV){
            setMsg(emailMsg, "bad", "이메일을 입력하세요.");
            emailInput.focus();
            e.preventDefault();
            return;
          }
          if(!nameV){
            setMsg(nameMsg, "bad", "닉네임을 입력하세요.");
            nameInput.focus();
            e.preventDefault();
            return;
          }
          if(!pwdV){
            alert("비밀번호를 입력하세요.");
            pwdInput.focus();
            e.preventDefault();
            return;
          }

          if(!emailOk){
            setMsg(emailMsg, "bad", "이메일 중복체크를 완료하세요.");
            e.preventDefault();
            return;
          }
          if(!nameOk){
            setMsg(nameMsg, "bad", "닉네임 중복체크를 완료하세요.");
            e.preventDefault();
            return;
          }
        });
      })();
    </script>

  </div>

</body>
</html>
