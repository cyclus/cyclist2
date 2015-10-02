package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class InstitutionCorralViewToolFactory implements ToolFactory {
	@Override
	public String getToolName() {
		return InstitutionCorralViewTool.TOOL_NAME;
	}
	
	@Override
	public String getToolType() {
		return InstitutionCorralViewTool.TYPE;
	}
	
	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public Tool create() {
		return new InstitutionCorralViewTool();
	}

	@Override
	public AwesomeIcon getIcon() {
		return InstitutionCorralViewTool.ICON;
	}
}
