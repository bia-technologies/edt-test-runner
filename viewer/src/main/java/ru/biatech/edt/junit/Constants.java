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

package ru.biatech.edt.junit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
  public static final String PLUGIN_ID = "ru.biatech.edt.junit"; //$NON-NLS-1$
  public static final String ID_EXTENSION_POINT_TESTRUN_LISTENERS = "ru.biatech.edt.junit.testRunListeners"; //$NON-NLS-1$ //$NON-NLS-2$
  public static final String ID_EXTENSION_POINT_TEST_KINDS = "ru.biatech.edt.junit.testKinds"; //$NON-NLS-1$ //$NON-NLS-2$
  public static final String HISTORY_DIR_NAME = "history"; //$NON-NLS-1$
}
