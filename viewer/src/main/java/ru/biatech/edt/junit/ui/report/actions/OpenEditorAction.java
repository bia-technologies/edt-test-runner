/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import com._1c.g5.v8.dt.core.platform.IV8Project;
import org.eclipse.jface.action.Action;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.ImageProvider;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.v8utils.BslSourceDisplay;
import ru.biatech.edt.junit.v8utils.MethodReference;

/**
 * Абстрактное действие для перехода к методу
 */
public abstract class OpenEditorAction extends Action {

  protected String fClassName;
  protected TestRunnerViewPart fTestRunner;

  protected OpenEditorAction(String text, String icon, TestRunnerViewPart testRunner, String testClassName) {
    super(text, ImageProvider.getImageDescriptor(icon));
    fClassName = testClassName;
    fTestRunner = testRunner;
  }

  @Override
  public void run() {
    var position = getPosition();
    if (position != null) {
      BslSourceDisplay.INSTANCE.displayBslSource(position, TestViewerPlugin.ui().getActivePage(), true);
    }
  }

  /**
   * @return the 1C project, or <code>null</code>
   */
  protected IV8Project getLaunchedProject() {
    return fTestRunner.getLaunchedProject();
  }

  protected MethodReference getPosition() {
    return null;
  }
}
