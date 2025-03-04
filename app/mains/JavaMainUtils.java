package mains;

import backend.logging.ScribeUtils;

public class JavaMainUtils {
  public static void turnOffLogging() {
    ScribeUtils.noLogs();
  }

  public static void waitForCarriageReturn() {
    System.console().readLine();
  }
}
