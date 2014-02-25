package hr.fer.zemris.struce.bayes;

import hr.fer.zemris.struce.data.DataAnalyzer;
import hr.fer.zemris.struce.data.DataModel;
import hr.fer.zemris.struce.data.Matrix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Pow;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class BayesClassificator {
	
	private DataModel model;
	private DataAnalyzer analyzer;
	private BayesType type;
	
	private Exp exp;
	private Pow pow;
	private Sqrt sqrt;
	
	private Set<String> classesSet;
	private Map<String, Double> aPrioriClassProbabilities;
	private Map<String, Matrix> componentMeans;
	
	private Map<String, Matrix> covMatrixes;
	
	private Matrix sharedCovMatrixInverse;
	private double sharedCovMatrixDeterminant;
	
	private Matrix diagonalCovMatrixInverse;
	private double diagonalCovMatrixDeterminant;
	
	private Matrix isotropicCovMatrixInverse;
	private double isotropicCovMatrixDeterminant;
	
	public BayesClassificator(DataModel model, BayesType type) {
		this.model = model;
		this.analyzer = new DataAnalyzer(model);
		this.type = type;
		this.classesSet = new LinkedHashSet<>();
		this.aPrioriClassProbabilities = new HashMap<>();
		this.componentMeans = new HashMap<>();
		this.covMatrixes = new HashMap<>();
		
		this.exp = new Exp();
		this.pow = new Pow();
		this.sqrt = new Sqrt();
		
		initClassesSet();
		initApriori();
		initComponentMeans();
		initCovMatrixes();
		initSharedCovMatrix();
		initDiagonalCovMatrix();
		initIsotropicCovMatrix();
	}

	private void initClassesSet() {
		classesSet.addAll(DataModel.defaultClasses);
		Set<String> otherClasses = new TreeSet<>(model.classSet);
		otherClasses.removeAll(DataModel.defaultClasses);
		classesSet.addAll(otherClasses);
	}

	private void initApriori() {
		for(String cl : model.classSet) {
			aPrioriClassProbabilities.put(cl, analyzer.getAPrioriClassProbability(cl));
		}
	}
	
	private void initComponentMeans() {
		for(String cl : model.classSet) {
			componentMeans.put(cl, analyzer.getComponentsMean(cl));
		}
	}
	
	private void initCovMatrixes() {
		for(String cl : model.classSet) {
			covMatrixes.put(cl, analyzer.getCovariationMatrix(cl));
		}
	}
	
	private void initSharedCovMatrix() {
		RealMatrix covMatrix = new Array2DRowRealMatrix(analyzer.getSharedCovMatrix().getData());
		LUDecomposition decomposition = new LUDecomposition(covMatrix);
		sharedCovMatrixInverse = new Matrix(decomposition.getSolver().getInverse().getData());
		sharedCovMatrixDeterminant = decomposition.getDeterminant();
	}
	
	private void initDiagonalCovMatrix() {
		Matrix sharedCovMatrix = analyzer.getSharedCovMatrix();
		
		for(int i = 0; i < sharedCovMatrix.getRows(); i++) {
			for(int j = 0; j < sharedCovMatrix.getCols(); j++) {
				if(i != j) {
					sharedCovMatrix.set(i, j, 0);
				}
			}
		}
		
		LUDecomposition decomposition = new LUDecomposition(
				new Array2DRowRealMatrix(sharedCovMatrix.getData()));
		
		diagonalCovMatrixInverse = new Matrix(decomposition.getSolver().getInverse().getData());
		diagonalCovMatrixDeterminant = decomposition.getDeterminant();
	}
	
	private void initIsotropicCovMatrix() {
		Matrix sharedCovMatrix = analyzer.getSharedCovMatrix();
		double sum = 0;
		
		for(int i = 0; i < sharedCovMatrix.getRows(); i++) {
			sum += sharedCovMatrix.get(i, i);
		}
		
		Matrix isotropicCovMatrix = new Matrix(model.nVariables).multiply(sum / model.nVariables);
		
		LUDecomposition decomposition = new LUDecomposition(
				new Array2DRowRealMatrix(isotropicCovMatrix.getData()));
		
		isotropicCovMatrixInverse = new Matrix(decomposition.getSolver().getInverse().getData());
		isotropicCovMatrixDeterminant = decomposition.getDeterminant();
	}
	
	public void generateAllFiles(DataModel testData) throws IOException {
		File greske = new File("../output/greske.dat");
		try {
			greske.delete();
		} catch(Exception ignorable) {}
		
		for(BayesType type : BayesType.values()) {
			this.type = type;
			generateFiles(testData);
		}
	}
	
	public void generateFiles(DataModel testData) throws IOException {
		String fileName = null;
		
		switch(type) {
		case GENERIC:
			fileName = "opceniti.dat";
			break;
			
		case SHARED_COV_MATRIX:
			fileName = "dijeljena.dat";
			break;
			
		case DIAGONAL_COV_MATRIX:
			fileName = "dijagonalna.dat";
			break;
			
		case ISOTROPIC_COV_MATRIX:
			fileName = "izotropna.dat";
			break;
		}
		
		fileName = "../output/" + fileName;
		
		Set<String> otherClasses = new TreeSet<>(model.classSet);
		otherClasses.removeAll(DataModel.defaultClasses);
		
		List<Matrix> hs = new ArrayList<>();
		
		for(int i = 0; i < testData.entries; i++) {
			Matrix entry = testData.data.rowAsMatrix(i);
			Matrix hypothesisVector = generateHypothesisVector(entry);
			hs.add(hypothesisVector);
		}
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
		
		writer.write("nar\tžuta\tzel\tplava\ttirk\tind\tmodra\tmag\t");
		for(String cl : otherClasses) {
			writer.write(cl.substring(0,4) + "\t");
		}
		writer.write("klasa\n");
		
		int wrongClassTest = 0;
		int wrongClassTrain = 0;
		int index = 0;
		
		for(Matrix hypothesisVector : hs) {
			String className = getClassName(hypothesisVector);
			
			if(!className.equals(testData.classes[index])) {
				wrongClassTest++;
			}
			
			index++;
			writer.write(hypothesisVector.transpose().toString());
			writer.write("\t" + className + "\n");
		}
		
		for(int i = 0; i < model.entries; i++) {
			String cl = getClassName(generateHypothesisVector(model.data.rowAsMatrix(i)));
			
			if(!cl.equals(model.classes[i])) {
				wrongClassTrain++;
			}
		}
		
		writer.flush();
		writer.close();
		
		writer = new PrintWriter(new BufferedWriter(new FileWriter("../output/greske.dat", true)));
		
		writer.write(type.toString() + "\t");
		writer.write(String.format("%.2f\t", wrongClassTrain / (double) model.entries));
		writer.write(String.format("%.2f\n", wrongClassTest / (double) testData.entries));
		
		writer.flush();
		writer.close();
		
		if(type == BayesType.GENERIC) {
			Collections.sort(hs, new Comparator<Matrix>() {

				@Override
				public int compare(Matrix o1, Matrix o2) {
					double max1 = o1.getMaxValue();
					double max2 = o2.getMaxValue();
					
					if(max1 > max2) {
						return 1;
					} else if(max1 < max2) {
						return -1;
					} else {
						return 0;
					}
				}
				
			});
			
			writer = new PrintWriter(new BufferedWriter(new FileWriter("../output/nejednoznacne.dat")));
			
			writer.write("nar\tžuta\tzel\tplava\ttirk\tind\tmodra\tmag\t");
			for(String cl : otherClasses) {
				writer.write(cl.substring(0,4) + "\t");
			}
			writer.write("klasa\n");
			
			for(int i = 0; i < 5; i++) {
				Matrix hypothesisVector = hs.get(i);
				writer.write(hypothesisVector.transpose().toString());
				writer.write("\t" + getClassName(hypothesisVector) + "\n");
			}
			
			writer.flush();
			writer.close();
		}
	}

	private String getClassName(Matrix hypothesisVector) {
		String cl = null;
		double best = -1.0;
		
		int i = 0;
		for(String className : classesSet) {
			double h = hypothesisVector.get(i, 0);
			
			if(h > best) {
				best = h;
				cl = className;
			}
			
			i++;
		}
		
		return cl;
	}

	private Matrix generateHypothesisVector(Matrix entry) {
		switch(type) {
		case GENERIC:
			return getGenericModelHypothesis(entry);
			
		case SHARED_COV_MATRIX:
			return getCovModelHypothesis(
					entry,
					sharedCovMatrixInverse,
					sharedCovMatrixDeterminant
					);
			
		case DIAGONAL_COV_MATRIX:
			return getCovModelHypothesis(
					entry,
					diagonalCovMatrixInverse,
					diagonalCovMatrixDeterminant
					);
			
		case ISOTROPIC_COV_MATRIX:
			return getCovModelHypothesis(
					entry,
					isotropicCovMatrixInverse,
					isotropicCovMatrixDeterminant
					);
			
		default:
			return null;
		}
	}

	private Matrix getCovModelHypothesis(Matrix entry, Matrix covMatrixInverse,
			double determinant) {
		double[] hypothesis = new double[model.nClasses];
		int i = 0;
		
		for(String cl : classesSet) {
			double pcj = aPrioriClassProbabilities.get(cl);
			
			Matrix XiMinusMi = entry.subtract(componentMeans.get(cl));
			
			double nominator = -0.5 * XiMinusMi.transpose().multiply(covMatrixInverse)
					.multiply(XiMinusMi).get(0, 0);
			
			double denominator = pow.value(2 * Math.PI, model.nVariables / 2);
			denominator *= sqrt.value(determinant);
			
			hypothesis[i] = exp.value(nominator) / denominator * pcj;
			i++;
		}
		
		return buildHypothesisVector(hypothesis);
	}

	private Matrix getGenericModelHypothesis(Matrix entry) {
		double[] hypothesis = new double[model.nClasses];
		int i = 0;
		
		for(String cl : classesSet) {
			double pcj = aPrioriClassProbabilities.get(cl);
			RealMatrix covMatrix = new Array2DRowRealMatrix(covMatrixes.get(cl).getData());
			
			Matrix XiMinusMi = entry.subtract(componentMeans.get(cl));
			
			LUDecomposition decomposition = new LUDecomposition(covMatrix);
			Matrix covMatrixInverse = new Matrix(decomposition.getSolver().getInverse().getData());
			
			double nominator = -0.5 * XiMinusMi.transpose().multiply(covMatrixInverse)
					.multiply(XiMinusMi).get(0, 0);
			
			double denominator = pow.value(2 * Math.PI, model.nVariables / 2);
			denominator *= sqrt.value(decomposition.getDeterminant());
			
			hypothesis[i] = exp.value(nominator) / denominator * pcj;
			i++;
		}
		
		return buildHypothesisVector(hypothesis);
	}
	
	private Matrix buildHypothesisVector(double[] hypothesis) {
		double sum = 0.0;
		
		for(double val : hypothesis) {
			sum += val;
		}
		
		return new Matrix(hypothesis).multiplyToThis(1.0 / sum);
	}

}
