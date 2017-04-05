package de.mhus.example.inka.crypto;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Baghdad {

	public static void main(String[] agrs) {
		
		{
			// (e*d) % z = 1
			long e = 41;
			long z = 1019;
			long d = compute1(e, z);
			test( (e*d)%z, 1, ""+d );
		}		
		
		
		{
			// (e*d) % z = 2
			long e = 41;
			long z = 1019;
			long d = compute2(2, e, z);
			test( (e*d)%z, 2, ""+d );
		}
		
		{
			// (e*d) % z = 5
			long e = 41;
			long z = 1019;
			long d = compute2(5, e, z);
			test( (e*d)%z, 5, ""+d );
		}

		{
			long e = 3;
			long z = 26;
			long c = 3;
			long found = compute3(c, e, z, 1000000);
			System.out.println("Found: " + found);
//			test( (long)Math.pow(e, d) % z, 1, ""+d );
		}
		
		
	}
	
	private static void test(long l, long r, String info) {
		if (l != r)
			throw new AssertionError(info + ": " + l + " is not " + r);
	}

	// using baghdad method
	static long compute1(long e, long z) {
		double d = 1;
		double t = 0;
		do {
			d = (d + (double)z);
			t = d / (double)e;
		} while ( Math.rint(t) != t );
		return (long) t;
	}

	// using baghdad method
	static long compute2(long start, long e, long z) {
		double d = start;
		double t = 0;
		do {
			d = (d + (double)z);
			t = d / (double)e;
		} while ( Math.rint(t) != t );
		return (long) t;
	}

	// using baghdad method to find canlidates
	static long compute3(long start, long e, long z, int rounds) {
		double d = start;
		double t = 0;
		double eLog = Math.log10(e);
		int r = rounds;
		int found = 0;
		do {
			d = (d + (double)z);
			t = d / (double)e;
			if (Math.rint(t) == t) { // d % e = 0
				long tt = (long)t;
				double exp = Math.log10(tt) / eLog;
				//System.out.println(tt + " " + exp);
				if (Math.rint(exp) == exp) {
					System.out.println(tt + " " + exp + " (" + d + ")");
					found++;
				}
				// minimize d ...
				
			}
			r--;
		} while (r > 0);
		return found;
	}
	
}
