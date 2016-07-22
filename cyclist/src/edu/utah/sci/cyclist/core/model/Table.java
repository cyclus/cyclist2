/*******************************************************************************
 * Copyright (c) 2013 SCI Institute, University of Utah.
 * All rights reserved.
 *
 * License for the specific language governing rights and limitations under Permission
 * is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: The above copyright notice 
 * and this permission notice shall be included in all copies  or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Yarden Livnat  
 *     Kristi Potter
 *******************************************************************************/
package edu.utah.sci.cyclist.core.model;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.controller.SessionController;
import edu.utah.sci.cyclist.core.controller.XMLMemento;
import edu.utah.sci.cyclist.core.util.DataFactory;
import edu.utah.sci.cyclist.core.util.QueryBuilder;
import edu.utah.sci.cyclist.core.util.SQL;
import edu.utah.sci.cyclist.core.util.SQLUtil;

public class Table implements Resource {

	public static final String DATA_SOURCE = "datasource";
	public static final String REMOTE_TABLE_NAME = "remote-table-name";

	static Logger log = Logger.getLogger(Table.class);
	
	public enum SourceLocation {
		REMOTE,
		LOCAL_ALL,
		LOCAL_SUBSET
	}
	
	public enum NumericRangeValues {
		MIN,
		MAX,
		CHOSEN_MIN,
		CHOSEN_MAX
	}
	
	private String _id = UUID.randomUUID().toString();
	private String _alias;
	private String _name;
	private Schema _schema = new Schema(this);
	private CyclistDatasource _datasource;
	private Map<String, Object> _properties = new HashMap<>();

	private List<TableRow> _rows = new ArrayList<>();
	private String _localDataFile;

	private SourceLocation _sourceLocation;
	private int _dataSubset;
	private String _saveDir = "";
	private Boolean _isStandardSimulation = false;
	//Holds the list of values of a field in the current table in a specified data source.
	//First key - the data source.
	//Then inside the specified data source it maps between a field and a list of values. 
	private Map<String,Map<String,List<Object>>> _cachedFieldsValues = new HashMap<>();
	
	public Table() {
		this("");
	}
	
	public Table(String name) {
		_name = name;
		_alias = name;
		_sourceLocation = SourceLocation.REMOTE;
		_dataSubset = 0;
		setProperty("uid", UUID.randomUUID().toString());
	}
	
	public Table(Table tbl){
		_name = tbl.getName();
		_alias = tbl.getAlias();
		_sourceLocation = tbl.getSourceLocation();
		_datasource = tbl.getDataSource();
		_localDataFile = tbl.getLocalDatafile();
		_saveDir = tbl.getSaveDir();
        setProperty(REMOTE_TABLE_NAME, _name);	
        extractSchema();
	}
	
	public String getUID() {
		return _id;
	};
	
    // Save the table
	public void save(IMemento memento) {
		memento.putString("uid", _id);
		memento.putString("name", getName());
		memento.putString("alias", getAlias());
		_schema.save(memento.createChild("Schema"));
		if (_datasource != null)
			memento.putString("datasource-uid", _datasource.getUID());
		memento.putString("source-location", _sourceLocation.toString());
		memento.putInteger("subset", _dataSubset);

		IMemento mapMemento = memento.createChild("property-map");

		// Set things saved in the properties map
		Set<String> keys = _properties.keySet();
		for(String key: keys){
			
			Object value = _properties.get(key);

			IMemento entryMemento = mapMemento.createChild("entry");
			entryMemento.putString("key", key);
			entryMemento.putString("class", value.getClass().toString());
			

			// Save integers or strings as strings
			if(value.getClass().toString().equals(String.class.toString()) || 
  			   value.getClass().toString().equals(Integer.class.toString()))
				entryMemento.putTextData((String)value);
			else{

				log.warn("Table:save() NEED TO CHECK FOR SAVE-ABLE OBJECTS!!");
				IMemento valueMemento = entryMemento.createChild("value");
				valueMemento.putString("value-ID", value.toString());		
			}
		}
	}
	
