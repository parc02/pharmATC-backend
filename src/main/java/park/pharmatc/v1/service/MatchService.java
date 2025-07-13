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

import java.util.*;

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

            DrugDimensionsEntity baseDim = null;
            if (base.getDimensions() != null && !base.getDimensions().isEmpty()) {
                baseDim = base.getDimensions().get(0);
            }

            if (!isValidDimensions(baseDim)) {
                log.warn("기준 약품에 유효한 크기 정보가 없습니다: {}", request.itemSeq());
                return new MatchResponse(Collections.emptyList());
            }

            final DrugDimensionsEntity finalBaseDim = baseDim;
            final double margin = request.tolerance() / 100.0;

            List<DrugItemEntity> allItems = drugItemRepository.findAllWithAssociations();

            List<DrugDto> matched = allItems.stream()
                    .filter(item -> !Objects.equals(item.getItemSeq(), base.getItemSeq()))
                    .map(item -> {
                        DrugDimensionsEntity dim = (item.getDimensions() != null && !item.getDimensions().isEmpty())
                                ? item.getDimensions().get(0) : null;
                        return new Object[]{item, dim};
                    })
                    .filter(pair -> {
                        DrugDimensionsEntity dim = (DrugDimensionsEntity) pair[1];
                        return isValidDimensions(dim) &&
                                isSmallerOrEqual(finalBaseDim, dim) &&
                                isWithinMargin(finalBaseDim.getLengLong(), dim.getLengLong(), margin) &&
                                isWithinMargin(finalBaseDim.getLengShort(), dim.getLengShort(), margin) &&
                                isWithinMargin(finalBaseDim.getThick(), dim.getThick(), margin);
                    })
                    .map(pair -> toDto((DrugItemEntity) pair[0], (DrugDimensionsEntity) pair[1]))
                    .toList();

            return new MatchResponse(matched);

        } catch (Exception e) {
            log.error("MatchService 오류 발생: {}", e.getMessage(), e);
            return new MatchResponse(Collections.emptyList());
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

    private DrugDto toDto(DrugItemEntity item, DrugDimensionsEntity dim) {
        DrugCompanyEntity company = item.getCompany();
        DrugImagesEntity img = (item.getImages() != null && !item.getImages().isEmpty())
                ? item.getImages().get(0) : null;

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
                item.getFormCodeName()
        );
    }
}
