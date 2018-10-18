package test.uxxx;

public class GermanSpecialChars {

	public static void main(String[] args) {
		String x = "ÄÖÜäöüß";
		for (int i = 0; i < x.length(); i++) {
			String o = Integer.toUnsignedString(x.charAt(i), 16);
			while (o.length() < 4) o = '0' + o;
			System.out.println(x.charAt(i) + ": \\u" + o );
		}

	}

}
