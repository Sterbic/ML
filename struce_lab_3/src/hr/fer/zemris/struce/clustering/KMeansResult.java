package hr.fer.zemris.struce.clustering;

import java.util.List;

import hr.fer.zemris.struce.algebra.Vector;

public class KMeansResult {
	
	public Vector[] centroids;
	public int[] count;
	public int iter;
	public double J;
	public List<Double> Jiter;
	
	public KMeansResult(Vector[] centroids, int[] count, int iter,
			double J, List<Double> Jiter) {
		this.centroids = centroids;
		this.count = count;
		this.iter = iter;
		this.J = J;
		this.Jiter = Jiter;
	}
	
}
