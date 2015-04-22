package relationalDatabaseTools.client;

public class BinaryCounter {
	private final char[] counter;
	private int decimalCounter;
	private boolean reachedMax;
	
	public BinaryCounter(int capacity) {
		counter = new char[capacity];
		for (int i = 0; i < counter.length; i++) {
			counter[i] = '0';
		}
		counter[0] = '1';
		decimalCounter = 1;
		reachedMax = false;
	}
	public char[] getCounter() {
		return counter;
	}
	
	public void incrementCounter() {
		boolean carryOver = false;
		for (int i = 0; i < counter.length; i++) {
			if (counter[i] == '0') {
				counter[i] = '1';
				carryOver = false;
			} else {
				counter[i] = '0';
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
