package mains.fixer;

import mains.JavaMainUtils;

// TODO get rid of this, use the plain old scala main in FolderFixer.
public class FolderFixerJava {
  public static void main(String[] args) throws Exception {
    try {
      JavaMainUtils.turnOffLogging();
      FolderFixer.main(args);
      System.exit(0);
    } catch (Throwable t) {
      t.printStackTrace();
      JavaMainUtils.waitForCarriageReturn();
      System.exit(1);
    }
  }
}
