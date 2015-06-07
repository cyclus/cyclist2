package edu.utexas.cycic;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javafx.scene.image.Image;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



public class XMLReader {
	/**
	 * 
	 */
	static skinSet SC2 = new skinSet(){
		{
			name = "SC2";
			images.put("reactor", new Image(new File("skinImages/reactor.png").toURI().toString()));
			images.put("facility", new Image(new File("skinImages/sourceFacSC2.jpg").toURI().toString()));
		}
	};
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	 public static skinSet loadSkin(String path){
		skinSet skin = new skinSet(){
			{
				name = "DSARR";
				radius = 45;
				textPlacement = "bottom";
				images.put("abr", new Image(new File(path + "/skinImages/fuelcycle_abr.png").toURI().toString(), 100, 100, false, false));
				images.put("facility", new Image(new File(path + "/skinImages/fuelcycle_enr.png").toURI().toString()));
				images.put("fuel fabrication", new Image(new File(path + "/skinImages/fuelcycle_fab.png").toURI().toString(), 100, 100, false, false));
				images.put("repository", new Image(new File(path + "/skinImages/fuelcycle_geo.png").toURI().toString()));
				images.put("mine", new Image(new File(path + "/skinImages/fuelcycle_mine.png").toURI().toString()));
				images.put("reactor", new Image(new File(path + "/skinImages/fuelcycle_rxtr.png").toURI().toString(), true));
				images.put("reprocessing", new Image(new File(path + "/skinImages/fuelcycle_sep.png").toURI().toString()));
				images.put("separations", new Image(new File(path + "/skinImages/fuelcycle_sep.png").toURI().toString()));
			}
		};
		return skin;
	}
	
	/**
	 * 
	 */
	static ArrayList<String> blackList = new ArrayList<String>(){
		{
			add("agents:agents:Sink");
			add("agents:agents:Source");
			add("agents:agents:KFacility");
			add("agents:agents:NullInst");
			add("agents:agents:NullRegion");
			add("agents:agents:Prey");
			add("agents:agents:Predator");
			add("stubs:StubFacility:StubFacility");
			add("stubs:StubInst:StubInst");
			add("stubs:StubRegion:StubRegion");
			add("StubFacility/cyclus/StubInst/cyclus/StubRegion:StubRegion:StubRegion");
			add("StubFacility/cyclus/StubInst:StubInst:StubInst");
			add("StubFacility:StubFacility:StubFacility");
			add(":cycaless:BatchReactor");
			add(":cycamore:BatchReactor");
			add(":cycaless:DeplayInst");
			add("commodconverter:CommodConverter:CommodConverter");
		}
	};
	
	/**
	 * 
	 */
	static ArrayList<String> facilityList = new ArrayList<String>(){
		{
			
		}
	};
	
	/**
	 * 
	 */
	static ArrayList<String> regionList = new ArrayList<String>(){
		{
		
		}
	};
	
