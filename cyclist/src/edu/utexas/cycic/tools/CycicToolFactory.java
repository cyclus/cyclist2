package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class CycicToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return CycicTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return CycicTool.TYPE;
	}

	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return CycicTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new CycicTool();
	}
}