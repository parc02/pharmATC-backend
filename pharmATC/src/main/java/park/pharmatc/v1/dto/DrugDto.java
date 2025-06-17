package park.pharmatc.v1.dto;

/**
 * 약 정보
 * 외부에서 API를 가저온 약품정보를 모두 이 형식으로 다룸
 */
public record DrugDto(
        String name,         // 약품 이름
        String ediCode,      // EDI 코드
        String dosageForm,   // 제형: 예) 정제, 캡슐
        double diameter,     // 지름 (mm)
        double thickness     // 두께 (mm)
) {
}
