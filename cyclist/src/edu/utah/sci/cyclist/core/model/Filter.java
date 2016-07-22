package edu.utah.sci.cyclist.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.model.DataType.Classification;
import edu.utah.sci.cyclist.core.model.DataType.FilterType;
import edu.utah.sci.cyclist.core.model.DataType.Role;
import edu.utah.sci.cyclist.core.model.DataType.Type;
import edu.utah.sci.cyclist.neup.model.Nuclide;

public class Filter implements Observable, Resource {
	static Logger log = Logger.getLogger(Filter.class);
	
	private String _id = UUID.randomUUID().toString();
	
	private boolean _valid = true;
	private String _toStringCache = "1=1";  //Sqlite doesn't accept "where true" but accepts "where 1=1".
	private CyclistDatasource _ds = null;
	private Field _field;
	private DataType _dataType;
	protected ListProperty<Object> _values = new SimpleListProperty<>();
	private ObjectProperty<Range> _valueRange = new SimpleObjectProperty<>();
	private ObservableSet<Object> _selectedItems = FXCollections.observableSet();
	private ObjectProperty<Range> _selectedRange = new SimpleObjectProperty<>(Range.INVALID_RANGE);
	
	private List<InvalidationListener> _listeners = new ArrayList<>();
	private  Consumer<Void> _onDSUpdated = null;
	private Boolean _dsChanged = true;
	private boolean _allSelected = false;
	private BooleanProperty _isRange = new SimpleBooleanProperty(false);
	private Consumer<Void> _onChanged = null;
	
	// for restore
	public Filter() {
	}
	
	public Filter(Field field) {
		this(field, true);
	}
	
	public Filter(Field field, boolean auto){
		_field = field;
		init(auto);
	}
	
	public void setOnChanged(Consumer<Void> action) {
		_onChanged = action;
	}
	
	public ObservableSet<Object> selectedItems() {
		return _selectedItems;
	}
	private void init(boolean auto) {
		_dataType = new DataType(_field.getDataType());
		
		switch (getClassification()) {
		case C:
			_isRange.set(false);
			break;
		case Cdate:
			_isRange.set(true);
			break;
		case Qd:
			_isRange.set(true);;
			break;
		case Qi:
			if(getFilterType() == FilterType.RANGE){
				_isRange.set(true);
			}else{
				_isRange.set(false);
			}
		}
		
		if (auto) {
			if (isRange()) {
				_selectedRange.set(_field.getValueRange());
				setAllSelected(_field.getValueRange().isValid());
			} else {
				_selectedItems.addAll(getValues() != null ? getValues() : new ArrayList<Object>());
				setAllSelected(true);
			}
			setupListeners();
		}	
		if (isRange()) {
			_valueRange.bind(_field.valueRangeProperty());
		} else {
			_values.bind(_field.valuesProperty());
		}
	}
	
	public BooleanProperty isRangeProperty() {
		return _isRange;
	}
	
	public boolean isRange() {
		return _isRange.get();
	}
	
	public ObjectProperty<Range> valueRangeProperty() {
		return _valueRange;
	}
	
	public Range getValueRange() {
		return _valueRange.get();
	}
	
	public Range getSelectedRange() {
		return _selectedRange.get();
	}
	
	public void setSelectedRange(Range range) {
		_selectedRange.set(range);
	}
	
