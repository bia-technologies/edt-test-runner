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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import ru.biatech.edt.junit.launcher.v8.LaunchHelper;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.util.stream.Collectors;

public class LaunchControl extends Composite {

  ComboViewer usedLaunchConfigurationControl;
  ComboViewer testExtensionControl;
  ComboViewer testModuleControl;

  /**
   * Create the composite.
   *
   * @param parent
   * @param style
   */
  public LaunchControl(Composite parent, int style) {
    super(parent, style);

    setLayout(new GridLayout(2, false));

    Label label = new Label(this, SWT.NONE);
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    label.setText(JUnitMessages.JUnitLaunchConfigurationTab_basic_launch_configuration);

    usedLaunchConfigurationControl = new AutoCompleteComboViewer(this, SWT.NONE);
    Combo combo = usedLaunchConfigurationControl.getCombo();
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    label = new Label(this, SWT.NONE);
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    label.setText(JUnitMessages.JUnitLaunchConfigurationTab_filter_test_extension);

    testExtensionControl = new AutoCompleteComboViewer(this, SWT.NONE);
    combo = testExtensionControl.getCombo();
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    label = new Label(this, SWT.NONE);
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    label.setText(JUnitMessages.JUnitLaunchConfigurationTab_filter_test_module);

    testModuleControl = new AutoCompleteComboViewer(this, SWT.NONE);
    combo = testModuleControl.getCombo();
    combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
  }

  @Override
  protected void checkSubclass() {
  }

  void initializeFrom() {
    UtilsUI.setValueSource(usedLaunchConfigurationControl, LaunchHelper.getOnecLaunchConfigurations().collect(Collectors.toList()));
    LabelProvider provider = LabelProvider.createTextProvider(e -> e == null ? "" : ((IExtensionProject) e).getDtProject().getName());
    UtilsUI.setValueSource(testExtensionControl, LaunchHelper.getExtensions(), provider);
  }
}
