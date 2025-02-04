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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.ITestSuiteElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.ui.utils.StringUtilities;
import ru.biatech.edt.junit.v8utils.VendorServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceTreeBuilder {
  private static final List<String> SIDE_MODULE_PREFIXES = Arrays.asList("ЮТ", "Мокито");
  private final IStacktraceParser stacktraceParser;

  public StackTraceTreeBuilder() {
    this.stacktraceParser = VendorServices.getStacktraceParser();
  }

  public Tree getTree(ITestElement testElement) {
    var root = new Tree();
    if (testElement == null) {
      return root;
    }
    testElement.getErrorsList().forEach(error -> {
      Tree parent = root;

      if (!StringUtilities.isNullOrEmpty(error.getMessage())) {
        var item = new TreeItem(error, null);
        root.children.add(item);
        parent = item;
      }

      var fullMethodName = testElement instanceof ITestCaseElement ? ((ITestCaseElement) testElement).getClassName() : ((ITestSuiteElement) testElement).getClassName();
      if (!StringUtilities.isNullOrEmpty(error.getTrace())) {
        var stacktrace = stacktraceParser.parse(error.getTrace(), fullMethodName, null);
        fillModel(parent.getChildren(), stacktrace.getChilden(), parent, error, fullMethodName);
      }
    });

    return root;
  }

  private void fillModel(List<TreeItem> treeItems, List<IStacktraceElement> elements, Tree parent, ErrorInfo error, String fullMethodName) {
    for (var item : elements) {
      var moduleName = getModuleName(item);
      boolean isEngineModule = isEngineModule(moduleName);
      boolean isMain = !isEngineModule && !moduleName.isBlank() && fullMethodName.contains(moduleName);

      var treeItem = new TreeItem(error, item, parent, isMain, isEngineModule);
      treeItems.add(treeItem);
      fillModel(treeItem.getChildren(), item.getChilden(), treeItem, error, fullMethodName);
    }
  }

  private boolean isEngineModule(String moduleName) {
    return SIDE_MODULE_PREFIXES.stream()
        .anyMatch(moduleName::startsWith);
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
    ErrorInfo error;
    boolean main;
    boolean side;

    public TreeItem(ErrorInfo error, IStacktraceElement data, Tree parent, boolean main, boolean side) {
      this.error = error;
      this.data = data;
      this.parent = parent;
      this.main = main;
      this.side = side;
      var lines = data.getName().split(":", 2);
      text = String.join("\n", lines);
    }

    public TreeItem(ErrorInfo error, Tree parent) {
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
