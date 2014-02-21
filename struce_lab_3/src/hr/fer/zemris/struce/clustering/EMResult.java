package hr.fer.zemris.struce.clustering;

import hr.fer.zemris.struce.algebra.Vector;

import java.util.List;

public class EMResult {
	
	public Vector[] mi;
	public int[] count;
	public int iter;
	public double ll;
	public List<Double> LLiter;
	
	public EMResult(Vector[] mi, int[] count, int iter,
			double ll, List<Double> LLiter) {
		this.mi = mi;
		this.count = count;
		this.iter = iter;
		this.ll = ll;
		this.LLiter = LLiter;
	}
	
}
