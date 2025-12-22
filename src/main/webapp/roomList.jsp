<%@ page isELIgnored="true" %>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>OMOK</title>

<style>
* { box-sizing: border-box; }
a, a:visited, a:hover, a:active { color: inherit; text-decoration: none; }

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

/* ===== top bar ===== */
.top-bar {
  width: 100%;
  background: #fff;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.logo { font-size: 24px; font-weight: 800; padding-left: 50px; }
.menu ul {
  display: flex; list-style: none; padding: 0; margin: 0;
  gap: clamp(40px, 10vw, 300px);
  padding-right: 100px;
}
.menu li { cursor: pointer; font-weight: 600; font-size: 20px; }
.menu li.active { color: #5483B9; }

/* ===== main container ===== */
.container {
  display: flex;
  flex-direction: column;
  width: 1000px;
  margin: 60px auto;
  background-color: #fff;
  border-radius: 15px;
  padding: 70px;
  padding-top: 130px;
}

/* ===== search box ===== */
.search-box {
  width: 580px;
  height: 237px;
  background: #fff;
  border-radius: 20px;
  padding: 10px 35px;
  text-align: left;
  margin: 0 auto 30px auto;
  border: 6px solid #D4D4D4;
}
.search-inner {
  display: flex;
  justify-content: center;
  gap: 5px;
  margin-top: 40px;
}
.search-inner input {
  width: 350px;
  height: 48px;
  border-radius: 14px;
  border: 6px solid #ddd;
  padding: 0 15px;
  font-size: 16px;
}
.search-inner button {
  height: 48px;
  padding: 0 24px;
  border: none;
  border-radius: 14px;
  background: #7CB342;
  color: #fff;
  font-weight: 800;
  font-size: 18px;
  cursor: pointer;
}

/* ===== ready list ===== */
.ready-box {
  width: 830px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 15px rgba(0,0,0,0.2);
  margin: 60px auto 0;
}
.ready-header {
  width: 830px;
  height: 67px;
  background: #7CB342;
  color: #fff;
  padding: 12px 20px;
  border-radius: 15px;
  font-size: 26px;
  font-weight: 800;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.addRoombtn {
  width: 50px; height: 45px;
  font-size: 36px; font-weight: 700;
  background: transparent;
  border: 4px solid #ffffff;
  border-radius: 15px;
  color: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.room-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin: 30px;
  min-height: 600px;
}
.room-card {
  height: 180px;
  border-radius: 15px;
  padding: 20px;
  border: 6px solid #85BE57;
  cursor: pointer;
  overflow: hidden;
}
.room-card.orange { border-color: #F0B061; }
.room-title { font-weight: 700; font-size: 24px; margin-bottom: 10px; }
.room-info {
  display: flex;
  font-size: 18px;
  font-weight: 500;
  justify-content: space-between;
  align-items: center;
  padding: 20px 10px 0;
  color: #3F3F3F;
}
.time { font-size: 50px; font-weight: 800; color: #DCDCDC; }

.empty-state { display: none; text-align: center; padding: 60px 0; }
@keyframes floatCat { 0%{transform:translateY(0)} 50%{transform:translateY(-10px)} 100%{transform:translateY(0)} }
.empty-state img { animation: floatCat 3s ease-in-out infinite; max-width: 200px; width: 80%; image-rendering: pixelated; }
.empty-state p { margin-top: 20px; font-size: 20px; font-weight: 700; color: #666; line-height: 1.4; }

.player-line {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.player-line .vs { margin: 0 6px; color: #999; font-weight: 700; }
</style>
</head>

<body>
<header class="top-bar">
  <div class="logo">OMOK</div>

  <nav class="menu">
    <ul>
      <li class="active"><a href="<%=ctx%>/roomList.jsp">HOME</a></li>
      <li><a href="<%=ctx%>/ranking">RANK</a></li>
      <li><a href="<%=ctx%>/howto">HOW</a></li>
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

<div class="container">
  <div class="search-box">
    <h1>SEARCH</h1>
    <div class="search-inner">
      <input type="text" id="searchInput" placeholder="SEARCH" onkeyup="searchRooms()"/>
      <button type="button" onclick="searchRooms()">JOIN</button>
    </div>
  </div>

  <div class="ready-box">
    <div class="ready-header">
      <span>READY</span>
      <span><button class="addRoombtn" onclick="openPopup()"> + </button></span>
    </div>

    <div id="emptyState" class="empty-state">
      <img src="<%=ctx%>/img/empty-room.png" alt="empty room">
      <p>아직 생성된 방이 없어요 <br>+ 버튼을 눌러 방을 만들어보세요!</p>
    </div>

    <div class="room-list" id="roomList"></div>
  </div>
</div>

<script>
  
  // 그래서 아래는 모두 문자열 + 연결 방식만 사용.

  var ctxPath = "<%=ctx%>";
  var wsBase = (location.protocol === "https:" ? "wss://" : "ws://") + location.host + ctxPath;
  const userNickname = "<%= user.getNickname() %>";
  
  var roomWs = new WebSocket(wsBase + "/room");
  var latestRooms = [];

  var roomListEl = document.getElementById("roomList");
  var emptyStateEl = document.getElementById("emptyState");
  var searchInputEl = document.getElementById("searchInput");

  function openPopup() {
    window.open(
      ctxPath + "/CreateRoomPopUp.jsp",
      "createRoomPopup",
      "width=1300,height=1100,resizable=no"
    );
  }
  
  function popUpUser() {
	  const isGuest = userNickname.startsWith("게스트");

	  // 게스트면 현재 창에서 회원가입 페이지로 이동 (새 창 X)
	  if (isGuest) {
	    location.href = ctxPath + "/signup.jsp";   // 또는 login.jsp로 바꿔도 됨
	    return;
	  }

	  // 로그인 유저면 마이페이지 팝업(서블릿)
	  const w = 980, h = 780;
	  const left = (screen.width - w) / 2;
	  const top  = (screen.height - h) / 2;

	  window.open(
	    ctxPath + "/mypage",
	    "mypagePopup",
	    "width=" + w + ",height=" + h + ",left=" + left + ",top=" + top + ",resizable=yes,scrollbars=yes"
	  );
	}

  roomWs.onmessage = function (e) {
    var raw = e.data;

    try {
      var data = JSON.parse(raw);

      // 1) 방 목록(배열)
      if (Array.isArray(data)) {
        latestRooms = data;
        renderRooms(filterRooms(latestRooms));
        return;
      }

      // 2) JOIN/CREATE 응답(객체)
      if (data && data.type) {
        if (data.type === "JOIN_OK") {
          var room = null;
          for (var i = 0; i < latestRooms.length; i++) {
            if (latestRooms[i].roomId === data.roomId) { room = latestRooms[i]; break; }
          }
          var time = 30;
          if (room && room.mode) {
            var digits = String(room.mode).replace(/[^0-9]/g, "");
            time = parseInt(digits, 10) || 30;
          }
          location.href = ctxPath + "/GameRoom.jsp?roomId=" + encodeURIComponent(data.roomId) + "&time=" + time;
          return;
        }

        if (data.type === "JOIN_DENY") {
          alert(data.reason || "이미 꽉 찬 방입니다.");
          return;
        }
      }
    } catch (err) {
      console.warn("roomWs parse fail:", err, raw);
    }
  };

  function joinRoom(roomId) {
    if (roomWs.readyState !== WebSocket.OPEN) {
      alert("서버 연결이 아직 안 됐어요. 잠깐 뒤 다시 시도!");
      return;
    }
    roomWs.send(JSON.stringify({ type: "JOIN_ROOM", roomId: roomId }));
  }

  function filterRooms(rooms) {
    var q = (searchInputEl && searchInputEl.value ? searchInputEl.value : "").trim().toLowerCase();
    if (!q) return rooms;
    return rooms.filter(function (r) {
      return String(r.title || "").toLowerCase().indexOf(q) >= 0;
    });
  }

  function renderRooms(rooms) {
    roomListEl.innerHTML = "";

    if (!rooms || rooms.length === 0) {
      emptyStateEl.style.display = "block";
      roomListEl.style.display = "none";
      return;
    }

    emptyStateEl.style.display = "none";
    roomListEl.style.display = "grid";

    for (var i = 0; i < rooms.length; i++) {
      var room = rooms[i];

      var blackName = (room.blackPlayer && room.blackPlayer.nickname) ? room.blackPlayer.nickname : "???";
      var whiteName = (room.whitePlayer && room.whitePlayer.nickname) ? room.whitePlayer.nickname : "대기중";
      var statusText = room.gameStatus ? "게임중" : "대기중";

      var card = document.createElement("div");
      card.className = "room-card" + (room.gameStatus ? " orange" : "");

      // ✅ 여기서 backtick + ${} 절대 사용 금지 (JSP EL 500)
      card.innerHTML =
        '<div class="room-title">' + escapeHtml(room.title || "") + '</div>' +
        '<div class="player-line">' + escapeHtml(blackName) +
          ' <span class="vs">VS</span> ' + escapeHtml(whiteName) + '</div>' +
        '<div class="room-info">' +
          '<div>' + escapeHtml(statusText) + '</div>' +
          '<div class="time">' + escapeHtml(room.mode || "") + '</div>' +
        '</div>';

      (function (rid) {
        card.onclick = function () { joinRoom(rid); };
      })(room.roomId);

      roomListEl.appendChild(card);
    }
  }

  function escapeHtml(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#039;"
      })[c];
    });
  }

  function searchRooms() {
    renderRooms(filterRooms(latestRooms));
  }
</script>

</body>
</html>

