package mains;

import backend.logging.ScribeUtils;
import scribe.Logger;

public class JavaMainUtils {
  public static void turnOffLogging() {
    ScribeUtils.noLogs(Logger.root());
  }

  public static void waitForCarriageReturn() {
    System.console().readLine();
  }
}
