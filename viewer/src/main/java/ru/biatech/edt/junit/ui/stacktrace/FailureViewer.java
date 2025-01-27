/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.stacktrace;

import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.Failure;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.stacktrace.actions.CompareResultsAction;
import ru.biatech.edt.junit.ui.stacktrace.actions.CopyTraceAction;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.v8utils.BslSourceDisplay;

/**
 * Класс для отображения и взаимодействия с ошибками тестирования
 */
public class FailureViewer {

  private StackTraceHtmlView htmlViewer;
  private StackTraceTreeView treeViewer;
  private StackTraceView activeViewer;
  private final TestRunnerViewPart viewPart;
  private final ViewForm parent;

  // Actions
  MenuManager menuManager;
  private CopyTraceAction copyTraceAction;
  private CompareResultsAction compareAction;
  private OpenAction openAction;

  ITestElement currentTestElement;

  public FailureViewer(TestRunnerViewPart viewPart, ViewForm parent) {
    this.viewPart = viewPart;
    this.parent = parent;

    createControls();
  }

  public void viewFailure(ITestElement testElement) {
    activeViewer.viewFailure(currentTestElement = testElement);
    handleSelected();
  }

  public void clear() {
    activeViewer.clear();
  }

  public void dispose() {
    if (htmlViewer != null) {
      htmlViewer.dispose();
      htmlViewer = null;
    }
    if (treeViewer != null) {
      treeViewer.dispose();
      treeViewer = null;
    }
  }

  private void createControls() {
    GridLayoutFactory.fillDefaults().applyTo(parent);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

    createLabel();
    registerActions();
    setStacktraceViewer(viewPart.getToolBar().isHtmlStackTrace());
  }

  private void registerActions() {
    compareAction = new CompareResultsAction();
    copyTraceAction = new CopyTraceAction();
    openAction = new OpenAction();

    // ToolBar
    ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.WRAP);
    parent.setTopCenter(toolBar);
    ToolBarManager failureToolBarManager = new ToolBarManager(toolBar);
    compareAction.setEnabled(false);
    failureToolBarManager.add(compareAction);
    failureToolBarManager.update(true);

    // Menu
    menuManager = new MenuManager();
    menuManager.add(openAction);
    menuManager.add(compareAction);
    menuManager.add(copyTraceAction);

    parent.addDisposeListener((e) -> {
      menuManager.dispose();
      failureToolBarManager.dispose();
    });
  }

  private void createLabel() {
    CLabel label = new CLabel(parent, SWT.NONE);
    label.setText(UIMessages.TestRunnerViewPart_label_failure);
    label.setImage(viewPart.getImageProvider().getStackViewIcon());
    parent.setTopLeft(label);
  }

  private void openSelected() {
    var element = activeViewer.getSelected();
    if (element == null) {
      return;
    }
    if (element instanceof Failure && ((Failure) element).isComparisonFailure()) {
      compareAction.run();
    } else if (element instanceof IStacktraceFrame) {
      openAction.run();
    }
  }

  void handleSelected() {
    var error = activeViewer.getSelectedError();

    compareAction.handleTestSelected(error);
    copyTraceAction.handleTestSelected(error);
    openAction.setEnabled(error != null);
  }

  public synchronized void setStacktraceViewer(boolean showHtmlView) {
    StackTraceView viewer;
    if (showHtmlView) {
      if (htmlViewer == null) {
        configureViewer(htmlViewer = new StackTraceHtmlView(parent));
      }
      viewer = htmlViewer;
    } else {
      if (treeViewer == null) {
        configureViewer(treeViewer = new StackTraceTreeView(parent));
      }
      viewer = treeViewer;
    }
    if (viewer == activeViewer) {
      return;
    }
    activeViewer = viewer;
    activeViewer.viewFailure(currentTestElement);
    parent.setContent(activeViewer.getContent());
  }

  private void configureViewer(StackTraceView viewer) {
    viewer.addSelectionChangedListeners(this::handleSelected);
    viewer.addOpenListeners(this::openSelected);
    viewer.registerMenu(menuManager);
  }


  class OpenAction extends Action {

    private OpenAction() {
      super("Goto line", ImageProvider.getImageDescriptor(ImageProvider.ACTION_GOTO_ERROR));
      setEnabled(false);
    }

    @Override
    public void run() {
      Object element = activeViewer.getSelected();
      if ((element instanceof IStacktraceFrame)) {
        IWorkbenchPage page = viewPart.getSite().getPage();
        BslSourceDisplay.INSTANCE.displayBslSource((IStacktraceFrame)element, page, false);
      }
    }
  }
}
