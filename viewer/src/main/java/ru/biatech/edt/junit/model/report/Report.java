package ru.biatech.edt.junit.model.report;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@JacksonXmlRootElement(localName = "testsuites")
@Data
public class Report<T extends TestSuite<?>> {
  /**
   * Properties (e.g., environment settings) set during test execution
   */
  @JacksonXmlElementWrapper(useWrapping = true, localName = "properties")
  Property[] property;
  T[] testsuite;
}
