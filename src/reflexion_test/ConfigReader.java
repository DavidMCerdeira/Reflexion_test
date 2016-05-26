package reflexion_test;

import reflexion_test.SpecificConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigReader {
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder builder = null;

	// set of info of all components;
	private HashMap<String, ComponentInfo> compInfoMap = null;

	String pathDelimeter = "/";
	String componentArchitecturePath = "/mnt/Data/Universidade/MESTRADO_EEIC/EMBEBIDOS/EmbSys-2/Carlos-git/elaborator_xml_test/reflexion_test/HE/Arch/";
	String componenteSpecificConfigPath = "/mnt/Data/Universidade/MESTRADO_EEIC/EMBEBIDOS/EmbSys-2/Carlos-git/elaborator_xml_test/reflexion_test/HE/Elaborations/";
	// Initialize Class
	ConfigReader(String configPath, String specificConfigRootPath) throws ParserConfigurationException {
		if (builder == null) { // if there's no builder, build one
			builder = factory.newDocumentBuilder();
		}

		compInfoMap = new HashMap<String, ComponentInfo>();
		//componentArchitecturePath = configPath;
		//componenteSpecificConfigPath = specificConfigRootPath;
		
		readDirectoryTree(componentArchitecturePath, 0);// get current path
	}

	// read directory tree so we get all component names, and properties
	private void readDirectoryTree(String configRootPath, int level) {
		File file = new File(configRootPath); // current folder
		File tFile = null; // temporary file to iterate all files
		String filePath = null; // next file path holder

		if (!file.isDirectory()) { // if this file object doesn't represent a
									// folder
			System.err.println(configRootPath + " is not a directory!");
			return;
		}

		String[] names = file.list();// get a list of all files and folders

		String compArqName = ""; // hold component architecture name
		String[] folderList; // hold all folders, so we can build the component
								// architecture name: ex Comp.Sub

		if (level != 0) {// this folder doesn't represent a component
			folderList = configRootPath.split(pathDelimeter); // split path into
														// folders
			// build component architecture name
			for (int i = level; i > 0; i--) {
				if (i != level) {
					compArqName += '.';
				}
				compArqName += folderList[folderList.length - i];
			}
			try {
				ComponentInfo ci = new ComponentInfo();
				compInfoMap.put(compArqName, ci);
				ci.ArchName = compArqName;
				ci.ElaborationName = null;
				ci.PropertyList = new HashMap<String, Object>();
				readFields(configRootPath, ci);
				
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// iterate over files searching for folders. Folders represent
		// components
		for (String name : names) {
			filePath = configRootPath + name;
			// System.out.println(filePath);

			tFile = new File(filePath);
			if (tFile.isDirectory()) {
				readDirectoryTree(filePath + pathDelimeter, level + 1); // start
																		// the
																		// process
																		// again,
																		// so
																		// increase
																		// level
			}
		}
	}

	// read all properties into the table
	// TODO read component properties info based on a component node
	private void readFields(String path, ComponentInfo ci) throws FileNotFoundException, SAXException, IOException {
		String archName = ci.ArchName;
		String compName = getNameFromArchName(archName);

		String fileName = path + compName + ".xml";
		// System.out.println("File name: " + fileName);
		File file = new File(fileName); // Open a file
		Document m_Doc = builder.parse(new FileInputStream(file));

		// get all elements with tag property
		NodeList comp = m_Doc.getElementsByTagName("component");
		if (comp.getLength() > 1) {
			System.err.println("Too many components");
			return;
		}
		
		ci.Type = ((Element) comp.item(0)).getAttribute("name");
		
		//System.out.println("Specific of " + ci.Type + "." + ci.getCompName());
		ci.SpecificPropertyList = readSpecificProperties(ci.Type, ci.getCompName());
		System.out.println(ci.SpecificPropertyList);
		
		NodeList fields = comp.item(0).getChildNodes();
		
		Node temp = getNodeOfName(fields, "properties");
		if(temp != null){
			ci.PropertyList = readProperties(temp.getChildNodes());
		}
		temp = getNodeOfName(fields, "elaboration");
		if(temp != null){
			ci.ElaborationName = temp.getTextContent();
		}
	}
	
	private HashMap<String, Object> readSpecificProperties(String type, String name){
		
		String fileName = componenteSpecificConfigPath + type + pathDelimeter + name + ".xml";
		// System.out.println("File name: " + fileName);
		File file = new File(fileName); // Open a file
		Document m_Doc = null;
		if(!file.canRead()){
			return null;
		}
		
		try {
			m_Doc = builder.parse(new FileInputStream(file));
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get all elements with tag property
		NodeList comp = m_Doc.getElementsByTagName("component");
		if (comp.getLength() > 1) {
			System.err.println("Too many components");
			return null;
		}
		
		NodeList fields = comp.item(0).getChildNodes();
		
		Node temp = getNodeOfName(fields, "properties");
		if(temp != null){
			return readProperties(temp.getChildNodes());
		}
		
		return null;
	}
	
	private Node getNodeOfName(NodeList nl, String name){
		int nNodes = nl.getLength();
		Node n = null;
		for (int i = 0; i < nNodes; i++) { // iterate over all properties
			n = nl.item(i);// current node
			// make sure the node is of type element
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				if (n.getNodeName().equals(name)) {
						return n; // read a property
				} 
			}
		}
		return n;
	}

	private HashMap<String, Object> readProperties(NodeList props) {
		String name = null; // parameters needed
		Object obj = null; // returned object
		Element prop = null;
		int size = props.getLength();
		HashMap<String, Object> list = new HashMap<String, Object>();

		for (int i = 0; i < size; i++) {
			if (props.item(i).getNodeType() == Node.ELEMENT_NODE) {
				prop = (Element) props.item(i);
				// read the necessary fields
				name = prop.getAttribute("name");
				obj = getObjectFromProperty(prop);
				System.out.println(name + " = " + obj);
				list.put(name, obj); // put property into
												// component
			}
		}
		return list;
	}

	private Object getObjectFromProperty(Element prop) {
		String type, subtype = null;
		type = prop.getAttribute("type");
		Node dv = getDefaultValueNode(prop);

		if (type.equals("array")) {
			System.out.println("Array!");
			subtype = prop.getAttribute("subtype");
			int maxElements = Integer.valueOf(prop.getAttribute("size"));
			int elementCounter = 0;
			NodeList nl = dv.getChildNodes();
			Node n = null;
			int size = nl.getLength();
			ArrayList<Object> list = new ArrayList<Object>();
			for (int i = 0; i < size; i++) {
				n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if (elementCounter > maxElements) {
						System.err.println("Too many elements on array");
					}
					System.out.println("Element: " + elementCounter + " - " + n.getTextContent());
					list.add(stringToType(n.getTextContent(), subtype));
					elementCounter++;
				}
			}
			return list;
		} else {
			return stringToType(dv.getTextContent(), type);
		}
	}

	private Node getDefaultValueNode(Element e) {
		NodeList nl = e.getChildNodes();
		int size = nl.getLength();
		Node n = null;
		int i = 0;
		while (i < size) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				n = (Element) nl.item(i);
				if (n.getNodeName().equals("defaultValue")) {
					return n;
				}
			}
			i++;
		}
		return null;
	}

	// Cast value to the right type
	private Object stringToType(String val, String type) {
		Object obj = null;
		if (val.equals(""))
			return null;
		switch (type) {
		case "int":
			obj = Integer.valueOf(val);
			break;
		case "float":
			obj = Float.valueOf(val);
			break;
		case "string":
			obj = val;
			break;
		case "bool":
			obj = Boolean.valueOf(val);
			break;
		default:
			System.err.println("Unrecognized type '" + type + "'");
		}
		return obj;
	}

	// Create and return the appropriate reader
	public SpecificConfigReader getSpecificConfigReader(String compArchName) {
		return new SpecificConfigReader(compInfoMap.get(compArchName).SpecificPropertyList);
	}

	// Get the name of the Elaboration class
	public String getElabName(String archName) {
		return compInfoMap.get(archName).ElaborationName;
	}

	// Get a components name based on it's architectural name
	public String getNameFromArchName(String archName) {
		String[] arqNameSplitd = archName.split("\\."); // Match the '.'
														// caracter
		int len = arqNameSplitd.length;
		return arqNameSplitd[len - 1];
	}

	public Object getProperty(String compArchName, String property) {
		return compInfoMap.get(compArchName).PropertyList.get(property);
	}

	/* TODO get array size: "compname#" + index */
	public int getArrayListSize(String compArrayArchName) {
		int size = 0;
		/*
		 * Debug System.out.println("getArrayListSize started"); do{
		 * System.out.println("Checking: " + compArrayArchName + "#" +
		 * Integer.valueOf(size)); } //
		 */
		while (compInfoMap.containsKey(compArrayArchName + "#" + Integer.valueOf(size++)))
			;

		return size - 1;
	}
}

class ComponentInfo {
	String ArchName; // Component name in the reference architecture
	String Type;
	String ElaborationName; // Elaboration name
	HashMap<String, Object> PropertyList; // List of component properties
	HashMap<String, Object> SpecificPropertyList;
	
	public String getCompName(){
		String str[] =  ArchName.split("\\.");
		int len = str.length;
		
		if(len == 1)
			return ArchName;
		
		return str[len-1];
	}
}