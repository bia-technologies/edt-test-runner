/*******************************************************************************
 * Copyright (c) 2025 BIA-Technologies Limited Liability Company.
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

package ru.biatech.edt.junit.ui.report;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.ui.IMemento;
import ru.biatech.edt.junit.Preferences;

@Getter
public class ReportSettings {
  // TODO В будущем сделать рефакторинг вынеся в самостоятельный класс
  private static final String TAG_RATIO = "ratio"; //$NON-NLS-1$
  private static final String TAG_ORIENTATION = "orientation"; //$NON-NLS-1$
  private static final String TAG_SCROLL = "scroll"; //$NON-NLS-1$
  private static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$
  private static final String TAG_FAILURES_ONLY = "failuresOnly"; //$NON-NLS-1$
  private static final String TAG_IGNORED_ONLY = "ignoredOnly"; //$NON-NLS-1$
  private static final String TAG_SHOW_TIME = "time"; //$NON-NLS-1$
  private static final String TAG_SORTING_CRITERION = "sortingCriterion"; //$NON-NLS-1$
  private static final String TAG_WEB_STACKTRACE = "webStackTrace"; //$NON-NLS-1$
  private final SettingsEventListener listener;
  @Setter
  IMemento memento;
  /**
   * Whether the output scrolls and reveals tests as they are executed.
   */
  @Setter
  private boolean autoScroll;
  @Setter
  private boolean showOnErrorOnly;
  private boolean failuresOnly;
  private boolean ignoredOnly;
  private boolean htmlStackTrace;
  private boolean showExecutionTime;
  @Setter
  private int ratio;
  /**
   * The current orientation; either <code>VIEW_ORIENTATION_HORIZONTAL</code>
   * <code>VIEW_ORIENTATION_VERTICAL</code>, or <code>VIEW_ORIENTATION_AUTOMATIC</code>.
   */
  private int orientation = TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC;
  /**
   * The current layout mode (LAYOUT_FLAT or LAYOUT_HIERARCHICAL).
   */
  @Getter
  private int layoutMode = TestRunnerViewPart.LAYOUT_HIERARCHICAL;
  /**
   * The current sorting criterion.
   */
  private TestRunnerViewPart.SortingCriterion sortingCriterion = TestRunnerViewPart.SortingCriterion.SORT_BY_EXECUTION_ORDER;

  public ReportSettings(TestRunnerViewPart testRunnerViewPart) {
    this.listener = testRunnerViewPart;
  }

  private static <T> T defaultValue(T value, T def) {
    return value != null ? value : def;
  }

  public void setShowFailuresOnly(boolean value) {
    if (failuresOnly == value) {
      return;
    }
    failuresOnly = value;
    listener.onShowFailuresOnly(value);
  }

  public void setShowIgnoredOnly(boolean value) {
    if (ignoredOnly == value) {
      return;
    }
    ignoredOnly = value;
    listener.onShowIgnoredOnlyChanged(value);
  }

  public void setHtmlStackTrace(boolean value) {
    if (htmlStackTrace == value) {
      return;
    }
    htmlStackTrace = value;
    listener.ontHtmlStackTraceChanged(value);
  }

  public void setShowExecutionTime(boolean value) {
    if (showExecutionTime == value) {
      return;
    }
    showExecutionTime = value;
    listener.onShowExecutionTimeChanged(value);
  }

  public void setLayoutMode(int value) {
    if (layoutMode == value) {
      return;
    }
    layoutMode = value;
    listener.onLayoutModeChanged(value);
  }

  public void setOrientation(int value) {
    if (this.orientation == value) {
      return;
    }
    this.orientation = value;
    listener.onOrientationChanged(value);
  }

  public void setSortingCriterion(TestRunnerViewPart.SortingCriterion value) {
    if (this.sortingCriterion == value) {
      return;
    }
    this.sortingCriterion = value;
    listener.onSortingCriterionChanged(value);
  }

  public void saveState(IMemento memento) {
    if (this.memento != null) {
      // Restore old values
      memento.putMemento(this.memento);
      return;
    }
    memento.putBoolean(TAG_FAILURES_ONLY, isFailuresOnly());
    memento.putBoolean(TAG_IGNORED_ONLY, isIgnoredOnly());

    memento.putInteger(TAG_LAYOUT, getLayoutMode());
    memento.putInteger(TAG_ORIENTATION, getOrientation());
    memento.putInteger(TAG_RATIO, getRatio());

    memento.putBoolean(TAG_SCROLL, isAutoScroll());
    memento.putInteger(TAG_SORTING_CRITERION, getSortingCriterion().ordinal());

    memento.putBoolean(TAG_SHOW_TIME, isShowExecutionTime());
    memento.putBoolean(TAG_WEB_STACKTRACE, isHtmlStackTrace());
  }

  public void restoreLayoutState() {
    setShowFailuresOnly(defaultValue(memento.getBoolean(TAG_FAILURES_ONLY), false));
    setShowIgnoredOnly(defaultValue(memento.getBoolean(TAG_IGNORED_ONLY), false));

    setLayoutMode(defaultValue(memento.getInteger(TAG_LAYOUT), TestRunnerViewPart.LAYOUT_HIERARCHICAL));
    setOrientation(defaultValue(memento.getInteger(TAG_ORIENTATION), TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC));
    setRatio(defaultValue(memento.getInteger(TAG_RATIO), 300));

    setAutoScroll(defaultValue(memento.getBoolean(TAG_SCROLL), false));
    var sort = defaultValue(memento.getInteger(TAG_SORTING_CRITERION), 1);
    setSortingCriterion(TestRunnerViewPart.SortingCriterion.values()[sort]);

    setShowExecutionTime(defaultValue(memento.getBoolean(TAG_SHOW_TIME), true));
    setHtmlStackTrace(defaultValue(memento.getBoolean(TAG_WEB_STACKTRACE), true));
    showOnErrorOnly = Preferences.getShowOnErrorOnly();

    memento = null;
  }
}