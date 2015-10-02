package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.presenter.ViewPresenter;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.FormBuilder;
import edu.utexas.cycic.presenter.FormBuilderPresenter;

public class FormBuilderTool implements Tool {

	public static final String ID 			= "edu.utexas.cycic.FormBuilderTool";
	public static final String TOOL_NAME 	= "Facility Form";
    public static final String TYPE			= ToolsLibrary.SCENARIO_TOOL;
	public static final AwesomeIcon ICON 	= AwesomeIcon.QUESTION_CIRCLE;
	
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
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public View getView() {
		if (_view == null) 
			_view = new FormBuilder();
		return _view;
	}

	@Override
	public ViewPresenter getPresenter(EventBus bus) {
		if (_presenter == null)
			_presenter = new FormBuilderPresenter(bus);
		return _presenter;
	}

}
