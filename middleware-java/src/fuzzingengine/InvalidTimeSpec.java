package fuzzingengine;

public class InvalidTimeSpec extends Exception {
	private String nature;
		
	public InvalidTimeSpec(String nature) {
		this.nature = nature;
	}
	
	public String getNature() {
		return nature;
	}
}