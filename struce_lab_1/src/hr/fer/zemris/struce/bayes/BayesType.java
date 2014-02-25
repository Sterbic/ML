package hr.fer.zemris.struce.bayes;

public enum BayesType {
	GENERIC("opceniti"),
	SHARED_COV_MATRIX("dijeljena"),
	DIAGONAL_COV_MATRIX("dijagonalna"),
	ISOTROPIC_COV_MATRIX("izotropna");
	
	private String name;
	
	private BayesType(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
