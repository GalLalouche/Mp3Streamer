package mains.splitter;

import mains.JavaMainUtils;

public class FlacSplitterJava {
    public static void main(String[] args) {
        try {
            FlacSplitter.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            JavaMainUtils.waitForCarriageReturn();
        }
    }
}
