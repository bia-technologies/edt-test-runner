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

import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceError;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestErrorInfo;
import ru.biatech.edt.junit.model.TestStatus;
import ru.biatech.edt.junit.ui.stacktrace.events.Listener;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

import java.util.List;

/**
 * Элемент для отображения стека ошибок
 */
public class StackTraceTreeView extends TreeViewer implements StackTraceView {
  private final ImageProvider imageProvider = new ImageProvider();

  private final StackTraceMixin mixin = new StackTraceMixin();

  public StackTraceTreeView(Composite parent) {
    super(parent, SWT.V_SCROLL | SWT.SINGLE);
    mixin.addTestElementChangedListener(this::renderTest);
    setUseHashlookup(true);
    createControl();
  }

  /**
   * {@link StackTraceView#viewFailure(TestElement)}
   */
  @Override
  public void viewFailure(TestElement testElement) {
    mixin.setTestElement(testElement);
  }

  /**
   * {@link StackTraceView#clear()}
   */
  @Override
  public void clear() {
    mixin.clear();
  }

  /**
   * {@link StackTraceView#getSelected()}
   */
  @Override
  public Object getSelected() {
    return mixin.getSelectedItemData();
  }

  /**
   * {@link StackTraceView#getSelectedError()}
   */
  @Override
  public TestErrorInfo getSelectedError() {
    return mixin.getSelectedError();
  }

  /**
   * {@link StackTraceView#registerMenu(MenuManager)}
   */
  @Override
  public void registerMenu(MenuManager menuManager) {
    Menu menu = menuManager.createContextMenu(getTree());
    getTree().setMenu(menu);
  }

  /**
   * {@link StackTraceView#dispose()}
   */
  @Override
  public void dispose() {
    getTree().dispose();
    imageProvider.dispose();
  }

  /**
   * {@link StackTraceView#addSelectionChangedListeners(Listener)}
   */
  @Override
  public void addSelectionChangedListeners(Listener listener) {
    mixin.addSelectionChangedListeners(listener);
  }

  /**
   * {@link StackTraceView#addOpenListeners(Listener)}
   */
  @Override
  public void addOpenListeners(Listener listener) {
    mixin.addOpenListeners(listener);
  }

  /**
   * {@link StackTraceView#getContent()}
   */
  @Override
  public Control getContent() {
    return getTree();
  }

  private void createControl() {
    GridDataFactory.fillDefaults().grab(true, true).applyTo(this.getControl());
    this.setContentProvider(new TreeItemContentProvider());
    this.setLabelProvider(new TreeItemColumnLabelProvider());
    ColumnViewerToolTipSupport.enableFor(this);

    IOpenEventListener openListener = e -> mixin.riseOnOpen();
    OpenStrategy handler = new OpenStrategy(getTree());
    handler.addOpenListener(openListener);

    getTree().addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        selectionChanged(e);
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        selectionChanged(e);
      }
    });
  }

  void selectionChanged(SelectionEvent e) {
    var data = e.data;
    if (data == null && e.item != null) {
      data = e.item.getData();
    }
    if (data instanceof IStructuredSelection) {
      var structuredSelection = (IStructuredSelection) data;
      if (structuredSelection.size() == 1) {
        var context = structuredSelection.getFirstElement();
        if (context instanceof StackTraceTreeBuilder.TreeItem) {
          mixin.setSelectedItem((StackTraceTreeBuilder.TreeItem) context);
          return;
        }
      }
    } else if (data instanceof StackTraceTreeBuilder.TreeItem) {
      mixin.setSelectedItem((StackTraceTreeBuilder.TreeItem) data);
      return;

    }
    mixin.setSelectedItem(null);
  }

  private void renderTest() {
    var tree = mixin.getTree();
    this.setInput(tree.getChildren());
    if (!tree.getChildren().isEmpty()) {
      this.setSelection(new StructuredSelection(tree.getChildren().get(0)));
    }
    this.expandAll();
  }

  private class TreeItemColumnLabelProvider extends ColumnLabelProvider {
    @Override
    public Image getImage(Object element) {
      var item = (StackTraceTreeBuilder.TreeItem) element;
      var data = item.getData();
      if (data instanceof TestErrorInfo) {
        var status = ((TestErrorInfo) item.getData()).getStatus();
        return getIcon(status);
      } else if (data instanceof IStacktraceElement) {
        return getIcon((IStacktraceElement) data);
      } else {
        return super.getImage(element);
      }
    }

    @Override
    public String getText(Object element) {
      return ((StackTraceTreeBuilder.TreeItem) element).getText();
    }

    @Override
    public String getToolTipText(Object element) {
      var item = (StackTraceTreeBuilder.TreeItem) element;
      if (item.getData() instanceof IStacktraceElement) {
        return ((IStacktraceElement) item.getData()).getName();
      } else {
        return item.getText();
      }
    }

    Image getIcon(TestStatus status) {
      switch (status) {
        case ERROR:
          return imageProvider.getTestErrorIcon();
        case FAILURE:
          return imageProvider.getTestFailIcon();
        default:
          return imageProvider.getMessageIcon();
      }
    }

    Image getIcon(IStacktraceElement element) {
      if (element instanceof IStacktraceError) {
        return imageProvider.getErrorIcon();
      } else if (element instanceof IStacktraceFrame) {
        return imageProvider.getStackIcon();
      } else {
        return imageProvider.getTargetIcon();
      }
    }
  }

  private static class TreeItemContentProvider implements ITreeContentProvider {
    @Override
    public Object[] getElements(Object inputElement) {
      return this.getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof List) {
        return ((List<?>) parentElement).toArray();
      } else if (parentElement instanceof StackTraceTreeBuilder.TreeItem) {
        return ((StackTraceTreeBuilder.TreeItem) parentElement).getChildren().toArray();
      } else {
        return new Object[0];
      }
    }

    @Override
    public Object getParent(Object element) {
      return element instanceof StackTraceTreeBuilder.TreeItem ? ((StackTraceTreeBuilder.TreeItem) element).getParent() : null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return this.getChildren(element).length > 0;
    }
  }
}