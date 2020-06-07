
public class LottoResult {

	int sup;
	int[] numbers = new int[6];
	
	public LottoResult(int nr1, int nr2, int nr3, int nr4, int nr5, int nr6, int sup) {
		numbers = new int[] {nr1, nr2, nr3, nr4, nr5, nr6};
		this.sup = sup;
	}
	
	public LottoResult() {
		for (int i = 0; i < 6; i++)
			while (true) {
				int r = (int)(Math.random() * 49) % 49 + 1;
				for (int j = 0; j < i; j++)
					if (numbers[j] == r)
						continue;
				numbers[i] = r;
				break;
			}
		sup = (int)(Math.random() * 10) % 10;
	}
	
	int compareNumbers(LottoResult tip) {
		int res = 0;
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 6; j++)
				if (tip.numbers[i] == numbers[j]) {
					res++;
					break;
				}
		return res;
	}
}
