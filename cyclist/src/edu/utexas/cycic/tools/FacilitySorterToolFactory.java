package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class FacilitySorterToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return FacilitySorterTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return FacilitySorterTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return FacilitySorterTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new FacilitySorterTool();
	}



}