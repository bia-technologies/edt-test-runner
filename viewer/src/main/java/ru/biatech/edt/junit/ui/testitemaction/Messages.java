/*******************************************************************************
 * Copyright (c) 2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.testitemaction;

import org.eclipse.osgi.util.NLS;

/**
 * @author alko
 */
public class Messages extends NLS {
  private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
  public static String GotoTestCaseAction_Present;
  public static String GotoTestSuiteAction_Present;
  public static String NewTestCaseAction_Failed;
  public static String NewTestCaseAction_Present;
  public static String NewTestSuiteAction_Failed;
  public static String NewTestSuiteAction_Present;
  public static String UpdateTestSuiteAction_Description;
  public static String UpdateTestSuiteAction_NoMethodsToAdd;
  public static String UpdateTestSuiteAction_Present;
  public static String GenerateMock_Present;

  public static String GenerateMockForEvents_events_not_found;
  public static String GenerateMock_failed_error_prefix;
  public static String GenerateMock_failed_message;
  public static String GenerateMockForEvents_present;
  public static String GenerateMockForEvents_select_event;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
