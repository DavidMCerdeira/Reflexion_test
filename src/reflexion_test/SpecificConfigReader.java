package reflexion_test;

import java.util.HashMap;

public class SpecificConfigReader {

	HashMap<String, Object> m_Properties;
	
	SpecificConfigReader(HashMap<String, Object> properties){
		m_Properties = properties;
	}

	// get the property corresponding to the property name
	public Object getProperty(String property) {
		return m_Properties.get(property);
	}
}