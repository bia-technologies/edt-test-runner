/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package ru.biatech.edt.junit.ui.report.actions;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.launcher.v8.RerunHelper;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

/**
 * Requests to rerun a test.
 */
public class RerunAction extends Action {
  private final String fClassName;
  private final TestRunnerViewPart fTestRunner;
  private final String fLaunchMode;

  /**
   * Constructor for RerunAction.
   *
   * @param actionName      the name of the action
   * @param runner          the JUnit view
   * @param className       the class name containing the test
   * @param launchMode      the launch mode
   */
  public RerunAction(String actionName, TestRunnerViewPart runner, String className, String launchMode) {
    super(actionName);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJUnitHelpContextIds.RERUN_ACTION);
    fTestRunner = runner;
    fClassName = className;
    fLaunchMode = launchMode;
    if (ILaunchManager.RUN_MODE.equals(launchMode)) {
      setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ACTION_RUN_TEST)); //$NON-NLS-1$
    } else if (ILaunchManager.DEBUG_MODE.equals(launchMode)) {
      setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ACTION_DEBUG_TEST)); //$NON-NLS-1$
    }
  }

  @Override
  public void run() {
    RerunHelper.rerunTest(fTestRunner.getTestRunSession(), fClassName, fLaunchMode);
  }
}
