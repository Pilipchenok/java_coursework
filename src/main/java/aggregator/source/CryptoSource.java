package aggregator.source;

import aggregator.model.AggregatedCryptoData;
import java.util.Map;

public interface CryptoSource {
    String getName();

    AggregatedCryptoData fetch(Map<String, String> params);
}
