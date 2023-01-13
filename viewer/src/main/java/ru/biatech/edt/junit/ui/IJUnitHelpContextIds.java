/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui;

/**
 * Help context ids for the JUnit UI.
 */
public interface IJUnitHelpContextIds {
  String PREFIX = JUnitUI.PLUGIN_ID + '.';

  // Actions
  String COPY_TRACE_ACTION = PREFIX + "copy_trace_action_context"; //$NON-NLS-1$
  String COPY_FAILURE_LIST_ACTION = PREFIX + "copy_failure_list_action_context"; //$NON-NLS-1$
  String OPEN_EDITOR_AT_LINE_ACTION = PREFIX + "open_editor_atline_action_context"; //$NON-NLS-1$
  String OPEN_TEST_ACTION = PREFIX + "open_test_action_context"; //$NON-NLS-1$
  String RERUN_ACTION = PREFIX + "rerun_test_action_context"; //$NON-NLS-1$
  String GOTO_REFERENCED_TEST_ACTION_CONTEXT = PREFIX + "goto_referenced_test_action_context"; //$NON-NLS-1$
  String OUTPUT_SCROLL_LOCK_ACTION = PREFIX + "scroll_lock"; //$NON-NLS-1$

  // view parts
  String RESULTS_VIEW = PREFIX + "results_view_context"; //$NON-NLS-1$
  String RESULTS_VIEW_TOGGLE_ORIENTATION_ACTION = PREFIX + "results_view_toggle_call_mode_action_context"; //$NON-NLS-1$

  // Preference/Property pages
  String JUNIT_PREFERENCE_PAGE = PREFIX + "junit_preference_page_context"; //$NON-NLS-1$

  // Wizard pages
  String NEW_TESTCASE_WIZARD_PAGE = PREFIX + "new_testcase_wizard_page_context"; //$NON-NLS-1$
  String NEW_TESTCASE_WIZARD_PAGE2 = PREFIX + "new_testcase_wizard_page2_context"; //$NON-NLS-1$
  String NEW_TESTSUITE_WIZARD_PAGE = PREFIX + "new_testsuite_wizard_page2_context"; //$NON-NLS-1$
  String LAUNCH_CONFIGURATION_DIALOG_JUNIT_MAIN_TAB = PREFIX + "launch_configuration_dialog_junit_main_tab"; //$NON-NLS-1$

  // Dialogs
  String TEST_SELECTION_DIALOG = PREFIX + "test_selection_context"; //$NON-NLS-1$
  String RESULT_COMPARE_DIALOG = PREFIX + "result_compare_context"; //$NON-NLS-1$
}