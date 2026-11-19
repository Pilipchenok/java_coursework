package aggregator.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KuCoinDto {
    public String code;
    public StatsData data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatsData {
        public long time;
        public String symbol;
        public String buy;
        public String sell;
        public String changeRate;
        public String high;
        public String low;
        public String last;
    }
}