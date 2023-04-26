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

package ru.biatech.edt.junit.ui.stacktrace;

import com._1c.g5.v8.dt.stacktraces.model.IStacktraceElement;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceParser;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestErrorInfo;
import ru.biatech.edt.junit.v8utils.VendorServices;

import java.util.ArrayList;
import java.util.List;

public class StackTraceTreeBuilder {
  private final IStacktraceParser stacktraceParser;

  public StackTraceTreeBuilder() {
    this.stacktraceParser = VendorServices.getStacktraceParser();
  }

  public Tree getTree(TestElement testElement) {
    var root = new Tree();
    if (testElement == null) {
      return root;
    }
    for (var error : testElement.getErrorsList()) {
      Tree parent = root;

      if (!Strings.isNullOrEmpty(error.getMessage())) {
        var item = new TreeItem(error, null);
        root.children.add(item);
        parent = item;
      }

      if (error.hasTrace()) {
        var stacktrace = stacktraceParser.parse(error.getTrace(), testElement.getTestName(), null);
        fillModel(parent.getChildren(), stacktrace.getChilden(), parent, error);
      }
    }

    return root;
  }

  private void fillModel(List<TreeItem> treeItems, List<IStacktraceElement> elements, Tree parent, TestErrorInfo error) {
    for (var item : elements) {
      var moduleName = getModuleName(item);
      boolean isSide = moduleName.startsWith("ЮТ")||moduleName.startsWith("Мокито"); // TODO Возможны ложные срабатывания
      boolean isMain = !isSide && !moduleName.isBlank() && error.getTestName().contains(moduleName); // TODO Возможны ложные срабатывания
      var treeItem = new TreeItem(error, item, parent, isMain, isSide);
      treeItems.add(treeItem);
      fillModel(treeItem.getChildren(), item.getChilden(), treeItem, error);
    }
  }

  String getModuleName(IStacktraceElement element) {
    if (element instanceof IStacktraceFrame) {
      var symlink = ((IStacktraceFrame) element).getSymlink();
      var chunks = symlink.split("\\.");
      if (chunks.length >= 2) {
        return chunks[1];
      }
    }
    return "";
  }

  @Getter
  public static class Tree {
    private final List<TreeItem> children = new ArrayList<>();
  }

  @Value
  @EqualsAndHashCode(exclude = "parent", callSuper = true)
  public static class TreeItem extends Tree {
    String text;
    Object data;
    Tree parent;
    TestErrorInfo error;
    boolean main;
    boolean side;

    public TreeItem(TestErrorInfo error, IStacktraceElement data, Tree parent, boolean main, boolean side) {
      this.error = error;
      this.data = data;
      this.parent = parent;
      this.main = main;
      this.side = side;
      var lines = data.getName().split(":", 2);
      text = String.join("\n", lines);
    }

    public TreeItem(TestErrorInfo error, Tree parent) {
      this.error = error;
      this.data = error;
      this.parent = parent;
      main = false;
      side = false;
      var lines = error.getMessage().split(":", 2);
      text = String.join("\n", lines);
    }
  }
}
