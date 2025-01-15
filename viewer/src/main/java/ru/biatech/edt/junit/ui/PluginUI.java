/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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
 *     Julien Ruaux: jruaux@octo.com
 * 	   Vincent Massol: vmassol@octo.com
 *     David Saff (saff@mit.edu) - bug 102632: [JUnit] Support for JUnit 4.
 *     Achim Demelt <a.demelt@exxcellent.de> - [junit] Separate UI from non-UI code - https://bugs.eclipse.org/bugs/show_bug.cgi?id=278844
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

/**
 * The plug-in runtime class for the JUnit plug-in.
 */
public class PluginUI {

  public static final String PLUGIN_ID = "ru.biatech.edt.junit"; //$NON-NLS-1$

  public Shell getActiveWorkbenchShell() {
    IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
    if (workBenchWindow == null) {
      IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
      return windows.length > 0 ? windows[0].getShell() : null;
    }
    return workBenchWindow.getShell();
  }

  /**
   * Returns the active workbench window
   *
   * @return the active workbench window
   */
  public IWorkbenchWindow getActiveWorkbenchWindow() {
    IWorkbench workBench = PlatformUI.getWorkbench();
    if (workBench == null)
      return null;
    return workBench.getActiveWorkbenchWindow();
  }

  public IWorkbenchPage getActivePage() {
    IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow();
    if (activeWorkbenchWindow == null)
      return null;
    return activeWorkbenchWindow.getActivePage();
  }

  public IDialogSettings getDialogSettingsSection(String name) {
    IDialogSettings dialogSettings = TestViewerPlugin.getDefault().getDialogSettings();
    IDialogSettings section = dialogSettings.getSection(name);
    if (section == null) {
      section = dialogSettings.addNewSection(name);
    }
    return section;
  }

  public void asyncShowTestRunnerViewPart() {
    getDisplay().asyncExec(this::showTestRunnerViewPartInActivePage);
  }

  public void showTestRunnerViewPartInActivePage() {
    try {
      // Have to force the creation of view part contents
      // otherwise the UI will not be updated
      IWorkbenchPage page = getActivePage();
      if (page == null)
        return;
      TestRunnerViewPart view = (TestRunnerViewPart) page.findView(TestRunnerViewPart.NAME);
      if (view == null) {
        //	create and show the result view if it isn't created yet.
        page.showView(TestRunnerViewPart.NAME, null, IWorkbenchPage.VIEW_VISIBLE);
      } else {
      }
    } catch (PartInitException pie) {
      TestViewerPlugin.log().logError(pie);
    }
  }

  public Display getDisplay() {
    Shell shell = getActiveWorkbenchShell();
    if (shell != null) {
      return shell.getDisplay();
    }
    Display display = Display.getCurrent();
    if (display == null) {
      display = Display.getDefault();
    }
    return display;
  }

  public Shell getShell() {
    return getActiveWorkbenchShell();
  }

}
