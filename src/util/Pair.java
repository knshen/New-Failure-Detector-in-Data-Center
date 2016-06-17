package util;

public class Pair<T, U> implements Comparable<Pair<T, U>>{
	public T first;
	public U second;

	public Pair(T f, U s) {
		this.first = f;
		this.second = s;
	}

	@Override
    public int compareTo(Pair<T, U> other) {	
		double a = Double.parseDouble(first.toString());
		double b = Double.parseDouble(other.first.toString());    

		if(a > b)
			return 1;
		else if(a == b) 
			return 0;
		return -1;
    }
	
	public String toString() {
		return first.toString() + ": " + second.toString();
	}
}
