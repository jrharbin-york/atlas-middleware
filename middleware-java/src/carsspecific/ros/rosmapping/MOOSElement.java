package carsspecific.ros.rosmapping;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MOOSElement {
	protected Map<String, List<Object>> properties = new LinkedHashMap<String, List<Object>>();  
	
	protected void writeProperties(FileWriter stream, String sep, String lineDelim) throws IOException {
		
		for (Map.Entry<String,List<Object>> entry : properties.entrySet()) {
			String name = entry.getKey();
			List<Object> values = entry.getValue();
			
			for (Object v : values) {
				stream.write(name + sep + v.toString() + lineDelim);
			}
		}
	}
	
	protected void writePropertiesDefault(FileWriter stream) throws IOException {
		writeProperties(stream, " = ", "\n");
	}
	
	public void setProperty(String key, Object value) {
        if (properties.get(key) == null) {
            List<Object> list = new ArrayList<>();
            list.add(value);
            properties.put(key, list);
        } else {
            properties.get(key).add(value);
        }
    }
	
	// Deletes all previous properties with this key 
	public void resetProperty(String key, Object value) {
		properties.remove(key);
		setProperty(key, value);
	}
	
	public Object getSingleProperty(String key) {
		List<Object> res = properties.get(key);
		if (res!=null) {
			return res.get(0);
		} else {
			return null;
		}
	}
	
	public List<Object> getAllProperties(String key) {
		return properties.get(key);
	}
}
