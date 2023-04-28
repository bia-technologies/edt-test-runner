/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
 *     Neale Upstone <neale@nealeupstone.com> - [JUnit] JUnit viewer doesn't recognise <skipped/> node - https://bugs.eclipse.org/bugs/show_bug.cgi?id=276068
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model.serialize;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import com.google.common.base.Strings;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import ru.biatech.edt.junit.model.Factory;
import ru.biatech.edt.junit.model.IXMLTags;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.model.TestStatus;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.v8utils.Projects;

import java.util.Stack;

/**
 * Класс-обработчик чтения отчета jUnit
 * Содержит основную логику парсинга файла отчета
 */
public class TestRunHandler extends DefaultHandler {

  /*
   * TODO: validate (currently assumes correct XML)
   */

  private final Stack<Boolean> fNotRun = new Stack<>();
  private final TestRunErrorInfo errorInfo = new TestRunErrorInfo();
  String fDefaultProjectName;
  private TestRunSession fTestRunSession;
  private TestSuiteElement fTestSuite;
  private TestCaseElement fTestCase;
  private Locator fLocator;
  private TestStatus fStatus;
  private IProgressMonitor fMonitor;
  private int fLastReportedLine;

  public TestRunHandler() {
  }

  public TestRunHandler(IProgressMonitor monitor) {
    fMonitor = monitor;
  }

  public TestRunHandler(TestRunSession testRunSession) {
    fTestRunSession = testRunSession;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    fLocator = locator;
  }

