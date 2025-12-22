package domain;

import lombok.AllArgsConstructor;
import lombok.Data;


//채원 : gameResult 구현
//사용하는곳 : OmokSocket에서! 

@AllArgsConstructor
@Data
public class GameResult {
	// 플레이어 이름
	private String nickname;

	// guest 인지?
	private boolean guest;

	// 회원일 때만 사용 (guest==false인경우) guest이면 null처리
	private Integer beforeScore;

	// 점수 plus
	private Integer plusScore; // +3 / +6 / 0

	// 승패여부
	private boolean win;

}
