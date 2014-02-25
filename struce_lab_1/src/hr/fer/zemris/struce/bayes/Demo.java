package hr.fer.zemris.struce.bayes;

import hr.fer.zemris.struce.data.DataModel;


public class Demo {
	
	public static void main(String[] args) {
		String trainPath = args[0];
		String testPath = args[1];
		
		if(trainPath.contains("=")) {
			trainPath = trainPath.split("=")[1];
			
			if(trainPath.startsWith("<") && trainPath.endsWith(">")) {
				trainPath = trainPath.substring(1, trainPath.length() - 1);
			}
		}
		
		if(testPath.contains("=")) {
			testPath = testPath.split("=")[1];
			
			if(testPath.startsWith("<") && testPath.endsWith(">")) {
				testPath = testPath.substring(1, testPath.length() - 1);
			}
		}

		DataModel trainData = new DataModel(trainPath);
		DataModel testData = new DataModel(testPath);
		
		try {
			BayesClassificator bayes = new BayesClassificator(trainData, BayesType.GENERIC);
			bayes.generateAllFiles(testData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
