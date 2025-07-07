package park.pharmatc.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.entity.DrugItemEntity;
import park.pharmatc.v1.external.DrugInfoClient;
import park.pharmatc.v1.repository.DrugItemRepository;
import park.pharmatc.v1.service.MatchService;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://pharmatc-90ac0.web.app",
        "http://3.25.215.61"
})

@RestController
@RequestMapping("api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final DrugItemRepository drugItemRepository;
    private final DrugInfoClient drugInfoClient;

    /**
     * POST /api/v1/match
     * 사용자가 입력한 기준 약품(itemSeq)과 허용 오차(tolerance)에 따라 유사 약품 리스트 반환
     */
    @PostMapping
    public ResponseEntity<MatchResponse> matchDrugs(@RequestBody MatchRequest matchRequest) {
        MatchResponse response = matchService.findMatches(matchRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/match?item_seq=12345678
     * 특정 품목코드(itemSeq)를 기준으로 단일 약품 정보 조회
     */
    @GetMapping
    public ResponseEntity<DrugDto> getDrugByItemSeq(@RequestParam("item_seq") String itemSeq) {
        DrugItemEntity item = drugItemRepository.findByItemSeq(itemSeq)
                .orElseThrow(() -> new RuntimeException("해당 품목코드의 약품을 찾을 수 없습니다: " + itemSeq));

        DrugDto dto = new DrugDto(
                item.getItemSeq(),
                item.getItemName(),
                item.getCompany().getEntpSeq(),
                item.getCompany().getEntpName(),
                item.getImage() != null ? item.getImage().getItemImage() : null,
                item.getDimensions() != null ? item.getDimensions().getLengLong() : null,
                item.getDimensions() != null ? item.getDimensions().getLengShort() : null,
                item.getDimensions() != null ? item.getDimensions().getThick() : null,
                item.getEdiCode(),
                item.getFormCodeName()
        );

        return ResponseEntity.ok(dto);
    }


    /**
     * GET /api/v1/match/debug
     * 외부 API 테스트용: 특정 itemSeq 포함 여부 콘솔 출력
     */
    @GetMapping("/debug")
    public void debugApi() {
        List<DrugDto> allDrugs = drugInfoClient.fetchAllDrugsParallel();
        allDrugs.stream()
                .filter(d -> d.itemSeq().equals("200201482"))
                .findFirst()
                .ifPresentOrElse(
                        d -> System.out.println("✅ FOUND: " + d),
                        () -> System.out.println("❌ NOT FOUND")
                );
    }
}
