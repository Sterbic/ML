package hr.fer.zemris.struce;

import hr.fer.zemris.struce.algebra.Vector;
import hr.fer.zemris.struce.clustering.EMAlgorithm;
import hr.fer.zemris.struce.clustering.EMResult;
import hr.fer.zemris.struce.clustering.KMeans;
import hr.fer.zemris.struce.clustering.KMeansResult;
import hr.fer.zemris.struce.data.Configuration;
import hr.fer.zemris.struce.data.DataSample;
import hr.fer.zemris.struce.data.Dataset;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Clustering {

	public static void main(String[] args) throws IOException {
		Dataset dataset = new Dataset(args[0]);
		Configuration conf = new Configuration(args[1], 4);
		
		String outputDir = args[2];
		if(!outputDir.endsWith("/")) {
			outputDir += "/";
		}
		
		List<int[]> centroids = new ArrayList<>();
		centroids.add(new int[]{15, 4});
		centroids.add(new int[]{15, 4, 0});
		centroids.add(new int[]{15, 4, 0, 2});
		centroids.add(new int[]{15, 4, 0, 2, 9});
		
		PrintWriter writer = openWriter("kmeans-all.dat", outputDir);
		DataSample[] K4C = new DataSample[4];
		
		for(int k = 2; k <= 5; k++) {
			dataset.resetClassification();
			
			KMeansResult result = KMeans.run(k, centroids.get(k - 2), dataset);
			
			writer.write("K = " + k + "\n");
			
			for(int i = 0; i < k; i++) {
				writer.write("c" + (i + 1) + ": ");
				writer.write(vectorToString(result.centroids[i]));
				writer.write("\ngrupa " + (i + 1) + ": " + (result.count[i]) + " primjera\n");
			}
			
			writer.write("#iter: " + result.iter + "\n");
			writer.write("J: " + stringForDecimal(result.J) + "\n");
			
			if(k == 4) {
				for(int i = 0; i < 4; i++) {
					K4C[i] = new DataSample(result.centroids[i], "");
				}
				
				PrintWriter w4 = openWriter("kmeans-k4.dat", outputDir);
				
				w4.write("#iteracije: J\n--\n");
				
				for(int i = 0; i < result.Jiter.size(); i++) {
					w4.write("#" + i + ": " + stringForDecimal(result.Jiter.get(i)) + "\n");
				}
				
				w4.write("--");
				
				List<Map<String, Integer>> maps = new ArrayList<>();
				for(int i = 0; i < 4; i++) {
					maps.add(new HashMap<String, Integer>());
				}
				
				for(DataSample sample : dataset.samples) {
					Map<String, Integer> map = maps.get(sample.currentClass);
					
					Integer count = map.get(sample.clazz);
					
					if(count == null) {
						count = 0;
					}
					
					map.put(sample.clazz, count + 1);
				}
				
				for(int i = 0; i < 4; i++) {
					w4.write("\nGrupa " + (i + 1) + ": ");
					
					List<Entry<String, Integer>> list = new ArrayList<>(
							maps.get(i).entrySet());
					
					Collections.sort(list, new Comparator<Entry<String, Integer>>() {

						@Override
						public int compare(Entry<String, Integer> o1,
								Entry<String, Integer> o2) {
							return -(o1.getValue() - o2.getValue());
						}
						
					});
					
					for(int j = 0; j < list.size(); j++) {
						Entry<String, Integer> entry = list.get(j);
						
						w4.write(entry.getKey() + " " + entry.getValue());
						
						if(j != list.size() - 1) {
							w4.write(", ");
						}
					}
				}
				
				closeWriter(w4);
			}
			
			if(k != 5) {
				writer.write("--\n");
			}
		}

		closeWriter(writer);		
		writer = openWriter("em-all.dat", outputDir);
		
		for(int k = 2; k <= 5; k++) {
			dataset.resetClassification();
			
			EMAlgorithm em = new EMAlgorithm(dataset, k, centroids.get(k - 2));
			EMResult result = em.run();
			
			writer.write("K = " + k + "\n");
			
			for(int i = 0; i < k; i++) {
				writer.write("c" + (i + 1) + ": ");
				writer.write(vectorToString(result.mi[i]));
				writer.write("\ngrupa " + (i + 1) + ": " + (result.count[i]) + " primjera\n");
			}
			
			writer.write("#iter: " + result.iter + "\n");
			writer.write("log-izglednost: " + stringForDecimal(result.ll) + "\n");
			
			if(k == 4) {
				PrintWriter w4 = openWriter("em-k4.dat", outputDir);
				
				for(int i = 0; i < 4; i++) {
					w4.write("Grupa " + (i + 1) + ":\n");
					
					List<DataSample> group = new ArrayList<>();
					
					for(DataSample sample : dataset.samples) {
						if(sample.currentClass == i) {
							group.add(sample);
						}
					}
					
					Collections.sort(group, new Comparator<DataSample>() {

						@Override
						public int compare(DataSample o1, DataSample o2) {
							double diff = -(o1.probability - o2.probability);
							
							if(Math.abs(diff) < 1E-6) {
								return 0;
							} if(diff > 0) {
								return 1;
							} else {
								return -1;
							}
						}
						
					});
					
					for(DataSample sample : group) {
						w4.write(sample.clazz + " ");
						w4.write(stringForDecimal(sample.probability) + "\n");
					}
					
					if(i != 3) {
						w4.write("--\n");
					}
				}
				
				closeWriter(w4);
			}
			
			if(k != 5) {
				writer.write("--\n");
			}
		}
		
		closeWriter(writer);
		writer = openWriter("em-konf.dat", outputDir);
		
		for(int c = 0; c < conf.size(); c++) {
			dataset.resetClassification();
			
			EMAlgorithm em = new EMAlgorithm(dataset, 4, conf.get(c));
			EMResult result = em.run();
			
			if(result == null) {
				continue;
			}
			
			writer.write("Konfiguracija " + (c + 1) + ":\n");
			writer.write("log-izglednost: " + stringForDecimal(result.ll) + "\n");
			writer.write("#iteracija: " + result.iter + "\n");
			
			if(c != conf.size() - 1) {
				writer.write("--\n");
			}
		}
		
		closeWriter(writer);
		dataset.resetClassification();
		
		EMAlgorithm em = new EMAlgorithm(dataset, 4, K4C);
		EMResult result = em.run();
		
		if(result == null) {
			return;
		}
		
		writer = openWriter("em-kmeans.dat", outputDir);
		
		writer.write("#iteracije: log-izglednost\n--\n");
		
		for(int i = 0; i < result.LLiter.size(); i++) {
			writer.write("#" + i + ": " + stringForDecimal(result.LLiter.get(i)) + "\n");
		}
		
		writer.write("--");
		
		for(int i = 0; i < 4; i++) {
			writer.write("Grupa " + (i + 1) + ":\n");
			
			List<DataSample> group = new ArrayList<>();
			
			for(DataSample sample : dataset.samples) {
				if(sample.currentClass == i) {
					group.add(sample);
				}
			}
			
			Collections.sort(group, new Comparator<DataSample>() {

				@Override
				public int compare(DataSample o1, DataSample o2) {
					double diff = -(o1.probability - o2.probability);
					
					if(Math.abs(diff) < 1E-6) {
						return 0;
					} if(diff > 0) {
						return 1;
					} else {
						return -1;
					}
				}
				
			});
			
			for(DataSample sample : group) {
				writer.write(sample.clazz + " ");
				writer.write(stringForDecimal(sample.probability) + "\n");
			}
			
			if(i != 3) {
				writer.write("--\n");
			}
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
	
	private static String vectorToString(Vector vector) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < vector.size(); i++) {
			sb.append(stringForDecimal(vector.get(i)));
			
			if(i != vector.size() - 1) {
				sb.append(" ");
			}
		}
		
		return sb.toString();
	}
	
	private static String stringForDecimal(double decimal) {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(decimal);
	}
	
}
