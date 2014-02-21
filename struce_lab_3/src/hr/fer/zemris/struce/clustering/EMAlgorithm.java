package hr.fer.zemris.struce.clustering;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.struce.algebra.Matrix;
import hr.fer.zemris.struce.algebra.Vector;
import hr.fer.zemris.struce.data.DataSample;
import hr.fer.zemris.struce.data.Dataset;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.SingularMatrixException;

public class EMAlgorithm {
	
	private Dataset dataset;
	private int K;
	
	private Vector pi;
	private Vector[] mi;
	private Matrix[] epsilon;
	private Vector[] h;
	private double[] determinant;
	private Matrix[] invEpsilon;
	
	public EMAlgorithm(Dataset dataset, int K, int[] centroidIds) {
		this.dataset = dataset;
		this.K = K;
		
		this.pi = new Vector(K);
		this.pi.fill(1.0 / K);
		
		this.mi = new Vector[K];
		for(int k = 0; k < K; k++) {
			this.mi[k] = dataset.samples[centroidIds[k]].data.copy();
		}
		
		this.epsilon = new Matrix[K];
		for(int k = 0; k < K; k++) {
			this.epsilon[k] = new Matrix(dataset.n);
		}
		
		this.h = new Vector[dataset.m];
		this.determinant = new double[K];
		this.invEpsilon = new Matrix[K];
	}
	
	public EMAlgorithm(Dataset dataset, int K, DataSample[] centroids) {
		this.dataset = dataset;
		this.K = K;
		
		this.pi = new Vector(K);
		this.pi.fill(1.0 / K);
		
		this.mi = new Vector[K];
		for(int k = 0; k < K; k++) {
			this.mi[k] = centroids[k].data.copy();
		}
		
		this.epsilon = new Matrix[K];
		for(int k = 0; k < K; k++) {
			this.epsilon[k] = new Matrix(dataset.n);
		}
		
		this.h = new Vector[dataset.m];
		this.determinant = new double[K];
		this.invEpsilon = new Matrix[K];
	}
	
	public EMResult run() {
		DataSample[] samples = dataset.samples;
		List<Double> LLiter = new ArrayList<>();
		
		int iter = 0;
		double ll = 0.0;
		
		if(!initEpsilon()) {
			return null;
		}
		
		for(DataSample sample : samples) {
			Vector probability = probability(sample.data);
			
			double sum = 0.0;
			for(int k = 0; k < K; k++) {
				sum += probability.get(k);
			}
			
			ll += Math.log(sum);
		}
		
		LLiter.add(ll);
		
		while(true) {
			iter++;
			
			for(int i = 0; i < dataset.m; i++) {
				h[i] = probability(samples[i].data);

				double sum = 0.0;
				for(int k = 0; k < K; k++) {
					sum += h[i].get(k);
				}
				
				h[i].multiplyToThis(1.0 / sum);
			}
			
			Vector hSum = new Vector(K);
			for(int i = 0; i < dataset.m; i++) {
				hSum.addToThis(h[i]);
			}
			
			pi.copyData(hSum).multiplyToThis(1.0 / dataset.m);
			
			for(int k = 0; k < K; k++) {
				mi[k].fill(0.0);
				
				for(int i = 0; i < dataset.m; i++) {
					mi[k].addToThis(samples[i].data.multiply(h[i].get(k)));
				}
				
				mi[k].multiplyToThis(1.0 / hSum.get(k));
			}
			
			for(int k = 0; k < K; k++) {
				epsilon[k].fill(0.0);
				
				for(int i = 0; i < dataset.m; i++) {
					Matrix diff = new Matrix(samples[i].data.subtract(mi[k]).getData());
					diff = diff.multiply(diff.transpose()).multiplyToThis(h[i].get(k));
					
					epsilon[k].addToThis(diff);
				}
				
				epsilon[k].multiplyToThis(1.0 / hSum.get(k));
			}
			
			if(!initEpsilon()) {
				return null;
			}
			
			double newLL = 0.0;
			for(DataSample sample : samples) {
				Vector probability = probability(sample.data);
				double sum = 0.0;
				
				for(int k = 0; k < K; k++) {
					sum += probability.get(k);
				}
				
				newLL += Math.log(sum);
			}
			
			if(Math.abs(ll - newLL) < 1E-5) {
				ll = newLL;
				break;
			}
			
			ll = newLL;
			LLiter.add(ll);
		}
		
		for(int i = 0; i < dataset.m; i++) {
			h[i] = probability(samples[i].data);

			double sum = 0.0;
			for(int k = 0; k < K; k++) {
				sum += h[i].get(k);
			}
			
			h[i].multiplyToThis(1.0 / sum);
		}
		
		int[] count = new int[K];
		
		for(int i = 0; i < dataset.m; i++) {
			int index = 0;
			
			for(int k = 1; k < K; k++) {
				if(h[i].get(k) > h[i].get(index)) {
					index = k;
				}
			}
			
			samples[i].currentClass = index;
			samples[i].probability = h[i].get(index);
			count[index]++;
		}
		
		return new EMResult(mi, count, iter, ll, LLiter);
	}
	
	private double probability(Vector x, int k) {
		double p = Math.pow(2 * Math.PI, -dataset.n / 2);
		p *= Math.pow(determinant[k], -0.5);
		
		Matrix diff = new Matrix(x.subtract(mi[k]).getData());
		double exp = diff.transpose().multiply(invEpsilon[k]).multiply(diff).get(0, 0);
		
		return p * Math.exp(-0.5 * exp);
	}
	
	private Vector probability(Vector x) {
		double[] values = new double[K];
		
		for(int k = 0; k < K; k++) {
			values[k] = probability(x, k) * pi.get(k);
		}
		
		return new Vector(values);
	}
	
	private boolean initEpsilon() {
		try {
			for(int k = 0; k < K; k++) {
				LUDecomposition decomposition = new LUDecomposition(
						new Array2DRowRealMatrix(epsilon[k].getData()));
				
				determinant[k] = decomposition.getDeterminant();
				invEpsilon[k] = new Matrix(decomposition.getSolver().getInverse().getData());
			}
		} catch(SingularMatrixException ex) {
			return false;
		}
		
		return true;
	}

}
