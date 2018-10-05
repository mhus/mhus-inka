package test.primetype;

public class ByteAdd {

	public static void main(String[] args) {
		byte a = 120;
		byte b = 7;
		
		byte r = (byte)((a + b) & 255);
		
		System.out.println(a + " + " + b + " = " + r);
	}

}
