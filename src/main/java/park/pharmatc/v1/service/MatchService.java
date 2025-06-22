package park.pharmatc.v1.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.entity.DrugCompanyEntity;
import park.pharmatc.v1.entity.DrugDimensionsEntity;
import park.pharmatc.v1.entity.DrugImagesEntity;
import park.pharmatc.v1.entity.DrugItemEntity;
import park.pharmatc.v1.repository.DrugItemRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);
    private final DrugItemRepository drugItemRepository;

    @Transactional(readOnly = true)
    public MatchResponse findMatches(MatchRequest request) {
        try {
            log.info("요청 itemSeq: {}, tolerance: {}", request.itemSeq(), request.tolerance());

            DrugItemEntity base = drugItemRepository.findByItemSeqWithAssociations(request.itemSeq())
                    .orElseThrow(() -> new IllegalArgumentException("기준 약품을 찾을 수 없습니다: " + request.itemSeq()));

            DrugDimensionsEntity baseDim = base.getDimensions();
            if (baseDim == null || baseDim.getLengLong() == null || baseDim.getLengShort() == null || baseDim.getThick() == null) {
                log.warn("기준 약품 크기 정보 없음: itemSeq={}, baseDim={}", request.itemSeq(), baseDim);
                return new MatchResponse(Collections.emptyList());
            }

            double margin = request.tolerance() / 100.0;
            List<DrugItemEntity> allItems = drugItemRepository.findAllWithAssociations();

            List<DrugDto> matched = allItems.stream()
                    .filter(d -> !Objects.equals(d.getItemSeq(), base.getItemSeq()))
                    .filter(d -> {
                        DrugDimensionsEntity dim = d.getDimensions();
                        if (dim == null || dim.getLengLong() == null || dim.getLengShort() == null || dim.getThick() == null) {
                            log.debug("제외: 크기 정보 없음 itemSeq={}", d.getItemSeq());
                            return false;
                        }
                        return isWithinMargin(baseDim.getLengLong(), dim.getLengLong(), margin) &&
                                isWithinMargin(baseDim.getLengShort(), dim.getLengShort(), margin) &&
                                isWithinMargin(baseDim.getThick(), dim.getThick(), margin);
                    })
                    .map(this::toDto)
                    .toList();

            return new MatchResponse(matched);
        } catch (Exception e) {
            log.error("MatchService 오류 발생", e);
            return new MatchResponse(Collections.emptyList());
        }
    }

    private boolean isWithinMargin(Double base, Double value, double margin) {
        if (base == null || value == null || base == 0) return false;
        double min = base * (1 - margin);
        double max = base * (1 + margin);
        return value >= min && value <= max;
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
