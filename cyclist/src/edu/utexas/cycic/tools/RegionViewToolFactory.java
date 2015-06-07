package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class RegionViewToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return RegionViewTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return RegionViewTool.TYPE;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return RegionViewTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new RegionViewTool();
	}



}