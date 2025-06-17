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
import java.util.Optional;

@Component
public class DrugInfoClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_BASE = "https://apis.data.go.kr/1471000/MdcinGrnIdntfcInfoService02/getMdcinGrnIdntfcInfoList02";
    private static final String API_KEY = "5FlmlUmrFvqEcJSuphoLdiC2hiqjs5AhZ2FyCcQrz21mcnrxN79/qveZuc8LLMVvwCm0wNuH2CwKnsP4RaJAPg==";

    public DrugDto fetchDrugInfo(String keyword) {
        URI uri = UriComponentsBuilder.fromHttpUrl(API_BASE)
                .queryParam("serviceKey", UriUtils.encodeQueryParam(API_KEY, StandardCharsets.UTF_8))
                .queryParam("Prduct", UriUtils.encodeQueryParam(keyword, StandardCharsets.UTF_8))
                .queryParam("type", "json")
                .queryParam("numOfRows", 100)
                .build(true)
                .toUri();

        DrugApiResponse response = restTemplate.getForObject(uri, DrugApiResponse.class);

        if (response == null || response.body == null || response.body.items == null || response.body.items.isEmpty()) {
            throw new RuntimeException("의약품 정보를 찾을 수 없습니다.");
        }

        String keywordNorm = normalize(keyword);

        DrugApiResponse.Item selected = response.body.items.stream()
                .filter(i -> i.ITEM_NAME != null && i.FORM_CODE_NAME != null && i.LENG_LONG != null && i.THICK != null)
                .min((a, b) -> levenshtein(normalize(a.ITEM_NAME), keywordNorm)
                        - levenshtein(normalize(b.ITEM_NAME), keywordNorm))
                .orElseThrow(() -> new RuntimeException("입력한 약품명과 일치하는 결과가 없습니다."));

        return new DrugDto(
                selected.ITEM_NAME,
                selected.EDI_CODE,
                selected.FORM_CODE_NAME,
                parseDouble(selected.LENG_LONG),
                parseDouble(selected.THICK)
        );
    }

    public List<DrugDto> fetchAllDrugs() {
        List<DrugDto> result = new ArrayList<>();
        int pageNo = 1;
        int numOfRows = 100;

        URI firstUri = buildUri(pageNo, numOfRows);
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
                    .filter(i -> i.ITEM_NAME != null && i.FORM_CODE_NAME != null && i.LENG_LONG != null && i.THICK != null)
                    .map(i -> new DrugDto(
                            i.ITEM_NAME,
                            i.EDI_CODE,
                            i.FORM_CODE_NAME,
                            parseDouble(i.LENG_LONG),
                            parseDouble(i.THICK)
                    ))
                    .toList();

            result.addAll(pageDrugs);
        }

        return result;
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
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", "")
                .replaceAll("[^ㄱ-ㅎ가-힣a-zA-Z0-9]", "")
                .toLowerCase();
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                            Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[a.length()][b.length()];
    }
}
