package mains.fixer;

public class FolderFixerJava {
	public static void main(String[] args) throws Exception {
		try {
			FolderFixer.main(args);
			System.exit(0);
		} catch (Throwable t) {
			t.printStackTrace();
			System.in.read();
			System.exit(1);
		}
	}
}
