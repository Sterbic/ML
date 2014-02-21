package hr.fer.zemris.struce.clustering;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.struce.algebra.Vector;
import hr.fer.zemris.struce.data.DataSample;
import hr.fer.zemris.struce.data.Dataset;

public class KMeans {
	
	public static KMeansResult run(int K, int[] centroidIds, Dataset dataset) {
		DataSample[] samples = dataset.samples;
		Vector[] centoids = new Vector[K];
		List<Double> Jiter = new ArrayList<>();
		
		for(int k = 0; k < K; k++) {
			centoids[k] = samples[centroidIds[k]].data.copy();
		}
		
		int iter = 0;
		double J = 0.0;
		
		while(true) {
			boolean changed = false;
			iter++;
			
			for(DataSample sample : samples) {
				double dist = Double.POSITIVE_INFINITY;
				
				if(sample.currentClass != -1) {
					dist = sample.data.distance(centoids[sample.currentClass]);
				}
				
				for(int k = 0; k < K; k++) {
					if(k != sample.currentClass) {
						double newDist = sample.data.distance(centoids[k]);
						
						if(newDist < dist) {
							dist = newDist;
							sample.currentClass = k;
							changed = true;
						}
					}
				}
			}
			
			J = 0.0;
			for(DataSample sample : samples) {
				J += sample.data.distance(centoids[sample.currentClass]);
			}
			Jiter.add(J);
			
			if(!changed) {
				break;
			}
			
			for(Vector centroid : centoids) {
				centroid.fill(0.0);
			}
			
			int[] count = new int[K];
			
			for(DataSample sample : samples) {
				centoids[sample.currentClass].addToThis(sample.data);
				count[sample.currentClass]++;
			}
			
			for(int k = 0; k < K; k++) {
				centoids[k].multiplyToThis(1.0 / count[k]);
			}
		}
		
		int[] count = new int[K];
		
		for(DataSample sample : samples) {
			count[sample.currentClass]++;
		}
		
		return new KMeansResult(centoids, count, iter, J, Jiter);
	}

}
