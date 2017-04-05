package de.mhus.example.inka.crypto;

import java.math.BigDecimal;

public class GenerateManual {

	public static void main(String[] args) {

		long p1 = 47;
		long p2 = 71;
		
		long n = p1 * p2;
		long z = (p1-1) * (p2-1);
		System.out.println("z: " + z);
		
		long e = 79; // kein teiler mit z
		
		long d = 1; // starte test mit 2, ignoriere 1 als lösung
		while ( 
				(e * d) % z != 1 
				
				|| d > 1000000000 ) {
//			if (d % 41 == 0) System.out.println("---");
//			System.out.println( d + ": " + (e * d) % z );
			d++;
		}
		
		System.out.println("D: " + d);
		System.out.println("D: " + computeDfromE(e,z) );
		System.out.println("Validate: " + (d*e)%z);
		System.out.println();
		
		System.out.println("Private (d: " + d + ", n:" + n + ")");
		System.out.println("Public  (e: " + e + ", n:" + n + ")");
		System.out.println();
		// so kann man z auch ausrechnen, d.h. kleinster, maximaler z sind ...
		System.out.println("Z: " + ( n - p1 - p2 + 1  ) );
		long Zmin = ( (2-1) * (n/2-1) );
		long Zmax = (long) (n - Math.sqrt(n) - Math.sqrt(n) + 1);
		System.out.println("Zmin: " + Zmin );
		System.out.println("Zmax: " + Zmax );
		// kleinster wert für d, da sonst kein mod möglich
		System.out.println("Dmin: " + ( z / e ));
		
		System.out.println();
		long DZmin = computeDfromE(e, Zmin);
		long DZmax = computeDfromE(e, Zmax);
		long DZcenter = ((DZmin - DZmax)/2 + DZmax) ;
		System.out.println("DZmin: " + DZmin);
		System.out.println("DZmax: " + DZmax);
		System.out.println("DZspread: " + (DZmin - DZmax) );
		
		System.out.println("DZcenter: " + DZcenter + " (" + (d - DZcenter) + ")" );
		
	}
	
	// using baghdad method
	static long computeDfromE(long e, long z) {
		double d = 1;
		double t = 0;
		do {
			d = (d + (double)z);
			t = d / (double)e;
		} while ( Math.rint(t) != t );
		return (long) t;
	}

}
