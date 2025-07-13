package park.pharmatc.v1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.entity.DrugCompanyEntity;
import park.pharmatc.v1.entity.DrugDimensionsEntity;
import park.pharmatc.v1.entity.DrugImagesEntity;
import park.pharmatc.v1.entity.DrugItemEntity;
import park.pharmatc.v1.repository.DrugItemRepository;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:3000",
        "https://pharmatc-90ac0.web.app",
        "http://3.25.208.164"
})
@RestController
@RequestMapping("/api/v1/drugs")
@RequiredArgsConstructor
public class DrugSearchController {

    private final DrugItemRepository drugItemRepository;

    @Transactional(readOnly = true)
    @GetMapping("/search")
    public List<DrugDto> searchDrugs(@RequestParam(required = false) String ediCode,
                                     @RequestParam(required = false) String itemName) {
        List<DrugItemEntity> results;

        // EdiCode 검색
        if (ediCode != null && !ediCode.isBlank()) {
            results = drugItemRepository.findByEdiCodeWithCompany(ediCode);
        }
        // itemName 검색, 길이가 3글자 이상인 경우만 처리
        else if (itemName != null && itemName.length() >= 3) {
            results = drugItemRepository.findByItemNameContainingWithCompany(itemName);
        } else {
            throw new IllegalArgumentException("검색 조건(보험코드 또는 약 이름 최소 3글자)이 필요합니다.");
        }

        if (results.isEmpty()) {
            throw new RuntimeException("검색 결과가 없습니다.");
        }

        // 검색된 결과를 DTO로 변환하여 반환
        return results.stream()
                .map(this::toDto)
                .toList();
    }

    // DrugItemEntity를 DrugDto로 변환하는 메서드
    private DrugDto toDto(DrugItemEntity item) {
        DrugCompanyEntity company = item.getCompany();

        // Dimensions와 Images는 Lazy Fetch로 가져오기
        DrugDimensionsEntity dim = item.getDimensions() != null && !item.getDimensions().isEmpty()
                ? item.getDimensions().get(0)  // 첫 번째 dimension 사용
                : null;

        DrugImagesEntity img = item.getImages() != null && !item.getImages().isEmpty()
                ? item.getImages().get(0)  // 첫 번째 이미지 사용
                : null;

        return DrugDto.of(
                item.getItemSeq(),
                item.getItemName(),
                company != null ? company.getEntpSeq() : null,
                company != null ? company.getEntpName() : null,
                img != null ? img.getItemImage() : null,
                dim != null ? dim.getLengLong() : null,
                dim != null ? dim.getLengShort() : null,
                dim != null ? dim.getThick() : null,
                item.getEdiCode(),
                item.getFormCodeName(),
                item.getId()  // id 필드를 추가하여 DTO로 반환
        );
    }
}
