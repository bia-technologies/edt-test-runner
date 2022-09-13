/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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

package ru.biatech.edt.junit.model;

public class MessageIds { // TODO Подозреваю, что не пригодится
  public static final int MSG_HEADER_LENGTH = 8;
  public static final String TRACE_START = "%TRACES ";
  public static final String TRACE_END = "%TRACEE ";
  public static final String EXPECTED_START = "%EXPECTS";
  public static final String EXPECTED_END = "%EXPECTE";
  public static final String ACTUAL_START = "%ACTUALS";
  public static final String ACTUAL_END = "%ACTUALE";
  public static final String RTRACE_START = "%RTRACES";
  public static final String RTRACE_END = "%RTRACEE";
  public static final String TEST_RUN_START = "%TESTC  ";
  public static final String TEST_START = "%TESTS  ";
  public static final String TEST_END = "%TESTE  ";
  public static final String TEST_ERROR = "%ERROR  ";
  public static final String TEST_FAILED = "%FAILED ";
  public static final String TEST_RUN_END = "%RUNTIME";
  public static final String TEST_STOPPED = "%TSTSTP ";
  public static final String TEST_RERAN = "%TSTRERN";
  public static final String TEST_TREE = "%TSTTREE";
  public static final String TEST_STOP = ">STOP   ";
  public static final String TEST_RERUN = ">RERUN  ";
  public static final String TEST_IDENTIFIER_MESSAGE_FORMAT = "{0}({1})";
  public static final String IGNORED_TEST_PREFIX = "@Ignore: ";
  public static final String ASSUMPTION_FAILED_TEST_PREFIX = "@AssumptionFailure: ";

  private MessageIds() {
  }
}