package com.manufacturing.erp.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class ReportDefinitionService {
  private final ObjectMapper objectMapper;
  private final AtomicReference<Map<String, List<String>>> headerCache = new AtomicReference<>();

  public ReportDefinitionService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<String> getHeaders(ReportType reportType) {
    Map<String, List<String>> headers = loadHeaders();
    List<String> reportHeaders = headers.get(reportType.getTemplateFileName());
    if (reportHeaders == null) {
      throw new IllegalStateException("Missing headers for template: " + reportType.getTemplateFileName());
    }
    return reportHeaders;
  }

  private Map<String, List<String>> loadHeaders() {
    Map<String, List<String>> cached = headerCache.get();
    if (cached != null) {
      return cached;
    }
    Path path = Paths.get("..", "docs", "report-samples", "_headers.json");
    if (!Files.exists(path)) {
      path = Paths.get("docs", "report-samples", "_headers.json");
    }
    if (!Files.exists(path)) {
      throw new IllegalStateException("Report headers file not found: " + path.toAbsolutePath());
    }
    try {
      Map<String, List<String>> headers = objectMapper.readValue(
          Files.readString(path),
          new TypeReference<>() {});
      headerCache.compareAndSet(null, headers);
      return headers;
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load report headers", ex);
    }
  }
}
