package ru.biatech.edt.junit.model.report;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.biatech.edt.junit.Serializer;

import java.io.File;

@UtilityClass
public class ReportLoader {

  @SneakyThrows
  public <T> T load(File path, Class<T> reportClass) {
    return Serializer.getXmlMapper().readValue(path, reportClass);
  }

  @SneakyThrows
  public <T> void loadInto(File path, T object) {
    Serializer.getXmlMapper().readerForUpdating(object).readValue(path);
  }

}