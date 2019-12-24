package mains;

import ch.qos.logback.core.spi.LifeCycle;
import org.slf4j.LoggerFactory;

public class JavaMainUtils {
  public static void turnOffLogging() {
    LifeCycle context = (LifeCycle) LoggerFactory.getILoggerFactory();
    context.stop();
  }

  public static void waitForCarriageReturn() {
    System.console().readLine();
  }
}
