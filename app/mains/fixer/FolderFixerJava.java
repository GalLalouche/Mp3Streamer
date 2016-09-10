package mains.fixer;

import mains.JavaMainUtils;

public class FolderFixerJava {
	public static void main(String[] args) throws Exception {
		try {
			FolderFixer.main(args);
			System.exit(0);
		} catch (Throwable t) {
			t.printStackTrace();
			JavaMainUtils.waitForCarriageReturn();
			System.exit(1);
		}
	}
}
