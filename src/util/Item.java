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
	
}
