import java.util.HashMap;
import java.util.LinkedList;

public class SimLotto {
	
	
	public static void main(String[] args) {
		new SimLotto().run3();
	}

	private long round;
	private long sum;
	private boolean quiet;

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