package edu.utexas.cycic.tools;


import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class TimelineDisplayToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return TimelineDisplayTool.TOOL_NAME;
	}
	
	@Override
	public String getToolType() {
		return TimelineDisplayTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}

	@Override
	public AwesomeIcon getIcon() {
		return TimelineDisplayTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new TimelineDisplayTool();
	}



}