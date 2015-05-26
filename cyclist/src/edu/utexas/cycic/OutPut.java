package edu.utexas.cycic;

import java.io.*;
import java.util.ArrayList;

import javafx.scene.control.Label;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.utah.sci.cyclist.core.ui.views.ChartView;
/**
 * Output class for the CYCIC GUI.
 * @author Robert
 *
 */
public class OutPut {
	static Logger log = Logger.getLogger(OutPut.class);
	/**
	 * Function to convert the information stored in the CYCIC
	 * simulation into a Cyclus input file. 
	 */
	public static void output(File file){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder= docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("simulation");
			doc.appendChild(rootElement);

			// General Simulation Information
			Element control = doc.createElement("control");
			rootElement.appendChild(control);
			controlSetup(doc, control);

			//Archetypes 
			archetypeSetup(doc, rootElement);

			// Commodities
			for(CommodityNode commod: CycicScenarios.workingCycicScenario.CommoditiesList){
				commodityBuilder(doc, rootElement, commod);
			}			
			// Facilities
			for(facilityNode facility : CycicScenarios.workingCycicScenario.FacilityNodes){
				Element facID = doc.createElement("facility");
				facilityBuilder(doc, facID, facility);
				rootElement.appendChild(facID);
			}
			// Regions
			for(regionNode region : CycicScenarios.workingCycicScenario.regionNodes) {
				Element regionID = doc.createElement("region");
				rootElement.appendChild(regionID);
				regionBuilder(doc, regionID, region.name, region.regionStruct, region.regionData, region.archetype.split(":")[2]);
				// Building the institutions within regions.
				for (instituteNode institution: CycicScenarios.workingCycicScenario.institNodes){
					for (String instit: region.institutions){
						if (institution.name.equalsIgnoreCase(instit)) {
							Element institID = doc.createElement("institution");
							regionID.appendChild(institID);
							Element initFacList = doc.createElement("initialfacilitylist");
							for(facilityItem facility: institution.availFacilities) {
								Element entry = doc.createElement("entry");
								Element initProto = doc.createElement("prototype");
								initProto.appendChild(doc.createTextNode(facility.name));
								entry.appendChild(initProto);
								Element number = doc.createElement("number");
								number.appendChild(doc.createTextNode(facility.number));
								entry.appendChild(number);
								initFacList.appendChild(entry);
							}
							institID.appendChild(initFacList);
							regionBuilder(doc, institID, institution.name, institution.institStruct, institution.institData, institution.archetype.split(":")[2]);
						}
					}
				}
			}

			//Recipes
			for(Nrecipe recipe : CycicScenarios.workingCycicScenario.Recipes){
				recipeBuilder(doc, rootElement, recipe);
			}

			// Writing out the xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce){
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}
	
