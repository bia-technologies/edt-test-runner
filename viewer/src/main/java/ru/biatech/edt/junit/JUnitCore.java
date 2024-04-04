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

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import ru.biatech.edt.junit.model.JUnitModel;

import java.io.File;

public class JUnitCore {
  public static final String CORE_PLUGIN_ID = "ru.biatech.edt.juni.core"; //$NON-NLS-1$
  /**
   * Plug-in ID of the <b>UI</b> plug-in ("ru.biatech.edt.junit").
   *
   * @see #CORE_PLUGIN_ID
   */
  public static final String PLUGIN_ID = "ru.biatech.edt"; //$NON-NLS-1$
  public static final String ID_EXTENSION_POINT_TESTRUN_LISTENERS = "ru.biatech.edt.junit.testRunListeners"; //$NON-NLS-1$ //$NON-NLS-2$
  public static final String ID_EXTENSION_POINT_TEST_KINDS = "ru.biatech.edt.junit.testKinds"; //$NON-NLS-1$ //$NON-NLS-2$
  private static final String HISTORY_DIR_NAME = "history"; //$NON-NLS-1$
  private static final boolean fIsStopped = false;
  /**
   * The single instance of this plug-in runtime class.
   */
  private static JUnitCore fgPlugin = null;
  private final JUnitModel fJUnitModel = new JUnitModel();

  private final LazyInitializer<ListenerList<TestRunListener>> newListeners = new LazyInitializer<>() {
    @Override
    protected ListenerList<TestRunListener> initialize() {
      ListenerList<TestRunListener> listeners = new ListenerList<>();
      IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ID_EXTENSION_POINT_TESTRUN_LISTENERS);
      if (extensionPoint == null) {
        return listeners;
      }
      IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
      MultiStatus status = new MultiStatus(CORE_PLUGIN_ID, IStatus.OK, "Could not load some testRunner extension points", null); //$NON-NLS-1$
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
  };


  public JUnitCore() {
    fgPlugin = this;
  }

  public static JUnitCore getDefault() {
    return fgPlugin;
  }

  public static boolean isStopped() {
    return fIsStopped;
  }

  public JUnitModel getModel() {
    return fJUnitModel;
  }

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
    try {
      return newListeners.get();
    } catch (ConcurrentException e) {
      TestViewerPlugin.log().logError(e);
      return null;
    }
  }

  public File getHistoryDirectory() throws IllegalStateException {
    File historyDir = TestViewerPlugin.getDefault().getStateLocation().append(HISTORY_DIR_NAME).toFile();
    if (!historyDir.isDirectory()) {
      historyDir.mkdir();
    }
    return historyDir;
  }
}
