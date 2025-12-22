<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>OMOK</title>

<style>
    body {
	    margin: 0;
	    font-family: 'Arial';
        background: url("${pageContext.request.contextPath}/img/back.png")
                    no-repeat center center fixed;
        
        background-size: cover;
        min-height: 100vh;
    }
    
    .main-container {
	    position: absolute;
	    bottom: 150px;
	    left: 50%;
	    transform: translateX(-50%);
	    text-align: center;
    }
    
    .btn-group {
        display: flex;
        gap: 20px;
        justify-content: center;
    }
        
    /* 버튼 스타일 변경 */
	.btn {
		display: flex;
		justify-content: center;
		align-items: center;
		
		background: #fff;
        padding: 1rem 5vw;
        text-decoration: none;
        color: #333;
        
	    width: 20vw; /* 너비 설정 */
	    max-width: 400px;
	    min-width: 200px;
	    
	    height: 5vh; /* 높이 설정 */
	    min-height: 50px;
	    
	    border-radius: 1rem; /* 모서리 반경 */
	    border: 0.3rem solid #808080; /* 회색 테두리 */
	    
	    font-family: 'Comic Sans MS', cursive; /* 폰트 변경 */
	    font-size: 32px; /* 폰트 크기 */
	    font-weight: bold; /* 굵게 */
	    
	    position: relative;
	    filter: url(#squiggle-filter);  /* 지글지글 효과를 위한 애니메이션 */
	}



</style>

</head>
<body>
	<!-- 지글지글 효과를 위한 SVG 필터 -->
	<svg style="position: absolute; width: 0; height: 0;">
	    <defs>
	        <filter id="squiggle-filter">
	            <feTurbulence baseFrequency="0.02" numOctaves="3" seed="2" />
	            <feDisplacementMap in="SourceGraphic" scale="3" />
	        </filter>
	    </defs>
	</svg>
	
	<div class="main-container">
	   <div class="btn-group">
	   		<%-- 클릭 시 컨트롤러 → 컨트롤러가 UUID를 만들고 세션에 User 객체를 저장 → 방 목록으로 도착 --%>
	       <a href="${pageContext.request.contextPath}/first?action=start"
	          class="btn">GAME START</a>
			<%-- 로그인 창으로 이동 --%>
	       <a href="${pageContext.request.contextPath}/login"
	          class="btn">LOGIN</a>
    	</div>
	</div>
</body>
</html>