package ranking;

import java.io.IOException;
import java.util.List;
import java.util.Map;
//import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.UserDAO;
import domain.User;
import ranking.RankingService;

@WebServlet("/ranking")
public class RankingController extends HttpServlet {
	
	 // 서비스 객체 생성
    private RankingService service = new RankingService();
    // DB에 접근하는 DAO 객체 생성
    private UserDAO dao = new UserDAO();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 외부에서 전달받은 값
            String userName = (String) request.getAttribute("GAME_USER_NAME");
            Integer gameScore = (Integer) request.getAttribute("GAME_SCORE");

            // DB에 저장된 전체 랭킹 리스트 가져오
            List<User> rankingList = service.showRanking();
            
            // 전달된 데이터가 있을 경우 순위 계산
            if (userName != null || gameScore != null) {
            	// 서비스에서 내 정보 + 내 순위 한 번에 처리
                Map<String, Object> rankingInfo = service.getUserRankingInfo(userName, gameScore, rankingList);
                // JSP로 전달
                request.setAttribute("myUser", rankingInfo.get("myUser"));
                request.setAttribute("myRank", rankingInfo.get("myRank"));
            }

            // JSP로 전달
            request.setAttribute("rankingList", rankingList);
            // ranking.jsp로 화면 이동
            request.getRequestDispatcher("/ranking.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}