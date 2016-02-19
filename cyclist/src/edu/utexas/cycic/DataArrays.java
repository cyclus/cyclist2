package edu.utexas.cycic;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;
import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.Resources1;


/**
 * This class contains all of the data structures used for CYCIC.
 * @author Robert
 *
 */
public class DataArrays{
    static Logger log = Logger.getLogger(DataArrays.class);

	
	static ArrayList<facilityNode> FacilityNodes = new ArrayList<facilityNode>();
	static ArrayList<Label> FacilityTypes = new ArrayList<Label>();	
	
	static ArrayList<nodeLink> Links = new ArrayList<nodeLink>();
	
	static ArrayList<CommodityNode> CommoditiesList = new ArrayList<CommodityNode>();
	
	static ArrayList<Nrecipe> Recipes = new ArrayList<Nrecipe>();
	static ArrayList<Label> RecipesList = new ArrayList<Label>();
	
	static ArrayList<regionNode> regionNodes = new ArrayList<regionNode>();
	static ArrayList<instituteNode> institNodes = new ArrayList<instituteNode>();
	
	static ArrayList<skinSet> visualizationSkins = new ArrayList<skinSet>();

	static simInfo simulationData = new simInfo();
	static ArrayList<facilityStructure> simFacilities = new ArrayList<facilityStructure>();
	static ArrayList<regionStructure> simRegions = new ArrayList<regionStructure>();
	static ArrayList<institutionStructure> simInstitutions = new ArrayList<institutionStructure>();	


    public static void cycicInitLoader(String entity) {
        Resources1 resource = new Resources1();
        File file = new File(resource.getCurrentPath());
        String path = "/" + file.getParent();
        try {
            defaultJsonReader(entity);
            log.info("Meta data loaded for default archetypes.\n Use DISCOVER ARCHETYPES button to load more.");
        } catch (IOException e1) {
            log.warn("Could not read default meta data.\n Use DISCOVER ARCHETYPES button to load more.");
        }
        visualizationSkins.add(XMLReader.loadSkin(path));
    }
    
	/**
	 * 
	 */
    public static void retrieveSchema(String rawMetadata, String entity) {
        // rawMetadata is a JSON string.
    	log.info("Attempting metadata retreival for entity type: "+ entity);
        Reader metaReader = new StringReader(rawMetadata);
        JsonReader metaJsonReader = Json.createReader(metaReader);
        JsonObject metadata = metaJsonReader.readObject();
        metaJsonReader.close();
        JsonObject schemas = metadata.getJsonObject("schema");
        JsonObject annotations = metadata.getJsonObject("annotations");
        switch (entity.toLowerCase()){
        case "facility":
        	DataArrays.simFacilities.clear();
        	break;
        case "institution":
            DataArrays.simInstitutions.clear();
            break;
        case "region":
        	DataArrays.simRegions.clear();
        	break;
        } 
        for(javax.json.JsonString specVal : metadata.getJsonArray("specs").getValuesAs(JsonString.class)){
        	String spec = specVal.getString();
            boolean test = true;
            for(int j = 0; j < XMLReader.blackList.size(); j++){
                if(spec.equalsIgnoreCase(XMLReader.blackList.get(j))){
                    test = false;
                }
            }
            if(test == false){
                continue;
            }
            
            
            String schema = schemas.getString(spec);
            String pattern1 = "<!--.*?-->";
            Pattern p = Pattern.compile(pattern1, Pattern.DOTALL);
            schema = p.matcher(schema).replaceAll("");
            if(schema.length() > 12){
            	if(!schema.substring(0, 12).equals("<interleave>")){
                	schema = "<interleave>" + schema + "</interleave>"; 
                }
            }
            JsonObject anno = annotations.getJsonObject(spec);
            switch(anno.getString("entity")){
            case "facility":
            	if(!entity.equalsIgnoreCase("facility")){
            		continue;
            	}
                log.info("Adding archetype "+spec);
                facilityStructure node = new facilityStructure();
                node.facAnnotations = anno.toString();
                node.facilityArch = spec;
                node.niche = anno.getString("niche", "facility");
                node.doc = anno.getString("doc", "facility");
                JsonObject facVars = anno.getJsonObject("vars");
                ArrayList<Object> facArray = new ArrayList<Object>();
                node.facStruct = XMLReader.nodeBuilder(facVars, facArray, XMLReader.readSchema_new(schema));
                node.facilityName = spec.replace(":", " ");
                DataArrays.simFacilities.add(node);
                break;
            case "region":
            	if(!entity.equalsIgnoreCase("region")){
            		continue;
            	}
                log.info("Adding archetype "+spec);
                regionStructure rNode = new regionStructure();
                rNode.regionAnnotations = anno.toString();
                rNode.regionArch = spec;
                rNode.doc = anno.getString("doc","region");
                JsonObject regionVars = anno.getJsonObject("vars");
                ArrayList<Object> regionArray = new ArrayList<Object>();
                rNode.regionStruct = XMLReader.nodeBuilder(regionVars,regionArray, XMLReader.readSchema_new(schema));
                rNode.regionName = spec.replace(":", " ");
                DataArrays.simRegions.add(rNode);
                break;
            case "institution":
            	if(!entity.equalsIgnoreCase("institution")){
            		continue;
            	}
                log.info("Adding archetype "+spec);
                institutionStructure iNode = new institutionStructure();
                iNode.institArch = spec;
                iNode.institAnnotations = anno.toString();
                iNode.doc = anno.getString("doc","institution");
                JsonObject instVars = anno.getJsonObject("vars");
                ArrayList<Object> instArray = new ArrayList<Object>();
                iNode.institStruct = XMLReader.nodeBuilder(instVars, instArray, XMLReader.readSchema_new(schema));
                iNode.institName = spec.replace(":", " ");
                DataArrays.simInstitutions.add(iNode);
                break;
            default:
                log.error(spec+" is not of the 'facility', 'region' or 'institution' type. "
                    + "Please check the entity value in the archetype annotation.");
            break;
            };  
        }
        log.info("Schema discovery complete");
	}



