package relationalDatabaseTools.client;

/**
 * Simple binary counter that is used to cycle through the power set of input attribute set.
 * @author Raymond
 *
 */
public class BinaryCounter {
	private final boolean[] counter;
	private int decimalCounter;
	private boolean reachedMax;
	
	public BinaryCounter(int capacity) {
		counter = new boolean[capacity];
		for (int i = 0; i < counter.length; i++) {
			counter[i] = false;
		}
		counter[0] = true;
		decimalCounter = 1;
		reachedMax = false;
	}
	public boolean[] getCounter() {
		return counter;
	}
	
	public void incrementCounter() {
		boolean carryOver = false;
		for (int i = 0; i < counter.length; i++) {
			if (!counter[i]) {
				counter[i] = true;
				carryOver = false;
			} else {
				counter[i] = false;
				carryOver = true;
			}
			if (!carryOver) {
				break;
			}
		}
		if (carryOver) {
			reachedMax = true;
		}
		decimalCounter++;
	}
	
	public int getDecimalCounter() {
		return decimalCounter;
	}
	
	public boolean hasReachedMax() {
		return reachedMax;
	}
}
