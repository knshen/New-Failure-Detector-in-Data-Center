package relatedWork.gossip;

import java.util.*;

public class LiveList {
	int num_nodes;
	int self_id;
	List<Double> last_time = new ArrayList<Double>();
	List<Boolean> isFail = new ArrayList<Boolean>();
	List<Boolean> isClean = new ArrayList<Boolean>();
	
	public LiveList(int id, int nn) {
		this.self_id = id;
		this.num_nodes = nn;
		
		for(int i=0; i<num_nodes; i++) {
			last_time.add(2.0);
			isFail.add(false);
			isClean.add(false);
		}
	}
	
}
