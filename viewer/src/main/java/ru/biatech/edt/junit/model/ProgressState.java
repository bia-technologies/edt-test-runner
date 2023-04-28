/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.model;

/**
 * Running states of a test.
 */
public enum ProgressState {
  /**
   * state that describes that the test element has not started
   */
  NOT_STARTED("Not Started"), //$NON-NLS-1$

  /**
   * state that describes that the test element has is running
   */
  RUNNING("Running"), //$NON-NLS-1$

  /**
   * state that describes that the test element has been stopped before being completed
   */
  STOPPED("Stopped"), //$NON-NLS-1$

  /**
   * state that describes that the test element has completed
   */
  COMPLETED("Completed"); //$NON-NLS-1$

  private final String fName;

  ProgressState(String name) {
    fName = name;
  }

  @Override
  public String toString() {
    return fName;
  }
}
