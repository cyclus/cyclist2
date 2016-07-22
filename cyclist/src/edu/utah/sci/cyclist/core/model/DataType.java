package edu.utah.sci.cyclist.core.model;


public class DataType {

	public static enum Role {
		MEASURE, DIMENSION, INT_TIME;
	}

	public enum Type {
		NUMERIC,
		TEXT,
		DATE,
		DATETIME,
		INT_TIME,
		BOOLEAN,
		NA
	}
	
	public enum Interpretation {
		DISCRETE, CONTINUOUS
	}

	public enum Classification {
		C, Cdate, Qd, Qi
	}
	
	public enum StructureType {
		SIMPLE, COMPLEX, HIERARCHICAL
	}
	
	public enum FilterType{
		RANGE, LIST
	}
	
	private Role _role;
	private Type _type;
	private Interpretation _interp;
	private Classification _classification;
	private StructureType  _structureType;
	private FieldStructure _structure;
	private FilterType _filterType;
	
	public DataType(Type type) {
		this(type, null);
	}
	
	public DataType(Type type, Role role) {
		_type = type;
		setRole(role != null ? role : defaultRole(type));
		update();
	}
	
	public DataType(Role role, Type type, Interpretation interp, Classification classification) {
		_role = role;
		_type = type;
		_interp = interp;
		_classification = classification;
		setDefaultFilterType();
	}
	
	public DataType(DataType copy) {
		_role = copy._role;
		_type = copy._type;
		_interp = copy._interp;
		_classification = copy._classification;
		_filterType = copy._filterType;
	}
	
	private Role defaultRole(Type type) {
		Role role = Role.DIMENSION; // TODO: this may not be the correct default
		switch (type) {
		case NUMERIC:
			role = Role.MEASURE;
			break;
		case TEXT:
			role = Role.DIMENSION;
			break;
		case DATE:
		case DATETIME:
		case INT_TIME:
			role = Role.DIMENSION;
			break;
		case BOOLEAN:
		case NA:
			break;
		}
		
		return role;
	}
	

	/**
	 * read-only
	 * @return
	 */
	
	public Type getType() {
		return _type;
	}
	
	/**
	 * read-only
	 * @return
	 */
	public Classification getClassification() {
		return _classification;
	}
	
	public void setClassification(Classification c) {
		_classification = c;
	}
	
	/**
	 * 
	 * @return
	 */
	public Role getRole() {
		return _role;
	}
	
	/**
	 * 
	 * @param role
	 */
	public void setRole(Role role) {
		_role = role;
		if (_type == Type.NUMERIC) {
			if (role == Role.DIMENSION || role == Role.INT_TIME)
				_interp = Interpretation.DISCRETE;
			else
				_interp = Interpretation.CONTINUOUS;
		} else {
			_interp = Interpretation.DISCRETE;
		}
		setDefaultFilterType();
		update();
	}
	
	/**
	 * 
	 * @return
	 */
	public Interpretation getInterpretation() {
		return _interp;
	}
	
	/**
	 * 
	 * @param i
	 */
	public void setInterpetation(Interpretation i) {
		_interp = i;
		update();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isContinuous() {
		return _interp == Interpretation.CONTINUOUS;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDate() {
		return _type == Type.DATE || _type == Type.DATETIME;
	}
	
	public StructureType getStructurTypee() {
		return _structureType;
	}
	
	public void setStructureType(StructureType value) {
		_structureType = value;
	}
	
	public void setStructure(FieldStructure value) {
		_structure = value;
	}
	
	public FieldStructure getStructure() {
		return _structure;
	}

    public FilterType getFilterType() {
        return _filterType;
    }

    public void setFilterType(FilterType value) {
    	_filterType = value;
    }
    
    /*
     * Sets initial value for the filter type, depending on the field Interpretation.
     */
    private void setDefaultFilterType(){
    	if(_interp == Interpretation.CONTINUOUS){
			_filterType = FilterType.RANGE;
		}else{
			_filterType= FilterType.LIST;
		}
    }
	
    boolean force = false;
	private void update() {
		if (_interp == Interpretation.DISCRETE) {
			if (_type == Type.DATE || _type == Type.DATETIME || _role== Role.INT_TIME) 
				_classification = Classification.Cdate;
			else if (_type == Type.TEXT || force)
				_classification = Classification.C;
			else
				_classification = Classification.Qi;
		} else { /* Interpretation.CONINUOUS */
			if (_type == Type.DATE || _type == Type.DATETIME || _role == Role.DIMENSION) 
				_classification = Classification.Qi;
			else
				_classification = Classification.Qd;
		}
		
		// determine default structure
		if (isDate()) {
			_structureType = StructureType.HIERARCHICAL;
			_structure = new DateTimeStructure();
		} else {
			_structureType = StructureType.SIMPLE;
			_structure = null;
		}
	}
}
