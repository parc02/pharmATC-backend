package park.pharmatc.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.external.DrugInfoClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final DrugInfoClient drugInfoClient;

    public MatchResponse findMatches(MatchRequest request) {

        //1. 기준 약품 조회
        DrugDto target = drugInfoClient.fetchDrugInfo(request.itemSeq());

        //2. 전체 약품 목록 조회
        List<DrugDto> allDrugs = drugInfoClient.fetchAllDrugs();

        //3. 필터링: 제형 동일 + 크기 오차 범위 내
        List<DrugDto> matched = allDrugs.stream()
                .filter(d -> d.dosageForm() != null && d.dosageForm().equals(target.dosageForm()))
                .filter(d -> isWithinTolerance(target, d, request.tolerance()))
                .toList();

        //4. 결과 반환
        return new MatchResponse(matched);
    }

    private boolean isWithinTolerance(DrugDto base, DrugDto candidate, int tolerance) {
        double rate = tolerance / 100.0;

        boolean longOk = Math.abs(base.lengLong() - candidate.lengLong()) <= base.lengLong() * rate;
        boolean shortOk = Math.abs(base.lengShort() - candidate.lengShort()) <= base.lengShort() * rate;
        boolean thicknessOk = Math.abs(base.thickness() - candidate.thickness()) <= base.thickness() * rate;

        return longOk && shortOk && thicknessOk;
    }



}
