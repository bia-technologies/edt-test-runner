/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

import com._1c.g5.v8.dt.stacktraces.model.IStacktrace;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceError;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.ImageProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Элемент для отображения стека ошибок
 */
public class StackTracesTreeView extends TreeViewer {

  public StackTracesTreeView(Composite parent) {
    super(parent, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SHEET);
    createControl();
  }

  public void setStackTrace(IStacktrace stacktrace) {
    if (stacktrace != null) {
      List<TreeItem> items = new ArrayList<>();
      fillModel(items, stacktrace.getChilden(), null);
      this.setInput(items);
      this.setSelection(new StructuredSelection(items.get(0)));
      this.expandAll();
    } else {
      this.setInput(null);
    }
  }

  public IStacktraceElement getSelected() {
    ISelection selection = getSelection();
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      if (structuredSelection.size() == 1) {
        Object context = structuredSelection.getFirstElement();
        if (context instanceof TreeItem) {
          return ((TreeItem) context).getData();
        }
      }
    }
    return null;
  }

  private void fillModel(List<TreeItem> treeItems, List<IStacktraceElement> elements, TreeItem parent) {
    for (var item : elements) {
      TreeItem treeItem = new TreeItem(item, parent);
      treeItems.add(treeItem);
      fillModel(treeItem.getChildren(), item.getChilden(), treeItem);
    }
  }

  private void createControl() {
    GridDataFactory.fillDefaults().grab(true, true).applyTo(this.getControl());
    this.setContentProvider(new TreeItemContentProvider());
    this.setLabelProvider(new TreeItemColumnLabelProvider());
    ColumnViewerToolTipSupport.enableFor(this);
  }

  private static class TreeItem {
    final String text;
    final IStacktraceElement data;
    final List<TreeItem> children = new ArrayList<>();
    final TreeItem parent;

    public TreeItem(IStacktraceElement data, TreeItem parent) {
      this.data = data;
      this.parent = parent;
      var lines = data.getName().split(":", 2);
      text = String.join("\n", lines);
    }

    public String getText() {
      return text == null ? data.getName() : text;
    }

    public IStacktraceElement getData() {
      return data;
    }

    public List<TreeItem> getChildren() {
      return children;
    }

    public TreeItem getParent() {
      return parent;
    }
  }

  private static class TreeItemColumnLabelProvider extends ColumnLabelProvider {
    @Override
    public Image getImage(Object element) {
      var data = ((TreeItem) element).getData();
      if (data instanceof IStacktraceError) {
        return TestViewerPlugin.ui().createImage(ImageProvider.ERROR_ICON);
      } else if (data instanceof IStacktraceFrame) {
        return TestViewerPlugin.ui().createImage(ImageProvider.STACK_ICON);
      } else {
        return TestViewerPlugin.ui().createImage(ImageProvider.TARGET_ICON);
      }
    }

    @Override
    public String getText(Object element) {
      return ((TreeItem) element).getText();
    }

    @Override
    public String getToolTipText(Object element) {
      return ((TreeItem) element).getData().getName();
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
      } else if (parentElement instanceof TreeItem) {
        return ((TreeItem) parentElement).getChildren().toArray();
      } else {
        return new Object[0];
      }
    }

    @Override
    public Object getParent(Object element) {
      return element instanceof TreeItem ? ((TreeItem) element).getParent() : null;
    }

    @Override
    public boolean hasChildren(Object element) {
      return this.getChildren(element).length > 0;
    }
  }
}