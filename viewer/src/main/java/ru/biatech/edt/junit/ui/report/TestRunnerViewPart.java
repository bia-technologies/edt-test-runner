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
import lombok.Setter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.PageSwitcher;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.JUnitPreferencesConstants;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.kinds.ITestKind;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestRunSessionListener;
import ru.biatech.edt.junit.model.ITestSessionListener;
import ru.biatech.edt.junit.model.JUnitModel;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestElement;
import ru.biatech.edt.junit.model.TestRunSession;
import ru.biatech.edt.junit.model.TestStatus;
import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
import ru.biatech.edt.junit.ui.JUnitMessages;
import ru.biatech.edt.junit.ui.JUnitUIPreferencesConstants;
import ru.biatech.edt.junit.ui.report.actions.ActivateOnErrorAction;
import ru.biatech.edt.junit.ui.report.actions.FailuresOnlyFilterAction;
import ru.biatech.edt.junit.ui.report.actions.IgnoredOnlyFilterAction;
import ru.biatech.edt.junit.ui.report.actions.RerunLastAction;
import ru.biatech.edt.junit.ui.report.actions.RerunLastFailedFirstAction;
import ru.biatech.edt.junit.ui.report.actions.ScrollLockAction;
import ru.biatech.edt.junit.ui.report.actions.ShowNextFailureAction;
import ru.biatech.edt.junit.ui.report.actions.ShowPreviousFailureAction;
import ru.biatech.edt.junit.ui.report.actions.ShowTestHierarchyAction;
import ru.biatech.edt.junit.ui.report.actions.ShowTimeAction;
import ru.biatech.edt.junit.ui.report.actions.ShowWebStackTraceAction;
import ru.biatech.edt.junit.ui.report.actions.ToggleOrientationAction;
import ru.biatech.edt.junit.ui.report.actions.ToggleSortingAction;
import ru.biatech.edt.junit.ui.report.history.RunnerViewHistory;
import ru.biatech.edt.junit.ui.stacktrace.FailureViewer;
import ru.biatech.edt.junit.ui.stacktrace.actions.CopyTraceAction;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * A ViewPart that shows the results of a test run.
 */
public class TestRunnerViewPart extends ViewPart {

  public static final String NAME = "ru.biatech.edt.junit.ResultView"; //$NON-NLS-1$
  public static final int LAYOUT_FLAT = 0;
  public static final int LAYOUT_HIERARCHICAL = 1;
  public static final Object FAMILY_JUNIT_RUN = new Object();
  private static final int REFRESH_INTERVAL = 200;
  //orientations
  public static final int VIEW_ORIENTATION_VERTICAL = 0;
  public static final int VIEW_ORIENTATION_HORIZONTAL = 1;
  public static final int VIEW_ORIENTATION_AUTOMATIC = 2;
  public static final String RERUN_LAST_COMMAND = "ru.biatech.edt.junit.junitShortcut.rerunLast"; //$NON-NLS-1$
  public static final String RERUN_FAILED_FIRST_COMMAND = "ru.biatech.edt.junit.junitShortcut.rerunFailedFirst"; //$NON-NLS-1$

  /**
   * @since 3.5
   */
  public static final String PREF_LAST_PATH = "lastImportExportPath"; //$NON-NLS-1$

  private FailureViewer failureViewer;
  private final ImageProvider imageProvider;
  private ProgressBar fProgressBar;
  private ProgressImages fProgressImages;
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
  private Action fNextAction;
  private Action fPreviousAction;
  private StopAction fStopAction;
  private CopyTraceAction fCopyAction;
  private Action fRerunLastTestAction;
  private IHandlerActivation fRerunLastActivation;
  private Action fRerunFailedFirstAction;
  private IHandlerActivation fRerunFailedFirstActivation;
  private FailuresOnlyFilterAction fFailuresOnlyFilterAction;
  private IgnoredOnlyFilterAction fIgnoredOnlyFilterAction;
  private ScrollLockAction fScrollLockAction;
  private ToggleOrientationAction[] fToggleOrientationActions;
  private ShowTestHierarchyAction fShowTestHierarchyAction;
  private ShowTimeAction fShowTimeAction;
  private ShowWebStackTraceAction showWebStackTraceAction;
  private ActivateOnErrorAction fActivateOnErrorAction;
  private ToggleSortingAction[] fToggleSortingActions;
  private IMenuListener fViewMenuListener;
  private TestRunSession fTestRunSession;
  private TestSessionListener fTestSessionListener;
  private RunnerViewHistory fViewHistory;
  private TestRunSessionListener fTestRunSessionListener;
  private IMemento fMemento;
  private SashForm fSashForm;
  private Composite fCounterComposite;
  private Composite fParent;
  /**
   * A Job that periodically updates view description, counters, and progress bar.
   */
  private UpdateUIJob fUpdateJob;
  /**
   * A Job that runs as long as a test run is running.
   * It is used to show busyness for running jobs in the view (title in italics).
   */
  private JUnitIsRunningJob fJUnitIsRunningJob;
  private ILock fJUnitIsRunningLock;
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

