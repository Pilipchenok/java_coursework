package aggregator.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinPaprikaDto {
    public String id;
    public String name;
    public String symbol;
    public int rank;

    @JsonProperty("last_updated")
    public String lastUpdated;

    public Map<String, QuoteInfo> quotes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuoteInfo {
        public double price;

        @JsonProperty("volume_24h")
        public double volume24h;

        @JsonProperty("percent_change_24h")
        public double percentChange24h;
    }
}
