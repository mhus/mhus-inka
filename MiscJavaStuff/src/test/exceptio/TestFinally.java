package test.exceptio;

public class TestFinally {

	public static void main(String[] args) throws Exception {
		
		try {
			System.out.println("Start");
			throw new Exception("bye");
		} catch (Exception e) {
			System.out.println("*** Exception: " + e);
			System.out.flush();
			Thread.sleep(200); // sync out and err stream
			throw e;
		} finally {
			System.out.println("Finally");
			System.out.flush();
			Thread.sleep(200); // sync out and err stream
		}
	}

}
