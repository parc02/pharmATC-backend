package park.pharmatc.v1.external;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import park.pharmatc.v1.dto.DrugDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DrugInfoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_BASE = "https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService02/getMdcinGrnIdntfcInfoList02";
    private static final String API_KEY = "5FlmlUmrFvqEcJSuphoLdiC2hiqjs5AhZ2FyCcQrz21mcnrxN79/qveZuc8LLMVvwCm0wNuH2CwKnsP4RaJAPg==";

    public DrugDto fetchDrugInfo(String itemSeq) {
        URI uri = UriComponentsBuilder.fromHttpUrl(API_BASE)
                .queryParam("serviceKey", UriUtils.encodeQueryParam(API_KEY, StandardCharsets.UTF_8))
                .queryParam("item_seq", itemSeq)
                .queryParam("type", "json")
                .queryParam("numOfRows", 100) // 1보다 넉넉히 요청
                .build(true)
                .toUri();

        DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);

        if (response == null || response.body == null || response.body.items == null || response.body.items.isEmpty()) {
            throw new RuntimeException("item_seq로 약품을 찾을 수 없습니다: " + itemSeq);
        }

        DrugApiResponse.Item item = response.body.items.stream()
                .filter(i -> itemSeq.equals(i.ITEM_SEQ))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("응답에 item_seq가 일치하는 항목이 없습니다: " + itemSeq));

        return new DrugDto(
                item.ITEM_NAME,
                item.ITEM_SEQ,
                item.FORM_CODE_NAME,
                parseDouble(item.LENG_LONG),
                parseDouble(item.LENG_SHORT),
                parseDouble(item.THICK)
        );
    }

    public List<DrugDto> fetchAllDrugs() {
        List<DrugDto> result = new ArrayList<>();
        int numOfRows = 100;

        URI firstUri = buildUri(1, numOfRows);
        DrugApiResponse firstResponse = restTemplate.getForObject(firstUri, DrugApiResponse.class);
        if (firstResponse == null || firstResponse.body == null || firstResponse.body.items == null) {
            throw new RuntimeException("첫 페이지 응답이 비정상입니다.");
        }

        int totalCount = firstResponse.body.totalCount;
        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

        for (int page = 1; page <= totalPages; page++) {
            URI uri = buildUri(page, numOfRows);
            DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);
            if (response == null || response.body == null || response.body.items == null) continue;

            List<DrugDto> pageDrugs = response.body.items.stream()
                    .filter(this::isValidItem)
                    .map(i -> new DrugDto(
                            i.ITEM_NAME,
                            i.ITEM_SEQ,
                            i.FORM_CODE_NAME,
                            parseDouble(i.LENG_LONG),
                            parseDouble(i.LENG_SHORT),
                            parseDouble(i.THICK)
                    ))
                    .toList();

            result.addAll(pageDrugs);
        }

        return result;
    }

    private boolean isValidItem(DrugApiResponse.Item i) {
        return i.ITEM_NAME != null && i.FORM_CODE_NAME != null && i.LENG_LONG != null && i.THICK != null;
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

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
