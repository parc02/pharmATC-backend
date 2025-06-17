package park.pharmatc.v1.external;

import org.springframework.stereotype.Component;
import park.pharmatc.v1.dto.DrugDto;

import java.util.List;

@Component
public class DrugInfoClient {

    // TODO: 실제 API 연동 대신 mock 데이터로 먼저 구현
    public DrugDto fetchDrugInfo(String keyword) {
        // keyword가 포함된 약 1개를 mock으로 반환
        return new DrugDto(
                "타이레놀",
                "12345678",
                "정제",
                8.0,
                3.0
        );
    }

    public List<DrugDto> fetchAllDrugs() {
        // mock 리스트 반환
        return List.of(
                new DrugDto("타이레놀", "12345678", "정제", 8.0, 3.0),
                new DrugDto("아세트아미노펜정", null, "정제", 7.6, 2.9),
                new DrugDto("이부프로펜정", "87654321", "정제", 9.2, 3.1),
                new DrugDto("시럽형약", "99999999", "시럽", 0.0, 0.0) // 제형이 다름 → 제외
        );
    }

}
