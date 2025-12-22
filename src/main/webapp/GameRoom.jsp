<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="domain.User, java.util.UUID"%>
<%
String ctx = request.getContextPath();
String roomId = request.getParameter("roomId");
String timeParam = request.getParameter("time");
if (timeParam == null || timeParam.isEmpty())
	timeParam = "30";

User user = (User) session.getAttribute("user");
if (user == null) {
	user = new User();
	user.setUserId(UUID.randomUUID().toString());
	user.setNickname("게스트-" + user.getUserId().substring(0, 4));
	user.setAvatar("/img/default-avatar.jpg");
	session.setAttribute("user", user);
}
if (user.getAvatar() == null)
	user.setAvatar("/img/default-avatar.jpg");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Omok Game Room</title>

<style>
html, body {
	width: 100%;
	height: 100%;
	margin: 0;
	font-family: 'Malgun Gothic', sans-serif;
	overflow: hidden;
}

body {
	margin: 0;
	font-family: Arial, sans-serif;
	background: linear-gradient(rgba(225, 225, 225, 0.7),
		rgba(225, 225, 225, 0.7)), url("img/back.png");
	background-position: center;
	background-size: cover;
	background-repeat: no-repeat;
	background-attachment: fixed;
}

body::before {
	content: "";
	position: fixed;
	inset: 0;
	background: rgba(255, 255, 255, 0.4);
	z-index: -1;
}

#game-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100vw;
	height: 100vh;
}

#game-container {
	position: relative;
	width: 650px;
	margin-top: 20px;
}

canvas {
	background-color: #DCB35C;
	border: 3px solid #5A3A20;
}

.profile {
	position: absolute;
	width: 300px;
	background: rgba(255, 255, 255, 0.95);
	box-shadow: 0 8px 18px rgba(0, 0, 0, 0.15);
	border-radius: 15px;
	padding: 15px;
}

#profile-left {
	left: -350px;
	bottom: 0px;
}

#profile-right {
	right: -300px;
	top: 0px;
}

.nickname {
	font-size: 15px;
	font-weight: 800;
	margin-bottom: 4px;
}

#status {
	margin: 10px 0;
	font-weight: 700;
}

#timer {
	margin: 6px 0;
	font-size: 18px;
	font-weight: 800;
}

/* 채팅 CSS */
#my-profile {
	position: fixed;
	bottom: 30px;
	left: 30px;
}

#opponent-profile {
	position: fixed;
	top: 30px;
	right: 30px;
}

.my-profile .chat-bubble-area {
	bottom: 100%; /* 프로필 위 */
	flex-direction: column;
}

.my-profile .chat-bubble {
	align-self: flex-start;
	background: #dbeafe;
}

.opponent-profile .chat-bubble-area {
	top: 100%; /* 프로필 아래 */
	flex-direction: column-reverse;
}

.opponent-profile .chat-bubble {
	align-self: flex-end;
	background: #fff3bf;
}

.chat-bubble-area {
	position: absolute;
	width: 100%;
	display: flex;
	gap: 6px;
	pointer-events: none;
	margin: 10px;
}

.chat-bubble {
	max-width: 270px;
	padding: 8px 12px;
	border-radius: 14px;
	font-size: 20px;
	line-height: 1.3;
	word-break: break-word;
	animation: fadeUp 0.25s ease-out;
}
.chat-bubble img {
  width: 150px;
  height: 150px;
  display: block;
}


/* 내 말풍선 */
#my-bubble-area {
	align-items: flex-start;
}

.chat-bubble.me {
	left: 0;
	background: #dbeafe;
	align-self: flex-start;
	flex-direction: column;  
	animation: fadeUp 0.25s ease-out;
}

/* 상대 말풍선 */
#opponent-bubble-area {
	align-items: flex-end;
}

.chat-bubble.opponent {
	right: 0;
	background: #fff3bf;
	align-self: flex-end;
 	animation: fadeDown 0.25s ease-out;
 }

/*버튼*/
.chat-buttons {
	display: flex;
	gap: 6px;
	margin-top: 4px;
}
.chat-buttons img {
	width: 50px;
	height: 50px;
}

.chat-buttons button {
	border: none;
	background: #f1f3f5;
	border-radius: 8px;
	font-size: 18px;
	cursor: pointer;
	padding: 4px 6px;
	transition: background 0.15s;
}

.chat-buttons button:hover {
	background: #dee2e6;
}

/*입력란*/
.chat-input {
	display: flex;
	gap: 4px;
	margin-top: 6px;
}

.chat-input input {
	flex: 1;
	border-radius: 10px;
	border: 1px solid #ccc;
	padding: 6px 8px;
	font-size: 12px;
}

