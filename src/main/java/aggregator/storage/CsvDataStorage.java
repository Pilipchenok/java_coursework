package aggregator.storage;

import aggregator.model.AggregatedCryptoData;
import aggregator.model.WriteMode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CsvDataStorage implements DataStorage {

    private static final String[] HEADERS = {"sourceName", "symbol", "priceUsd", "change24h", "timestamp"};

    private final Path filePath;

    public CsvDataStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void save(List<AggregatedCryptoData> data, WriteMode mode) {
        try {
            boolean fileExistsAndNotEmpty = Files.exists(filePath) && Files.size(filePath) > 0;
            boolean isAppend = (mode == WriteMode.APPEND && fileExistsAndNotEmpty);

            BufferedWriter writer = isAppend
                    ? Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                    : Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            CSVFormat format = isAppend
                    ? CSVFormat.DEFAULT
                    : CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

            try (writer; CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (AggregatedCryptoData item : data) {
                    printer.printRecord(
                            item.sourceName(),
                            item.symbol(),
                            item.priceUsd(),
                            item.change24h(),
                            item.timestamp().toString()
                    );
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV in: " + filePath, e);
        }
    }

    @Override
    public List<AggregatedCryptoData> readAll() {
        List<AggregatedCryptoData> result = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return result;
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader reader = Files.newBufferedReader(filePath);
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                AggregatedCryptoData item = new AggregatedCryptoData(
                        record.get("sourceName"),
                        record.get("symbol"),
                        Double.parseDouble(record.get("priceUsd")),
                        Double.parseDouble(record.get("change24h")),
                        Instant.parse(record.get("timestamp"))
                );
                result.add(item);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV from: " + filePath, e);
        }
        return result;
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
