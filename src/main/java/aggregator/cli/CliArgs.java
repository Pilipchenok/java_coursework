package aggregator.cli;

import aggregator.model.OutputFormat;
import aggregator.model.WriteMode;

import java.util.List;
import java.util.Map;

public record CliArgs(
        String action,
        List<String> sources,
        OutputFormat format,
        WriteMode mode,
        String sourceFilter,
        Map<String, String> queryParams
) {}
