/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

import lombok.Getter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.report.actions.settings.ActivateOnErrorAction;
import ru.biatech.edt.junit.ui.report.actions.settings.FailuresOnlyFilterAction;
import ru.biatech.edt.junit.ui.report.actions.settings.IgnoredOnlyFilterAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ScrollLockAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ShowTestHierarchyAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ShowTimeAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ShowWebStackTraceAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ToggleOrientationAction;
import ru.biatech.edt.junit.ui.report.actions.settings.ToggleSortingAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class ToolBarManager {
  public static final int VIEW_ORIENTATION_VERTICAL = 0;
  public static final int VIEW_ORIENTATION_HORIZONTAL = 1;
  public static final int VIEW_ORIENTATION_AUTOMATIC = 2;

  private final TestRunnerViewPart view;

  private List<UpdateAble> updateAble;
  private Action nextItemAction;
  private Action previousItemAction;
  private FailuresOnlyFilterAction failuresOnlyFilterAction;
  private IgnoredOnlyFilterAction ignoredOnlyFilterAction;
  private ScrollLockAction scrollLockAction;
  private List<ToggleOrientationAction> toggleOrientationActions;
  private ShowTestHierarchyAction showTestHierarchyAction;
  private ShowTimeAction showTimeAction;
  private ShowWebStackTraceAction showWebStackTraceAction;
  private ActivateOnErrorAction activateOnErrorAction;
  private List<ToggleSortingAction> toggleSortingActions;
  private Action rerunSessionAction;
  private Action rerunFailedTestsAction;

  private IMenuListener viewMenuListener;

  public ToolBarManager(TestRunnerViewPart view) {
    this.view = view;
  }

  public void configureToolBar() {
    IActionBars actionBars = view.getViewSite().getActionBars();
    IToolBarManager toolBar = actionBars.getToolBarManager();
    IMenuManager viewMenu = actionBars.getMenuManager();
    var settings = view.getSettings();

    nextItemAction = new ShowNextFailureAction(view);
    nextItemAction.setEnabled(false);

    previousItemAction = new ShowPreviousFailureAction(view);
    previousItemAction.setEnabled(false);

    failuresOnlyFilterAction = new FailuresOnlyFilterAction(settings);
    ignoredOnlyFilterAction = new IgnoredOnlyFilterAction(settings);

    rerunSessionAction = new RerunLastAction(view);
    rerunFailedTestsAction = new RerunLastFailedFirstAction(view);

    scrollLockAction = new ScrollLockAction(settings);
    showTestHierarchyAction = new ShowTestHierarchyAction(settings);
    // Global actions
    actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), nextItemAction);
    actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), previousItemAction);

    // Fill toolbar
    toolBar.add(showTestHierarchyAction);
    toolBar.add(nextItemAction);
    toolBar.add(previousItemAction);
    toolBar.add(new Separator());
    toolBar.add(failuresOnlyFilterAction);
    toolBar.add(ignoredOnlyFilterAction);
    toolBar.add(scrollLockAction);
    toolBar.add(new Separator());
    toolBar.add(rerunSessionAction);
    toolBar.add(rerunFailedTestsAction);
    toolBar.add(new Separator());
    toolBar.add(view.getViewHistoryManager().createHistoryDropDownAction());


    // Fill view menu
    viewMenu.add(showTestHierarchyAction);
    viewMenu.add(showTimeAction = new ShowTimeAction(settings));
    viewMenu.add(new Separator());

    toggleSortingActions = List.of(
        new ToggleSortingAction(settings, TestRunnerViewPart.SortingCriterion.SORT_BY_EXECUTION_ORDER),
        new ToggleSortingAction(settings, TestRunnerViewPart.SortingCriterion.SORT_BY_EXECUTION_TIME),
        new ToggleSortingAction(settings, TestRunnerViewPart.SortingCriterion.SORT_BY_NAME))
    ;
    var sortByMenu = new MenuManager(UIMessages.TestRunnerViewPart_sort_by_menu);
    toggleSortingActions.forEach(sortByMenu::add);
    viewMenu.add(sortByMenu);

    viewMenu.add(new Separator());

    toggleOrientationActions = List.of(
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_VERTICAL),
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_HORIZONTAL),
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_AUTOMATIC))
    ;
    MenuManager layoutSubMenu = new MenuManager(UIMessages.TestRunnerViewPart_layout_menu);
    toggleOrientationActions.forEach(layoutSubMenu::add);
    viewMenu.add(layoutSubMenu);

    viewMenu.add(new Separator());

    viewMenu.add(failuresOnlyFilterAction);
    viewMenu.add(ignoredOnlyFilterAction);
    viewMenu.add(activateOnErrorAction = new ActivateOnErrorAction(settings));
    viewMenu.add(showWebStackTraceAction = new ShowWebStackTraceAction(settings));

    viewMenuListener = manager -> activateOnErrorAction.update();

    viewMenu.addMenuListener(viewMenuListener);
    actionBars.updateActionBars();

    updateAble = new ArrayList<>(Arrays.asList(
        failuresOnlyFilterAction,
        ignoredOnlyFilterAction,
        scrollLockAction,
        showTestHierarchyAction,
        showTimeAction,
        showWebStackTraceAction,
        activateOnErrorAction
    ));
    updateAble.addAll(toggleOrientationActions);
    updateAble.addAll(toggleSortingActions);
  }

  public void updateActions() {
    boolean hasErrorsOrFailures = view.hasErrorsOrFailures();
    nextItemAction.setEnabled(hasErrorsOrFailures);
    previousItemAction.setEnabled(hasErrorsOrFailures);

    updateAble.forEach(UpdateAble::update);
  }

  public void onChangedSession() {
    var session = view.getSession();

    var sessionRunnable = session != null && session.getLaunch() != null;
    rerunSessionAction.setEnabled(sessionRunnable);
    rerunFailedTestsAction.setEnabled(sessionRunnable && view.hasErrorsOrFailures());
  }

  public void dispose() {
    if (viewMenuListener != null) {
      view.getViewSite().getActionBars().getMenuManager().removeMenuListener(viewMenuListener);
    }
  }
}
