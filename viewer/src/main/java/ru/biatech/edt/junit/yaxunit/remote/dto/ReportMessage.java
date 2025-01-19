package ru.biatech.edt.junit.yaxunit.remote.dto;

import ru.biatech.edt.junit.model.TestSuiteElement;

public class ReportMessage extends Message<TestSuiteElement[]> {

  public ReportMessage() {
    super(REPORT_TYPE);
  }
}
