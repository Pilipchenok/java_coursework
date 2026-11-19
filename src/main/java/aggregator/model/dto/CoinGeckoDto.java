package aggregator.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinGeckoDto {
    public String id;
    public String symbol;
    public String name;

    @JsonProperty("current_price")
    public double currentPrice;

    @JsonProperty("market_cap")
    public double marketCap;

    @JsonProperty("price_change_percentage_24h")
    public double priceChangePercentage24h;

    @JsonProperty("sparkline_in_7d")
    public Sparkline sparklineIn7d;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sparkline {
        public List<Double> price;
    }
}
