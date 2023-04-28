/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Neale Upstone <neale@nealeupstone.com> - [JUnit] JUnit viewer doesn't recognise <skipped/> node - https://bugs.eclipse.org/bugs/show_bug.cgi?id=276068
 *     BIA-Technologies LLC - adaptation for EDT
 *******************************************************************************/

package ru.biatech.edt.junit.model;

public interface IXMLTags {

  String NODE_TESTRUN = "testrun"; //$NON-NLS-1$
  String NODE_TESTSUITES = "testsuites"; //$NON-NLS-1$
  String NODE_TESTSUITE = "testsuite"; //$NON-NLS-1$
  String NODE_PROPERTIES = "properties"; //$NON-NLS-1$
  String NODE_PROPERTY = "property"; //$NON-NLS-1$
  String NODE_TESTCASE = "testcase"; //$NON-NLS-1$
  String NODE_ERROR = "error"; //$NON-NLS-1$
  String NODE_FAILURE = "failure"; //$NON-NLS-1$
  String NODE_EXPECTED = "expected";  //$NON-NLS-1$
  String NODE_ACTUAL = "actual";  //$NON-NLS-1$
  String NODE_SYSTEM_OUT = "system-out"; //$NON-NLS-1$
  String NODE_SYSTEM_ERR = "system-err"; //$NON-NLS-1$
  String NODE_SKIPPED = "skipped"; //$NON-NLS-1$

  /**
   * value: String
   */
  String ATTR_NAME = "name"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_PROJECT = "project"; //$NON-NLS-1$
  String ATTR_CONTEXT = "context"; //$NON-NLS-1$
  /**
   * value: Integer
   */
  String ATTR_TESTS = "tests"; //$NON-NLS-1$
  /**
   * value: Integer
   */
  String ATTR_STARTED = "started"; //$NON-NLS-1$
  /**
   * value: Integer
   */
  String ATTR_FAILURES = "failures"; //$NON-NLS-1$
  /**
   * value: Integer
   */
  String ATTR_ERRORS = "errors"; //$NON-NLS-1$
  /**
   * value: Boolean
   */
  String ATTR_IGNORED = "ignored"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_PACKAGE = "package"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_ID = "id"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_CLASSNAME = "classname"; //$NON-NLS-1$
  /**
   * value: Boolean
   */
  String ATTR_INCOMPLETE = "incomplete"; //$NON-NLS-1$
  /**
   * value: Double
   */
  String ATTR_TIME = "time"; //$NON-NLS-1$
  /**
   * value: Date
   */
  String ATTR_TIMESTAMP = "timestamp"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_MESSAGE = "message"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_DISPLAY_NAME = "displayname"; //$NON-NLS-1$
  /**
   * value: Boolean
   */
  String ATTR_DYNAMIC_TEST = "dynamicTest"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_PARAMETER_TYPES = "parameters"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_UNIQUE_ID = "uniqueid"; //$NON-NLS-1$

  /**
   * value: String
   */
  String ATTR_INCLUDE_TAGS = "include_tags"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_EXCLUDE_TAGS = "exclude_tags"; //$NON-NLS-1$
  /**
   * value: String
   */
  String ATTR_TYPE = "type"; //$NON-NLS-1$
}
