/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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
package ru.biatech.edt.junit;

import org.eclipse.osgi.util.TextProcessor;

import java.io.File;


/**
 * A label provider for basic elements like paths. The label provider will make sure that the labels are correctly
 * shown in RTL environments.
 *
 * @since 3.4
 */
public class BasicElementLabels {

  private BasicElementLabels() {
  }

  /**
   * Adds special marks so that that the given string is readable in a BIDI environment.
   *
   * @param string     the string
   * @param delimiters the additional delimiters
   * @return the processed styled string
   */
  private static String markLTR(String string, String delimiters) {
    return TextProcessor.process(string, delimiters);
  }

  /**
   * Returns the label of the path of a file.
   *
   * @param file the file
   * @return the label of the file path to be used in the UI.
   */
  public static String getPathLabel(File file) {
    return markLTR(file.getAbsolutePath(), "/\\:.");  //$NON-NLS-1$
  }

  /**
   * Returns a label for Java element name. Example is 'new Test<? extends List>() { ...}'.
   * This method should only be used for simple element names. Use
   * JavaElementLabels to create a label from a Java element.
   *
   * @param name the Java element name.
   * @return the label for the Java element
   */
  public static String getElementName(String name) {
    return markLTR(name, "<>()?,{}.:"); //$NON-NLS-1$
  }
}
