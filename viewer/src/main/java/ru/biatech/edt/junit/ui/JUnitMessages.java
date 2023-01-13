/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

  public static String LaunchConfigurationName;

  public static String LaunchConfigurationTab_tab_label;
  public static String LaunchConfigurationTab_basic_launch_configuration;
  public static String LaunchConfigurationTab_basic_launch_configuration_tooltip;
  public static String LaunchConfigurationTab_filter_group;
  public static String LaunchConfigurationTab_filter_test_extension;
  public static String LaunchConfigurationTab_filter_test_module;
  public static String LaunchConfigurationTab_failedRestoreSettings;

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

  // GotoReferencedTestAction
  public static String GotoReferencedTestAction_dialog_error;
  public static String GotoReferencedTestAction_dialog_error_nomethod;
  public static String GotoReferencedTestAction_dialog_message;
  public static String GotoReferencedTestAction_dialog_select_message;
  public static String GotoReferencedTestAction_dialog_title;
  public static String GotoReferencedTestAction_selectdialog_title;

  // JUnitContainerWizardPage
  public static String JUnitContainerWizardPage_combo_label;
  public static String JUnitContainerWizardPage_error_problem_configuring_container;
  public static String JUnitContainerWizardPage_error_title;
  public static String JUnitContainerWizardPage_error_version_not_available;
  public static String JUnitContainerWizardPage_lib_not_found;
  public static String JUnitContainerWizardPage_option_junit3;
  public static String JUnitContainerWizardPage_option_junit4;
  public static String JUnitContainerWizardPage_option_junit5;
  public static String JUnitContainerWizardPage_resolved_label;
  public static String JUnitContainerWizardPage_source_location_label;
  public static String JUnitContainerWizardPage_source_not_found;
  public static String JUnitContainerWizardPage_wizard_description;
  public static String JUnitContainerWizardPage_wizard_title;

  // JUnitLaunchConfiguration
  public static String JUnitLaunchConfigurationDelegate_dialog_title;
  public static String JUnitLaunchConfigurationTab_error_invalidProjectName;
  public static String JUnitLaunchConfigurationTab_error_noContainer;
  public static String JUnitLaunchConfigurationTab_error_notJavaProject;
  public static String JUnitLaunchConfigurationTab_error_projectnotdefined;
  public static String JUnitLaunchConfigurationTab_error_projectnotexists;
  public static String JUnitLaunchConfigurationTab_error_test_class_not_found;
  public static String JUnitLaunchConfigurationTab_error_test_method_not_found;
  public static String JUnitLaunchConfigurationTab_error_testannotationnotonpath;
  public static String JUnitLaunchConfigurationTab_error_testcasenotonpath;
  public static String JUnitLaunchConfigurationTab_error_testnotdefined;
  public static String JUnitLaunchConfigurationTab_folderdialog_message;
  public static String JUnitLaunchConfigurationTab_folderdialog_title;
  public static String JUnitLaunchConfigurationTab_label_browse;
  public static String JUnitLaunchConfigurationTab_label_containerTest;
  public static String JUnitLaunchConfigurationTab_label_keeprunning;
  public static String JUnitLaunchConfigurationTab_label_method;
  public static String JUnitLaunchConfigurationTab_label_oneTest;
  public static String JUnitLaunchConfigurationTab_label_project;
  public static String JUnitLaunchConfigurationTab_label_search;
  public static String JUnitLaunchConfigurationTab_label_search_method;
  public static String JUnitLaunchConfigurationTab_method_text_decoration;
  public static String JUnitLaunchConfigurationTab_select_method_header;
  public static String JUnitLaunchConfigurationTab_select_method_title;
  public static String JUnitLaunchConfigurationTab_all_methods_text;
  public static String JUnitLaunchConfigurationTab_label_test;
  public static String JUnitLaunchConfigurationTab_projectdialog_message;
  public static String JUnitLaunchConfigurationTab_projectdialog_title;
  public static String JUnitLaunchConfigurationTab_Test_Loader;
  public static String JUnitLaunchConfigurationTab_testdialog_message;
  public static String JUnitLaunchConfigurationTab_testdialog_title;
  public static String JUnitLaunchConfigurationTab_addtag_label;
  public static String JUnitLaunchConfigurationTab_addtag_text;
  public static String JUnitLaunchConfigurationTab_includetag_checkbox_label;
  public static String JUnitLaunchConfigurationTab_excludetag_checkbox_label;
  public static String JUnitLaunchConfigurationTab_includetags_description;
  public static String JUnitLaunchConfigurationTab_excludetags_description;
  public static String JUnitLaunchConfigurationTab_addincludeexcludetagdialog_title;
  public static String JUnitLaunchConfigurationTab_includetag_empty_error;
  public static String JUnitLaunchConfigurationTab_excludetag_empty_error;

  // Shortcut
  public static String JUnitLaunchShortcut_dialog_title;
  public static String JUnitLaunchShortcut_dialog_title2;
  public static String JUnitLaunchShortcut_message_launchfailed;
  public static String JUnitLaunchShortcut_message_notests;
  public static String JUnitLaunchShortcut_message_selectConfiguration;
  public static String JUnitLaunchShortcut_message_selectDebugConfiguration;
  public static String JUnitLaunchShortcut_message_selectRunConfiguration;
  public static String JUnitLaunchShortcut_message_selectTestToDebug;
  public static String JUnitLaunchShortcut_message_selectTestToRun;

  public static String JUnitMainTab_label_defaultpackage;

  // PreferencePage
  public static String JUnitPreferencePage_addfilterbutton_label;
  public static String JUnitPreferencePage_addpackagebutton_label;
  public static String JUnitPreferencePage_addpackagedialog_message;
  public static String JUnitPreferencePage_addpackagedialog_title;
  public static String JUnitPreferencePage_addtypebutton_label;
  public static String JUnitPreferencePage_addtypedialog_error_message;
  public static String JUnitPreferencePage_addtypedialog_message;
  public static String JUnitPreferencePage_addtypedialog_title;
  public static String JUnitPreferencePage_description;
  public static String JUnitPreferencePage_disableallbutton_label;
  public static String JUnitPreferencePage_enableallbutton_label;
  public static String JUnitPreferencePage_filter_label;
  public static String JUnitPreferencePage_invalidstepfilterreturnescape;
  public static String JUnitPreferencePage_removefilterbutton_label;
  public static String JUnitPreferencePage_enableassertionscheckbox_label;
  public static String JUnitPreferencePage_showInAllViews_label;

  public static String JUnitViewEditorLauncher_dialog_title;
  public static String JUnitViewEditorLauncher_error_occurred;

  public static String LaunchConfigChange_configDeleted;
  public static String LaunchConfigRenameChange_name;
  public static String LaunchConfigSetAttributeChange_name;
  public static String OpenEditorAction_action_label;
  public static String OpenEditorAction_error_cannotopen_message;
  public static String OpenEditorAction_error_cannotopen_title;
  public static String OpenEditorAction_error_dialog_message;
  public static String OpenEditorAction_error_dialog_title;
  public static String OpenEditorAction_message_cannotopen;
  public static String OpenTestAction_error_methodNoFound;
  public static String OpenTestAction_dialog_title;
  public static String OpenTestAction_select_element;
  public static String RerunAction_label_debug;
  public static String RerunAction_label_run;
  public static String RerunAction_label_rerun;
  public static String ScrollLockAction_action_label;
  public static String ScrollLockAction_action_tooltip;
  public static String ShowNextFailureAction_label;
  public static String ShowNextFailureAction_tooltip;
  public static String ShowPreviousFailureAction_label;
  public static String ShowPreviousFailureAction_tooltip;
  public static String ShowStackTraceInConsoleViewAction_description;
  public static String ShowStackTraceInConsoleViewAction_label;
  public static String ShowStackTraceInConsoleViewAction_tooltip;
  public static String TestMethodSelectionDialog_dialog_title;
  public static String TestMethodSelectionDialog_error_notfound_message;
  public static String TestMethodSelectionDialog_error_notfound_title;
  public static String TestMethodSelectionDialog_error_title;
  public static String TestMethodSelectionDialog_no_tests_title;
  public static String TestMethodSelectionDialog_notfound_message;
  public static String TestMethodSelectionDialog_select_dialog_title;
  public static String TestMethodSelectionDialog_test_not_found;
  public static String TestMethodSelectionDialog_testproject;

  // TestRunnerViewPart
  public static String TestRunnerViewPart_activate_on_failure_only;
  public static String TestRunnerViewPart_cannotrerun_title;
  public static String TestRunnerViewPart_cannotrerurn_message;
  public static String TestRunnerViewPart_configName;
  public static String TestRunnerViewPart__error_cannotrun;
  public static String TestRunnerViewPart_error_cannotrerun;
  public static String TestRunnerViewPart_error_notests_kind;
  public static String TestRunnerViewPart_ExportTestRunSessionAction_error_title;
  public static String TestRunnerViewPart_ExportTestRunSessionAction_name;
  public static String TestRunnerViewPart_ExportTestRunSessionAction_title;
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
  public static String TestRunnerViewPart_rerunFailedFirstLaunchConfigName;
  public static String TestRunnerViewPart_stopaction_text;
  public static String TestRunnerViewPart_stopaction_tooltip;
  public static String TestRunnerViewPart_terminate_message;
  public static String TestRunnerViewPart_terminate_title;
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
  public static String TestRunnerViewPart_message_stopping;
  public static String TestRunnerViewPart_clear_history_label;
  public static String TestRunnerViewPart_layout_menu;

  public static String TestSessionLabelProvider_testName_elapsedTimeInSeconds;
  public static String TestSessionLabelProvider_testName_JUnitVersion;
  public static String TestSessionLabelProvider_testMethodName_className;
  public static String TestRunSession_unrootedTests;

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
  public static String Launcher_ConfigurationLocation;
  public static String Launcher_Launch;

  public static String TestMethodActionDelegate_CollectingMarkers;
  public static String TestMethodActionDelegate_Debug;
  public static String TestMethodActionDelegate_Run;
  public static String TestMethodMarker_LaunchTest;
  public static String TestMethodMarker_MarkerCreationError;
  public static String TestMethodMarker_MarkersCleanError;
  public static String JUnitLaunchListener_ProcessError;
  public static String JUnitModel_ReportIsEmpty;

  static {
    NLS.initializeMessages(BUNDLE_NAME, JUnitMessages.class);
  }

  private JUnitMessages() {
    // Do not instantiate
  }


}
