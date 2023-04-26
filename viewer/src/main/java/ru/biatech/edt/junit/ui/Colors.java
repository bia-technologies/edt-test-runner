/*******************************************************************************
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import ru.biatech.edt.junit.TestViewerPlugin;
import ru.biatech.edt.junit.ui.stacktrace.events.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Помощник по работе с цветами среды исполения
 * Возвращяет актуальные цвета с учетом темы оформления
 */
public class Colors {

  private final static List<Listener> listeners = new ArrayList<>();

  /**
   * Основной цвет фона
   */
  public static volatile Color BG_COLOR = new Color(255, 255, 225);

  /**
   * Основной цвет текста
   */
  public static volatile Color FG_COLOR = new Color(0, 0, 0);

  /**
   * Цвет фона выбранного элемента
   */
  public static volatile Color SELECTION_BG_COLOR = new Color(0, 0, 250);

  /**
   * Цвет текста выбранного элемента
   */
  public static volatile Color SELECTION_FG_COLOR = new Color(0, 0, 250);

  /**
   * Цвет фона элемента, над которым находится курсор
   */
  public static volatile Color HOVER_BG_COLOR = new Color(0, 0, 250);

  static {
    final var display = Display.getDefault();
    if (display != null && !display.isDisposed()) {
      updateColors();
      try {
        display.asyncExec(() -> {
          installColorUpdater(display);
        });
      } catch (SWTError err) {
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=45294
        if (err.code != SWT.ERROR_DEVICE_DISPOSED)
          throw err;
      }
    }
  }

  public static void addColorChangedListener(Listener listener) {
    listeners.add(listener);
  }

  public static void removeColorChangedListener(Listener listener) {
    listeners.remove(listener);
  }

  public static void updateColors() {
    var registry = JFaceResources.getColorRegistry();
    var display = Display.getCurrent();
    BG_COLOR = registry.get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
    FG_COLOR = registry.get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
    SELECTION_BG_COLOR = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
    SELECTION_FG_COLOR = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
    HOVER_BG_COLOR = darker(BG_COLOR);
  }

  private static Color darker(Color baseColor) {
    var factor = 0.9D;
    return new Color(
        Math.max((int) ((double) baseColor.getRed() * factor), 0),
        Math.max((int) ((double) baseColor.getGreen() * factor), 0),
        Math.max((int) ((double) baseColor.getBlue() * factor), 0),
        baseColor.getAlpha());
  }

  private static void installColorUpdater(final Display display) {
    display.addListener(SWT.Settings, event -> onChanged());
    JFaceResources.getColorRegistry().addListener(event -> onChanged());
  }

  private static void onChanged() {
    TestViewerPlugin.log().debug("Colors changed");
    var oldColor = BG_COLOR;
    updateColors();
    if (!BG_COLOR.equals(oldColor)) {
      listeners.forEach(Listener::handle);
    }
  }
}
