package mains.cover;

public class DownloadCoverJava {
    public static void main(String[] args) throws Exception {
        try {
            DownloadCover.main(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.in.read();
        }
    }
}
