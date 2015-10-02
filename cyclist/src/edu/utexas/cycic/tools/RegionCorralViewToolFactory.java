package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class RegionCorralViewToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return RegionCorralViewTool.TOOL_NAME;
	}
	
	@Override
	public String getToolType() {
		return RegionCorralViewTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public Tool create() {
		return new RegionCorralViewTool();
	}

	@Override
	public AwesomeIcon getIcon() {
		return RegionCorralViewTool.ICON;
	}
}