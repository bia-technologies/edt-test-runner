package ru.biatech.edt.junit.model.report;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

@Data
public class BaseTestItem {
  /**
   * Properties (e.g., environment settings) set during test execution
   */
  @JacksonXmlElementWrapper(useWrapping = true, localName = "properties")
  Property[] property;
  ErrorInfo[] error;
}
