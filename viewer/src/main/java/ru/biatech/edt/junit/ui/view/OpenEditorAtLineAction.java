/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *     Sebastian Davids: sdavids@gmx.de bug 37333 Failure Trace cannot
 * 			navigate to non-public class in CU throwing Exception
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.view;

import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.kinds.ITestResolver;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;

/**
 * Open a test in the editor and reveal a given line
 */
public class OpenEditorAtLineAction extends OpenEditorAction {

  // TODO Сейчас не используется, пригодится при реанимации отображения стека
  private final int fLineNumber;

  public OpenEditorAtLineAction(TestRunnerViewPart testRunner, String className, int line) {
    super(testRunner, className);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJUnitHelpContextIds.OPENEDITORATLINE_ACTION);
    fLineNumber = line;
  }

  @Override
  protected ITestResolver.MethodPositionInfo getPosition() {
    return getResolver(getLaunchedProject()).getMethodPositionInfo(fClassName, fLineNumber);
  }
}
