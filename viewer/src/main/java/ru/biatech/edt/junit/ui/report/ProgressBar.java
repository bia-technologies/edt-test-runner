/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * Copyright (c) 2022-2023 BIA-Technologies Limited Liability Company.
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
 *     Stephan Michels, stephan@apache.org - 104944 [JUnit] Unnecessary code in JUnitProgressBar
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A progress bar with a red/green indication for success or failure.
 */
public class ProgressBar extends Canvas {
  private static final int DEFAULT_WIDTH = 160;
  private static final int DEFAULT_HEIGHT = 18;

  private int fCurrentTickCount = 0;
  private int fMaxTickCount = 0;
  private int fColorBarWidth = 0;
  private final Color fOKColor;
  private final Color fFailureColor;
  private final Color fStoppedColor;
  private boolean fError;
  private boolean fStopped = false;

  public ProgressBar(Composite parent) {
    super(parent, SWT.NONE);

    addControlListener(new ControlAdapter() {
      @Override
      public void controlResized(ControlEvent e) {
        fColorBarWidth = scale(fCurrentTickCount);
        redraw();
      }
    });
    addPaintListener(this::paint);
    Display display = parent.getDisplay();
    fFailureColor = new Color(display, 159, 63, 63);
    fOKColor = new Color(display, 95, 191, 95);
    fStoppedColor = new Color(display, 120, 120, 120);
  }

  public void setMaximum(int max) {
    fMaxTickCount = max;
  }

  public void reset() {
    fError = false;
    fStopped = false;
    fCurrentTickCount = 0;
    fMaxTickCount = 0;
    fColorBarWidth = 0;
    redraw();
  }

  public void reset(boolean hasErrors, boolean stopped, int ticksDone, int maximum) {
    boolean noChange = fError == hasErrors && fStopped == stopped && fCurrentTickCount == ticksDone && fMaxTickCount == maximum;
    fError = hasErrors;
    fStopped = stopped;
    fCurrentTickCount = ticksDone;
    fMaxTickCount = maximum;
    fColorBarWidth = scale(ticksDone);
    if (!noChange)
      redraw();
  }

  private void paintStep(int startX, int endX) {
    GC gc = new GC(this);
    setStatusColor(gc);
    Rectangle rect = getClientArea();
    startX = Math.max(1, startX);
    gc.fillRectangle(startX, 1, endX - startX, rect.height - 2);
    gc.dispose();
  }

  private void setStatusColor(GC gc) {
    if (fStopped)
      gc.setBackground(fStoppedColor);
    else if (fError)
      gc.setBackground(fFailureColor);
    else
      gc.setBackground(fOKColor);
  }

  private int scale(int value) {
    if (fMaxTickCount > 0) {
      Rectangle r = getClientArea();
      if (r.width != 0)
        return Math.max(0, value * (r.width - 2) / fMaxTickCount);
    }
    return value;
  }

  private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
    gc.setForeground(topleft);
    gc.drawLine(x, y, x + w - 1, y);
    gc.drawLine(x, y, x, y + h - 1);

    gc.setForeground(bottomright);
    gc.drawLine(x + w, y, x + w, y + h);
    gc.drawLine(x, y + h, x + w, y + h);
  }

  private void paint(PaintEvent event) {
    GC gc = event.gc;
    Display disp = getDisplay();

    Rectangle rect = getClientArea();
    gc.fillRectangle(rect);
    drawBevelRect(gc, rect.x, rect.y, rect.width - 1, rect.height - 1,
            disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
            disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));

    setStatusColor(gc);
    fColorBarWidth = Math.min(rect.width - 2, fColorBarWidth);
    gc.fillRectangle(1, 1, fColorBarWidth, rect.height - 2);
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    checkWidget();
    Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    if (wHint != SWT.DEFAULT) size.x = wHint;
    if (hHint != SWT.DEFAULT) size.y = hHint;
    return size;
  }
}