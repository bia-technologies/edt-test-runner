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
 *     Julien Ruaux: jruaux@octo.com see bug 25324 Ability to know when tests are finished [junit]
 *     Vincent Massol: vmassol@octo.com 25324 Ability to know when tests are finished [junit]
 *     Sebastian Davids: sdavids@gmx.de 35762 JUnit View wasting a lot of screen space [JUnit]
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Achim Demelt <a.demelt@exxcellent.de> - [junit] Separate UI from non-UI code - https://bugs.eclipse.org/bugs/show_bug.cgi?id=278844
 *     Andrew Eisenberg <andrew@eisenberg.as> - [JUnit] Rerun failed first does not work with JUnit4 - https://bugs.eclipse.org/bugs/show_bug.cgi?id=140392
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - [JUnit] Avoid rerun test launch on UI thread - https://bugs.eclipse.org/bugs/show_bug.cgi?id=411841
 *     Andrew Eisenberg <andrew@eisenberg.as> - [JUnit] Add a monospace font option for the junit results view - https://bugs.eclipse.org/bugs/show_bug.cgi?id=411794
 *     Andrej Zachar <andrej@chocolatejar.eu> - [JUnit] Add a filter for ignored tests - https://bugs.eclipse.org/bugs/show_bug.cgi?id=298603
 *     Sandra Lions <sandra.lions-piron@oracle.com> - [JUnit] allow to sort by name and by execution time - https://bugs.eclipse.org/bugs/show_bug.cgi?id=219466
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.report;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import lombok.Getter;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.PageSwitcher;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestKind;
import ru.biatech.edt.junit.model.ISessionListener;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.Session;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.UIPreferencesConstants;
import ru.biatech.edt.junit.ui.report.actions.ToolBarManager;
import ru.biatech.edt.junit.ui.report.history.RunnerViewHistory;
import ru.biatech.edt.junit.ui.stacktrace.FailureViewer;
import ru.biatech.edt.junit.ui.stacktrace.actions.CopyTraceAction;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

import java.text.MessageFormat;

/**
 * A ViewPart that shows the results of a test run.
 */
public class TestRunnerViewPart extends ViewPart implements SettingsEventListener {

  public static final String NAME = "ru.biatech.edt.junit.ResultView"; //$NON-NLS-1$
  public static final int LAYOUT_FLAT = 0;
  public static final int LAYOUT_HIERARCHICAL = 1;
  public static final Object FAMILY_JUNIT_RUN = new Object();
  //orientations
  public static final int VIEW_ORIENTATION_VERTICAL = 0;
  public static final int VIEW_ORIENTATION_HORIZONTAL = 1;
  public static final int VIEW_ORIENTATION_AUTOMATIC = 2;

  /**
   * @since 3.5
   */
  public static final String PREF_LAST_PATH = "lastImportExportPath"; //$NON-NLS-1$

  private FailureViewer failureViewer;
  @Getter
  private final ImageProvider imageProvider;
  private ProgressBar fProgressBar;
  private Image fViewImage;
  private CounterPanel fCounterPanel;
  private volatile String fInfoMessage;
  private boolean fPartIsVisible;
  Image fOriginalViewImage;
  /**
   * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
   * <code>VIEW_ORIENTATION_VERTICAL</code>.
   */
  private int fCurrentOrientation;
  private TestViewer fTestViewer;
  /**
   * Is the UI disposed?
   */
  private boolean fIsDisposed;
  /**
   * Actions
   */
  @Getter
  ToolBarManager toolBar = new ToolBarManager(this);
  private CopyTraceAction fCopyAction;
  @Getter
  private Session session;
  @Getter
  private RunnerViewHistory viewHistoryManager;
  private SessionListener fTestRunSessionListener;
  @Getter
  final ReportSettings settings;
  private SashForm fSashForm;
  private Composite fCounterComposite;
  private Composite fParent;

  private final IPartListener2 fPartListener = new IPartListener2() {
    @Override
    public void partVisible(IWorkbenchPartReference ref) {
      if (getSite().getId().equals(ref.getId())) {
        fPartIsVisible = true;
      }
    }

    @Override
    public void partHidden(IWorkbenchPartReference ref) {
      if (getSite().getId().equals(ref.getId())) {
        fPartIsVisible = false;
      }
    }
  };



