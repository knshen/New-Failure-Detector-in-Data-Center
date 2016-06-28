package util;

public class Item implements Comparable {
	public String item_name;
	public double value;
	
	public Item(String in, double v) {
		this.item_name = in;
		this.value = v;
	}
	
	@Override
	public int compareTo(Object o) {
		Item item = (Item) o;
		if(value > item.value)
			return -1;
		else if(value == item.value)
			return 0;
		return 1;
	}
	
	@Override
	public int hashCode() {
		return item_name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		Item item = (Item) o;
		return item_name.equals(item.item_name);
	}
	
	@Override
	public String toString() {
		return this.item_name + ": " + this.value;
	}
}
