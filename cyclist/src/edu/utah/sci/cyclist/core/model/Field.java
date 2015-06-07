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
 *******************************************************************************/
package edu.utah.sci.cyclist.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.model.DataType.Role;
import edu.utah.sci.cyclist.core.util.SQL;

public class Field implements Resource {
	static Logger log = Logger.getLogger(Field.class);

	private String _id = UUID.randomUUID().toString();
	private String _name;
	private String _semantic;
	private DataType _dataType;
	private BooleanProperty _selected;
	private ObjectProperty<Table> _tableProperty = new SimpleObjectProperty<>(); 
	private ListProperty<Object> _valuesProperty = new SimpleListProperty<>();
	private ObjectProperty<Range> _valueRangeProperty = new SimpleObjectProperty<Range>(Range.INVALID_RANGE);
	
	private Map<String, Object> _properties = new HashMap<>();
	private Boolean _isHidden = false;

	public Field(){
		this("");
	}

	public Field(Field other) {
		this(other._name, other._semantic);
		_dataType = new DataType(other._dataType);
		_selected = other._selected;
		setTable(other.getTable()); 
		for (Map.Entry<String, Object> entry : other._properties.entrySet()) {
			_properties.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Field(String name) {
		this(name, name);
	}
	
	public Field(String name, String semantic) {
		this._name = name;
		this._semantic = semantic;
		_selected = new SimpleBooleanProperty();
		_selected.set(true);
	}

	public String getUID() {
		return _id;
	}
	
	/*
	 * Values
	 */
	public ListProperty<Object> valuesProperty() {
		return _valuesProperty;
	}
	
	public void setValues(ObservableList<Object> values) {
		valuesProperty().set(values);
	}
	
	public ObservableList<Object> getValues() {
		return valuesProperty().get();
	}
	
	/*
	 * Range
	 */
	public ObjectProperty<Range> valueRangeProperty() {
		return _valueRangeProperty;
	}
	
	public Range getValueRange() {
		return _valueRangeProperty.get();
	}
	
	public void setValueRange(Range range) {
		_valueRangeProperty.set(range);
	}
	
	public String getName() {
		return _name;
	}

	public String getSemantic() {
		return _semantic;
	}
	
	public ObjectProperty<Table> tableProperty() {
		return _tableProperty;
	}
	
	public void setTable(Table table) {
		_tableProperty.set(table);
	}
	
	public Table getTable() {
		return _tableProperty.get();
	}
	
	public String getDataTypeName() {
		return (String) _properties.get(FieldProperties.DATA_TYPE_NAME);
	}

	public void set(String property, Object value) {
		_properties.put(property, value);
	}

	public Object get(String property) {
		return _properties.get(property);
	}
	
	public <T> T get(String key, Class<T> type) {
		Object value = _properties.get(key);
		return value == null ? null : type.cast(value);
	}
	
	public String getString(String property) {
		return (String) get(property);
	}
	
	public int getInt(String property) {
		return (int) get(property);
	}
	
	public DataType getDataType() {
		return _dataType;
	}
	
	public void setDataType(DataType dataType) {
		_dataType = dataType;
	}
	

	public DataType.Role getRole() {
		return _dataType.getRole();
	}
	
	public void setRole(DataType.Role role) {
		_dataType.setRole(role);
	}
	
	public DataType.Type getType() {
		return _dataType.getType();
	}
	
	public DataType.Classification getClassification() {
		return _dataType.getClassification();
	}
	
	public String toString() {
		return _name;
	}
	
	public boolean similar(Field field) {
		return getName().equals(field.getName()) && getType() == field.getType();
	}
	
	public boolean similarWithTable(Field field) {
		return getName().equals(field.getName()) && getType() == field.getType() && getTable() == field.getTable();
	}
	
	public Boolean isHidden(){
		return _isHidden;
	}
	
	
	// Save this field
	public void save(IMemento memento) {
		memento.putString("UID", getUID());
		memento.putString("name", _name);
		
		IMemento dataMemento = memento.createChild("datatype");
		String role = getRole()!=null?getRole().toString():"";
		dataMemento.putString("role", role);
		dataMemento.putString("type", getType().toString());
		String interp =  _dataType.getInterpretation()!=null?_dataType.getInterpretation().toString():"";
		dataMemento.putString("interp", interp);		
		dataMemento.putString("classification", getClassification().toString());
		dataMemento.putString("semantic", _semantic);

		memento.putBoolean("selected", _selected.get());
		
		memento.createChild("table").putString("ref-uid", getTable().getUID());
		
		// Set things saved in the properties map
		Set<String> keys = _properties.keySet();
		for(String key: keys){
			Object value = _properties.get(key);
			IMemento entryMemento = memento.createChild("entry");
			entryMemento.putString("key", key);
			
			if(value == null)
				entryMemento.putTextData("null");	
			else{
				entryMemento.putString("class", value.getClass().toString());
				entryMemento.putTextData(value.toString());
//				// Save integers or strings as strings
//				if(value.getClass().toString().equals(String.class.toString()) || 
//						value.getClass().toString().equals(Integer.class.toString()))
//					entryMemento.putTextData(value.toString());
//				else{
//					// TODO/FIXME: save some sort of Factory-ID
//					log.error("Table:save() NEED TO CHECK FOR SAVE-ABLE OBJECTS!!");
//					IMemento valueMemento = entryMemento.createChild("value");
//					valueMemento.putString("value-ID", value.toString());		
//				}
			}
		}
	}
	
	// Restore this field
	public void restore(IMemento memento, Context ctx) {
		try{
			_id = memento.getString("UID");
			_name = memento.getString("name");
			if (_id == null) _id = _name;
			ctx.put(_id, this);
//			System.out.println("restore "+_name);
    		
    		IMemento dataMemento = memento.getChild("datatype");
    		DataType.Role role = dataMemento.getString("role")!= "" ? DataType.Role.valueOf(dataMemento.getString("role")) : null;
    		DataType.Type type = DataType.Type.valueOf(dataMemento.getString("type"));
    		DataType.Interpretation interp = dataMemento.getString("interp")!= "" ? DataType.Interpretation.valueOf(dataMemento.getString("interp")) : null;
    		DataType.Classification classification = DataType.Classification.valueOf(dataMemento.getString("classification"));
    		
    		_dataType = new DataType(role, type, interp, classification);
    
    		_semantic = memento.getString("semantic");
    		_selected.set(memento.getBoolean("selected"));
    		String ref = memento.getChild("table").getString("ref-uid");
    		setTable(ctx.get(ref, Table.class));
    				
    		// Get the entries in the field
    		IMemento[] entries = memento.getChildren("entry");
    		for(IMemento entry:entries) {
    			String key = entry.getString("key");
    			String cls = entry.getString("class");
    						
    			if (cls == null) {
    				set(key, cls);
    			} 
    			else if((String.class.toString().equals(cls))){
    				String value = entry.getTextData();
    				set(key, value);
    			}
    			else if(Integer.class.toString().equals(cls)){
    				Integer value = Integer.parseInt(entry.getTextData());
    				set(key, value);
    			}	
    			else{
    				System.out.println("Field:load() NEED TO IMPLEMENT OBJECT FACTORIES!!");
    			}	
    		}	
		} catch(NullPointerException e){
			log.error("Error while restoring field: "+_name);
		}
	}
	
	/*
	 * Restores fields from the simulation configuration file.
	 * Restores only the information which is relevant to the simulation fields.
	 */
	public void createFromConfig(IMemento memento, Context ctx) {
			_id = memento.getString("UID");
			_name = memento.getString("name");
			if (_id == null) _id = _name;
			ctx.put(_id,  this);
			_semantic = memento.getString("semantic");
			if (_semantic == null) _semantic = _name;
			_isHidden = memento.getBoolean("isHidden");
			if(_isHidden==null) _isHidden=false;
			IMemento dataTypeMemento = memento.getChild("datatype");
			
			DataType.Type type = DataType.Type.valueOf(dataTypeMemento.getString("type"));
			DataType.Role role = dataTypeMemento.getString("role")!= "" ? DataType.Role.valueOf(dataTypeMemento.getString("role")):null;
			_dataType = new DataType(type, role);
			if ("Nuclide".equals(_semantic))
				_dataType.setClassification(DataType.Classification.C);
			if (_dataType.getRole() == Role.MEASURE){
				set(FieldProperties.AGGREGATION_DEFAULT_FUNC, SQL.DEFAULT_FUNCTION);
			}
			
	}

	public BooleanProperty getSelectedProperty() {
		return _selected;
	}
	
	public void setSelectedProperty(boolean selected){
		_selected.set(selected);
	}
	
	public Field clone() {
		Field copy = new Field(getName());
		
		copy._dataType = new DataType(_dataType);
		copy._selected = _selected;
		copy.setTable(getTable()); 
		for (Map.Entry<String, Object> entry : _properties.entrySet()) {
			copy._properties.put(entry.getKey(), entry.getValue());
		}
		
		return copy;
	}
	
}