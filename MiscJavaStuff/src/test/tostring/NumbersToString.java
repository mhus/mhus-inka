package test.tostring;

import java.util.Locale;

public class NumbersToString {

	public static void main(String[] args) {
		
		System.out.println(String.valueOf(12.123));
		
		Locale.setDefault(Locale.US);
		System.out.println(String.valueOf(12.123));
		
		Locale.setDefault(Locale.GERMANY);
		System.out.println(String.valueOf(12.123));
		
	}
}
