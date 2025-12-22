package ranking;

import java.util.HashMap;	// 한 번에 여러 값 반환
import java.util.List;	// List 인터페이스 사용 (랭킹 목록)
import java.util.Map;	// 한 번에 여러 값 반환

import DAO.UserDAO;
import domain.User;	// 유저 한 명 표현 객체

public class RankingService {
	// DB 작업을 위해 DAO 객체 보유
	private UserDAO dao = new UserDAO();

    // 컨트롤러에서 호출하는 메서드
    public List<User> showRanking() throws Exception {
        return dao.showRanking();
    }
    
    // 내 랭킹 정보 조회(로그인 유저 vs 게스트 구분)
    public Map<String, Object> getUserRankingInfo(String userName, Integer gameScore, List<User> rankingList) throws Exception {
    	// 결과를 담아서 컨트롤러에 넘겨줄 보따리(Map) 생성
    	Map<String, Object> result = new HashMap<>();
    	// 내 정보를 담을 임시 객체 생성
        User myUser = new User();
        myUser.setNickname("Guest");
        myUser.setScore(gameScore != null ? gameScore : 0);
        
        // 현재 내 점수가 전체 랭킹 리스트에서 몇 등인지 계산
        int myRank = calculateRank(rankingList, myUser.getScore());
        
        // 보따리에 '내 정보 객체'와 '계산된 순위' 담음
        result.put("myUser", myUser);
        result.put("myRank", myRank);
        // 완성된 보따리 반환
        return result;
    }

    // 리스트 돌며 나보다 점수 높은 사람이 몇 명인지 세는 로직
    public int calculateRank(List<User> list, int score) {
    	// 기본 1등부터 시작해서 나보다 잘난 사람 찾음
        int rank = 1;
        if (list != null) {
        	// 랭킹 목록을 하나씩 비교
            for (User u : list) {
                if (u.getScore() > score) {
                	// 내 등수를 한 칸 뒤로 미룸
                    rank++;
                }
            }
        }
        // 최종 계산된 등수
        return rank;
    }
}