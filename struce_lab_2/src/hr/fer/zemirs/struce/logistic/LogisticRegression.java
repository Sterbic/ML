package hr.fer.zemirs.struce.logistic;

import hr.fer.zemirs.struce.data.Dataset;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class LogisticRegression {
	
	private static final double DELTA_ETA = 0.01;
	
	private Dataset trainSet;
	private double lambda;
	private double[] theta;
	private double theta0;
	
	public LogisticRegression(Dataset trainSet, double lambda) {
		this.trainSet = trainSet;
		this.lambda = lambda;
		this.theta = new double[trainSet.nFeatures];
	}
	
	public void train() {
		Arrays.fill(theta, 0.0);
		theta0 = 0.0;
		
		double error = getCEError();
		
		double deltaEta = DELTA_ETA;
		if(lambda >= 100) {
			deltaEta /= lambda;
		}
		
		while(true) {
			double[] delta = new double[trainSet.nFeatures];
			double delta0 = 0.0;
			
			for(int i = 0; i < trainSet.m; i++) {
				Map<Integer, Double> x = trainSet.data.get(i);
				
				double h = modelOutput(x, theta0, theta);
				double diff = h - trainSet.classification[i];
				
				delta0 += diff;
				
				for(Entry<Integer, Double> pair : x.entrySet()) {
					delta[pair.getKey()] += diff * pair.getValue();
				}
			}
			
			double eta = 0.0;
			double currentError = error;
			
			while(true) {
				eta += deltaEta;
				
				if(eta >= 1.0) {
					eta = 1.0;
					break;
				}
				
				double[] thetai = Arrays.copyOf(theta, theta.length);
				mulArray(thetai, 1.0 - eta * lambda);
				thetai = scaleSubArrays(theta, delta, eta);
				
				double theta0i = theta0 - eta * delta0;
				
				double nextError = getCEError(theta0i, thetai);
				
				if(nextError > currentError) {
					eta -= deltaEta;
					break;
				}
				
				currentError = nextError;
			}
			
			mulArray(theta, 1.0 - eta * lambda);
			theta = scaleSubArrays(theta, delta, eta);
			theta0 -= eta * delta0;
			
			if(error - currentError <= 0.001) {
				break;
			}
			
			error = currentError;
		}
	}
	
	private void mulArray(double[] array, double scalar) {
		if(Math.abs(scalar - 1.0) < 1E-6) {
		//	return;
		}
		
		for(int i = 0; i < array.length; i++) {
			array[i] *= scalar;
		}
	}

	public double getCEErrorWR() {
		return getCEErrorWR(theta0, theta);
	}
	
	public double getCEErrorWR(double theta0, double[] theta) {
		double error = 0.0;
		
		for(int i = 0; i < trainSet.m; i++) {
			double h = modelOutput(trainSet.data.get(i), theta0, theta);
			int y = trainSet.classification[i];
			
			if((h == 1) && (y == 1) || (h == 0) && (y == 0)) {
				continue;
			}
			
			error += y * Math.log(h) + (1 - y) * Math.log(1 - h);
		}
		
		return -error;
	}
	
	public double getCEError() {
		return getCEError(theta0, theta);
	}
	
	private double getCEError(double theta0, double[] theta) {
		double error = 0.0;
		
		for(int i = 0; i < trainSet.m; i++) {
			double h = modelOutput(trainSet.data.get(i), theta0, theta);
			int y = trainSet.classification[i];
			
			if((h == 1) && (y == 1) || (h == 0) && (y == 0)) {
				continue;
			}
			
			error += y * Math.log(h) + (1 - y) * Math.log(1 - h);
		}
		
		double regularization = 0.0;
		
		for(int i = 0; i < theta.length; i++) {
			regularization += Math.pow(theta[i], 2);
		}
		
		return -error + lambda / 2.0 * regularization;
	}
	
	public double modelOutput(Map<Integer, Double> x, double theta0, double[] theta) {
		double result = 0.0;
		
		for(Entry<Integer, Double> pair : x.entrySet()) {
			result += theta[pair.getKey()] * pair.getValue();
		}
		
		return sigmoid(result + theta0);
	}
	
	private double sigmoid(double value) {
		return 1.0 / (1.0 + Math.exp(-value));
	}
	
	private double[] scaleSubArrays(double[] first, double[] second, double scale) {
		double[] result = new double[first.length];
		
		for(int i = 0; i < second.length; i++) {
			if(Math.abs(scale - 1.0) < 1E-6) {
				result[i] = first[i] - second[i];
			} else {
				result[i] = first[i] - scale * second[i];
			}
		}
		
		return result;
	}
	
	public double[] getTheta() {
		return Arrays.copyOf(theta, theta.length);
	}
	
	public double getTheta0() {
		return theta0;
	}

	public double getEmpiricError() {
		return getEmpiricError(trainSet);
	}
	
	public double getEmpiricError(Dataset dataset) {
		int wrong = 0;
		
		for(int i = 0; i < dataset.m; i++) {
			double output = modelOutput(dataset.data.get(i), theta0, theta);
			
			if(output >= 0.5) {
				if(dataset.classification[i] == 0) {
					wrong++;
				}
			} else {
				if(dataset.classification[i] == 1) {
					wrong++;
				}
			}
		}
		
		return wrong / (double) dataset.m;
	}
	
}
