package hr.fer.zemris.struce.data;

import hr.fer.zemris.struce.algebra.Vector;

public class DataSample {
	
	public Vector data;
	public String clazz;
	public int currentClass;
	public double probability;

	public DataSample(Vector data, String clazz) {
		this.data = data;
		this.clazz = clazz;
		this.currentClass = -1;
		this.probability = 0.0;
	}

}
