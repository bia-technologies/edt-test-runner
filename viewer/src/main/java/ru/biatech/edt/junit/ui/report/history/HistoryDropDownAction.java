/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * Copyright (c) 2023 BIA-Technologies Limited Liability Company.
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
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation
 * 			(report 36180: Callers/Callees view)
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/
package ru.biatech.edt.junit.ui.report.history;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

import java.util.List;

class HistoryDropDownAction<E> extends Action {

  private class HistoryAction extends Action {
    private final E fElement;

    public HistoryAction(E element, int accelerator) {
      super("", AS_RADIO_BUTTON); //$NON-NLS-1$
      Assert.isNotNull(element);
      fElement = element;

      String label = fHistory.getText(element);
      if (accelerator < 10) {
        label = String.format("&%s %s", accelerator, label);
      }

      setText(label);
      setImageDescriptor(fHistory.getImageDescriptor(element));
    }

    @Override
    public void run() {
      if (isChecked()) {
        fHistory.setActiveEntry(fElement);
      }
    }
  }

  private class HistoryMenuCreator implements IMenuCreator {

    @Override
    public Menu getMenu(Menu parent) {
      return null;
    }

    @Override
    public Menu getMenu(Control parent) {
      if (fMenu != null) {
        fMenu.dispose();
      }
      final MenuManager manager = new MenuManager();
      manager.setRemoveAllWhenShown(true);
      manager.addMenuListener(new IMenuListener() {
        @Override
        public void menuAboutToShow(IMenuManager manager2) {
          if (fHistory == null) {
            return;
          }
          List<E> entries = fHistory.getHistoryEntries();
          addEntryMenuItems(manager2, entries);

          manager2.add(new Separator());

          Action clearAction = fHistory.getClearAction();
          if (clearAction != null) {
            manager2.add(clearAction);
          }

          manager2.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

          fHistory.addMenuEntries(manager);
        }

        private void addEntryMenuItems(IMenuManager manager2, List<E> entries) {
          if (entries.isEmpty()) {
            return;
          }

          int min = Math.min(entries.size(), RESULTS_IN_DROP_DOWN);
          for (int i = 0; i < min; i++) {
            E entry = entries.get(i);
            HistoryAction action = new HistoryAction(entry, i + 1);
            boolean check = entry.equals(fHistory.getCurrentEntry());
            action.setChecked(check);
            manager2.add(action);
          }
        }
      });

      fMenu = manager.createContextMenu(parent);

      //workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=129973
      final Display display = parent.getDisplay();
      fMenu.addMenuListener(new MenuAdapter() {
        @Override
        public void menuHidden(final MenuEvent e) {
          display.asyncExec(() -> {
            manager.removeAll();
            if (fMenu != null) {
              fMenu.dispose();
              fMenu = null;
            }
          });
        }
      });
      return fMenu;
    }

    @Override
    public void dispose() {
      fHistory = null;

      if (fMenu != null) {
        fMenu.dispose();
        fMenu = null;
      }
    }
  }

  public static final int RESULTS_IN_DROP_DOWN = 10;

  private ViewHistory<E> fHistory;
  private Menu fMenu;

  public HistoryDropDownAction(ViewHistory<E> history) {
    fHistory = history;
    fMenu = null;
    setMenuCreator(new HistoryMenuCreator());
    fHistory.configureHistoryDropDownAction(this);
  }
}