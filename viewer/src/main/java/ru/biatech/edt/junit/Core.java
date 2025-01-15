/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Mirko Raner <mirko@raner.ws> - Expose JUnitModel.exportTestRunSession(...) as API - https://bugs.eclipse.org/316199
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit;

import lombok.Getter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import ru.biatech.edt.junit.model.SessionsManager;

import java.io.File;

public class Core {
  /**
   * The single instance of this plug-in runtime class.
   */
  @Getter
  private final SessionsManager sessionsManager = new SessionsManager();

  private ListenerList<TestRunListener> newListeners = null;

  /**
   * Adds a TestRun listener to the collection of listeners
   *
   * @param newListener the listener to add
   * @deprecated to avoid deprecation warnings
   */
  @Deprecated
  public void addTestRunListener(TestRunListener newListener) {
    var listeners = getNewTestRunListeners();
    for (TestRunListener o : listeners) {
      if (o == newListener)
        return;
    }
    getNewTestRunListeners().add(newListener);
  }

  /**
   * @return a <code>ListenerList</code> of all <code>TestRunListener</code>s
   */
  public ListenerList<TestRunListener> getNewTestRunListeners() {
    if (newListeners == null) {
      newListeners = calculateNewListeners();
    }
    return newListeners;
  }

  private ListenerList<TestRunListener> calculateNewListeners() {
    ListenerList<TestRunListener> listeners = new ListenerList<>();
    IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Constants.ID_EXTENSION_POINT_TESTRUN_LISTENERS);
    if (extensionPoint == null) {
      return listeners;
    }
    IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
    MultiStatus status = new MultiStatus(Constants.PLUGIN_ID, IStatus.OK, "Could not load some testRunner extension points", null); //$NON-NLS-1$
    for (IConfigurationElement config : configs) {
      try {
        Object testRunListener = config.createExecutableExtension("class"); //$NON-NLS-1$
        if (testRunListener instanceof TestRunListener) {
          listeners.add((TestRunListener) testRunListener);
        }
      } catch (CoreException e) {
        status.add(e.getStatus());
      }
    }
    if (!status.isOK()) {
      TestViewerPlugin.log().log(status);
    }
    return listeners;
  }

  public File getHistoryDirectory() throws IllegalStateException {
    File historyDir = TestViewerPlugin.getDefault().getStateLocation().append(Constants.HISTORY_DIR_NAME).toFile();
    if (!historyDir.isDirectory() && !historyDir.mkdir()) {
      throw new IllegalStateException("Не удалось создать директорию истории: " + historyDir);
    }
    return historyDir;
  }
}
