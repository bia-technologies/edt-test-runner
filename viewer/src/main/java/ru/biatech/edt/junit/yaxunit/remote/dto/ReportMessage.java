package ru.biatech.edt.junit.yaxunit.remote.dto;

import lombok.Data;

public class ReportMessage extends Message<ReportMessage.Params> {

  public ReportMessage() {
    super(REPORT_TYPE);
  }

  @Data
  public static class Params {
    TestSuite[] tests;
  }
  @Data
  public static class TestSuite {
    String status;
    String present;
    String method;
    long duration;
    Error[] errors;
  }

  @Data
  public static class Error {
    String message, trace, type;
  }
}
