package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class SimulationInfoToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return SimulationInfoTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return SimulationInfoTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return SimulationInfoTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new SimulationInfoTool();
	}



}