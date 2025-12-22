// socket은 파일 최상단, 단 1번만 생성
const socket = new WebSocket(
  (location.protocol === "https:" ? "wss://" : "ws://") +
  location.host +
  "/mini_project/room"
);

let pendingJoinRoomId = null;

socket.onopen = () => {
  console.log("RoomList WebSocket connected");
};

socket.onmessage = e => {
  const data = JSON.parse(e.data);

  // ✅ 방 리스트 브로드캐스트
  if (Array.isArray(data)) {
    renderRooms(data);
    return;
  }

  // ✅ JOIN 성공 → 이때만 이동
  if (data.type === "JOIN_OK" && pendingJoinRoomId === data.roomId) {
    pendingJoinRoomId = null;
    location.href = "GameRoom.jsp?roomId=" + data.roomId;
    return;
  }

  // ❌ JOIN 실패
  if (data.type === "JOIN_DENY" && pendingJoinRoomId === data.roomId) {
    pendingJoinRoomId = null;
    alert(data.reason || "입장할 수 없습니다.");
    return;
  }
};

/* ===================== 검색 ===================== */
function searchRooms() {
  const keyword = document
    .getElementById("searchInput")
    .value.toLowerCase();

  document.querySelectorAll(".room-card").forEach(card => {
    const title = card
      .querySelector(".room-title")
      .innerText
      .toLowerCase();

    card.style.display = title.includes(keyword) ? "" : "none";
  });
}

/* ===================== 렌더링 ===================== */
function renderRooms(rooms) {
  const list = document.getElementById("roomList");
  const emptyState = document.getElementById("emptyState");

  list.innerHTML = "";

  // ✅ 방이 없을 때
  if (rooms.length === 0) {
    if (emptyState) emptyState.style.display = "block";
    list.style.display = "none";
    return;
  }

  if (emptyState) emptyState.style.display = "none";
  list.style.display = "grid";

  // ✅ 안 찬 방 → 꽉 찬 방 순서
  rooms.sort((a, b) => {
    const countA =
      (a.blackPlayer ? 1 : 0) + (a.whitePlayer ? 1 : 0);
    const countB =
      (b.blackPlayer ? 1 : 0) + (b.whitePlayer ? 1 : 0);

    return (countA === 2) - (countB === 2);
  });

  rooms.forEach(room => {
    const black = room.blackPlayer;
    const white = room.whitePlayer;

    const count =
      (black ? 1 : 0) +
      (white ? 1 : 0);

    const isFull = count === 2;

    const leftName = black ? black.nickname : "대기중";
    const rightName = white ? white.nickname : "대기중";

    // ✅ 닉네임 vs 닉네임 (한 줄 + 말줄임표)
    const playersHtml = `
      <div class="player-line" title="${leftName} vs ${rightName}">
        ${leftName} <span class="vs">vs</span> ${rightName}
      </div>
    `;

    const card = document.createElement("div");
    card.className = "room-card" + (isFull ? " orange" : "");

    card.onclick = () => {
      if (isFull) {
        alert("이미 꽉 찬 방입니다.");
        return;
      }

      pendingJoinRoomId = room.roomId;
      socket.send(JSON.stringify({
        type: "JOIN_ROOM",
        roomId: room.roomId
      }));
    };

    card.innerHTML = `
      <div class="room-title">${room.title}</div>
      <div class="room-info">
        <div class="player-list">
          ${playersHtml}
        </div>
        <span class="time">${room.mode}</span>
      </div>
    `;

    list.appendChild(card);
  });
}
