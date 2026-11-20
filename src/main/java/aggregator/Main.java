package aggregator;

import aggregator.cli.CliRunner;
import aggregator.client.HttpClientService;
import aggregator.client.JavaHttpClient;
import aggregator.model.OutputFormat;
import aggregator.service.AggregatorService;
import aggregator.source.CoinGeckoSource;
import aggregator.source.CoinPaprikaSource;
import aggregator.source.CryptoSource;
import aggregator.source.KuCoinSource;
import aggregator.storage.CsvDataStorage;
import aggregator.storage.DataStorage;
import aggregator.storage.JsonDataStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            HttpClientService httpClient = new JavaHttpClient();

            List<CryptoSource> sources = List.of(
                    new CoinPaprikaSource(httpClient, objectMapper),
                    new CoinGeckoSource(httpClient, objectMapper),
                    new KuCoinSource(httpClient, objectMapper)
            );

            Map<OutputFormat, DataStorage> storages = Map.of(
                    OutputFormat.JSON, new JsonDataStorage(Path.of("aggregated_data.json"), objectMapper),
                    OutputFormat.CSV, new CsvDataStorage(Path.of("aggregated_data.csv"))
            );

            AggregatorService aggregatorService = new AggregatorService(sources, storages);
            CliRunner cliRunner = new CliRunner(aggregatorService);
            cliRunner.run(args);

        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в параметрах запуска: " + extractErrorMessage(e));
            System.exit(1);

        } catch (Exception e) {
            System.err.println("Ошибка выполнения: " + extractErrorMessage(e));
            System.exit(1);
        }
    }

    private static String extractErrorMessage(Throwable throwable) {
        String message = throwable.getMessage();

        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        if (message == null || message.isBlank()) {
            String rootMsg = rootCause.getMessage();
            return (rootMsg != null && !rootMsg.isBlank())
                    ? rootCause.getClass().getSimpleName() + ": " + rootMsg
                    : rootCause.getClass().getSimpleName();
        }

        if (message.endsWith(": null")) {
            return message.substring(0, message.length() - 4) + rootCause.getClass().getSimpleName();
        }

        return message;
    }
}