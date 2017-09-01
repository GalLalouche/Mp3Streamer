package mains;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class JavaMainUtils {
  public static void turnOffLogging() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    context.stop();
  }
  public static void waitForCarriageReturn() {
    System.console().readLine();
  }
}
