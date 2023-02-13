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

import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceError;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceParser;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestErrorInfo;
import ru.biatech.edt.junit.model.TestStatus;
import ru.biatech.edt.junit.ui.ImageProvider;
import ru.biatech.edt.junit.v8utils.VendorServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Элемент для отображения стека ошибок
 */
 public class StackTracesTreeView extends TreeViewer {
  private final IStacktraceParser stacktraceParser;
  private final ImageProvider imageProvider = new ImageProvider();

  public StackTracesTreeView(Composite parent) {
    super(parent, SWT.V_SCROLL | SWT.SINGLE);
    setUseHashlookup(true);
    stacktraceParser = VendorServices.getStacktraceParser();
    createControl();
  }

  public void viewFailure(TestElement testElement) {
    if (testElement == null) {
      clear();
      return;
    }

    var items = new ArrayList<TreeItem>();
    for (var error : testElement.getErrorsList()) {
      TreeItem parent = null;

      if (!Strings.isNullOrEmpty(error.getMessage())) {
        items.add(parent = new TreeItem(error, null));
      }

      if (error.hasTrace()) {
        var stacktrace = stacktraceParser.parse(error.getTrace(), testElement.getTestName(), null);
        fillModel(parent == null ? items : parent.getChildren(), stacktrace.getChilden(), parent, error);
      }
    }

    this.setInput(items);
    if (!items.isEmpty()) {
      this.setSelection(new StructuredSelection(items.get(0)));
    }
    this.expandAll();
  }

  public void clear() {
    this.setInput(null);
  }

  public Object getSelected() {
    var item = getSelectedItem();
    return item == null ? null : item.getData();
  }

  public TestErrorInfo getSelectedError() {
    var item = getSelectedItem();
    return item == null ? null : item.getError();
  }

  public void dispose() {
    getTree().dispose();
    imageProvider.dispose();
  }

  private TreeItem getSelectedItem() {
    var selection = getSelection();
    if (selection instanceof IStructuredSelection) {
      var structuredSelection = (IStructuredSelection) selection;
      if (structuredSelection.size() == 1) {
        var context = structuredSelection.getFirstElement();
        if (context instanceof TreeItem) {
          return ((TreeItem) context);
        }
      }
    }
    return null;
  }

  private void fillModel(List<TreeItem> treeItems, List<IStacktraceElement> elements, TreeItem parent, TestErrorInfo error) {
    for (var item : elements) {
      var treeItem = new TreeItem(error, item, parent);
      treeItems.add(treeItem);
      fillModel(treeItem.getChildren(), item.getChilden(), treeItem, error);
    }
  }

  private void createControl() {
    GridDataFactory.fillDefaults().grab(true, true).applyTo(this.getControl());
    this.setContentProvider(new TreeItemContentProvider());
    this.setLabelProvider(new TreeItemColumnLabelProvider());
    ColumnViewerToolTipSupport.enableFor(this);
  }

  @Value
  @EqualsAndHashCode(exclude = "parent")
  private static class TreeItem {
    String text;
    Object data;
    List<TreeItem> children = new ArrayList<>();
    TreeItem parent;
    TestErrorInfo error;

    public TreeItem(TestErrorInfo error, IStacktraceElement data, TreeItem parent) {
      this.error = error;
      this.data = data;
      this.parent = parent;
      var lines = data.getName().split(":", 2);
      text = String.join("\n", lines);
    }

    public TreeItem(TestErrorInfo error, TreeItem parent) {
      this.error = error;
      this.data = error;
      this.parent = parent;
      var lines = error.getMessage().split(":", 2);
      text = String.join("\n", lines);
    }
  }

  private class TreeItemColumnLabelProvider extends ColumnLabelProvider {
    @Override
    public Image getImage(Object element) {
      var item = (TreeItem) element;
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
      return ((TreeItem) element).getText();
    }

    @Override
    public String getToolTipText(Object element) {
      var item = (TreeItem) element;
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