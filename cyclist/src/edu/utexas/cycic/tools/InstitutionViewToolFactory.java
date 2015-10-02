package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class InstitutionViewToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return InstitutionViewTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return InstitutionViewTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return InstitutionViewTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new InstitutionViewTool();
	}



}