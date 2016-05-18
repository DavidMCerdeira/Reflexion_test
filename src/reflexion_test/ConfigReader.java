package reflexion_test;

import reflexion_test.SpecificConfigReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
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
		readDirectoryTree(path, 0);// get
									// current
									// path
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
				readSpecificElaboration(ci);
				ci.PropertyList = new HashMap<String, Object>();

				readProperties(rootPath, ci);
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

	// TODO read component elaboration info based on a component node
	private String readSpecificElaboration(ComponentInfo ci) {
		return "";
	}

	// read all properties into the table
	// TODO read component properties info based on a component node
	private void readProperties(String path, ComponentInfo ci) throws FileNotFoundException, SAXException, IOException {
		String archName = ci.ArchName;
		String compName = getNameFromArchName(archName);

		String fileName = path + compName + ".xml";
		// System.out.println("File name: " + fileName);
		File file = new File(fileName); // Open a file
		Document m_Doc = builder.parse(new FileInputStream(file));

		HashMap<String, Object> tempMap = ci.PropertyList;

		NodeList list = m_Doc.getElementsByTagName("property"); // get all
																// elements with
																// tag: property
		Node tempNode = null; // Auxiliary iterator
		Element elmnt; // Auxiliary so we don't have to cast multiple times
		NodeList nL; // Auxiliary list for child nodes
		String value, type, name = null; // parameters needed
		Object obj; // returned object

		int nNodes = list.getLength();// number of properties
		for (int i = 0; i < nNodes; i++) { // iterator over all properties
			tempNode = list.item(i);// current node
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) { // make sure the
																// node is of
																// type element
				elmnt = (Element) tempNode; // make a cast to alleviate syntax
				// read the necessary fields
				name = elmnt.getAttribute("name");
				type = elmnt.getAttribute("type");

				nL = tempNode.getChildNodes(); // list with child nodes so we
												// can read defaultValue
				value = getDefaultValue(nL); // read the default value from
												// child nodes
				if (value != "") {
					obj = stringToType(value, type); // convert from string to
														// the right type
					System.out.println(type + " " + name + " = " + value);
					tempMap.put(name, obj); // put property into
											// component
				}
			}
		}
	}

	// search for the default value in node list
	private String getDefaultValue(NodeList nL) {
		int length = nL.getLength(); // number of child nodes
		Node item; // temporary node
		for (int i = 0; i < length; i++) {
			item = nL.item(i);
			if (item instanceof Element) { // make sure its the type of node we
											// need
				if (item.getNodeName() == "defaultValue") { // we are looking
															// for
															// the default
															// values
					// System.out.println(
					// "Node name: " + item.getNodeName() + " → " + ((Element)
					// item).getAttribute("value"));
					return ((Element) item).getAttribute("value");
				}
			}
		}

		return "";

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
	public String getElabName(String archName){
		return compInfoMap.get(archName).ElaborationName;
	}
	
	// Get a components name based on it's architectural name
	public String getNameFromArchName(String archName) {
		System.out.println("Component: " + archName);
		String[] arqNameSplitd = archName.split("\\."); // Match the '.'
														// caracter
		int len = arqNameSplitd.length;
		return arqNameSplitd[len - 1];
	}
	
	public Object getProperty(String compArchName, String property){
		return compInfoMap.get(compArchName).PropertyList.get(property);
	}
}

class ComponentInfo {
	String ArchName; // Component name in the reference architecture
	String ElaborationName; // Elaboration name
	HashMap<String, Object> PropertyList; // List of component properties
}