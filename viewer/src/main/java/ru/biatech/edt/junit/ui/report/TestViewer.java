/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Xavier Coulon <xcoulon@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512 - [JUnit] test method name cut off before (
 *     Andrej Zachar <andrej@chocolatejar.eu> - [JUnit] Add a filter for ignored tests - https://bugs.eclipse.org/bugs/show_bug.cgi?id=298603
 *     Gautier de Saint Martin Lacaze <gautier.desaintmartinlacaze@gmail.com> - [JUnit] need 'collapse all' feature in JUnit view - https://bugs.eclipse.org/bugs/show_bug.cgi?id=277806
 *     Sandra Lions <sandra.lions-piron@oracle.com> - [JUnit] allow to sort by name and by execution time - https://bugs.eclipse.org/bugs/show_bug.cgi?id=219466
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.ui.report;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.PageBook;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.ITestElementContainer;
import ru.biatech.edt.junit.model.ITestSuiteElement;
import ru.biatech.edt.junit.model.Session;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestResult;
import ru.biatech.edt.junit.model.TestStatus;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.labelProvider.TestSessionLabelProvider;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart.SortingCriterion;
import ru.biatech.edt.junit.ui.report.actions.CopyFailureListAction;
import ru.biatech.edt.junit.ui.report.actions.OpenTestAction;
import ru.biatech.edt.junit.ui.report.actions.OpenUnderTestMethodAction;
import ru.biatech.edt.junit.ui.report.actions.RerunAction;
import ru.biatech.edt.junit.ui.report.contentProviders.TestSessionTableContentProvider;
import ru.biatech.edt.junit.ui.report.contentProviders.TestSessionTreeContentProvider;
import ru.biatech.edt.junit.ui.stacktrace.actions.CopyTraceAction;
import ru.biatech.edt.junit.ui.viewsupport.ColoringLabelProvider;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;
import ru.biatech.edt.junit.ui.viewsupport.SelectionProviderMediator;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс-помощник для отображения отчета о тестировании
 */
public class TestViewer {
  private final FailuresOnlyFilter fFailuresOnlyFilter = new FailuresOnlyFilter();
  private final IgnoredOnlyFilter fIgnoredOnlyFilter = new IgnoredOnlyFilter();

  private final TestRunnerViewPart fTestRunnerPart;

  private PageBook fViewerBook;
  private TreeViewer fTreeViewer;
  private TestSessionTreeContentProvider fTreeContentProvider;
  private TestSessionLabelProvider fTreeLabelProvider;
  private TableViewer fTableViewer;
  private TestSessionLabelProvider fTableLabelProvider;
  private SelectionProviderMediator fSelectionProvider;

  private int fLayoutMode;
  private boolean fTreeHasFilter;
  private boolean fTableHasFilter;

  private Session session;

  private boolean fTreeNeedsRefresh;
  private boolean fTableNeedsRefresh;
  private HashSet<ITestElement> fNeedUpdate;
  private ITestCaseElement fAutoScrollTarget;

  private LinkedList<ITestSuiteElement> fAutoClose;
  private HashSet<ITestSuiteElement> fAutoExpand;
  private final ImageProvider imageProvider = new ImageProvider();

  public TestViewer(Composite parent, TestRunnerViewPart runner) {
    fTestRunnerPart = runner;

    fLayoutMode = TestRunnerViewPart.LAYOUT_HIERARCHICAL;

    createTestViewers(parent);

    registerViewersRefresh();

    initContextMenu();
  }

  public Control getTestViewerControl() {
    return fViewerBook;
  }

  public synchronized void registerActiveSession(Session session) {
    this.session = session;
    registerAutoScrollTarget(null);
    registerViewersRefresh();
  }

  public synchronized void setShowTime(boolean showTime) {
    try {
      fViewerBook.setRedraw(false);
      fTreeLabelProvider.setShowTime(showTime);
      fTableLabelProvider.setShowTime(showTime);
    } finally {
      fViewerBook.setRedraw(true);
    }
  }

  public synchronized void setSortingCriterion(SortingCriterion sortingCriterion) {
    ViewerComparator viewerComparator;
    switch (sortingCriterion) {
      case SORT_BY_EXECUTION_ORDER:
        viewerComparator = null;
        break;
      case SORT_BY_EXECUTION_TIME:
        viewerComparator = new TestExecutionTimeComparator();
        break;
      case SORT_BY_NAME:
        viewerComparator = new TestNameComparator();
        break;
      default:
        viewerComparator = null;
        break;
    }
    fTableViewer.setComparator(viewerComparator);
    fTreeViewer.setComparator(viewerComparator);
  }

  void handleDefaultSelected() {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    if (selection.size() != 1) {
      return;
    }

    var testElement = (ITestElement) selection.getFirstElement();

    OpenTestAction action;
    if (testElement instanceof TestSuiteElement) {
      action = getOpenTestAction((TestSuiteElement) testElement);
    } else if (testElement instanceof TestCaseElement) {
      action = getOpenTestAction((TestCaseElement) testElement);
    } else {
      throw new IllegalStateException(String.valueOf(testElement));
    }

    if (action.isEnabled()) {
      action.run();
    }
  }

  /**
   * It makes sense to display either failed or ignored tests, not both together.
   *
   * @param failuresOnly whether to show only failed tests
   * @param ignoredOnly  whether to show only skipped tests
   * @param layoutMode   the layout mode
   */
  synchronized void setShowFailuresOrIgnoredOnly(boolean failuresOnly, boolean ignoredOnly, int layoutMode) {
    /*
     * Management of fTreeViewer and fTableViewer
     * ******************************************
     * - invisible viewer is updated on registerViewerUpdate unless its f*NeedsRefresh is true
     * - invisible viewer is not refreshed upfront
     * - on layout change, new viewer is refreshed if necessary
     * - filter only applies to "current" layout mode / viewer
     */
    try {
      fViewerBook.setRedraw(false);

      IStructuredSelection selection = null;
      boolean switchLayout = layoutMode != fLayoutMode;
      if (switchLayout) {
        selection = (IStructuredSelection) fSelectionProvider.getSelection();
        if (layoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
          if (fTreeNeedsRefresh) {
            clearUpdateAndExpansion();
          }
        } else {
          if (fTableNeedsRefresh) {
            clearUpdateAndExpansion();
          }
        }
        fLayoutMode = layoutMode;
        fViewerBook.showPage(getActiveViewer().getControl());
      }
      //avoid realizing all TableItems, especially in flat mode!
      StructuredViewer viewer = getActiveViewer();
      if (failuresOnly || ignoredOnly) {
        if (getActiveViewerHasFilter()) {
          //For simplicity clear both filters (only one of them is used)
          viewer.removeFilter(fFailuresOnlyFilter);
          viewer.removeFilter(fIgnoredOnlyFilter);
        }
        setActiveViewerHasFilter(true);
        viewer.setInput(null);
        //Set either the failures or the skipped tests filter
        ViewerFilter filter = fFailuresOnlyFilter;
        if (ignoredOnly) {
          filter = fIgnoredOnlyFilter;
        }
        viewer.addFilter(filter);
        setActiveViewerNeedsRefresh(true);

      } else {
        if (getActiveViewerHasFilter()) {
          setActiveViewerNeedsRefresh(true);
          setActiveViewerHasFilter(false);
          viewer.setInput(null);
          viewer.removeFilter(fIgnoredOnlyFilter);
          viewer.removeFilter(fFailuresOnlyFilter);
        }
      }
      processChangesInUI();

      if (selection != null) {
        // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=125708
        // (ITreeSelection not adapted if TreePaths changed):
        StructuredSelection flatSelection = new StructuredSelection(selection.toList());
        fSelectionProvider.setSelection(flatSelection, true);
      }

    } finally {
      fViewerBook.setRedraw(true);
    }
  }

  /**
   * To be called periodically by the TestRunnerViewPart (in the UI thread).
   */
  void processChangesInUI() {
    if (session == null) {
      registerViewersRefresh();
      fTreeNeedsRefresh = false;
      fTableNeedsRefresh = false;
      fTreeViewer.setInput(null);
      fTableViewer.setInput(null);
      return;
    }
    var viewer = getActiveViewer();
    if (getActiveViewerNeedsRefresh()) {
      clearUpdateAndExpansion();
      setActiveViewerNeedsRefresh(false);
      viewer.setInput(session);

    } else {
      ITestElement[] toUpdate;
      synchronized (this) {
        toUpdate = fNeedUpdate.toArray(ITestElement[]::new);
        fNeedUpdate.clear();
      }
      if (!fTreeNeedsRefresh && toUpdate.length > 0) {
        if (fTreeHasFilter) {
          for (var element : toUpdate) {
            updateElementInTree(element);
          }
        } else {
          var toUpdateWithParents = new HashSet<>(Arrays.asList(toUpdate));
          for (var element : toUpdate) {
            var parent = element.getParent();
            while (parent != null) {
              toUpdateWithParents.add(parent);
              parent = parent.getParent();
            }
          }
          fTreeViewer.update(toUpdateWithParents.toArray(), null);
        }
      }
      if (!fTableNeedsRefresh && toUpdate.length > 0) {
        if (fTableHasFilter) {
          for (var element : toUpdate) {
            updateElementInTable(element);
          }
        } else {
          fTableViewer.update(toUpdate, null);
        }
      }
    }
    autoScrollInUI();
  }

  void selectFirstFailure() {
    var firstFailure = getNextChildFailure(session, true);
    if (firstFailure != null) {
      getActiveViewer().setSelection(new StructuredSelection(firstFailure), true);
    }
  }

  void selectFailure(boolean showNext) {
    var selection = (IStructuredSelection) getActiveViewer().getSelection();
    var selected = (ITestElement) selection.getFirstElement();
    ITestElement next;

    if (selected == null) {
      next = getNextChildFailure(session, showNext);
    } else {
      next = getNextFailure(selected, showNext);
    }

    if (next != null) {
      getActiveViewer().setSelection(new StructuredSelection(next), true);
    }
  }

  synchronized void registerViewersRefresh() {
    fTreeNeedsRefresh = true;
    fTableNeedsRefresh = true;
    clearUpdateAndExpansion();
  }

  /**
   * @param testElement the added test
   */
  synchronized void registerTestAdded(ITestElement testElement) {
    //TODO: performance: would only need to refresh parent of added element
    fTreeNeedsRefresh = true;
    fTableNeedsRefresh = true;
  }

  synchronized void registerViewerUpdate(final ITestElement testElement) {
    fNeedUpdate.add(testElement);
  }

  void registerAutoScrollTarget(ITestCaseElement testCaseElement) {
    fAutoScrollTarget = testCaseElement;
  }

  synchronized void registerFailedForAutoScroll(ITestElement testElement) {
    TestSuiteElement parent = (TestSuiteElement) fTreeContentProvider.getParent(testElement);
    if (parent != null) {
      fAutoExpand.add(parent);
    }
  }

  void expandFirstLevel() {
    fTreeViewer.expandToLevel(2);
  }

  private void createTestViewers(Composite parent) {
    fViewerBook = new PageBook(parent, SWT.NULL);

    fTreeViewer = new TreeViewer(fViewerBook, SWT.V_SCROLL | SWT.SINGLE);
    fTreeViewer.setUseHashlookup(true);
    fTreeContentProvider = new TestSessionTreeContentProvider();
    fTreeViewer.setContentProvider(fTreeContentProvider);
    fTreeLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestRunnerViewPart.LAYOUT_HIERARCHICAL);
    fTreeViewer.setLabelProvider(new ColoringLabelProvider(fTreeLabelProvider));

    fTableViewer = new TableViewer(fViewerBook, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
    fTableViewer.setUseHashlookup(true);
    TestSessionTableContentProvider fTableContentProvider = new TestSessionTableContentProvider();
    fTableViewer.setContentProvider(fTableContentProvider);
    fTableLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestRunnerViewPart.LAYOUT_FLAT);
    fTableViewer.setLabelProvider(new ColoringLabelProvider(fTableLabelProvider));

    fSelectionProvider = new SelectionProviderMediator(new StructuredViewer[]{fTreeViewer, fTableViewer}, fTreeViewer);
    fSelectionProvider.addSelectionChangedListener(new TestSelectionListener());
    TestOpenListener testOpenListener = new TestOpenListener();
    fTreeViewer.getTree().addSelectionListener(testOpenListener);
    fTableViewer.getTable().addSelectionListener(testOpenListener);

    fTestRunnerPart.getSite().setSelectionProvider(fSelectionProvider);

    fViewerBook.showPage(fTreeViewer.getTree());
  }

  private void initContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(this::handleMenuAboutToShow);
    fTestRunnerPart.getSite().registerContextMenu(menuMgr, fSelectionProvider);
    Menu menu = menuMgr.createContextMenu(fViewerBook);
    fTreeViewer.getTree().setMenu(menu);
    fTableViewer.getTable().setMenu(menu);
  }

  private void handleMenuAboutToShow(IMenuManager manager) {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    if (!selection.isEmpty()) {
      var testElement = (ITestElement) selection.getFirstElement();

      if (testElement instanceof ITestSuiteElement) {
        var testSuiteElement = (ITestSuiteElement) testElement;
        manager.add(getOpenTestAction(testSuiteElement));
        manager.add(new Separator());
        if (!fTestRunnerPart.lastLaunchIsKeptAlive()) {
          addRerunActions(manager, testSuiteElement);
        }
      } else {
        TestCaseElement testCaseElement = (TestCaseElement) testElement;

        addRerunActions(manager, testCaseElement);
        manager.add(new Separator());
        manager.add(getOpenTestAction(testCaseElement));
        manager.add(new OpenUnderTestMethodAction(fTestRunnerPart, testCaseElement));

      }
      if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
        manager.add(new Separator());
        manager.add(new ExpandAllAction());
        manager.add(new CollapseAllAction());
      }
      manager.add(new Separator());
      var action = new CopyTraceAction();
      action.handleTestSelected(testElement);
      manager.add(action);
    }

    if (session != null && session.getFailureCount() + session.getErrorCount() > 0) {
      manager.add(new CopyFailureListAction(fTestRunnerPart));
    }
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
  }

  private void addRerunActions(IMenuManager manager, ITestCaseElement testCaseElement) {
    String className = testCaseElement.getClassName();
    if (fTestRunnerPart.lastLaunchIsKeptAlive()) {
      manager.add(new RerunAction(UIMessages.RerunAction_label_rerun, fTestRunnerPart, className, ILaunchManager.RUN_MODE));
    } else {
      manager.add(new RerunAction(UIMessages.Run_Test_Label, fTestRunnerPart, className, ILaunchManager.RUN_MODE));
      manager.add(new RerunAction(UIMessages.Debug_Test_Label, fTestRunnerPart, className, ILaunchManager.DEBUG_MODE));
    }
  }

  private void addRerunActions(IMenuManager manager, ITestSuiteElement testSuiteElement) {
    // TODO Разобраться в меню. Метод закоменчен, но команды имеются
//		String qualifiedName= null;
//		String testMethodName= null; // test method name is null when re-running a regular test class
//
//		String testName= testSuiteElement.getTestName();
//
//		IType testType= findTestClass(testSuiteElement, true);
//		if (testType != null) {
//			qualifiedName= testType.getFullyQualifiedName();
//
//			if (!qualifiedName.equals(testName)) {
//				int index= testName.indexOf('(');
//				if (index > 0) { // test factory method
//					testMethodName= testName.substring(0, index);
//				}
//			}
//			String[] parameterTypes= testSuiteElement.getParameterTypes();
//			if (testMethodName != null && parameterTypes != null) {
//				String paramTypesStr= Arrays.stream(parameterTypes).collect(Collectors.joining(",")); //$NON-NLS-1$
//				testMethodName= testMethodName + "(" + paramTypesStr + ")"; //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		} else {
//			// see bug 443498
//			testType= findTestClass(testSuiteElement.getParent(), false);
//			if (testType != null) {
//				qualifiedName= testType.getFullyQualifiedName();
//
//				String className= testSuiteElement.getSuiteTypeName();
//				if (!qualifiedName.equals(className)) {
//					testMethodName= testName;
//				}
//			}
//		}
//		if (qualifiedName != null) {
//			manager.add(new RerunAction(JUnitMessages.RerunAction_label_run, fTestRunnerPart, testSuiteElement.getId(), qualifiedName, testMethodName, testSuiteElement.getDisplayName(), testSuiteElement.getUniqueId(), ILaunchManager.RUN_MODE));
//			manager.add(new RerunAction(JUnitMessages.RerunAction_label_debug, fTestRunnerPart, testSuiteElement.getId(), qualifiedName, testMethodName, testSuiteElement.getDisplayName(), testSuiteElement.getUniqueId(), ILaunchManager.DEBUG_MODE));
//		}
  }

  private OpenTestAction getOpenTestAction(ITestCaseElement testCase) {
    return new OpenTestAction(fTestRunnerPart, testCase);
  }

  private OpenTestAction getOpenTestAction(ITestSuiteElement testSuite) {
    var children = testSuite.getChildren();
    if (children.length > 0) {
      return new OpenTestAction(fTestRunnerPart, children[0]);
    }

    return new OpenTestAction(fTestRunnerPart, testSuite);
  }

  private void handleSelected() {
    var selection = (IStructuredSelection) fSelectionProvider.getSelection();
    ITestElement testElement = null;
    if (selection.size() == 1) {
      testElement = (ITestElement) selection.getFirstElement();
    }
    fTestRunnerPart.handleTestSelected(testElement);
  }

  private Comparator<ITestElement> getComparator() {
    var sortingCriterion = fTestRunnerPart.settings.getSortingCriterion();
    Comparator<ITestElement> comparator;
    switch (sortingCriterion) {
      case SORT_BY_EXECUTION_TIME:
        comparator = Comparator.comparing(ITestElement::getElapsedTimeInSeconds);
        break;
      case SORT_BY_NAME:
        comparator = Comparator.comparing(ITestElement::getName, String::compareToIgnoreCase);
        break;
      default:
        comparator = null;
        break;
    }
    return comparator;
  }

  private StructuredViewer getActiveViewer() {
    return fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL ? fTreeViewer : fTableViewer;
  }

  private boolean getActiveViewerHasFilter() {
    return fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL ? fTreeHasFilter : fTableHasFilter;
  }

  private void setActiveViewerHasFilter(boolean filter) {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
      fTreeHasFilter = filter;
    } else {
      fTableHasFilter = filter;
    }
  }

  private boolean getActiveViewerNeedsRefresh() {
    return fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL ? fTreeNeedsRefresh : fTableNeedsRefresh;
  }

  private void setActiveViewerNeedsRefresh(boolean needsRefresh) {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
      fTreeNeedsRefresh = needsRefresh;
    } else {
      fTableNeedsRefresh = needsRefresh;
    }
  }

  private void updateElementInTree(final ITestElement testElement) {
    if (isShown(testElement)) {
      updateShownElementInTree(testElement);
    } else {
      var current = testElement;
      do {
        if (fTreeViewer.testFindItem(current) != null) {
          fTreeViewer.remove(current);
        }
        current = current.getParent();
      } while (current != null && !isShown(current));

      while (current != null) {
        fTreeViewer.update(current, null);
        current = current.getParent();
      }
    }
  }

  private void updateShownElementInTree(ITestElement testElement) {
    if (testElement == null) { // paranoia null check
      return;
    }

    var parent = testElement.getParent();
    updateShownElementInTree(parent); // make sure parent is shown and up-to-date

    if (fTreeViewer.testFindItem(testElement) == null) {
      fTreeViewer.add(parent, testElement); // if not yet in tree: add
    } else {
      fTreeViewer.update(testElement, null); // if in tree: update
    }
  }

  private void updateElementInTable(ITestElement element) {
    if (isShown(element)) {
      if (fTableViewer.testFindItem(element) == null) {
        var previous = getNextFailure(element, false);
        int insertionIndex = -1;
        if (previous != null) {
          TableItem item = (TableItem) fTableViewer.testFindItem(previous);
          if (item != null) {
            insertionIndex = fTableViewer.getTable().indexOf(item);
          }
        }
        fTableViewer.insert(element, insertionIndex);
      } else {
        fTableViewer.update(element, null);
      }
    } else {
      fTableViewer.remove(element);
    }
  }

  private boolean isShown(ITestElement current) {
    return fFailuresOnlyFilter.select(current);
  }

  private void autoScrollInUI() {
    if (!fTestRunnerPart.settings.isAutoScroll()) {
      clearAutoExpand();
      fAutoClose.clear();
      return;
    }

    if (fLayoutMode == TestRunnerViewPart.LAYOUT_FLAT) {
      if (fAutoScrollTarget != null) {
        fTableViewer.reveal(fAutoScrollTarget);
      }
      return;
    }

    synchronized (this) {
      for (ITestSuiteElement suite : fAutoExpand) {
        fTreeViewer.setExpandedState(suite, true);
      }
      clearAutoExpand();
    }

    ITestCaseElement current = fAutoScrollTarget;
    fAutoScrollTarget = null;

    TestSuiteElement parent = current == null ? null : (TestSuiteElement) fTreeContentProvider.getParent(current);
    if (fAutoClose.isEmpty() || !fAutoClose.getLast().equals(parent)) {
      // we're in a new branch, so let's close old OK branches:
      for (var iter = fAutoClose.listIterator(fAutoClose.size()); iter.hasPrevious(); ) {
        var previousAutoOpened = iter.previous();
        if (previousAutoOpened.equals(parent)) {
          break;
        }

        if (previousAutoOpened.getStatus() == TestStatus.OK) {
          // auto-opened the element, and all children are OK -> auto close
          iter.remove();
          fTreeViewer.collapseToLevel(previousAutoOpened, AbstractTreeViewer.ALL_LEVELS);
        }
      }

      while (parent != null && !fTreeViewer.getExpandedState(parent)) {
        fAutoClose.add(parent); // add to auto-opened elements -> close later if STATUS_OK
        parent = (TestSuiteElement) fTreeContentProvider.getParent(parent);
      }
    }
    if (current != null) {
      fTreeViewer.reveal(current);
    }
  }

  private ITestElement getNextFailure(ITestElement selected, boolean showNext) {
    if (selected instanceof TestSuiteElement) {
      var nextChild = getNextChildFailure((TestSuiteElement) selected, showNext);
      if (nextChild != null) {
        return nextChild;
      }
    }
    return getNextFailureSibling(selected, showNext);
  }

  private ITestElement getNextFailureSibling(ITestElement current, boolean showNext) {
    var parent = current.getParent();
    if (parent == null) {
      return null;
    }

    var elements = parent.getChildren();
    var comparator = getComparator();
    if (comparator != null) {
      Arrays.sort(elements, comparator);
    }
    var siblings = Arrays.asList(elements);

    if (!showNext) {
      siblings = new ReverseList<>(siblings);
    }

    var nextIndex = siblings.indexOf(current) + 1;
    for (int i = nextIndex; i < siblings.size(); i++) {
      var sibling = siblings.get(i);
      if (sibling.getStatus().isErrorOrFailure()) {
        if (sibling instanceof ITestCaseElement) {
          return sibling;
        } else {
          var testSuiteElement = (ITestSuiteElement) sibling;
          if (testSuiteElement.getChildren().length == 0) {
            return testSuiteElement;
          }
          return getNextChildFailure(testSuiteElement, showNext);
        }
      }
    }
    return getNextFailureSibling(parent, showNext);
  }

  private ITestElement getNextChildFailure(ITestElementContainer root, boolean showNext) {
    var elements = root.getChildren();
    var comparator = getComparator();
    if (comparator != null) {
      Arrays.sort(elements, comparator);
    }
    var children = Arrays.asList(elements);
    if (!showNext) {
      children = new ReverseList<>(children);
    }
    for (var element : children) {
      if (element.getStatus().isErrorOrFailure()) {
        if (element instanceof ITestCaseElement) {
          return element;
        } else {
          var testSuiteElement = (ITestSuiteElement) element;
          if (testSuiteElement.getChildren().length == 0) {
            return testSuiteElement;
          }
          return getNextChildFailure(testSuiteElement, showNext);
        }
      }
    }
    return null;
  }

  private void clearUpdateAndExpansion() {
    fNeedUpdate = new LinkedHashSet<>();
    fAutoClose = new LinkedList<>();
    fAutoExpand = new HashSet<>();
  }

  private synchronized void clearAutoExpand() {
    fAutoExpand.clear();
  }

  public void dispose() {
    imageProvider.dispose();
  }

  private final class TestSelectionListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      handleSelected();
    }
  }

  private final class TestOpenListener extends SelectionAdapter {
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      handleDefaultSelected();
    }
  }

  private static final class TestNameComparator extends ViewerComparator {
    @Override
    public int compare(Viewer viewer, Object testElement1, Object testElement2) {
      String testName1 = ((ITestElement) testElement1).getName();
      String testName2 = ((ITestElement) testElement2).getName();
      return testName1.compareToIgnoreCase(testName2);
    }
  }

  private static final class TestExecutionTimeComparator extends ViewerComparator {
    @Override
    public int compare(Viewer viewer, Object testElement1, Object testElement2) {
      double elapsedTime1 = ((ITestElement) testElement1).getElapsedTimeInSeconds();
      double elapsedTime2 = ((ITestElement) testElement2).getElapsedTimeInSeconds();
      return Double.compare(elapsedTime2, elapsedTime1);
    }
  }

  private static class ReverseList<E> extends AbstractList<E> {
    private final List<E> fList;

    public ReverseList(List<E> list) {
      fList = list;
    }

    @Override
    public E get(int index) {
      return fList.get(fList.size() - index - 1);
    }

    @Override
    public int size() {
      return fList.size();
    }
  }

  private class ExpandAllAction extends Action {
    public ExpandAllAction() {
      setText(UIMessages.ExpandAllAction_text);
      setToolTipText(UIMessages.ExpandAllAction_tooltip);
      setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ICONS_EXPAND_ALL));
    }

    @Override
    public void run() {
      fTreeViewer.expandAll();
    }
  }

  private class CollapseAllAction extends Action {
    public CollapseAllAction() {
      setText(UIMessages.CollapseAllAction_text);
      setToolTipText(UIMessages.CollapseAllAction_tooltip);
      setImageDescriptor(ImageProvider.getImageDescriptor(ImageProvider.ICONS_COLLAPSE_ALL));
    }

    @Override
    public void run() {
      fTreeViewer.collapseAll();
    }
  }

  private final class FailuresOnlyFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return select(((ITestElement) element));
    }

    public boolean select(ITestElement testElement) {
      TestStatus status = testElement.getStatus();
      return status.isErrorOrFailure() || (!session.isRunning() && status == TestStatus.RUNNING);  // rerunning
    }
  }

  private final class IgnoredOnlyFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return select(((ITestElement) element));
    }

    public boolean select(ITestElement testElement) {
      return hasIgnoredInTestResult(testElement) || (!session.isRunning() && testElement.getStatus() == TestStatus.RUNNING); // rerunning
    }

    /**
     * Checks whether a test was skipped i.e. it was ignored (<code>@Ignored</code>) or had any
     * assumption failure.
     *
     * @param testElement the test element (a test suite or a single test case)
     * @return <code>true</code> if the test element or any of its children has
     * {@link TestResult#SKIPPED} test result
     */
    private boolean hasIgnoredInTestResult(ITestElement testElement) {
      if (testElement instanceof ITestSuiteElement) {
        var children = ((ITestSuiteElement) testElement).getChildren();
        for (var child : children) {
          var hasIgnoredTestResult = hasIgnoredInTestResult(child);
          if (hasIgnoredTestResult) {
            return true;
          }
        }
        return false;
      }

      return testElement.getResultStatus(false) == TestResult.SKIPPED;
    }
  }

}