	// Restore the table
	public void restore(IMemento memento, Context ctx ){
		_id = memento.getString("uid");
		ctx.put(_id, this);
		
		setName(memento.getString("name"));
		setAlias(memento.getString("alias"));

		// Get the datasource
		String datasourceId = memento.getString("datasource-uid");
		setDataSource(ctx.get(datasourceId, CyclistDatasource.class));
		
		// Get the location of the data source
		setSourceLocation(memento.getString("source-location"));
		
		// Get the data subset
		setDataSubset(memento.getInteger("subset"));
		
		// Save the subset
		memento.putInteger("subset", _dataSubset);
		
		// Get values in the property map	
		IMemento mapMemento = memento.getChild("property-map");
		IMemento[] entries =  mapMemento.getChildren("entry");
		for(IMemento entry: entries){

			// Get the key of the object
			String key = entry.getString("key");
			
			// Get the class of the object
			String classType = entry.getString("class");
				
			// If we have a string
			if(classType.equals(String.class.toString())){
				 String value = entry.getTextData();
				 setProperty(key, value);
			}
			// If we have an Integer
			else if(classType.equals(Integer.class.toString())){
				 Integer value = Integer.parseInt(entry.getTextData());
				 setProperty(key, value);
			}
			else{
				log.warn("Table:load() NEED TO IMPLEMENT OBJECT FACTORIES!!");
			}	
		}
		
		// Restore the schema
		Schema schema = new Schema(this);
		schema.restore(memento.getChild("Schema"), ctx);
		setSchema(schema);
//		extractSchema();
		
	}

	// Extract the table schema from the database
	public void extractSchema(){
		
		try (Connection conn = _datasource.getConnection()) {
			//printTypeInfo(conn);
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getColumns(null, null, getName(), null);
			while (rs.next()) {				
				String colName = rs.getString("COLUMN_NAME");
			
				Field field = new Field(colName);
				field.setTable(this);
				field.set(FieldProperties.REMOTE_NAME, colName);
				field.set(FieldProperties.REMOTE_DATA_TYPE, rs.getInt("DATA_TYPE"));
				field.set(FieldProperties.REMOTE_DATA_TYPE_NAME, rs.getString("TYPE_NAME"));
				
				_schema.addField(field);
			}
		} catch (Exception e) {
			log.error("Error while parsing schema: "+e);
		}finally{
			_datasource.releaseConnection();
		}
		
		_schema.update();
	}
	
