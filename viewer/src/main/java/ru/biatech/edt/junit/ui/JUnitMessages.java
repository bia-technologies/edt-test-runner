/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *     David Saff (saff@mit.edu) - bug 102632: [JUnit] Support for JUnit 4.
 *     Robert Konigsberg <konigsberg@google.com> - [JUnit] Improve discoverability of the ability to run a single method under JUnit Tests - https://bugs.eclipse.org/bugs/show_bug.cgi?id=285637
 *     Andrej Zachar <andrej@chocolatejar.eu> - [JUnit] Add a filter for ignored tests - https://bugs.eclipse.org/bugs/show_bug.cgi?id=298603
 *     Gautier de Saint Martin Lacaze <gautier.desaintmartinlacaze@gmail.com> - [JUnit] need 'collapse all' feature in JUnit view - https://bugs.eclipse.org/bugs/show_bug.cgi?id=277806
 *     Sandra Lions <sandra.lions-piron@oracle.com> - [JUnit] allow to sort by name and by execution time - https://bugs.eclipse.org/bugs/show_bug.cgi?id=219466
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui;

import org.eclipse.osgi.util.NLS;

public final class JUnitMessages extends NLS {

  private static final String BUNDLE_NAME = "ru.biatech.edt.junit.ui.JUnitMessages";//$NON-NLS-1$

  public static String LaunchConfigurationTab_tab_label;
  public static String LaunchConfigurationTab_basic_launch_configuration;
  public static String LaunchConfigurationTab_basic_launch_configuration_tooltip;
  public static String LaunchConfigurationTab_filter_group;
  public static String LaunchConfigurationTab_filter_test_extension;
  public static String LaunchConfigurationTab_filter_test_module;
  public static String LaunchConfigurationTab_failedRestoreSettings;
  public static String LaunchConfigurationTab_ProjectPath;
  public static String LaunchConfigurationTab_SettingsTab;
  public static String CompareResultDialog_actualLabel;
  public static String CompareResultDialog_expectedLabel;
  public static String CompareResultDialog_labelOK;
  public static String CompareResultDialog_title;
  public static String CompareResultsAction_description;
  public static String CompareResultsAction_label;
  public static String CompareResultsAction_tooltip;
  public static String CopyFailureList_action_label;
  public static String CopyFailureList_clipboard_busy;
  public static String CopyFailureList_problem;
  public static String CopyTrace_action_label;
  public static String CopyTraceAction_clipboard_busy;
  public static String CopyTraceAction_problem;
  public static String CounterPanel_label_errors;
  public static String CounterPanel_label_failures;
  public static String CounterPanel_label_runs;
  public static String CounterPanel_runcount;
  public static String CounterPanel_runcount_assumptionsFailed;
  public static String CounterPanel_runcount_ignored;
  public static String CounterPanel_runcount_skipped;
  public static String CounterPanel_runcount_ignored_assumptionsFailed;
  public static String ExpandAllAction_text;
  public static String ExpandAllAction_tooltip;
  public static String CollapseAllAction_text;
  public static String CollapseAllAction_tooltip;

  public static String Debug_Test_Label;
  public static String Run_Test_Label;
  public static String RerunAction_label_rerun;
  public static String ScrollLockAction_action_label;
  public static String ScrollLockAction_action_tooltip;
  public static String ShowNextFailureAction_label;
  public static String ShowNextFailureAction_tooltip;
  public static String ShowPreviousFailureAction_label;
  public static String ShowPreviousFailureAction_tooltip;
  public static String ShowWebStackTraceAction_name;

  // TestRunnerViewPart
  public static String TestRunnerViewPart_activate_on_failure_only;
  public static String TestRunnerViewPart_error_notests_kind;
  public static String TestRunnerViewPart_ImportTestRunSessionAction_error_title;
  public static String TestRunnerViewPart_ImportTestRunSessionAction_name;
  public static String TestRunnerViewPart_ImportTestRunSessionAction_title;
  public static String TestRunnerViewPart_jobName;
  public static String TestRunnerViewPart_label_failure;
  public static String TestRunnerViewPart_Launching;
  public static String TestRunnerViewPart_message_finish;
  public static String TestRunnerViewPart_message_started;
  public static String TestRunnerViewPart_message_stopped;
  public static String TestRunnerViewPart_message_terminated;
  public static String TestRunnerViewPart_rerunaction_label;
  public static String TestRunnerViewPart_rerunaction_tooltip;
  public static String TestRunnerViewPart_rerunfailuresaction_label;
  public static String TestRunnerViewPart_rerunfailuresaction_tooltip;
  public static String TestRunnerViewPart_toggle_automatic_label;
  public static String TestRunnerViewPart_toggle_horizontal_label;
  public static String TestRunnerViewPart_toggle_vertical_label;
  public static String TestRunnerViewPart_titleToolTip;
  public static String TestRunnerViewPart_wrapperJobName;
  public static String TestRunnerViewPart_history;
  public static String TestRunnerViewPart_test_run_history;
  public static String TestRunnerViewPart_test_runs;
  public static String TestRunnerViewPart_select_test_run;
  public static String TestRunnerViewPart_testName_startTime;
  public static String TestRunnerViewPart_max_remembered;
  public static String TestRunnerViewPart_show_execution_time;
  public static String TestRunnerViewPart_show_failures_only;
  public static String TestRunnerViewPart_show_ignored_only;
  public static String TestRunnerViewPart_hierarchical_layout;
  public static String TestRunnerViewPart_sort_by_menu;
  public static String TestRunnerViewPart_toggle_name_label;
  public static String TestRunnerViewPart_toggle_execution_order_label;
  public static String TestRunnerViewPart_toggle_execution_time_label;
  public static String TestRunnerViewPart_clear_history_label;
  public static String TestRunnerViewPart_layout_menu;

  public static String TestSessionLabelProvider_testName_elapsedTimeInSeconds;
  public static String TestSessionLabelProvider_testName_JUnitVersion;
  public static String TestSessionLabelProvider_testMethodName_className;

  public static String LaunchConfigurationDelegate_Launching;

  public static String LaunchHelper_LaunchConfigurationNotFound;
  public static String LaunchHelper_LaunchConfigurationNotSpecified;
  public static String JUnitModel_LoadReport;
  public static String JUnitModel_ReportFile;
  public static String JUnitModel_ReportFileNotFound;
  public static String JUnitModel_UnknownErrorOnReportLoad;

  public static String JUnitModel_could_not_import;
  public static String JUnitModel_could_not_read;
  public static String JUnitModel_importing_from_url;
  public static String TestRunHandler_lines_read;
  public static String TestMethodActionDelegate_CollectingMarkers;
  public static String TestMethodMarker_LaunchTest;
  public static String TestMethodMarker_MarkerCreationError;
  public static String TestMethodMarker_MarkersCleanError;
  public static String JUnitLaunchListener_ProcessError;
  public static String JUnitModel_ReportIsEmpty;

  public static String LaunchTest_dialog_title;
  public static String LaunchTest_error_is_not_test;

  public static String OpenTestAction_label;
  public static String OpenTestAction_error_not_found;
  public static String OpenUnderTestMethodAction_label;
  public static String OpenUnderTestMethodAction_error_not_found;

  static {
    NLS.initializeMessages(BUNDLE_NAME, JUnitMessages.class);
  }

  private JUnitMessages() {
    // Do not instantiate
  }
}
