package park.pharmatc.v1.service;

import org.junit.jupiter.api.Test;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.external.DrugInfoClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchServiceTest {
    @Test
    void findMatches_필터링정상작동() {
        // given
        DrugInfoClient mockClient = mock(DrugInfoClient.class);

        DrugDto 기준약 = new DrugDto("타이레놀", "1234", "정제", 8.0, 3.0);
        List<DrugDto> 전체약품 = List.of(
                기준약,
                new DrugDto("비슷한약", "1111", "정제", 7.9, 3.1),
                new DrugDto("크기가너무큼", "2222", "정제", 10.0, 5.0),
                new DrugDto("제형다름", "3333", "시럽", 8.0, 3.0)
        );

        when(mockClient.fetchDrugInfo("타이레놀")).thenReturn(기준약);
        when(mockClient.fetchAllDrugs()).thenReturn(전체약품);

        MatchService service = new MatchService(mockClient);

        // when
        MatchRequest 요청 = new MatchRequest("타이레놀", 10); // 허용오차 10%
        MatchResponse 응답 = service.findMatches(요청);

        // then
        List<DrugDto> 결과 = 응답.matchedDrugs();
        assertEquals(2, 결과.size());
        assertTrue(결과.stream().anyMatch(d -> d.itemName().equals("타이레놀")));
        assertTrue(결과.stream().anyMatch(d -> d.itemName().equals("비슷한약")));
    }
}
