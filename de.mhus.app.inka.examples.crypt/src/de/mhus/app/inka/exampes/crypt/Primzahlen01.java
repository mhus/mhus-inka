package de.mhus.app.inka.exampes.crypt;

public class Primzahlen01 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int max = 30;
		int last = 0;
		int start = 4;
		for (int i = start; i <= max; i++) {
			System.out.print(i);
			int cnt = 0;
			for (int t = start; t <= i; t++) {
				if (i % t == 0)
					cnt++;
			}
			if (cnt == 1)
				System.out.print("*");
			System.out.println(" " + cnt + " " + (cnt - last));
			last = cnt;
		}
	}

}
