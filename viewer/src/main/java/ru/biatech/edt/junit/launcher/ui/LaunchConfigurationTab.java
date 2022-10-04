/*******************************************************************************
 * Copyright (c) 2021-2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.launcher.ui;

import com._1c.g5.v8.dt.core.platform.IExtensionProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.launcher.v8.LaunchConfigurationAttributes;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.JUnitMessages;

public class LaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private LaunchControl control;

  @Override
  public void createControl(Composite parent) {
    Composite composite = new Composite(parent, SWT.None);
    this.setControl(composite);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), this.getHelpContextId());
    GridLayoutFactory.swtDefaults().applyTo(composite);
    composite.setFont(parent.getFont());
    control = new LaunchControl(composite, 0);

    control.usedLaunchConfigurationControl.addSelectionChangedListener(this::selectionChanged);
    control.testExtensionControl.addSelectionChangedListener(this::selectionChanged);
    control.testModuleControl.addSelectionChangedListener(this::selectionChanged);
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    var configurations = LaunchHelper.getOnecLaunchConfigurations();
    var onecConfiguration = configurations.findFirst();
    onecConfiguration.ifPresent(launchConfiguration ->
                                        configuration.setAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, launchConfiguration.getName()));

    var extensions = LaunchHelper.getExtensions();
    if (extensions.size() == 1)
      configuration.setAttribute(LaunchConfigurationAttributes.TEST_EXTENSION, extensions.get(0).getDtProject().getName());
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    control.initializeFrom();

    control.testExtensionControl.addSelectionChangedListener(event -> {
      var project = UtilsUI.getSelection(control.testExtensionControl, IExtensionProject.class);
      UtilsUI.setValueSource(control.testModuleControl, project == null ? null : LaunchHelper.getTestModules(project));
    });

    updateParametersFromConfig(configuration);
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    var usedConfiguration = UtilsUI.getSelection(control.usedLaunchConfigurationControl, ILaunchConfiguration.class);
    var project = UtilsUI.getSelection(control.testExtensionControl, IExtensionProject.class);
    var module = UtilsUI.getSelection(control.testModuleControl, String.class);

    configuration.setAttribute(LaunchConfigurationAttributes.USED_LAUNCH_CONFIGURATION, usedConfiguration == null ? null : usedConfiguration.getName());
    configuration.setAttribute(LaunchConfigurationAttributes.TEST_EXTENSION, project == null ? null : project.getDtProject().getName());
    configuration.setAttribute(LaunchConfigurationAttributes.TEST_MODULE, module);
  }

  @Override
  public String getName() {
    return JUnitMessages.LaunchConfigurationTab_tab_label;
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {

    setErrorMessage(null);
    boolean success = true;
    try {
      LaunchHelper.checkConfiguration(launchConfig);
    } catch (CoreException e) {
      setErrorMessage(e.getMessage());
      success = false;
    }
    return success;
  }

  protected void updateParametersFromConfig(ILaunchConfiguration configuration) {
    try {
      var usedConfiguration = LaunchHelper.getTargetConfiguration(configuration);
      var project = LaunchHelper.getTestExtension(configuration);
      var moduleName = LaunchConfigurationAttributes.getTestModuleName(configuration);

      UtilsUI.setSelection(control.usedLaunchConfigurationControl, usedConfiguration);
      UtilsUI.setSelection(control.testExtensionControl, project);
      UtilsUI.setSelection(control.testModuleControl, moduleName);
    } catch (CoreException e) {
      TestViewerPlugin.log().logError(JUnitMessages.LaunchConfigurationTab_failedRestoreSettings, e);
    }
  }


  public void selectionChanged(SelectionChangedEvent event) {
    setDirty(true);
    updateLaunchConfigurationDialog();
  }
}
