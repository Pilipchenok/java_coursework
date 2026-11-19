package aggregator.storage;

import aggregator.model.AggregatedCryptoData;
import aggregator.model.WriteMode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonDataStorage implements DataStorage {

    private final Path filePath;
    private final ObjectMapper objectMapper;

    public JsonDataStorage(Path filePath, ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(List<AggregatedCryptoData> newData, WriteMode mode) {
        List<AggregatedCryptoData> allData = new ArrayList<>();

        if (mode == WriteMode.APPEND && Files.exists(filePath)) {
            allData.addAll(readAll());
        }

        allData.addAll(newData);

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), allData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON in: " + filePath, e);
        }
    }

    @Override
    public List<AggregatedCryptoData> readAll() {
        try {
            if (!Files.exists(filePath) || Files.size(filePath) == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(filePath.toFile(), new TypeReference<List<AggregatedCryptoData>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from: " + filePath, e);
        }
    }

    @Override
    public List<AggregatedCryptoData> readBySource(String sourceName) {
        List<AggregatedCryptoData> filtered = new ArrayList<>();

        for (AggregatedCryptoData item : readAll()) {
            if (item.sourceName().equalsIgnoreCase(sourceName)) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
