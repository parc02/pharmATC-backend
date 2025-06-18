package park.pharmatc.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.service.MatchService;
import park.pharmatc.v1.external.DrugInfoClient;

import java.util.List;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("api/v1/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final DrugInfoClient drugInfoClient;

    @PostMapping
    public ResponseEntity<MatchResponse> matchDrugs(@RequestBody MatchRequest matchRequest) {
        MatchResponse response = matchService.findMatches(matchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<DrugDto> getDrugByItemSeq(@RequestParam("item_seq") String itemSeq) {
        List<DrugDto> allDrugs = drugInfoClient.fetchAllDrugs();

        DrugDto drug = allDrugs.stream()
                .filter(d -> d.itemSeq().equals(itemSeq))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 품목코드의 약품을 찾을 수 없습니다: " + itemSeq));

        return ResponseEntity.ok(drug);
    }
}
