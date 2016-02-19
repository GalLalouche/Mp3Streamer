package mains.fixer;

public class FolderFixerJava {
	public static void main(String[] args) throws Exception {
		try {
			FolderFixer.main(args);
		} catch (Throwable t) {
			t.printStackTrace();
			System.in.read();
		}
	}
}
