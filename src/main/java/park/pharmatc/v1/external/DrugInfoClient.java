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

        // 나머지 페이지 (순차 처리 + 재시도)
        List<DrugDto> rest = IntStream.rangeClosed(2, totalPages)
                .mapToObj(page -> fetchPageWithRetry(page, 3))
                .flatMap(List::stream)
                .toList();

        result.addAll(rest);

        log.info("✅ 전체 수신 약품 수: {}", result.size());
        return result;
    }

    private List<DrugDto> fetchPageWithRetry(int page, int retries) {
        for (int i = 1; i <= retries; i++) {
            try {
                URI uri = buildUri(page, 100);
                DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);

                if (response != null && response.body != null && response.body.items != null) {
                    List<DrugDto> pageItems = response.body.items.stream()
                            .map(this::convertToDto)
                            .filter(this::isValidItem)
                            .toList();

                    log.info("✅ page {} (시도 {}회차): {}건 수신", page, i, pageItems.size());
                    return pageItems;
                } else {
                    log.warn("⚠ page {} (시도 {}회차): 응답 없음", page, i);
                }
            } catch (Exception e) {
                log.warn("❗ page {} (시도 {}회차): 예외 발생 - {}", page, i, e.getMessage());
            }
        }
        log.error("❌ page {} 최종 실패 - 재시도 끝", page);
        return new ArrayList<>();
    }

    private boolean isValidItem(DrugDto dto) {
        // itemSeq, itemName, formCodeName, lengLong, lengShort, thick 값이 모두 유효하고, id도 유효한지 확인
        return dto.itemSeq() != null && dto.itemName() != null && dto.formCodeName() != null
                && dto.lengLong() > 0 && dto.lengShort() > 0 && dto.thick() > 0
                && dto.id() != null;  // id 값도 유효성 검사에 포함
    }

    private DrugDto convertToDto(DrugApiResponse.Item item) {
        // itemSeq가 null이거나 빈 문자열인 경우를 필터링
        if (item.ITEM_SEQ == null || item.ITEM_SEQ.trim().isEmpty()) {
            log.warn("❌ itemSeq is null or empty for item: {}", item);
            return null;  // 유효하지 않은 데이터는 건너뛰기
        }

        // itemSeq를 id로 Long 변환
        Long id = Long.parseLong(item.ITEM_SEQ);  // itemSeq를 Long으로 변환

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
                item.FORM_CODE_NAME,
                id  // itemSeq를 Long 타입의 id로 변환하여 전달
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
