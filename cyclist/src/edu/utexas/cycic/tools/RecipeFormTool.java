package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.presenter.ViewPresenter;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.RecipeForm;
import edu.utexas.cycic.presenter.RecipeFormPresenter;

public class RecipeFormTool implements Tool {

	public static final String ID 			= "edu.utexas.cycic.RecipeFormTool";
    public static final String TOOL_NAME    = "Recipe Builder";
    public static final String TYPE			= ToolsLibrary.SCENARIO_TOOL;
    public static final AwesomeIcon ICON    = AwesomeIcon.SORT_ALPHA_DESC;
	
	private View _view = null;
	private ViewPresenter _presenter = null;
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return TOOL_NAME;
	}

	@Override
	public View getView() {
		if (_view == null) 
			_view = new RecipeForm();
		return _view;
	}

	@Override
	public ViewPresenter getPresenter(EventBus bus) {
		if (_presenter == null)
			_presenter = new RecipeFormPresenter(bus);
		return _presenter;
	}

}
