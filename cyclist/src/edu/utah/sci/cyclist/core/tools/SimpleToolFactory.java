package edu.utah.sci.cyclist.core.tools;

import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class SimpleToolFactory implements ToolFactory {

	private String _id;
	private String _name;
	private String _type;
	private boolean _userlevel;
	private AwesomeIcon _iconName;
	private String _viewClass;
	private String _presenterClass;
	
	public SimpleToolFactory(String pkg, String name, String type, Boolean userlevel, AwesomeIcon iconName, String viewClass, String presenterClass) {
		_id = pkg+"."+name;
		_name = name;
		_type = type;
		_userlevel = userlevel;
		_iconName = iconName;
		_viewClass = pkg+"."+viewClass;
		_presenterClass = pkg+"."+presenterClass;
	}
	
	@Override
	public String getToolName() {
		return _name;
	}

	@Override
	public String getToolType() {
		return _type;
	}
	
	@Override
	public boolean isUserLevel() {
		return _userlevel;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return _iconName;
	}

	@Override
	public Tool create() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return new GenericTool(_id, _name, _viewClass, _presenterClass);
	}

}
