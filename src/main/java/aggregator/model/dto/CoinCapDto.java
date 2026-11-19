package aggregator.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinCapDto {
    public List<AssetData> data;
    public long timestamp;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetData {
        public String id;
        public String rank;
        public String symbol;
        public String name;
        public String priceUsd;
        public String changePercent24Hr;
    }
}
