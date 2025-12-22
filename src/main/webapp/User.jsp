<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>

<style>
<style>
html, body { width: 100%; height: 100%; margin: 0; }
body { display: flex; justify-content: center; align-items: center; overflow: hidden; }
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

<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<body>
	기존 유저
</body>
</html>