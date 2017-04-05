package de.mhus.example.inka.crypto;

import java.math.BigInteger;
import java.util.Random;

public class TestBigInt {

	public static void main(String[] args) {

//		BigInteger a = new BigInteger( new byte[ 256 / 8 ] );
		BigInteger a = new BigInteger( 256, new Random() );
		System.out.println("Bits: " + a.bitLength() );
		System.out.println("Value: " + a.toString(16));
		System.out.println("Value: " + a.toString(10));

		BigInteger n = new BigInteger( 256, new Random() );

		for (int i = 1; i <= 3; i++) {
			System.out.println(i + ": " + a.toString(10));
			BigInteger m = a.mod(n);
			System.out.println("    Rest: " + m);
			a = a.multiply(a);
		}
	}

}
