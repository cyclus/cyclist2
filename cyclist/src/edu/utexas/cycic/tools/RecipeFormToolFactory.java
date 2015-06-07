package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;

public class RecipeFormToolFactory implements ToolFactory {

	@Override
	public String getToolName() {
		return RecipeFormTool.TOOL_NAME;
	}
	
	@Override
	public String getToolType() {
		return RecipeFormTool.TYPE;
	}
	
	@Override
	public AwesomeIcon getIcon() {
		return RecipeFormTool.ICON;
	}
	
	@Override
	public Tool create() {
		return new RecipeFormTool();
	}



}