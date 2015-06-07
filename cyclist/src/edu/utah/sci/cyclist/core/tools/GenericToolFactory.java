package edu.utah.sci.cyclist.core.tools;

import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class GenericToolFactory<ToolImpl extends Tool> implements ToolFactory {

	private Class<ToolImpl> cls;
	private String name;
	private String type;
	private AwesomeIcon iconName;
	
	public GenericToolFactory(Class<ToolImpl> cls, String name, AwesomeIcon iconName) {
		this.cls = cls;
		this.name = name;
		this.iconName = iconName;
	}

	@Override
	public String getToolName() {
		return name;
	}

	@Override
	public String getToolType() {
		return type;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return iconName;
	}

	@Override
	public Tool create() throws InstantiationException, IllegalAccessException {
		return cls.newInstance();
	}

}
