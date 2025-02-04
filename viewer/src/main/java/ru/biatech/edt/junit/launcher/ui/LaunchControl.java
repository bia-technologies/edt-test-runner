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
import com._1c.g5.v8.dt.platform.services.ui.AutoCompleteComboViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.UIMessages;

import java.util.stream.Collectors;

public class LaunchControl extends Composite {

  ComboViewer usedLaunchConfigurationControl;
  ComboViewer testExtensionControl;
  ComboViewer testModuleControl;
  Text projectPathControl;
  Button loggingControl;
  Button useRemoteLaunchControl;

  public LaunchControl(Composite parent, int style) {
    super(parent, style);

    setLayout(new GridLayout(2, false));

    appendLabel(this, UIMessages.LaunchConfigurationTab_basic_launch_configuration);
    usedLaunchConfigurationControl = appendAutoCompleteComboViewer(this);
    usedLaunchConfigurationControl.getCombo().setToolTipText(UIMessages.LaunchConfigurationTab_basic_launch_configuration_tooltip);

    Group grpFilter = new Group(this, SWT.NONE);
    grpFilter.setText(UIMessages.LaunchConfigurationTab_filter_group);
    grpFilter.setLayout(new GridLayout(2, false));
    grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 2));

    appendLabel(grpFilter, UIMessages.LaunchConfigurationTab_filter_test_extension);
    testExtensionControl = appendAutoCompleteComboViewer(grpFilter);

    appendLabel(grpFilter, UIMessages.LaunchConfigurationTab_filter_test_module);
    testModuleControl = appendAutoCompleteComboViewer(grpFilter);

    Group grpSettings = new Group(this, SWT.NONE);
    grpSettings.setText(UIMessages.LaunchConfigurationTab_SettingsTab);
    grpSettings.setLayout(new GridLayout(3, false));
    grpSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 2));

    appendLabel(grpSettings, UIMessages.LaunchConfigurationTab_ProjectPath);
    projectPathControl = new Text(grpSettings, SWT.BORDER);
    projectPathControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    appendLabel(grpSettings, "Выводить лог в консоль");
    loggingControl = new Button(grpSettings, SWT.CHECK);
    loggingControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    var toolTip = "Запуск тестов без перезапуска.1С:Предприятие на закрывается и слушает команды 1С:EDT";
    appendLabel(grpSettings, "Запуск тестов без перезапуска.", toolTip);
    useRemoteLaunchControl = new Button(grpSettings, SWT.CHECK);
    useRemoteLaunchControl.setToolTipText(toolTip);
    useRemoteLaunchControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
  }

  void initializeFrom() {
    UtilsUI.setValueSource(usedLaunchConfigurationControl, LaunchHelper.getOnecLaunchConfigurations().collect(Collectors.toList()));
    LabelProvider provider = LabelProvider.createTextProvider(e -> e == null ? "" : ((IExtensionProject) e).getDtProject().getName()); //$NON-NLS-1$
    UtilsUI.setValueSource(testExtensionControl, LaunchHelper.getTestExtensions(), provider);
  }

  private void appendLabel(Composite parent, String text) {
    appendLabel(parent, text, null);
  }

  private void appendLabel(Composite parent, String text, String tooltip) {
    Label label = new Label(parent, SWT.NONE);
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    label.setText(text);
    label.setToolTipText(tooltip);
  }

  private AutoCompleteComboViewer appendAutoCompleteComboViewer(Composite parent) {
    AutoCompleteComboViewer auto = new AutoCompleteComboViewer(parent, SWT.NONE);
    Combo combo = auto.getCombo();
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    return auto;
  }
}
