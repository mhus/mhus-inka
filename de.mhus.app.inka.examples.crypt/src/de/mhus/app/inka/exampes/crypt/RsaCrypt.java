package de.mhus.app.inka.exampes.crypt;

// http://de.wikipedia.org/wiki/RSA-Kryptosystem

public class RsaCrypt {

	public static void main(String[] args) throws java.lang.Exception {
		// Als beispiel zwei primzahlen q und p
		int p = 17;
		int q = 13;

		// errechne N
		int n = p * q;

		// errechne euler funktion
		int fn = (p - 1) * (q - 1);

		System.out.println("n: " + n);
		System.out.println("fn: " + fn);

		// finde einen teilerfremden e, der einen gemeinsammen teiler mit fn hat
		// fange mit dem max aus q,p an, nimm aber nicht p oder q
		int e = Math.max(p, q) + 1;
		int d = 0;
		int k = 0;
		do {
			for (e = e; e <= fn; e++) {
				if (fn % e != 0)
					break; // wenn teilerfremd, dann weiter
			}
			if (e == fn) { // schleife ist zuende gelaufen ... nicht mšglich.
				System.out.println("e not found");
				return;
			}

			// e=23; // beispiel aus wikipedia - zum testen

			// jetzt finde den ggT d und als nebenprodukt k
			d = fn;
			k = 0;
			for (d = d; d > 0; d--) {
				k = -e * d / fn;
				if (e * d + fn * k == 1)
					break; // gefunden, dann weiter
			}
			if (d == 0) { // nicht gefunden, suche ein weiteren wert fuer e
				System.out.println("d not found; e=" + e);
				e++;
			}
		} while (d == 0); // dann weiter

		System.out.println("e: " + e);
		System.out.println("d: " + d + " (" + (e * d) + ")");
		System.out.println("k: " + k + " (" + (k * fn) + ")");

		// e=23; // beispiel aus wikipedia
		// d=47; // beispiel aus wikipedia

		// Test der zuordnungen
		for (int t = 0; t < Math.min(256, n); t++) {

			System.out.print(Integer.toHexString(t) + "=");
			int x = t;
			for (int i = 1; i < e; i++) {
				x = x * t % n;
			}
			System.out.print(Integer.toHexString(x));
			int o = x;
			for (int i = 1; i < d; i++) {
				o = o * x % n;
			}
			if (o != t)
				System.out.print("!");
			System.out.print(" ");
			if (t % 20 == 19)
				System.out.println();
		}
		System.out.println();

		// Test eines textes
		String s = "delta bravo";
		char[] cb = new char[s.length()];
		char[] ca = new char[s.length()];
		System.out.println(s);
		for (int i = 0; i < s.length(); i++) {
			int ib = (int) s.charAt(i);
			System.out.print(Integer.toHexString(ib) + " ");
		}
		System.out.println();
		for (int i = 0; i < s.length(); i++) {
			int ib = code((int) s.charAt(i), e, n);
			System.out.print(Integer.toHexString(ib) + " ");
			cb[i] = (char) ib;
			ca[i] = (char) code(ib, d, n);
		}
		System.out.println();
		System.out.println(new String(cb));
		System.out.println(new String(ca));
	}

	static int code(int t, int e, int n) {
		int x = t;
		for (int i = 1; i < e; i++) {
			x = x * t % n;
		}
		return x;
	}
}
