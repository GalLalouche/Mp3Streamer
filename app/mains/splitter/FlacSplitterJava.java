package mains.splitter;

import mains.JavaMainUtils;

public class FlacSplitterJava {
    private FlacSplitterJava() {}

    public static void main(String[] args) {
        try {
            JavaMainUtils.turnOffLogging();
            FlacSplitter.main(args);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            JavaMainUtils.waitForCarriageReturn();
        }
    }
}