  public TestRunnerViewPart() {
    imageProvider = new ImageProvider();
    settings = new ReportSettings(this);
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    settings.setMemento(memento);
    IWorkbenchSiteProgressService progressService = getProgressService();
    if (progressService != null) {
      progressService.showBusyForFamily(TestRunnerViewPart.FAMILY_JUNIT_RUN);
    }
  }

  private IWorkbenchSiteProgressService getProgressService() {
    return getSite().getAdapter(IWorkbenchSiteProgressService.class);
  }

  public void selectNextFailure() {
    fTestViewer.selectFailure(true);
  }

  public void selectPreviousFailure() {
    fTestViewer.selectFailure(false);
  }

  /**
   * @return the display name of the current test run sessions kind, or <code>null</code>
   */
  public String getTestKindDisplayName() {
    ITestKind kind = session.getTestRunnerKind();
    if (!kind.isNull()) {
      return kind.getDisplayName();
    }
    return null;
  }

  public void registerInfoMessage(String message) {
    fInfoMessage = message;
  }

  @Override
  public synchronized void dispose() {
    fIsDisposed = true;
    if (fTestRunSessionListener != null) {
      TestViewerPlugin.core().getSessionsManager().removeTestRunSessionListener(fTestRunSessionListener);
    }

    setActiveSession(null);

    getViewSite().getPage().removePartListener(fPartListener);

    imageProvider.dispose();
    toolBar.dispose();
    if (failureViewer != null) {
      failureViewer.dispose();
    }
    fTestViewer.dispose();
  }

  public IV8Project getLaunchedProject() {
    return session == null ? null : session.getLaunchedProject();
  }

  public boolean lastLaunchIsKeptAlive() {
    return session != null && session.isKeptAlive();
  }

  public Shell getShell() {
    return fParent.getShell();
  }

  /**
   * @param session new active test run session
   * @return deactivated session, or <code>null</code> iff no session got deactivated
   */
  public Session setActiveSession(Session session) {
/*
- State:
fTestRunSession
fTestSessionListener
Jobs
fTestViewer.processChangesInUI();
- UI:
fCounterPanel
fProgressBar
setContentDescription / fInfoMessage
setTitleToolTip
view icons
statusLine
fFailureTrace

action enablement
 */
    if (this.session == session) {
      return null;
    }

    Session deactivatedSession = this.session;

    this.session = session;
    fTestViewer.registerActiveSession(session);

    toolBar.onChangedSession();
    if (fSashForm.isDisposed()) {
      postSyncProcessChanges();
      return deactivatedSession;
    }

    if (session == null) {
      setTitleToolTip(null);
      resetViewIcon();
      clearStatus();
      failureViewer.clear();

      registerInfoMessage(" "); //$NON-NLS-1$
      postSyncProcessChanges();

    } else {
      if (!this.session.isStarting() && !settings.isShowOnErrorOnly()) {
        showTestResultsView();
      }

      setTitleToolTip();

      clearStatus();
      failureViewer.clear();
      registerInfoMessage(BasicElementLabels.getElementName(this.session.getTestRunPresent()));

      postSyncProcessChanges();

      fTestViewer.expandFirstLevel();
      settings.setSortingCriterion(settings.getSortingCriterion());
      selectFirstFailure();
    }
    return deactivatedSession;
  }

  public ITestCaseElement[] getAllFailures() {
    return session.getAllFailedTestElements();
  }

  void handleTestSelected(ITestElement test) {
    showFailure(test);
    toolBar.updateActions();
    fCopyAction.handleTestSelected(test);
  }

  private void processChangesInUI() {
    if (fSashForm.isDisposed()) {
      return;
    }

    doShowInfoMessage();
    refreshCounters();

    if (!fPartIsVisible) {
      updateViewTitleProgress();
    } else {
      updateViewIcon();
    }

    toolBar.updateActions();
    fTestViewer.processChangesInUI();
  }

  private void selectFirstFailure() {
    fTestViewer.selectFirstFailure();
  }

  public boolean hasErrorsOrFailures() {
    return getErrorsPlusFailures() > 0;
  }

  private int getErrorsPlusFailures() {
    if (session == null) {
      return 0;
    } else {
      return session.getErrorCount() + session.getFailureCount();
    }
  }

  private void resetViewIcon() {
    fViewImage = fOriginalViewImage;
    firePropertyChange(IWorkbenchPart.PROP_TITLE);
  }

