/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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

package ru.biatech.edt.junit.ui.viewsupport;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import java.util.List;


/**
 * History support for a view.
 *
 * @param <E> the type of elements managed by this history
 */
public abstract class ViewHistory<E> {

  /**
   * Configure the history List action.
   * Clients typically want to set a text and an image.
   *
   * @param action the action
   */
  public abstract void configureHistoryListAction(IAction action);

  /**
   * Configure the history drop down action.
   * Clients typically want to set a tooltip and an image.
   *
   * @param action the action
   */
  public abstract void configureHistoryDropDownAction(IAction action);

  /**
   * @return action to clear history entries, or <code>null</code>
   */
  public abstract Action getClearAction();

  public abstract String getHistoryListDialogTitle();

  public abstract String getHistoryListDialogMessage();

  public abstract Shell getShell();


  /**
   * @return An unmodifiable list of history entries, can be empty. The list
   * is sorted by age, youngest first.
   */
  public abstract List<E> getHistoryEntries();

  /**
   * @return the active entry from the history
   */
  public abstract E getCurrentEntry();

  /**
   * @param entry the entry to activate, or <code>null</code> if none should be active
   */
  public abstract void setActiveEntry(E entry);

  /**
   * @param remainingEntries all the remaining history entries, can be empty
   * @param activeEntry      the entry to activate, or <code>null</code> if none should be active
   */
  public abstract void setHistoryEntries(List<E> remainingEntries, E activeEntry);

  /**
   * @param element the element to render
   * @return the image descriptor for the given element, or <code>null</code>
   */
  public abstract ImageDescriptor getImageDescriptor(Object element);

  /**
   * @param element the element to render
   * @return the label text for the given element
   */
  public abstract String getText(E element);

  /**
   * @return a history drop down action, ready for inclusion in a view toolbar
   */
  public final IAction createHistoryDropDownAction() {
    return null;
    //return new HistoryDropDownAction<>(this);
  }

  public abstract void addMenuEntries(MenuManager manager);

  public abstract String getMaxEntriesMessage();

  public abstract int getMaxEntries();

  public abstract void setMaxEntries(int maxEntries);

}