package mains.cover;

import mains.JavaMainUtils;

public class DownloadCoverJava {
    public static void main(String[] args) throws Exception {
        try {
            JavaMainUtils.turnOffLogging();
            DownloadCover.main(args);
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            JavaMainUtils.waitForCarriageReturn();
            System.exit(1);
        }
    }
}
