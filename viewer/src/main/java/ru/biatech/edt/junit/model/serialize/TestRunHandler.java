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
import ru.biatech.edt.junit.model.Session;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestElement;
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
  private Session session;
  private TestSuiteElement testSuite;
  private TestCaseElement testCase;
  private Locator locator;
  private TestStatus testStatus;
  private IProgressMonitor progressMonitor;
  private int lastReportedLine;

  public TestRunHandler() {
  }

  public TestRunHandler(IProgressMonitor monitor) {
    progressMonitor = monitor;
  }

  public TestRunHandler(Session session) {
    this.session = session;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  @Override
  public void startDocument() throws SAXException {
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (locator != null && progressMonitor != null) {
      int line = locator.getLineNumber();
      if (line - 20 >= lastReportedLine) {
        line -= line % 20;
        lastReportedLine = line;
        progressMonitor.subTask(NLS.bind(JUnitMessages.TestRunHandler_lines_read, line));
      }
    }
    if (Thread.interrupted())
      throw new OperationCanceledException();

    switch (qName) {
      case IXMLTags.NODE_TESTRUN:
        if (session == null) {
          session = readSession(attributes);
        } else {
          session.reset();
        }
        testSuite = session.getTestRoot();
        break;
      case IXMLTags.NODE_TESTSUITES:
        break;
      case IXMLTags.NODE_TESTSUITE: {
        testSuite = readTestSuite(attributes);
        break;
      }
      // not interested
      case IXMLTags.NODE_PROPERTIES:
      case IXMLTags.NODE_PROPERTY:
        break;
      case IXMLTags.NODE_TESTCASE: {
        testCase = readTestCase(attributes);
        break;
      }
      case IXMLTags.NODE_ERROR:
        //TODO: multiple failures: https://bugs.eclipse.org/bugs/show_bug.cgi?id=125296
        testStatus = TestStatus.ERROR;
        errorInfo.clean(true);
        errorInfo.message = attributes.getValue(IXMLTags.ATTR_MESSAGE);
        errorInfo.type = attributes.getValue(IXMLTags.ATTR_TYPE);
        break;
      case IXMLTags.NODE_FAILURE:
        //TODO: multiple failures: https://bugs.eclipse.org/bugs/show_bug.cgi?id=125296
        testStatus = TestStatus.FAILURE;
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
        testStatus = TestStatus.OK;
        errorInfo.clean(true);
        errorInfo.message = attributes.getValue(IXMLTags.ATTR_MESSAGE);
        if (errorInfo.message != null) {
          errorInfo.trace.append(errorInfo.message).append('\n');
        }
        break;
      default:
        throw new SAXParseException("unknown node '" + qName + "'", locator);  //$NON-NLS-1$//$NON-NLS-2$
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
        handleTestElementEnd(testSuite);
        testSuite = testSuite.getParent();
        //TODO: end suite: compare counters?
        break;
      case IXMLTags.NODE_TESTCASE:
        handleTestElementEnd(testCase);
        testCase = null;
        break;
      case IXMLTags.NODE_FAILURE:
      case IXMLTags.NODE_ERROR: {
        TestElement testElement = testCase;
        if (testElement == null)
          testElement = testSuite;
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
        TestElement testElement = testCase;
        if (testElement == null)
          testElement = testSuite;
        if (errorInfo.trace.length() > 0 || !Strings.isNullOrEmpty(errorInfo.message)) {
          handleFailure(testElement);
          testElement.setAssumptionFailure(true);
        } else if (testCase != null) {
          testCase.setIgnored(true);
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
    var testCase = Factory.createTestCase(testSuite, testName, displayName, isDynamicTest, paramTypes, uniqueId, context);
    fNotRun.push(Boolean.parseBoolean(attributes.getValue(IXMLTags.ATTR_INCOMPLETE)));
    testCase.setIgnored(Boolean.parseBoolean(attributes.getValue(IXMLTags.ATTR_IGNORED)));
    readTime(testCase, attributes);

    return testCase;
  }

  TestSuiteElement readTestSuite(Attributes attributes) {
    String name = attributes.getValue(IXMLTags.ATTR_NAME);
    TestSuiteElement parentSuite;

    if (session == null) {
      // support standalone suites and Ant's 'junitreport' task:
      IV8Project project = null;
      if (!Strings.isNullOrEmpty(fDefaultProjectName)) {
        project = Projects.getProject(fDefaultProjectName); // TODO Подумать как определять проект
      }
      session = new Session(name, project);
      parentSuite = session.getTestRoot();
    } else {
      parentSuite = testSuite;
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

  Session readSession(Attributes attributes) {
    String name = attributes.getValue(IXMLTags.ATTR_NAME);
    String projectName = attributes.getValue(IXMLTags.ATTR_PROJECT);
    IV8Project project = null;

    if (Strings.isNullOrEmpty(projectName)) {
      projectName = fDefaultProjectName;
    }
    if (!Strings.isNullOrEmpty(projectName)) {
      project = Projects.getProject(projectName);
    }
    session = new Session(name, project);
    String includeTags = attributes.getValue(IXMLTags.ATTR_INCLUDE_TAGS);
    if (includeTags != null && includeTags.trim().length() > 0) {
      session.setIncludeTags(includeTags);
    }
    String excludeTags = attributes.getValue(IXMLTags.ATTR_EXCLUDE_TAGS);
    if (excludeTags != null && excludeTags.trim().length() > 0) {
      session.setExcludeTags(excludeTags);
    }
    //TODO: read counts?
    return session;
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
    session.registerTestEnded(testElement, completed);
  }

  private void handleFailure(TestElement testElement) {
    testElement.pushErrorInfo(testStatus, errorInfo.message, errorInfo.type, errorInfo.getTrace(), errorInfo.getExpected(), errorInfo.getActual());
    session.registerTestFailureStatus(testElement);
    errorInfo.clean(false);
    testStatus = null;
  }

  private void handleUnknownNode(String qName) throws SAXException {
    //TODO: just log if debug option is enabled?
    StringBuilder msg = new StringBuilder("unknown node '").append(qName).append("'"); //$NON-NLS-1$//$NON-NLS-2$
    if (locator != null) {
      msg.append(" at line ").append(locator.getLineNumber()).append(", column ").append(locator.getColumnNumber());  //$NON-NLS-1$//$NON-NLS-2$
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
  public Session getTestRunSession() {
    return session;
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
