package aggregator.storage;

import aggregator.model.AggregatedCryptoData;
import aggregator.model.WriteMode;
import java.util.List;

public interface DataStorage {
    void save(List<AggregatedCryptoData> data, WriteMode mode);

    List<AggregatedCryptoData> readAll();

    List<AggregatedCryptoData> readBySource(String sourceName);
}
