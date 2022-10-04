///*******************************************************************************
// * Copyright (c) 2000, 2010 IBM Corporation and others.
// *
// * This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License 2.0
// * which accompanies this distribution, and is available at
// * https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//
// TODO Реанимировать команду
//
//package ru.biatech.edt.junit.ui;
//
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IField;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.IMember;
//import org.eclipse.jdt.core.IMethod;
//import org.eclipse.jdt.core.IType;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.ErrorDialog;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.IWorkbenchWindowActionDelegate;
//import org.eclipse.ui.PartInitException;
//import ru.biatech.edt.junit.TestViewerPlugin;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Shows a dialog with test methods that refer to the selection.
// */
//public class GotoReferencedTestAction implements IWorkbenchWindowActionDelegate {
//  ISelection fSelection;
//  IWorkbenchWindow fWorkbench;
//
//  private void run(IStructuredSelection selection) {
//    IJavaElement[] elements = getSelectedElements(selection);
//    if (elements.length == 0) {
//      MessageDialog.openInformation(getShell(), JUnitMessages.GotoReferencedTestAction_dialog_title, JUnitMessages.GotoReferencedTestAction_dialog_message);
//      return;
//    }
//    try {
//      run(elements);
//    } catch (CoreException e) {
//      ErrorDialog.openError(getShell(), JUnitMessages.GotoReferencedTestAction_dialog_title, JUnitMessages.GotoReferencedTestAction_dialog_error, e.getStatus());
//    }
//  }
//
//  private void runWithEditorSelection() {
////		try {
////			JavaEditor editor= getActiveEditor();
////			if (editor == null)
////				return;
////			IJavaElement element= SelectionConverter.getElementAtOffset(editor);
////			int type= element != null ? element.getElementType() : -1;
////			if (type != IJavaElement.METHOD && type != IJavaElement.TYPE) {
////		 		element= SelectionConverter.getTypeAtOffset(editor);
////		 		if (element == null) {
////					MessageDialog.openInformation(getShell(), JUnitMessages.GotoReferencedTestAction_dialog_title, JUnitMessages.GotoReferencedTestAction_dialog_error_nomethod);
////					return;
////		 		}
////			}
////			run(new IMember[] { (IMember)element });
////		} catch (CoreException e) {
////			ErrorDialog.openError(getShell(), JUnitMessages.GotoReferencedTestAction_dialog_title, JUnitMessages.GotoReferencedTestAction_dialog_error, e.getStatus());
////		}
//  }
//
//  private void run(IJavaElement[] elements) throws PartInitException, JavaModelException {
////		IJavaElement element= elements[0];
////
////		SelectionStatusDialog dialog = new TestMethodSelectionDialog(getShell(), element);
////		dialog.setTitle(JUnitMessages.GotoReferencedTestAction_selectdialog_title);
////		String msg= Messages.format(JUnitMessages.GotoReferencedTestAction_dialog_select_message, JavaElementLabels.getElementLabel(element, JavaElementLabels.ALL_DEFAULT));
////		dialog.setMessage(msg);
////
////		if (dialog.open() == Window.CANCEL)
////			return;
////
////		Object result = dialog.getFirstResult();
////		if (result == null)
////			return;
////
////		openElement((IJavaElement)result);
//  }
//
//  private void openElement(IJavaElement result) throws JavaModelException, PartInitException {
////		IEditorPart part= JavaUI.openInEditor(result); TODO
////		JavaUI.revealInEditor(part, result);
//  }
//
//  private IJavaElement[] getSelectedElements(IStructuredSelection selection) {
//    List<?> elements = selection.toList();
//    int size = elements.size();
//    if (size == 0)
//      return new IJavaElement[0];
//
//    ArrayList<IJavaElement> result = new ArrayList<>(size);
//
//    for (int i = 0; i < size; i++) {
//      Object e = elements.get(i);
//      if (e instanceof ICompilationUnit) {
//        ICompilationUnit unit = (ICompilationUnit) e;
//        IType[] types = new IType[0];
//        try {
//          types = unit.getTypes();
//        } catch (JavaModelException ex) {
//        }
//        result.addAll(Arrays.asList(types));
//      } else if (e instanceof IMethod || e instanceof IType || e instanceof IField) {
//        result.add((IMember) e);
//      } else {
//        return new IJavaElement[0];
//      }
//    }
//    return result.toArray(new IJavaElement[result.size()]);
//  }
//
//  @Override
//  public void run(IAction action) {
//    if (fSelection instanceof IStructuredSelection)
//      run((IStructuredSelection) fSelection);
//    else {
//      runWithEditorSelection();
//    }
//
//  }
//
//  @Override
//  public void selectionChanged(IAction action, ISelection selection) {
////		fSelection= selection;
////		action.setEnabled(getActiveEditor() != null);
//  }
//
//  private Shell getShell() {
//    if (fWorkbench != null)
//      return fWorkbench.getShell();
//    return TestViewerPlugin.ui().getActiveWorkbenchShell();
//  }
//
//  @Override
//  public void dispose() {
//  }
//
//  @Override
//  public void init(IWorkbenchWindow window) {
//    fWorkbench = window;
//  }
////
////	private JavaEditor getActiveEditor() {
////		IEditorPart editor= fWorkbench.getActivePage().getActiveEditor();
////		if (editor instanceof JavaEditor)
////			return (JavaEditor) editor;
////		return null;
////	}
//}
