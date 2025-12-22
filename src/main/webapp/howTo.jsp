<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="domain.User, java.util.UUID" %>
<%
    String ctx = request.getContextPath();
    User user = (User) session.getAttribute("user");
    if (user == null) {
        user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setNickname("게스트-" + user.getUserId().substring(0, 4));
        user.setAvatar("/img/default-avatar.jpg");
        session.setAttribute("user", user);
    }
    if (user.getAvatar() == null) user.setAvatar("/img/default-avatar.jpg");
%>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>게임 방법</title>


<style>
* {
    box-sizing: border-box;
}

a,
a:visited,
a:hover,
a:active {
    color: inherit;
    text-decoration: none;
}

body {
  margin: 0;
  font-family: Arial, sans-serif;
  background:
    linear-gradient(rgba(225,225,225,0.7), rgba(225,225,225,0.7)),
    url("img/back.png");
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

/* 상단 바 */
.top-bar {
    width: 100%;
    background: #fff;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.logo {
    font-size: 24px;
    font-weight: 800;
    padding-left: 50px;
}
.menu ul {
    display: flex;
    list-style: none;
    padding: 0;
    margin: 0;
    gap: clamp(40px, 10vw, 300px);
    padding-right: 100px;
}
.menu li {
    cursor: pointer;
    font-weight: 600;
    font-size: 20px;
}
.menu li.active {
    color: #5483B9;
}

/* 게임 방법 흰색 박스 */
.how-box {
    background-color: #ffffff; /* 배경 */
    width: 70%;              /* 박스 너비 */
    max-width: 700px;
    margin: 40px auto;	/* 가운데 정렬 */
    min-height: 60vh;
    padding: 5vh 5vw;;        /* 내부 여백 (위아래 좌우) */
    border-radius: 30px;       /* 모서리를 아주 둥글게 */
    text-align: left;          /* 텍스트 왼쪽 정렬 */
}

/* 박스 제목 (게임 방법) */
.how-box h2 {
    font-size: 2.8rem;
    color: #ff7a00;
    text-shadow: 3px 3px 0 #ffd966;
    /* ✨ 살짝 통통 튀는 효과 */
    animation: bounce 1.5s infinite;
}
	
@keyframes bounce {
    50% { transform: translateY(-6px); }
}	

/* 규칙 리스트*/
.how-box h3 {
    font-size: 1.8rem;
    color: #444;
    margin-top: 2rem;
    margin-bottom: 1rem;
}

.how-box ol {
	padding-left: 30px;
    padding-left: 0;
}

.how-box ol li {
    font-size: 1.4rem; /* 약간 조정하여 가독성 높임 */
    line-height: 2.0;
    color: #333;
    font-weight: 600;
    font-family: 'Arial';
    letter-spacing: 1px;
}

.description {
    font-size: 1.2rem;
    color: #666;
    line-height: 1.6;
    margin-top: 10px;
    font-family: 'Arial';
}
	
/* 난이도 배지 */
.mode-container {
    display: flex;
    gap: 15px;
    margin-top: 10px;
}

.mode-badge {
    padding: 8px 15px;
    border-radius: 8px;
    font-weight: bold;
    font-size: 1.1rem;
    color: #fff;
}

.mode-badge.normal {
    background-color: #4a90e2; /* 파란색 (일반) */
}

.mode-badge.pro {
    background-color: #e94e77; /* 빨간색 (고수) */
}

/* 강조 텍스트 */
strong {
    color: #000;
    text-decoration: underline;
}
</style>

</head>
<body>
	<%-- 상단바 --%>
	<header class="top-bar">
	    <div class="logo">OMOK</div>
	
	    <nav class="menu">
	        <ul>
	            <li><a href="<%=ctx%>/roomList.jsp">HOME</a></li>
	            <li><a href="<%=ctx%>/ranking">RANK</a></li>
	            <li class="active"><a href="<%=ctx%>/howto">HOW</a></li>
	        </ul>
	    </nav>
	
	    <img
  	onclick = "popUpUser()"
    src="<%=ctx + user.getAvatar()%>"
    onerror="this.src='<%=ctx%>/img/default-avatar.jpg'"
    alt="avatar"
    width="36"
    height="36"
    style="cursor: pointer;"
  >
	</header>
	
	<div class="how-box">
	    <h2>🎮 게임 방법</h2>
	    <h3>오목 규칙</h3>
	    <ol>
	        <li>플레이어는 번갈아 돌⚫⚪을 둡니다</li>
	        <li>가로 / 세로 / 대각선 5목 완성 시 승리</li>
	        <li>상대 턴에는 돌을 둘 수 없습니다</li>
	        <li>승리 시 점수가 랭킹에 반영됩니다</li>
	    </ol>
		<div class="info-section">
		        <h3>⏱️ 난이도 선택</h3>
		        <div class="mode-container">
		            <span class="mode-badge normal">60초 모드 (일반)</span>
		            <span class="mode-badge pro">30초 모드 (고수)</span>
	        	</div>
	        	<p class="description">제한 시간 내에 두지 못하면 패배할 수 있으니 주의하세요!</p>
	    </div>
	
	    <div class="info-section">
	        <h3>🏆 랭킹 시스템</h3>
	        <p class="description"><strong>회원가입 후</strong> 플레이 시 승리 점수가 기록됩니다. <br> 전 세계 유저들과 순위를 겨뤄보세요!</p>
	    </div>
	</div>
<script>
var ctxPath = "<%=ctx%>";
const userNickname = "<%= user.getNickname() %>";
function popUpUser() {
    const isGuest = userNickname.startsWith("게스트");

    const url = isGuest
        ? ctxPath + "/newUser.jsp"
        : ctxPath + "/User.jsp";

    window.open(
        url,
        "UserPopup",
        "width=1300,height=1100,resizable=no"
    );
}
</script>
</body>
</html>