  @Override
  public void startDocument() throws SAXException {
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (fLocator != null && fMonitor != null) {
      int line = fLocator.getLineNumber();
      if (line - 20 >= fLastReportedLine) {
        line -= line % 20;
        fLastReportedLine = line;
        fMonitor.subTask(NLS.bind(JUnitMessages.TestRunHandler_lines_read, line));
      }
    }
    if (Thread.interrupted())
      throw new OperationCanceledException();

    switch (qName) {
      case IXMLTags.NODE_TESTRUN:
        if (fTestRunSession == null) {
          fTestRunSession = readSession(attributes);
        } else {
          fTestRunSession.reset();
        }
        fTestSuite = fTestRunSession.getTestRoot();
        break;
      case IXMLTags.NODE_TESTSUITES:
        break;
      case IXMLTags.NODE_TESTSUITE: {
        fTestSuite = readTestSuite(attributes);
        break;
      }
      // not interested
      case IXMLTags.NODE_PROPERTIES:
      case IXMLTags.NODE_PROPERTY:
        break;
      case IXMLTags.NODE_TESTCASE: {
        fTestCase = readTestCase(attributes);
        break;
      }
      case IXMLTags.NODE_ERROR:
        //TODO: multiple failures: https://bugs.eclipse.org/bugs/show_bug.cgi?id=125296
        fStatus = TestStatus.ERROR;
        errorInfo.clean(true);
        errorInfo.message = attributes.getValue(IXMLTags.ATTR_MESSAGE);
        errorInfo.type = attributes.getValue(IXMLTags.ATTR_TYPE);
        break;
      case IXMLTags.NODE_FAILURE:
        //TODO: multiple failures: https://bugs.eclipse.org/bugs/show_bug.cgi?id=125296
        fStatus = TestStatus.FAILURE;
        errorInfo.clean(true);
        errorInfo.message = attributes.getValue(IXMLTags.ATTR_MESSAGE);
        break;
      case IXMLTags.NODE_EXPECTED:
        errorInfo.inExpected = true;
        break;
      case IXMLTags.NODE_ACTUAL:
        errorInfo.inActual = true;
        break;
      // not interested
      case IXMLTags.NODE_SYSTEM_OUT:
      case IXMLTags.NODE_SYSTEM_ERR:
        break;
      case IXMLTags.NODE_SKIPPED:
        // before Ant 1.9.0: not an Ant JUnit tag, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=276068
        // later: child of <suite> or <test>, see https://issues.apache.org/bugzilla/show_bug.cgi?id=43969
        fStatus = TestStatus.OK;
        errorInfo.clean(true);
        errorInfo.message = attributes.getValue(IXMLTags.ATTR_MESSAGE);
        if (errorInfo.message != null) {
          errorInfo.trace.append(errorInfo.message).append('\n');
        }
        break;
      default:
        throw new SAXParseException("unknown node '" + qName + "'", fLocator);  //$NON-NLS-1$//$NON-NLS-2$
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (errorInfo.inExpected) {
      errorInfo.expected.append(ch, start, length);
    } else if (errorInfo.inActual) {
      errorInfo.actual.append(ch, start, length);
    } else if (errorInfo.isError) {
      errorInfo.trace.append(ch, start, length);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    switch (qName) {
      // OK
      case IXMLTags.NODE_TESTRUN:
      case IXMLTags.NODE_TESTSUITES:
      case IXMLTags.NODE_PROPERTIES:
      case IXMLTags.NODE_PROPERTY:
      case IXMLTags.NODE_SYSTEM_OUT:
      case IXMLTags.NODE_SYSTEM_ERR:
        break;
      case IXMLTags.NODE_TESTSUITE:
        handleTestElementEnd(fTestSuite);
        fTestSuite = fTestSuite.getParent();
        //TODO: end suite: compare counters?
        break;
      case IXMLTags.NODE_TESTCASE:
        handleTestElementEnd(fTestCase);
        fTestCase = null;
        break;
      case IXMLTags.NODE_FAILURE:
      case IXMLTags.NODE_ERROR: {
        TestElement testElement = fTestCase;
        if (testElement == null)
          testElement = fTestSuite;
        handleFailure(testElement);
        break;
      }
      case IXMLTags.NODE_EXPECTED:
        errorInfo.inExpected = false;
        // skip whitespace from before <expected> and <actual> nodes
        errorInfo.trace.setLength(0);
        break;
      case IXMLTags.NODE_ACTUAL:
        errorInfo.inActual = false;
        // skip whitespace from before <expected> and <actual> nodes
        errorInfo.trace.setLength(0);
        break;
      case IXMLTags.NODE_SKIPPED: {
        TestElement testElement = fTestCase;
        if (testElement == null)
          testElement = fTestSuite;
        if (errorInfo.trace.length() > 0 || !Strings.isNullOrEmpty(errorInfo.message)) {
          handleFailure(testElement);
          testElement.setAssumptionFailure(true);
        } else if (fTestCase != null) {
          fTestCase.setIgnored(true);
        } else { // not expected
          testElement.setAssumptionFailure(true);
        }
        break;
      }
      default:
        handleUnknownNode(qName);
        break;
    }
  }

  TestCaseElement readTestCase(Attributes attributes) {
    // TODO Реализовать чтение message
    String name = attributes.getValue(IXMLTags.ATTR_NAME);
    String classname = attributes.getValue(IXMLTags.ATTR_CLASSNAME);
    String context = attributes.getValue(IXMLTags.ATTR_CONTEXT);
    String testName = name + '(' + classname + ')';
    boolean isDynamicTest = Boolean.parseBoolean(attributes.getValue(IXMLTags.ATTR_DYNAMIC_TEST));
    String displayName = attributes.getValue(IXMLTags.ATTR_DISPLAY_NAME);
    String paramTypesStr = attributes.getValue(IXMLTags.ATTR_PARAMETER_TYPES);
    String[] paramTypes;

    if (paramTypesStr != null && !paramTypesStr.isBlank()) {
      paramTypes = paramTypesStr.split(","); //$NON-NLS-1$
    } else {
      paramTypes = null;
    }

    String uniqueId = attributes.getValue(IXMLTags.ATTR_UNIQUE_ID);
    if (uniqueId != null && uniqueId.isBlank()) {
      uniqueId = null;
    }
    var testCase = Factory.createTestCase(fTestSuite, testName, displayName, isDynamicTest, paramTypes, uniqueId, context);
    fNotRun.push(Boolean.parseBoolean(attributes.getValue(IXMLTags.ATTR_INCOMPLETE)));
    testCase.setIgnored(Boolean.parseBoolean(attributes.getValue(IXMLTags.ATTR_IGNORED)));
    readTime(testCase, attributes);

    return testCase;
  }

  TestSuiteElement readTestSuite(Attributes attributes) {
    String name = attributes.getValue(IXMLTags.ATTR_NAME);
    TestSuiteElement parentSuite;

    if (fTestRunSession == null) {
      // support standalone suites and Ant's 'junitreport' task:
      IV8Project project = null;
      if (!Strings.isNullOrEmpty(fDefaultProjectName)) {
        project = Projects.getProject(fDefaultProjectName); // TODO Подумать как определять проект
      }
      fTestRunSession = new TestRunSession(name, project);
      parentSuite = fTestRunSession.getTestRoot();
    } else {
      parentSuite = fTestSuite;
    }

    TestSuiteElement suite;
    String pack = attributes.getValue(IXMLTags.ATTR_PACKAGE);
    String context = attributes.getValue(IXMLTags.ATTR_CONTEXT);
    String suiteName = pack == null ? name : pack + "." + name; //$NON-NLS-1$
    String displayName = attributes.getValue(IXMLTags.ATTR_DISPLAY_NAME);
    String paramTypesStr = attributes.getValue(IXMLTags.ATTR_PARAMETER_TYPES);
    String[] paramTypes;

    if (paramTypesStr != null && !paramTypesStr.isBlank()) {
      paramTypes = paramTypesStr.split(","); //$NON-NLS-1$
    } else {
      paramTypes = null;
    }

    String uniqueId = attributes.getValue(IXMLTags.ATTR_UNIQUE_ID);
    if (uniqueId != null && uniqueId.isBlank()) {
      uniqueId = null;
    }
    suite = Factory.createTestSuite(parentSuite, suiteName, displayName, paramTypes, uniqueId, context);
    readTime(suite, attributes);
    fNotRun.push(Boolean.valueOf(attributes.getValue(IXMLTags.ATTR_INCOMPLETE)));

    return suite;
  }

  TestRunSession readSession(Attributes attributes) {
    String name = attributes.getValue(IXMLTags.ATTR_NAME);
    String projectName = attributes.getValue(IXMLTags.ATTR_PROJECT);
    IV8Project project = null;

    if (Strings.isNullOrEmpty(projectName)) {
      projectName = fDefaultProjectName;
    }
    if (!Strings.isNullOrEmpty(projectName)) {
      project = Projects.getProject(projectName);
    }
    fTestRunSession = new TestRunSession(name, project);
    String includeTags = attributes.getValue(IXMLTags.ATTR_INCLUDE_TAGS);
    if (includeTags != null && includeTags.trim().length() > 0) {
      fTestRunSession.setIncludeTags(includeTags);
    }
    String excludeTags = attributes.getValue(IXMLTags.ATTR_EXCLUDE_TAGS);
    if (excludeTags != null && excludeTags.trim().length() > 0) {
      fTestRunSession.setExcludeTags(excludeTags);
    }
    //TODO: read counts?
    return fTestRunSession;
  }

  private void readTime(TestElement testElement, Attributes attributes) {
    String timeString = attributes.getValue(IXMLTags.ATTR_TIME);
    if (timeString != null) {
      try {
        testElement.setElapsedTimeInSeconds(Double.parseDouble(timeString));
      } catch (NumberFormatException e) {
      }
    }
  }

  private void handleTestElementEnd(TestElement testElement) {
    boolean completed = fNotRun.pop() != Boolean.TRUE;
    fTestRunSession.registerTestEnded(testElement, completed);
  }

  private void handleFailure(TestElement testElement) {
    testElement.pushErrorInfo(fStatus, errorInfo.message, errorInfo.type, errorInfo.getTrace(), errorInfo.getExpected(), errorInfo.getActual());
    fTestRunSession.registerTestFailureStatus(testElement);
    errorInfo.clean(false);
    fStatus = null;
  }

  private void handleUnknownNode(String qName) throws SAXException {
    //TODO: just log if debug option is enabled?
    StringBuilder msg = new StringBuilder("unknown node '").append(qName).append("'"); //$NON-NLS-1$//$NON-NLS-2$
    if (fLocator != null) {
      msg.append(" at line ").append(fLocator.getLineNumber()).append(", column ").append(fLocator.getColumnNumber());  //$NON-NLS-1$//$NON-NLS-2$
    }
    throw new SAXException(msg.toString());
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void warning(SAXParseException e) throws SAXException {
    throw e;
  }

  /**
   * @return the parsed test run session, or <code>null</code>
   */
  public TestRunSession getTestRunSession() {
    return fTestRunSession;
  }

  static class TestRunErrorInfo {
    String message;
    StringBuffer trace = new StringBuffer();
    String type;
    StringBuffer actual = new StringBuffer();
    StringBuffer expected = new StringBuffer();

    boolean inExpected = false;
    boolean inActual = false;
    boolean isError = false;

    void clean(boolean isError) {
      this.isError = isError;
      inActual = false;
      inExpected = false;
      message = null;
      trace.setLength(0);
      type = null;
      actual.setLength(0);
      expected.setLength(0);
    }

    String getTrace() {
      return getBufferString(trace);
    }

    String getActual() {
      return getBufferString(actual);
    }

    String getExpected() {
      return getBufferString(expected);
    }

    private String getBufferString(StringBuffer buffer) {
      if (buffer.length() > 0) {
        return buffer.toString();
      } else {
        return null;
      }
    }
  }
}
