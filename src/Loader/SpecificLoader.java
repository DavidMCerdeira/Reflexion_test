package Loader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import ConfigReader.SpecificConfigReader;

public class SpecificLoader {
	String m_Path = "/mnt/Data/Universidade/MESTRADO_EEIC/EMBEBIDOS/EmbSys-2/Carlos-git/elaborator_xml_test/reflexion_test/HE/SpecificElaborations/";

	ClassLoader cl = null;
	// Compile source file.
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	public SpecificLoader(String path) {
		// m_Path = path;
		// must end with '/', denoting a folder
		String strURL = "file:" + m_Path;
		
		//*		
		URL url = null;
		try {
			url = new URL(strURL);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch blocklistl
			e.printStackTrace();
		}
		URL[] urls = new URL[] { url };
		/*/
		URL[] urls = getCurrentFolderFoldersURL(m_Path);		 
		//*/

		cl = new URLClassLoader(urls);
	}

	URL[] getCurrentFolderFoldersURL(String path) {
		File file = new File(path); // current folder

		if (!file.isDirectory()) { // if this file object doesn't represent a
			// folder
			System.err.println(path + " is not a directory!");
			return null;
		}
		
		String[] names = file.list();// get a list of all files and folders
		File tempFile = null;
		int size = names.length;
		URL[] urls = new URL[size];
		for(int i = 0; i < size; i++){
			tempFile = new File(path + File.separator + names[i]);
			if (tempFile.isDirectory()) { // if this file object doesn't represent a
				try {
					urls[i] = tempFile.toURI().toURL();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return urls;
	}

	public Class<?> getElaboratorClass(String CompName) {
		String compName = "Specific" + CompName; //Cannot be the same type because of namespace shadowing
		System.out.println("Loading: " + compName);
		compiler.run(null, null, null, m_Path + compName.replace(".", File.separator) + ".java");
		compName.replace(File.separator,".");
		Class<?> clss = null;
		try {
			clss = cl.loadClass(compName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return clss;
	}

	public Object LoadElaborator(String CompName, Object parameter) {

		Class<?> clss = getElaboratorClass(CompName);

		Object obj = null;
		try {
			Constructor<?> constr = clss.getConstructor(new Class[] { Object.class });
			obj = constr.newInstance(parameter);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return obj;
	}

	public void setConfigReader(Object elab, SpecificConfigReader confReadr) {
		Method setCR = null;
		try {
			setCR = elab.getClass().getMethod("setConfigReader", SpecificConfigReader.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			setCR.invoke(elab, confReadr);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// this method is not calling elaborate, it's calling another method just to
	// test
	public void elaborate(Object elab) {
		Method elaborate = null;
		try {
			elaborate = elab.getClass().getMethod("elaborate", (Class<?>[]) null);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			elaborate.invoke(elab, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