    private static void defaultJsonReader(String entity) throws IOException{
    	InputStream in = Cyclist.class.getResourceAsStream("assets/default-metadata.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");
        
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }
        reader.close();
        retrieveSchema(stringBuilder.toString(), entity);
    }




}

class CommodityNode {
	Label name = new Label();
	Double priority;
}
/**
 * 
 * @author Robert
 *
 */
class facilityStructure {
	String facilityName;
	String facilityArch;
	String niche;
	String doc;
	ArrayList<Object> facStruct = new ArrayList<Object>();
	String facSchema;
	String facAnnotations;
	protected boolean loaded = false;
	
	String getName(){
		return this.facilityName;
	}
	
	ArrayList<Object> getStruct(){
		return this.facStruct;
	}
	
	void setName(String name){
		this.facilityName = name;
	}
	
	void setStruct(ArrayList<Object> struct){
		this.facStruct = struct;
	}
}

/**
 * 
 * @author Robert
 *
 */
class regionStructure {
	String regionName;
	String doc;
	String regionArch;
	String regionSchema;
	String regionAnnotations;
	ArrayList<Object> regionStruct = new ArrayList<Object>();
	
	String getName(){
		return this.regionName;
	}
	
	ArrayList<Object> getStruct(){
		return this.regionStruct;
	}
	
	void setName(String name){
		this.regionName = name;
	}
	
	void setStruct(ArrayList<Object> struct){
		this.regionStruct = struct;
	}
}

/**
 * 
 * @author Robert
 *
 */
class institutionStructure {
	String institName;
	String institArch;
	String institSchema;
	String institAnnotations;
	String doc;
	ArrayList<Object> institStruct = new ArrayList<Object>();
	
	 
	String getName(){
		return this.institName;
	}
	
	ArrayList<Object> getStruct(){
		return this.institStruct;
	}
	
	void setName(String name){
		this.institName = name;
	}
	
	void setStruct(ArrayList<Object> struct){
		this.institStruct = struct;
	}
}

/**
 * Class used to build the recipes for cyclus.
 * @author Robert
 *
 */
class Nrecipe {
	String Name = new String();
	String Basis = new String();
	ArrayList<isotopeData> Composition = new ArrayList<isotopeData>();
	
	String getName(){
		return this.Name;
	}
	
	String getBasis(){
		return this.Basis;
	}
	
	ArrayList<isotopeData> getComposition(){
		return this.Composition;
	}
	
	void setName(String name){
		this.Name = name;
	}
	
	void setBasis(String basis){
		this.Basis = basis;
	}
	
	void setComposition(ArrayList<isotopeData> isoData){
		this.Composition = isoData;
	}
}

/**
 * Contains name, atom, and mass information for specify an isotope within a recipe.
 * @author Robert
 *
 */
class isotopeData {
	String Name = new String();
	double atom;
	double value;
	
	
}

/**
 * Class used to create the links between nodes.
 * @author Robert
 *
 */
class nodeLink {
	Object source;
	Object target;
	ConnectorLine line = new ConnectorLine();
	
}

/**
 * Class used to represent regions in Cyclus. Contains structures for; 
 * name, type, data structures, data, available facilities, and institutions.
 * @author Robert
 *
 */
class regionNode{
	String name = new String();
	String type = new String();
	String entity; 
	String archetype;
	String doc;
	ArrayList<Object> regionStruct = new ArrayList<Object>();
	ArrayList<Object> regionData  = new ArrayList<Object>();
	ArrayList<String> institutions = new ArrayList<String>();
	static RegionShape regionShape = new RegionShape();
}

/**
 * Class used to represent institutions in Cyclus. Contains structures for; 
 * name,type, data structures, data, available prototypes, and 
 * initial facilityItems (facility name /number)
 * @author Robert
 *
 */
class instituteNode{
	String name;
	String type;
	String entity;
	String archetype;
	String doc;
	ArrayList<Object> institStruct  = new ArrayList<Object>();
	ArrayList<Object> institData  = new ArrayList<Object>();
	Map<String, Integer> availFacilities = new HashMap<String, Integer>();
	static InstitutionShape institutionShape = new InstitutionShape();
}

/**
 * Class containing the simulation information for the current working
 * simulation. 
 * @author Robert
 *
 */
class simInfo{
	String duration;
	String startMonth;
	String startYear;
	String decay;
	String simHandle;
	String notes;
	String description;
}

class facilityNode{
	Object name;
	String facilityType = "";
	Integer facTypeIndex = 0;
	String facLifetime  = "";
	Integer userLevel = 0;
	ArrayList<Object> facilityData = new ArrayList<Object>();
	ArrayList<Object> facilityStructure = new ArrayList<Object>();	
	ArrayList<facilityNode> facilityClones = new ArrayList<facilityNode>();
	int parentIndex;
	FacilityCircle cycicCircle = new FacilityCircle();
	FacilityCircle sorterCircle = new FacilityCircle();
	protected String niche;
	protected String doc;
	public String archetype;
}


class skinSet{
	String name; 
	Integer radius;
	String textPlacement;
	Map<String, Image> images = new HashMap<String, Image>();
}

