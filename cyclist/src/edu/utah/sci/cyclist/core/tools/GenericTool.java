package edu.utah.sci.cyclist.core.tools;

import java.lang.reflect.InvocationTargetException;

import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.presenter.ViewPresenter;
import edu.utah.sci.cyclist.core.ui.View;

public class GenericTool implements Tool {
	
	private String _id;
	private String _name;
	private String _viewClass;
	private String _presenterClass;
	
	private View _view = null;
	private ViewPresenter _presenter = null;
	
	public GenericTool(String id, String name, String viewClass, String presenterClass) {
		_id = id;
		_name = name;
		_viewClass = viewClass;
		_presenterClass = presenterClass;
	}
	
	@Override
	public String getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean isUserLevel() {
		return true;
	}
	
	@Override
	public View getView() {
		if (_view == null) {
			try {
				_view = (View)Class.forName(_viewClass).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return _view;
	}

	@Override
	public ViewPresenter getPresenter(EventBus bus) {
		if (_presenter == null) {
			try {
				_presenter = (ViewPresenter) Class.forName(_presenterClass).getConstructor(EventBus.class).newInstance(bus);;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return _presenter;
	}

}
