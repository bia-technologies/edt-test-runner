/*******************************************************************************
 * Copyright (c) 2022 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import ru.biatech.edt.junit.TestViewerPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Служебный класс для работы с иконками
 */
public class ImageProvider {
  // SUITE
  public static final String SUITE_ICON = "obj16/tsuite.png"; //$NON-NLS-1$
  public static final String SUITE_OK_ICON = "obj16/tsuiteok.png"; //$NON-NLS-1$
  public static final String SUITE_ERROR_ICON = "obj16/tsuiteerror.png"; //$NON-NLS-1$
  public static final String SUITE_FAIL_ICON = "obj16/tsuitefail.png"; //$NON-NLS-1$
  public static final String SUITE_RUNNING_ICON = "obj16/tsuiterun.png"; //$NON-NLS-1$

  // TEST RUN
  public static final String TEST_RUN_OK_ICON = "eview16/junitsucc.png"; //$NON-NLS-1$
  public static final String TEST_RUN_FAIL_ICON = "eview16/juniterr.png"; //$NON-NLS-1$

  // TEST
  public static final String TEST_ICON = "obj16/test.png"; //$NON-NLS-1$
  public static final String TEST_OK_ICON = "obj16/testok.png"; //$NON-NLS-1$
  public static final String TEST_ERROR_ICON = "obj16/testerr.png"; //$NON-NLS-1$
  public static final String TEST_FAIL_ICON = "obj16/testfail.png"; //$NON-NLS-1$
  public static final String TEST_RUNNING_ICON = "obj16/testrun.png"; //$NON-NLS-1$
  public static final String TEST_IGNORED_ICON = "obj16/testignored.png"; //$NON-NLS-1$
  public static final String TEST_ASSUMPTION_FAILURE_ICON = "obj16/testassumptionfailed.png"; //$NON-NLS-1$

  // COMMON
  public static final String FAILURES_ICON = "obj16/failures.png"; //$NON-NLS-1$
  public static final String EXCEPTION_ICON = "obj16/exc_catch.png"; //$NON-NLS-1$
  public static final String STACK_ICON = "obj16/stkfrm_obj.png"; //$NON-NLS-1$
  public static final String ERROR_ICON = "obj16/error.png"; //$NON-NLS-1$
  public static final String TARGET_ICON = "obj16/error.png"; //$NON-NLS-1$
  public static final String GOTO_ICON = "obj16/goto.png"; //$NON-NLS-1$

  private final Image fStackViewIcon;
  private final Image fTestRunOKIcon;
  private final Image fTestRunFailIcon;
  private final Image fTestIcon;
  private final Image fTestOkIcon;
  private final Image fTestErrorIcon;
  private final Image fTestFailIcon;
  private final Image fTestAssumptionFailureIcon;
  private final Image fTestRunningIcon;
  private final Image fTestIgnoredIcon;
  private final Image fSuiteIcon;
  private final Image fSuiteOkIcon;
  private final Image fSuiteErrorIcon;
  private final Image fSuiteFailIcon;
  private final Image fSuiteRunningIcon;

  private final ImageDescriptor fSuiteIconDescriptor = getImageDescriptor(SUITE_ICON);
  private final ImageDescriptor fSuiteOkIconDescriptor = getImageDescriptor(SUITE_OK_ICON);
  private final ImageDescriptor fSuiteErrorIconDescriptor = getImageDescriptor(SUITE_ERROR_ICON);
  private final ImageDescriptor fSuiteFailIconDescriptor = getImageDescriptor(SUITE_FAIL_ICON);
  private final ImageDescriptor fSuiteRunningIconDescriptor = getImageDescriptor(SUITE_RUNNING_ICON);
  private final List<Image> fImagesToDispose;

  public static ImageDescriptor getImageDescriptor(String relativePath) {
    return TestViewerPlugin.ui().getImageDescriptor(relativePath);
  }

