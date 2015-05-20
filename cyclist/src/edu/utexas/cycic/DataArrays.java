package edu.utexas.cycic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.shape.Line;

/**
 * This class contains all of the data structures used for CYCIC.
 * @author Robert
 *
 */
public class DataArrays{
	
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
	double mass;
	
	
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
	ArrayList<Object> regionStruct = new ArrayList<Object>();
	ArrayList<Object> regionData  = new ArrayList<Object>();
	ArrayList<String> institutions = new ArrayList<String>();
	static RegionShape regionCircle = new RegionShape();
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
	ArrayList<Object> institStruct  = new ArrayList<Object>();
	ArrayList<Object> institData  = new ArrayList<Object>();
	ArrayList<facilityItem> availFacilities = new ArrayList<facilityItem>();
	InstitutionShape institutionShape = new InstitutionShape();
}

/**
 * Class used to indicate an initial facility within an institution and
 * the number of these institutions at the start of the simualtion.
 * @author Robert
 *
 */
class facilityItem{
	String name;
	String number;
	
	facilityItem(String name, int number){
		this.name = name;
		this.number = Integer.toString(number);
	}
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
	String notes;
	String description;
}

class facilityNode{
	Object name;
	String facilityType = "";
	String entity; 
	Integer facTypeIndex = 0;
	ArrayList<Object> facilityData = new ArrayList<Object>();
	ArrayList<Object> facilityStructure = new ArrayList<Object>();	
	ArrayList<facilityNode> facilityClones = new ArrayList<facilityNode>();
	int parentIndex;
	FacilityCircle cycicCircle = new FacilityCircle();
	FacilityCircle sorterCircle = new FacilityCircle();
	protected String niche;
	public String archetype;
}


class skinSet{
	String name; 
	Map<String, Image> images = new HashMap<String, Image>();
}