package ru.biatech.edt.junit.model.report;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TestCase extends BaseTestItem {

  /**
   * Full class name for the class the test method is in.
   */
  @JacksonXmlProperty(localName = "classname")
  String className;
  /**
   * Name of the test method
   */
  String name;
  /**
   * Time taken (in seconds) to execute the test
   */
  double time;
  /**
   * Indicates that the test was skipped
   */
  ErrorInfo[] skipped;
  /**
   * Indicates that the test errored.  An errored test is one that had an unanticipated problem. e.g., an unchecked throwable; or a problem with the implementation of the test. Contains as a text node relevant data for the error, e.g., a stack trace
   */
  ErrorInfo[] error;
  /**
   * Indicates that the test failed. A failure is a test which the code has explicitly failed by using the mechanisms for that purpose. e.g., via an assertEquals. Contains as a text node relevant data for the failure, e.g., a stack trace
   */
  Failure[] failure;
  /**
   * Data that was written to standard out while the test was executed
   */
  @JacksonXmlProperty(localName = "system-out")
  String systemOut;
  /**
   * Data that was written to standard error while the test was executed
   */
  @JacksonXmlProperty(localName = "system-err")
  String systemErr;
  String context;
}
