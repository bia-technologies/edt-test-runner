/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package ru.biatech.edt.junit.ui.report.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.SessionsManager;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;

import java.io.File;

/**
 * Команда загрузки отчета о тестировании из файла
 */
 public class ImportTestRunSessionAction extends Action {
  public ImportTestRunSessionAction() {
    super(UIMessages.TestRunnerViewPart_ImportTestRunSessionAction_name);
   }

  @Override
  public void run() {
    var shell = TestViewerPlugin.ui().getShell();

    var importDialog = new FileDialog(shell, SWT.OPEN | SWT.SHEET);
    importDialog.setText(UIMessages.TestRunnerViewPart_ImportTestRunSessionAction_title);
    var dialogSettings = TestViewerPlugin.getDefault().getDialogSettings();
    var lastPath = dialogSettings.get(TestRunnerViewPart.PREF_LAST_PATH);
    if (lastPath != null) {
      importDialog.setFilterPath(lastPath);
    }
    importDialog.setFilterExtensions(new String[]{"*.xml", "*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
    var path = importDialog.open();
    if (path == null) {
      return;
    }

    //TODO: MULTI: getFileNames()
    File file = new File(path);

    try {
      SessionsManager.importSession(file, null);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(e);
      ErrorDialog.openError(shell, UIMessages.TestRunnerViewPart_ImportTestRunSessionAction_error_title, e.getStatus().getMessage(), e.getStatus());
    }
  }
}