package park.pharmatc.v1.dto;

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
        String formCodeName
) {}
