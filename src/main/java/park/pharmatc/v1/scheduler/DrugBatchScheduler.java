package park.pharmatc.v1.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.entity.*;
import park.pharmatc.v1.external.DrugInfoClient;
import park.pharmatc.v1.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrugBatchScheduler {

    private final DrugInfoClient drugInfoClient;
    private final DrugItemRepository drugItemRepository;
    private final DrugCompanyRepository drugCompanyRepository;

    @Transactional
    public void runOnce() {
        log.info("🚀 [Manual] 약품 데이터 적재 시작");

        Map<String, DrugCompanyEntity> companyCache = new ConcurrentHashMap<>();
        List<DrugDto> drugs = drugInfoClient.fetchAllDrugsParallel();

        Map<String, DrugItemEntity> existingItems = drugItemRepository.findAll().stream()
                .collect(Collectors.toMap(DrugItemEntity::getItemSeq, item -> item, (a, b) -> a));

        Set<String> incomingItemSeqs = new HashSet<>();
        int inserted = 0, updated = 0, failed = 0;
        List<String> failedItemSeqs = new ArrayList<>();

        for (DrugDto dto : drugs) {
            try {
                if (dto.itemSeq() == null) {
                    log.warn("⚠️ itemSeq null: {}", dto);
                    failed++;
                    failedItemSeqs.add("NULL");
                    continue;
                }

                incomingItemSeqs.add(dto.itemSeq());

                DrugItemEntity item = existingItems.get(dto.itemSeq());
                boolean isNew = false;
                if (item == null) {
                    item = new DrugItemEntity();
                    item.setItemSeq(dto.itemSeq());
                    isNew = true;
                }

                item.setItemName(dto.itemName());
                item.setEdiCode(dto.ediCode());
                item.setFormCodeName(dto.formCodeName());
                item.setUpdatedAt(LocalDateTime.now());

                // 회사 정보
                if (dto.entpSeq() != null) {
                    DrugCompanyEntity company = companyCache.computeIfAbsent(dto.entpSeq(), seq ->
                            drugCompanyRepository.findById(seq).orElseGet(() -> {
                                DrugCompanyEntity newCompany = new DrugCompanyEntity();
                                newCompany.setEntpSeq(seq);
                                newCompany.setEntpName(dto.entpName());
                                return drugCompanyRepository.save(newCompany);
                            }));
                    item.setCompany(company);
                }

                // dimensions 중복 여부 확인
                if (item.getDimensions() == null) item.setDimensions(new ArrayList<>());
                boolean dimExists = item.getDimensions().stream()
                        .anyMatch(d -> Objects.equals(d.getLengLong(), dto.lengLong()) &&
                                Objects.equals(d.getLengShort(), dto.lengShort()) &&
                                Objects.equals(d.getThick(), dto.thick()));
                if (!dimExists) {
                    DrugDimensionsEntity dim = DrugDimensionsEntity.builder()
                            .itemSeq(dto.itemSeq())
                            .lengLong(dto.lengLong())
                            .lengShort(dto.lengShort())
                            .thick(dto.thick())
                            .drugItem(item)
                            .build();
                    item.getDimensions().add(dim);
                }

                // 이미지 중복 확인
                if (item.getImages() == null) item.setImages(new ArrayList<>());
                boolean imgExists = item.getImages().stream()
                        .anyMatch(i -> Objects.equals(i.getItemImage(), dto.itemImage()));
                if (!imgExists) {
                    DrugImagesEntity img = DrugImagesEntity.builder()
                            .itemSeq(dto.itemSeq())
                            .itemImage(dto.itemImage())
                            .lengLong(dto.lengLong())
                            .lengShort(dto.lengShort())
                            .thick(dto.thick())
                            .drugItem(item)
                            .build();
                    item.getImages().add(img);
                }

                drugItemRepository.save(item);
                if (isNew) inserted++;
                else updated++;

            } catch (Exception e) {
                failed++;
                failedItemSeqs.add(dto.itemSeq());
                log.warn("❗ 약품 {} 저장 중 예외 발생: {}", dto.itemSeq(), e.getMessage(), e);
            }
        }

        List<DrugItemEntity> toDelete = existingItems.values().stream()
                .filter(item -> !incomingItemSeqs.contains(item.getItemSeq()))
                .toList();

        if (!toDelete.isEmpty()) {
            drugItemRepository.deleteAll(toDelete);
            log.info("❌ 삭제된 데이터 {}건", toDelete.size());
        }

        log.info("✅ 적재 완료 - 추가: {}, 수정: {}, 실패: {}, 총 처리: {}, 삭제: {}",
                inserted, updated, failed, inserted + updated + failed, toDelete.size());

        if (!failedItemSeqs.isEmpty()) {
            log.warn("❗ 실패 itemSeq ({}건): {}", failedItemSeqs.size(), failedItemSeqs);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDrugData() {
        log.info("🕒 [Scheduled] 약품 데이터 갱신 시작");
        runOnce();
        log.info("🕒 [Scheduled] 약품 데이터 갱신 완료");
    }
}
