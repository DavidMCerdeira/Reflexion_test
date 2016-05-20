package reflexion_test;

import reflexion_test.SpecificLoader;
import reflexion_test.ConfigReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;

import reflexion_test.SpecificConfigReader;

public class Main {
	public static void main(String[] args) {
		
		SpecificLoader sl = new SpecificLoader("");
		
		SpecificConfigReader er = null;
		ConfigReader cr = null;
		try {
			cr = new ConfigReader("");
			er = cr.getConfigReader("CI.SCa");
			System.out.println("array is of size: " + cr.getArrayListSize("CI.array"));
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Object obj = sl.LoadElaborator("P8051");
		sl.setConfigReader(obj, er);
		sl.elaborate(obj);
	}
}
