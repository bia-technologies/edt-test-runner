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

package ru.biatech.edt.junit.ui.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

  public static String Dialogs_Information_Title;
  public static String Dialogs_Select_Methods_ForTestSuite;

  public static String Dialogs_Select_Module_Title;
  public static String Dialogs_Select_Module_Message;

  public static String Dialogs_Select_TestProject_ForTestSuite;
  public static String Message_RelatedProjectsNotFound;
  public static String Dialogs_Select_Action_Message;
  public static String Dialogs_Select_Action_Title;
  public static String Dialogs_Select_Method_Message;
  public static String Dialogs_Select_Method_Title;
  public static String Dialogs_Select_Project_Title;
  public static String Dialogs_Select_Project_ForMock;

  static {
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
    // Do not instantiate
  }
}
