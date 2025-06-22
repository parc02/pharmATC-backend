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
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrugBatchScheduler {

    private final DrugInfoClient drugInfoClient;
    private final DrugItemRepository drugItemRepository;
    private final DrugCompanyRepository drugCompanyRepository;

    public void runOnce() {
        log.info("🚀 [Manual] 약품 데이터 적재 시작 (변경 감지 기반)");

        Map<String, DrugCompanyEntity> companyCache = new ConcurrentHashMap<>();
        List<DrugDto> drugs = drugInfoClient.fetchAllDrugsParallel();

        Map<String, DrugItemEntity> existingItems = drugItemRepository.findAll().stream()
                .collect(Collectors.toMap(DrugItemEntity::getItemSeq, item -> item));

        Set<String> incomingItemSeqs = new HashSet<>();
        int inserted = 0, updated = 0, failed = 0;
        List<String> failedItemSeqs = new ArrayList<>();

        for (DrugDto dto : drugs) {
            try {
                if (dto.itemSeq() == null) {
                    log.warn("⚠️ itemSeq가 null인 데이터 발견, 스킵됨: {}", dto);
                    failed++;
                    failedItemSeqs.add("NULL");
                    continue;
                }

                incomingItemSeqs.add(dto.itemSeq());

                DrugItemEntity item = existingItems.get(dto.itemSeq());
                boolean isNew = false;
                boolean changed = false;

                if (item == null) {
                    Optional<DrugItemEntity> dbItemOpt = drugItemRepository.findByItemSeq(dto.itemSeq());
                    if (dbItemOpt.isPresent()) {
                        item = dbItemOpt.get();
                    } else {
                        item = new DrugItemEntity();
                        item.setItemSeq(dto.itemSeq());
                        isNew = true;
                        changed = true;
                    }
                }

                if (!Objects.equals(dto.itemName(), item.getItemName())) {
                    item.setItemName(dto.itemName());
                    changed = true;
                }

                if (!Objects.equals(dto.formCodeName(), item.getFormCodeName())) {
                    item.setFormCodeName(dto.formCodeName());
                    changed = true;
                }

                if (!Objects.equals(dto.ediCode(), item.getEdiCode())) {
                    item.setEdiCode(dto.ediCode());
                    changed = true;
                }

                // 회사 정보 처리
                DrugCompanyEntity company = null;
                if (dto.entpSeq() != null) {
                    company = companyCache.computeIfAbsent(dto.entpSeq(), seq ->
                            drugCompanyRepository.findById(seq).orElseGet(() -> {
                                DrugCompanyEntity newCompany = new DrugCompanyEntity();
                                newCompany.setEntpSeq(dto.entpSeq());
                                newCompany.setEntpName(dto.entpName());
                                return drugCompanyRepository.save(newCompany);
                            }));
                }

                if (company != null && (item.getCompany() == null ||
                        !Objects.equals(item.getCompany().getEntpSeq(), company.getEntpSeq()))) {
                    item.setCompany(company);
                    changed = true;
                }

                // 크기 정보
                DrugDimensionsEntity dim = item.getDimensions();
                if (dim == null) {
                    dim = new DrugDimensionsEntity();
                    dim.setDrugItem(item);
                    item.setDimensions(dim);
                    changed = true;
                }
                if (!Objects.equals(dim.getLengLong(), dto.lengLong())) {
                    dim.setLengLong(dto.lengLong());
                    changed = true;
                }
                if (!Objects.equals(dim.getLengShort(), dto.lengShort())) {
                    dim.setLengShort(dto.lengShort());
                    changed = true;
                }
                if (!Objects.equals(dim.getThick(), dto.thick())) {
                    dim.setThick(dto.thick());
                    changed = true;
                }

                // 이미지 정보
                DrugImagesEntity img = item.getImage();
                if (img == null) {
                    img = new DrugImagesEntity();
                    img.setDrugItem(item);
                    item.setImage(img);
                    changed = true;
                }
                if (!Objects.equals(img.getItemImage(), dto.itemImage())) {
                    img.setItemImage(dto.itemImage());
                    changed = true;
                }

                if (isNew || changed) {
                    item.setUpdatedAt(LocalDateTime.now());
                    drugItemRepository.save(item);
                    if (isNew) inserted++;
                    else updated++;
                }

            } catch (Exception e) {
                failed++;
                failedItemSeqs.add(dto.itemSeq());
                log.warn("❗️약품 {} 저장 중 예외 발생: {}", dto.itemSeq(), e.getMessage(), e);
            }
        }

        // 삭제 감지
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
            log.warn("❗ 실패한 약품 itemSeq 목록 ({}건): {}", failedItemSeqs.size(), failedItemSeqs);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDrugData() {
        log.info("🕒 [Scheduled] 약품 데이터 갱신 시작");
        runOnce();
        log.info("🕒 [Scheduled] 약품 데이터 갱신 완료");
    }
}