  private void updateViewIcon() {
    if (session == null) {
      fViewImage = fOriginalViewImage;
    } else if (session.getTestsuite().length == 0) {
      fViewImage = imageProvider.getInactiveLogo();
    } else if (session.getErrorCount() != 0) {
      fViewImage = imageProvider.getTestRunErrorIcon();
    } else if (session.getFailureCount() != 0) {
      fViewImage = imageProvider.getTestRunFailIcon();
    } else {
      fViewImage = imageProvider.getTestRunOKIcon();
    }
    firePropertyChange(IWorkbenchPart.PROP_TITLE);
  }

  private void updateViewTitleProgress() {
    if (session != null) {
      updateViewIcon();
    } else {
      resetViewIcon();
    }
  }

  private void setTitleToolTip() {
    var testKindDisplayStr = getTestKindDisplayName();
    var label = BasicElementLabels.getElementName(session.getName());

    if (testKindDisplayStr != null) {
      setTitleToolTip(MessageFormat.format(UIMessages.TestRunnerViewPart_titleToolTip, label, testKindDisplayStr));
    } else {
      setTitleToolTip(label);
    }
  }

  private void postSyncRunnable(Runnable r) {
    if (!isDisposed()) {
      getDisplay().syncExec(r);
    }
  }

  private void refreshCounters() {
    // TODO: Inefficient. Either
    // - keep a boolean fHasTestRun and update only on changes, or
    // - improve components to only redraw on changes (once!).

    int startedCount;
    int ignoredCount;
    int totalCount;
    int errorCount;
    int failureCount;
    int assumptionFailureCount;
    boolean hasErrorsOrFailures;
    boolean stopped;

    if (session != null) {
      startedCount = session.getStartedCount();
      ignoredCount = session.getIgnoredCount();
      totalCount = session.getTotalCount();
      errorCount = session.getErrorCount();
      failureCount = session.getFailureCount();
      assumptionFailureCount = session.getAssumptionFailureCount();
      hasErrorsOrFailures = errorCount + failureCount > 0;
      stopped = session.isStopped();
    } else {
      startedCount = 0;
      ignoredCount = 0;
      totalCount = 0;
      errorCount = 0;
      failureCount = 0;
      assumptionFailureCount = 0;
      hasErrorsOrFailures = false;
      stopped = false;
    }

    fCounterPanel.setTotal(totalCount);
    fCounterPanel.setRunValue(startedCount, ignoredCount, assumptionFailureCount);
    fCounterPanel.setErrorValue(errorCount);
    fCounterPanel.setFailureValue(failureCount);

    int ticksDone;
    if (startedCount == 0) {
      ticksDone = 0;
    } else {
      ticksDone = totalCount;
    }

    fProgressBar.reset(hasErrorsOrFailures, stopped, ticksDone, totalCount);
  }

  private void showTestResultsView() {
    IWorkbenchWindow window = getSite().getWorkbenchWindow();
    IWorkbenchPage page = window.getActivePage();
    TestRunnerViewPart testRunner;

    if (page != null) {
      try { // show the result view
        testRunner = (TestRunnerViewPart) page.findView(TestRunnerViewPart.NAME);
        if (testRunner == null) {
          IWorkbenchPart activePart = page.getActivePart();
          page.showView(TestRunnerViewPart.NAME, null, IWorkbenchPage.VIEW_VISIBLE);
          //restore focus
          page.activate(activePart);
        } else {
          page.bringToTop(testRunner);
        }
      } catch (PartInitException pie) {
        TestViewerPlugin.log().logError(pie);
      }
    }
  }

  private void doShowInfoMessage() {
    if (fInfoMessage != null) {
      setContentDescription(fInfoMessage);
      fInfoMessage = null;
    }
  }

  private SashForm createSashForm(Composite parent) {
    fSashForm = new SashForm(parent, SWT.VERTICAL);

    ViewForm top = new ViewForm(fSashForm, SWT.NONE);

    Composite empty = new Composite(top, SWT.NONE);
    empty.setLayout(new Layout() {
      @Override
      protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return new Point(1, 1); // (0, 0) does not work with super-intelligent ViewForm
      }

      @Override
      protected void layout(Composite composite, boolean flushCache) {
      }
    });
    top.setTopLeft(empty); // makes ViewForm draw the horizontal separator line ...
    fTestViewer = new TestViewer(top, this);
    top.setContent(fTestViewer.getTestViewerControl());

    ViewForm bottom = new ViewForm(fSashForm, SWT.NONE);
    failureViewer = new FailureViewer(this, bottom);

