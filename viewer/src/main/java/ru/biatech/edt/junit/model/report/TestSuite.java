package ru.biatech.edt.junit.model.report;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Contains the results of executing a testsuite
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TestSuite<T extends TestCase> extends BaseTestItem {
  protected int id;
  @JacksonXmlProperty(localName = "package")
  protected String packageName;
  /**
   * Full class name for the class the test method is in.
   */
  @JacksonXmlProperty(localName = "classname")
  protected String className;
  /**
   * Full class name of the test for non-aggregated testsuite documents. Class name without the package for aggregated testsuites documents
   */
  protected String name;
  /**
   * when the test was executed. Timezone may not be specified.
   */
  protected Date timestamp;
  /**
   * Host on which the tests were executed. 'localhost' should be used if the hostname cannot be determined.
   */
  protected String hostname;
  /**
   * Time taken (in seconds) to execute the tests in the suite
   */
  protected double time;
  protected String context;
  protected T[] testcase;

  // counters
  protected int tests;
  protected int errors;
  protected int skipped;
  protected int failures;
}
