package park.pharmatc.v1.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import park.pharmatc.v1.dto.DrugDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
public class DrugInfoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_BASE = "https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService02/getMdcinGrnIdntfcInfoList02";
    private static final String API_KEY = "5FlmlUmrFvqEcJSuphoLdiC2hiqjs5AhZ2FyCcQrz21mcnrxN79/qveZuc8LLMVvwCm0wNuH2CwKnsP4RaJAPg==";

    public int getTotalPageCount() {
        DrugApiResponse response = getDrugItems("1");
        if (response == null || response.body == null) {
            throw new RuntimeException("응답이 비정상입니다.");
        }
        int totalCount = response.body.totalCount;
        return (int) Math.ceil(totalCount / 100.0);
    }

    public DrugApiResponse getDrugItems(String pageNo) {
        URI uri = buildUri(null, Integer.parseInt(pageNo), 100);
        return restTemplate.getForObject(uri, DrugApiResponse.class);
    }

    public List<DrugDto> fetchAllDrugsParallel() {
        int numOfRows = 100;
        URI firstUri = buildUri(null, 1, numOfRows);
        DrugApiResponse firstResponse = restTemplate.getForObject(firstUri, DrugApiResponse.class);

        if (firstResponse == null || firstResponse.body == null || firstResponse.body.items == null) {
            throw new RuntimeException("첫 페이지 응답이 비정상입니다.");
        }

        int totalCount = firstResponse.body.totalCount;
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
        log.info("공공 API totalCount: {}, totalPages: {}", totalCount, totalPages);

        List<DrugDto> result = new ArrayList<>(totalCount);

        List<DrugDto> firstPage = firstResponse.body.items.stream()
                .filter(this::isValidItem)
                .map(this::convertToDto)
                .toList();

        result.addAll(firstPage);

        List<DrugDto> rest = IntStream.rangeClosed(2, totalPages)
                .parallel()
                .mapToObj(page -> {
                    try {
                        URI uri = buildUri(null, page, numOfRows);
                        DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);
                        if (response == null || response.body == null || response.body.items == null)
                            return List.<DrugDto>of();

                        return response.body.items.stream()
                                .filter(this::isValidItem)
                                .map(this::convertToDto)
                                .toList();
                    } catch (Exception e) {
                        log.warn("❗ 페이지 {} 불러오기 실패: {}", page, e.getMessage());
                        return List.<DrugDto>of();
                    }
                })
                .flatMap(List::stream)
                .toList();

        result.addAll(rest);
        return result;
    }

    private boolean isValidItem(DrugApiResponse.Item i) {
        return i.ITEM_SEQ != null && i.ITEM_NAME != null && i.FORM_CODE_NAME != null &&
                i.LENG_LONG != null && i.LENG_SHORT != null && i.THICK != null;
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
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private URI buildUri(String itemSeq, int page, int numOfRows) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_BASE)
                .queryParam("serviceKey", UriUtils.encodeQueryParam(API_KEY, StandardCharsets.UTF_8))
                .queryParam("type", "json")
                .queryParam("pageNo", page)
                .queryParam("numOfRows", numOfRows);

        if (itemSeq != null) {
            builder.queryParam("item_seq", itemSeq);
        }

        return builder.build(true).toUri();
    }
}
