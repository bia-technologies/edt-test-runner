/*******************************************************************************
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.text.MessageFormat;

public class Logger {

  private static final boolean debug = Boolean.parseBoolean(System.getProperty("ru.biatech.edt.junit.debug", "false"));

  private String getPluginID() {
    return Constants.PLUGIN_ID;
  }

  public void log(IStatus status) {
    TestViewerPlugin.getDefault().getLog().log(status);
  }

  public void logError(Throwable throwable) {
    log(createErrorStatus(throwable.getMessage(), throwable));
  }

  public void logError(String message, Throwable exc) {
    log(createErrorStatus(message, exc));
  }

  public void logError(String message) {
    log(createErrorStatus(message));
  }

  public IStatus createErrorStatus(String message, Throwable throwable) {
    return new Status(IStatus.ERROR, getPluginID(), 0, message, throwable);
  }

  public IStatus createErrorStatus(String message) {
    return new Status(IStatus.ERROR, getPluginID(), message);
  }

  public IStatus createWarningStatus(String message) {
    return new Status(IStatus.WARNING, getPluginID(), 0, message, null);
  }

  public void debug(String message) {
    if (debug) {
      log(new Status(IStatus.OK, getPluginID(), 0, message, null));
    }
  }

  public void debug(String template, Object... objects) {
    if (debug) {
      String message = MessageFormat.format(template, objects);
      debug(message);
    }
  }

  public void warning(String template, Object... objects) {
    String message = MessageFormat.format(template, objects);
    log(createWarningStatus(message));
  }
}
