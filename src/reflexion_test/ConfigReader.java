package reflexion_test;

import reflexion_test.SpecificConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

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

	// Initialize Class
	ConfigReader(String path) throws ParserConfigurationException {
		if (builder == null) { // if there's no builder, build one
			builder = factory.newDocumentBuilder();
		}

		compInfoMap = new HashMap<String, ComponentInfo>();
		/*
		 * try { path = (path == null) ?
		 * getClass().getResource("").toURI().getPath() : path; } catch
		 * (URISyntaxException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } /
		 */
		path = componentArchitecturePath;
		// */
		readDirectoryTree(path, 0);// get current path
	}

	// read directory tree so we get all component names, and properties
	private void readDirectoryTree(String rootPath, int level) {
		File file = new File(rootPath); // current folder
		File tFile = null; // temporary file to iterate all files
		String filePath = null; // next file path holder

		if (!file.isDirectory()) { // if this file object doesn't represent a
									// folder
			System.err.println(rootPath + " is not a directory!");
			return;
		}

		String[] names = file.list();// get a list of all files and folders

		String compArqName = ""; // hold component architecture name
		String[] folderList; // hold all folders, so we can build the component
								// architecture name: ex Comp.Sub

		if (level != 0) {// this folder doesn't represent a component
			folderList = rootPath.split(pathDelimeter); // split path into
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

				readFields(rootPath, ci);
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// iterate over files searching for folders. Folders represent
		// components
		for (String name : names) {
			filePath = rootPath + name;
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

		NodeList comp = m_Doc.getElementsByTagName("component"); // get all
																	// elements
																	// with
																	// tag:
																	// property
		if (comp.getLength() > 1) {
			System.err.println("Too many components");
			return;
		}

		Node field = null; // Auxiliary iterator

		NodeList fields = comp.item(0).getChildNodes();
		int nNodes = fields.getLength();
		for (int i = 0; i < nNodes; i++) { // iterator over all properties
			field = fields.item(i);// current node
			if (field.getNodeType() == Node.ELEMENT_NODE) { // make sure the
															// node is of
															// type element
				if (field.getNodeName() == "properties") {
					// System.out.println("Reading property");
					readProperties((NodeList) field, ci); // read a property
				} else if (field.getNodeName() == "elaboration") {
					// System.out.print("Reading elaboration: ");
					ci.ElaborationName = ((Element) field).getAttribute("name");
					// ystem.out.println(ci.ElaborationName);
				}
			}
		}
	}

	private void readProperties(NodeList props, ComponentInfo ci) {
		String name = null; // parameters needed
		Object obj = null; // returned object
		Element prop = null;
		int size = props.getLength();

		for (int i = 0; i < size; i++) {
			if (props.item(i).getNodeType() == Node.ELEMENT_NODE) {
				prop = (Element) props.item(i);
				// read the necessary fields
				name = prop.getAttribute("name");
				obj = getObjectFromProperty(prop);
				System.out.println(name + " = " + obj);
				ci.PropertyList.put(name, obj); // put property into
												// component
			}
		}
	}

	private Object getObjectFromProperty(Element prop) {
		Object obj = null;
		String value, type, subtype = null;
		type = prop.getAttribute("type");
		if (type.equals("list")) {
			subtype = prop.getAttribute("subtype");
			NodeList nl = prop.getChildNodes();
			int size = nl.getLength();
			ArrayList<Object> list = new ArrayList<Object>();
			for(int i = 0; i < size; i++){
				if(nl.item(i).getNodeType() == Node.ELEMENT_NODE){
					list.add(getAtomicProperty((Element)nl.item(i), subtype));
				}
			}
			obj = (Object)list;
		} else {
			obj = getAtomicProperty(prop, type);
		}
		return obj;
	}

	private Object getAtomicProperty(Element prop, String type){
		Object obj = null;
		String value = prop.getTextContent();
		if (!value.equals("")) {
			obj = stringToType(value, type); // convert from string to
												// the right type
		} else {
			value = prop.getAttribute("defaultValue");
			if (!value.equals("")) {
				if (value != "") {
					obj = stringToType(value, type);
				} else {
					obj = null;
				}
			}
		}
		return obj;
	}

	// Cast value to the right type
	private Object stringToType(String val, String type) {
		Object obj = null;
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
	public SpecificConfigReader getConfigReader(String compArchName) {
		return new SpecificConfigReader(compInfoMap.get(compArchName).PropertyList);
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
	String ElaborationName; // Elaboration name
	HashMap<String, Object> PropertyList; // List of component properties
}