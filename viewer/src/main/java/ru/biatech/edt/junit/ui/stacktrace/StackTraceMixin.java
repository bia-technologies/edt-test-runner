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

import lombok.Getter;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.ui.stacktrace.events.Listener;

import java.util.ArrayList;
import java.util.List;

public class StackTraceMixin {
  private final List<Listener> testElementChangedListeners = new ArrayList<>();
  private final List<Listener> openListeners = new ArrayList<>();
  private final List<Listener> selectionChangedListeners = new ArrayList<>();
  @Getter
  private ITestElement testElement;
  private final StackTraceTreeBuilder treeBuilder;
  @Getter
  private StackTraceTreeBuilder.TreeItem selectedItem;

  public StackTraceMixin() {
    treeBuilder = new StackTraceTreeBuilder();
  }

  public void clear() {
    setTestElement(null);
  }

  public StackTraceTreeBuilder.Tree getTree() {
    return treeBuilder.getTree(testElement);
  }

  public void setTestElement(ITestElement testElement) {
    if (this.testElement != testElement) {
      this.testElement = testElement;
      rise(testElementChangedListeners);
      setSelectedItem(null);
    }
  }

  public Object getSelectedItemData() {
    var item = getSelectedItem();
    return item == null ? null : item.getData();
  }

  public ErrorInfo getSelectedError() {
    var item = getSelectedItem();
    return item == null ? null : item.getError();
  }

  public void setSelectedItem(StackTraceTreeBuilder.TreeItem selectedItem) {
    if (this.selectedItem != selectedItem) {
      TestViewerPlugin.log().debug("Select item: " + (selectedItem == null ? "<null>" : selectedItem.getText()));
      this.selectedItem = selectedItem;
      rise(selectionChangedListeners);
    }
  }

  public void addTestElementChangedListener(Listener listener) {
    testElementChangedListeners.add(listener);
  }

  public void addSelectionChangedListeners(Listener listener) {
    selectionChangedListeners.add(listener);
  }

  public void addOpenListeners(Listener listener) {
    openListeners.add(listener);
  }

  void riseOnOpen() {
    rise(openListeners);
  }

  private static void rise(List<Listener> listeners) {
    final var lListeners = listeners;
    PlatformUI.getWorkbench().getDisplay().syncExec(() -> lListeners.forEach(Listener::handle));
  }
}
