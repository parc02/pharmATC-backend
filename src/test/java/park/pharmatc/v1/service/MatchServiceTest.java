package park.pharmatc.v1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import park.pharmatc.v1.dto.DrugDto;
import park.pharmatc.v1.dto.MatchRequest;
import park.pharmatc.v1.dto.MatchResponse;
import park.pharmatc.v1.entity.*;
import park.pharmatc.v1.repository.DrugItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchServiceTest {

    private DrugItemRepository drugItemRepository;
    private MatchService matchService;

    @BeforeEach
    void setUp() {
        drugItemRepository = mock(DrugItemRepository.class);
        matchService = new MatchService(drugItemRepository);
    }

    @Test
    void findMatches_필터링정상작동() {
        // given
        DrugItemEntity 기준약 = makeDrug("1000", "정제", 8.0, 3.0, 2.0);
        when(drugItemRepository.findByItemSeq("1000"))
                .thenReturn(Optional.of(기준약));

        List<DrugItemEntity> 전체 = List.of(
                기준약,
                makeDrug("1001", "정제", 7.9, 3.1, 2.1),  // 허용됨
                makeDrug("1002", "정제", 10.0, 5.0, 3.5), // 크기 너무 큼
                makeDrug("1003", "시럽", 8.0, 3.0, 2.0)   // 제형 다름이지만 제형 기준 X
        );

        when(drugItemRepository.findAllWithAssociations()).thenReturn(전체);

        // when
        MatchRequest 요청 = new MatchRequest("1000", 10); // 10% 오차
        MatchResponse 응답 = matchService.findMatches(요청);

        // then
        List<DrugDto> 결과 = 응답.matchedDrugs();
        assertEquals(2, 결과.size());
        assertEquals("1001", 결과.get(0).itemSeq());
    }

    private DrugItemEntity makeDrug(String itemSeq, String form, double lenLong, double lenShort, double thick) {
        DrugItemEntity item = new DrugItemEntity();
        item.setItemSeq(itemSeq);
        item.setItemName("약품" + itemSeq);
        item.setFormCodeName(form);
        item.setEdiCode("EDI" + itemSeq);

        DrugCompanyEntity company = new DrugCompanyEntity();
        company.setEntpSeq("ENTP" + itemSeq);
        company.setEntpName("회사" + itemSeq);
        item.setCompany(company);

        DrugDimensionsEntity dim = new DrugDimensionsEntity();
        dim.setLengLong(lenLong);
        dim.setLengShort(lenShort);
        dim.setThick(thick);
        dim.setDrugItem(item);
        item.setDimensions(dim);

        DrugImagesEntity image = new DrugImagesEntity();
        image.setItemImage("image.jpg");
        image.setDrugItem(item);
        item.setImage(image);

        return item;
    }
}
