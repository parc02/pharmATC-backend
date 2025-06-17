package park.pharmatc.v1.dto;

/**
 * 사용자 입력을 받는 DTO
 * 예) 타이레놀정 500mg 혹은 보험코드 와 오차률 입력
 * @param keyword
 * @param tolerance
 */
public record MatchRequest(
        String keyword, // 약품명 또는 EDI 코드
        int tolerance // 허용 오차 (%): 5, 10, 15, 20 중 하나
) {

}
