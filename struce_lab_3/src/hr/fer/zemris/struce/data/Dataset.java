package hr.fer.zemris.struce.data;

import hr.fer.zemris.struce.algebra.Vector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Dataset {
	
	public int m;
	public int n;
	public DataSample[] samples;
	
	public Dataset(String path) {
		List<String> lines = new ArrayList<>();
		
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
		this.samples = new DataSample[this.m];
		
		for(int i = 0; i < lines.size(); i++) {
			String[] parts = lines.get(i).split("\\s+");
			double[] data = new double[parts.length - 1];
			
			for(int j = 0; j < data.length; j++) {
				data[j] = Double.parseDouble(parts[j]);
			}
			
			this.samples[i] = new DataSample(new Vector(data), parts[parts.length - 1]);
		}
		
		this.n = this.samples[0].data.size();
	}
	
	public void resetClassification() {
		for(DataSample sample : samples) {
			sample.currentClass = -1;
			sample.probability = 0.0;
		}
	}

}
