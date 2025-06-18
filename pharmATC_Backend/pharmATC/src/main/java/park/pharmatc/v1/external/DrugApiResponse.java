package park.pharmatc.v1.external;

import java.util.List;

public class DrugApiResponse {
    public Body body;

    public static class Body {
        public int totalCount;
        public List<Item> items;
    }

    public static class Item {
        public String ITEM_SEQ;           // 품목일련번호
        public String ITEM_NAME;          // 품목명
        public String ENTP_SEQ;           // 업체일련번호
        public String ENTP_NAME;          // 업체명
        public String ITEM_IMAGE;         // 큰제품이미지

        public String LENG_LONG;          // 크기 장축
        public String LENG_SHORT;         // 크기 단축
        public String THICK;              // 크기 두께

        public String EDI_CODE;           // 보험코드
        public String FORM_CODE_NAME;     // 제형코드이름
    }
}
