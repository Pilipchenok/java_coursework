package aggregator.cli;

import aggregator.model.AggregatedCryptoData;
import aggregator.model.OutputFormat;
import aggregator.model.WriteMode;
import aggregator.service.AggregatorService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliRunner {

    private final AggregatorService aggregatorService;

    public CliRunner(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    public void run(String[] rawArgs) {
        if (rawArgs.length == 0) {
            printHelp();
            return;
        }

        CliArgs args = parseArguments(rawArgs);

        if ("poll".equalsIgnoreCase(args.action())) {
            List<AggregatedCryptoData> saved = aggregatorService.pollAndSave(
                    args.sources(),
                    args.queryParams(),
                    args.format(),
                    args.mode()
            );
            System.out.println("Successful polled: " + saved.size() + ". Format: " + args.format());
            printData(saved);

        } else if ("read".equalsIgnoreCase(args.action())) {
            List<AggregatedCryptoData> data = aggregatorService.readSavedData(
                    args.format(),
                    args.sourceFilter()
            );
            System.out.println("Read records: (" + args.format() + "): " + data.size());
            printData(data);

        } else {
            throw new IllegalArgumentException("Unknown command --action=" + args.action() + ". Must use poll or read.");
        }
    }

    private CliArgs parseArguments(String[] rawArgs) {
        Map<String, String> flags = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();

        for (String arg : rawArgs) {
            if (arg.startsWith("--") && arg.contains("=")) {
                String[] parts = arg.substring(2).split("=", 2);
                flags.put(parts[0].toLowerCase(), parts[1]);
            }
        }

        String action = flags.getOrDefault("action", "poll");
        String formatStr = flags.getOrDefault("format", "json").toUpperCase();
        String modeStr = flags.getOrDefault("mode", "create").toUpperCase();
        String sourcesStr = flags.getOrDefault("sources", "coincap,coingecko,kucoin");
        String sourceFilter = flags.get("source");

        OutputFormat format = OutputFormat.valueOf(formatStr);

        WriteMode mode = (modeStr.equals("CREATE") || modeStr.equals("CREATE_NEW"))
                ? WriteMode.CREATE_NEW
                : WriteMode.APPEND;

        List<String> sources = Arrays.asList(sourcesStr.split(","));

        if (flags.containsKey("symbol")) queryParams.put("symbol", flags.get("symbol"));
        if (flags.containsKey("ids")) queryParams.put("ids", flags.get("ids"));
        if (flags.containsKey("vs_currency")) queryParams.put("vs_currency", flags.get("vs_currency"));
        if (flags.containsKey("coin")) queryParams.put("coin", flags.get("coin"));
        if (flags.containsKey("quotes")) queryParams.put("quotes", flags.get("quotes"));

        return new CliArgs(action, sources, format, mode, sourceFilter, queryParams);
    }

    private void printData(List<AggregatedCryptoData> dataList) {
        if (dataList.isEmpty()) {
            System.out.println("[EMPTY]");
            return;
        }
        for (AggregatedCryptoData d : dataList) {
            System.out.printf("Источник: %-10s | Монета: %-5s | Цена: $%-10.2f | Изм (24ч): %+6.2f%% | Время: %s%n",
                    d.sourceName(), d.symbol(), d.priceUsd(), d.change24h(), d.timestamp());
        }
    }

    private void printHelp() {
        System.out.println("Using Guidelines:");
        System.out.println("  API polling:  --action=poll --sources=coincap,coingecko,kucoin --format=json --mode=create");
        System.out.println("  Reading:     --action=read --format=json [--source=kucoin]");
    }
}
