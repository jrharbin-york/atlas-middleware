package carsspecific.ros.rosmapping;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MOOSSetModeDetails extends MOOSElement {
	private String modeName;
	private String extraTag = "";
	
	MOOSSetModeDetails(String modeName, String extraTag) {
		this.modeName = modeName;
		this.extraTag = extraTag;
	}
	
	MOOSSetModeDetails(String modeName) {
		this.modeName = modeName;
	}
	
	protected void generate(FileWriter stream) throws IOException {
		stream.write("set MODE = " + modeName + " {\n");
		
		for (Map.Entry<String,List<Object>> entry : properties.entrySet()) {
			String name = entry.getKey();
			List<Object> values = entry.getValue();
			
			for (Object v : values) {
				stream.write(name + " = " + v.toString() + "\n");
			}
		}
		stream.write("} " + extraTag + "\n\n");
	}
}