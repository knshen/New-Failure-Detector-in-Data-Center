package crash;

public class RandomTimeGetter {

	public static double makeTime(int start, int end) {
		return Math.random() * (end - start) + start;
	}

	public static void main(String[] args) {
		System.out.println(RandomTimeGetter.makeTime(2, 60));

	}

}
