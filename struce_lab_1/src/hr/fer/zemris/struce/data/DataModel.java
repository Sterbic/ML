package hr.fer.zemris.struce.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DataModel {
	
	public static final Set<String> defaultClasses = new LinkedHashSet<>(
			Arrays.asList(new String[]{"Narančasta", "Žuta", "Zelena", 
					"Plava", "Tirkizna", "Indigo", "Modra", "Magenta"}
			));
	
	public Matrix data;
	public String[] classes;
	public Set<String> classSet;
	public int entries;
	public int nClasses;
	public int nVariables;
	public Map<String, List<Integer>> classValuesMap;
	
	public DataModel(String path) {
		this.classSet = new TreeSet<>();
		this.classValuesMap = new HashMap<>();
		
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(new FileInputStream(new File(path)))))) {
			String line = br.readLine();
			String[] parts = line.split("\\s+");
			
			int rows = Integer.parseInt(parts[0]);
			int row = 0;
			
			this.entries = rows;
			this.classes = new String[rows];
			this.nClasses = Integer.parseInt(parts[1]);
			
			while(true) {
				line = br.readLine();
				
				if(line == null) {
					break;
				}
				
				line = line.trim();
				
				if(line.isEmpty()) {
					continue;
				}
				
				parts = line.split("\\s+");
				
				if(this.data == null) {
					this.data = new Matrix(rows, parts.length - 1);
				}
				
				this.nVariables = parts.length - 1;
				String cl = parts[this.nVariables];

				
				for(int i = 0; i < parts.length; i++) {
					if(i != parts.length - 1) {
						this.data.set(row, i, Double.parseDouble(parts[i]));
					} else {
						classes[row] = cl;
						classSet.add(cl);
					}
				}
				
				List<Integer> indexesList = classValuesMap.get(cl);
				
				if(indexesList == null) {
					indexesList = new ArrayList<>();
					classValuesMap.put(cl, indexesList);
				}
				
				indexesList.add(row);
				row++;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getDefaultClasses() {
		Set<String> set = new LinkedHashSet<>();
		
		for(String cl : defaultClasses) {
			set.add(cl);
		}
		
		return set;
	}
	
}