	/**
	 * 
	 */
	static ArrayList<String> institutionList = new ArrayList<String>(){
		{

		}
	};
	
	
	/**
	 * 
	 * @param xmlSchema
	 * @return
	 */
	static ArrayList<String> readSchema_new(String xmlSchema){
		ArrayList<String> schema = new ArrayList<String>();
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlSchema));
			Document doc = dBuilder.parse(is);
			NodeList top = doc.getChildNodes();
			NodeList node = top.item(0).getChildNodes();
			for(int i = 1; i < node.getLength(); i+=2){
				switch (node.item(i).getNodeName()){
				case "optional":
					Node temp_node = node.item(i).getChildNodes().item(1);
					for(int k = 0; k < temp_node.getAttributes().getLength(); k++){
						if (( temp_node).getAttributes().item(k).getNodeName() == "name"){
							schema.add((temp_node).getAttributes().item(k).getNodeValue());
						} 
					}
					break;
				default:
					for(int j = 0; j < node.item(i).getAttributes().getLength(); j++){
						if (node.item(i).getAttributes().item(j).getNodeName() == "name"){
							schema.add(node.item(i).getAttributes().item(j).getNodeValue());
						} 
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return schema;
	}
	
	/**
	 * 
	 * @param jsonSchema
	 * @return
	 */
	static String entityReader(String jsonSchema){
		Reader schema = new StringReader(jsonSchema);
		JsonReader jsonReader = Json.createReader(schema);
		JsonObject jsonObject = jsonReader.readObject();
		jsonReader.close();
		JsonString string = jsonObject.getJsonString("entity");		
		return string.toString();
	}
	
	/**
	 * 
	 * @param jsonSchema
	 * @return
	 */
	static String nicheReader(String jsonSchema){
		Reader schema = new StringReader(jsonSchema);
		JsonReader jsonReader = Json.createReader(schema);
		JsonObject jsonObject = jsonReader.readObject();
		jsonReader.close();
		JsonString string = jsonObject.getJsonString("niche");		
		return string.toString();
	}

	/**
	 * 
	 * @param dataArray
	 * @return
	 */
	static ArrayList<Object> cycicResize(ArrayList<Object> dataArray){
		while(dataArray.size() < 11){
			if(dataArray.size() == 6){
				dataArray.add(0);
			}
			if(dataArray.size() == 10){
				dataArray.add(true);
			}
			if(dataArray.size() == 3){
				if(dataArray.get(2) == null){
					//dataArray.set(2, dataArray.get(1));
				}
			}
			dataArray.add(null);
		}
		return dataArray;
	}
	
	
	static ArrayList<Object> nodeBuilder(JsonObject anno, ArrayList<Object> facArray, ArrayList<String> vars){
		for(int i = 0; i < vars.size(); i++){
			String var = vars.get(i);	
			if(anno.get(var).getValueType() == ValueType.STRING){
				ArrayList<String> tempArray = new ArrayList<String>();
				tempArray.add(anno.getString(var));
				nodeBuilder(anno, facArray, tempArray);
				break;
			}
			JsonObject anno1 = anno.getJsonObject(var);
			ValueType test = anno1.get("type").getValueType();
			String doc_s = (anno1.getJsonString("doc") != null) ? anno1.getString("doc") : null;
			if(test == ValueType.ARRAY){
				JsonArray type = anno1.getJsonArray("type");
				JsonArray alias = anno1.getJsonArray("alias");
				JsonArray uitype = anno1.getJsonArray("uitype");
				JsonArray units = anno1.getJsonArray("units");
				JsonArray range = anno1.getJsonArray("range");
				if(range == null){
					range = anno1.getJsonArray("categorical");
				}
				JsonArray defType = null;
				if(anno1.get("default") != null){
					if(anno1.get("default").getValueType() == JsonValue.ValueType.OBJECT){
						JsonObject defTypeObj = anno1.getJsonObject("default");
						if(defTypeObj.size() == 0){
							defType = Json.createArrayBuilder()
									.add("false")
									.add("")
									.add("")
									.build();
						} else {
							defTypeObj.keySet().toArray();
							defTypeObj.values().toArray();
							//TODO add method for walking arrays. 
						}
					} else if(anno1.get("default").getValueType() == JsonValue.ValueType.ARRAY){
						JsonArray defTypeArray = anno1.getJsonArray("default");
						if(defTypeArray.size() == 0){
							defType = Json.createArrayBuilder()
									.add("false")
									.add("")
									.build();
						} else {
							//TODO add method for walking arrays
						}
					} else if(anno1.get("default").getValueType() == JsonValue.ValueType.STRING){
					} else if(anno1.get("default").getValueType() == JsonValue.ValueType.NUMBER){
					} 
				}
				JsonArray userLevel = anno1.getJsonArray("userlevel");
				JsonArray tooltip = anno1.getJsonArray("tooltip");
				JsonArray uilabel = anno1.getJsonArray("uilabel");
				ArrayList<Object> newArray = new ArrayList<Object>();
				newArray = annotationBuilder(type, alias, uitype, var,units, range, defType, 
						userLevel, tooltip, uilabel, newArray);
				newArray.set(8, doc_s);
				facArray.add(newArray);
			} else {
                String alias_s, uitype_s, units_s, range_s, defType_s, tooltip_s, uilabel_s, cat_s;
				int userLevel_s;
				JsonString type = anno1.getJsonString("type");
				JsonString alias = anno1.getJsonString("alias");
				JsonString uitype = anno1.getJsonString("uitype");
				units_s = (anno1.getJsonString("units") != null) ? anno1.getString("units") : null;
				range_s = (anno1.getJsonString("range") != null) ? anno1.getString("range") : null;
				cat_s = (anno1.getJsonArray("categorical") != null) ?
						anno1.getJsonArray("categorical").toString().replaceAll("\\[", "").replaceAll("\\]", "") : null;
				range_s = range_s == null ? cat_s : range_s;
				userLevel_s = (anno1.get("userlevel") != null) ? anno1.getInt("userlevel") : 0;
				defType_s = (anno1.get("default") != null) ? anno1.get("default").toString() : null;
				userLevel_s = (anno1.get("userlevel") != null) ? anno1.getInt("userlevel") : 0;
				tooltip_s = (anno1.getJsonString("tooltip") != null) ? anno1.getString("tooltip") : null;
				uilabel_s = (anno1.getJsonString("uilabel") != null) ? anno1.getString("uilabel") : null;
				ArrayList<Object> newArray = new ArrayList<Object>();
				if(alias == null){
					alias_s = var;
				} else {
					alias_s = alias.toString();
				}
				if (uitype == null){
					uitype_s = type.toString();
				} else {
					uitype_s = uitype.toString();
				}
				newArray = stringAnnotationReq(type.toString(), alias_s, uitype_s, newArray);
				newArray = stringAnnotationHelp(userLevel_s, tooltip_s, newArray);
				newArray = stringAnnotationCos(units_s, range_s, defType_s, uilabel_s, newArray);
				newArray.set(8, doc_s);
				facArray.add(newArray);
				
			}
		}
		return facArray;
	}
	
	/**
	 * 
	 * @param type
	 * @param alias
	 * @param uitype
	 * @param facArray
	 * @return
	 */
	static ArrayList<Object> annotationBuilder(JsonArray type, JsonArray alias, JsonArray uitype, String var, JsonArray units,
			JsonArray range, JsonArray defType, JsonArray userLevel, JsonArray tooltip, JsonArray uilabel,
			ArrayList<Object> facArray){
		switch (type.getJsonString(0).toString().replaceAll("\"", "")){
		case "std::map":
			ArrayList<Object> structArray = new ArrayList<Object>();
			for(int i = 1; i < type.size(); i++){
				if(type.get(i) instanceof JsonArray){
					ArrayList<Object> tempArray = new ArrayList<Object>();
					JsonArray unitsArray = (units == null) ? null : units.getJsonArray(i);
					JsonArray rangeArray = (range == null) ? null : range.getJsonArray(i);
					JsonArray defTypeArray = (defType == null) ? null : defType.getJsonArray(i);
					JsonArray userLevelArray = (userLevel == null) ? null : userLevel.getJsonArray(i);
					tempArray = annotationBuilder(type.getJsonArray(i), alias.getJsonArray(i), uitype.getJsonArray(i), var,
							unitsArray, rangeArray, defTypeArray, userLevelArray,
							tooltip.getJsonArray(i), uilabel.getJsonArray(i), tempArray);
					structArray.add(tempArray);
				} else {
					String alias_s, uitype_s, units_s, range_s, defType_s, tooltip_s, uilabel_s;
					int userLevel_s;
					ArrayList<Object> tempArray = new ArrayList<Object>();
					alias_s = aliasTest(alias, i);
					uitype_s = uitypeTest(uitype, type, i);
					units_s = unitsTest(units, i);
					range_s = rangeTest(range, i);
					if(defType instanceof JsonObject){
						defType_s = "";
					} else {
						defType_s = defaultTest(defType);
					}
					userLevel_s = userLevelTest(userLevel, i);
					tooltip_s = tooltipTest(tooltip, i);
					uilabel_s = labelTest(uilabel, i);
					tempArray = stringAnnotationReq(type.getString(i), alias_s, uitype_s, tempArray);
					tempArray = stringAnnotationHelp(userLevel_s, tooltip_s, tempArray);
					tempArray = stringAnnotationCos(units_s, range_s, defType_s, uilabel_s, tempArray);
					structArray.add(tempArray);
					
				}
			}
			ArrayList<Object> itemArray = new ArrayList<Object>();
			if(alias == null){
				facArray.add(var);
				itemArray.add("item");
			} else {
				facArray.add(alias.getJsonArray(0).getString(0));	
				itemArray.add(alias.getJsonArray(0).getString(1));
			}
			itemArray.add(structArray);
			itemArray.add("item");
			cycicResize(itemArray);
			facArray.add(itemArray);
			cycicResize(facArray);
			if(defType != null){
				facArray.set(5, defType.getString(0));
				facArray.set(2, "zeroOrMore");
				facArray.set(6, 1);
				facArray.set(10, false);
			} else {
				facArray.set(2, "oneOrMoreMap");
				if(userLevel == null){
					facArray.set(6, 0);		
				} else {
					facArray.set(6, userLevel.getJsonArray(0).getString(0));		
				}
			} 
			if(tooltip == null){
				facArray.add("");
			} else {
				facArray.set(7, tooltip.getJsonArray(0).getString(0));		
			}
			if(uilabel == null){
				facArray.add(var);		
			} else {
				facArray.set(9, uilabel.getJsonArray(0).getString(0));		
			}
			break;
		case "std::pair":
			ArrayList<Object> structArrayPair = new ArrayList<Object>();
			for(int i = 1; i < type.size(); i++){
				if(type.get(i) instanceof JsonArray){
					ArrayList<Object> tempArray = new ArrayList<Object>();
					JsonArray unitsArray = (units == null) ? null : units.getJsonArray(i);
					JsonArray rangeArray = (range == null) ? null : range.getJsonArray(i);
					JsonArray defTypeArray = (defType == null) ? null : defType.getJsonArray(i);
					JsonArray userLevelArray = (userLevel == null) ? null : userLevel.getJsonArray(i);
					tempArray = annotationBuilder(type.getJsonArray(i), alias.getJsonArray(i), uitype.getJsonArray(i), var,
							unitsArray, rangeArray, defTypeArray, userLevelArray,
							tooltip.getJsonArray(i), uilabel.getJsonArray(i), tempArray);
					structArrayPair.add(tempArray);
				} else {
					ArrayList<Object> tempArray = new ArrayList<Object>();
					String alias_s, uitype_s, units_s, range_s, defType_s, tooltip_s, uilabel_s;
					int userLevel_s;
					alias_s = aliasTest(alias, i);
					uitype_s = uitypeTest(uitype, type, i);
					units_s = unitsTest(units, i);
					range_s = rangeTest(range, i);
					if(defType instanceof JsonObject){
						defType_s = "";
					} else {
						defType_s = defaultTest(defType);
					}
					userLevel_s = userLevelTest(userLevel, i);
					tooltip_s = tooltipTest(tooltip, i);
					uilabel_s = labelTest(uilabel, i);
					tempArray = stringAnnotationReq(type.getString(i), alias_s, uitype_s, tempArray);
					tempArray = stringAnnotationHelp(userLevel_s, tooltip_s, tempArray);
					tempArray = stringAnnotationCos(units_s, range_s, defType_s, uilabel_s, tempArray);
					structArrayPair.add(tempArray);
				}
			}
			if(alias == null){
				facArray.add(var);		
			} else {
				facArray.add(alias.getString(0));		
			}
			facArray.add(structArrayPair);
			facArray.add("pair");
			cycicResize(facArray);
			if(defType != null){
				facArray.set(5, defType.getString(0));
				facArray.set(2, "zeroOrMore");
				facArray.set(6, 1);
				facArray.set(10, false);
			} else {
				facArray.set(2, "oneOrMore");
				if(userLevel == null){
					facArray.set(6, 0);		
				} else {
					facArray.set(6, userLevel.getJsonArray(0).getString(0));		
				}
			} 
			if(tooltip == null){
				facArray.add("");		
			} else {
				facArray.set(7, tooltip.getString(0));		
			}
			if(uilabel == null){
				facArray.add(var);	
			} else {
				facArray.set(9, uilabel.getString(0));		
			}
			break;
		case "std::vector":
			ArrayList<Object> structArrayVector = new ArrayList<Object>();
			if(type.get(1) instanceof JsonArray){
				ArrayList<Object> tempArray = new ArrayList<Object>();
				JsonArray unitsArray = (units == null) ? null : units.getJsonArray(1);
				JsonArray rangeArray = (range == null) ? null : range.getJsonArray(1);
				JsonArray defTypeArray = (defType == null) ? null : defType.getJsonArray(1);
				JsonArray userLevelArray = (userLevel == null) ? null : userLevel.getJsonArray(1);
				tempArray = annotationBuilder(type.getJsonArray(1), alias.getJsonArray(1), uitype.getJsonArray(1), var,
						unitsArray, rangeArray, defTypeArray, userLevelArray, tooltip.getJsonArray(1), 
						uilabel.getJsonArray(1), tempArray);
				structArrayVector.add(tempArray);
			} else {
				String alias_s, uitype_s, units_s, range_s, defType_s, tooltip_s, uilabel_s;
				int userLevel_s;
				ArrayList<Object> tempArray = new ArrayList<Object>();
				alias_s = aliasTest(alias);
				uitype_s = uitypeTest(uitype, type);
				units_s = unitsTest(units);
				range_s = rangeTest(range);
				if(defType instanceof JsonArray){
					defType_s = "";
				} else {
					defType_s = defaultTest(defType);
				}
				userLevel_s = userLevelTest(userLevel);
				tooltip_s = tooltipTest(tooltip);
				uilabel_s = labelTest(uilabel);
				tempArray = stringAnnotationReq(type.getString(1), alias_s, uitype_s, tempArray);
				tempArray = stringAnnotationHelp(userLevel_s, tooltip_s, tempArray);
				tempArray = stringAnnotationCos(units_s, range_s, defType_s, uilabel_s, tempArray);
				structArrayVector.add(tempArray);
			}
			if(alias == null){
				facArray.add(var);
			} else {
				facArray.add(alias.getString(0));
			}
			facArray.add(structArrayVector);
			facArray.add("oneOrMore");
			cycicResize(facArray);
			cycicResize(facArray);
			if(defType != null){
				facArray.set(5, defType.getString(0));
				facArray.set(2, "zeroOrMore");
				facArray.set(6, 1);
				facArray.set(10, false);
			} else {
				facArray.set(2, "oneOrMore");
				if(userLevel == null){
					facArray.set(6, 0);		
				} else {
					facArray.set(6, userLevel.getJsonArray(0).getString(0));		
				}
			} 
			if(tooltip == null){
				facArray.add("");		
			} else {
				facArray.set(7, tooltip.getString(0));		
			}
			if(uilabel == null){
				facArray.add(var);	
			} else {
				facArray.set(9, uilabel.getString(0));		
			}
			break;
		case "std::string":
		case "int":
		case "double":
			ArrayList<Object> tempArray = new ArrayList<Object>();
			String alias_s, uitype_s, units_s, range_s, defType_s, tooltip_s, uilabel_s;
			int userLevel_s;
			alias_s = aliasTest(alias);
			uitype_s = uitypeTest(uitype, type);
			units_s = unitsTest(units);
			range_s = rangeTest(range);
			defType_s = defaultTest(defType);
			userLevel_s = userLevelTest(userLevel);
			tooltip_s = tooltipTest(tooltip);
			uilabel_s = labelTest(uilabel);
			tempArray = stringAnnotationReq(type.getString(1), alias_s, uitype_s, tempArray);
			tempArray = stringAnnotationHelp(userLevel_s, tooltip_s, tempArray);
			tempArray = stringAnnotationCos(units_s, range_s, defType_s, uilabel_s, tempArray);
			break;
		default : 
			System.out.println("Default");
			System.out.println("HEY RADIO YOU FORGOT " + type.getJsonString(0).toString());
			break;
		}
		return facArray;		
	}
	
	static ArrayList<Object> stringAnnotationReq(String type, String alias, String uitype, ArrayList<Object> facArray){
		cycicResize(facArray);
		facArray.set(0, alias);
		facArray.set(1, type);
		if(uitype == null){
			facArray.set(2, type);
		} else {
			facArray.set(2, uitype.toString().replaceAll("\"", ""));
		}
		return facArray;
	}
	
	static ArrayList<Object> stringAnnotationCos(String units, String range, String defType, String uilabel, ArrayList<Object> facArray){
		facArray.set(3, units);
		facArray.set(4, range);
		facArray.set(5, defType);
		if(defType != null){
			facArray.set(6, 1);
			facArray.set(10, false);
		}
		facArray.set(9, uilabel);
		return facArray;
	}
	
	static ArrayList<Object> stringAnnotationHelp(int userLevel, String tooltip, ArrayList<Object> facArray){
		facArray.set(6, userLevel);
		facArray.set(7, tooltip);
		return facArray;
	}
	
	static String aliasTest(JsonArray alias){
		String alias_string;
		if(alias == null){
			alias_string = "key";
		} else {
			alias_string = alias.getString(1);
		}
		return alias_string;
	}
	
	static String aliasTest(JsonArray alias, int i){
		String alias_string;
		if(alias == null){
			alias_string = "key";
		} else {
			alias_string = alias.getString(i);
		}
		return alias_string;
	}
	
	static String uitypeTest(JsonArray uitype, JsonArray type){
		String uitype_s;
		if (uitype == null){
			uitype_s = type.getString(1);
		} else {
			uitype_s = uitype.getString(1);
		}
		return uitype_s;
	}
	
	static String uitypeTest(JsonArray uitype, JsonArray type, int i){
		String uitype_s;
		if (uitype == null){
			uitype_s = type.getString(i);
		} else {
			uitype_s = uitype.getString(i);
		}
		return uitype_s;
	}
	
	static String defaultTest(JsonArray defType){
		String defType_s; 
		if(defType == null){
			defType_s = null;
		} else {
			defType_s = defType.getString(1);
		}
		return defType_s;
	}
	
	static String defaultTest(JsonArray defType, int i){
		String defType_s; 
		if(defType == null){
			defType_s = null;
		} else {
			defType_s = defType.getString(i);
		}
		return defType_s;
	}
	
	static String tooltipTest(JsonArray tooltip){
		String tooltip_s; 
		if(tooltip == null){
			tooltip_s = null;
		} else {
			tooltip_s = tooltip.getString(1);
		}
		return tooltip_s;
	}
	
	static String tooltipTest(JsonArray tooltip, int i){
		String tooltip_s; 
		if(tooltip == null){
			tooltip_s = null;
		} else {
			tooltip_s = tooltip.getString(i);
		}
		return tooltip_s;
	}
	
	static String helpTest(JsonArray help){
		String help_s; 
		if(help == null){
			help_s = null;
		} else {
			help_s = help.getString(1);
		}
		return help_s;
	}
	
	static String helpTest(JsonArray help, int i){
		String help_s; 
		if(help == null){
			help_s = null;
		} else {
			help_s = help.getString(i);
		}
		return help_s;
	}
	
	static int userLevelTest(JsonArray userLevel){
		int userLevel_s; 
		if(userLevel == null){
			userLevel_s = 0;
		} else {
			userLevel_s = userLevel.getInt(1);
		}
		return userLevel_s;
	}
	
	static int userLevelTest(JsonArray userLevel, int i){
		int userLevel_s; 
		if(userLevel == null){
			userLevel_s = 0;
		} else {
			userLevel_s = userLevel.getInt(i);
		}
		return userLevel_s;
	}
	
	static String unitsTest(JsonArray units){
		String units_s; 
		if(units == null){
			units_s = null;
		} else {
			units_s = units.getString(1);
		}
		return units_s;
	}
	
	static String unitsTest(JsonArray units, int i){
		String units_s; 
		if(units == null){
			units_s = null;
		} else {
			units_s = units.getString(i);
		}
		return units_s;
	}
	
	static String labelTest(JsonArray uilabel){
		String label_s; 
		if(uilabel == null){
			label_s = null;
		} else {
			label_s = uilabel.getString(1);
		}
		return label_s;
	}
	
	static String labelTest(JsonArray uilabel, int i){
		String label_s; 
		if(uilabel == null){
			label_s = null;
		} else {
			label_s = uilabel.getString(i);
		}
		return label_s;
	}
	
	static String rangeTest(JsonArray range){
		String range_s; 
		if(range == null){
			range_s = null;
		} else {
			range_s = range.getString(1);
		}
		return range_s;
	}
	
	static String rangeTest(JsonArray range, int i){
		String range_s; 
		if(range == null){
			range_s = null;
		} else {
			range_s = range.getString(i);
		}
		return range_s;
	}
	
	static String categoricalTest(JsonArray categorical){
		String categorical_s; 
		if(categorical == null){
			categorical_s = null;
		} else {
			categorical_s = categorical.getString(1);
		}
		return categorical_s;
	}
	
	static String categoricalTest(JsonArray categorical, int i){
		String categorical_s; 
		if(categorical == null){
			categorical_s = null;
		} else {
			categorical_s = categorical.getString(i);
		}
		return categorical_s;
	}
	
	static JsonArray defaultBuilder(JsonArray defArray){
		
		return defArray;
	}
}
