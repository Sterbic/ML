package hr.fer.zemirs.struce.data;

import java.util.HashMap;
import java.util.Map;

public class DataSample {
	
	public Map<Integer, Double> data;
	public int cl;
	
	public DataSample(int cl) {
		this.cl = cl;
		this.data = new HashMap<Integer, Double>();
	}

}
