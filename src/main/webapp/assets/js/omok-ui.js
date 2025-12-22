// 회원가입: 돌 선택 (호환 버전)
(function () {
  var stones = document.querySelectorAll("[data-stone]");
  var hidden = document.getElementById("stoneStyle");
  if (!stones.length || !hidden) return;

  for (var i = 0; i < stones.length; i++) {
    (function (el) {
      el.addEventListener("click", function () {
        for (var j = 0; j < stones.length; j++) {
          stones[j].classList.remove("is-selected");
        }
        el.classList.add("is-selected");
        hidden.value = el.getAttribute("data-stone"); // "1"~"6"
      });
    })(stones[i]);
  }
})();

// 회원가입: 프로필 이미지 미리보기 (호환 버전)
(function () {
  var file = document.getElementById("profileFile");
  var preview = document.getElementById("profilePreview");
  if (!file || !preview) return;

  file.addEventListener("change", function () {
    var f = (file.files && file.files.length > 0) ? file.files[0] : null;
    if (!f) return;
    preview.src = URL.createObjectURL(f);
  });
})();
