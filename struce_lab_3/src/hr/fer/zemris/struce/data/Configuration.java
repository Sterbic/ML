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

public class Configuration {

	private List<DataSample[]> configurations;
	
	public Configuration(String path, int splitSize) {
		List<String> lines = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(new FileInputStream(new File(path)))))) {			
			while(true) {
				String line = br.readLine();
				
				if(line == null) {
					break;
				}
				
				line = line.trim();
				
				if(line.isEmpty() || line.startsWith("Konfiguracija")) {
					continue;
				}
				
				lines.add(line);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.configurations = new ArrayList<>();
		
		DataSample[] conf = new DataSample[splitSize];
		
		for(int i = 0; i < lines.size(); i++) {
			String[] parts = lines.get(i).split("\\s+");
			double[] data = new double[parts.length - 1];
			
			for(int j = 0; j < data.length; j++) {
				data[j] = Double.parseDouble(parts[j]);
			}
			
			conf[i % splitSize] = new DataSample(new Vector(data), parts[parts.length - 1]);
			
			if((i + 1) % splitSize == 0) {
				this.configurations.add(conf);
				conf = new DataSample[splitSize];
			}
		}
	}
	
	public int size() {
		return configurations.size();
	}
	
	public DataSample[] get(int index) {
		return configurations.get(index);
	}
		
}
