package hr.fer.zemirs.struce.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dataset {
	
	public int m;
	public int nFeatures;
	public List<Map<Integer, Double>> data;
	public int classification[];
	
	private Dataset(int m, int nFeautres) {
		this.m = m;
		this.nFeatures = nFeautres;
		this.data = new ArrayList<>(m);
		this.classification = new int[m];
	}
	
	public Dataset(String path, int nFeatures) {
		List<String> lines = new ArrayList<String>();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(new FileInputStream(new File(path)))))) {			
			while(true) {
				String line = br.readLine();
				
				if(line == null) {
					break;
				}
				
				line = line.trim();
				
				if(!line.isEmpty()) {
					lines.add(line);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.m = lines.size();
		this.nFeatures = nFeatures;
		this.data = new ArrayList<>(m);
		this.classification = new int[m];
		
		for(int i = 0; i < lines.size(); i++) {
			String[] parts = lines.get(i).split("\\s+");
			
			this.classification[i] = Integer.parseInt(parts[0]);
			
			Map<Integer, Double> sample = new HashMap<Integer, Double>();
			
			for(int j = 1; j < parts.length; j++) {
				String[] pair = parts[j].split(":");
				
				int column = Integer.parseInt(pair[0]);
				double value = Double.parseDouble(pair[1]);
				
				sample.put(column, value);
			}
			
			this.data.add(sample);
		}
	}
	
	public Dataset merge(Dataset other) {
		Dataset merged = new Dataset(m + other.m, nFeatures);
		
		merged.data.addAll(data);
		merged.data.addAll(other.data);
		
		System.arraycopy(classification, 0, merged.classification, 0, m);
		System.arraycopy(other.classification, 0, merged.classification, m, other.m);
		
		return merged;
	}

}
