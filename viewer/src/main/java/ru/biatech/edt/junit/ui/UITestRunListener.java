/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
package ru.biatech.edt.junit.ui;

import ru.biatech.edt.junit.TestRunListener;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestRunSession;

/**
 * This test run listener is the entry point that makes sure the org.eclipse.jdt.junit
 * plug-in gets loaded when a JUnit launch configuration is launched.
 *
 * @since 3.6
 */
public class UITestRunListener extends TestRunListener {
  // TODO Использовать на замену ShowJUnitResult
  /*
   * @see ru.biatech.edt.junit.TestRunListener#sessionLaunched(org.eclipse.jdt.junit.model.ITestRunSession)
   * @since 3.6
   */
  @Override
  public void sessionLaunched(ITestRunSession session) {
    TestViewerPlugin.ui().asyncShowTestRunnerViewPart();
    TestViewerPlugin.log().debug("UITestRunListener:sessionLaunched");
  }

  @Override
  public void sessionFinished(ITestRunSession session) {
    TestViewerPlugin.log().debug("UITestRunListener:sessionFinished");
  }

  @Override
  public void testCaseStarted(ITestCaseElement testCaseElement) {
    TestViewerPlugin.log().debug("UITestRunListener:testCaseStarted");
  }

  @Override
  public void sessionStarted(ITestRunSession session) {
    TestViewerPlugin.log().debug("UITestRunListener:sessionStarted");
  }

  @Override
  public void testCaseFinished(ITestCaseElement testCaseElement) {
    TestViewerPlugin.log().debug("UITestRunListener:testCaseFinished");
  }
}
