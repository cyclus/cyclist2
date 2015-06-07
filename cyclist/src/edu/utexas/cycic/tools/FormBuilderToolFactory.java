package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class FormBuilderToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return FormBuilderTool.TOOL_NAME;
	}

	@Override
	public String getToolType() {
		return FormBuilderTool.TYPE;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return FormBuilderTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new FormBuilderTool();
	}
}