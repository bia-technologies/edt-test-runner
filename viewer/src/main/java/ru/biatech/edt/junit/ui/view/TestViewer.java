/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.view;

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
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.PageBook;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.ITestElement.Result;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestElement.Status;
import ru.biatech.edt.junit.model.TestRoot;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.TestSessionTableContentProvider;
import ru.biatech.edt.junit.ui.TestSessionTreeContentProvider;
import ru.biatech.edt.junit.ui.view.TestRunnerViewPart.SortingCriterion;
import ru.biatech.edt.junit.ui.viewsupport.ColoringLabelProvider;
import ru.biatech.edt.junit.ui.viewsupport.SelectionProviderMediator;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;


public class TestViewer {
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

  private final class FailuresOnlyFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return select(((TestElement) element));
    }

    public boolean select(TestElement testElement) {
      Status status = testElement.getStatus();
      if (status.isErrorOrFailure())
        return true;
      else
        return !fTestRunSession.isRunning() && status == Status.RUNNING;  // rerunning
    }
  }

  private final class IgnoredOnlyFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return select(((TestElement) element));
    }

    public boolean select(TestElement testElement) {
      if (hasIgnoredInTestResult(testElement))
        return true;
      else
        return !fTestRunSession.isRunning() && testElement.getStatus() == Status.RUNNING; // rerunning
    }

    /**
     * Checks whether a test was skipped i.e. it was ignored (<code>@Ignored</code>) or had any
     * assumption failure.
     *
     * @param testElement the test element (a test suite or a single test case)
     * @return <code>true</code> if the test element or any of its children has
     * {@link Result#IGNORED} test result
     */
    private boolean hasIgnoredInTestResult(TestElement testElement) {
      if (testElement instanceof TestSuiteElement) {
        ITestElement[] children = ((TestSuiteElement) testElement).getChildren();
        for (ITestElement child : children) {
          boolean hasIgnoredTestResult = hasIgnoredInTestResult((TestElement) child);
          if (hasIgnoredTestResult) {
            return true;
          }
        }
        return false;
      }

      return testElement.getTestResult(false) == Result.IGNORED;
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
      setText(JUnitMessages.ExpandAllAction_text);
      setToolTipText(JUnitMessages.ExpandAllAction_tooltip);
      setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/expandall.png")); //$NON-NLS-1$
    }

    @Override
    public void run() {
      fTreeViewer.expandAll();
    }
  }

  private class CollapseAllAction extends Action {
    public CollapseAllAction() {
      setText(JUnitMessages.CollapseAllAction_text);
      setToolTipText(JUnitMessages.CollapseAllAction_tooltip);
      setImageDescriptor(TestViewerPlugin.ui().getImageDescriptor("elcl16/collapseall.png")); //$NON-NLS-1$
    }

    @Override
    public void run() {
      fTreeViewer.collapseAll();
    }
  }

  private final FailuresOnlyFilter fFailuresOnlyFilter = new FailuresOnlyFilter();
  private final IgnoredOnlyFilter fIgnoredOnlyFilter = new IgnoredOnlyFilter();

  private final TestRunnerViewPart fTestRunnerPart;
  private final Clipboard fClipboard;

  private PageBook fViewerbook;
  private TreeViewer fTreeViewer;
  private ru.biatech.edt.junit.ui.TestSessionTreeContentProvider fTreeContentProvider;
  private ru.biatech.edt.junit.ui.view.TestSessionLabelProvider fTreeLabelProvider;
  private TableViewer fTableViewer;
  private ru.biatech.edt.junit.ui.TestSessionTableContentProvider fTableContentProvider;
  private ru.biatech.edt.junit.ui.view.TestSessionLabelProvider fTableLabelProvider;
  private SelectionProviderMediator fSelectionProvider;

  private int fLayoutMode;
  private boolean fTreeHasFilter;
  private boolean fTableHasFilter;

  private TestRunSession fTestRunSession;

  private boolean fTreeNeedsRefresh;
  private boolean fTableNeedsRefresh;
  private HashSet<TestElement> fNeedUpdate;
  private TestCaseElement fAutoScrollTarget;

  private LinkedList<TestSuiteElement> fAutoClose;
  private HashSet<TestSuiteElement> fAutoExpand;


  public TestViewer(Composite parent, Clipboard clipboard, ru.biatech.edt.junit.ui.view.TestRunnerViewPart runner) {
    fTestRunnerPart = runner;
    fClipboard = clipboard;

    fLayoutMode = ru.biatech.edt.junit.ui.view.TestRunnerViewPart.LAYOUT_HIERARCHICAL;

    createTestViewers(parent);

    registerViewersRefresh();

    initContextMenu();
  }

  private void createTestViewers(Composite parent) {
    fViewerbook = new PageBook(parent, SWT.NULL);

    fTreeViewer = new TreeViewer(fViewerbook, SWT.V_SCROLL | SWT.SINGLE);
    fTreeViewer.setUseHashlookup(true);
    fTreeContentProvider = new TestSessionTreeContentProvider();
    fTreeViewer.setContentProvider(fTreeContentProvider);
    fTreeLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestRunnerViewPart.LAYOUT_HIERARCHICAL);
    fTreeViewer.setLabelProvider(new ColoringLabelProvider(fTreeLabelProvider));

    fTableViewer = new TableViewer(fViewerbook, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
    fTableViewer.setUseHashlookup(true);
    fTableContentProvider = new TestSessionTableContentProvider();
    fTableViewer.setContentProvider(fTableContentProvider);
    fTableLabelProvider = new TestSessionLabelProvider(fTestRunnerPart, TestRunnerViewPart.LAYOUT_FLAT);
    fTableViewer.setLabelProvider(new ColoringLabelProvider(fTableLabelProvider));

    fSelectionProvider = new SelectionProviderMediator(new StructuredViewer[]{fTreeViewer, fTableViewer}, fTreeViewer);
    fSelectionProvider.addSelectionChangedListener(new TestSelectionListener());
    TestOpenListener testOpenListener = new TestOpenListener();
    fTreeViewer.getTree().addSelectionListener(testOpenListener);
    fTableViewer.getTable().addSelectionListener(testOpenListener);

    fTestRunnerPart.getSite().setSelectionProvider(fSelectionProvider);

    fViewerbook.showPage(fTreeViewer.getTree());
  }

  private void initContextMenu() {
    MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(this::handleMenuAboutToShow);
    fTestRunnerPart.getSite().registerContextMenu(menuMgr, fSelectionProvider);
    Menu menu = menuMgr.createContextMenu(fViewerbook);
    fTreeViewer.getTree().setMenu(menu);
    fTableViewer.getTable().setMenu(menu);
  }


  void handleMenuAboutToShow(IMenuManager manager) {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    if (!selection.isEmpty()) {
      TestElement testElement = (TestElement) selection.getFirstElement();

      if (testElement instanceof TestSuiteElement) {
        TestSuiteElement testSuiteElement = (TestSuiteElement) testElement;
        manager.add(getOpenTestAction(testSuiteElement));
        manager.add(new Separator());
        if (!fTestRunnerPart.lastLaunchIsKeptAlive()) {
          addRerunActions(manager, testSuiteElement);
        }
      } else {
        TestCaseElement testCaseElement = (TestCaseElement) testElement;
        manager.add(getOpenTestAction(testCaseElement));
        manager.add(new Separator());
        addRerunActions(manager, testCaseElement);
      }
      if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
        manager.add(new Separator());
        manager.add(new ExpandAllAction());
        manager.add(new CollapseAllAction());
      }

    }
    if (fTestRunSession != null && fTestRunSession.getFailureCount() + fTestRunSession.getErrorCount() > 0) {
      if (fLayoutMode != TestRunnerViewPart.LAYOUT_HIERARCHICAL)
        manager.add(new Separator());
      manager.add(new CopyFailureListAction(fTestRunnerPart, fClipboard));
    }
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
  }

  private void addRerunActions(IMenuManager manager, TestCaseElement testCaseElement) {
    String className = testCaseElement.getClassName();
    String testMethodName = testCaseElement.getTestMethodName();
    String[] parameterTypes = testCaseElement.getParameterTypes();
    if (parameterTypes != null) {
      String paramTypesStr = Arrays.stream(parameterTypes).collect(Collectors.joining(",")); //$NON-NLS-1$
      testMethodName = testMethodName + "(" + paramTypesStr + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    if (fTestRunnerPart.lastLaunchIsKeptAlive()) {
      manager.add(new RerunAction(JUnitMessages.RerunAction_label_rerun, fTestRunnerPart, className, ILaunchManager.RUN_MODE));
    } else {
      manager.add(new RerunAction(JUnitMessages.RerunAction_label_run, fTestRunnerPart, className, ILaunchManager.RUN_MODE));
      manager.add(new RerunAction(JUnitMessages.RerunAction_label_debug, fTestRunnerPart, className, ILaunchManager.DEBUG_MODE));
    }
  }

  private void addRerunActions(IMenuManager manager, TestSuiteElement testSuiteElement) {
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

  public Control getTestViewerControl() {
    return fViewerbook;
  }

  public synchronized void registerActiveSession(TestRunSession testRunSession) {
    fTestRunSession = testRunSession;
    registerAutoScrollTarget(null);
    registerViewersRefresh();
  }

  void handleDefaultSelected() {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    if (selection.size() != 1)
      return;

    TestElement testElement = (TestElement) selection.getFirstElement();

    OpenTestAction action;
    if (testElement instanceof TestSuiteElement) {
      action = getOpenTestAction((TestSuiteElement) testElement);
    } else if (testElement instanceof TestCaseElement) {
      action = getOpenTestAction((TestCaseElement) testElement);
    } else {
      throw new IllegalStateException(String.valueOf(testElement));
    }

    if (action.isEnabled())
      action.run();
  }

  private OpenTestAction getOpenTestAction(TestCaseElement testCase) {
    return new OpenTestAction(fTestRunnerPart, testCase);
  }

  private OpenTestAction getOpenTestAction(TestSuiteElement testSuite) {
    String testName = testSuite.getTestName();
    ITestElement[] children = testSuite.getChildren();

    if (testName.startsWith("[") && testName.endsWith("]") && children.length > 0 && children[0] instanceof TestCaseElement) { //$NON-NLS-1$ //$NON-NLS-2$
      // a group of parameterized tests
      return new OpenTestAction(fTestRunnerPart, (TestCaseElement) children[0]);
    }

    int index = testName.indexOf('(');
    // test factory method
    if (index > 0) {
      return new OpenTestAction(fTestRunnerPart, testName.substring(0, index));
    }

    // regular test class
    return new OpenTestAction(fTestRunnerPart, testName);
  }

  private void handleSelected() {
    IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
    TestElement testElement = null;
    if (selection.size() == 1) {
      testElement = (TestElement) selection.getFirstElement();
    }
    fTestRunnerPart.handleTestSelected(testElement);
  }

  public synchronized void setShowTime(boolean showTime) {
    try {
      fViewerbook.setRedraw(false);
      fTreeLabelProvider.setShowTime(showTime);
      fTableLabelProvider.setShowTime(showTime);
    } finally {
      fViewerbook.setRedraw(true);
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

  private final class TestNameComparator extends ViewerComparator {
    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
      return compareName(o1, o2);
    }
  }

  private final class TestExecutionTimeComparator extends ViewerComparator {
    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
      return compareElapsedTime(o1, o2);
    }
  }

  private Comparator<ITestElement> getComparator() {
    SortingCriterion sortingCriterion = fTestRunnerPart.getSortingCriterion();
    Comparator<ITestElement> comparator;
    switch (sortingCriterion) {
      case SORT_BY_EXECUTION_TIME:
        comparator = this::compareElapsedTime;
        break;
      case SORT_BY_NAME:
        comparator = this::compareName;
        break;
      default:
        comparator = null;
        break;
    }
    return comparator;
  }

  private int compareElapsedTime(Object o1, Object o2) {
    double elapsedTime1 = ((TestElement) o1).getElapsedTimeInSeconds();
    double elapsedTime2 = ((TestElement) o2).getElapsedTimeInSeconds();
    return Double.compare(elapsedTime2, elapsedTime1);
  }

  private int compareName(Object o1, Object o2) {
    String testName1 = ((TestElement) o1).getTestName();
    String testName2 = ((TestElement) o2).getTestName();
    return testName1.toLowerCase().compareTo(testName2.toLowerCase());
  }

  /**
   * It makes sense to display either failed or ignored tests, not both together.
   *
   * @param failuresOnly whether to show only failed tests
   * @param ignoredOnly  whether to show only skipped tests
   * @param layoutMode   the layout mode
   */
  public synchronized void setShowFailuresOrIgnoredOnly(boolean failuresOnly, boolean ignoredOnly, int layoutMode) {
    /*
     * Management of fTreeViewer and fTableViewer
     * ******************************************
     * - invisible viewer is updated on registerViewerUpdate unless its f*NeedsRefresh is true
     * - invisible viewer is not refreshed upfront
     * - on layout change, new viewer is refreshed if necessary
     * - filter only applies to "current" layout mode / viewer
     */
    try {
      fViewerbook.setRedraw(false);

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
        fViewerbook.showPage(getActiveViewer().getControl());
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
      fViewerbook.setRedraw(true);
    }
  }

  private boolean getActiveViewerHasFilter() {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL)
      return fTreeHasFilter;
    else
      return fTableHasFilter;
  }

  private void setActiveViewerHasFilter(boolean filter) {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL)
      fTreeHasFilter = filter;
    else
      fTableHasFilter = filter;
  }

  public StructuredViewer getActiveViewer() {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL)
      return fTreeViewer;
    else
      return fTableViewer;
  }

  private boolean getActiveViewerNeedsRefresh() {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL)
      return fTreeNeedsRefresh;
    else
      return fTableNeedsRefresh;
  }

  private void setActiveViewerNeedsRefresh(boolean needsRefresh) {
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL)
      fTreeNeedsRefresh = needsRefresh;
    else
      fTableNeedsRefresh = needsRefresh;
  }

  /**
   * To be called periodically by the TestRunnerViewPart (in the UI thread).
   */
  public void processChangesInUI() {
    TestRoot testRoot;
    if (fTestRunSession == null) {
      registerViewersRefresh();
      fTreeNeedsRefresh = false;
      fTableNeedsRefresh = false;
      fTreeViewer.setInput(null);
      fTableViewer.setInput(null);
      return;
    }

    testRoot = fTestRunSession.getTestRoot();

    StructuredViewer viewer = getActiveViewer();
    if (getActiveViewerNeedsRefresh()) {
      clearUpdateAndExpansion();
      setActiveViewerNeedsRefresh(false);
      viewer.setInput(testRoot);

    } else {
      Object[] toUpdate;
      synchronized (this) {
        toUpdate = fNeedUpdate.toArray();
        fNeedUpdate.clear();
      }
      if (!fTreeNeedsRefresh && toUpdate.length > 0) {
        if (fTreeHasFilter)
          for (Object element : toUpdate)
            updateElementInTree((TestElement) element);
        else {
          HashSet<Object> toUpdateWithParents = new HashSet<>(Arrays.asList(toUpdate));
          for (Object element : toUpdate) {
            TestElement parent = ((TestElement) element).getParent();
            while (parent != null) {
              toUpdateWithParents.add(parent);
              parent = parent.getParent();
            }
          }
          fTreeViewer.update(toUpdateWithParents.toArray(), null);
        }
      }
      if (!fTableNeedsRefresh && toUpdate.length > 0) {
        if (fTableHasFilter)
          for (Object element : toUpdate)
            updateElementInTable((TestElement) element);
        else
          fTableViewer.update(toUpdate, null);
      }
    }
    autoScrollInUI();
  }

  private void updateElementInTree(final TestElement testElement) {
    if (isShown(testElement)) {
      updateShownElementInTree(testElement);
    } else {
      TestElement current = testElement;
      do {
        if (fTreeViewer.testFindItem(current) != null)
          fTreeViewer.remove(current);
        current = current.getParent();
      } while (!(current instanceof TestRoot) && !isShown(current));

      while (current != null && !(current instanceof TestRoot)) {
        fTreeViewer.update(current, null);
        current = current.getParent();
      }
    }
  }

  private void updateShownElementInTree(TestElement testElement) {
    if (testElement == null || testElement instanceof TestRoot) // paranoia null check
      return;

    TestSuiteElement parent = testElement.getParent();
    updateShownElementInTree(parent); // make sure parent is shown and up-to-date

    if (fTreeViewer.testFindItem(testElement) == null) {
      fTreeViewer.add(parent, testElement); // if not yet in tree: add
    } else {
      fTreeViewer.update(testElement, null); // if in tree: update
    }
  }

  private void updateElementInTable(TestElement element) {
    if (isShown(element)) {
      if (fTableViewer.testFindItem(element) == null) {
        TestElement previous = getNextFailure(element, false);
        int insertionIndex = -1;
        if (previous != null) {
          TableItem item = (TableItem) fTableViewer.testFindItem(previous);
          if (item != null)
            insertionIndex = fTableViewer.getTable().indexOf(item);
        }
        fTableViewer.insert(element, insertionIndex);
      } else {
        fTableViewer.update(element, null);
      }
    } else {
      fTableViewer.remove(element);
    }
  }

  private boolean isShown(TestElement current) {
    return fFailuresOnlyFilter.select(current);
  }

  private void autoScrollInUI() {
    if (!fTestRunnerPart.isAutoScroll()) {
      clearAutoExpand();
      fAutoClose.clear();
      return;
    }

    if (fLayoutMode == TestRunnerViewPart.LAYOUT_FLAT) {
      if (fAutoScrollTarget != null)
        fTableViewer.reveal(fAutoScrollTarget);
      return;
    }

    synchronized (this) {
      for (TestSuiteElement suite : fAutoExpand) {
        fTreeViewer.setExpandedState(suite, true);
      }
      clearAutoExpand();
    }

    TestCaseElement current = fAutoScrollTarget;
    fAutoScrollTarget = null;

    TestSuiteElement parent = current == null ? null : (TestSuiteElement) fTreeContentProvider.getParent(current);
    if (fAutoClose.isEmpty() || !fAutoClose.getLast().equals(parent)) {
      // we're in a new branch, so let's close old OK branches:
      for (ListIterator<TestSuiteElement> iter = fAutoClose.listIterator(fAutoClose.size()); iter.hasPrevious(); ) {
        TestSuiteElement previousAutoOpened = iter.previous();
        if (previousAutoOpened.equals(parent))
          break;

        if (previousAutoOpened.getStatus() == TestElement.Status.OK) {
          // auto-opened the element, and all children are OK -> auto close
          iter.remove();
          fTreeViewer.collapseToLevel(previousAutoOpened, AbstractTreeViewer.ALL_LEVELS);
        }
      }

      while (parent != null && !fTestRunSession.getTestRoot().equals(parent) && !fTreeViewer.getExpandedState(parent)) {
        fAutoClose.add(parent); // add to auto-opened elements -> close later if STATUS_OK
        parent = (TestSuiteElement) fTreeContentProvider.getParent(parent);
      }
    }
    if (current != null)
      fTreeViewer.reveal(current);
  }

  public void selectFirstFailure() {
    TestElement firstFailure = getNextChildFailure(fTestRunSession.getTestRoot(), true);
    if (firstFailure != null)
      getActiveViewer().setSelection(new StructuredSelection(firstFailure), true);
  }

  public void selectFailure(boolean showNext) {
    IStructuredSelection selection = (IStructuredSelection) getActiveViewer().getSelection();
    TestElement selected = (TestElement) selection.getFirstElement();
    TestElement next;

    if (selected == null) {
      next = getNextChildFailure(fTestRunSession.getTestRoot(), showNext);
    } else {
      next = getNextFailure(selected, showNext);
    }

    if (next != null)
      getActiveViewer().setSelection(new StructuredSelection(next), true);
  }

  private TestElement getNextFailure(TestElement selected, boolean showNext) {
    if (selected instanceof TestSuiteElement) {
      TestElement nextChild = getNextChildFailure((TestSuiteElement) selected, showNext);
      if (nextChild != null)
        return nextChild;
    }
    return getNextFailureSibling(selected, showNext);
  }

  private TestElement getNextFailureSibling(TestElement current, boolean showNext) {
    TestSuiteElement parent = current.getParent();
    if (parent == null)
      return null;

    ITestElement[] elements = parent.getChildren();
    Comparator<ITestElement> comparator = getComparator();
    if (comparator != null) {
      Arrays.sort(elements, comparator);
    }
    List<ITestElement> siblings = Arrays.asList(elements);

    if (!showNext)
      siblings = new ReverseList<>(siblings);

    int nextIndex = siblings.indexOf(current) + 1;
    for (int i = nextIndex; i < siblings.size(); i++) {
      TestElement sibling = (TestElement) siblings.get(i);
      if (sibling.getStatus().isErrorOrFailure()) {
        if (sibling instanceof TestCaseElement) {
          return sibling;
        } else {
          TestSuiteElement testSuiteElement = (TestSuiteElement) sibling;
          if (testSuiteElement.getChildren().length == 0) {
            return testSuiteElement;
          }
          return getNextChildFailure(testSuiteElement, showNext);
        }
      }
    }
    return getNextFailureSibling(parent, showNext);
  }

  private TestElement getNextChildFailure(TestSuiteElement root, boolean showNext) {
    ITestElement[] elements = root.getChildren();
    Comparator<ITestElement> comparator = getComparator();
    if (comparator != null) {
      Arrays.sort(elements, comparator);
    }
    List<ITestElement> children = Arrays.asList(elements);
    if (!showNext)
      children = new ReverseList<>(children);
    for (ITestElement element : children) {
      TestElement child = (TestElement) element;
      if (child.getStatus().isErrorOrFailure()) {
        if (child instanceof TestCaseElement) {
          return child;
        } else {
          TestSuiteElement testSuiteElement = (TestSuiteElement) child;
          if (testSuiteElement.getChildren().length == 0) {
            return testSuiteElement;
          }
          return getNextChildFailure(testSuiteElement, showNext);
        }
      }
    }
    return null;
  }

  public synchronized void registerViewersRefresh() {
    fTreeNeedsRefresh = true;
    fTableNeedsRefresh = true;
    clearUpdateAndExpansion();
  }

  private void clearUpdateAndExpansion() {
    fNeedUpdate = new LinkedHashSet<>();
    fAutoClose = new LinkedList<>();
    fAutoExpand = new HashSet<>();
  }

  /**
   * @param testElement the added test
   */
  public synchronized void registerTestAdded(TestElement testElement) {
    //TODO: performance: would only need to refresh parent of added element
    fTreeNeedsRefresh = true;
    fTableNeedsRefresh = true;
  }

  public synchronized void registerViewerUpdate(final TestElement testElement) {
    fNeedUpdate.add(testElement);
  }

  private synchronized void clearAutoExpand() {
    fAutoExpand.clear();
  }

  public void registerAutoScrollTarget(TestCaseElement testCaseElement) {
    fAutoScrollTarget = testCaseElement;
  }

  public synchronized void registerFailedForAutoScroll(TestElement testElement) {
    TestSuiteElement parent = (TestSuiteElement) fTreeContentProvider.getParent(testElement);
    if (parent != null)
      fAutoExpand.add(parent);
  }

  public void expandFirstLevel() {
    fTreeViewer.expandToLevel(2);
  }

}

