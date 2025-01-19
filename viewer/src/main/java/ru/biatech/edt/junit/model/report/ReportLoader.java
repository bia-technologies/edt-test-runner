package ru.biatech.edt.junit.model.report;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class ReportLoader {
  ObjectMapper mapper = init();

  @SneakyThrows
  public <T> T load(File path, Class<T> reportClass) {
    return mapper.readValue(path, reportClass);
  }

  @SneakyThrows
  public <T> T loadInto(File path, T object) {
    return mapper.readerForUpdating(object).readValue(path);
  }

  @SneakyThrows
  public static String dump(Report report) {
    return mapper.writeValueAsString(report);
  }

  ObjectMapper init() {
    return new XmlMapper()
        .setDefaultUseWrapper(false)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
  }
}