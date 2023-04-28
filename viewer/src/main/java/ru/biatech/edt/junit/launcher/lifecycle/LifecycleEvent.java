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

package ru.biatech.edt.junit.launcher.lifecycle;

public class LifecycleEvent {
  public static final int UNKNOWN = 0;
  public static final int START = 1;
  public static final int STOP = 2;
  public static final int SUCCESS = 4;
  public static final int ERROR = 8;
  public static final int CANCEL = 16;
  public static final int FINISHED = STOP | SUCCESS;
  public static final int FINISHED_WITH_ERROR = STOP | ERROR;
  public static final int CANCELED = STOP | CANCEL;

  public static boolean isFinished(int value) {
    return value == FINISHED || value == FINISHED_WITH_ERROR;
  }

  public static String getPresent(int value) {
    String present;
    switch (value) {
      case START:
        present = "START";
        break;
      case STOP:
        present = "STOP";
        break;
      case SUCCESS:
        present = "SUCCESS";
        break;
      case ERROR:
        present = "ERROR";
        break;
      case CANCEL:
        present = "CANCEL";
        break;
      case FINISHED:
        present = "FINISHED";
        break;
      case FINISHED_WITH_ERROR:
        present = "FINISHED_WITH_ERROR";
        break;
      case CANCELED:
        present = "CANCELED";
        break;
      default:
        present = "UNKNOWN";
    }
    return present;
  }

  public static boolean isStop(int eventType) {
    return (eventType & STOP) == STOP;
  }
}
