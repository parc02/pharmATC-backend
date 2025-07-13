package park.pharmatc.v1.dto;

/**
 * 약품 데이터 전송 객체 (기본 단일 이미지 기준)
 */
public record DrugDto(
        String itemSeq,
        String itemName,
        String entpSeq,
        String entpName,
        String itemImage,
        Double lengLong,
        Double lengShort,
        Double thick,
        String ediCode,
        String formCodeName,

        // 추가 필드: itemImage에 따라 구분되는 약품을 구분하기 위한 임시 ID (비DB 식별 목적)
        Long id  // UUID에서 Long으로 수정
) {
    public static DrugDto of(String itemSeq,
                             String itemName,
                             String entpSeq,
                             String entpName,
                             String itemImage,
                             Double lengLong,
                             Double lengShort,
                             Double thick,
                             String ediCode,
                             String formCodeName,
                             Long id) {  // Long 타입으로 수정
        return new DrugDto(
                itemSeq,
                itemName,
                entpSeq,
                entpName,
                itemImage,
                lengLong,
                lengShort,
                thick,
                ediCode,
                formCodeName,
                id  // Long 타입으로 전달
        );
    }
}