	private void setupListeners() {	
		_values.addListener(new InvalidationListener() {	
			@Override
			public void invalidated(Observable observable) {
				if (getValues() == null) return;
				
				if (_allSelected) {
    				_selectedItems.addAll(getValues());
    			} else {
    			}	
    			invalidate();
    			if (_onChanged != null)
    				_onChanged.accept(null);
			}
		});
		
		_valueRange.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				Range range = getValueRange();
				Range selected = getSelectedRange();
				setAllSelected(range.min == selected.min && range.max == selected.max);
				if (!selected.isValid()) {
					setSelectedRange(range);
				} else if (!_allSelected && (range.min > selected.min || range.max < selected.max)) {
					setSelectedRange(new Range(Math.max(range.min,  selected.min), Math.min(range.max, selected.max)));
				}
				if (_onChanged != null)
					_onChanged.accept(null);
			}
		});
	}
	
	public String getUID() {
		return _id;
	}
	
	public void save(IMemento memento) {
		_field.save(memento.createChild("field"));
		
		IMemento group;
		
		if (isRange()) {
			group = memento.createChild("selected-range");
			group.putDouble("from", getSelectedRange().min);
			group.putDouble("to", getSelectedRange().max);
		} else {
    		group = memento.createChild("selected");
    		// TODO: can save the class only once
    		for (Object value : _selectedItems) {
    			if (value instanceof Number || value instanceof String || value instanceof Nuclide) {
    				saveObj(group.createChild("value"), value);		
    			} 
    		}
		}
	}
	
	private void saveObj(IMemento memento, Object value) {
		memento.putString("class", value.getClass().toString());
		memento.putTextData(value.toString());
	}
	
	private Object restoreObj(IMemento memento) {
		return createObj(memento.getString("class"), memento.getTextData());
	}
	
	private Object createObj(String cls, String str) {
		if (cls.equals(String.class.toString())) {
			return str;
		} else if (cls.equals(Integer.class.toString())) {
			return Integer.valueOf(str);
		} else if (cls.equals(Double.class.toString())) {
			return Double.valueOf(str);
		} else if (cls.equals(Nuclide.class.toString())) {
			return Nuclide.create(str);
		} else {
			log.warn("*** unknown value class: "+cls);
			return str;
		}
	}
	
	
	public void restore(final IMemento memento, Context ctx) {
		// a filter owns its own copy of a field. 
		// create a field instead of looking it up in the context
		_field = new Field();
		_field.restore(memento.getChild("field"), ctx);
		
		init(false);
		if (isRange()) {
			IMemento group = memento.getChild("selected-range");
			setSelectedRange(new Range(group.getDouble("from"), group.getDouble("to")));
			if (_onChanged != null)
				_onChanged.accept(null);
			setupListeners();
		} else {	
			IMemento group = memento.getChild("selected");
			for (IMemento ref : group.getChildren("value")) {
				_selectedItems.add(restoreObj(ref));
			}
			if (_onChanged != null)
				_onChanged.accept(null);
			setupListeners();
		} 
	}

	
	public void setOnDatasouceChanged(Consumer<Void> action){
		_onDSUpdated = action;
	}
	
	public void setDatasource(CyclistDatasource ds) {
//		if (_ds == ds) return;
		
		_ds = ds;
		_dsChanged = true;
		if(_onDSUpdated != null){
			_onDSUpdated.accept(null);
		}
	}
	
	public CyclistDatasource getDatasource(){
		return _ds;
	}
	
	public Field getField() {
		return _field;
	}
	
	public String getName() {
		return _field.getName();
	}
	
	public String getLabel() {
		return getName();
	}
	
	public DataType getDataType() {
		return _dataType;
	}
	
	public Role getRole() {
		return _dataType.getRole();
	}
	
	public Classification getClassification() {
		return _dataType.getClassification();
	}
	
	public FilterType getFilterType(){
		return _dataType.getFilterType();
	}
	
	public Type getType() {
		return _dataType.getType();
	}
	
	public boolean isValid() {
		Boolean reply = getValues() != null && !_dsChanged;
		_dsChanged = false;
		return reply;
	}
	
	public boolean isRangeValid() {
		Boolean valid =  _field.getValueRange().isValid() && !_dsChanged;
		_dsChanged = false;
		return valid;
	}
	
	public ObservableList<Object> getValues() {
		return _values.get(); // _field.getValues();
	}
	
	public ListProperty<Object> valuesProperty() {
		return _values;
	}
	
	public ObservableSet<Object> getSelectedValues() {
		return _selectedItems;
	}
	
	public boolean isSelected(Object value) {
		return _selectedItems.contains(value);
	}
	
	public void setAllSelected(boolean value) {
		_allSelected = value;
	}
	
	public void selectValue(Object value, boolean select) {
		if (select) {
			_selectedItems.add(value);
		} else {
			_selectedItems.remove(value);
		}
		setAllSelected(_selectedItems.size() == getValues().size());
		invalidate();
	}

	public void selectAll(boolean value) {
		if (value) {
			if (_selectedItems.size() == getValues().size()) return;
			_selectedItems.addAll(_values);
			setAllSelected(true);
		} else {
			if (_selectedItems.size() == 0) return;
			_selectedItems.clear();
			setAllSelected(false);
		}
		invalidate();
	}
	
	public void selectRange(Range range) {
		setSelectedRange(range);
		Range value = getValueRange();
		setAllSelected(value.min == range.min && value.max == range.max);
		invalidate();
	}
	
	@Override
	public void addListener(InvalidationListener listener) {
		if (!_listeners.contains(listener))
			_listeners.add(listener);
		
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		_listeners.remove(listener);
	}
	
	private void invalidate() {
		if (_valid) {
			_valid = false;
			for (InvalidationListener listener : _listeners) {
				listener.invalidated(this);
			}
		}
	}
	
	/*
	 * Reset the filter to include all the values.
	 * (As when it is first created)
	 * Is used when a new simulation brings new set of values to the filter, so the old selected values have to be removed.
	 * ===  TBD - Might change depending on the request of what to do if the data source of the filter has been changed. ===  
	 */
	private void resetFilterValues(){
		if (_valid) {
			_toStringCache = "1=1";
		}
	}
	
	public boolean isActive() {
		return !_allSelected;
	}
	
	public String toString() {
		if (!_valid) {
			if (_allSelected) {
    			_toStringCache = "1=1";
			} else if (isRange()) {
				String function = null;
				String name = function != null? (function.indexOf(")") >-1 ? function.substring(0, function.indexOf(")"))+ " " +getName() +")" : function+"("+getName()+")") : getName();
				String str = name+" >=" + getSelectedRange().min + " AND " + name +" <=" + getSelectedRange().max;
				_toStringCache = str;
			} else {
				StringBuilder builder = new StringBuilder();
				if (_selectedItems.size() > 0) {
					builder.append(getName()).append(" in (");
					boolean first = true;
					for (Object item : _selectedItems) {
						if (first) first = false;
						else builder.append(", ");
			
						if (item instanceof String) {
							builder.append("'").append(item.toString()).append("'");
						} else if (item instanceof CyclistData) {
							builder.append(((CyclistData)item).sqlValue());
						} else 
							builder.append(item.toString());
					}
					builder.append(")");
					
				} else {
					builder.append("1=0");
				}
			
				_toStringCache = builder.toString();
			}
			_valid = true;
		}
		
//		System.out.println("filter to string:"+_toStringCache);
		return _toStringCache;
	}
	
	public void setValid(Boolean isValid) {
		_valid = isValid;
	}
}
