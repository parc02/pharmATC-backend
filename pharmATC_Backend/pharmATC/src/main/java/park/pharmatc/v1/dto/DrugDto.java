package park.pharmatc.v1.dto;

public record DrugDto(
        String itemSeq,         // 품목일련번호
        String itemName,        // 약품명
        String entpSeq,         // 업체일련번호
        String entpName,        // 업체명
        String itemImage,       // 큰제품이미지
        Double lengLong,        // 크기 장축
        Double lengShort,       // 크기 단축
        Double thick,           // 크기 두께
        String ediCode,         // 보험코드
        String formCodeName     // 제형코드이름
) {}
