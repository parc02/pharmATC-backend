package park.pharmatc.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.entity.DrugItemEntity;
import park.pharmatc.v1.repository.DrugItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final DrugItemRepository drugItemRepository;

    public MatchResponse findMatches(MatchRequest request) {
        // 1. 기준 약품 조회
        DrugItemEntity baseItem = drugItemRepository.findByItemSeq(request.itemSeq())
                .orElseThrow(() -> new IllegalArgumentException("기준 약품을 찾을 수 없습니다: " + request.itemSeq()));

        // 2. 전체 약품 조회
        List<DrugItemEntity> allItems = drugItemRepository.findAll();

        // 3. 필터링
        List<DrugDto> matched = allItems.stream()
                .filter(i -> i.getFormCodeName() != null && i.getFormCodeName().equals(baseItem.getFormCodeName()))
                .filter(i -> isWithinTolerance(baseItem, i, request.tolerance()))
                .map(this::toDto)
                .toList();

        // 4. 반환
        return new MatchResponse(matched);
    }

    private boolean isWithinTolerance(DrugItemEntity base, DrugItemEntity candidate, int tolerance) {
        double rate = tolerance / 100.0;

        double baseLong = base.getDimensions().getLengLong();
        double baseShort = base.getDimensions().getLengShort();
        double baseThick = base.getDimensions().getThick();

        double cLong = candidate.getDimensions().getLengLong();
        double cShort = candidate.getDimensions().getLengShort();
        double cThick = candidate.getDimensions().getThick();

        boolean longOk = Math.abs(baseLong - cLong) <= baseLong * rate;
        boolean shortOk = Math.abs(baseShort - cShort) <= baseShort * rate;
        boolean thickOk = Math.abs(baseThick - cThick) <= baseThick * rate;

        return longOk && shortOk && thickOk;
    }

    private DrugDto toDto(DrugItemEntity item) {
        return new DrugDto(
                item.getItemSeq(),
                item.getItemName(),
                item.getCompany().getEntpSeq(),
                item.getCompany().getEntpName(),
                item.getImage().getItemImage(),
                item.getDimensions().getLengLong(),
                item.getDimensions().getLengShort(),
                item.getDimensions().getThick(),
                item.getEdiCode(),
                item.getFormCodeName()
        );
    }
}
