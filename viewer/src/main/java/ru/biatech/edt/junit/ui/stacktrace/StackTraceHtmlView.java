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
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceError;
import com._1c.g5.v8.dt.stacktraces.model.IStacktraceFrame;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.report.ErrorInfo;
import ru.biatech.edt.junit.model.report.Failure;
import ru.biatech.edt.junit.ui.stacktrace.actions.CompareResultsAction;
import ru.biatech.edt.junit.ui.stacktrace.actions.CopyTraceAction;
import ru.biatech.edt.junit.ui.stacktrace.events.Listener;
import ru.biatech.edt.junit.ui.viewsupport.Colors;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Элемент для отображения стека ошибок с использованием web view
 */
public class StackTraceHtmlView implements StackTraceView {
  private Browser browser;
  private static final String PAGE_TEMPLATE_NAME = "/resources/stack-trace.html";
  private static final String CLICK_EVENT = "click";
  private static final String DBL_CLICK_EVENT = "dblclick";
  private static final String ACTION_EVENT = "action";
  private final String PAGE_TEMPLATE;

  private IAction openAction;
  private IAction compareAction;
  private IAction copyTraceAction;

  private final StackTraceMixin mixin = new StackTraceMixin();

  private Listener colorChangedListener;
  private BrowserFunction riseJavaEvent;
  private final Map<String, StackTraceTreeBuilder.TreeItem> treeItems = new HashMap<>();

  public StackTraceHtmlView(Composite parent) {
    mixin.addTestElementChangedListener(this::renderTest);
    PAGE_TEMPLATE = getResourceContent(PAGE_TEMPLATE_NAME);
    initBrowser(parent);
    renderTest();

    Colors.addColorChangedListener(colorChangedListener = this::renderTest);
  }

