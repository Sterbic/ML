package hr.fer.zemirs.struce;

import hr.fer.zemirs.struce.data.Dataset;
import hr.fer.zemirs.struce.data.Dictionary;
import hr.fer.zemirs.struce.logistic.LogisticRegression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextClassification {
	
	private static double[] lambdas = new double[]{0, 0.1, 1, 5, 10, 100, 1000};
	
	public static void main(String[] args) throws IOException {
		Dictionary dictionary = new Dictionary(args[0]);
		
		Dataset train = new Dataset(args[1], dictionary.size());
		Dataset ce = new Dataset(args[2], dictionary.size());
		Dataset test = new Dataset(args[3], dictionary.size());
		
		String outputDir = args[4];
		if(!outputDir.endsWith("/")) {
			outputDir += "/";
		}
		
		LogisticRegression lr0 = new LogisticRegression(train, 0.0);
		lr0.train();
		
		PrintWriter writer = openWriter("tezine1.dat", outputDir);
		writer.write(stringForDecimal(lr0.getTheta0()) + "\n");
		
		for(double theta : lr0.getTheta()) {
			writer.write(stringForDecimal(theta) + "\n");
		}

		writer.write("EE: " + stringForDecimal(lr0.getEmpiricError()) + "\n");
		writer.write("CEE: " + stringForDecimal(lr0.getCEErrorWR()));
		
		closeWriter(writer);
		
		LogisticRegression[] lrs = new LogisticRegression[lambdas.length];
		double errors[] = new double[lambdas.length];
		
		for(int i = 0; i < lambdas.length; i++) {
			lrs[i] = new LogisticRegression(train, lambdas[i]);
			lrs[i].train();
			errors[i] = lrs[i].getEmpiricError(ce);
		}
		
		writer = openWriter("optimizacija.dat", outputDir);
		int optimal = 0;
		
		for(int i = 0; i < lambdas.length; i++) {
			writer.write("\u03BB" + " = " + lambdas[i] + ", " + stringForDecimal(errors[i]) + "\n");
			
			if(errors[i] <= errors[optimal]) {
				optimal = i;
			}
		}
		
		writer.write("optimalno: " + "\u03BB" + " = " + lambdas[optimal]);
		
		closeWriter(writer);
		
		Dataset combined = train.merge(ce);
		
		LogisticRegression lr1 = new LogisticRegression(combined, lambdas[optimal]);
		
		lr1.train();
		double theta0 = lr1.getTheta0();
		double[] thetas = lr1.getTheta();
		
		writer = openWriter("tezine2.dat", outputDir);
		writer.write(stringForDecimal(theta0) + "\n");
		
		for(double theta : thetas) {
			writer.write(stringForDecimal(theta) + "\n");
		}

		writer.write("EE: " + stringForDecimal(lr1.getEmpiricError()) + "\n");
		writer.write("CEE: " + stringForDecimal(lr1.getCEErrorWR()));
		
		closeWriter(writer);
		
		writer = openWriter("ispitni-predikcije.dat", outputDir);
		int wrong = 0;
		
		for(int i = 0; i < test.m; i++) {
			if(lr1.modelOutput(test.data.get(i), theta0, thetas) >= 0.5) {
				writer.write("1\n");
				
				if(test.classification[i] == 0) {
					wrong++;
				}
			} else {
				writer.write("0\n");
				
				if(test.classification[i] == 1) {
					wrong++;
				}
			}
		}
		
		writer.write("Greška: " + stringForDecimal(wrong / (double) test.m));
		
		closeWriter(writer);
		
		writer = openWriter("rijeci.txt", outputDir);
		
		List<Theta> list = new ArrayList<>();
		
		for(int i = 1 ; i < thetas.length; i++) {
			list.add(new Theta(thetas[i], i));
		}
		
		Collections.sort(list);
		
		for(Theta t : list.subList(0, 20)) {
			writer.write(dictionary.getWord(t.index) + "\n");
		}
		
		closeWriter(writer);
	}
	
	private static PrintWriter openWriter(String file, String dir) throws IOException {
		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				new File(dir + file)), StandardCharsets.UTF_8));
	}
	
	private static void closeWriter(PrintWriter writer) {
		writer.flush();
		writer.close();
	}
	
	private static String stringForDecimal(double decimal) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(decimal);
	}

	private static class Theta implements Comparable<Theta> {
		
		private double value;
		private int index;
		
		public Theta(double value, int index) {
			this.value = value;
			this.index = index;
		}
		
		@Override
		public int compareTo(Theta o) {
			double diff = -(value - o.value);
			
			if(Math.abs(diff) < 1E-6) {
				return 0;
			} if(diff < 0) {
				return -1;
			} else {
				return 1;
			}
		}
		
	}
	
}
