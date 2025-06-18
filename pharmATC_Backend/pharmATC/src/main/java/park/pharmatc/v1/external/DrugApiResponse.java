package park.pharmatc.v1.external;

import java.util.List;

public class DrugApiResponse {
    public Body body;

    public static class Body {
        public int totalCount;
        public List<Item> items;
    }

    public static class Item {
        public String ITEM_NAME;
        public String ITEM_SEQ;
        public String FORM_CODE_NAME;
        public String LENG_LONG;
        public String LENG_SHORT;
        public String THICK;
    }


}