	/**
	 * Sets up the control information for the simulation.
	 * @param doc The xml.parser document that controls the cyclus 
	 * input document.
	 * @param control Element in the xml.parser document the simulation 
	 * information will be added to.
	 */
	public static void controlSetup(Document doc, Element control){
		
		Element simDuration = doc.createElement("duration");
		
		simDuration.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.duration));
		control.appendChild(simDuration);

		Element simStartMon = doc.createElement("startmonth");
		simStartMon.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.startMonth));
		control.appendChild(simStartMon);
			
		Element simStartYear = doc.createElement("startyear");
		simStartYear.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.startYear));
		control.appendChild(simStartYear);	
	}
	
	/**
	 * 
	 * @param doc
	 * @param rootElement
	 */
	public static void archetypeSetup(Document doc, Element rootElement){
		Element archetypes = doc.createElement("archetypes");
		rootElement.appendChild(archetypes);
		
		for(facilityNode facility: CycicScenarios.workingCycicScenario.FacilityNodes){
			Element spec = doc.createElement("spec");
			String[] fullPath = facility.archetype.split(":");
			if(!fullPath[0].equalsIgnoreCase("")){
				Element path = doc.createElement("path");
				path.setTextContent(fullPath[0]);
				spec.appendChild(path);
			}
			Element lib = doc.createElement("lib");
			lib.setTextContent(fullPath[1]);
			spec.appendChild(lib);
			Element name = doc.createElement("name");
			name.setTextContent(fullPath[2]);
			spec.appendChild(name);
			archetypes.appendChild(spec);
		}	
		for(regionNode region: CycicScenarios.workingCycicScenario.regionNodes){
			Element spec = doc.createElement("spec");
			Element lib = doc.createElement("lib");
			System.out.println(region.archetype);
			String[] fullPath = region.archetype.split(":");
			
			if(!fullPath[0].equalsIgnoreCase("")){
				Element path = doc.createElement("path");
				path.setTextContent(fullPath[0]);
				spec.appendChild(path);
			}
			lib.setTextContent(fullPath[1]);
			spec.appendChild(lib);
			Element name = doc.createElement("name");
			name.setTextContent(fullPath[2]);
			spec.appendChild(name);
			archetypes.appendChild(spec);
		}
		for(instituteNode instit: CycicScenarios.workingCycicScenario.institNodes){
			Element spec = doc.createElement("spec");
			String[] fullPath = instit.archetype.split(":");
			if(!fullPath[0].equalsIgnoreCase("")){
				Element path = doc.createElement("path");
				path.setTextContent(fullPath[0]);
				spec.appendChild(path);
			}
			Element lib = doc.createElement("lib");
			lib.setTextContent(fullPath[1]);
			spec.appendChild(lib);
			Element name = doc.createElement("name");
			name.setTextContent(fullPath[2]);
			spec.appendChild(name);
			archetypes.appendChild(spec);
		}
	}

	/**
	 * Function used to create commodities in the Cyclus input xml.
	 * @param doc The xml.parser document that controls the cyclus input document.
	 * @param rootElement The element that will serve as the heading for 
	 * substructures built in this function.
	 * @param commodity Label containing the commodity name.
	 */
	public static void commodityBuilder(Document doc, Element rootElement, CommodityNode commodity){
		Element commod = doc.createElement("commodity");
		Element commodName = doc.createElement("name");
		commodName.appendChild(doc.createTextNode(commodity.name.getText()));
		commod.appendChild(commodName);
		Element commodPrior = doc.createElement("solution_priority");
		commodPrior.appendChild(doc.createTextNode(commodity.priority.toString()));
		commod.appendChild(commodPrior);
		rootElement.appendChild(commod);
	}
	
	/**
	 * Function used to add recipes to the Cyclus input xml.
	 * @param doc The xml.parser document that controls the cyclus input document.
	 * @param rootElement The element that will serve as the heading for 
	 * substructures built in this function.
	 * @param recipe The Cyclus recipe being added to the input xml.
	 */
	public static void recipeBuilder(Document doc, Element rootElement, Nrecipe recipe){
		Element recipeEle = doc.createElement("recipe");
		rootElement.appendChild(recipeEle);
		
		Element recipeName = doc.createElement("name");
		recipeName.appendChild(doc.createTextNode(recipe.Name));
		recipeEle.appendChild(recipeName);
		
		Element recipeBasis = doc.createElement("basis");
		recipeBasis.appendChild(doc.createTextNode(recipe.Basis));
		recipeEle.appendChild(recipeBasis);
		
		for(isotopeData iso : recipe.Composition){
			Element isotope = doc.createElement("nuclide");
			recipeEle.appendChild(isotope);
			
			Element isoID = doc.createElement("id");
			isoID.appendChild(doc.createTextNode(iso.Name));
			isotope.appendChild(isoID);
			
			Element isoComp = doc.createElement("comp");
			if (recipe.Basis == "mass"){
				isoComp.appendChild(doc.createTextNode(String.format("%f2", iso.mass)));
			} else {
				isoComp.appendChild(doc.createTextNode(String.format("%f2", iso.atom)));
			}
			isotope.appendChild(isoComp);
		}
	}
	
	/**
	 * Function responsible for adding facilities to the cyclus xml input. 
	 * @param doc The xml.parser document that controls the cyclus 
	 * input document.
	 * @param rootElement The element that will serve as the heading for 
	 * substructures built in this function.
	 * @param facArray The ArrayList<Object> containing the facility structure.
	 * @param dataArray The ArrayList<Object> containing the facility data. 
	 * @param facType A string that indicates the type of the facility. 
	 */
	@SuppressWarnings("unchecked")
	public static void facilityBuilder(Document doc, Element rootElement, facilityNode facility){
		String facType = facility.archetype.split(":")[2];
		String facName = (String) facility.name;
		ArrayList<Object> facArray = facility.facilityStructure;
		ArrayList<Object> dataArray = facility.facilityData;
		
		Element name = doc.createElement("name");
		name.setTextContent(facName);
		rootElement.appendChild(name);
		
		Element config = doc.createElement("config");
		rootElement.appendChild(config);
		
		Element configType = doc.createElement(facType.replace(" ", "").toString());
		config.appendChild(configType);
		
		for(int i = 0; i < dataArray.size(); i++){
			if (dataArray.get(i) instanceof ArrayList){
				facilityDataElement(doc, configType, (ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i));
			} else {
				// Adding the label
				if((Boolean) facArray.get(10) == false){
					break;
				}
				Element heading = doc.createElement((String) facArray.get(0));
				heading.appendChild(doc.createTextNode((String) dataArray.get(0)));
				configType.appendChild(heading);
			}
		}
	}
	
	/**
	 * Special function for adding the <name> field to the facility 
	 * input definition.
	 * @param doc The xml.parser document that controls the cyclus 
	 * input document.
	 * @param dataArray ArrayList<Object> that contains the name 
	 * information for the facility.
	 * @return Element added to the facility rootElement.
	 */
	
	public static Element facilityNameElement(Document doc, String name){
		Element nameElement = doc.createElement("name");
		nameElement.appendChild(doc.createTextNode(name));
		return nameElement;
	}
	
	/**
	 * This function produces a xml element that contains the 
	 * information for a facility input.
	 * @param doc The xml.parser document that controls the cyclus 
	 * input document.
	 * @param rootElement The element that will serve as the heading for 
	 * substructures built in this function.
	 * @param facArray ArrayList<Object> containing the 
	 * facility input field information.
	 * @param dataArray ArrayList<Object> containing the data associated 
	 * with the input field information.
	 */
	@SuppressWarnings("unchecked")
	public static void facilityDataElement(Document doc, Element rootElement, ArrayList<Object> facArray, ArrayList<Object> dataArray){
		for (int i = 0; i < dataArray.size(); i++){
			if (dataArray.get(i) instanceof ArrayList){
				if (facArray.size() > 2 && !(facArray.get(2) instanceof ArrayList)){ 
					if((Boolean) facArray.get(10) == false){
						break;
					}
					if (indentCheck((String) facArray.get(2))){
						Element tempElement = doc.createElement((String) facArray.get(0).toString().replace(" ", ""));
						rootElement.appendChild(tempElement);
						for(int j = 0; j < dataArray.size(); j++){
							facilityDataElement(doc, tempElement, (ArrayList<Object>) facArray.get(1), (ArrayList<Object>) dataArray.get(j));
						}
						break;
						
					}
				} else {
					facilityDataElement(doc, rootElement, (ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i));
				}
			} else {
				if((Boolean) facArray.get(10) == false){
					break;
				}
				Element name = doc.createElement((String) facArray.get(0).toString().replace(" ", "").replace("\"", ""));
				name.appendChild(doc.createTextNode((String)dataArray.get(0)));
				rootElement.appendChild(name);
			}
		}
	}
	
	/**
	 * Function for doing proper indentation for the special 
	 * oneOrMore, zeroOrMore input fields. 
	 * @param string String used to test for indentation.
	 * @return Boolean to indicate whether a indent is required. 
	 */
	public static boolean indentCheck(String string){
		if(string == "oneOrMore" || string == "zeroOrMore"||string == "oneOrMoreMap" || string == "pair" || string == "item"){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param doc The xml.parser document that controls the 
	 * cyclus input document.
	 * @param rootElement The element that will serve as the heading for 
	 * substructures built in this function.
	 * @param structArray Region or Institution array used to 
	 * indicate the name for the xml tag.
	 * @param dataArray ArrayList<Object> containing the data 
	 * to be input into the xml tag. 
	 * @param nodeType String containing information about 
	 * the type of the node. 
	 */
	@SuppressWarnings("unchecked")
	public static void regionBuilder(Document doc, Element rootElement, String name, ArrayList<Object> structArray, ArrayList<Object> dataArray, String nodeType){
		rootElement.appendChild(facilityNameElement(doc, name));
		Element model = doc.createElement("config");
		rootElement.appendChild(model);
		
		Element modelType = doc.createElement(nodeType.replace(" ", "").toString());
		model.appendChild(modelType);
		
		for(int i = 0; i < dataArray.size(); i++){
			if (dataArray.get(i) instanceof ArrayList){
				facilityDataElement(doc, modelType, (ArrayList<Object>) structArray.get(i), (ArrayList<Object>) dataArray.get(i));
			} else {
				// Adding the label
				Element heading = doc.createElement((String) structArray.get(0));
				heading.appendChild(doc.createTextNode((String) dataArray.get(0)));
				modelType.appendChild(heading);
			}
		}
	}
	
	public static void saveFile(Document doc, Element rootElement){
			Element cycicElement = doc.createElement("CycicSimulation");
			rootElement.appendChild(cycicElement);
			
			for (facilityNode facility: CycicScenarios.workingCycicScenario.FacilityNodes){				
				cycicElement.appendChild(outputFacility(doc, facility));
			}
	}
	
	public static void loadFile(File file){
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
					
			loadSimControl(doc);
			loadCommodities(doc);
			loadFacilities(doc);
				
			Cycic.workingScenario = CycicScenarios.workingCycicScenario;
			NodeList facList = doc.getElementsByTagName("facilityNode");
			
			for (int i = 0; i < facList.getLength(); i++){
				org.w3c.dom.Node facNode = facList.item(i);
				
				facilityNode tempNode = new facilityNode();
				Element element = (Element) facNode;
				tempNode.name = element.getElementsByTagName("name").item(0).getTextContent();
				tempNode.cycicCircle = CycicCircles.addNode((String) tempNode.name, tempNode);
				double radius = Double.parseDouble(element.getElementsByTagName("radius").item(0).getTextContent());
				double xPosition = Double.parseDouble(element.getElementsByTagName("xPosition").item(0).getTextContent());
				tempNode.cycicCircle.setCenterX(xPosition);
				tempNode.cycicCircle.text.setLayoutX(xPosition-radius*0.6);
				tempNode.cycicCircle.menu.setLayoutX(xPosition);
				double yPosition = Double.parseDouble(element.getElementsByTagName("yPosition").item(0).getTextContent());
				tempNode.cycicCircle.setCenterY(yPosition);
				tempNode.cycicCircle.text.setLayoutY(yPosition-radius*0.6);
				tempNode.cycicCircle.menu.setLayoutY(yPosition);
			}
			
			NodeList marketList = doc.getElementsByTagName("marketNode");
			VisFunctions.redrawPane();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	static Element outputFacility(Document doc, facilityNode facility){
		Element facElement = doc.createElement("facilityNode");
		// Name
		Element facName = doc.createElement("name");
		facName.appendChild(doc.createTextNode((String) facility.name));
		facElement.appendChild(facName);
		// X position
		Element xPosition = doc.createElement("xPosition");
		xPosition.appendChild(doc.createTextNode(String.format("%.2f", facility.cycicCircle.getCenterX())));
		facElement.appendChild(xPosition);
		// Y position
		Element yPosition = doc.createElement("yPosition");
		yPosition.appendChild(doc.createTextNode(String.format("%.2f", facility.cycicCircle.getCenterY())));
		facElement.appendChild(yPosition);
		// radius
		Element radius = doc.createElement("radius");
		radius.appendChild(doc.createTextNode(String.format("%.2f", facility.cycicCircle.getRadius())));
		facElement.appendChild(radius);
		
		for (String commodity: facility.cycicCircle.incommods){
			Element commodityObj = doc.createElement("cycicInCommod");
			commodityObj.appendChild(doc.createTextNode(commodity));
			facElement.appendChild(commodityObj);
		}
		for (String commodity: facility.cycicCircle.outcommods){
			Element commodityObj = doc.createElement("cycicOutCommod");
			commodityObj.appendChild(doc.createTextNode(commodity));
			facElement.appendChild(commodityObj);
		}
		
		return facElement;
	}
	
	static String xmlToString(Document doc){
		try {
			  Transformer transformer = TransformerFactory.newInstance().newTransformer();
			  StreamResult result = new StreamResult(new StringWriter());
			  DOMSource source = new DOMSource(doc);
			  transformer.transform(source, result);
			  return result.getWriter().toString();
			} catch(TransformerException ex) {
			  ex.printStackTrace();
			  return null;
			}
	}
	
	static Element outputRegion(Document doc, regionNode region){
		Element regionElement = doc.createElement("regionNode");
		Element regionName = doc.createElement("name");
		regionName.appendChild(doc.createTextNode((String) region.name));
		regionElement.appendChild(regionName);
		// X position
		Element xPosition = doc.createElement("xPosition");
		xPosition.appendChild(doc.createTextNode(String.format("%.2f", region.regionCircle.getX())));
		regionElement.appendChild(xPosition);
		// Y position
		Element yPosition = doc.createElement("yPosition");
		yPosition.appendChild(doc.createTextNode(String.format("%.2f", region.regionCircle.getY())));
		
		regionElement.appendChild(yPosition);
		
		
		return regionElement;
	}

	public static void loadNewFile(File file){
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			loadSimControl(doc);
			loadCommodities(doc);
			loadFacilities(doc);
			
			Cycic.workingScenario = CycicScenarios.workingCycicScenario;
			NodeList facList = doc.getElementsByTagName("facility");
			
			for (int i = 0; i < facList.getLength(); i++){
				org.w3c.dom.Node facNode = facList.item(i);
				
				facilityNode tempNode = new facilityNode();
				Element element = (Element) facNode;
				tempNode.name = element.getElementsByTagName("name").item(0).getTextContent();
				tempNode.cycicCircle = CycicCircles.addNode((String) tempNode.name, tempNode);
			}	
			VisFunctions.redrawPane();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	static public void loadCommodities(Document doc){
		NodeList commodityList = doc.getElementsByTagName("commodity");
		for (int i = 0; i < commodityList.getLength(); i++){
			Node commodity = commodityList.item(i);
			CommodityNode commod = new CommodityNode();
			commod.name = new Label(commodity.getChildNodes().item(0).getTextContent());
			commod.priority =  Double.parseDouble(commodity.getChildNodes().item(0).getTextContent());
			Cycic.workingScenario.CommoditiesList.add(commod);
		}		
	}
	
	static public void loadSimControl(Document doc){
		// Duration
		String duration = doc.getElementsByTagName("duration").item(0).getTextContent();
		Cycic.workingScenario.simulationData.duration = duration;
		
		// Start Month
		String startMonth = doc.getElementsByTagName("startmonth").item(0).getTextContent();
		Cycic.workingScenario.simulationData.startMonth = startMonth;
		
		// Start Year
		String startYear = doc.getElementsByTagName("startyear").item(0).getTextContent();
		Cycic.workingScenario.simulationData.startYear = startYear;
		
	}
	
	static public void loadFacilities(Document doc){
		NodeList facList = doc.getElementsByTagName("facility");
		for (int i = 0; i < facList.getLength(); i++){
			Element facility = (Element) facList.item(i);
			System.out.println(facility);
			Element model = (Element) facility.getChildNodes().item(1).getChildNodes().item(0);
			for (int j = 0; j < model.getChildNodes().getLength(); j++){
				System.out.println(model.getChildNodes().item(j).getTextContent());
			}
		}

	}
	
	public static Boolean inputTest(){
		Boolean errorTest = true;
		String errorLog = "";
		DataArrays scen = CycicScenarios.workingCycicScenario;
		if(scen.FacilityNodes.size() == 0){
			errorLog += "Warning: There are no facilities in your simulation. Please add a facility to your simulation.\n";
			errorTest = false;
		}
		if(scen.regionNodes.size() == 0){
			errorLog += "Warning: There are no regions in your simulation. Please add a region to your simulation.\n";
		}
		if(scen.institNodes.size() == 0){
			errorLog += "Warning: There are no institutions in your simulation. Please add an institution to your simulation.\n";
		}
		if(scen.simulationData.duration.equalsIgnoreCase("0")){
			errorLog += "ERROR: Please add a duration to your cyclus simulation.\n";
			errorTest = false;
		}
		if(errorTest == false){
			log.error(errorLog);
		}
		return errorTest;
	}
	public static String xmlStringGen(){
		if(inputTest()){
			try {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder= docFactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("simulation");
				doc.appendChild(rootElement);

				// General Simulation Information
				Element control = doc.createElement("control");
				rootElement.appendChild(control);
				controlSetup(doc, control);
				
				//Archetypes 
				archetypeSetup(doc, rootElement);

				// Commodities
				for(CommodityNode commod: CycicScenarios.workingCycicScenario.CommoditiesList){
					commodityBuilder(doc, rootElement, commod);
				}			
				// Facilities
				for(facilityNode facility : CycicScenarios.workingCycicScenario.FacilityNodes){
					Element facID = doc.createElement("facility");
					facilityBuilder(doc, facID, facility);
					rootElement.appendChild(facID);
				}
				// Regions
				for(regionNode region : CycicScenarios.workingCycicScenario.regionNodes) {
					Element regionID = doc.createElement("region");
					rootElement.appendChild(regionID);

					regionBuilder(doc, regionID, region.name, region.regionStruct, region.regionData, region.archetype.split(":")[2]);
					// Building the institutions within regions.
					for (instituteNode institution: CycicScenarios.workingCycicScenario.institNodes){
						for (String instit: region.institutions){
							if (institution.name.equalsIgnoreCase(instit)) {
								Element institID = doc.createElement("institution");
								regionID.appendChild(institID);
								Element initFacList = doc.createElement("initialfacilitylist");
								for(facilityItem facility: institution.availFacilities) {
									Element entry = doc.createElement("entry");
									Element initProto = doc.createElement("prototype");
									initProto.appendChild(doc.createTextNode(facility.name));
									entry.appendChild(initProto);
									Element number = doc.createElement("number");
									number.appendChild(doc.createTextNode(facility.number));
									entry.appendChild(number);
									initFacList.appendChild(entry);
								}
								institID.appendChild(initFacList);
								regionBuilder(doc, institID, institution.name, institution.institStruct, institution.institData, institution.archetype.split(":")[2]);
							}
						}
					}
				}

				//Recipes
				for(Nrecipe recipe : CycicScenarios.workingCycicScenario.Recipes){
					recipeBuilder(doc, rootElement, recipe);
				}

				// Writing out the xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				
				return xmlToString(doc);
			} catch (ParserConfigurationException pce){
				pce.printStackTrace();				
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}

