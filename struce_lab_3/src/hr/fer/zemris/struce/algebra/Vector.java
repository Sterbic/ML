package hr.fer.zemris.struce.algebra;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Vector {
	
	private double[] data;
	
	public Vector(int n) {
		this.data = new double[n];
	}
	
	public Vector(double[] data) {
		this.data = data;
	}
	
	public Vector(Vector copyFrom) {
		this(Arrays.copyOf(copyFrom.data, copyFrom.data.length));
	}
	
	public int size() {
		return data.length;
	}
	
	public double get(int index) {
		return data[index];
	}
	
	public void set(int index, double value) {
		data[index] = value;
	}
	
	public void setAdd(int index, double value) {
		data[index] += value;
	}
	
	public Vector addToThis(Vector other) {
		for(int i = 0; i < data.length; i++) {
			data[i] += other.data[i];
		}
		
		return this;
	}
	
	public Vector add(Vector other) {
		return this.copy().addToThis(other);
	}
	
	public Vector subtractToThis(Vector other) {
		for(int i = 0; i < data.length; i++) {
			data[i] -= other.data[i];
		}
		
		return this;
	}

	public Vector subtract(Vector other) {
		return this.copy().subtractToThis(other);
	}
	
	public Vector multiplyToThis(Vector other) {
		for(int i = 0; i < data.length; i++) {
			data[i] *= other.data[i];
		}
		
		return this;
	}

	public Vector multiply(Vector other) {
		return this.copy().multiplyToThis(other);
	}
	
	public Vector multiplyToThis(double scalar) {
		for(int i = 0; i < data.length; i++) {
			data[i] *= scalar;
		}
		
		return this;
	}
	
	public double getSqNorm() {
		double result = 0.0;
		
		for(double value : data) {
			result += value * value;
		}
		
		return result;
	}
	
	public double distance(Vector other) {
		double result = 0.0;
		
		for(int i = 0; i < data.length; i++) {
			result += Math.pow(data[i] - other.data[i], 2);
		}
		
		return result;
	}
	
	public Vector multiply(double scalar) {
		return this.copy().multiplyToThis(scalar);
	}
	
	public void fill(double value) {
		Arrays.fill(data, value);
	}
	
	public Vector copyData(Vector other) {
		System.arraycopy(other.data, 0, data, 0, other.size());
		return this;
	}
	
	public Vector copy() {
		return new Vector(this);
	}
	
	public void print() {
		System.out.println(this.toString());
	}
	
	public boolean save(String path) {
		if(path == null) {
			throw new IllegalArgumentException("Staza datoteke ne smije biti null!");
		}
		
		try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)))) {
			writer.write(this.toString());
			writer.flush();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	public double[] getData() {
		return data;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < data.length; i++) {
			sb.append(data[i]);
			
			if(i != data.length - 1) {
				sb.append("\t");
			}
		}
		
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		return Arrays.equals(data, other.data);
	}

	public static Vector load(String path) {
		Vector vector = null;
		
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), Charset.defaultCharset());
			
			String[] parts = lines.get(0).split("\\s+");
			
			double[] data = new double[parts.length];
			
			for(int i = 0; i < parts.length; i++) {
				data[i] = Double.parseDouble(parts[i]);
			}
			
			vector = new Vector(data);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return vector;
	}

}