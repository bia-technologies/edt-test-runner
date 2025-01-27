package ru.biatech.edt.junit.model.report;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.Data;

@Data
public class ErrorInfo {
  String message;
  @JacksonXmlText
  String trace;
  String type;

}
