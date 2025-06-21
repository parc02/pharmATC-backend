package park.pharmatc.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.scheduler.DrugBatchScheduler;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    
    private final DrugBatchScheduler drugBatchScheduler;

    @PostMapping("/load-drugs")
    public ResponseEntity<String> loadDrugs() {
        drugBatchScheduler.runOnce();
        return ResponseEntity.ok("약품 데이터 초기 적재 완료!");
    }
}
