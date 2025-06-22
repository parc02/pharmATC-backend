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
            DrugItemEntity base = drugItemRepository.findByItemSeq(request.itemSeq())
                    .orElse(null);

            if (base == null) {
                log.warn("기준 약품을 찾을 수 없습니다: {}", request.itemSeq());
                return new MatchResponse(Collections.emptyList());
            }

            DrugDimensionsEntity baseDim = base.getDimensions();
            if (!isValidDimensions(baseDim)) {
                log.warn("기준 약품에 유효한 크기 정보가 없습니다: {}", request.itemSeq());
                return new MatchResponse(Collections.emptyList());
            }

            double margin = request.tolerance() / 100.0;
            List<DrugItemEntity> allItems = drugItemRepository.findAllWithAssociations();

            List<DrugDto> matched = allItems.stream()
                    .filter(item -> !Objects.equals(item.getItemSeq(), base.getItemSeq()))
                    .filter(item -> {
                        DrugDimensionsEntity dim = item.getDimensions();
                        return isValidDimensions(dim) &&
                                isSmallerOrEqual(baseDim, dim) &&
                                isWithinMargin(baseDim.getLengLong(), dim.getLengLong(), margin) &&
                                isWithinMargin(baseDim.getLengShort(), dim.getLengShort(), margin) &&
                                isWithinMargin(baseDim.getThick(), dim.getThick(), margin);
                    })
                    .map(this::toDto)
                    .toList();

            return new MatchResponse(matched);

        } catch (Exception e) {
            log.error("MatchService 오류 발생: {}", e.getMessage(), e);
            return new MatchResponse(Collections.emptyList()); // fallback
        }
    }

    private boolean isValidDimensions(DrugDimensionsEntity dim) {
        return dim != null && dim.getLengLong() != null && dim.getLengShort() != null && dim.getThick() != null;
    }

    private boolean isSmallerOrEqual(DrugDimensionsEntity base, DrugDimensionsEntity target) {
        return target.getLengLong() <= base.getLengLong()
                && target.getLengShort() <= base.getLengShort()
                && target.getThick() <= base.getThick();
    }

    private boolean isWithinMargin(Double base, Double value, double margin) {
        return base != null && value != null && base != 0 &&
                Math.abs(value - base) / base <= margin;
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
