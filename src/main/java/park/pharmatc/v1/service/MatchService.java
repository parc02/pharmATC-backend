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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);
    private final DrugItemRepository drugItemRepository;

    @Transactional(readOnly = true)
    public MatchResponse findMatches(MatchRequest request) {
        System.out.println("Processing match request with id: " + request.id() + " and tolerance: " + request.tolerance());

        try {
            // 기준 약품을 id로 찾기 (id는 Long 타입)
            DrugItemEntity base = drugItemRepository.findById(request.id())
                    .orElse(null);

            if (base == null) {
                log.warn("기준 약품을 찾을 수 없습니다: {}", request.id());
                return new MatchResponse(Collections.emptyList());
            }

            // dimensions가 리스트인 경우 첫 번째 요소를 사용
            DrugDimensionsEntity baseDim = base.getDimensions() != null && !base.getDimensions().isEmpty()
                    ? base.getDimensions().get(0)
                    : null;

            if (!isValidDimensions(baseDim)) {
                log.warn("기준 약품에 유효한 크기 정보가 없습니다: {}", request.id());
                return new MatchResponse(Collections.emptyList());
            }

            double margin = request.tolerance() / 100.0;
            // findAll()을 사용하여 모든 약품을 가져옵니다.
            List<DrugItemEntity> allItems = drugItemRepository.findAll();

            // 결과 필터링
            List<DrugDto> matched = allItems.stream()
                    .filter(item -> !Objects.equals(item.getId(), base.getId())) // id로 비교 (Long 타입)
                    .filter(item -> {
                        DrugDimensionsEntity dim = item.getDimensions() != null && !item.getDimensions().isEmpty()
                                ? item.getDimensions().get(0)
                                : null;
                        return isValidDimensions(dim) &&
                                isSmallerOrEqual(baseDim, dim) &&
                                isWithinMargin(baseDim.getLengLong(), dim.getLengLong(), margin) &&
                                isWithinMargin(baseDim.getLengShort(), dim.getLengShort(), margin) &&
                                isWithinMargin(baseDim.getThick(), dim.getThick(), margin);
                    })
                    .map(this::toDto)  // DTO로 변환
                    .collect(Collectors.toList());

            return new MatchResponse(matched);

        } catch (Exception e) {
            log.error("MatchService 오류 발생: {}", e.getMessage(), e);
            return new MatchResponse(Collections.emptyList());  // fallback
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
        // `company`, `dimensions`, `images`을 안전하게 가져오기
        DrugCompanyEntity company = item.getCompany();
        DrugDimensionsEntity dim = item.getDimensions() != null && !item.getDimensions().isEmpty() ? item.getDimensions().get(0) : null;  // 첫 번째 요소만 사용
        DrugImagesEntity img = item.getImages() != null && !item.getImages().isEmpty() ? item.getImages().get(0) : null; // 첫 번째 이미지만 가져옴

        // DTO 객체로 변환하여 반환
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
                item.getFormCodeName(),
                item.getId()  // 수정된 부분: id 필드 추가
        );
    }
}
