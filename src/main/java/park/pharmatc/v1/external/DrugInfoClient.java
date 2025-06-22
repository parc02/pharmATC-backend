package park.pharmatc.v1.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import park.pharmatc.v1.dto.DrugDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DrugInfoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_BASE = "https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService02/getMdcinGrnIdntfcInfoList02";
    private static final String API_KEY = "5FlmlUmrFvqEcJSuphoLdiC2hiqjs5AhZ2FyCcQrz21mcnrxN79/qveZuc8LLMVvwCm0wNuH2CwKnsP4RaJAPg==";

    public List<DrugDto> fetchAllDrugsParallel() {
        int numOfRows = 100;
        URI firstUri = buildUri(1, numOfRows);
        DrugApiResponse firstResponse = restTemplate.getForObject(firstUri, DrugApiResponse.class);

        if (firstResponse == null || firstResponse.body == null || firstResponse.body.items == null) {
            throw new RuntimeException("첫 페이지 응답이 비정상입니다.");
        }

        int totalCount = firstResponse.body.totalCount;
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
        log.info("📦 공공 API totalCount: {}, totalPages: {}", totalCount, totalPages);

        List<DrugDto> result = new ArrayList<>();

        // 첫 페이지
        List<DrugDto> firstPage = firstResponse.body.items.stream()
                .map(this::convertToDto)
                .filter(this::isValidItem)
                .toList();
        result.addAll(firstPage);

        // 나머지 병렬 수집
        List<DrugDto> rest = IntStream.rangeClosed(2, totalPages)
                .parallel()
                .mapToObj(page -> {
                    List<DrugDto> pageItems = new ArrayList<>();
                    try {
                        URI uri = buildUri(page, numOfRows);
                        DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);
                        if (response == null || response.body == null || response.body.items == null) {
                            log.warn("⚠ 페이지 {} 응답 누락", page);
                            return pageItems;
                        }

                        for (DrugApiResponse.Item item : response.body.items) {
                            DrugDto dto = convertToDto(item);
                            if (isValidItem(dto)) {
                                pageItems.add(dto);
                                if ("200302457".equals(dto.itemSeq())) {
                                    log.warn("🟢 유니펙탄 200302457 발견 (page {})", page);
                                }
                            }
                        }

                        log.info("✅ page {} → {}건 수신", page, pageItems.size());
                    } catch (Exception e) {
                        log.warn("❗ 페이지 {} 불러오기 실패: {}", page, e.getMessage());
                    }
                    return pageItems;
                })
                .flatMap(List::stream)
                .toList();

        result.addAll(rest);
        return result;
    }

    private boolean isValidItem(DrugDto dto) {
        return dto.itemSeq() != null && dto.itemName() != null && dto.formCodeName() != null
                && dto.lengLong() > 0 && dto.lengShort() > 0 && dto.thick() > 0;
    }

    private DrugDto convertToDto(DrugApiResponse.Item item) {
        return new DrugDto(
                item.ITEM_SEQ,
                item.ITEM_NAME,
                item.ENTP_SEQ,
                item.ENTP_NAME,
                item.ITEM_IMAGE,
                parseDouble(item.LENG_LONG),
                parseDouble(item.LENG_SHORT),
                parseDouble(item.THICK),
                item.EDI_CODE,
                item.FORM_CODE_NAME
        );
    }

    private double parseDouble(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return -1.0;
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return -1.0;
        }
    }

    private URI buildUri(int page, int numOfRows) {
        return UriComponentsBuilder.fromHttpUrl(API_BASE)
                .queryParam("serviceKey", UriUtils.encodeQueryParam(API_KEY, StandardCharsets.UTF_8))
                .queryParam("type", "json")
                .queryParam("pageNo", page)
                .queryParam("numOfRows", numOfRows)
                .build(true)
                .toUri();
    }
}
