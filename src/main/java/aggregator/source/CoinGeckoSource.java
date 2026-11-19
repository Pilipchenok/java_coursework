package aggregator.source;

import aggregator.client.HttpClientService;
import aggregator.model.AggregatedCryptoData;
import aggregator.model.dto.CoinGeckoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CoinGeckoSource implements CryptoSource {

    private final HttpClientService httpClient;
    private final ObjectMapper objectMapper;

    public CoinGeckoSource(HttpClientService httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "coingecko";
    }

    @Override
    public AggregatedCryptoData fetch(Map<String, String> params) {
        String currency = params.getOrDefault("vs_currency", "usd");
        String coinId = params.getOrDefault("ids", "bitcoin");

        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency="
                + currency + "&ids=" + coinId + "&sparkline=true";

        String json = httpClient.get(url);

        try {
            CoinGeckoDto[] dtos = objectMapper.readValue(json, CoinGeckoDto[].class);

            if (dtos == null || dtos.length == 0) {
                throw new RuntimeException("Empty data in CoinGecko for: " + coinId);
            }

            CoinGeckoDto coin = dtos[0];

            return new AggregatedCryptoData(
                    getName(),
                    coin.symbol.toUpperCase(),
                    coin.currentPrice,
                    coin.priceChangePercentage24h,
                    Instant.now()
            );
        } catch (Exception e) {
            throw new RuntimeException("Responce parcing error, CoinGecko: " + e.getMessage(), e);
        }
    }
}
