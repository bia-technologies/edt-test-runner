/*******************************************************************************
 * Copyright (c) 2022-2024 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.viewsupport;

import lombok.Getter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
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
  public static final String LOGO = "eview16/logo.png"; //$NON-NLS-1$
  // SUITE
  public static final String SUITE_ICON = "obj16/tsuite.png"; //$NON-NLS-1$
  public static final String SUITE_OK_ICON = "obj16/tsuiteok.png"; //$NON-NLS-1$
  public static final String SUITE_ERROR_ICON = "obj16/tsuiteerror.png"; //$NON-NLS-1$
  public static final String SUITE_FAIL_ICON = "obj16/tsuitefail.png"; //$NON-NLS-1$
  public static final String SUITE_RUNNING_ICON = "obj16/tsuiterun.png"; //$NON-NLS-1$

  // TEST
  public static final String TEST_ICON = "test-state/testError.png"; //$NON-NLS-1$
  public static final String TEST_OK_ICON = "test-state/testPassed.png"; //$NON-NLS-1$
  public static final String TEST_ERROR_ICON = "test-state/testError.png"; //$NON-NLS-1$
  public static final String TEST_FAIL_ICON = "test-state/testFailed.png"; //$NON-NLS-1$
  public static final String TEST_IGNORED_ICON = "test-state/testIgnored.png"; //$NON-NLS-1$
  public static final String TEST_SKIPPED_ICON = "test-state/testSkipped.png"; //$NON-NLS-1$

  // COMMON
  public static final String FAILURES_ICON = "obj16/failures.png"; //$NON-NLS-1$
  public static final String STACK_ICON = "obj16/stkfrm_obj.png"; //$NON-NLS-1$
  public static final String ERROR_ICON = "obj16/error.png"; //$NON-NLS-1$
  public static final String EVENT_ICON = "obj16/event.png"; //$NON-NLS-1$

  // ACTIONS
  public static final String ACTION_NEW_TESTCASE = "actions16/item-add.png"; //$NON-NLS-1$
  public static final String ACTION_NEW_TESTSUITE = "actions16/list-add.png"; //$NON-NLS-1$
  public static final String ACTION_RUN_TEST = "actions16/run.png"; //$NON-NLS-1$
  public static final String ACTION_DEBUG_TEST = "actions16/debug.png"; //$NON-NLS-1$
  public static final String ACTION_GOTO_TESTSUITE = "actions16/goto-test-suite.png"; //$NON-NLS-1$
  public static final String ACTION_GOTO_TESTCASE = "actions16/goto-test-case.png"; //$NON-NLS-1$
  public static final String ACTION_GOTO_ERROR = "actions16/goto-error.png"; //$NON-NLS-1$
  public static final String ACTION_GOTO_METHOD = "actions16/goto-method.png"; //$NON-NLS-1$
  public static final String ACTION_COMPARE = "actions16/compare.png"; //$NON-NLS-1$
  public static final String ACTION_COMPARE_DISABLED = "actions16/compare-disable.png"; //$NON-NLS-1$
  public static final String ACTION_NEW_MOCK = "actions16/mock.png"; //$NON-NLS-1$
  public static final String ACTION_NEW_EVENT_MOCK = "actions16/event_mock.png"; //$NON-NLS-1$

  public static final String ICONS_COLLAPSE_ALL = "elcl16/collapseall.png"; //$NON-NLS-1$
  public static final String ICONS_EXPAND_ALL = "elcl16/expandall.png"; //$NON-NLS-1$

  // OVERLAY
  public static final String OVERLAY_ERROR = "ovr16/error_ovr.png"; //$NON-NLS-1$
  public static final String OVERLAY_FAILED = "ovr16/failed_ovr.png"; //$NON-NLS-1$
  public static final String OVERLAY_SUCCESS = "ovr16/success_ovr.png"; //$NON-NLS-1$

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
  private final List<Image> imagesToDispose = new ArrayList<>();
  @Getter(lazy = true)
  private final Image logo = createManagedImage(LOGO);
  @Getter(lazy = true)
  private final Image inactiveLogo = createGrayManagedImage(getLogo());
  @Getter(lazy = true)
  private final Image testRunOKIcon = createOverlayIcon(getLogo(), OVERLAY_SUCCESS);
  @Getter(lazy = true)
  private final Image testRunFailIcon = createOverlayIcon(getLogo(), OVERLAY_FAILED);
  @Getter(lazy = true)
  private final Image testRunErrorIcon = createOverlayIcon(getLogo(), OVERLAY_ERROR);
  @Getter(lazy = true)
  private final Image testIcon = createManagedImage(TEST_ICON);
  @Getter(lazy = true)
  private final Image testOkIcon = createManagedImage(TEST_OK_ICON);
  @Getter(lazy = true)
  private final Image testErrorIcon = createManagedImage(TEST_ERROR_ICON);
  @Getter(lazy = true)
  private final Image testFailIcon = createManagedImage(TEST_FAIL_ICON);
  @Getter(lazy = true)
  private final Image testSkippedIcon = createManagedImage(TEST_SKIPPED_ICON);
  @Getter(lazy = true)
  private final Image testIgnoredIcon = createManagedImage(TEST_IGNORED_ICON);
  @Getter(lazy = true)
  private final Image stackViewIcon = createManagedImage(STACK_ICON);
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
  private final Image messageIcon = createManagedImage(getSharedImage(ISharedImages.IMG_OBJS_INFO_TSK));
  @Getter(lazy = true)
  private final Image eventIcon = createManagedImage(EVENT_ICON);

  // ACTIONS
  @Getter(lazy = true)
  private final Image runTestIcon = createManagedImage(ACTION_RUN_TEST);
  @Getter(lazy = true)
  private final Image debugTestIcon = createManagedImage(ACTION_DEBUG_TEST);
  @Getter(lazy = true)
  private final Image gotoTestSuite = createManagedImage(ACTION_GOTO_TESTSUITE);
  @Getter(lazy = true)
  private final Image gotoTestCase = createManagedImage(ACTION_GOTO_TESTCASE);
  @Getter(lazy = true)
  private final Image gotoError = createManagedImage(ACTION_GOTO_ERROR);
  @Getter(lazy = true)
  private final Image newTestSuite = createManagedImage(ACTION_NEW_TESTSUITE);
  @Getter(lazy = true)
  private final Image actionNewTestCase = createManagedImage(ACTION_NEW_TESTCASE);
  @Getter(lazy = true)
  private final Image actionNewMock = createManagedImage(ACTION_NEW_MOCK);
  @Getter(lazy = true)
  private final Image actionNewEventMock = createManagedImage(ACTION_NEW_EVENT_MOCK);

  public static ImageDescriptor getImageDescriptor(String relativePath) {
    return TestViewerPlugin.getDefault().createImageDescriptor(relativePath, true);
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

  private Image createGrayManagedImage(Image base) {
    var image = new Image(Display.getDefault(), base, SWT.IMAGE_GRAY);
    imagesToDispose.add(image);
    return image;
  }

  private Image createOverlayIcon(Image base, String second) {
    return createOverlayIcon(base, getImageDescriptor(second));
  }

  private Image createOverlayIcon(Image base, ImageDescriptor second) {
    return new DecorationOverlayIcon(base, second, IDecoration.BOTTOM_RIGHT).createImage();
  }
}
