package ru.biatech.edt.junit.model.report;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.Serializer;

import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class ReportLoader {

  @SneakyThrows
  public <T> T load(Path path, Class<T> reportClass) {
    try (var stream = Files.newInputStream(path)) {
      return Serializer.getXmlMapper().readValue(stream, reportClass);
    }
  }

  @SneakyThrows
  public <T> void loadInto(Path path, T object) {
    try (var stream = Files.newInputStream(path)) {
      Serializer.getXmlMapper().readerForUpdating(object).readValue(stream);
    }
  }

}