  public static ImageDescriptor getSharedImage(String imageID) {
    IWorkbench workbench = PlatformUI.getWorkbench();
    return workbench.getSharedImages().getImageDescriptor(imageID);
  }

  public ImageProvider() {
    fImagesToDispose = new ArrayList<>();

    fStackViewIcon = createManagedImage(STACK_ICON);
    fTestRunOKIcon = createManagedImage(TEST_RUN_OK_ICON);
    fTestRunFailIcon = createManagedImage(TEST_RUN_FAIL_ICON);

    fTestIcon = createManagedImage(TEST_ICON);
    fTestOkIcon = createManagedImage(TEST_OK_ICON);
    fTestErrorIcon = createManagedImage(TEST_ERROR_ICON);
    fTestFailIcon = createManagedImage(TEST_FAIL_ICON);
    fTestRunningIcon = createManagedImage(TEST_RUNNING_ICON);
    fTestIgnoredIcon = createManagedImage(TEST_IGNORED_ICON);
    fTestAssumptionFailureIcon = createManagedImage(TEST_ASSUMPTION_FAILURE_ICON);

    fSuiteIcon = createManagedImage(fSuiteIconDescriptor);
    fSuiteOkIcon = createManagedImage(fSuiteOkIconDescriptor);
    fSuiteErrorIcon = createManagedImage(fSuiteErrorIconDescriptor);
    fSuiteFailIcon = createManagedImage(fSuiteFailIconDescriptor);
    fSuiteRunningIcon = createManagedImage(fSuiteRunningIconDescriptor);

  }

  public void dispose() {
    for (Image imageToDispose : fImagesToDispose) {
      imageToDispose.dispose();
    }
  }

  public Image getStackViewIcon() {
    return fStackViewIcon;
  }

  public Image getTestRunOKIcon() {
    return fTestRunOKIcon;
  }

  public Image getTestRunFailIcon() {
    return fTestRunFailIcon;
  }

  public Image getTestIcon() {
    return fTestIcon;
  }

  public Image getTestOkIcon() {
    return fTestOkIcon;
  }

  public Image getTestErrorIcon() {
    return fTestErrorIcon;
  }

  public Image getTestFailIcon() {
    return fTestFailIcon;
  }

  public Image getTestAssumptionFailureIcon() {
    return fTestAssumptionFailureIcon;
  }

  public Image getTestRunningIcon() {
    return fTestRunningIcon;
  }

  public Image getTestIgnoredIcon() {
    return fTestIgnoredIcon;
  }

  public ImageDescriptor getSuiteIconDescriptor() {
    return fSuiteIconDescriptor;
  }

  public ImageDescriptor getSuiteOkIconDescriptor() {
    return fSuiteOkIconDescriptor;
  }

  public ImageDescriptor getSuiteErrorIconDescriptor() {
    return fSuiteErrorIconDescriptor;
  }

  public ImageDescriptor getSuiteFailIconDescriptor() {
    return fSuiteFailIconDescriptor;
  }

  public ImageDescriptor getSuiteRunningIconDescriptor() {
    return fSuiteRunningIconDescriptor;
  }

  public Image getSuiteIcon() {
    return fSuiteIcon;
  }

  public Image getSuiteOkIcon() {
    return fSuiteOkIcon;
  }

  public Image getSuiteErrorIcon() {
    return fSuiteErrorIcon;
  }

  public Image getSuiteFailIcon() {
    return fSuiteFailIcon;
  }

  public Image getSuiteRunningIcon() {
    return fSuiteRunningIcon;
  }

  private Image createManagedImage(ImageDescriptor descriptor) {
    Image image = descriptor.createImage();
    if (image == null) {
      image = ImageDescriptor.getMissingImageDescriptor().createImage();
    }
    fImagesToDispose.add(image);
    return image;
  }

  private Image createManagedImage(String path) {
    return createManagedImage(getImageDescriptor(path));
  }
}
