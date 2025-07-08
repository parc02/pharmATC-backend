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

        if (ediCode != null && !ediCode.isBlank()) {
            results = drugItemRepository.findByEdiCodeWithAll(ediCode);
        } else if (itemName != null && itemName.length() >= 3) {
            results = drugItemRepository.findByItemNameContainingWithAll(itemName);
        } else {
            throw new IllegalArgumentException("검색 조건(보험코드 또는 약 이름 최소 3글자)이 필요합니다.");
        }

        return results.stream()
                .map(this::toDto)
                .toList();
    }

    private DrugDto toDto(DrugItemEntity item) {
        DrugCompanyEntity company = item.getCompany();
        DrugDimensionsEntity dim = item.getDimensions();
        DrugImagesEntity img = item.getImage();

        return new DrugDto(
                item.getItemSeq(),
                item.getItemName(),
                company != null ? company.getEntpSeq() : null,
                company != null ? company.getEntpName() : null,
                img != null ? img.getItemImage() : null,
                dim != null ? dim.getLengLong() : null,
                dim != null ? dim.getLengShort() : null,
                dim != null ? dim.getThick() : null,
                item.getEdiCode(),
                item.getFormCodeName()
        );
    }
}
