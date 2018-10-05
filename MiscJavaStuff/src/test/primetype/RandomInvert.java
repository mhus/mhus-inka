package test.primetype;

public class RandomInvert {

    private static final double UNIT = 0x1.0p-53; // 1.0 / (1L << 53)

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			int x = (int)(Math.random() * Integer.MAX_VALUE);
			double d = (((long)(x) << 22) + x) * UNIT;
					
			System.out.println(d);
		}
	}

}
