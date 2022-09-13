///*******************************************************************************
// * Copyright (c) 2000, 2011 IBM Corporation and others.
// *
// * This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License 2.0
// * which accompanies this distribution, and is available at
// * https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation
// *          (report 36180: Callers/Callees view)
// *******************************************************************************/
//package ru.biatech.edt.junit.ui.viewsupport;
// TODO Возможно стоит реанимировать
//import java.util.HashMap;
//import java.util.List;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Image;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.core.runtime.IStatus;
//
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.StatusDialog;
//import org.eclipse.jface.resource.ImageDescriptor;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.jface.window.Window;
//
//import org.eclipse.jdt.internal.corext.util.Messages;
//
//import org.eclipse.jdt.internal.ui.JavaUIMessages;
//import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
//import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
//
///*package*/ class HistoryListAction<E> extends Action {
//
//	private class HistoryListDialog extends StatusDialog {
//		private static final int MAX_MAX_ENTRIES= 100;
//		private ListDialogField<E> fHistoryList;
//		private StringDialogField fMaxEntriesField;
//		private int fMaxEntries;
//
//		private E fResult;
//
//		private HistoryListDialog() {
//			super(fHistory.getShell());
//			setTitle(fHistory.getHistoryListDialogTitle());
//
//			createHistoryList();
//			createMaxEntriesField();
//			setHelpAvailable(false);
//		}
//
//		/*
//		 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
//		 * @since 3.4
//		 */
//		@Override
//		protected boolean isResizable() {
//			return true;
//		}
//
//		private void createHistoryList() {
//			IListAdapter<E> adapter= new IListAdapter<E>() {
//				@Override
//				public void customButtonPressed(ListDialogField<E> field, int index) {
//					doCustomButtonPressed(index);
//				}
//				@Override
//				public void selectionChanged(ListDialogField<E> field) {
//					doSelectionChanged();
//				}
//
//				@Override
//				public void doubleClicked(ListDialogField<E> field) {
//					doDoubleClicked();
//				}
//			};
//			String[] buttonLabels= new String[] { JavaUIMessages.HistoryListAction_remove, JavaUIMessages.HistoryListAction_remove_all };
//			LabelProvider labelProvider= new TestRunLabelProvider();
//			fHistoryList= new ListDialogField<>(adapter, buttonLabels, labelProvider);
//			fHistoryList.setLabelText(fHistory.getHistoryListDialogMessage());
//
//			List<E> historyEntries= fHistory.getHistoryEntries();
//			fHistoryList.setElements(historyEntries);
//
//			Object currentEntry= fHistory.getCurrentEntry();
//			ISelection sel;
//			if (currentEntry != null) {
//				sel= new StructuredSelection(currentEntry);
//			} else {
//				sel= new StructuredSelection();
//			}
//			fHistoryList.selectElements(sel);
//		}
//
//		private void createMaxEntriesField() {
//			fMaxEntriesField= new StringDialogField();
//			fMaxEntriesField.setLabelText(fHistory.getMaxEntriesMessage());
//			fMaxEntriesField.setDialogFieldListener(field -> {
//				String maxString= fMaxEntriesField.getText();
//				boolean valid;
//				try {
//					fMaxEntries= Integer.parseInt(maxString);
//					valid= fMaxEntries > 0 && fMaxEntries < MAX_MAX_ENTRIES;
//				} catch (NumberFormatException e) {
//					valid= false;
//				}
//				if (valid)
//					updateStatus(StatusInfo.OK_STATUS);
//				else
//					updateStatus(new StatusInfo(IStatus.ERROR, Messages.format(JavaUIMessages.HistoryListAction_max_entries_constraint, Integer.toString(MAX_MAX_ENTRIES))));
//			});
//			fMaxEntriesField.setText(Integer.toString(fHistory.getMaxEntries()));
//		}
//
//		/*
//		 * @see Dialog#createDialogArea(Composite)
//		 */
//		@Override
//		protected Control createDialogArea(Composite parent) {
//			initializeDialogUnits(parent);
//
//			Composite composite= (Composite) super.createDialogArea(parent);
//
//			Composite inner= new Composite(composite, SWT.NONE);
//			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
//			inner.setFont(composite.getFont());
//
//			LayoutUtil.doDefaultLayout(inner, new DialogField[] { fHistoryList, new Separator() }, true);
//			LayoutUtil.setHeightHint(fHistoryList.getListControl(null), convertHeightInCharsToPixels(12));
//			LayoutUtil.setHorizontalGrabbing(fHistoryList.getListControl(null));
//
//			Composite additionalControls= new Composite(inner, SWT.NONE);
//			additionalControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//			LayoutUtil.doDefaultLayout(additionalControls, new DialogField[] { fMaxEntriesField }, false);
//			LayoutUtil.setHorizontalGrabbing(fMaxEntriesField.getTextControl(null));
//
//			applyDialogFont(composite);
//			return composite;
//		}
//
//		private void doCustomButtonPressed(int index) {
//			switch (index) {
//				case 0: // remove
//					fHistoryList.removeElements(fHistoryList.getSelectedElements());
//					fHistoryList.selectFirstElement();
//					break;
//
//				case 1: // remove all
//					fHistoryList.removeAllElements();
//					break;
//				default:
//					break;
//			}
//		}
//
//		private void doDoubleClicked() {
//			okPressed();
//		}
//
//		private void doSelectionChanged() {
//			List<E> selected= fHistoryList.getSelectedElements();
//			if (selected.size() >= 1) {
//				fResult= selected.get(0);
//			} else {
//				fResult= null;
//			}
//			fHistoryList.enableButton(0, !selected.isEmpty());
//		}
//
//		public E getResult() {
//			return fResult;
//		}
//
//		public List<E> getRemaining() {
//			return fHistoryList.getElements();
//		}
//
//		public int getMaxEntries() {
//			return fMaxEntries;
//		}
//
//	}
//
//	private final class TestRunLabelProvider extends LabelProvider {
//		private final HashMap<ImageDescriptor, Image> fImages= new HashMap<>();
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public String getText(Object element) {
//			return fHistory.getText((E) element);
//		}
//
//		@Override
//		public Image getImage(Object element) {
//			ImageDescriptor imageDescriptor= fHistory.getImageDescriptor(element);
//			return getCachedImage(imageDescriptor);
//		}
//
//		private Image getCachedImage(ImageDescriptor imageDescriptor) {
//			Object cached= fImages.get(imageDescriptor);
//			if (cached != null)
//				return (Image) cached;
//			Image image= imageDescriptor.createImage(fHistory.getShell().getDisplay());
//			fImages.put(imageDescriptor, image);
//			return image;
//		}
//
//		@Override
//		public void dispose() {
//			for (Image image : fImages.values()) {
//				image.dispose();
//			}
//			fImages.clear();
//		}
//	}
//
//	private ViewHistory<E> fHistory;
//
//	public HistoryListAction(ViewHistory<E> history) {
//		super(null, IAction.AS_RADIO_BUTTON);
//		fHistory= history;
//		fHistory.configureHistoryListAction(this);
//	}
//
//	/*
//	 * @see IAction#run()
//	 */
//	@Override
//	public void run() {
//		HistoryListDialog dialog= new HistoryListDialog();
//		if (dialog.open() == Window.OK) {
//			fHistory.setHistoryEntries(dialog.getRemaining(), dialog.getResult());
//			fHistory.setMaxEntries(dialog.getMaxEntries());
//		}
//	}
//
//}
