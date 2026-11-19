package aggregator.model;

import java.time.Instant;

public record AggregatedCryptoData(
        String sourceName,
        String symbol,
        double priceUsd,
        double change24h,
        Instant timestamp
) {}
