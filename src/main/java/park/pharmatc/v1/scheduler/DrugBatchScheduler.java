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
        log.info("\uD83D\uDE80 [Manual] 약품 데이터 적재 시작 (변경 감지 기반)");

        Map<String, DrugCompanyEntity> companyCache = new ConcurrentHashMap<>();
        List<DrugDto> drugs = drugInfoClient.fetchAllDrugsParallel();

        // 현재 DB에 있는 itemSeq 리스트 미리 로드
        Map<String, DrugItemEntity> existingItems = drugItemRepository.findAll().stream()
                .collect(Collectors.toMap(DrugItemEntity::getItemSeq, item -> item));

        Set<String> incomingItemSeqs = new HashSet<>();

        int inserted = 0;
        int updated = 0;

        for (DrugDto dto : drugs) {
            incomingItemSeqs.add(dto.itemSeq());

            DrugItemEntity item = existingItems.getOrDefault(dto.itemSeq(), new DrugItemEntity());
            boolean isNew = item.getId() == null;
            boolean changed = false;

            // 기본 정보 설정
            if (isNew) {
                item.setItemSeq(dto.itemSeq());
                changed = true;
            }

            if (!dto.itemName().equals(item.getItemName())) {
                item.setItemName(dto.itemName());
                changed = true;
            }
            if (!dto.formCodeName().equals(item.getFormCodeName())) {
                item.setFormCodeName(dto.formCodeName());
                changed = true;
            }
            if (!dto.ediCode().equals(item.getEdiCode())) {
                item.setEdiCode(dto.ediCode());
                changed = true;
            }

            // 회사 정보 처리
            DrugCompanyEntity company = companyCache.computeIfAbsent(dto.entpSeq(), seq ->
                    drugCompanyRepository.findById(seq).orElseGet(() -> {
                        DrugCompanyEntity newCompany = new DrugCompanyEntity();
                        newCompany.setEntpSeq(dto.entpSeq());
                        newCompany.setEntpName(dto.entpName());
                        return drugCompanyRepository.save(newCompany);
                    }));
            if (item.getCompany() == null || !item.getCompany().getEntpSeq().equals(company.getEntpSeq())) {
                item.setCompany(company);
                changed = true;
            }

            // Dimensions
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

            // Image
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

            if (changed) {
                item.setUpdatedAt(LocalDateTime.now());
                drugItemRepository.save(item);
                if (isNew) inserted++;
                else updated++;
            }
        }

        // 삭제 감지: 기존에 있었지만 새 목록에 없는 것 삭제
        List<DrugItemEntity> toDelete = existingItems.values().stream()
                .filter(item -> !incomingItemSeqs.contains(item.getItemSeq()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            drugItemRepository.deleteAll(toDelete);
            log.info("\u274C 삭제된 데이터 {}건", toDelete.size());
        }

        log.info("\u2705 적재 완료 - 추가: {}, 수정: {}, 총 처리: {}, 삭제: {}",
                inserted, updated, inserted + updated, toDelete.size());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateDrugData() {
        log.info("\uD83D\uDD52 [Scheduled] 약품 데이터 갱신 시작");
        runOnce();
        log.info("\uD83D\uDD52 [Scheduled] 약품 데이터 갱신 완료");
    }
}
