package hr.fer.zemris.struce.data;

import java.util.List;

public class DataAnalyzer {
	
	private DataModel model;
	
	public DataAnalyzer(DataModel model) {
		this.model = model;
	}
	
	public double getAPrioriClassProbability(String className) {
		double n = model.classValuesMap.get(className).size();
		return n / model.entries;
	}
	
	public Matrix getComponentsMean(String className) {
		double[] means = new double[model.data.getCols()];
		List<Integer> classIndexes = model.classValuesMap.get(className);
		
		for(int i : classIndexes) {
			for(int j = 0; j < model.data.getCols(); j++) {
				means[j] += model.data.get(i, j);
			}
		}
		
		return new Matrix(means).multiplyToThis(1.0 / classIndexes.size());
	}
	
	public Matrix getComponentsSigmaSquared(String className) {
		double[] sigmas = new double[model.data.getCols()];
		List<Integer> classIndexes = model.classValuesMap.get(className);
		
		Matrix means = getComponentsMean(className);
		
		for(int i : classIndexes) {
			for(int j = 0; j < model.data.getCols(); j++) {
				sigmas[j] += Math.pow(model.data.get(i, j) - means.get(j, 0), 2);
			}
		}
		
		return new Matrix(means).multiplyToThis(1.0 / classIndexes.size());
	}
	
	public Matrix getCovariationMatrix(String className) {
		Matrix covMatrix = new Matrix(model.nVariables, model.nVariables);
		List<Integer> classIndexes = model.classValuesMap.get(className);
		
		Matrix means = getComponentsMean(className);
		
		for(int i : classIndexes) {
			Matrix XiMinusMi = model.data.rowAsMatrix(i).subtractToThis(means);
			
			covMatrix.addToThis(XiMinusMi.multiply(XiMinusMi.transpose()));
		}
		
		return covMatrix.multiplyToThis(1.0 / classIndexes.size());
	}
	
	public Matrix getSharedCovMatrix() {
		Matrix covMatrix = new Matrix(model.nVariables, model.nVariables);
		
		for(String cl : model.classSet) {
			Matrix covForClass = getCovariationMatrix(cl);
			double classProbability = getAPrioriClassProbability(cl);
			
			covMatrix.addToThis(covForClass.multiplyToThis(classProbability));
		}
		
		return covMatrix;
	}

}
