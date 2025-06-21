package park.pharmatc.v1.dto;

import java.util.List;

/**
 * 매칭된 약품을 담은 리스트가 응답에 담겨서 프론트로 나가게 됨
 *
 * @param matchedDrugs
 */

public record MatchResponse(
        List<DrugDto> matchedDrugs
)
{
}