	/*
	 * Restores table from the simulation configuration file.
	 * Restores only the information which is relevant to the simulation.
	 */
	public void restoreSimulated(IMemento memento, Context ctx){
		_id = memento.getString("UID");
		setName(memento.getString("name"));
		if (_id == null) _id = getName(); //UUID.randomUUID().toString();
		
		ctx.put(_id, this);
//		System.out.println("restore table: "+_id);
		
		setDataSource(null);
		
		// Restore the schema
		Schema schema = new Schema(this);
		schema.restoreSimulated(memento.getChild("Schema"), ctx);
		setSchema(schema);
		_isStandardSimulation = true;
	}

//	private void printTypeInfo(Connection conn) {
//		DatabaseMetaData d;
//		try {
//			d = conn.getMetaData();
//			ResultSet rs = d.getTypeInfo();
//			ResultSetMetaData cmd = rs.getMetaData();
//			System.out.println("database types:");
//			while (rs.next()) {
//				for (int i=1; i<=cmd.getColumnCount(); i++) {
//					System.out.print(cmd.getColumnName(i)+": "+rs.getObject(i)+"  ");
//				}
//				System.out.println();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;	
	}
	
	public String getAlias(){
		if(_alias.equals(""))
			return getName();
		else
			return _alias;
	}
	
	public void setAlias(String alias){
		_alias = alias;
	}
	
	public void setSourceLocation(SourceLocation source){
		_sourceLocation = source;
	}
	
	public void setSourceLocation(String source){
		if(source.equals("REMOTE"))
			_sourceLocation = SourceLocation.REMOTE;
		else if(source.equals("LOCAL_ALL"))
			_sourceLocation = SourceLocation.LOCAL_ALL;
		else if(source.equals("LOCAL_SUBSET"))
			_sourceLocation = SourceLocation.LOCAL_SUBSET;
	}
	
	public SourceLocation getSourceLocation(){
		return _sourceLocation;
	}
	
	public void setDataSubset(int subset){
		_dataSubset = subset;
	}
	
	public int getDataSubset(){
		return _dataSubset;
	}
	
	@Override
    public String toString() {
        return getName();
    }
	
	public void setProperty(String property, Object value) {
		_properties.put(property, value);
	}

	public void removeProperty(String property) {
		_properties.remove(property);
	}

	public Object getProperty(String property) {
		return _properties.get(property);
	}

	public boolean hasProperty(String property) {
		return _properties.containsKey(property);
	}

	public String getStringProperty(String property) {
		Object value = _properties.get(property);
		if (value == null)
			return null;
		else if (value instanceof String)
			return (String)value;
		else
			return value.toString();
	}
	
	/**
	 * Convenient method
	 * @param datasource
	 */
	public void setDataSource(CyclistDatasource datasource){
		_datasource = datasource;
	}
	
	/**
	 * Convenient method
	 * @return
	 */
	public CyclistDatasource getDataSource(){
		return _datasource;
	}
	
	public void setSchema(Schema schema) {
		_schema = schema;
		clear();
	}
	
	public Schema getSchema() {
		return _schema;
	}
	
	public List<Field> getFields() {
		List<Field> list = new ArrayList<Field>();
		int n = _schema.size();
		for (int i=0; i<n; i++) {
			list.add(_schema.getField(i));
		}
		return list;
	}

	public Field getField(int index) {
		return _schema.getField(index);
	}
	
	public Field getField(String name) {
		return _schema.getField(name);
	}
	
	public Field getField(String name, Boolean casesensitive) {
		return _schema.getField(name, casesensitive);
	}
	
	public void setFieldSelected(int index, boolean selected){
		_schema.getField(index).setSelectedProperty(selected);
	}

	public boolean hasField(Field field) {
		if (field.getTable() == this) return true;
		
		// for now check based on field name
		return _schema.contain(field);
	}
	
	public int getNumColumns() {
		return _schema.size();
	}


	public void addRow(TableRow row) {
		_rows.add(row);
	}

	public List<TableRow> getRows() {
		return _rows;
	}

	
	public QueryBuilder queryBuilder() {
		return new QueryBuilder(this);
	}
	
	/*
	 * Returns whether the table was loaded for simulation purpose 
	 * (from the simulation configuration file)
	 * @return true- if the table is for simulation, false - otherwise.
	 */
	public Boolean getIsStandardSimulation(){
		return _isStandardSimulation;
	}
	
	public Task<ObservableList<Object>> getFieldValues(final Field field) {
		CyclistDatasource ds = getDataSource();
		return getFieldValues(ds, field);
	}
	
	public Task<ObservableList<Object>> getFieldValues(CyclistDatasource ds, final Field field)
	{
		return getFieldValues(ds, field, false);
	}
	
	public Task<ObservableList<Object>> getFieldValues(CyclistDatasource ds1, final Field field, boolean force) {
		final CyclistDatasource ds = getAvailableDataSource(ds1,force);
			
		Task<ObservableList<Object>> task = new Task<ObservableList<Object>>() {
			@Override
			protected ObservableList<Object> call() throws Exception {
				List<Object> values = new ArrayList<>();
				if (ds != null) {
					values = readFieldValuesFromCache(field.getName(), ds);
					//If couldn't find in the cache - try from the file.
					if(values.size() == 0){
						values = readFieldValuesFromFile(field.getName(),ds);
					}
					if(values.size() == 0)
					{
						try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()){
							updateMessage("querying");
						
							// TODO: Fix this query building hack 
							String query = "select distinct "+field.getName()+" from "+getName()+" order by "+field.getName();
							log.debug("query: "+query);
							
							ResultSet rs = stmt.executeQuery(query);
							Function<Object, Object> convert[] = SQLUtil.factories(rs.getMetaData());
							
							while (rs.next()) {
								if (isCancelled()) {
									log.info("task canceled");
									stmt.cancel();
									updateMessage("Canceled");
									break;
								}
							
								values.add(convert[0].apply(rs.getObject(1)));
							}

							writeFieldValuesToCache(field.getName(), ds, values);
							writeFieldValuesToFile(field.getName(), field.getType().toString(), field.getRole().toString(), ds, values);
						} catch (SQLException e) {
							log.error("task sql exception: ",e);
							updateMessage(e.getLocalizedMessage());
							throw new Exception(e.getMessage(), e);
						} finally {
							ds.releaseConnection();
						}
					}
				}
				return FXCollections.observableList(values);
			}
		};
		
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		return task;
	}
	
	
	public TableRow getRow(int index) {
		return _rows.get(index);
	}

	public void clear() {
		_rows.clear();
	}
	
	public String getLocalDatafile() {
		return _localDataFile;
	}
	public void setLocalDatafile(String workDir) {
		_saveDir = workDir;
		_localDataFile = workDir + "/" + getDataSource()+"/" + getName() + ".sqlite";
	}
	
	public String getSaveDir(){
		return _saveDir;
	}
	
	/* Saves the values of a chosen filter into a file 
	 * Creates an xml file with the table name, and writes the filter's field name, its type, role and its values
	 * If the field already exist in the file - do nothing. 
	 * @param fieldName - String.
	 * @param fieldType - DataType.Type enum.
	 * @param role - DataType.Role enum
	 * @param ds - data source.
	 * @param List<Object> values - list of values to save.
	 * */
	private void writeFieldValuesToFile(String fieldName, String fieldType, String role ,CyclistDatasource ds, List<Object> values){
		if(_saveDir == ""){
			_saveDir = SessionController.DEFAULT_WORKSPACE;
		}
		File defaultDir = new File(_saveDir);
		if(!defaultDir.exists()){
			defaultDir.mkdir();
		}
		
		// If the save directory does not exist, create it
		File saveDir = new File(_saveDir+ "/" + ds.getUID() +"/");
		if (!saveDir.exists()){
			saveDir.mkdir();
		}
			
		// The save file
		File saveFile = new File(saveDir+"/"+ getName() + ".xml");
		
		XMLMemento root;
		IMemento fieldsNode = null;
		Boolean writeNewNode= true;
		
		 try {
			 //If file already exists - read the existing nodes, and add the new Field in its place.
			 if(saveFile.exists()){
				 Reader reader = new FileReader(saveFile);
				 
				 //Checks if the root node exists.
				 try{
					 root = XMLMemento.createReadRoot(reader);
					 fieldsNode = root.getChild("Fields");
				 }catch(Exception e){
					 root = XMLMemento.createWriteRoot("root");
				 }
				 
				 // Checks if the "Fields" node exists. 
				 // If yes - try to find the Field node with the given name. 
				 // If not - creates a new "Fields" node and mark that a new field node has to be written.
				 if(fieldsNode != null)
				 {
					 //Check if the field node already exists. If yes - no need to write it again to the table xml file.
					 if (getField(fieldsNode, fieldName) != null)
					 {
						 writeNewNode = false;
					 }
				 } else{
					 fieldsNode = root.createChild("Fields");
				 }
			 } else{
		
				 // If new file - Create the root memento
				 root = XMLMemento.createWriteRoot("root");
				// Create Fields node
				 fieldsNode = root.createChild("Fields");
			 }
	    
			 //If no such field node yet - write the field and its values into the file.
			 if(writeNewNode){
				 writeFieldNodeToFile(fieldName, fieldType, role, fieldsNode, values);
			 }
			 root.save(new PrintWriter(saveFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/*Checks if a given field already exists in the table xml file.
	 * If field exists - return the field else- return null */
	private IMemento getField(IMemento fields, String fieldName){
		IMemento fieldResult = null;
		try
		{
			 IMemento[] fieldNodes = fields.getChildren("Field");
			 for (IMemento field:fieldNodes){
				 String name = field.getString("name");
				 if (name.equals(fieldName)){
					 fieldResult = field;
					 break;
				 }
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fieldResult;
	}
	
	/* Creates a new Field node in the table xml file*/
	private void writeFieldNodeToFile(String fieldName, String fieldType, String fieldRole, IMemento fieldsNode, List<Object> values){
		
		//Create the Field node
		 IMemento FieldNode = fieldsNode.createChild("Field");
		
		 // Set the field name
		 FieldNode.putString("name", fieldName);
		 FieldNode.putString("type", fieldType);
		 FieldNode.putString("role", fieldRole);
		 if (values.size() > 0 && values.get(0) != null) {
			 FieldNode.putString("class", values.get(0).getClass().getCanonicalName());
		 }
		 StringBuilder sb = new StringBuilder(); 
		 for(Object value:values){
			 if (value == null) {
				 log.warn("*** Warning: field '"+fieldName+"' has a null value");
			 } else  {
				 sb.append(value.toString()+";");
			 }
		 }
		 FieldNode.putTextData(sb.toString());
	}
	
	/* Saves a distinct values list to the cache.
	 * For a given field in the current table in the specified data source - 
	 * write the list values to the cache, where the keys are the field and the data source.
	 * 
	 * @param String fieldName - the field to use as a key in the map.
	 * @param  CyclistDatasource ds - the data source to to use as a key in the map.
	 * @param List<Object> - the list of values to write in the cache for the specified keys.  */
	private void writeFieldValuesToCache(String fieldName, CyclistDatasource ds, List<Object> values){
		
		Map<String,List<Object>> fieldValues = _cachedFieldsValues.get(ds.getUID());
		
		if(fieldValues == null){
			fieldValues = new HashMap<String,List<Object>>();
			fieldValues.put(fieldName, values);
			_cachedFieldsValues.put(ds.getUID(), fieldValues);
			
		} else{
			fieldValues.put(fieldName, values);
		}
	}
	
	/* Reads distinct values from the cache.
	 * For a given field in the current table in the specified data source - 
	 * if the data source key and the field key exist in the cache - read its values from the cache.
	 * 
	 * @param String fieldName - the field to look for in the map.
	 * @param  CyclistDatasource ds - the data source to look for in the map   
	 * @return List<Object> - the list of values found for the given data source and field.
	 *                        (returns an empty list if not found)  */
	
	private List<Object> readFieldValuesFromCache(String fieldName, CyclistDatasource ds){
		List<Object> values = new ArrayList<>();
		Map<String,List<Object>> fieldValues = _cachedFieldsValues.get(ds.getUID());
		if(fieldValues != null){
			if(fieldValues.get(fieldName) != null){
				values = fieldValues.get(fieldName);
			}
		}
		return values;
	}
	
	/* Reads distinct values from a file 
	 * For a given field in a given table- 
	 * if the table xml file exists and it contains the field values - read the values from the file 
	 * Before reading - check the field type, and convert the values to the right type. 
	 * @param fieldName - field name to search
	 * @param ds - defines the file to look for the field properties 
	 * */

	private List<Object> readFieldValuesFromFile(String fieldName, CyclistDatasource ds){
		
		List<Object> values = new ArrayList<>();
		if(_saveDir == ""){
			_saveDir = SessionController.DEFAULT_WORKSPACE;
		}
		
		// If the save file does not exist - return an empty list.
		File saveFile = new File(_saveDir+ "/" + ds.getUID() +"/"+ getName() + ".xml");
		if (!saveFile.exists()){
			return values;
		} else{
			try{
				 Reader reader = new FileReader(saveFile);
				 XMLMemento root = XMLMemento.createReadRoot(reader);
				 IMemento fieldsNode = root.getChild("Fields");
				 IMemento field = getField(fieldsNode, fieldName);
				 if(field != null){
					 String className = field.getString("class");
					 Function<String, Object> factory = DataFactory.findFactory(className);
					 
					 String[] tmpValues = field.getTextData().split(";");
					 
					 for (String value : tmpValues) {
						 values.add(factory.apply(value));
					 }
//					 String type = field.getString("type") == null?"TEXT":field.getString("type");
//					 try{
//						 switch(DataType.Type.valueOf(type)){
//							 case TEXT:
//								 for(String value: tmpValues){
//									 values.add(value);
//								 }
//								 break;
//							 case NUMERIC:
//								 String role = field.getString("role")==null? "MEASURE":field.getString("role");
//								 if(DataType.Role.valueOf(role) == Role.MEASURE){
//									 for(String value: tmpValues){
//										 values.add(Double.parseDouble(value));
//									 }
//								 }else{
//									 for(String value: tmpValues){
//										 values.add(Integer.parseInt(value));
//									 }
//								 }
//								 break;
//							 case INT_TIME:
//								 for(String value: tmpValues){
//									 values.add(Integer.parseInt(value));
//								 }
//								 break;
//							default: 
//									 for(String value: tmpValues){
//										 values.add(value);
//									 }
//						 }
//					 }catch(NumberFormatException ex){
//						 for(String value: tmpValues){
//							 values.add(value);
//						 }
//					 }
					 
					 
				 }
				 //If reading from file - it means the values weren't found in the cache.
				 //Save them in the cache for the next time.
				 if(values.size() >0){
					 writeFieldValuesToCache(fieldName, ds, values);
				 }
				 return values;
			 }catch(Exception e){
				return values;
			 }
		}
	}
	
	/*
	 * Returns the currently available data source.
	 * 
	 * @param CyclistDatasource externalDs - suggested data source other than the table's data source.
	 * @param boolean force - whether the external data source must be applied 
	 * 		  (even it the table has its own data source), or not.
	 * @return CyclistDatasource
	 */
	private CyclistDatasource getAvailableDataSource(CyclistDatasource externalDs, boolean force){
		CyclistDatasource lds = getDataSource();
		CyclistDatasource ds = force ? ( externalDs != null ? externalDs : lds ) : (lds != null ? lds : externalDs);
		return ds;
	}
	
	
	public Task<ObservableValue<Range>> getFieldRange(final Field field) {
		CyclistDatasource ds = getDataSource();
		return getFieldRange(ds, field);
	}
	
	public Task<ObservableValue<Range>> getFieldRange(CyclistDatasource ds, final Field field)
	{
		return getFieldRange(ds, field, false);
	}
	
	/* Name: getFieldRange
	 * For a numeric field filter - gets the minimum and maximum values within its possible range, for any possible grouping.
	 * It checks for the field SQL function and finds the values accordingly. 
	 */
	public Task<ObservableValue<Range>> getFieldRange(CyclistDatasource externalDs, final Field field, boolean force) {
		final CyclistDatasource ds = getAvailableDataSource(externalDs, force);
		
		Task<ObservableValue<Range>> task = new Task<ObservableValue<Range>>() {
			
			@Override
			protected ObservableValue<Range> call() throws Exception {
				double min=0, max=0;

				if (ds != null) {
					try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()){
						updateMessage("querying");
						SQL.Functions function = SQL.Functions.VALUE;
						if(field.getString(FieldProperties.AGGREGATION_FUNC) != null){
							function = SQL.Functions.getEnum(field.getString(FieldProperties.AGGREGATION_FUNC));
						}
						String query = "";
						Boolean checkForSum = false;
						
//						switch(function){
//						case AVG:
//						case MIN:
//						case MAX:
//						case VALUE:
//							query = "SELECT MIN("+field.getName()+") AS min, MAX(" + field.getName() + ") AS max FROM "+ getName();
//							break;
//						case COUNT:
//							query = "SELECT 0 AS min, COUNT(" + field.getName() + ") AS max FROM "+ getName();
//							break;
//						case COUNT_DISTINCT:
//							query = "SELECT 0 AS min, COUNT( DISTINCT " + field.getName() + ") AS max FROM "+ getName();
//						case SUM:
//							query = "SELECT SUM( CASE WHEN " + field.getName()+ " <0 THEN " + field.getName() + " ELSE 0 END) AS neg_sum, " +  
//									"SUM( CASE WHEN " + field.getName()+ " >0 THEN " + field.getName() + " ELSE 0 END) AS pos_sum, " + 
//									"MIN(" + field.getName() +") as min, MAX(" + field.getName() +") as max "+
//									"FROM " + getName();
//							checkForSum = true;
//							break;
//						}
						
						query = "SELECT MIN("+field.getName()+") AS min, MAX(" + field.getName() + ") AS max FROM "+ getName();
						
						log.debug("query: "+query);
						ResultSet rs = stmt.executeQuery(query);
						
						while (rs.next()) {
							if (isCancelled()) {
								log.info("task canceled");
								updateMessage("Canceled");
								break;
							}
						
							min = rs.getDouble("min");
							max = rs.getDouble("max");
							if(checkForSum)
							{
								double posSum = rs.getDouble("pos_sum");
								max= (posSum==0)?max:posSum;
								
								double negSum = rs.getDouble("neg_sum");
								min= (negSum==0)?min:negSum;
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}finally {
						ds.releaseConnection();
					}
				}
				final Range range = new Range(min, max);
				ObservableValue<Range> res = new ObservableValueBase<Range>() {
					@Override
                    public Range getValue() {
	                    return range;
					}
				};
				return res;
			}
			
		};
		
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		
		return task;
		
	}
	
	

	
	private static final String GET_ROWS_QUERY = "select * from $table limit ?";
}