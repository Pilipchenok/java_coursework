package aggregator.source;

import aggregator.client.HttpClientService;
import aggregator.model.AggregatedCryptoData;
import aggregator.model.dto.CoinCapDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CoinCapSource implements CryptoSource {

    private final HttpClientService httpClient;
    private final ObjectMapper objectMapper;

    public CoinCapSource(HttpClientService httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "coincap";
    }

    @Override
    public AggregatedCryptoData fetch(Map<String, String> params) {
        String searchCoin = params.getOrDefault("search", "bitcoin");
        String limit = params.getOrDefault("limit", "1");

        String url = "https://api.coincap.io/v2/assets?search=" + searchCoin + "&limit=" + limit;
        String json = httpClient.get(url);

        try {
            CoinCapDto dto = objectMapper.readValue(json, CoinCapDto.class);

            if (dto.data == null || dto.data.isEmpty()) {
                throw new RuntimeException("Empty data in CoinCap for: " + searchCoin);
            }

            CoinCapDto.AssetData asset = dto.data.get(0);
            double price = Double.parseDouble(asset.priceUsd);
            double change = asset.changePercent24Hr != null ? Double.parseDouble(asset.changePercent24Hr) : 0.0;

            return new AggregatedCryptoData(
                    getName(),
                    asset.symbol.toUpperCase(),
                    price,
                    change,
                    Instant.now()
            );
        } catch (Exception e) {
            throw new RuntimeException("Responce parcing error, CoinCap: " + e.getMessage(), e);
        }
    }
}
