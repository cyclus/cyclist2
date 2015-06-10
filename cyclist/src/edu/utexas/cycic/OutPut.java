package edu.utexas.cycic;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
		
			//Regions
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
                            if (institution.availFacilities.size() > 0) {
                                Element initFacList = doc.createElement("initialfacilitylist");
                                for (Map.Entry<String, Integer> facility: institution.availFacilities.entrySet()) {
                                    Element entry = doc.createElement("entry");
                                    Element initProto = doc.createElement("prototype");
                                    initProto.appendChild(doc.createTextNode(facility.getKey()));
                                    entry.appendChild(initProto);
                                    Element number = doc.createElement("number");
                                    number.appendChild(doc.createTextNode(Integer.toString(facility.getValue())));
                                    entry.appendChild(number);
                                    initFacList.appendChild(entry);
                                }
                                institID.appendChild(initFacList);
                            }
							regionBuilder(doc, institID, institution.name, institution.institStruct, institution.institData, institution.archetype.split(":")[2]);
						}
					}
				}
			}
			
			for(instituteNode inst : CycicScenarios.workingCycicScenario.institNodes){
				if(inst.name.equalsIgnoreCase("__inst__")){
					CycicScenarios.workingCycicScenario.institNodes.remove(inst);
					break;
				}
			}
                        
			for(regionNode region : CycicScenarios.workingCycicScenario.regionNodes){
				if(region.name.equalsIgnoreCase("__region__")){
					CycicScenarios.workingCycicScenario.regionNodes.remove(region);
					break;
				}
			}
			
			//Recipes
			for(Nrecipe recipe : CycicScenarios.workingCycicScenario.Recipes){
				recipeBuilder(doc, rootElement, recipe);
			}
			
			Element ui = doc.createElement("ui");
			saveFile(doc, ui);
			rootElement.appendChild(ui);

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
		int month = Integer.parseInt(CycicScenarios.workingCycicScenario.simulationData.startMonth);
		simStartMon.appendChild(doc.createTextNode(String.valueOf(month)));
		control.appendChild(simStartMon);
			
		Element simStartYear = doc.createElement("startyear");
		simStartYear.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.startYear));
		control.appendChild(simStartYear);
		
		if(!CycicScenarios.workingCycicScenario.simulationData.decay.equalsIgnoreCase("never")){
			Element decay = doc.createElement("decay");
			decay.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.decay));
			control.appendChild(decay);
		}
		
		if(CycicScenarios.workingCycicScenario.simulationData.simHandle == null){
			
		} else if(!CycicScenarios.workingCycicScenario.simulationData.simHandle.equalsIgnoreCase("")){
			Element simHandle = doc.createElement("simhandle");
			simHandle.appendChild(doc.createTextNode(CycicScenarios.workingCycicScenario.simulationData.simHandle));
			control.appendChild(simHandle);
		}
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
			if (recipe.Basis.equalsIgnoreCase("mass")){
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

		if (!facility.facLifetime.equals("")) {
			Element lifetime = doc.createElement("lifetime");
			lifetime.setTextContent(facility.facLifetime);
			rootElement.appendChild(lifetime);
		}
		
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
		if(string.equalsIgnoreCase("oneOrMore") || string.equalsIgnoreCase("zeroOrMore")||string.equalsIgnoreCase("oneOrMoreMap") || string.equalsIgnoreCase("pair") || string.equalsIgnoreCase("item")){
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
			String institString = "";
			String regionString = "";
			JSONObject ui = new JSONObject();
			JSONObject facs = new JSONObject();
			for (facilityNode facility: CycicScenarios.workingCycicScenario.FacilityNodes){				
				facs.put(facility.name.toString(), outputFacility(facility));
			}
			ui.put("facilities", facs);
			
			JSONObject inst = new JSONObject();
			for (instituteNode instit: CycicScenarios.workingCycicScenario.institNodes){				
				inst.put(instit.name.toString(), outputInstitution(instit));
			}
			ui.put("institutions", inst);
			
			JSONObject reg = new JSONObject();
			for (regionNode region: CycicScenarios.workingCycicScenario.regionNodes){				
				reg.put(region.name.toString(), outputRegion(region));
			}
			ui.put("regions", reg);
			
			ui.put("description", CycicScenarios.workingCycicScenario.simulationData.description);
			ui.put("decay", CycicScenarios.workingCycicScenario.simulationData.decay);
			ui.put("simHandle", CycicScenarios.workingCycicScenario.simulationData.simHandle);
			
			rootElement.setTextContent(ui.toJSONString());
	}
	
	public static void loadFile(File file){
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			
			Cycic.workingScenario = CycicScenarios.workingCycicScenario;
			
			loadSimControl(doc);
			loadCommodities(doc);
			loadRecipes(doc);
			Cycic.buildCommodPane();
			NodeList uiItem = doc.getElementsByTagName("ui");
			String uiString = uiItem.item(0).getTextContent().replaceAll("\\\\\"", "\"");
			JSONObject json = (JSONObject) JSONValue.parse(uiString);
			Map facs = (Map) json.get("facilities");
			loadFacilities(facs);	
			
			Map inst = (Map) json.get("institutions");
			loadInst(inst);	
			
			Map region = (Map) json.get("regions");
			loadRegion(region);	
			
			String description = (String) json.get("description");
			CycicScenarios.workingCycicScenario.simulationData.description = description;
			Cycic.description.setText(description);	
			
			String decay = (String) json.get("decay");
			CycicScenarios.workingCycicScenario.simulationData.description = decay;
			Cycic.decay.setValue(decay);	

			String simHandle = (String) json.get("simHandle");
			CycicScenarios.workingCycicScenario.simulationData.description = simHandle;
			Cycic.simHandle.setText(simHandle);	

			VisFunctions.redrawPane();
			VisFunctions.redrawInstitPane();
			VisFunctions.redrawRegionPane();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	static JSONObject outputFacility(facilityNode facility){
		String facArray = alObjtoString(facility.facilityStructure);
		String dataArray = alObjtoString(facility.facilityData);
		String incommods = alStrtoString(facility.cycicCircle.incommods);
		String outcommods = alStrtoString(facility.cycicCircle.outcommods);
	
		JSONObject facObject = new JSONObject();
		
		facObject.put("name", facility.name.toString());
		facObject.put("X", String.format("%.2f", facility.cycicCircle.getCenterX()));
		facObject.put("Y", String.format("%.2f", facility.cycicCircle.getCenterY()));
		facObject.put("niche", facility.niche);
		facObject.put("type", facility.facilityType);
		facObject.put("archetype", facility.archetype);
		facObject.put("facArray", facArray);
		facObject.put("dataArray", dataArray);
		facObject.put("incommods", incommods);
		facObject.put("outcommods", outcommods);
		
		return facObject;
	}
	
	static JSONObject outputInstitution(instituteNode institute){
		String facArray = alObjtoString(institute.institStruct);
		String dataArray = alObjtoString(institute.institData);
		String facMap = MaptoString(institute.availFacilities);

		JSONObject institObject = new JSONObject();
		institObject.put("name", institute.name);
		institObject.put("X", String.format("%.2f", institute.institutionShape.getCenterX()));
		institObject.put("Y", String.format("%.2f", institute.institutionShape.getCenterY()));
		institObject.put("type", institute.type);
		institObject.put("archetype", institute.archetype);
		institObject.put("facArray", facArray);
		institObject.put("dataArray", dataArray);
		institObject.put("availableFacs", facMap);
		
		return institObject;
	}
	
	static JSONObject outputRegion(regionNode region){
		String facArray = alObjtoString(region.regionStruct);
		String dataArray = alObjtoString(region.regionData);
		String institutions = alStrtoString(region.institutions);
	
		JSONObject regionObject = new JSONObject();
		regionObject.put("name", region.name);
		regionObject.put("X", String.format("%.2f", region.regionShape.getX()));
		regionObject.put("Y", String.format("%.2f", region.regionShape.getY()));
		regionObject.put("type", region.type);
		regionObject.put("archetype", region.archetype);
		regionObject.put("facArray", facArray);
		regionObject.put("dataArray", dataArray);
		regionObject.put("institutions", institutions);

		return regionObject;
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
		xPosition.appendChild(doc.createTextNode(String.format("%.2f", region.regionShape.getX())));
		regionElement.appendChild(xPosition);
		// Y position
		Element yPosition = doc.createElement("yPosition");
		yPosition.appendChild(doc.createTextNode(String.format("%.2f", region.regionShape.getY())));
		
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
			//loadFacilities(doc);
			
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
			commod.name = new Label(commodity.getChildNodes().item(1).getTextContent());
			commod.priority =  Double.parseDouble(commodity.getChildNodes().item(3).getTextContent());
			Cycic.workingScenario.CommoditiesList.add(commod);
		}		
	}
	
	static public void loadSimControl(Document doc){
		// Duration
		String duration = doc.getElementsByTagName("duration").item(0).getTextContent();
		Cycic.workingScenario.simulationData.duration = duration;
		Cycic.duration.setText(duration);
		
		// Start Month
		String startMonth = doc.getElementsByTagName("startmonth").item(0).getTextContent();
		Cycic.workingScenario.simulationData.startMonth = startMonth;
		Cycic.startMonth.setValue(Cycic.monthList.get(Integer.parseInt(Cycic.workingScenario.simulationData.startMonth)-1));
		
		// Start Year
		String startYear = doc.getElementsByTagName("startyear").item(0).getTextContent();
		Cycic.workingScenario.simulationData.startYear = startYear;
		Cycic.startYear.setText(startYear);
		
	}

        
	static public void loadRecipes(Document doc){
		NodeList recipeList = doc.getElementsByTagName("recipe");
		for (int i = 0; i < recipeList.getLength(); i++){
			Node tempRecipe = recipeList.item(i);
			Nrecipe recipe = new Nrecipe();
			recipe.Name = tempRecipe.getChildNodes().item(1).getTextContent();
			recipe.Basis =  tempRecipe.getChildNodes().item(3).getTextContent();
			for(int j = 5; j < tempRecipe.getChildNodes().getLength(); j+=2){
				Node comp = tempRecipe.getChildNodes().item(j);
				isotopeData composition = new isotopeData();
				composition.Name = comp.getChildNodes().item(1).getTextContent();
				if(recipe.Basis.equalsIgnoreCase("mass")){
					composition.mass = Double.parseDouble(comp.getChildNodes().item(3).getTextContent());
				} else {
					composition.atom = Double.parseDouble(comp.getChildNodes().item(3).getTextContent());
				}
				recipe.Composition.add(composition);
			}
			Cycic.workingScenario.Recipes.add(recipe);
		}		
	}
	
	static public void loadFacilities(Map object){
		Object[] keys = object.keySet().toArray();
		for(int i = 0; i < keys.length; i++){
			Map fac = (Map) object.get(keys[i]);
			
			facilityNode facNode = new facilityNode();
			facNode.name = fac.get("name");
			facNode.archetype = (String) fac.get("archetype");
			facNode.facilityType = (String) fac.get("type");
			facNode.cycicCircle = CycicCircles.addNode((String) facNode.name, facNode);
			facNode.niche = (String) fac.get("niche");
			facNode.cycicCircle.niche = (String) fac.get("niche");
			facNode.cycicCircle.setCenterX(Double.parseDouble((String) fac.get("X")));
			facNode.cycicCircle.setCenterY(Double.parseDouble((String) fac.get("Y")));
			facNode.cycicCircle.image.setLayoutX(facNode.cycicCircle.getCenterX()-60);
			facNode.cycicCircle.image.setLayoutY(facNode.cycicCircle.getCenterY()-60);
			VisFunctions.placeTextOnCircle(facNode.cycicCircle, "bottom");
			
			String facByte = (String) fac.get("facArray");
			facNode.facilityStructure = StringtoALObj(facByte);

			
			String dataByte = (String) fac.get("dataArray");
			facNode.facilityData = StringtoALObj(dataByte);
			
			String incommods = (String) fac.get("incommods");
			facNode.cycicCircle.incommods = StringtoALStr(incommods);
			
			String outcommods = (String) fac.get("outcommods");
			facNode.cycicCircle.outcommods = StringtoALStr(outcommods);
		}	
	}
	
	static public void loadInst(Map object){
		Object[] keys = object.keySet().toArray();
		for(int i = 0; i < keys.length; i++){
			Map instit = (Map) object.get(keys[i]);
			
			instituteNode institNode = new instituteNode();
			institNode.name = (String) instit.get("name");
			institNode.archetype = (String) instit.get("archetype");
			institNode.type = (String) instit.get("type");
			institNode.institutionShape = InstitutionShape.addInst((String) institNode.name, institNode);
			institNode.institutionShape.setCenterX(Double.parseDouble((String) instit.get("X")));
			institNode.institutionShape.setCenterY(Double.parseDouble((String) instit.get("Y")));
			VisFunctions.placeTextOnEllipse(institNode.institutionShape, "middle");
			
			String facByte = (String) instit.get("facArray");
			institNode.institStruct = StringtoALObj(facByte);

			
			String dataByte = (String) instit.get("dataArray");
			institNode.institData = StringtoALObj(dataByte);
			
			String facList = (String) instit.get("availableFacs");
			institNode.availFacilities = StringtoMap(facList);
			
			CycicScenarios.workingCycicScenario.institNodes.add(institNode);
		}	
	}
	
	static public void loadRegion(Map object){
		Object[] keys = object.keySet().toArray();
		for(int i = 0; i < keys.length; i++){
			Map region = (Map) object.get(keys[i]);
			
			regionNode regionNode = new regionNode();
			regionNode.name = (String) region.get("name");
			regionNode.archetype = (String) region.get("archetype");
			regionNode.type = (String) region.get("type");
			regionNode.regionShape = RegionShape.addRegion((String) regionNode.name, regionNode);
			regionNode.regionShape.setX(Double.parseDouble((String) region.get("X")));
			regionNode.regionShape.setY(Double.parseDouble((String) region.get("Y")));
			VisFunctions.placeTextOnRectangle(regionNode.regionShape, "middle");
						
			String facByte = (String) region.get("facArray");
			regionNode.regionStruct = StringtoALObj(facByte);
			
			String dataByte = (String) region.get("dataArray");
			regionNode.regionData = StringtoALObj(dataByte);
			
			String institByte = (String) region.get("institutions");
			regionNode.institutions = StringtoALStr(institByte);
			
			CycicScenarios.workingCycicScenario.regionNodes.add(regionNode);
		}	
	}
	
	public static Boolean inputTest(){
		Boolean errorTest = true;
		DataArrays scen = CycicScenarios.workingCycicScenario;
		if(scen.FacilityNodes.size() == 0){
			log.error("There are no facilities in your simulation. Please add a facility to your simulation.");
			errorTest = false;
		}
		if(scen.simulationData.duration.equalsIgnoreCase("0")){
			log.error("Please add a duration to your cyclus simulation.");
			errorTest = false;
		}
		if(scen.regionNodes.size() == 0 && scen.institNodes.size() == 0){
			log.warn("Warning: No institutions or regions found.");
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
                                if (institution.availFacilities.size() > 0) {
                                    Element initFacList = doc.createElement("initialfacilitylist");
                                    for (Map.Entry<String, Integer> facility: institution.availFacilities.entrySet()) {
                                        Element entry = doc.createElement("entry");
                                        Element initProto = doc.createElement("prototype");
                                        initProto.appendChild(doc.createTextNode(facility.getKey()));
                                        entry.appendChild(initProto);
                                        Element number = doc.createElement("number");
                                        number.appendChild(doc.createTextNode(Integer.toString(facility.getValue())));
                                        entry.appendChild(number);
                                        initFacList.appendChild(entry);
                                    }
                                    institID.appendChild(initFacList);
                                }
								regionBuilder(doc, institID, institution.name, institution.institStruct, institution.institData, institution.archetype.split(":")[2]);
							}
						}
					}
				}

				//Recipes
				for(Nrecipe recipe : CycicScenarios.workingCycicScenario.Recipes){
					recipeBuilder(doc, rootElement, recipe);
				}
				
				for(instituteNode inst : CycicScenarios.workingCycicScenario.institNodes){
					if(inst.name.equalsIgnoreCase("__inst__")){
						CycicScenarios.workingCycicScenario.institNodes.remove(inst);
						break;
					}
				}
	                        
				for(regionNode region : CycicScenarios.workingCycicScenario.regionNodes){
					if(region.name.equalsIgnoreCase("__region__")){
						CycicScenarios.workingCycicScenario.regionNodes.remove(region);
						break;
					}
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

  public static void CheckInjection() {    
    if(CycicScenarios.workingCycicScenario.regionNodes.size() == 0
       && CycicScenarios.workingCycicScenario.institNodes.size() == 0) {
      Dialog dg = new Dialog();
      dg.setTitle("Input Injection");
      dg.setHeaderText("Warning: No Region or Institution found");
      dg.setContentText("Would you like to add defaults?");
      dg.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
      Optional<ButtonType> result = dg.showAndWait();
      if(result.get() == ButtonType.YES){
        OutPut.addNullRegion();
        OutPut.addNullInst();
      }
    }
  }
  
  public static void addNullRegion(){
    regionNode region = new regionNode();
    region.name = "__region__";
    region.archetype = ":agents:NullRegion";
    region.type = "agents NullRegion";
    for(instituteNode inst: CycicScenarios.workingCycicScenario.institNodes){
      region.institutions.add(inst.name);
    }
    CycicScenarios.workingCycicScenario.regionNodes.add(region);
  }
    
  public static void addNullInst(){
    instituteNode inst = new instituteNode();
    inst.name = "__inst__";
    inst.archetype = ":agents:NullInst";
    inst.type = "agents NullInst";
    for(facilityNode fac: CycicScenarios.workingCycicScenario.FacilityNodes){        
      inst.availFacilities.put((String) fac.name, 1);
    }
    CycicScenarios.workingCycicScenario.institNodes.add(inst);
    CycicScenarios.workingCycicScenario.regionNodes.get(0).institutions.add(inst.name);
  }
	static String alObjtoString(ArrayList<Object> array){
		String returnString = null;
		try {
			ByteArrayOutputStream boa = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(boa);
			oos.writeObject(array);
			oos.close();
			returnString = Base64.getEncoder().encodeToString(boa.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnString;
	}
	
	static String alStrtoString(ArrayList<String> array){
		String returnString = null;
		try {
			ByteArrayOutputStream boa = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(boa);
			oos.writeObject(array);
			oos.close();
			returnString = Base64.getEncoder().encodeToString(boa.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnString;
	}
	
	
	static String MaptoString(Map map){
		String returnString = null;
		try {
			ByteArrayOutputStream boa = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(boa);
			oos.writeObject(map);
			oos.close();
			returnString = Base64.getEncoder().encodeToString(boa.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnString;
	}
	
	static ArrayList<Object> StringtoALObj(String string){
		ArrayList<Object> array = new ArrayList<Object>();
		byte[] facTempArray = Base64.getDecoder().decode(string);
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(facTempArray));
			array = (ArrayList<Object>) ois.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}
	
	static ArrayList<String> StringtoALStr(String string){
		ArrayList<String> array = new ArrayList<String>();
		byte[] facTempArray = Base64.getDecoder().decode(string);
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(facTempArray));
			array = (ArrayList<String>) ois.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array;
	}
	
	static Map<String, Integer> StringtoMap(String string){
		Map map = null;
		byte[] facTempArray = Base64.getDecoder().decode(string);
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(facTempArray));
			map = (Map) ois.readObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
}

