package park.pharmatc.v1.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.entity.*;
import park.pharmatc.v1.external.DrugInfoClient;
import park.pharmatc.v1.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrugBatchScheduler {

    private final DrugInfoClient drugInfoClient;
    private final DrugItemRepository drugItemRepository;
    private final DrugCompanyRepository drugCompanyRepository;

    public void runOnce() {
        log.info("🚀 [Manual] 약품 데이터 적재 (신규 + 덮어쓰기) 시작");

        Map<String, DrugCompanyEntity> companyCache = new ConcurrentHashMap<>();
        List<DrugDto> drugs = drugInfoClient.fetchAllDrugsParallel();

        List<DrugItemEntity> items = drugs.parallelStream()
                .map(dto -> {
                    DrugItemEntity item = drugItemRepository.findById(Long.valueOf(dto.itemSeq()))
                            .orElseGet(DrugItemEntity::new); // 신규 or 기존 업데이트

                    item.setItemSeq(String.valueOf(dto.itemSeq()));
                    item.setItemName(dto.itemName());
                    item.setFormCodeName(dto.formCodeName());
                    item.setEdiCode(dto.ediCode());
                    item.setUpdatedAt(LocalDateTime.now());

                    // 업체 설정
                    DrugCompanyEntity company = companyCache.computeIfAbsent(dto.entpSeq(), seq ->
                            drugCompanyRepository.findById(seq).orElseGet(() -> {
                                DrugCompanyEntity newCompany = new DrugCompanyEntity();
                                newCompany.setEntpSeq(dto.entpSeq());
                                newCompany.setEntpName(dto.entpName());
                                return drugCompanyRepository.save(newCompany);
                            }));
                    item.setCompany(company);

                    // dimensions (새로 덮어쓰기)
                    DrugDimensionsEntity dim = new DrugDimensionsEntity();
                    dim.setLengLong(dto.lengLong());
                    dim.setLengShort(dto.lengShort());
                    dim.setThick(dto.thick());
                    dim.setDrugItem(item);
                    item.setDimensions(dim);

                    // image (새로 덮어쓰기)
                    DrugImagesEntity img = new DrugImagesEntity();
                    img.setItemImage(dto.itemImage());
                    img.setDrugItem(item);
                    item.setImage(img);

                    return item;
                })
                .toList();

        log.info("📥 저장/업데이트 대상 약품 수: {}", items.size());

        for (int i = 0; i < items.size(); i += 100) {
            int end = Math.min(i + 100, items.size());
            drugItemRepository.saveAll(items.subList(i, end));
        }

        log.info("✅ 적재 완료 (insert+update): {}", items.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDrugData() {
        log.info("🕒 [Scheduled] 약품 데이터 갱신 시작");
        runOnce();
        log.info("🕒 [Scheduled] 약품 데이터 갱신 완료");
    }
}
