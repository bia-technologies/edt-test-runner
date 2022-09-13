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
// TODO Реанимировать диалог
//
//package ru.biatech.edt.junit.ui.dialogs;
//
//
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.IJavaProject;
//import org.eclipse.jdt.core.IType;
//import org.eclipse.jdt.core.JavaCore;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jdt.core.search.IJavaSearchConstants;
//import org.eclipse.jdt.core.search.IJavaSearchScope;
//import org.eclipse.jdt.core.search.SearchEngine;
//import org.eclipse.jdt.core.search.SearchMatch;
//import org.eclipse.jdt.core.search.SearchParticipant;
//import org.eclipse.jdt.core.search.SearchPattern;
//import org.eclipse.jdt.core.search.SearchRequestor;
//import org.eclipse.jface.dialogs.ErrorDialog;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.jface.window.Window;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.dialogs.ElementListSelectionDialog;
//import ru.biatech.edt.junit.BasicElementLabels;
//import ru.biatech.edt.junit.JUnitCore;
//import ru.biatech.edt.junit.Messages;
//import ru.biatech.edt.junit.ui.IJUnitHelpContextIds;
//import ru.biatech.edt.junit.ui.JUnitMessages;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.atomic.AtomicReference;
//
////import org.eclipse.jdt.ui.JavaElementLabelProvider;
////import org.eclipse.jdt.ui.JavaElementLabels;
//
///**
// * A dialog to select a test method.
// */
//public class TestMethodSelectionDialog extends ElementListSelectionDialog {
//
//  private IJavaElement fElement;
//
//  public static class TestReferenceCollector extends SearchRequestor {
//    Set<IJavaElement> fResult = new HashSet<>(200);
//
//    @Override
//    public void acceptSearchMatch(SearchMatch match) throws CoreException {
//      IJavaElement enclosingElement = (IJavaElement) match.getElement();
//      if (enclosingElement.getElementName().startsWith("test")) //$NON-NLS-1$
//        fResult.add(enclosingElement);
//    }
//
//    public Object[] getResult() {
//      return fResult.toArray();
//    }
//  }
//
//  public TestMethodSelectionDialog(Shell shell, IJavaElement element) {
//    super(shell, null);//new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_POST_QUALIFIED));
//    fElement = element;
//  }
//
//  /*
//   * @see Windows#configureShell
//   */
//  @Override
//  protected void configureShell(Shell newShell) {
//    super.configureShell(newShell);
//    PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJUnitHelpContextIds.TEST_SELECTION_DIALOG);
//  }
//
//  /*
//   * @see Window#open()
//   */
//  @Override
//  public int open() {
//    Object[] elements;
//    IType testType = findTestType();
//
//    if (testType == null)
//      return Window.CANCEL;
//
//    try {
//      elements = searchTestMethods(fElement, testType);
//    } catch (InterruptedException e) {
//      return Window.CANCEL;
//    } catch (InvocationTargetException e) {
//      MessageDialog.openError(getParentShell(), JUnitMessages.TestMethodSelectionDialog_error_title, e.getTargetException().getMessage());
//      return Window.CANCEL;
//    }
//
//    if (elements.length == 0) {
////			String msg= Messages.format(JUnitMessages.TestMethodSelectionDialog_notfound_message, JavaElementLabels.getElementLabel(fElement, JavaElementLabels.ALL_DEFAULT));
////			MessageDialog.openInformation(getParentShell(), JUnitMessages.TestMethodSelectionDialog_no_tests_title, msg);
//      return Window.CANCEL;
//    }
//    setElements(elements);
//    return super.open();
//  }
//
//  private IType findTestType() {
//    String qualifiedName = JUnitCore.TEST_INTERFACE_NAME;
//    IJavaProject[] projects;
//    Set<IType> result = new HashSet<>();
//    try {
//      projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
//      for (IJavaProject project : projects) {
//        IType type = project.findType(qualifiedName);
//        if (type != null)
//          result.add(type);
//      }
//    } catch (JavaModelException e) {
//      ErrorDialog.openError(getParentShell(), JUnitMessages.TestMethodSelectionDialog_error_notfound_title, JUnitMessages.TestMethodSelectionDialog_error_notfound_message, e.getStatus());
//      return null;
//    }
//    if (result.isEmpty()) {
//      String msg = Messages.format(JUnitMessages.TestMethodSelectionDialog_test_not_found, BasicElementLabels.getJavaElementName(JUnitCore.TEST_INTERFACE_NAME));
//      MessageDialog.openError(getParentShell(), JUnitMessages.TestMethodSelectionDialog_select_dialog_title, msg);
//      return null;
//    }
//    if (result.size() == 1)
//      return (IType) result.toArray()[0];
//
//    return selectTestType(result);
//  }
//
//  private IType selectTestType(Set<IType> result) {
////		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_PARAMETERS | JavaElementLabelProvider.SHOW_ROOT);
////		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getParentShell(), labelProvider);
////		dialog.setTitle(JUnitMessages.TestMethodSelectionDialog_dialog_title);
////		String msg= Messages.format(JUnitMessages.TestMethodSelectionDialog_testproject, BasicElementLabels.getJavaElementName("junit.framework.Test")); //$NON-NLS-1$
////		dialog.setMessage(msg);
////		IJavaProject[] projects= new IJavaProject[result.size()];
////		IType[] testTypes= result.toArray(new IType[result.size()]);
////		for (int i= 0; i < projects.length; i++)
////			projects[i]= testTypes[i].getJavaProject();
////		dialog.setElements(projects);
////		if (dialog.open() == Window.CANCEL)
////			return null;
////		IJavaProject project= (IJavaProject) dialog.getFirstResult();
////		for (IType testType : testTypes) {
////			if (testType.getJavaProject().equals(project))
////				return testType;
////		}
//    return null;
//  }
//
//  public Object[] searchTestMethods(final IJavaElement element, final IType testType) throws InvocationTargetException, InterruptedException {
//    final AtomicReference<TestReferenceCollector> col = new AtomicReference<>();
//
//    IRunnableWithProgress runnable = progressMonitor -> {
//      try {
//        col.set(doSearchTestMethods(element, testType, progressMonitor));
//      } catch (CoreException e) {
//        throw new InvocationTargetException(e);
//      }
//    };
//    PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
//    return col.get().getResult();
//  }
//
//  private TestReferenceCollector doSearchTestMethods(IJavaElement element, IType testType, IProgressMonitor pm) throws CoreException {
//    int matchRule = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH;
//    SearchPattern pattern = SearchPattern.createPattern(element, IJavaSearchConstants.REFERENCES, matchRule);
//    SearchParticipant[] participants = new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()};
//    IJavaSearchScope scope = SearchEngine.createHierarchyScope(testType);
//    TestReferenceCollector requestor = new TestReferenceCollector();
//    new SearchEngine().search(pattern, participants, scope, requestor, pm);
//    return requestor;
//  }
//}
