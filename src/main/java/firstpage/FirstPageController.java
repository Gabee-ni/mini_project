package firstpage;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import domain.User;


@WebServlet("/first")
public class FirstPageController extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String action = request.getParameter("action");
        
		// GAME START 버튼 클릭 시 바로 roomList.jsp로 리다이렉트
        if ("start".equals(action)) {
            // response.sendRedirect는 브라우저에게 해당 페이지로 다시 접속하라고 명령합니다.
            response.sendRedirect(request.getContextPath() + "/roomList.jsp");
            return; // 리다이렉트 후 반드시 리턴하여 로직 종료
        }
		
		// 원래는 이거 하나만 내 코드
		request.getRequestDispatcher("/firstPage.jsp").forward(request, response);
	}

}