    fSashForm.setWeights(50, 50);
    return fSashForm;
  }

  private void clearStatus() {
    getStatusLine().setMessage(null);
    getStatusLine().setErrorMessage(null);
  }

  @Override
  public void setFocus() {
    if (fTestViewer != null) {
      fTestViewer.getTestViewerControl().setFocus();
    }
  }

  @Override
  public void createPartControl(Composite parent) {
    fParent = parent;
    addResizeListener(parent);

    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    parent.setLayout(gridLayout);

    viewHistoryManager = new RunnerViewHistory(this);
    toolBar.configureToolBar();

    fCounterComposite = createProgressCountPanel(parent);
    fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    SashForm sashForm = createSashForm(parent);
    sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

    IActionBars actionBars = getViewSite().getActionBars();

    fCopyAction = new CopyTraceAction();
    fCopyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
    actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);

    initPageSwitcher();

    fOriginalViewImage = getTitleImage();

    getViewSite().getPage().addPartListener(fPartListener);

    settings.setLayoutMode(LAYOUT_HIERARCHICAL);
    settings.setShowExecutionTime(true);
    settings.restoreLayoutState();
    toolBar.updateActions();

    // Apply ratio settings
    var value = settings.getRatio();
    fSashForm.setWeights(value, 1000 - value);

    fTestRunSessionListener = new SessionListener();
    TestViewerPlugin.core().getSessionsManager().addTestRunSessionListener(fTestRunSessionListener);

    // always show youngest test run in view. simulate "sessionAdded" event to do that
    var sessions = TestViewerPlugin.core().getSessionsManager().getSessions();
    if (!sessions.isEmpty()) {
      fTestRunSessionListener.sessionAdded(sessions.get(0));
    }
  }

  @Override
  public void saveState(IMemento memento) {
    if (fSashForm != null) {
      int[] weights = fSashForm.getWeights();
      settings.setRatio((weights[0] * 1000) / (weights[0] + weights[1]));
    }
    settings.saveState(memento);
  }

  /*
   * @see IWorkbenchPart#getTitleImage()
   */
  @Override
  public Image getTitleImage() {
    if (fOriginalViewImage == null) {
      fOriginalViewImage = super.getTitleImage();
    }

    if (fViewImage == null) {
      return super.getTitleImage();
    }
    return fViewImage;
  }

  private void initPageSwitcher() {
    @SuppressWarnings("unused") PageSwitcher pageSwitcher = new PageSwitcher(this) {
      @Override
      public Object[] getPages() {
        return viewHistoryManager.getHistoryEntries().toArray();
      }

      @Override
      public String getName(Object page) {
        return viewHistoryManager.getText((Session) page);
      }

      @Override
      public ImageDescriptor getImageDescriptor(Object page) {
        return viewHistoryManager.getImageDescriptor(page);
      }

      @Override
      public void activatePage(Object page) {
        viewHistoryManager.setActiveEntry((Session) page);
      }

      @Override
      public int getCurrentPageIndex() {
        return viewHistoryManager.getHistoryEntries().indexOf(viewHistoryManager.getCurrentEntry());
      }
    };
  }

  private void addResizeListener(Composite parent) {
    parent.addControlListener(new ControlListener() {
      @Override
      public void controlMoved(ControlEvent e) {
      }

      @Override
      public void controlResized(ControlEvent e) {
        computeOrientation();
      }
    });
  }

  private void computeOrientation() {
    if (settings.getOrientation() != VIEW_ORIENTATION_AUTOMATIC) {
      fCurrentOrientation = settings.getOrientation();
      setOrientation(fCurrentOrientation);
    } else {
      Point size = fParent.getSize();
      if (size.x != 0 && size.y != 0) {
        if (size.x > size.y) {
          setOrientation(VIEW_ORIENTATION_HORIZONTAL);
        } else {
          setOrientation(VIEW_ORIENTATION_VERTICAL);
        }
      }
    }
  }

  private IStatusLineManager getStatusLine() {
    // we want to show messages globally hence we
    // have to go through the active part
    IViewSite site = getViewSite();
    IWorkbenchPage page = site.getPage();
    IWorkbenchPart activePart = page.getActivePart();

    if (activePart instanceof IViewPart) {
      IViewPart activeViewPart = (IViewPart) activePart;
      IViewSite activeViewSite = activeViewPart.getViewSite();
      return activeViewSite.getActionBars().getStatusLineManager();
    }

    if (activePart instanceof IEditorPart) {
      IEditorPart activeEditorPart = (IEditorPart) activePart;
      IEditorActionBarContributor contributor = activeEditorPart.getEditorSite().getActionBarContributor();
      if (contributor instanceof EditorActionBarContributor) {
        return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
      }
    }
    // no active part
    return getViewSite().getActionBars().getStatusLineManager();
  }

  private Composite createProgressCountPanel(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    composite.setLayout(layout);
    setCounterColumns(layout);

    fCounterPanel = new CounterPanel(composite);
    fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    fProgressBar = new ProgressBar(composite);
    fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    return composite;
  }

  private void showFailure(final ITestElement test) {
    postSyncRunnable(() -> {
      if (!isDisposed()) failureViewer.viewFailure(test);
    });
  }

  private boolean isDisposed() {
    return fIsDisposed || fCounterPanel.isDisposed();
  }

  private Display getDisplay() {
    return getViewSite().getShell().getDisplay();
  }

  private void postSyncProcessChanges() {
    postSyncRunnable(this::processChangesInUI);
  }

  private void setOrientation(int orientation) {
    if ((fSashForm == null) || fSashForm.isDisposed()) {
      return;
    }
    boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
    fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
    fCurrentOrientation = orientation;
    GridLayout layout = (GridLayout) fCounterComposite.getLayout();
    setCounterColumns(layout);
    fParent.layout();
    toolBar.updateActions();
  }

  private void setCounterColumns(GridLayout layout) {
    if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL) {
      layout.numColumns = 2;
    } else {
      layout.numColumns = 1;
    }
  }

  @Override
  public void ontHtmlStackTraceChanged(boolean value) {
    failureViewer.setStacktraceViewer(value);
  }

  @Override
  public void onOrientationChanged(int value) {
    computeOrientation();
  }

  @Override
  public void onSortingCriterionChanged(SortingCriterion value) {
    if (session != null) {
      fTestViewer.setSortingCriterion(value);
    }
  }

  @Override
  public void onLayoutModeChanged(int value) {
    fTestViewer.setShowFailuresOrIgnoredOnly(settings.isFailuresOnly(), settings.isIgnoredOnly(), value);
  }

  @Override
  public void onShowExecutionTimeChanged(boolean value) {
    fTestViewer.setShowTime(value);
  }

  @Override
  public void onShowIgnoredOnlyChanged(boolean value) {
    fTestViewer.setShowFailuresOrIgnoredOnly(settings.isFailuresOnly(), value, settings.getLayoutMode());
  }

  @Override
  public void onShowFailuresOnly(boolean value) {
    fTestViewer.setShowFailuresOrIgnoredOnly(value, settings.isIgnoredOnly(), settings.getLayoutMode());
  }

  public enum SortingCriterion {
    SORT_BY_NAME,
    SORT_BY_EXECUTION_ORDER,
    SORT_BY_EXECUTION_TIME
  }

  private class SessionListener implements ISessionListener {
    @Override
    public void sessionAdded(final Session session) {
      getDisplay().asyncExec(() -> {
        if (UIPreferencesConstants.getShowInAllViews() || getSite().getWorkbenchWindow() == TestViewerPlugin.ui().getActiveWorkbenchWindow()) {
          if (fInfoMessage == null) {
            String testRunLabel = BasicElementLabels.getElementName(session.getName());
            String msg;
            if (session.getLaunch() != null) {
              msg = MessageFormat.format(UIMessages.TestRunnerViewPart_Launching, testRunLabel);
            } else {
              msg = testRunLabel;
            }
            registerInfoMessage(msg);
          }

          Session deactivatedSession = setActiveSession(session);
          if (deactivatedSession != null) {
            deactivatedSession.swapOut();
          }
        }
      });
    }

    @Override
    public void sessionRemoved(final Session session) {
      getDisplay().asyncExec(() -> {
        if (session.equals(TestRunnerViewPart.this.session)) {
          var sessions = TestViewerPlugin.core().getSessionsManager().getSessions();
          Session deactivatedSession;
          if (!sessions.isEmpty()) {
            deactivatedSession = setActiveSession(sessions.get(0));
          } else {
            deactivatedSession = setActiveSession(null);
          }
          if (deactivatedSession != null) {
            deactivatedSession.swapOut();
          }
        }
      });
    }
  }

}
