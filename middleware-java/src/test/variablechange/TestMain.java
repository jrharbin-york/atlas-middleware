package test.variablechange;

import fuzzingengine.*;
import middleware.core.CARSVariableUpdate;
import middleware.core.CARSVariableUpdate.VariableInvalid;

public class TestMain {
	public static void testReplace(String invalue) {
		System.out.println("before = " + invalue);
		String out = invalue.replaceAll("A", "B");
		System.out.println("after=" + out);	
	}
	
	public static void main(String [] args) {
		testReplace("A");
		//FuzzingEngine vf = new NumericVariableChangeFuzzingEngine(0.0);
		
		CARSVariableUpdate mup;
		try {
			mup = new CARSVariableUpdate("ella", "TEST=q=23.5", 12.5);
			//CARSVariableUpdate mupChanged = vf.fuzzMessage(mup);
			//System.out.println("changed value = " + mupChanged.getValue());
		} catch (VariableInvalid e) {
			e.printStackTrace();
		}
	}
}
