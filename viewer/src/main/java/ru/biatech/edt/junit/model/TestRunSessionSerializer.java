/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.core.runtime.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import ru.biatech.edt.junit.model.ITestElement.FailureTrace;
import ru.biatech.edt.junit.model.ITestElement.ProgressState;
import ru.biatech.edt.junit.model.ITestElement.Result;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class TestRunSessionSerializer implements XMLReader {

  private static final String EMPTY = ""; //$NON-NLS-1$
  private static final String CDATA = "CDATA"; //$NON-NLS-1$
  private static final Attributes NO_ATTS = new AttributesImpl();
  private final TestRunSession fTestRunSession;
  private ContentHandler fHandler;
  private ErrorHandler fErrorHandler;

  private final NumberFormat timeFormat = new DecimalFormat("0.0##", new DecimalFormatSymbols(Locale.US)); //$NON-NLS-1$ // not localized, parseable by Double.parseDouble(..)

  /**
   * @param testRunSession the test run session to serialize
   */
  public TestRunSessionSerializer(TestRunSession testRunSession) {
    Assert.isNotNull(testRunSession);
    fTestRunSession = testRunSession;
  }

  @Override
  public void parse(InputSource input) throws IOException, SAXException {
    if (fHandler == null)
      throw new SAXException("ContentHandler missing"); //$NON-NLS-1$

    fHandler.startDocument();
    handleTestRun();
    fHandler.endDocument();
  }

  private void handleTestRun() throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    addCDATA(atts, IXMLTags.ATTR_NAME, fTestRunSession.getTestRunName());
    IV8Project project = fTestRunSession.getLaunchedProject();
    if (project != null)
      addCDATA(atts, IXMLTags.ATTR_PROJECT, project.getProject().getName());
    addCDATA(atts, IXMLTags.ATTR_TESTS, fTestRunSession.getTotalCount());
    addCDATA(atts, IXMLTags.ATTR_STARTED, fTestRunSession.getStartedCount());
    addCDATA(atts, IXMLTags.ATTR_FAILURES, fTestRunSession.getFailureCount());
    addCDATA(atts, IXMLTags.ATTR_ERRORS, fTestRunSession.getErrorCount());
    addCDATA(atts, IXMLTags.ATTR_IGNORED, fTestRunSession.getIgnoredCount());
    String includeTags = fTestRunSession.getIncludeTags();
    if (includeTags != null && !includeTags.isBlank()) {
      addCDATA(atts, IXMLTags.ATTR_INCLUDE_TAGS, includeTags);
    }
    String excludeTags = fTestRunSession.getExcludeTags();
    if (excludeTags != null && !excludeTags.isBlank()) {
      addCDATA(atts, IXMLTags.ATTR_EXCLUDE_TAGS, excludeTags);
    }
    startElement(IXMLTags.NODE_TESTRUN, atts);

    TestRoot testRoot = fTestRunSession.getTestRoot();
    ITestElement[] topSuites = testRoot.getChildren();
    for (ITestElement topSuite : topSuites) {
      handleTestElement(topSuite);
    }

    endElement(IXMLTags.NODE_TESTRUN);
  }

  private void handleTestElement(ITestElement testElement) throws SAXException {
    if (testElement instanceof TestSuiteElement) {
      TestSuiteElement testSuiteElement = (TestSuiteElement) testElement;

      AttributesImpl atts = new AttributesImpl();
      // Need to store the full #getTestName instead of only the #getSuiteTypeName for test factory methods
      addCDATA(atts, IXMLTags.ATTR_NAME, testSuiteElement.getTestName());
      if (!Double.isNaN(testSuiteElement.getElapsedTimeInSeconds()))
        addCDATA(atts, IXMLTags.ATTR_TIME, timeFormat.format(testSuiteElement.getElapsedTimeInSeconds()));
      if (testElement.getProgressState() != ProgressState.COMPLETED || testElement.getTestResult(false) != Result.UNDEFINED)
        addCDATA(atts, IXMLTags.ATTR_INCOMPLETE, Boolean.TRUE.toString());
      if (testSuiteElement.getDisplayName() != null) {
        addCDATA(atts, IXMLTags.ATTR_DISPLAY_NAME, testSuiteElement.getDisplayName());
      }
      String[] paramTypes = testSuiteElement.getParameterTypes();
      if (paramTypes != null) {
        String paramTypesStr = Arrays.stream(paramTypes).collect(Collectors.joining(",")); //$NON-NLS-1$
        addCDATA(atts, IXMLTags.ATTR_PARAMETER_TYPES, paramTypesStr);
      }
      if (testSuiteElement.getUniqueId() != null) {
        addCDATA(atts, IXMLTags.ATTR_UNIQUE_ID, testSuiteElement.getUniqueId());
      }
      startElement(IXMLTags.NODE_TESTSUITE, atts);
      addFailure(testSuiteElement);

      ITestElement[] children = testSuiteElement.getChildren();
      for (ITestElement child : children) {
        handleTestElement(child);
      }
      endElement(IXMLTags.NODE_TESTSUITE);

    } else if (testElement instanceof TestCaseElement) {
      TestCaseElement testCaseElement = (TestCaseElement) testElement;

      AttributesImpl atts = new AttributesImpl();
      addCDATA(atts, IXMLTags.ATTR_NAME, testCaseElement.getTestMethodName());
      addCDATA(atts, IXMLTags.ATTR_CLASSNAME, testCaseElement.getClassName());
      if (!Double.isNaN(testCaseElement.getElapsedTimeInSeconds()))
        addCDATA(atts, IXMLTags.ATTR_TIME, timeFormat.format(testCaseElement.getElapsedTimeInSeconds()));
      if (testElement.getProgressState() != ProgressState.COMPLETED)
        addCDATA(atts, IXMLTags.ATTR_INCOMPLETE, Boolean.TRUE.toString());
      if (testCaseElement.isIgnored())
        addCDATA(atts, IXMLTags.ATTR_IGNORED, Boolean.TRUE.toString());
      if (testCaseElement.isDynamicTest()) {
        addCDATA(atts, IXMLTags.ATTR_DYNAMIC_TEST, Boolean.TRUE.toString());
      }
      if (testCaseElement.getDisplayName() != null) {
        addCDATA(atts, IXMLTags.ATTR_DISPLAY_NAME, testCaseElement.getDisplayName());
      }
      String[] paramTypes = testCaseElement.getParameterTypes();
      if (paramTypes != null) {
        String paramTypesStr = Arrays.stream(paramTypes).collect(Collectors.joining(",")); //$NON-NLS-1$
        addCDATA(atts, IXMLTags.ATTR_PARAMETER_TYPES, paramTypesStr);
      }
      if (testCaseElement.getUniqueId() != null) {
        addCDATA(atts, IXMLTags.ATTR_UNIQUE_ID, testCaseElement.getUniqueId());
      }
      startElement(IXMLTags.NODE_TESTCASE, atts);
      addFailure(testCaseElement);

      endElement(IXMLTags.NODE_TESTCASE);

    } else {
      throw new IllegalStateException(String.valueOf(testElement));
    }

  }

  private void addFailure(TestElement testElement) throws SAXException {
    FailureTrace failureTrace = testElement.getFailureTrace();

    if (testElement.isAssumptionFailure()) {
      startElement(IXMLTags.NODE_SKIPPED, NO_ATTS);
      if (failureTrace != null) {
        addCharacters(failureTrace.getTrace());
      }
      endElement(IXMLTags.NODE_SKIPPED);

    } else if (failureTrace != null) {
      AttributesImpl failureAtts = new AttributesImpl();
      String failureKind = testElement.getTestResult(false) == Result.ERROR ? IXMLTags.NODE_ERROR : IXMLTags.NODE_FAILURE;
      startElement(failureKind, failureAtts);
      String expected = failureTrace.getExpected();
      String actual = failureTrace.getActual();
      if (expected != null) {
        startElement(IXMLTags.NODE_EXPECTED, NO_ATTS);
        addCharacters(expected);
        endElement(IXMLTags.NODE_EXPECTED);
      }
      if (actual != null) {
        startElement(IXMLTags.NODE_ACTUAL, NO_ATTS);
        addCharacters(actual);
        endElement(IXMLTags.NODE_ACTUAL);
      }
      String trace = failureTrace.getTrace();
      addCharacters(trace);
      endElement(failureKind);
    }
  }

  private void startElement(String name, Attributes atts) throws SAXException {
    fHandler.startElement(EMPTY, name, name, atts);
  }

  private void endElement(String name) throws SAXException {
    fHandler.endElement(EMPTY, name, name);
  }

  private static void addCDATA(AttributesImpl atts, String name, int value) {
    addCDATA(atts, name, Integer.toString(value));
  }

  private static void addCDATA(AttributesImpl atts, String name, String value) {
    atts.addAttribute(EMPTY, EMPTY, name, CDATA, value);
  }

  private void addCharacters(String string) throws SAXException {
    string = escapeNonUnicodeChars(string);
    fHandler.characters(string.toCharArray(), 0, string.length());
  }

  /**
   * Replaces all non-Unicode characters in the given string.
   *
   * @param string a string
   * @return string with Java-escapes
   * @since 3.6
   */
  private static String escapeNonUnicodeChars(String string) {
    StringBuilder buf = null;
    for (int i = 0; i < string.length(); i++) {
      char ch = string.charAt(i);
      if (ch != 9 && ch != 10 && ch != 13 && ch < 32) {
        if (buf == null) {
          buf = new StringBuilder(string.substring(0, i));
        }
        buf.append("\\u"); //$NON-NLS-1$
        String hex = Integer.toHexString(ch);
        for (int j = hex.length(); j < 4; j++)
          buf.append('0');
        buf.append(hex);
      } else if (buf != null) {
        buf.append(ch);
      }
    }
    if (buf != null) {
      return buf.toString();
    }
    return string;
  }

  @Override
  public void setContentHandler(ContentHandler handler) {
    this.fHandler = handler;
  }

  @Override
  public ContentHandler getContentHandler() {
    return fHandler;
  }

  @Override
  public void setErrorHandler(ErrorHandler handler) {
    fErrorHandler = handler;
  }

  @Override
  public ErrorHandler getErrorHandler() {
    return fErrorHandler;
  }

  // ignored:

  @Override
  public void parse(String systemId) throws IOException, SAXException {
  }

  @Override
  public void setDTDHandler(DTDHandler handler) {
  }

  @Override
  public DTDHandler getDTDHandler() {
    return null;
  }

  @Override
  public void setEntityResolver(EntityResolver resolver) {
  }

  @Override
  public EntityResolver getEntityResolver() {
    return null;
  }

  @Override
  public void setProperty(java.lang.String name, java.lang.Object value) {
  }

  @Override
  public Object getProperty(java.lang.String name) {
    return null;
  }

  @Override
  public void setFeature(java.lang.String name, boolean value) {
  }

  @Override
  public boolean getFeature(java.lang.String name) {
    return false;
  }
}
