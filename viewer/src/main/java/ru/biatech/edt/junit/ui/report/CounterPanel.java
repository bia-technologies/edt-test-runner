/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.report;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.JUnitMessages;

import java.text.MessageFormat;

/**
 * A panel with counters for the number of Runs, Errors and Failures.
 */
public class CounterPanel extends Composite {
  protected Text fNumberOfErrors;
  protected Text fNumberOfFailures;
  protected Text fNumberOfRuns;
  protected int fTotal;
  protected int fIgnoredCount;
  protected int fAssumptionFailedCount;

  private final Image fErrorIcon = TestViewerPlugin.ui().createImage("ovr16/error_ovr.png"); //$NON-NLS-1$
  private final Image fFailureIcon = TestViewerPlugin.ui().createImage("ovr16/failed_ovr.png"); //$NON-NLS-1$

  public CounterPanel(Composite parent) {
    super(parent, SWT.WRAP);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 9;
    gridLayout.makeColumnsEqualWidth = false;
    gridLayout.marginWidth = 0;
    setLayout(gridLayout);

    fNumberOfRuns = createLabel(JUnitMessages.CounterPanel_label_runs, null, " 0/0  "); //$NON-NLS-1$
    fNumberOfErrors = createLabel(JUnitMessages.CounterPanel_label_errors, fErrorIcon, " 0 "); //$NON-NLS-1$
    fNumberOfFailures = createLabel(JUnitMessages.CounterPanel_label_failures, fFailureIcon, " 0 "); //$NON-NLS-1$

    addDisposeListener(e -> disposeIcons());
  }

  private void disposeIcons() {
    fErrorIcon.dispose();
    fFailureIcon.dispose();
  }

  private Text createLabel(String name, Image image, String init) {
    Label label = new Label(this, SWT.NONE);
    if (image != null) {
      image.setBackground(label.getBackground());
      label.setImage(image);
    }
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

    label = new Label(this, SWT.NONE);
    label.setText(name);
    label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
    //label.setFont(JFaceResources.getBannerFont());

    Text value = new Text(this, SWT.READ_ONLY);
    value.setText(init);
    // bug: 39661 Junit test counters do not repaint correctly [JUnit]
    fixReadonlyTextBackground(value);
    value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
    return value;
  }

  public void reset() {
    setErrorValue(0);
    setFailureValue(0);
    setRunValue(0, 0, 0);
    fTotal = 0;
  }

  public void setTotal(int value) {
    fTotal = value;
  }

  public int getTotal() {
    return fTotal;
  }

  public void setRunValue(int value, int ignoredCount, int assumptionFailureCount) {
    String runString;
    String runStringTooltip;
    if (ignoredCount == 0 && assumptionFailureCount == 0) {
      runString = MessageFormat.format(JUnitMessages.CounterPanel_runcount, value, fTotal);
      runStringTooltip = runString;
    } else if (ignoredCount != 0 && assumptionFailureCount == 0) {
      runString = MessageFormat.format(JUnitMessages.CounterPanel_runcount_skipped, value, fTotal, ignoredCount);
      runStringTooltip = MessageFormat.format(JUnitMessages.CounterPanel_runcount_ignored, value, fTotal, ignoredCount);
    } else if (ignoredCount == 0 && assumptionFailureCount != 0) {
      runString = MessageFormat.format(JUnitMessages.CounterPanel_runcount_skipped, value, fTotal, assumptionFailureCount);
      runStringTooltip = MessageFormat.format(JUnitMessages.CounterPanel_runcount_assumptionsFailed, value, fTotal, assumptionFailureCount);
    } else {
      runString = MessageFormat.format(JUnitMessages.CounterPanel_runcount_skipped, value, fTotal, ignoredCount + assumptionFailureCount);
      runStringTooltip = MessageFormat.format(JUnitMessages.CounterPanel_runcount_ignored_assumptionsFailed, value, fTotal, ignoredCount, assumptionFailureCount);
    }
    fNumberOfRuns.setText(runString);
    fNumberOfRuns.setToolTipText(runStringTooltip);

    if (fIgnoredCount == 0 && ignoredCount > 0 || fIgnoredCount != 0 && ignoredCount == 0) {
      layout();
    } else if (fAssumptionFailedCount == 0 && assumptionFailureCount > 0 || fAssumptionFailedCount != 0 && assumptionFailureCount == 0) {
      layout();
    } else {
      fNumberOfRuns.redraw();
      redraw();
    }
    fIgnoredCount = ignoredCount;
    fAssumptionFailedCount = assumptionFailureCount;
  }

  public void setErrorValue(int value) {
    fNumberOfErrors.setText(Integer.toString(value));
    redraw();
  }

  public void setFailureValue(int value) {
    fNumberOfFailures.setText(Integer.toString(value));
    redraw();
  }
  public static void fixReadonlyTextBackground(Text textField) {
    // move from SWTUtil
    textField.setBackground(textField.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
  }
}