  @Getter
  final ReportSettings settings;

  private static boolean getShowOnErrorOnly() {
    return Platform.getPreferencesService().getBoolean(TestViewerPlugin.getPluginId(), JUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, false, null);
  }

  private static void importTestRunSession(final String url) {
    try {
      PlatformUI.getWorkbench().getProgressService().busyCursorWhile(monitor -> JUnitModel.importTestRunSession(url, null, monitor));
    } catch (InterruptedException e) {
      // cancelled
    } catch (InvocationTargetException e) {
      CoreException ce = (CoreException) e.getCause();
      StatusManager.getManager().handle(ce.getStatus(), StatusManager.SHOW | StatusManager.LOG);
    }
  }

  public TestRunnerViewPart() {
    imageProvider = new ImageProvider();
    settings = new ReportSettings();
  }

  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);
    fMemento = memento;
    IWorkbenchSiteProgressService progressService = getProgressService();
    if (progressService != null) {
      progressService.showBusyForFamily(TestRunnerViewPart.FAMILY_JUNIT_RUN);
    }
  }

  private IWorkbenchSiteProgressService getProgressService() {
    return getSite().getAdapter(IWorkbenchSiteProgressService.class);
  }

  /**
   * Stops the currently running test and shuts down the RemoteTestRunner
   */
  public void stopTest() {
    if (fTestRunSession != null) {
      if (fTestRunSession.isRunning()) {
        setContentDescription(JUnitMessages.TestRunnerViewPart_message_stopping);
      }
      fTestRunSession.stopTestRun();
    }
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
    ITestKind kind = fTestRunSession.getTestRunnerKind();
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
      TestViewerPlugin.core().getModel().removeTestRunSessionListener(fTestRunSessionListener);
    }

    IHandlerService handlerService = getSite().getWorkbenchWindow().getService(IHandlerService.class);
    handlerService.deactivateHandler(fRerunLastActivation);
    handlerService.deactivateHandler(fRerunFailedFirstActivation);
    setActiveTestRunSession(null);

    if (fProgressImages != null) {
      fProgressImages.dispose();
    }
    getViewSite().getPage().removePartListener(fPartListener);

    imageProvider.dispose();
    if (fViewMenuListener != null) {
      getViewSite().getActionBars().getMenuManager().removeMenuListener(fViewMenuListener);
    }
    if (failureViewer != null) {
      failureViewer.dispose();
    }
  }

  public IV8Project getLaunchedProject() {
    return fTestRunSession == null ? null : fTestRunSession.getLaunchedProject();
  }

  public ImageProvider getImageProvider() {
    return imageProvider;
  }

  public boolean lastLaunchIsKeptAlive() {
    return fTestRunSession != null && fTestRunSession.isKeptAlive();
  }

  public Shell getShell() {
    return fParent.getShell();
  }

  public TestRunSession getTestRunSession() {
    return fTestRunSession;
  }

  /**
   * @param testRunSession new active test run session
   * @return deactivated session, or <code>null</code> iff no session got deactivated
   */
  public TestRunSession setActiveTestRunSession(TestRunSession testRunSession) {
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
    if (fTestRunSession == testRunSession) {
      return null;
    }

    deregisterTestSessionListener(true);

    TestRunSession deactivatedSession = fTestRunSession;

    fTestRunSession = testRunSession;
    fTestViewer.registerActiveSession(testRunSession);

    if (fSashForm.isDisposed()) {
      stopUpdateJobs();
      return deactivatedSession;
    }

    if (testRunSession == null) {
      setTitleToolTip(null);
      resetViewIcon();
      clearStatus();
      failureViewer.clear();

      registerInfoMessage(" "); //$NON-NLS-1$
      stopUpdateJobs();

      fStopAction.setEnabled(false);
      fRerunFailedFirstAction.setEnabled(false);
      fRerunLastTestAction.setEnabled(false);

    } else {
      if (fTestRunSession.isStarting() || fTestRunSession.isRunning() || fTestRunSession.isKeptAlive()) {
        fTestSessionListener = new TestSessionListener();
        fTestRunSession.addTestSessionListener(fTestSessionListener);
      }
      if (!fTestRunSession.isStarting() && !settings.isShowOnErrorOnly()) {
        showTestResultsView();
      }

      setTitleToolTip();

      clearStatus();
      failureViewer.clear();
      registerInfoMessage(BasicElementLabels.getJavaElementName(fTestRunSession.getTestRunPresent()));

      updateRerunFailedFirstAction();
      fRerunLastTestAction.setEnabled(fTestRunSession.getLaunch() != null);

      if (fTestRunSession.isRunning()) {
        startUpdateJobs();

        fStopAction.setEnabled(true);
        fTestViewer.setSortingCriterion(SortingCriterion.SORT_BY_EXECUTION_ORDER);

      } else /* old or fresh session: don't want jobs at this stage */ {
        stopUpdateJobs();

        fStopAction.setEnabled(fTestRunSession.isKeptAlive());
        fTestViewer.expandFirstLevel();
        settings.setSortingCriterion(settings.getSortingCriterion());
      }
    }
    return deactivatedSession;
  }

  public ITestCaseElement[] getAllFailures() {
    return fTestRunSession.getAllFailedTestElements();
  }

  void handleTestSelected(TestElement test) {
    showFailure(test);
    fCopyAction.handleTestSelected(test);
  }

  private void startUpdateJobs() {
    postSyncProcessChanges();

    if (fUpdateJob != null) {
      return;
    }
    fJUnitIsRunningJob = new JUnitIsRunningJob(JUnitMessages.TestRunnerViewPart_wrapperJobName);
    fJUnitIsRunningLock = Job.getJobManager().newLock();
    // acquire lock while a test run is running
    // the lock is released when the test run terminates
    // the wrapper job will wait on this lock.
    fJUnitIsRunningLock.acquire();
    getProgressService().schedule(fJUnitIsRunningJob);

    fUpdateJob = new UpdateUIJob(JUnitMessages.TestRunnerViewPart_jobName);
    fUpdateJob.schedule(REFRESH_INTERVAL);
  }

  private void stopUpdateJobs() {
    if (fUpdateJob != null) {
      fUpdateJob.stop();
      fUpdateJob = null;
    }
    if (fJUnitIsRunningJob != null && fJUnitIsRunningLock != null) {
      fJUnitIsRunningLock.release();
      fJUnitIsRunningJob = null;
    }
    postSyncProcessChanges();
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
    updateNextPreviousActions();

    fTestViewer.processChangesInUI();
  }

  private void updateNextPreviousActions() {
    boolean hasErrorsOrFailures = !settings.isShowIgnoredOnly() && hasErrorsOrFailures();
    fNextAction.setEnabled(hasErrorsOrFailures);
    fPreviousAction.setEnabled(hasErrorsOrFailures);
  }

  private void selectFirstFailure() {
    fTestViewer.selectFirstFailure();
  }

  private boolean hasErrorsOrFailures() {
    return getErrorsPlusFailures() > 0;
  }

  private int getErrorsPlusFailures() {
    if (fTestRunSession == null) {
      return 0;
    } else {
      return fTestRunSession.getErrorCount() + fTestRunSession.getFailureCount();
    }
  }

  private String elapsedTimeAsString(long runTime) {
    return NumberFormat.getInstance().format((double) runTime / 1000);
  }

  private void handleStopped() {
    postSyncRunnable(() -> {
      if (isDisposed()) {
        return;
      }
      resetViewIcon();
      fStopAction.setEnabled(false);
      updateRerunFailedFirstAction();
    });
    stopUpdateJobs();
    logMessageIfNoTests();
  }

  private void logMessageIfNoTests() {
    if (fTestRunSession != null && fTestRunSession.getTotalCount() == 0) {
      String msg = MessageFormat.format(JUnitMessages.TestRunnerViewPart_error_notests_kind, fTestRunSession.getTestRunnerKind().getDisplayName());
      Platform.getLog(getClass()).error(msg);
    }
  }

  private void resetViewIcon() {
    fViewImage = fOriginalViewImage;
    firePropertyChange(IWorkbenchPart.PROP_TITLE);
  }

  private void updateViewIcon() {
    if (fTestRunSession == null || fTestRunSession.isStopped() || fTestRunSession.isRunning() || fTestRunSession.getStartedCount() == 0) {
      fViewImage = fOriginalViewImage;
    } else if (hasErrorsOrFailures()) {
      fViewImage = imageProvider.getTestRunFailIcon();
    } else {
      fViewImage = imageProvider.getTestRunOKIcon();
    }
    firePropertyChange(IWorkbenchPart.PROP_TITLE);
  }

  private void updateViewTitleProgress() {
    if (fTestRunSession != null) {
      if (fTestRunSession.isRunning()) {
        Image progress = fProgressImages.getImage(fTestRunSession.getStartedCount(),
            fTestRunSession.getTotalCount(),
            fTestRunSession.getErrorCount(),
            fTestRunSession.getFailureCount());
        if (progress != fViewImage) {
          fViewImage = progress;
          firePropertyChange(IWorkbenchPart.PROP_TITLE);
        }
      } else {
        updateViewIcon();
      }
    } else {
      resetViewIcon();
    }
  }

  private void deregisterTestSessionListener(boolean force) {
    if (fTestRunSession != null && fTestSessionListener != null && (force || !fTestRunSession.isKeptAlive())) {
      fTestRunSession.removeTestSessionListener(fTestSessionListener);
      fTestSessionListener = null;
    }
  }

  private void updateRerunFailedFirstAction() {
    boolean state = hasErrorsOrFailures() && fTestRunSession.getLaunch() != null;
    fRerunFailedFirstAction.setEnabled(state);
  }

  private void setTitleToolTip() {
    String testKindDisplayStr = getTestKindDisplayName();

    String testRunLabel = BasicElementLabels.getJavaElementName(fTestRunSession.getTestRunName());
    if (testKindDisplayStr != null) {
      setTitleToolTip(MessageFormat.format(JUnitMessages.TestRunnerViewPart_titleToolTip, testRunLabel, testKindDisplayStr));
    } else {
      setTitleToolTip(testRunLabel);
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

    if (fTestRunSession != null) {
      startedCount = fTestRunSession.getStartedCount();
      ignoredCount = fTestRunSession.getIgnoredCount();
      totalCount = fTestRunSession.getTotalCount();
      errorCount = fTestRunSession.getErrorCount();
      failureCount = fTestRunSession.getFailureCount();
      assumptionFailureCount = fTestRunSession.getAssumptionFailureCount();
      hasErrorsOrFailures = errorCount + failureCount > 0;
      stopped = fTestRunSession.isStopped();
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
    } else if (startedCount == totalCount && !fTestRunSession.isRunning()) {
      ticksDone = totalCount;
    } else {
      ticksDone = startedCount - 1;
    }

    fProgressBar.reset(hasErrorsOrFailures, stopped, ticksDone, totalCount);
  }

  private void postShowTestResultsView() {
    postSyncRunnable(() -> {
      if (isDisposed()) {
        return;
      }
      showTestResultsView();
    });
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

    fViewHistory = new RunnerViewHistory(this);
    configureToolBar();

    fCounterComposite = createProgressCountPanel(parent);
    fCounterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    SashForm sashForm = createSashForm(parent);
    sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

    IActionBars actionBars = getViewSite().getActionBars();

    fCopyAction = new CopyTraceAction();
    fCopyAction.setActionDefinitionId(ActionFactory.COPY.getCommandId());
    actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fCopyAction);

    initPageSwitcher();
    addDropAdapter(parent);

    fOriginalViewImage = getTitleImage();
    fProgressImages = new ProgressImages();
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IJUnitHelpContextIds.RESULTS_VIEW);

    getViewSite().getPage().addPartListener(fPartListener);

    settings.setLayoutMode(LAYOUT_HIERARCHICAL);
    settings.setShowExecutionTime(true);
    if (fMemento != null) {
      settings.restoreLayoutState(fMemento);
    }
    fMemento = null;

    fTestRunSessionListener = new TestRunSessionListener();
    TestViewerPlugin.core().getModel().addTestRunSessionListener(fTestRunSessionListener);

    // always show youngest test run in view. simulate "sessionAdded" event to do that
    List<TestRunSession> testRunSessions = TestViewerPlugin.core().getModel().getTestRunSessions();
    if (!testRunSessions.isEmpty()) {
      fTestRunSessionListener.sessionAdded(testRunSessions.get(0));
    }
  }

  @Override
  public void saveState(IMemento memento) {
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

  private void addDropAdapter(Composite parent) {
    DropTarget dropTarget = new DropTarget(parent, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK | DND.DROP_DEFAULT);
    dropTarget.setTransfer(TextTransfer.getInstance());
    class DropAdapter extends DropTargetAdapter {
      @Override
      public void dragEnter(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
      }

      @Override
      public void dragOver(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
      }

      @Override
      public void dragOperationChanged(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
        event.feedback = DND.FEEDBACK_NONE;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          String url = (String) event.data;
          importTestRunSession(url);
        }
      }
    }
    dropTarget.addDropListener(new DropAdapter());
  }

  private void initPageSwitcher() {
    @SuppressWarnings("unused") PageSwitcher pageSwitcher = new PageSwitcher(this) {
      @Override
      public Object[] getPages() {
        return fViewHistory.getHistoryEntries().toArray();
      }

      @Override
      public String getName(Object page) {
        return fViewHistory.getText((TestRunSession) page);
      }

      @Override
      public ImageDescriptor getImageDescriptor(Object page) {
        return fViewHistory.getImageDescriptor(page);
      }

      @Override
      public void activatePage(Object page) {
        fViewHistory.setActiveEntry((TestRunSession) page);
      }

      @Override
      public int getCurrentPageIndex() {
        return fViewHistory.getHistoryEntries().indexOf(fViewHistory.getCurrentEntry());
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

  private void configureToolBar() {
    IActionBars actionBars = getViewSite().getActionBars();
    IToolBarManager toolBar = actionBars.getToolBarManager();
    IMenuManager viewMenu = actionBars.getMenuManager();

    fNextAction = new ShowNextFailureAction(this);
    fNextAction.setEnabled(false);
    actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAction);

    fPreviousAction = new ShowPreviousFailureAction(this);
    fPreviousAction.setEnabled(false);
    actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAction);

    fStopAction = new StopAction();
    fStopAction.setEnabled(false);

    fRerunLastTestAction = new RerunLastAction(this);
    IHandlerService handlerService = getSite().getWorkbenchWindow().getService(IHandlerService.class);
    IHandler handler = new AbstractHandler() {
      @Override
      public Object execute(ExecutionEvent event) {
        fRerunLastTestAction.run();
        return null;
      }

      @Override
      public boolean isEnabled() {
        return fRerunLastTestAction.isEnabled();
      }
    };
    fRerunLastActivation = handlerService.activateHandler(RERUN_LAST_COMMAND, handler);

    fRerunFailedFirstAction = new RerunLastFailedFirstAction(this);
    handler = new AbstractHandler() {
      @Override
      public Object execute(ExecutionEvent event) {
        fRerunFailedFirstAction.run();
        return null;
      }

      @Override
      public boolean isEnabled() {
        return fRerunFailedFirstAction.isEnabled();
      }
    };
    fRerunFailedFirstActivation = handlerService.activateHandler(RERUN_FAILED_FIRST_COMMAND, handler);

    toolBar.add(fNextAction);
    toolBar.add(fPreviousAction);
    toolBar.add(fFailuresOnlyFilterAction = new FailuresOnlyFilterAction(settings));
    toolBar.add(fIgnoredOnlyFilterAction = new IgnoredOnlyFilterAction(settings));
    toolBar.add(fScrollLockAction = new ScrollLockAction(settings));
    toolBar.add(new Separator());
    toolBar.add(fRerunLastTestAction);
    toolBar.add(fRerunFailedFirstAction);
    toolBar.add(fStopAction);
    toolBar.add(fViewHistory.createHistoryDropDownAction());


    viewMenu.add(fShowTestHierarchyAction = new ShowTestHierarchyAction(settings));
    viewMenu.add(fShowTimeAction = new ShowTimeAction(settings));
    viewMenu.add(new Separator());

    fToggleSortingActions = new ToggleSortingAction[]{
        new ToggleSortingAction(settings, SortingCriterion.SORT_BY_EXECUTION_ORDER),
        new ToggleSortingAction(settings, SortingCriterion.SORT_BY_EXECUTION_TIME),
        new ToggleSortingAction(settings, SortingCriterion.SORT_BY_NAME)};
    MenuManager fSortByMenu = new MenuManager(JUnitMessages.TestRunnerViewPart_sort_by_menu);
    for (ToggleSortingAction fToggleSortingAction : fToggleSortingActions) {
      fSortByMenu.add(fToggleSortingAction);
    }
    viewMenu.add(fSortByMenu);
    viewMenu.add(new Separator());

    fToggleOrientationActions = new ToggleOrientationAction[]{
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_VERTICAL),
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_HORIZONTAL),
        new ToggleOrientationAction(settings, VIEW_ORIENTATION_AUTOMATIC)};

    MenuManager layoutSubMenu = new MenuManager(JUnitMessages.TestRunnerViewPart_layout_menu);
    for (ToggleOrientationAction toggleOrientationAction : fToggleOrientationActions) {
      layoutSubMenu.add(toggleOrientationAction);
    }
    viewMenu.add(layoutSubMenu);
    viewMenu.add(new Separator());

    viewMenu.add(fFailuresOnlyFilterAction);
    viewMenu.add(fIgnoredOnlyFilterAction);
    viewMenu.add(fActivateOnErrorAction = new ActivateOnErrorAction(settings));
    viewMenu.add(showWebStackTraceAction = new ShowWebStackTraceAction(settings));
    fViewMenuListener = manager -> fActivateOnErrorAction.update();

    viewMenu.addMenuListener(fViewMenuListener);
    actionBars.updateActionBars();
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

  private void showFailure(final TestElement test) {
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

  private void warnOfContentChange() {
    IWorkbenchSiteProgressService service = getProgressService();
    if (service != null) {
      service.warnOfContentChange();
    }
  }

  private void setOrientation(int orientation) {
    if ((fSashForm == null) || fSashForm.isDisposed()) {
      return;
    }
    boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
    fSashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
    for (ToggleOrientationAction toggleOrientationAction : fToggleOrientationActions) {
      toggleOrientationAction.update();
    }
    fCurrentOrientation = orientation;
    GridLayout layout = (GridLayout) fCounterComposite.getLayout();
    setCounterColumns(layout);
    fParent.layout();
  }

  private void setCounterColumns(GridLayout layout) {
    if (fCurrentOrientation == VIEW_ORIENTATION_HORIZONTAL) {
      layout.numColumns = 2;
    } else {
      layout.numColumns = 1;
    }
  }

  public enum SortingCriterion {
    SORT_BY_NAME,
    SORT_BY_EXECUTION_ORDER,
    SORT_BY_EXECUTION_TIME
  }

  private class TestRunSessionListener implements ITestRunSessionListener {
    @Override
    public void sessionAdded(final TestRunSession testRunSession) {
      getDisplay().asyncExec(() -> {
        if (JUnitUIPreferencesConstants.getShowInAllViews() || getSite().getWorkbenchWindow() == TestViewerPlugin.ui().getActiveWorkbenchWindow()) {
          if (fInfoMessage == null) {
            String testRunLabel = BasicElementLabels.getJavaElementName(testRunSession.getTestRunName());
            String msg;
            if (testRunSession.getLaunch() != null) {
              msg = MessageFormat.format(JUnitMessages.TestRunnerViewPart_Launching, testRunLabel);
            } else {
              msg = testRunLabel;
            }
            registerInfoMessage(msg);
          }

          TestRunSession deactivatedSession = setActiveTestRunSession(testRunSession);
          if (deactivatedSession != null) {
            deactivatedSession.swapOut();
          }
        }
      });
    }

    @Override
    public void sessionRemoved(final TestRunSession testRunSession) {
      getDisplay().asyncExec(() -> {
        if (testRunSession.equals(fTestRunSession)) {
          List<TestRunSession> testRunSessions = TestViewerPlugin.core().getModel().getTestRunSessions();
          TestRunSession deactivatedSession;
          if (!testRunSessions.isEmpty()) {
            deactivatedSession = setActiveTestRunSession(testRunSessions.get(0));
          } else {
            deactivatedSession = setActiveTestRunSession(null);
          }
          if (deactivatedSession != null) {
            deactivatedSession.swapOut();
          }
        }
      });
    }
  }

  private class TestSessionListener implements ITestSessionListener {

    @Override
    public void sessionStarted() {
      fTestViewer.registerViewersRefresh();
      settings.setShowOnErrorOnly(getShowOnErrorOnly());

      startUpdateJobs();

      fStopAction.setEnabled(true);
      fRerunLastTestAction.setEnabled(true);

      // While tests are running, always use the execution order
      getDisplay().asyncExec(() -> fTestViewer.setSortingCriterion(SortingCriterion.SORT_BY_EXECUTION_ORDER));
    }

    @Override
    public void sessionEnded(long elapsedTime) {
      deregisterTestSessionListener(false);

      fTestViewer.registerAutoScrollTarget(null);

      String msg = MessageFormat.format(JUnitMessages.TestRunnerViewPart_message_finish, elapsedTimeAsString(elapsedTime));
      registerInfoMessage(msg);

      postSyncRunnable(() -> {
        if (isDisposed()) {
          return;
        }
        fStopAction.setEnabled(lastLaunchIsKeptAlive());
        updateRerunFailedFirstAction();
        processChangesInUI();
        if (hasErrorsOrFailures()) {
          selectFirstFailure();
        }
        warnOfContentChange();
      });
      stopUpdateJobs();
      logMessageIfNoTests();

      // When test session ended, apply user sorting criterion
      getDisplay().asyncExec(() -> settings.setSortingCriterion(settings.getSortingCriterion()));
    }

    @Override
    public void sessionStopped(final long elapsedTime) {
      deregisterTestSessionListener(false);

      fTestViewer.registerAutoScrollTarget(null);

      registerInfoMessage(JUnitMessages.TestRunnerViewPart_message_stopped);
      handleStopped();
    }

    @Override
    public void sessionTerminated() {
      deregisterTestSessionListener(true);

      fTestViewer.registerAutoScrollTarget(null);

      registerInfoMessage(JUnitMessages.TestRunnerViewPart_message_terminated);
      handleStopped();
    }

    @Override
    public void runningBegins() {
      if (!settings.isShowOnErrorOnly()) {
        postShowTestResultsView();
      }
    }

    @Override
    public void testStarted(TestCaseElement testCaseElement) {
      fTestViewer.registerAutoScrollTarget(testCaseElement);
      fTestViewer.registerViewerUpdate(testCaseElement);

      String className = BasicElementLabels.getJavaElementName(testCaseElement.getClassName());
      String method = BasicElementLabels.getJavaElementName(testCaseElement.getTestMethodName());
      String status = MessageFormat.format(JUnitMessages.TestRunnerViewPart_message_started, className, method);
      registerInfoMessage(status);
    }

    @Override
    public void testFailed(TestElement testElement, TestStatus status, String trace, String expected, String actual) {
      if (settings.isAutoScroll()) {
        fTestViewer.registerFailedForAutoScroll(testElement);
      }
      fTestViewer.registerViewerUpdate(testElement);

      // show the view on the first error only
      if (settings.isShowOnErrorOnly() && (getErrorsPlusFailures() == 1)) {
        postShowTestResultsView();
      }

      //TODO:
      // [Bug 35590] JUnit window doesn't report errors from junit.extensions.TestSetup [JUnit]
      // when a failure occurs in test setup then no test is running
      // to update the views we artificially signal the end of a test run
//		    if (!fTestIsRunning) {
//				fTestIsRunning= false;
//				testEnded(testCaseElement);
//			}
    }

    @Override
    public void testEnded(TestCaseElement testCaseElement) {
      fTestViewer.registerViewerUpdate(testCaseElement);
    }

    @Override
    public void testRerun(TestCaseElement testCaseElement, TestStatus status, String trace, String expectedResult, String actualResult) {
      fTestViewer.registerViewerUpdate(testCaseElement); //TODO: autoExpand?
      postSyncProcessChanges();
      showFailure(testCaseElement);
    }

    @Override
    public void testAdded(TestElement testElement) {
      fTestViewer.registerTestAdded(testElement);
    }

    @Override
    public boolean acceptsSwapToDisk() {
      return false;
    }
  }

  private class UpdateUIJob extends UIJob {
    private boolean fRunning = true;

    public UpdateUIJob(String name) {
      super(name);
      setSystem(true);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      if (!isDisposed()) {
        processChangesInUI();
      }
      schedule(REFRESH_INTERVAL);
      return Status.OK_STATUS;
    }

    public void stop() {
      fRunning = false;
    }

    @Override
    public boolean shouldSchedule() {
      return fRunning;
    }
  }

  private class JUnitIsRunningJob extends Job {
    public JUnitIsRunningJob(String name) {
      super(name);
      setSystem(true);
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
      // wait until the test run terminates
      fJUnitIsRunningLock.acquire();
      return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object family) {
      return family == TestRunnerViewPart.FAMILY_JUNIT_RUN;
    }
  }

  private class StopAction extends Action {
    public StopAction() {
      setText(JUnitMessages.TestRunnerViewPart_stopaction_text);
      setToolTipText(JUnitMessages.TestRunnerViewPart_stopaction_tooltip);
      TestViewerPlugin.ui().setLocalImageDescriptors(this, "stop.png"); //$NON-NLS-1$
    }

    @Override
    public void run() {
      stopTest();
      setEnabled(false);
    }
  }

  public class ReportSettings {
    // TODO В будущем сделать рефакторинг вынеся в самостоятельный класс
    private static final String TAG_RATIO = "ratio"; //$NON-NLS-1$
    private static final String TAG_ORIENTATION = "orientation"; //$NON-NLS-1$
    private static final String TAG_SCROLL = "scroll"; //$NON-NLS-1$
    private static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$
    private static final String TAG_FAILURES_ONLY = "failuresOnly"; //$NON-NLS-1$
    private static final String TAG_IGNORED_ONLY = "ignoredOnly"; //$NON-NLS-1$
    private static final String TAG_SHOW_TIME = "time"; //$NON-NLS-1$
    private static final String TAG_SORTING_CRITERION = "sortingCriterion"; //$NON-NLS-1$
    private static final String TAG_WEB_STACKTRACE = "webStackTrace"; //$NON-NLS-1$

    /**
     * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
     * <code>VIEW_ORIENTATION_VERTICAL</code>, or <code>VIEW_ORIENTATION_AUTOMATIC</code>.
     */
    @Getter
    private int orientation = VIEW_ORIENTATION_AUTOMATIC;

    /**
     * The current layout mode (LAYOUT_FLAT or LAYOUT_HIERARCHICAL).
     */
    @Getter
    private int layoutMode = LAYOUT_HIERARCHICAL;

    /**
     * Whether the output scrolls and reveals tests as they are executed.
     */
    @Getter
    @Setter
    boolean autoScroll;

    @Getter
    @Setter
    boolean showOnErrorOnly;

    /**
     * The current sorting criterion.
     */
    @Getter
    private SortingCriterion sortingCriterion = SortingCriterion.SORT_BY_EXECUTION_ORDER;

    public boolean isShowFailuresOnly() {
      return fFailuresOnlyFilterAction != null && fFailuresOnlyFilterAction.isChecked();
    }

    public void setShowFailuresOnly(boolean failuresOnly) {
      updateFilterAndLayout(failuresOnly, false /*ignoredOnly must be off*/, getLayoutMode());
    }

    public boolean isShowIgnoredOnly() {
      return fIgnoredOnlyFilterAction != null && fIgnoredOnlyFilterAction.isChecked();
    }

    public void setShowIgnoredOnly(boolean ignoredOnly) {
      updateFilterAndLayout(false, isShowIgnoredOnly(), getLayoutMode());
    }

    public boolean isScrollLock() {
      return fScrollLockAction != null && fScrollLockAction.isChecked();
    }

    public boolean isShowExecutionTime() {
      return fShowTimeAction != null && fShowTimeAction.isChecked();
    }

    public boolean isHtmlStackTrace() {
      return showWebStackTraceAction != null && showWebStackTraceAction.isChecked();
    }

    public void setHtmlStackTrace(boolean value) {
      showWebStackTraceAction.setChecked(value);
      failureViewer.setStacktraceViewer(value);
    }

    public void setShowExecutionTime(boolean showTime) {
      fTestViewer.setShowTime(showTime);
      fShowTimeAction.setChecked(showTime);
    }

    public int getRatio() {
      int[] weights = fSashForm.getWeights();
      return (weights[0] * 1000) / (weights[0] + weights[1]);
    }

    public void setRation(Integer ratio) {
      if (ratio != null) {
        fSashForm.setWeights(ratio, 1000 - ratio);
      }
    }

    public void setLayoutMode(int mode) {
      updateFilterAndLayout(isShowFailuresOnly(), isShowIgnoredOnly(), mode);
    }

    public void setOrientation(int orientation) {
      this.orientation = orientation;
      computeOrientation();
    }

    public void setSortingCriterion(SortingCriterion sortingCriterion) {
      this.sortingCriterion = sortingCriterion;
      if (fTestRunSession != null && !fTestRunSession.isStarting() && !fTestRunSession.isRunning()) {
        fTestViewer.setSortingCriterion(this.sortingCriterion);
      }
    }

    public void saveState(IMemento memento) {
      if (fSashForm == null) {
        // part has not been created
        if (fMemento != null) //Keep the old state;
          memento.putMemento(fMemento);
        return;
      }

      memento.putBoolean(TAG_SCROLL, isScrollLock());
      memento.putInteger(TAG_RATIO, getRatio());
      memento.putInteger(TAG_ORIENTATION, getOrientation());

      memento.putBoolean(TAG_FAILURES_ONLY, isShowFailuresOnly());
      memento.putBoolean(TAG_IGNORED_ONLY, isShowIgnoredOnly());
      memento.putInteger(TAG_LAYOUT, getLayoutMode());
      memento.putBoolean(TAG_SHOW_TIME, isShowExecutionTime());
      memento.putInteger(TAG_SORTING_CRITERION, getSortingCriterion().ordinal());
      memento.putBoolean(TAG_WEB_STACKTRACE, isHtmlStackTrace());
    }

    private void restoreLayoutState(IMemento memento) {
      setRation(memento.getInteger(TAG_RATIO));

      var orientation = memento.getInteger(TAG_ORIENTATION);
      if (orientation != null) {
        this.orientation = orientation;
      }
      computeOrientation();

      var scrollLock = memento.getBoolean(TAG_SCROLL);
      if (scrollLock != null) {
        fScrollLockAction.setChecked(scrollLock);
        setAutoScroll(!isScrollLock());
      }

      var layout = memento.getInteger(TAG_LAYOUT);
      layout = layout == null ? LAYOUT_HIERARCHICAL : layout;

      var failuresOnly = memento.getBoolean(TAG_FAILURES_ONLY);
      failuresOnly = failuresOnly != null && failuresOnly;

      var ignoredOnly = memento.getBoolean(TAG_IGNORED_ONLY);
      ignoredOnly = ignoredOnly != null && ignoredOnly;

      var time = memento.getBoolean(TAG_SHOW_TIME);
      time = time == null || time;

      var webStack = memento.getBoolean(TAG_WEB_STACKTRACE);
      webStack = webStack == null || webStack; // default - true

      var tagSortingCriterion = memento.getInteger(TAG_SORTING_CRITERION);
      var sortingCriterion = tagSortingCriterion == null ?
          TestRunnerViewPart.SortingCriterion.SORT_BY_EXECUTION_ORDER :
          TestRunnerViewPart.SortingCriterion.values()[tagSortingCriterion];

      setSortingCriterion(sortingCriterion);

      for (var toggleSortingAction : fToggleSortingActions) {
        toggleSortingAction.update();
      }

      updateFilterAndLayout(failuresOnly, ignoredOnly, layout);
      setShowExecutionTime(time);
      setHtmlStackTrace(webStack);
    }

    private void updateFilterAndLayout(boolean failuresOnly, boolean ignoredOnly, int layoutMode) {
      this.layoutMode = layoutMode;
      fShowTestHierarchyAction.update();
      fFailuresOnlyFilterAction.setChecked(failuresOnly);
      fIgnoredOnlyFilterAction.setChecked(ignoredOnly);
      fTestViewer.setShowFailuresOrIgnoredOnly(failuresOnly, ignoredOnly, layoutMode);
      updateNextPreviousActions();
    }

  }
}