  /**
   * {@link StackTraceView#viewFailure(ITestElement)}
   */
  @Override
  public void viewFailure(ITestElement testElement) {
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
  public ErrorInfo getSelectedError() {
    return mixin.getSelectedError();
  }

  /**
   * {@link StackTraceView#registerMenu(MenuManager)}
   */
  @Override
  public void registerMenu(MenuManager menuManager) {
    for (var item : menuManager.getItems()) {
      if (item instanceof ActionContributionItem) {
        var actionItem = (ActionContributionItem) item;
        if (actionItem.getAction() instanceof FailureViewer.OpenAction) {
          openAction = actionItem.getAction();
        } else if (actionItem.getAction() instanceof CompareResultsAction) {
          compareAction = actionItem.getAction();
        } else if (actionItem.getAction() instanceof CopyTraceAction) {
          copyTraceAction = actionItem.getAction();
        }
      }
    }
  }

  /**
   * {@link StackTraceView#dispose()}
   */
  @Override
  public void dispose() {
    if (riseJavaEvent != null && !riseJavaEvent.isDisposed()) {
      riseJavaEvent.dispose();
      riseJavaEvent = null;
    }
    if (browser != null && !browser.isDisposed()) {
      browser.close();
      browser.dispose();
    }
    if (colorChangedListener != null) {
      Colors.removeColorChangedListener(colorChangedListener);
      colorChangedListener = null;
    }
    browser = null;
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
    return browser;
  }

  private static String getResourceContent(String resourceName) {
    var content = "";
    try (var stream = TestViewerPlugin.getDefault().getResourceStream(resourceName); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      content = read(reader);
    } catch (IOException e) {
      TestViewerPlugin.log().logError(e);
    }
    return content;
  }

  private static String read(Reader reader) {
    var buffer = new StringBuilder();
    var readBuffer = new char[2048];

    try {
      var read = reader.read(readBuffer);
      while (read > 0) {
        buffer.append(readBuffer, 0, read);
        read = reader.read(readBuffer);
      }
      return buffer.toString();
    } catch (IOException e) {
      TestViewerPlugin.log().logError(e);
    }
    return "";
  }

  private void initBrowser(Composite parent) {
    browser = new Browser(parent, SWT.NONE);
    browser.setJavascriptEnabled(true);
    browser.setBackground(Colors.BG_COLOR);
    browser.setBackgroundMode(SWT.NONE);
    browser.addProgressListener(ProgressListener.completedAdapter(progressEvent -> registerEventHandler()));
  }

  private void handleViewEvent(String eventData) {
    if (eventData == null || eventData.isBlank() || !eventData.startsWith("{")) {
      return;
    }
    TestViewerPlugin.log().debug("Handling browser event: " + eventData);
    var parser = new GsonBuilder().create();
    var event = parser.fromJson(eventData, EventData.class);

    switch (event.getEvent()) {
      case CLICK_EVENT:
        TestViewerPlugin.log().debug("click event: " + event.getElement());
        mixin.setSelectedItem(treeItems.get(event.getElement()));
        break;
      case DBL_CLICK_EVENT:
        TestViewerPlugin.log().debug("double click event: " + event.getElement());
        mixin.setSelectedItem(treeItems.get(event.getElement()));
        mixin.riseOnOpen();
        break;
      case ACTION_EVENT:
        TestViewerPlugin.log().debug("action event: " + event.getElement());
        callAction(event.getElement());
        break;
    }
  }

  private void callAction(String element) {
    IAction action = null;
    switch (element) {
      case "gotoLineAction":
        action = openAction;
        break;
      case "compareAction":
        action = compareAction;
        break;
      case "copyTraceAction":
        action = copyTraceAction;
        break;
    }
    if (action != null && action.isEnabled()) {
      action.run();
    }
  }

  private void renderTest() {
    treeItems.clear();
    var tree = mixin.getTree();
    String content;
    if (tree.getChildren().isEmpty()) {
      content = emptyPage();
    } else {
      var html = new HTMLRender();
      renderTree(tree, html, 0);
      var tree_content = html.getContent();
      content = HTMLRender.replaceColors(PAGE_TEMPLATE).replace("TREE_CONTENT", tree_content);
    }

    browser.setText(content, true);
    TestViewerPlugin.log().debug(content);
  }

  private void registerEventHandler() {
    var methodName = "riseJavaEvent";
    var exists = browser.evaluate(String.format("return typeof %s !== 'undefined'", methodName), true);
    if (Boolean.TRUE.equals(exists)) {
      return;
    }
    if (riseJavaEvent != null && !riseJavaEvent.isDisposed()) {
      riseJavaEvent.dispose();
    }
    riseJavaEvent = new JSFunction(browser, methodName);
  }

  private String emptyPage() {
    var template = "<html><body style=\"background: BG_COLOR;\"><script>document.oncontextmenu = function (e) { e.preventDefault() }</script></body></html";
    return HTMLRender.replaceColors(template);
  }

  private void renderTree(StackTraceTreeBuilder.Tree tree, HTMLRender html, int level) {
    var offset = 2;
    for (var item : tree.getChildren()) {
      var statusClass = getCssClassName(item);
      if (item.isMain()) {
        statusClass += " main";
      }
      if (item.isSide()) {
        statusClass += " side";
      }
      var nested = !item.getChildren().isEmpty();

      var id = pushToTreeItems(item);
      var indent = String.valueOf(level * offset);
      if (nested) {
        html.start("div")
            .text("<div class=\"row ").text(statusClass).text("\" id =\"").text(id).text("\" style=\"padding-left:").text(indent).text("em\">")
            .start("span", "caret").end("span")
            .start("span", "status").end("span")
            .text(item.getText())
            .end("div")
            .start("div", "nested");
        renderTree(item, html, level + 1);
        html.end("div").end("div");
      } else {
        html.text("<div class=\"row ").text(statusClass).text("\" id =\"").text(id).text("\" style=\"padding-left:").text(indent).text("em\">")
            .start("span", "status").end("span")
            .text(item.getText())
            .end("div");
      }
    }
  }

  private String pushToTreeItems(StackTraceTreeBuilder.TreeItem item) {
    String id = "row_" + treeItems.size();
    treeItems.put(id, item);
    return id;
  }

  private String getCssClassName(StackTraceTreeBuilder.TreeItem item) {
    var data = item.getData();
    if (data instanceof ErrorInfo) {
      if (data instanceof Failure) {
        return "failure";
      } else {
        return "error";
      }

    } else if (data instanceof IStacktraceElement) {
      if (data instanceof IStacktraceError) {
        return "error";
      } else if (data instanceof IStacktraceFrame) {
        return "line";
      }
    }
    return "unknown";
  }

  @Data
  private static class EventData {
    String event;
    String element;
  }

  private class JSFunction extends BrowserFunction {
    public JSFunction(Browser browser, String name) {
      super(browser, name);
    }

    @Override
    public Object function(Object[] arguments) {
      TestViewerPlugin.log().debug(String.format("call browser function: %s(%s)", getName(), Arrays.toString(arguments)));
      if (arguments.length == 1 && arguments[0] instanceof String) {
        handleViewEvent((String) arguments[0]);
      }
      return null;
    }
  }
}
