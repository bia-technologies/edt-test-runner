/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.model;

import org.eclipse.core.runtime.ListenerList;
import ru.biatech.edt.junit.TestRunListener;
import ru.biatech.edt.junit.TestViewerPlugin;


/**
 * Notifier for the callback listener API {@link TestRunListener}.
 */
public class TestRunListenerAdapter implements ITestSessionListener {

  private final Session session;

  public TestRunListenerAdapter(Session session) {
    this.session = session;
  }

  private ListenerList<TestRunListener> getListenerList() {
    return TestViewerPlugin.core().getNewTestRunListeners();
  }

  private void fireSessionStarted() {
    for (TestRunListener listener : getListenerList()) {
      listener.sessionStarted(session);
    }
  }

  private void fireSessionFinished() {
    for (TestRunListener listener : getListenerList()) {
      listener.sessionFinished(session);
    }
  }

  private void fireTestCaseStarted(ITestCaseElement testCaseElement) {
    for (TestRunListener listener : getListenerList()) {
      listener.testCaseStarted(testCaseElement);
    }
  }

  private void fireTestCaseFinished(ITestCaseElement testCaseElement) {
    for (TestRunListener listener : getListenerList()) {
      listener.testCaseFinished(testCaseElement);
    }
  }


  @Override
  public void sessionStarted() {
    // wait until all test are added
  }

  @Override
  public void sessionEnded(long elapsedTime) {
    fireSessionFinished();
    session.swapOut();
  }

  @Override
  public void sessionStopped(long elapsedTime) {
    fireSessionFinished();
    session.swapOut();
  }

  @Override
  public void sessionTerminated() {
    session.swapOut();
  }

  @Override
  public void testAdded(ITestElement testElement) {
    // do nothing
  }

  @Override
  public void runningBegins() {
    fireSessionStarted();
  }

  @Override
  public void testStarted(ITestCaseElement testCaseElement) {
    fireTestCaseStarted(testCaseElement);
  }

  @Override
  public void testEnded(ITestCaseElement testCaseElement) {
    fireTestCaseFinished(testCaseElement);
  }

  @Override
  public void testFailed(ITestElement testElement, TestStatus status, String trace, String expected, String actual) {
    // ignore
  }

  @Override
  public void testRerun(ITestCaseElement testCaseElement, TestStatus status, String trace, String expectedResult, String actualResult) {
    // ignore
  }

  @Override
  public boolean acceptsSwapToDisk() {
    return true;
  }
}
