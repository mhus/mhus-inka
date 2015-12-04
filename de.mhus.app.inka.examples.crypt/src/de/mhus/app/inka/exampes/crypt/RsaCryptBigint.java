package de.mhus.app.inka.exampes.crypt;

import java.math.BigInteger;

public class RsaCryptBigint {
	public static void main(String[] args) throws java.lang.Exception {
		// Als beispiel zwei primzahlen q und p
		BigInteger p = new BigInteger("13249");
		BigInteger q = new BigInteger("55061");
		
		// errechne N
		BigInteger n = p.multiply(q);

		// errechne euler funktion
		BigInteger fn = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

		System.out.println("p: " + p);
		System.out.println("q: " + q);
		System.out.println("n: " + n);
		System.out.println("fn: " + fn);

		// finde einen teilerfremden e, der einen gemeinsammen teiler mit fn hat
		// fange mit dem max aus q,p an, nimm aber nicht p oder q
		BigInteger e = p.max(q).add(BigInteger.ONE);
		BigInteger d = BigInteger.ZERO;
		BigInteger k = BigInteger.ZERO;
		do {
			for (;e.compareTo(fn) <= 0;e = e.add(BigInteger.ONE)) {
				if (!fn.mod(e).equals(BigInteger.ZERO))
					break; // wenn teilerfremd, dann weiter
			}
			if (e.equals(fn)) { // schleife ist zuende gelaufen ... nicht mšglich.
				System.out.println("e not found");
				return;
			}

			// e=23; // beispiel aus wikipedia - zum testen

			// jetzt finde den ggT d und als nebenprodukt k
			d = fn;
			k = BigInteger.ZERO;
			for (; d.compareTo(BigInteger.ZERO) > 0; d = d.subtract(BigInteger.ONE)) {
				k = e.multiply(d).divide(fn).negate();
				BigInteger i1 = e.multiply(d).add( fn.multiply(k) );
				if (i1.equals(BigInteger.ONE))
					break; // gefunden, dann weiter
			}
			if (d.equals(BigInteger.ZERO)) { // nicht gefunden, suche ein weiteren wert fuer e
				System.out.println("d not found; e=" + e);
				e = e.add(BigInteger.ONE);
			}
		} while (d.equals(BigInteger.ZERO)); // dann weiter

		System.out.println("e: " + e);
		System.out.println("d: " + d);
		System.out.println("k: " + k);

		System.out.println("Bits: n " + (n.toString(2).length()) );
		System.out.println("Bits: e " + (e.toString(2).length()) );
		System.out.println("Bits: d " + (d.toString(2).length()) );
		
		// e=23; // beispiel aus wikipedia
		// d=47; // beispiel aus wikipedia

		// Test der zuordnungen
		for (BigInteger t = BigInteger.ZERO; t.compareTo(n.min(new BigInteger("10"))) < 0; t = t.add(BigInteger.ONE)) {

			System.out.print(t.toString(16) + "=");
			BigInteger x = t;
			for (BigInteger i = BigInteger.ONE; i.compareTo(e) < 0; i = i.add(BigInteger.ONE)) {
				x = x.multiply(t).mod(n);
			}
			System.out.print(x.toString(16));
			BigInteger o = x;
			for (BigInteger i = BigInteger.ONE; i.compareTo(d) < 0; i = i.add(BigInteger.ONE)) {
				o = o.multiply(x).mod(n);
			}
			if (!o.equals(t))
				System.out.print("!");
			System.out.print(" ");
			if (t.mod(new BigInteger("20")).equals(new BigInteger("19")))
				System.out.println();
		}
		System.out.println();

		// Test eines textes
		String s = "delta bravo";
//		char[] cb = new char[s.length()];
		char[] ca = new char[s.length()];
		System.out.println(s);
		for (int i = 0; i < s.length(); i++) {
			int ib = (int) s.charAt(i);
			System.out.print(Integer.toHexString(ib) + " ");
		}
		System.out.println();
		for (int i = 0; i < s.length(); i++) {
			BigInteger ib = code(new BigInteger( String.valueOf((int) s.charAt(i)) ), e, n);
			System.out.print(ib.toString(16) + " ");
//			cb[i] = (char) ib;
			ca[i] = (char)code(ib, d, n).intValue();
		}
		System.out.println();
//		System.out.println(new String(cb));
		System.out.println(new String(ca));
	}

	static BigInteger code(BigInteger t, BigInteger e, BigInteger n) {
		BigInteger x = t;
		for (BigInteger i = BigInteger.ONE; i.compareTo(e) < 0; i = i.add(BigInteger.ONE)) {
			x = x.multiply(t).mod(n);
		}
		return x;
	}
}
