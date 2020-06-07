import java.util.HashMap;

public class SimLotto {
	
	
	public static void main(String[] args) {
		new SimLotto().run7();
	}

	private long round;
	private long sum;
	private boolean quiet;

	
	private void run7() {
		quiet = true;
		long roundSum = 0;
		long roundMax = 0;
		long roundMin = Long.MAX_VALUE;
		
		for (int i = 1; i < 1000; i++) {
			run4();
			roundSum = roundSum + round;
			roundMax = Math.max(roundMax, round);
			roundMin = Math.min(roundMin, round);
			System.out.println(i + ": " + round + " \t" + sum + " \t| " + (roundSum / i) + " \t| " + roundMin + " - " + roundMax);
		}
	}
	
	private void run6() {
		quiet = true;
		for (int i = 0; i < 1000; i++) {
			run5();
			System.out.println(i + ": " + round + "  \t" + sum);
		}
	}
	
	/**
	 * Calculate round until first win
	 */
	private void run5() {

		HashMap<String, Integer> in = new HashMap<>();
		in.put("true 2", 5);
		in.put("false 3", 12);
		in.put("true 3", 30);
		in.put("false 4", 50);
		in.put("true 4", 300);
		in.put("false 5", 4183);
		in.put("true 5", 19552);
		in.put("false 6", 1994315);
		in.put("true 6", 26887087);
		
		LottoResult result = new LottoResult(5,18,33,35,42,43,0);
		round = 1;
		
		sum = 0;
		long cost = 2;
		
		while (true) {
			LottoResult tip = new LottoResult();
			sum = sum - cost;
			int equ = result.compareNumbers(tip);
			boolean sup = tip.sup == result.sup;
			Integer ini = in.get(sup + " " + equ);
			if (ini != null)
				sum = sum + ini.longValue();
			if (!quiet)
				System.out.println(round + ": " + sup + " " + equ + " --- " + sum);
			if (ini != null) {
				break;
			}
			round++;
		}
	}
		
	/**
	 * Calculate round until zero
	 */
	private void run4() {

		HashMap<String, Integer> in = new HashMap<>();
		in.put("true 2", 5);
		in.put("false 3", 12);
		in.put("true 3", 30);
		in.put("false 4", 50);
		in.put("true 4", 300);
		in.put("false 5", 4183);
		in.put("true 5", 19552);
		in.put("false 6", 1994315);
		in.put("true 6", 26887087);
		
		LottoResult result = new LottoResult(5,18,33,35,42,43,0);
		round = 1;
		
		sum = 0;
		long cost = 2;
		
		while (true) {
			LottoResult tip = new LottoResult();
			sum = sum - cost;
			int equ = result.compareNumbers(tip);
			boolean sup = tip.sup == result.sup;
			Integer ini = in.get(sup + " " + equ);
			if (ini != null)
				sum = sum + ini.longValue();
			if (!quiet)
				System.out.println(round + ": " + sup + " " + equ + " --- " + sum);
			if (sum >= 0) {
				break;
			}
			round++;
		}
	}
	
	
	private void run3() {
		quiet = true;
		long roundSum = 0;
		long roundMax = 0;
		long roundMin = Long.MAX_VALUE;
		
		for (int i = 1; i < 100000; i++) {
			run1();
			roundSum = roundSum + round;
			roundMax = Math.max(roundMax, round);
			roundMin = Math.min(roundMin, round);
			System.out.println(i + ": " + round + " \t" + sum + " \t| " + (roundSum / i) + " \t| " + roundMin + " - " + roundMax);
		}
		
		System.out.println("Average: " + (roundSum / 1000));
		
	}
	
	
	private void run1() {

		HashMap<String, Integer> in = new HashMap<>();
		in.put("true 2", 5);
		in.put("false 3", 12);
		in.put("true 3", 30);
		in.put("false 4", 50);
		in.put("true 4", 300);
		in.put("false 5", 4183);
		in.put("true 5", 19552);
		in.put("false 6", 1994315);
		in.put("true 6", 26887087);
		
		LottoResult result = new LottoResult(5,18,33,35,42,43,0);
		round = 1;
		
		sum = 100;
		long cost = 2;
		
		while (true) {
			LottoResult tip = new LottoResult();
			sum = sum - cost;
			int equ = result.compareNumbers(tip);
			boolean sup = tip.sup == result.sup;
			Integer ini = in.get(sup + " " + equ);
			if (ini != null)
				sum = sum + ini.longValue();
			if (!quiet)
				System.out.println(round + ": " + sup + " " + equ + " --- " + sum);
			if (sup && equ == 6 || sum > 1000000) {
				break;
			}
			round++;
		}
	}

	private void run2() {

		HashMap<String, Integer> in = new HashMap<>();
		in.put("true 2", 5);
		in.put("false 3", 12);
		in.put("true 3", 30);
		in.put("false 4", 50);
		in.put("true 4", 300);
		in.put("false 5", 4183);
		in.put("true 5", 19552);
		in.put("false 6", 1994315);
		in.put("true 6", 26887087);
		
		round = 1;
		
		sum = 100;
		long cost = 2;
		
		
		while (true) {
			LottoResult result = new LottoResult();
			LottoResult tip = new LottoResult();
			sum = sum - cost;
			int equ = result.compareNumbers(tip);
			boolean sup = tip.sup == result.sup;
			Integer ini = in.get(sup + " " + equ);
			if (ini != null)
				sum = sum + ini.longValue();
			if (!quiet)
				System.out.println(round + ": " + sup + " " + equ + " --- " + sum);
			if (sup && equ == 6 || sum > 1000000)
				break;
			round++;
		}
	}

}