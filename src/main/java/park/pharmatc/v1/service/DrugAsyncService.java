package park.pharmatc.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import park.pharmatc.v1.scheduler.DrugBatchScheduler;

@Service
@RequiredArgsConstructor
public class DrugAsyncService {

    private final DrugBatchScheduler drugBatchScheduler;

    @Async
    public void runBatchAsync() {
        drugBatchScheduler.runOnce();
    }
}