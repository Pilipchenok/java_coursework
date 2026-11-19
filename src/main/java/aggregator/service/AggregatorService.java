package aggregator.service;

import aggregator.model.AggregatedCryptoData;
import aggregator.model.OutputFormat;
import aggregator.model.WriteMode;
import aggregator.source.CryptoSource;
import aggregator.storage.DataStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatorService {

    private final Map<String, CryptoSource> sourcesRegistry = new HashMap<>();
    private final Map<OutputFormat, DataStorage> storages;

    public AggregatorService(List<CryptoSource> sources, Map<OutputFormat, DataStorage> storages) {
        for (CryptoSource source : sources) {
            this.sourcesRegistry.put(source.getName().toLowerCase(), source);
        }
        this.storages = storages;
    }

    public List<AggregatedCryptoData> pollAndSave(
            List<String> requestedSources,
            Map<String, String> queryParams,
            OutputFormat format,
            WriteMode mode
    ) {
        List<AggregatedCryptoData> collectedData = new ArrayList<>();

        for (String sourceName : requestedSources) {
            CryptoSource source = sourcesRegistry.get(sourceName.toLowerCase().trim());
            if (source == null) {
                throw new IllegalArgumentException("Undefined source API: " + sourceName
                        + ". You can use: " + sourcesRegistry.keySet());
            }

            AggregatedCryptoData data = source.fetch(queryParams);
            collectedData.add(data);
        }

        DataStorage storage = getStorage(format);
        storage.save(collectedData, mode);

        return collectedData;
    }

    public List<AggregatedCryptoData> readSavedData(OutputFormat format, String sourceFilter) {
        DataStorage storage = getStorage(format);

        if (sourceFilter == null || sourceFilter.isBlank()) {
            return storage.readAll();
        }
        return storage.readBySource(sourceFilter.trim());
    }

    private DataStorage getStorage(OutputFormat format) {
        DataStorage storage = storages.get(format);
        if (storage == null) {
            throw new IllegalArgumentException("Couldn't saving to: " + format);
        }
        return storage;
    }
}
