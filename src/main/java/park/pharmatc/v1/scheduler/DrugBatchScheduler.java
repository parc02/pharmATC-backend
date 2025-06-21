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

        int updated = 0;
        int inserted = 0;

        for (DrugDto dto : drugs) {
            try {
                DrugItemEntity item = drugItemRepository.findById(Long.valueOf(String.valueOf(Long.valueOf(dto.itemSeq()))))
                        .orElseGet(() -> {
                            DrugItemEntity newItem = new DrugItemEntity();
                            newItem.setItemSeq(dto.itemSeq());
                            return newItem;
                        });

                item.setItemName(dto.itemName());
                item.setFormCodeName(dto.formCodeName());
                item.setEdiCode(dto.ediCode());
                item.setUpdatedAt(LocalDateTime.now());

                // 업체 처리
                DrugCompanyEntity company = companyCache.computeIfAbsent(dto.entpSeq(), seq ->
                        drugCompanyRepository.findById(seq).orElseGet(() -> {
                            DrugCompanyEntity newCompany = new DrugCompanyEntity();
                            newCompany.setEntpSeq(dto.entpSeq());
                            newCompany.setEntpName(dto.entpName());
                            return drugCompanyRepository.save(newCompany);
                        }));
                item.setCompany(company);

                // dimensions
                DrugDimensionsEntity dim = item.getDimensions();
                if (dim == null) {
                    dim = new DrugDimensionsEntity();
                    dim.setDrugItem(item);
                    item.setDimensions(dim);
                }
                dim.setLengLong(dto.lengLong());
                dim.setLengShort(dto.lengShort());
                dim.setThick(dto.thick());

                // image
                DrugImagesEntity img = item.getImage();
                if (img == null) {
                    img = new DrugImagesEntity();
                    img.setDrugItem(item);
                    item.setImage(img);
                }
                img.setItemImage(dto.itemImage());

                // 저장
                drugItemRepository.save(item);

                if (item.getUpdatedAt() == null) {
                    inserted++;
                } else {
                    updated++;
                }

            } catch (Exception e) {
                log.warn("❌ 저장 실패 - itemSeq: {}, 이유: {}", dto.itemSeq(), e.getMessage());
            }
        }

        log.info("✅ 적재 완료 - 추가: {}, 수정: {}, 실패 제외: {}", inserted, updated, drugs.size() - inserted - updated);

    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDrugData() {
        log.info("🕒 [Scheduled] 약품 데이터 갱신 시작");
        runOnce();
        log.info("🕒 [Scheduled] 약품 데이터 갱신 완료");
    }
}
