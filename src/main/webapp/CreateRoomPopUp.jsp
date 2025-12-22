<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>방 만들기</title>

<style>
html, body { width: 100%; height: 100%; margin: 0; }
body { display: flex; justify-content: center; align-items: center; overflow: hidden; }

.popup-container {
  width: 700px; height: 500px;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 30px;
}
h1 { font-size: 36px; font-weight: 800; align-self: flex-start; padding-left: 50px; }

#addTitle {
  display: flex; flex-direction: column;
  width: 580px; height: 240px;
  border: 6px solid #D4D4D4;
  border-radius: 15px;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
}
#title {
  width: 430px; height: 50px;
  font-size: 24px;
  padding-left: 15px;
  font-weight: 500;
  border: 6px solid #D4D4D4;
  border-radius: 15px;
  margin-bottom: 30px;
}

.radio-box input[type="radio"]{ display: none; }
.radio-wrap { display: flex; gap: 70px; margin: 10px; }
.radio-box { cursor: pointer; line-height: 110px; }
.radio-box input:checked + .text-stroke {
  color: white;
  -webkit-text-stroke: 4px #7F7F7F;
  border: 5px dashed #F0B061;
}
.text-stroke {
  color: white;
  text-align: center;
  font-size: 64px;
  font-weight: 800;
  -webkit-text-stroke: 4px #DCDCDC;
  width: 200px;
  height: 120px;
  background-color: #f5f5f5;
  border-radius: 15px;
  border: 4px dashed #85BE57;
}

#addBtn {
  width: 314px; height: 70px;
  border-radius: 15px;
  background-color: #85BE57;
  color: white;
  font-size: 35px;
  font-weight: 700;
  border: 0px;
  text-align: center;
  line-height: 70px;
  cursor: pointer;
}
</style>

<script>
window.onload = function () {
  var TARGET_WIDTH = 1000;
  var TARGET_HEIGHT = 700;

  var widthDiff  = window.outerWidth  - window.innerWidth;
  var heightDiff = window.outerHeight - window.innerHeight;

  window.resizeTo(TARGET_WIDTH + widthDiff, TARGET_HEIGHT + heightDiff);
  window.moveTo((screen.width - (TARGET_WIDTH + widthDiff)) / 2,
                (screen.height - (TARGET_HEIGHT + heightDiff)) / 2);
};
</script>
</head>

<body>
<div class="popup-container">
  <div id="addTitle">
    <h1>방제</h1>
    <input type="text" id="title" placeholder="방 제목">
  </div>

  <div class="radio-wrap">
    <label class="radio-box">
      <input type="radio" name="mode" value="30s" checked>
      <div class="text-stroke">30s</div>
    </label>

    <label class="radio-box">
      <input type="radio" name="mode" value="60s">
      <div class="text-stroke">60s</div>
    </label>
  </div>

  <button type="button" id="addBtn" onclick="createRoom()">방 만들기</button>
</div>

<script>
  var ctxPath = "<%= request.getContextPath() %>";
  var wsBase = (location.protocol === "https:" ? "wss://" : "ws://") + location.host + ctxPath;

  var socket = new WebSocket(wsBase + "/room");
  var selectedTime = 30;

  socket.onmessage = function (e) {
    var data = JSON.parse(e.data);

    if (data.type === "ROOM_CREATED") {
      window.opener.location.href =
        ctxPath + "/GameRoom.jsp?roomId=" + encodeURIComponent(data.roomId) + "&time=" + selectedTime;
      window.close();
    }
  };

  function createRoom() {
    var title = document.getElementById("title").value.trim();
    var mode = document.querySelector("input[name=mode]:checked").value; // 30s/60s

    if (!title) {
      alert("방 제목을 입력하세요");
      return;
    }

    selectedTime = parseInt(String(mode).replace("s",""), 10) || 30;

    if (socket.readyState !== WebSocket.OPEN) {
      alert("서버 연결 중입니다. 잠깐 뒤 다시 시도!");
      return;
    }

    socket.send(JSON.stringify({
      type: "CREATE_ROOM",
      title: title,
      mode: mode
    }));
  }
</script>
</body>
</html>

