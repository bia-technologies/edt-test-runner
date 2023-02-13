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

import lombok.Getter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
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
  public static final String TARGET_ICON = "obj16/target.png"; //$NON-NLS-1$
  public static final String GOTO_ICON = "elcl16/goto_input.png"; //$NON-NLS-1$

  // ACTIONS
  public static final String RUN_TEST = "etool16/run_exc.png"; //$NON-NLS-1$
  public static final String DEBUG_TEST = "etool16/debug_exc.png"; //$NON-NLS-1$
  public static final String UNDER_TEST = "elcl16/under_test.png"; //$NON-NLS-1$

  @Getter
  private final ImageDescriptor suiteIconDescriptor = getImageDescriptor(SUITE_ICON);

  @Getter
  private final ImageDescriptor suiteOkIconDescriptor = getImageDescriptor(SUITE_OK_ICON);

  @Getter
  private final ImageDescriptor suiteErrorIconDescriptor = getImageDescriptor(SUITE_ERROR_ICON);

  @Getter
  private final ImageDescriptor suiteFailIconDescriptor = getImageDescriptor(SUITE_FAIL_ICON);

  @Getter
  private final ImageDescriptor suiteRunningIconDescriptor = getImageDescriptor(SUITE_RUNNING_ICON);

  @Getter(lazy = true)
  private final Image stackViewIcon = createManagedImage(STACK_ICON);

  @Getter(lazy = true)
  private final Image testRunOKIcon = createManagedImage(TEST_RUN_OK_ICON);

  @Getter(lazy = true)
  private final Image testRunFailIcon = createManagedImage(TEST_RUN_FAIL_ICON);

  @Getter(lazy = true)
  private final Image testIcon = createManagedImage(TEST_ICON);

  @Getter(lazy = true)
  private final Image testOkIcon = createManagedImage(TEST_OK_ICON);

  @Getter(lazy = true)
  private final Image testErrorIcon = createManagedImage(TEST_ERROR_ICON);

  @Getter(lazy = true)
  private final Image testFailIcon = createManagedImage(TEST_FAIL_ICON);

  @Getter(lazy = true)
  private final Image testAssumptionFailureIcon = createManagedImage(TEST_ASSUMPTION_FAILURE_ICON);

  @Getter(lazy = true)
  private final Image testRunningIcon = createManagedImage(TEST_RUNNING_ICON);

  @Getter(lazy = true)
  private final Image testIgnoredIcon = createManagedImage(TEST_IGNORED_ICON);

  @Getter(lazy = true)
  private final Image suiteIcon = createManagedImage(suiteIconDescriptor);

  @Getter(lazy = true)
  private final Image suiteOkIcon = createManagedImage(suiteOkIconDescriptor);

  @Getter(lazy = true)
  private final Image suiteErrorIcon = createManagedImage(suiteErrorIconDescriptor);

  @Getter(lazy = true)
  private final Image suiteFailIcon = createManagedImage(suiteFailIconDescriptor);

  @Getter(lazy = true)
  private final Image suiteRunningIcon = createManagedImage(suiteRunningIconDescriptor);

  @Getter(lazy = true)
  private final Image errorIcon = createManagedImage(ERROR_ICON);

  @Getter(lazy = true)
  private final Image stackIcon = createManagedImage(STACK_ICON);

  @Getter(lazy = true)
  private final Image targetIcon = createManagedImage(TARGET_ICON);

  @Getter(lazy = true)
  private final Image messageIcon = createManagedImage(getSharedImage(ISharedImages.IMG_OBJS_INFO_TSK));

  @Getter(lazy = true)
  private final Image runTestIcon = createManagedImage(RUN_TEST);

  @Getter(lazy = true)
  private final Image debugTestIcon = createManagedImage(DEBUG_TEST);

  @Getter(lazy = true)
  private final Image gotoIcon = createManagedImage(GOTO_ICON);

  private final List<Image> imagesToDispose = new ArrayList<>();

  public static ImageDescriptor getImageDescriptor(String relativePath) {
    return TestViewerPlugin.ui().getImageDescriptor(relativePath);
  }

  public static ImageDescriptor getSharedImage(String imageID) {
    IWorkbench workbench = PlatformUI.getWorkbench();
    return workbench.getSharedImages().getImageDescriptor(imageID);
  }

  public void dispose() {
    for (Image imageToDispose : imagesToDispose) {
      imageToDispose.dispose();
    }
    imagesToDispose.clear();
  }

  private Image createManagedImage(ImageDescriptor descriptor) {
    Image image = descriptor.createImage();
    if (image == null) {
      image = ImageDescriptor.getMissingImageDescriptor().createImage();
    }
    imagesToDispose.add(image);
    return image;
  }

  private Image createManagedImage(String path) {
    return createManagedImage(getImageDescriptor(path));
  }
}
