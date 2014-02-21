package hr.fer.zemirs.struce.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
	
	private int size;
	private String[] words;
	private int[] count;

	public Dictionary(String path) {
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

		this.size = lines.size();
		this.words = new String[size];
		this.count = new int[size];
		
		for(int i = 0; i < this.size; i++) {
			String[] parts = lines.get(i).split("\\s+");
			
			words[i] = parts[0];
			count[i] = Integer.parseInt(parts[1]);
		}
	}
	
	public String getWord(int index) {
		return words[index];
	}
	
	public int size() {
		return size;
	}
	
}
