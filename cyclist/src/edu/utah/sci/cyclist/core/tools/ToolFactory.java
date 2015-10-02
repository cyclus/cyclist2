package edu.utah.sci.cyclist.core.tools;

import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public interface ToolFactory {
	String getToolName();
	String getToolType();
	boolean isUserLevel();
	AwesomeIcon getIcon();
	
	Tool create() throws InstantiationException, IllegalAccessException, ClassNotFoundException;
}
