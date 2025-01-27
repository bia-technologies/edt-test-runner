/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * Copyright (c) 2022-2024 BIA-Technologies Limited Liability Company.
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
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.ui.labelProvider;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import ru.biatech.edt.junit.BasicElementLabels;
import ru.biatech.edt.junit.model.ITestCaseElement;
import ru.biatech.edt.junit.model.ITestElement;
import ru.biatech.edt.junit.model.ITestSuiteElement;
import ru.biatech.edt.junit.model.TestCaseElement;
import ru.biatech.edt.junit.model.TestSuiteElement;
import ru.biatech.edt.junit.ui.UIMessages;
import ru.biatech.edt.junit.ui.report.TestRunnerViewPart;
import ru.biatech.edt.junit.ui.utils.StringUtilities;
import ru.biatech.edt.junit.ui.viewsupport.ImageProvider;

import java.text.MessageFormat;
import java.text.NumberFormat;

/**
 * Провайдер формирования представлений строк дерева тестов
 */
public class TestSessionLabelProvider extends LabelProvider implements IStyledLabelProvider {

  private final TestRunnerViewPart fTestRunnerPart;
  private final int fLayoutMode;
  private final NumberFormat timeFormat;
  private final ImageProvider imageProvider;
  private boolean fShowTime;

  public TestSessionLabelProvider(TestRunnerViewPart testRunnerPart, int layoutMode) {
    fTestRunnerPart = testRunnerPart;
    fLayoutMode = layoutMode;
    fShowTime = true;

    timeFormat = NumberFormat.getNumberInstance();
    timeFormat.setGroupingUsed(true);
    timeFormat.setMinimumFractionDigits(3);
    timeFormat.setMaximumFractionDigits(3);
    timeFormat.setMinimumIntegerDigits(1);
    imageProvider = testRunnerPart.getImageProvider();
  }

  @Override
  public StyledString getStyledText(Object element) {
    String label = getSimpleLabel(element);

    if (label == null) {
      return new StyledString(element.toString());
    }
    StyledString text = new StyledString(label);

    ITestElement testElement = (ITestElement) element;
    if (fLayoutMode == TestRunnerViewPart.LAYOUT_HIERARCHICAL) {
      if (element instanceof ITestSuiteElement) {
        text = addContext(text, ((ITestSuiteElement) testElement).getContext());
      }
    } else {
      if (element instanceof ITestCaseElement) {
        String decorated = getTextForFlatLayout((TestCaseElement) testElement, label);
        text = StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.QUALIFIER_STYLER, text);
        text = addContext(text, ((ITestCaseElement) testElement).getContext());
      }
    }
    text = addElapsedTime(text, testElement.getElapsedTimeInSeconds());

    return text;
  }

  private String getTextForFlatLayout(TestCaseElement testCaseElement, String label) {
    String parentName;
    String parentDisplayName = testCaseElement.getParent().getDisplayName();
    if (parentDisplayName != null) {
      parentName = parentDisplayName;
    } else {
      parentName = testCaseElement.getClassName();
    }
    return MessageFormat.format(UIMessages.TestSessionLabelProvider_testMethodName_className, label, BasicElementLabels.getElementName(parentName));
  }

  private StyledString addElapsedTime(StyledString styledString, double time) {
    String string = styledString.getString();
    String decorated = addElapsedTime(string, time);
    return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.COUNTER_STYLER, styledString);
  }

  private StyledString addContext(StyledString styledString, String context) {
    if (StringUtilities.isNullOrEmpty(context)) {
      return styledString;
    }

    String substring = "[" + context + "]";
    String styledStringText = styledString.getString();
    if (!styledStringText.contains(substring)) {
      substring = context;
      if (!styledStringText.contains(substring)) {
        substring = null;
      }
    }

    if (substring == null) {
      String decorated = styledStringText + " [" + context + "]";
      return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.DECORATIONS_STYLER, styledString);
    } else {
      int start = styledStringText.indexOf(substring);
      styledString.setStyle(start, substring.length(), StyledString.DECORATIONS_STYLER);
      return styledString;
    }
  }

  private String addElapsedTime(String string, double time) {
    if (!fShowTime || Double.isNaN(time)) {
      return string;
    }
    String formattedTime = timeFormat.format(time);
    return MessageFormat.format(UIMessages.TestSessionLabelProvider_testName_elapsedTimeInSeconds, string, formattedTime);
  }

  private String getSimpleLabel(Object element) {
    if (element instanceof ITestCaseElement) {

      return BasicElementLabels.getElementName(((ITestCaseElement) element).getDisplayName());

    } else if (element instanceof ITestSuiteElement) {

      return BasicElementLabels.getElementName(((ITestSuiteElement) element).getDisplayName());

    }
    return null;
  }

  @Override
  public String getText(Object element) {
    String label = getSimpleLabel(element);
    if (label == null) {
      return element.toString();
    }
    ITestElement testElement = (ITestElement) element;
    if (fLayoutMode != TestRunnerViewPart.LAYOUT_HIERARCHICAL && element instanceof TestCaseElement) {
        label = getTextForFlatLayout((TestCaseElement) testElement, label);
    }
    return addElapsedTime(label, testElement.getElapsedTimeInSeconds());
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof TestCaseElement) {
      switch (((TestCaseElement) element).getResultStatus(true)) {
        case SKIPPED:
          return imageProvider.getTestSkippedIcon();
        case ERROR:
          return imageProvider.getTestErrorIcon();
        case FAILURE:
          return imageProvider.getTestFailIcon();
        case OK:
          return imageProvider.getTestOkIcon();
        case UNDEFINED:
          return imageProvider.getTestIgnoredIcon();
        default:
          throw new IllegalStateException(element.toString());
      }

    } else if (element instanceof TestSuiteElement) {
      switch (((TestSuiteElement) element).getResultStatus(true)) {
        case SKIPPED:
        case UNDEFINED:
          return imageProvider.getSuiteIcon();
        case ERROR:
          return imageProvider.getSuiteErrorIcon();
        case FAILURE:
          return imageProvider.getSuiteFailIcon();
        case OK:
          return imageProvider.getSuiteOkIcon();
        default:
          throw new IllegalStateException(element.toString());
      }
    } else {
      throw new IllegalArgumentException(String.valueOf(element));
    }
  }

  public void setShowTime(boolean showTime) {
    fShowTime = showTime;
    fireLabelProviderChanged(new LabelProviderChangedEvent(this));
  }
}
