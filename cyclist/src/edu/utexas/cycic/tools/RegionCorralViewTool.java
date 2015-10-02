package edu.utexas.cycic.tools;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.presenter.ViewPresenter;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utexas.cycic.RegionCorralView;
import edu.utexas.cycic.presenter.RegionCorralViewPresenter;

public class RegionCorralViewTool implements Tool {

	public static final String ID 			= "edu.utexas.cycic.RegionCorralViewTool";
    public static final String TOOL_NAME    = "Region Corral";
    public static final String TYPE			= ToolsLibrary.SCENARIO_TOOL;
    public static final AwesomeIcon ICON    = AwesomeIcon.GLOBE;
	
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
			_view = new RegionCorralView();
		return _view;
	}

	@Override
	public ViewPresenter getPresenter(EventBus bus) {
		if (_presenter == null)
			_presenter = new RegionCorralViewPresenter(bus);
		return _presenter;
	}

}
