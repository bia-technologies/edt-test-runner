package ru.biatech.edt.junit.model.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Failure extends ErrorInfo {
  String actual;
  String expected;

  public boolean isComparisonFailure() {
    return getExpected() != null && getActual() != null;
  }
}
