package park.pharmatc.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.dto.ApiResponse;
import park.pharmatc.v1.service.DrugAsyncService;


@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://pharmatc-90ac0.web.app",
        "http://3.25.208.164"
})

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DrugAsyncService drugAsyncService;

    @PostMapping("/load-drugs")
    public ResponseEntity<ApiResponse> loadDrugs() {
        drugAsyncService.runBatchAsync(); // 🔄 비동기로 약품 적재 실행
        return ResponseEntity.ok(ApiResponse.success("ok","약품 적재를 백그라운드에서 시작했습니다."));
    }
}
