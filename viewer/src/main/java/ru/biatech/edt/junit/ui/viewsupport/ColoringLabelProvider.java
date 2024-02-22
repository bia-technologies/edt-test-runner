/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.viewsupport;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public class ColoringLabelProvider extends DecoratingStyledCellLabelProvider implements ILabelProvider {
  public ColoringLabelProvider(IStyledLabelProvider labelProvider) {
    this(labelProvider, null, null);
  }

  public ColoringLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator, IDecorationContext decorationContext) {
    super(labelProvider, decorator, decorationContext);
  }

  @Override
  public void initialize(ColumnViewer viewer, ViewerColumn column) {
    ColoredViewersManager.install(this);
    setOwnerDrawEnabled(ColoredViewersManager.showColoredLabels());

    super.initialize(viewer, column);
  }

  @Override
  public void dispose() {
    super.dispose();
    ColoredViewersManager.uninstall(this);
  }

  public void update() {
    ColumnViewer viewer = getViewer();

    if (viewer == null) {
      return;
    }

    boolean needsUpdate = false;

    boolean showColoredLabels = ColoredViewersManager.showColoredLabels();
    if (showColoredLabels != isOwnerDrawEnabled()) {
      setOwnerDrawEnabled(showColoredLabels);
      needsUpdate = true;
    } else if (showColoredLabels) {
      needsUpdate = true;
    }
    if (needsUpdate) {
      fireLabelProviderChanged(new LabelProviderChangedEvent(this));
    }
  }

  @Override
  protected StyleRange prepareStyleRange(StyleRange styleRange, boolean applyColors) {
    if (!applyColors && styleRange.background != null) {
      styleRange = super.prepareStyleRange(styleRange, applyColors);
      styleRange.borderStyle = SWT.BORDER_DOT;
      return styleRange;
    }
    return super.prepareStyleRange(styleRange, applyColors);
  }

  @Override
  public String getText(Object element) {
    return getStyledText(element).getString();
  }

}