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
        "http://3.25.208.164"
})
@RestController
@RequestMapping("api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final DrugItemRepository drugItemRepository;
    private final DrugInfoClient drugInfoClient;

    @PostMapping
    public ResponseEntity<MatchResponse> matchDrugs(@RequestBody MatchRequest matchRequest) {
        System.out.println("Received match request: id=" + matchRequest.id() + ", tolerance=" + matchRequest.tolerance());
        MatchResponse response = matchService.findMatches(matchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<DrugDto> getDrugByItemSeq(@RequestParam("item_seq") String itemSeq) {
        DrugItemEntity item = drugItemRepository.findByItemSeq(itemSeq)
                .orElseThrow(() -> new RuntimeException("해당 품목코드의 약품을 찾을 수 없습니다: " + itemSeq));

        String image = item.getImages() != null && !item.getImages().isEmpty()
                ? item.getImages().get(0).getItemImage()
                : null;

        Double lengthLong = item.getDimensions() != null && !item.getDimensions().isEmpty()
                ? item.getDimensions().get(0).getLengLong()
                : null;

        Double lengthShort = item.getDimensions() != null && !item.getDimensions().isEmpty()
                ? item.getDimensions().get(0).getLengShort()
                : null;

        Double thick = item.getDimensions() != null && !item.getDimensions().isEmpty()
                ? item.getDimensions().get(0).getThick()
                : null;

        DrugDto dto = DrugDto.of(
                item.getItemSeq(),
                item.getItemName(),
                item.getCompany().getEntpSeq(),
                item.getCompany().getEntpName(),
                image,
                lengthLong,
                lengthShort,
                thick,
                item.getEdiCode(),
                item.getFormCodeName(),
                item.getId()  // Ensure correct mapping of the ID
        );

        return ResponseEntity.ok(dto);
    }

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
