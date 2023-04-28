/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import lombok.NonNull;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * A selection provider for view parts with more that one viewer.
 * Tracks the focus of the viewers to provide the correct selection.
 */
public class SelectionProviderMediator implements IPostSelectionProvider {

  private class InternalListener implements ISelectionChangedListener, FocusListener {
    /*
     * @see ISelectionChangedListener#selectionChanged
     */
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      doSelectionChanged(event);
    }

    /*
     * @see FocusListener#focusGained
     */
    @Override
    public void focusGained(FocusEvent e) {
      doFocusChanged(e.widget);
    }

    /*
     * @see FocusListener#focusLost
     */
    @Override
    public void focusLost(FocusEvent e) {
    }
  }

  private class InternalPostSelectionListener implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
      doPostSelectionChanged(event);
    }

  }

  private final StructuredViewer[] fViewers;

  private StructuredViewer fViewerInFocus;
  private final ListenerList<ISelectionChangedListener> fSelectionChangedListeners;
  private final ListenerList<ISelectionChangedListener> fPostSelectionChangedListeners;

  /**
   * @param viewers       All viewers that can provide a selection
   * @param viewerInFocus the viewer currently in focus or <code>null</code>
   */
  public SelectionProviderMediator(@NonNull StructuredViewer[] viewers, StructuredViewer viewerInFocus) {
    fViewers = viewers;
    InternalListener listener = new InternalListener();
    fSelectionChangedListeners = new ListenerList<>();
    fPostSelectionChangedListeners = new ListenerList<>();
    fViewerInFocus = viewerInFocus;

    for (StructuredViewer viewer : fViewers) {
      viewer.addSelectionChangedListener(listener);
      viewer.addPostSelectionChangedListener(new InternalPostSelectionListener());
      Control control = viewer.getControl();
      control.addFocusListener(listener);
    }
  }

  private void doFocusChanged(Widget control) {
    for (StructuredViewer viewer : fViewers) {
      if (viewer.getControl() == control) {
        propagateFocusChanged(viewer);
        return;
      }
    }
  }

  final void doPostSelectionChanged(SelectionChangedEvent event) {
    ISelectionProvider provider = event.getSelectionProvider();
    if (provider == fViewerInFocus) {
      firePostSelectionChanged();
    }
  }

  final void doSelectionChanged(SelectionChangedEvent event) {
    ISelectionProvider provider = event.getSelectionProvider();
    if (provider == fViewerInFocus) {
      fireSelectionChanged();
    }
  }

  final void propagateFocusChanged(StructuredViewer viewer) {
    if (viewer != fViewerInFocus) { // OK to compare by identity
      fViewerInFocus = viewer;
      fireSelectionChanged();
      firePostSelectionChanged();
    }
  }

  private void fireSelectionChanged() {
    if (fSelectionChangedListeners != null) {
      SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());

      for (ISelectionChangedListener listener : fSelectionChangedListeners) {
        listener.selectionChanged(event);
      }
    }
  }

  private void firePostSelectionChanged() {
    if (fPostSelectionChangedListeners != null) {
      SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());

      for (ISelectionChangedListener listener : fPostSelectionChangedListeners) {
        listener.selectionChanged(event);
      }
    }
  }

  /*
   * @see ISelectionProvider#addSelectionChangedListener
   */
  @Override
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    fSelectionChangedListeners.add(listener);
  }

  /*
   * @see ISelectionProvider#removeSelectionChangedListener
   */
  @Override
  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    fSelectionChangedListeners.remove(listener);
  }

  @Override
  public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
    fPostSelectionChangedListeners.add(listener);
  }


  @Override
  public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
    fPostSelectionChangedListeners.remove(listener);
  }

  /*
   * @see ISelectionProvider#getSelection
   */
  @Override
  public ISelection getSelection() {
    if (fViewerInFocus != null) {
      return fViewerInFocus.getSelection();
    }
    return StructuredSelection.EMPTY;
  }

  /*
   * @see ISelectionProvider#setSelection
   */
  @Override
  public void setSelection(ISelection selection) {
    if (fViewerInFocus != null) {
      fViewerInFocus.setSelection(selection);
    }
  }

  public void setSelection(ISelection selection, boolean reveal) {
    if (fViewerInFocus != null) {
      fViewerInFocus.setSelection(selection, reveal);
    }
  }

  /**
   * Returns the viewer in focus or null if no viewer has the focus
   *
   * @return returns the current viewer in focus
   */
  public StructuredViewer getViewerInFocus() {
    return fViewerInFocus;
  }
}