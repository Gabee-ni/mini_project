<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Login</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/omok-ui.css">
</head>
<body>
  <!-- wavy border filter -->
<svg width="0" height="0" style="position:absolute">
  <filter id="wavyEdge">
    <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" seed="2" result="noise"/>
    <feDisplacementMap in="SourceGraphic" in2="noise" scale="2" xChannelSelector="R" yChannelSelector="G"/>
  </filter>
</svg>
<%
  String error = request.getParameter("error");
  String serverEmailMsg = null;
  String serverPwdMsg = null;

  if ("EMAIL_EMPTY".equals(error)) serverEmailMsg = "이메일을 입력하세요.";
  else if ("PASSWORD_EMPTY".equals(error)) serverPwdMsg = "비밀번호를 입력하세요.";
  else if ("EMAIL_INVALID_FORMAT".equals(error)) serverEmailMsg = "이메일 형식이 올바르지 않습니다.";
  else if ("NO_USER".equals(error)) serverEmailMsg = "아이디(이메일)가 존재하지 않습니다.";
  else if ("WRONG_PASSWORD".equals(error)) serverPwdMsg = "비밀번호가 틀렸습니다.";
%>

  <div class="bg-screen" style="background-image:url('<%=request.getContextPath()%>/assets/img/bg/start.png');">
    <div class="dim-layer"></div>
    <a class="back-btn" href="<%=request.getContextPath()%>/first">‹</a>

    <div class="paper-panel">
      <div class="card">
        <h1 class="title-en">LOGIN</h1>

        <form id="loginForm" action="<%=request.getContextPath()%>/login" method="post" novalidate>
          <input class="input" type="text" name="email" id="email" placeholder="아이디(이메일)" autocomplete="username">
          <div class="err" id="emailErr"></div>

          <input class="input" type="password" name="pwd" id="pwd" placeholder="패스워드" autocomplete="current-password">
          <div class="err" id="pwdErr"></div>

          <button class="btn btn-green" type="submit">LOGIN</button>
        </form>

        <a class="link" href="<%=request.getContextPath()%>/signup.jsp">회원가입</a>
      </div>
    </div>
  </div>

  <script>
    const form = document.getElementById("loginForm");
    const email = document.getElementById("email");
    const pwd = document.getElementById("pwd");
    const emailErr = document.getElementById("emailErr");
    const pwdErr = document.getElementById("pwdErr");

    function setErr(el, box, msg) {
      box.textContent = msg || "";
      if (msg) el.classList.add("input-error");
      else el.classList.remove("input-error");
    }

    function clearAll() {
      setErr(email, emailErr, "");
      setErr(pwd, pwdErr, "");
    }

    // 서버에서 redirect로 돌아온 메시지 표시
    const serverEmailMsg = "<%= (serverEmailMsg == null) ? "" : serverEmailMsg %>";
    const serverPwdMsg   = "<%= (serverPwdMsg == null) ? "" : serverPwdMsg %>";

    if (serverEmailMsg) setErr(email, emailErr, serverEmailMsg);
    if (serverPwdMsg)   setErr(pwd, pwdErr, serverPwdMsg);

    // 빈칸 제출 방지
    form.addEventListener("submit", (e) => {
      clearAll();

      const emailVal = email.value.trim();
      const pwdVal = pwd.value;

      let ok = true;

      if (!emailVal) {
        setErr(email, emailErr, "이메일을 입력하세요.");
        email.focus();
        ok = false;
      }

      if (ok && !pwdVal) {
        setErr(pwd, pwdErr, "비밀번호를 입력하세요.");
        pwd.focus();
        ok = false;
      }

      if (!ok) e.preventDefault();
    });

    email.addEventListener("input", () => setErr(email, emailErr, ""));
    pwd.addEventListener("input", () => setErr(pwd, pwdErr, ""));
  </script>


</body>
</html>