.chat-input button {
	border: none;
	border-radius: 10px;
	padding: 6px 10px;
	background: #4dabf7;
	color: white;
	font-size: 12px;
	cursor: pointer;
}

.chat-input button:hover {
	background: #339af0;
}

/* 숨김 처리 */
.chat-input.hidden {
	display: none;
}


/*애니메이션*/
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeDown {
  from { opacity: 1; transform: translateY(0); }
  to   { opacity: 0; transform: translateY(-10px); }
}


}
/*채원 구현 */
#game-result-overlay {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.55);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 9999;
}

.result-box {
	background: white;
	border-radius: 20px;
	padding: 30px;
	width: 420px;
	text-align: center;
}

.result-img {
	width: 180px;
	margin-bottom: 20px;
}

.result-line {
	font-size: 16px;
	margin: 6px 0;
}

.result-buttons {
	display: flex;
	justify-content: space-around;
	margin-top: 20px;
}

.result-btn {
	width: 120px;
	cursor: pointer;
}
</style>
</head>

<body>
	<div id="game-wrapper">
		<h2>
			오목 게임방 (ROOM: <span id="roomDisplay"><%=roomId%></span>)
		</h2>

		<button onclick="leaveRoom()">🚪 나가기</button>

		<div id="status">연결 중...</div>
		<div id="timer"></div>

		<div id="game-container">
			<canvas id="board" width="600" height="600"></canvas>
			<div id="profile-left" class="profile my-profile">
				<div class="chat-bubble-area" id="my-bubble-area"></div>
				<div class="nickname"><%=user.getNickname()%></div>
				<div>PLAYER 1</div>
	
				<div class="chat-buttons">
					<button onclick="sendEmoji(`/omok/img/emoji/emoji_cat1.png`)" ><img src="/omok/img/emoji/emoji_btn1.png"></button>
					<button onclick="sendEmoji(`/omok/img/emoji/emoji_cat2.png`)"><img src="/omok/img/emoji/emoji_btn2.png"></button>
					<button onclick="sendEmoji(`/omok/img/emoji/emoji_cat3.png`)"><img src="/omok/img/emoji/emoji_btn3.png"></button>
					<button onclick="toggleChatInput()">💬</button>
				</div>
	
				<div id="chat-input-box" class="chat-input hidden">
					<input type="text" id="chatText" placeholder="메시지 입력">
					<button onclick="sendText()">전송</button>
				</div>
			</div>
	
			<div id="profile-right" class="profile opponent-profile">
				<div class="nickname">상대방 대기중</div>
				<div>PLAYER 2</div>
				<div class="chat-bubble-area" id="opponent-bubble-area"></div>
			</div>
		</div>
	</div>

	<!-- (채원) 게임 끝났을 때 되서 보일 오버레이 (재대결 때문에 websocket이 끊기면 안되서 여기서 구현해야함 -->
	<div id="game-result-overlay" style="display: none;">
		<div class="result-box">
			<img id="result-image" src="" class="result-img">

			<div id="result-list"></div>

			<div class="result-buttons">
				<img src="NEXT_botton.png" onclick="rematch()" class="result-btn">
				<img src="NEXT_botton.png" onclick="leaveRoom()" class="result-btn">
			</div>
		</div>
	</div>

	<script>
  var ctxPath = "<%=ctx%>";
  var roomId = "<%=roomId%>";
  var timeLimit = parseInt("<%=timeParam%>", 10) || 30;

  var wsBase = (location.protocol === "https:" ? "wss://" : "ws://") + location.host + ctxPath;

  // ✅ 게임 진행: /omok/{roomId}?time=30
  var gameWs = new WebSocket(wsBase + "/omok/" + encodeURIComponent(roomId) + "?time=" + encodeURIComponent(timeLimit));

  // ✅ 방 퇴장 정리용: /room
  var roomWs = new WebSocket(wsBase + "/room");

  var statusEl = document.getElementById("status");
  var timerEl = document.getElementById("timer");

  var myColor = 0;     // 1=흑, 2=백
  var myTurn = false;
  var gameOver = false;

  var board = Array.from({length: 19}, function () { return Array(19).fill(0); });

  var timerInterval = null;
  var remain = 0;

  function startLocalTimer(sec) {
    remain = sec;
    if (timerInterval) clearInterval(timerInterval);
    timerEl.innerText = "⏳ 남은 시간: " + remain + "s";
    timerInterval = setInterval(function () {
      remain--;
      if (remain < 0) remain = 0;
      timerEl.innerText = "⏳ 남은 시간: " + remain + "s";
      if (remain <= 0) {
        clearInterval(timerInterval);
        timerInterval = null;
      }
    }, 1000);
  }

  gameWs.onopen = function () {
    statusEl.innerText = "연결됨! 상대방 대기 중...";
  };

  gameWs.onmessage = function (e) {
    var msg = e.data;
    try {
        const json = JSON.parse(msg);
    	
    	//(채원) : 게임이 끝났다면  GameResult화면을 보여주자 
        if (json.type === "GAME_RESULT") {
            showGameResult(json.results);
            return; 
          }
    	
    	
        if (json.type === 'CHAT') {
            const { kind, content, sender } = json.payload;
            
            if (sender === "<%=user.getUserId()%>") {
                showBubble("my-bubble-area", content);
            } else {
                showBubble("opponent-bubble-area", content);
            }
            return;
        }
    } catch (e) {
        console.log(e);
    }

    if (msg.indexOf("INFO:") === 0) {
      statusEl.innerText = msg.substring(5);
      return;
    }

    if (msg.indexOf("MYNAME:") === 0) {
      document.querySelector("#profile-left .nickname").innerText = msg.split(":")[1] || "";
      return;
    }

    if (msg.indexOf("OPPNAME:") === 0) {
      document.querySelector("#profile-right .nickname").innerText = msg.split(":")[1] || "상대방";
      return;
    }

    if (msg.indexOf("START:") === 0) {
      var c = msg.split(":")[1]; // BLACK | WHITE
      myColor = (c === "BLACK") ? 1 : 2;
      myTurn = (myColor === 1);
      statusEl.innerText = "게임 시작! 내 돌: " + (myColor === 1 ? "흑" : "백") + (myTurn ? " (내 턴)" : " (상대 턴)");
      return;
    }

    if (msg.indexOf("TIMER:") === 0) {
      var sec = parseInt(msg.split(":")[1], 10);
      if (!isNaN(sec)) startLocalTimer(sec);
      return;
    }

    if (msg.indexOf("PUT:") === 0) {
      var payload = msg.substring(4);
      var parts = payload.split(",");
      var x = parseInt(parts[0], 10);
      var y = parseInt(parts[1], 10);
      var color = parseInt(parts[2], 10);
      //가빈 이미지 수정 부분
      var img = parts[3];

      if (Number.isFinite(x) && Number.isFinite(y) && Number.isFinite(color)) {
        if (x >= 0 && x <= 18 && y >= 0 && y <= 18) {
          board[y][x] = color;
        //drawStone(x, y, color);
          drawStoneImage(x, y, img); //가빈 이미지로 돌 두기
          myTurn = (color !== myColor);
          if (!gameOver) statusEl.innerText = myTurn ? "내 턴" : "상대 턴";
        }
      }
      return;
    }

    if (msg.indexOf("WIN:") === 0) {
      gameOver = true;
      statusEl.innerText = "게임 종료: " + msg.substring(4);
      if (timerInterval) clearInterval(timerInterval);
      
   // ✅ 결과 요청 (추가!)
      if ("REQUEST_RESULT".equals(type)) {
          generateGameResult(room);
          return;
      }
    }
  };

  function leaveRoom() {
    try {
      if (roomWs.readyState === WebSocket.OPEN) {
        roomWs.send(JSON.stringify({ type: "LEAVE_ROOM", roomId: roomId }));
      }
    } catch (e) {}

    try { gameWs.close(); } catch (e) {}
    try { roomWs.close(); } catch (e) {}

    location.href = ctxPath + "/roomList.jsp";
  }
  window.leaveRoom = leaveRoom;

  window.addEventListener("beforeunload", function () {
    try {
      if (roomWs.readyState === WebSocket.OPEN) {
        roomWs.send(JSON.stringify({ type: "LEAVE_ROOM", roomId: roomId }));
      }
    } catch (e) {}
  });

  // ====== 오목 보드 ======
  var canvas = document.getElementById("board");
  var ctx2d = canvas.getContext("2d");
  var gap = canvas.width / 19;

  function drawBoard() {
    ctx2d.fillStyle = "#DCB35C";
    ctx2d.fillRect(0, 0, canvas.width, canvas.height);
    ctx2d.strokeStyle = "#000";

    for (var i = 0; i < 19; i++) {
      ctx2d.beginPath();
      ctx2d.moveTo(gap / 2, gap / 2 + i * gap);
      ctx2d.lineTo(gap / 2 + 18 * gap, gap / 2 + i * gap);
      ctx2d.stroke();

      ctx2d.beginPath();
      ctx2d.moveTo(gap / 2 + i * gap, gap / 2);
      ctx2d.lineTo(gap / 2 + i * gap, gap / 2 + 18 * gap);
      ctx2d.stroke();
    }
  }

//가빈 기존 돌 두기 주석 처리 후 새 돌 두기 함수

  /* function drawStone(x, y, color, img) {
    var cx = gap / 2 + x * gap;
    var cy = gap / 2 + y * gap;
    ctx2d.beginPath();
    ctx2d.arc(cx, cy, gap * 0.40, 0, Math.PI * 2);
    ctx2d.fillStyle = (color === 1) ? "black" : "white";
    ctx2d.fill();
    ctx2d.strokeStyle = "#333";
    ctx2d.stroke();
  }*/
  
  function drawStoneImage(x, y, imgPath) {
	  var cx = gap / 2 + x * gap;
	  var cy = gap / 2 + y * gap;

	  var img = new Image();
	  img.src = imgPath;

	  img.onload = function () {
	    ctx2d.drawImage(
	      img,
	      cx - gap * 0.5,
	      cy - gap * 0.5,
	      gap * 1.2,
	      gap * 1.2
	    );
	  };
	}

  drawBoard();
  canvas.onclick = function (e) {
    if (gameOver) return;
    if (!myTurn) { statusEl.innerText = "상대 턴입니다."; return; }

    var x = Math.round((e.offsetX - gap / 2) / gap);
    var y = Math.round((e.offsetY - gap / 2) / gap);

    if (x < 0 || x > 18 || y < 0 || y > 18) return;
    if (board[y][x] !== 0) { statusEl.innerText = "이미 돌이 있습니다."; return; }

    gameWs.send(x + "," + y); // ✅ 서버가 기대하는 포맷
  };
  
//말풍선 출력
  function showBubble(areaId, content) {
	const MAX_BUBBLES = 3;
	const BUBBLE_LIFETIME = 5000; 
	const area = document.getElementById(areaId);
	
	if (!area) return;
	
	const bubble = document.createElement("div");
	bubble.classList.add("chat-bubble");

	if (areaId === "my-bubble-area") {
	  bubble.classList.add("me");
	} else {
	  bubble.classList.add("opponent");
	}
	
	if (content.endsWith(".png")) {
		bubble.innerHTML = '<img src="' + content + '">';
	} else {
		bubble.innerText = content;		
	}
	
	area.appendChild(bubble);
	
	while (area.children.length > MAX_BUBBLES) {
	    area.firstElementChild.remove();
	}
	
	setTimeout(() => {
	    if (!bubble.parentNode) return; // 이미 제거됐으면 무시

	    bubble.classList.add("fade-out");
	    setTimeout(() => bubble.remove(), 200);
	  }, BUBBLE_LIFETIME);
	
  };
  
  
  //전송 
  function sendEmoji(emoji) {
	sendChat("EMOJI", emoji);
  };
  function sendText() {
	  const input = document.getElementById("chatText");
	  if (!input.value.trim()) return;

	  sendChat("TEXT", input.value);
	  input.value = "";
	  toggleChatInput();
  };
  function sendChat(kind, content) {
	gameWs.send(JSON.stringify({
		    type: "CHAT",
		    payload: {
		      kind,
		      content
		    }
		}));
  };
  //입력창 토글
  function toggleChatInput() {
    document.getElementById("chat-input-box").classList.toggle("hidden");
  }

  //(채원) : 결과 그리기 
 function showGameResult(results) {
  const overlay = document.getElementById("game-result-overlay");
  const list = document.getElementById("result-list");
  const img = document.getElementById("result-image");

  list.innerHTML = "";

  // 내 결과 기준으로 win / lose 이미지
  const myResult = results.find(r => r.nickname === "<%=user.getNickname()%>");
  img.src = myResult && myResult.win ? "win.png" : "lose.png";

  results.forEach(r => {
    let text = "";

    if (r.guest) {
      text = `${r.nickname} (GUEST)`;
    } else {
      text = r.win
        ? `${r.nickname} (${r.beforeScore}) +${r.plusScore}`
        : `${r.nickname} (${r.beforeScore}) +0`;
    }

    const div = document.createElement("div");
    div.className = "result-line";
    div.innerText = text;
    list.appendChild(div);
  });

  overlay.style.display = "flex";
}
  //채원 : 재대결 버튼 클릭시 
  function rematch() {
  gameWs.send(JSON.stringify({ type: "REMATCH" }));
  
  //클라이언트 상태 초기화 
  gameOver = false;
  myTurn = (myColor === 1);   // 흑이면 선공
  board = Array.from({length: 19}, () => Array(19).fill(0));
  drawBoard();

  
  document.getElementById("game-result-overlay").style.display = "none";
}
</script>

</body>
</html>

