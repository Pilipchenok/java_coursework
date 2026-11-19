package aggregator.source;

import aggregator.client.HttpClientService;
import aggregator.model.AggregatedCryptoData;
import aggregator.model.dto.KuCoinDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class KuCoinSource implements CryptoSource {

    private final HttpClientService httpClient;
    private final ObjectMapper objectMapper;

    public KuCoinSource(HttpClientService httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "kucoin";
    }

    @Override
    public AggregatedCryptoData fetch(Map<String, String> params) {
        String symbolPair = params.getOrDefault("symbol", "BTC-USDT");

        String url = "https://api.kucoin.com/api/v1/market/stats?symbol=" + symbolPair;

        String json = httpClient.get(url);

        try {
            KuCoinDto dto = objectMapper.readValue(json, KuCoinDto.class);

            if (dto.data == null || dto.data.last == null) {
                throw new RuntimeException("Empty data in KuCoin for: " + symbolPair);
            }

            double price = Double.parseDouble(dto.data.last);
            double changePercent = dto.data.changeRate != null
                    ? Double.parseDouble(dto.data.changeRate) * 100.0
                    : 0.0;

            String cleanSymbol = dto.data.symbol.split("-")[0];

            return new AggregatedCryptoData(
                    getName(),
                    cleanSymbol,
                    price,
                    changePercent,
                    Instant.ofEpochMilli(dto.data.time)
            );
        } catch (Exception e) {
            throw new RuntimeException("Responce parcing error, KuCoin: " + e.getMessage(), e);
        }
    }
}
