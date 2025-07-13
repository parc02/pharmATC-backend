package park.pharmatc.v1.dto;

import java.util.UUID;

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
        UUID instanceId
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
                             String formCodeName) {
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
                UUID.randomUUID()  // 각각 DTO 인스턴스를 유일하게 식별
        );
    }
}
