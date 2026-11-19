package aggregator.source;

import aggregator.client.HttpClientService;
import aggregator.model.AggregatedCryptoData;
import aggregator.model.dto.CoinPaprikaDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

public class CoinPaprikaSource implements CryptoSource {

    private final HttpClientService httpClient;
    private final ObjectMapper objectMapper;

    public CoinPaprikaSource(HttpClientService httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "coinpaprika";
    }

    @Override
    public AggregatedCryptoData fetch(Map<String, String> params) {
        String coinId = params.getOrDefault("coin", "btc-bitcoin");
        String quote = params.getOrDefault("quotes", "USD").toUpperCase();

        String url = "https://api.coinpaprika.com/v1/tickers/" + coinId + "?quotes=" + quote;

        String json = httpClient.get(url);

        try {
            CoinPaprikaDto dto = objectMapper.readValue(json, CoinPaprikaDto.class);

            if (dto.quotes == null || !dto.quotes.containsKey(quote)) {
                throw new RuntimeException("CoinPaprika don't return the quote: " + quote);
            }

            CoinPaprikaDto.QuoteInfo quoteInfo = dto.quotes.get(quote);

            return new AggregatedCryptoData(
                    getName(),
                    dto.symbol.toUpperCase(),
                    quoteInfo.price,
                    quoteInfo.percentChange24h,
                    Instant.now()
            );
        } catch (Exception e) {
            throw new RuntimeException("Parsing CoinPaprika error: " + e.getMessage(), e);
        }
    }
}
