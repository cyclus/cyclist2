package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.presenter.ViewPresenter;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.SimulationInfo;
import edu.utexas.cycic.presenter.SimulationInfoPresenter;

public class SimulationInfoTool implements Tool {

	public static final String ID 			= "edu.utexas.cycic.SimulationInfoTool";
	public static final String TOOL_NAME 	= "CycIC - Commodities and Details";
    public static final String TYPE			= ToolsLibrary.SCENARIO_TOOL;
	public static final AwesomeIcon ICON 	= AwesomeIcon.EYE;
	
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
			_view = new SimulationInfo();
		return _view;
	}

	@Override
	public ViewPresenter getPresenter(EventBus bus) {
		if (_presenter == null)
			_presenter = new SimulationInfoPresenter(bus);
		return _presenter;
	}

}
