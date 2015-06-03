/*******************************************************************************
 * Copyright (c) 2013 SCI Institute, University of Utah.
 * All rights reserved.
 *
 * License for the specific language governing rights and limitations under Permission
 * is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: The above copyright notice 
 * and this permission notice shall be included in all copies  or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Yarden Livnat  
 *******************************************************************************/
package edu.utah.sci.cyclist.core.presenter;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.event.notification.CyclistFilterNotification;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotification;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotificationHandler;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotifications;
import edu.utah.sci.cyclist.core.event.notification.CyclistSimulationNotification;
import edu.utah.sci.cyclist.core.event.notification.CyclistTableNotification;
import edu.utah.sci.cyclist.core.event.notification.CyclistViewNotification;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.event.notification.SimpleEventBus;
import edu.utah.sci.cyclist.core.event.notification.SimpleNotification;
import edu.utah.sci.cyclist.core.model.Context;
import edu.utah.sci.cyclist.core.model.Filter;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.ui.components.CyclistViewBase;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import edu.utah.sci.cyclist.core.ui.views.FilterPanel;
import edu.utah.sci.cyclist.core.ui.views.VisWorkspace;

public class VisWorkspacePresenter extends CyclistViewPresenter {

	private List<ViewPresenter> _presenters = new ArrayList<>();
	private List<FilterPresenter> _filterPresenters = new ArrayList<>();
	ObservableList<Tool> _tools = FXCollections.observableArrayList();
	private EventBus _localBus;

	public VisWorkspacePresenter(EventBus bus/*, Model model*/) {
		super(bus);
		build();
		_localBus = /*getWorkspace().isToplevel() ? getEventBus() : */ new SimpleEventBus();
		addListeners();
	}

	public VisWorkspace getWorkspace() {
		return (VisWorkspace) getView();
	}

	public void setView(View view) {
		super.setView(view);

		if (view instanceof VisWorkspace) {
			
			_tools.addListener(new ListChangeListener<Tool>(){
				@Override
				public void onChanged(ListChangeListener.Change<? extends Tool> newList) {
					setDirtyFlag(true);
				}
			});
			
			
			VisWorkspace workspace = getWorkspace();


			if (getWorkspace().isToplevel())
				addToplevelListeners();

			workspace.setOnToolDrop(new Closure.V3<Tool, Double, Double>() {

				@Override
				public void call(Tool tool, Double x, Double y) {
					addTool(tool, x, y);
				}
			});

			workspace.setOnTableDrop(new Closure.V1<Table>() {
				@Override
				public void call(Table table) {
					addTable(table, false /*remote*/, false /* active */, false /* remoteActive */);
					broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_ADD, table));
					getSelectionModelTbl().selectItem(table, true);
				}
			});

			workspace.setOnTableRemoved(new Closure.V1<Table>() {
				@Override
				public void call(Table table) {
					removeTable(table);
					broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_REMOVE, table));
				}
			});

			workspace.setOnShowTable(new Closure.V4<Tool, Table, Double, Double>() {

				@Override
				public void call(Tool tool, Table table, Double x, Double y) {
					TablePresenter presenter = (TablePresenter) addTool(tool, x, y);
					presenter.addTable(table, false /* remote */, true /* active */, false /* remoteActive */);
				}
			});

			workspace.filters().addListener(new ListChangeListener<Filter>() {

				@Override
				public void onChanged(ListChangeListener.Change<? extends Filter> change) {
					while (change.next()) {
						for (Filter filter : change.getRemoved()) {
							broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.REMOVE_REMOTE_FILTER, filter));
						}
						for (Filter filter : change.getAddedSubList()) {
							broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.ADD_REMOTE_FILTER, filter));
						}
					}
				}                                
			});

			workspace.remoteFilters().addListener(new ListChangeListener<Filter>() {

				@Override
				public void onChanged(ListChangeListener.Change<? extends Filter> change) {
					while (change.next()) {
						for (Filter filter : change.getRemoved()) {
							broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.REMOVE_REMOTE_FILTER, filter));
						}
						for (Filter filter : change.getAddedSubList()) {
							broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.ADD_REMOTE_FILTER, filter));
						}
					}
				}                                
			});

			final Closure.V1<Filter> onShowFunc = workspace.getOnShowFilter();
			workspace.setOnShowFilter(new Closure.V1<Filter>() {
				@Override
				public void call(Filter filter) {
					if(onShowFunc != null){
						onShowFunc.call(filter);
					}
					broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.SHOW_FILTER, filter));
				}
			});
			
			final Closure.V1<Filter> onRemoveFunc = workspace.getOnRemoveFilter();
			workspace.setOnRemoveFilter(new Closure.V1<Filter>() {
				@Override
				public void call(Filter filter) {
					if(onRemoveFunc != null){
						onRemoveFunc.call(filter);
					}
					broadcast(getLocalEventBus(), new CyclistFilterNotification(CyclistNotifications.REMOVE_FILTER, filter));
				}
			});

			workspace.setOnSimulationDrop(new Closure.V1<Simulation>() {

				@Override
				public void call(Simulation simulation) {
					addLocalSimulation(simulation,true);
					broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_ADD, simulation));
				}

			});

			workspace.setOnSimulationRemoved(new Closure.V1<Simulation>() {
				@Override
				public void call(Simulation simulation) {
					getSelectionModelSim().removeItem(simulation);
					broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_REMOVE, simulation));
				}
			});

		}
	}

	/*
	 * addTool
	 */
	public Presenter addTool(Tool tool) {
		return addTool(tool, 100, 100);
	}
	
	@Override
	public void save(IMemento memento) {
		super.save(memento);
		for (Tool tool : _tools){
			IMemento toolMemento = memento.createChild("Tool");
			toolMemento.putString("name", tool.getName());
			toolMemento.putString("id", tool.getId());
			toolMemento.putString("x", Double.toString(((Node)tool.getView()).getLayoutX()));
			toolMemento.putString("y", Double.toString(((Node)tool.getView()).getLayoutY()));
			toolMemento.putString("width", Double.toString(((Region)tool.getView()).getPrefWidth()));
			toolMemento.putString("height", Double.toString(((Region)tool.getView()).getPrefHeight()));
			tool.getPresenter(_localBus).save(toolMemento);
			//Reset the dirty flag after save.
			setDirtyFlag(false);
		}
	}
	
	@Override
	public void restore(IMemento memento, Context ctx) {	
		super.restore(memento, ctx);
		if(memento != null){
			IMemento[] tools = memento.getChildren("Tool");
			for(IMemento toolMemento : tools)
			{
				//Get the location
				Double x = Double.parseDouble(toolMemento.getString("x"));
				Double y = Double.parseDouble(toolMemento.getString("y"));
				
				double width = Double.parseDouble(toolMemento.getString("width"));
				double height = Double.parseDouble(toolMemento.getString("height"));
				
				String toolName = toolMemento.getString("name");
				
				try {
					Tool tool = ToolsLibrary.createTool(toolName);
					addTool(tool,x,y);
					((Region)tool.getView()).setPrefSize(width, height);
					tool.getPresenter(_localBus).restore(toolMemento, ctx);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		//Reset the dirty flag after restore.
		setDirtyFlag(false);
	}
	
	/*
	 * Clears all the existing views and filters of the workspace.
	 * (For example when changing between workspaces).
	 */
	public void clearWorkspace(){
		removeOldViews();
		removeOldFilters();
	}
	
	/** Handles the special case when the first simulation is selected in the simulation panel.
	 * Add the simulation automatically to the workspace bar, and make it the current selected simulation.
	 * Also broadcast the selected simulation to all the views under the workSpace.
	 * @param Simulation sim - the selected simulation.
	 **/
	public void addFirstSelectedSimulation(Simulation sim,boolean select){
		addLocalSimulation(sim,select);
		broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_ADD, sim));
	}
	
	/**
	 * Removes the simulation from the tool bar and also broadcast it to all the views under the workspace.
	 * @param 
	 */
	@Override
	public void removeSimulation(Simulation sim){
		super.removeSimulation(sim);
		broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_REMOVE, sim));
	}

	private Presenter addTool(Tool tool, double x, double y) {
		ViewBase view = (ViewBase) tool.getView();
		ViewPresenter presenter = tool.getPresenter(getLocalEventBus());
		
		_tools.add(tool);
		
		return addTool(view, presenter, x, y);
	}
	
	private Presenter addTool(ViewBase view, ViewPresenter presenter, double x, double y) {
		view.setLayoutX(x);
		view.setLayoutY(y);
		getWorkspace().addView(view);

		if (presenter != null) {        
			_presenters.add(presenter);
			presenter.setView(view);   
			if (presenter instanceof CyclistViewPresenter) {
				CyclistViewPresenter p = (CyclistViewPresenter) presenter;
				p.setRemoteTables(getTableRecords());
				p.setRemoteSimulations(getSimulationRecords());
				p.addRemoteFilters(getWorkspace().filters());
				p.addRemoteFilters(getWorkspace().remoteFilters());
			}
		}
	
		return presenter;
	}

	private void build() {

		SelectionModel<Table> selectionModelTbl = new SingleSelection<Table>();
		SelectionModel<Simulation> selectionModelSim = new SingleSelection<Simulation>();
		selectionModelTbl.setOnSelectItemAction(new Closure.V2<Table, Boolean>() {

			@Override
			public void call(Table table, Boolean activate) {
				getView().selectTable(table, activate);        
				String msg = activate ? CyclistNotifications.DATASOURCE_SELECTED : CyclistNotifications.DATASOURCE_UNSELECTED;
				broadcast(getLocalEventBus(), new CyclistTableNotification(msg, table));
			}

		});

		selectionModelSim.setOnSelectItemAction(new Closure.V2<Simulation, Boolean>() {

			@Override
			public void call(Simulation simulation, Boolean activate) {  
				getView().selectSimulation(simulation, activate);
				String msg = activate ? CyclistNotifications.SIMULATION_SELECTED : CyclistNotifications.SIMULATION_UNSELECTED;
				broadcast(getLocalEventBus(), new CyclistSimulationNotification(msg, simulation));
			}

		});

		setSelectionModelTbl(selectionModelTbl);
		setSelectionModelSim(selectionModelSim);
	}



	/*
	 * addListeners
	 */
	 private void addListeners() {

		// local notifications
		addLocalNotificationHandler(CyclistNotifications.REMOVE_VIEW, new CyclistNotificationHandler() {        
			@Override
			public void handle(CyclistNotification event) {
				String id = ((SimpleNotification)event).getMsg();
				removeView(id);
			}
		});

		addLocalNotificationHandler(CyclistNotifications.VIEW_SELECTED, new CyclistNotificationHandler() {
			@Override
			public void handle(CyclistNotification event) {
				View view = ((CyclistViewNotification)event).getView();
				getWorkspace().selectView(view);
				if (view instanceof CyclistViewBase) {
					CyclistViewBase base = (CyclistViewBase) view;
					List<Filter> f1 = base.filters();
					List<Filter> f2 = base.remoteFilters();
					for (FilterPresenter p : _filterPresenters) {
						Filter f = p.getFilter();
						p.highlight(f1.contains(f) || f2.contains(f));
					}
				}
			}
		});

		addLocalNotificationHandler(CyclistNotifications.DUPLICATE_VIEW, new CyclistNotificationHandler() {
			@Override
			public void handle(CyclistNotification event) {
				Presenter presenter = (Presenter) event.getSource();
				if (presenter instanceof ChartPresenter) {
					duplicateView((ChartPresenter) presenter);
				}
			}

		});

		addLocalNotificationHandler(CyclistNotifications.SHOW_FILTER, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				Filter filter = ((CyclistFilterNotification)event).getFilter();

				FilterPresenter presenter = getFilterPresenter(filter);
				if (presenter == null) {
					FilterPanel panel = new FilterPanel(filter);
					presenter = new FilterPresenter(getLocalEventBus());
					presenter.setPanel(panel);
					getWorkspace().addPanel(panel);
					_filterPresenters.add(presenter);
				} else {
					getWorkspace().showPanel(presenter.getPanel(), true);
				}
			}
		});

		addLocalNotificationHandler(CyclistNotifications.HIDE_FILTER, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				Filter filter = ((CyclistFilterNotification)event).getFilter();
				FilterPresenter presenter = getFilterPresenter(filter);
				if (presenter != null) {
					getWorkspace().showPanel(presenter.getPanel(), false);
				}
			}
		});

		addLocalNotificationHandler(CyclistNotifications.REMOVE_FILTER, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				Filter filter = ((CyclistFilterNotification)event).getFilter();
				removeFilterFromPanel(filter);
			}
		});
	
		// parent notifications
		addNotificationHandler(CyclistNotifications.REMOVE_REMOTE_FILTER, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				broadcast(getLocalEventBus(), event);
			}
		});

		addNotificationHandler(CyclistNotifications.ADD_REMOTE_FILTER, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				broadcast(getLocalEventBus(), event);
			}
		});

		// Handlers for an internal workspace.
		addNotificationHandler(CyclistNotifications.DATASOURCE_ADD, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;

				addTable(notification.getTable(), true /*remote*/, false /* active */, false /* remoteActive */);
				broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_ADD, notification.getTable()));
			}
		});

		addNotificationHandler(CyclistNotifications.DATASOURCE_REMOVE, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				removeTable(notification.getTable());
				broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_REMOVE, notification.getTable()));
			}
		});

		addNotificationHandler(CyclistNotifications.DATASOURCE_SELECTED, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				getSelectionModelTbl().selectItem(notification.getTable(), true);
				if(!getSelectionModelTbl().isRemoteActive(notification.getTable())){
					broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_SELECTED, notification.getTable()));
				}
			}
		});

		addNotificationHandler(CyclistNotifications.DATASOURCE_UNSELECTED, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				getSelectionModelTbl().selectItem(notification.getTable(), false);
				broadcast(getLocalEventBus(), new CyclistTableNotification(CyclistNotifications.DATASOURCE_UNSELECTED, notification.getTable()));
			}
		});
		addNotificationHandler(CyclistNotifications.SIMULATION_ADD, new CyclistNotificationHandler() {
			@Override
			public void handle(CyclistNotification event) {
				CyclistSimulationNotification notification = (CyclistSimulationNotification) event;
				broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_ADD, notification.getSimulation()));
			}
		});

		addNotificationHandler(CyclistNotifications.SIMULATION_REMOVE, new CyclistNotificationHandler() {

			@Override
			public void handle(CyclistNotification event) {
				CyclistSimulationNotification notification = (CyclistSimulationNotification) event;
				removeSimulation(notification.getSimulation());
				broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_REMOVE, notification.getSimulation()));
			}
		});

		addNotificationHandler(CyclistNotifications.SIMULATION_SELECTED, new CyclistNotificationHandler() {
			@Override
			public void handle(CyclistNotification event) {
				CyclistSimulationNotification notification = (CyclistSimulationNotification) event;
				if(!getSelectionModelSim().isRemoteActive(notification.getSimulation())){
					broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_SELECTED, notification.getSimulation()));
				}
			}
		});

		addNotificationHandler(CyclistNotifications.SIMULATION_UNSELECTED, new CyclistNotificationHandler() {
			@Override
			public void handle(CyclistNotification event) {
				CyclistSimulationNotification notification = (CyclistSimulationNotification) event;
				broadcast(getLocalEventBus(), new CyclistSimulationNotification(CyclistNotifications.SIMULATION_UNSELECTED, notification.getSimulation()));
			}
		});

	 }

	 private void duplicateView(ViewPresenter presenter) {
		 ViewBase view = (ViewBase) presenter.getView();

		 ViewBase newView = view.clone();
		 newView.setLayoutX( view.getLayoutX()+10);
		 newView.setLayoutY( view.getLayoutY()+10);
		 getWorkspace().addView(newView);

		 ViewPresenter newPresenter = presenter.clone(newView);
		 _presenters.add(newPresenter);
	 }

	 private void addToplevelListeners() {
		 addLocalNotificationHandler(CyclistNotifications.DATASOURCE_FOCUS, new CyclistNotificationHandler() {

			 @Override
			 public void handle(CyclistNotification event) {
				 broadcast(event);
			 }
		 });
	 }

	 @Override
	 public void onViewSelected(View view) {
		 super.onViewSelected(view);
		 if (getWorkspace().isToplevel())
			 broadcast(getLocalEventBus(), (new CyclistViewNotification(CyclistNotifications.VIEW_SELECTED, view)));
	 }

	 private FilterPresenter getFilterPresenter(Filter filter) {
		 for (FilterPresenter p : _filterPresenters) {
			 if (p.getFilter() == filter) 
				 return p;
		 }
		 return null;
	 }

	 private EventBus getLocalEventBus() {
		 return _localBus;
	 }
	 
	 /*
	  * Removes a tool from the tools list according to its presenter id.
	  * @parameter: String presenterId. 
	  */
	 private void removeTool(String presenterId){
		 for(Tool tool : _tools){
			 if(tool.getPresenter(_localBus).getId().equals(presenterId)){
				 _tools.remove(tool);
				 break;
				 
			 }
		 }
	 }
	 
	 /* 
	  * Removes the panel of a specified filter from the filters panel.
	  * @param Filter filter - the filter to be removed.
	  */
	 private void removeFilterFromPanel(Filter filter){
		FilterPresenter presenter = getFilterPresenter(filter);
		if (presenter != null) {
			_filterPresenters.remove(presenter);
			getWorkspace().removePanel(presenter.getPanel());
		}
	 }
	 
	 /*
	  * Iterates the local filters of the given view, 
	  * for each of them - remove from the filters panel.
	  * @param ViewBase view - the view which holds the list of filters to remove.
	  */
	 private void removeViewFilters(ViewBase view){
		if(view instanceof CyclistViewBase){
			CyclistViewBase cyclistView = (CyclistViewBase)view;
			List<Filter> filters = cyclistView.filters();
			for(Filter filter:filters){
				removeFilterFromPanel(filter);
			}
		}
	 }
	 
	 /*
	  * Removes a view from the workspace.
	  * @param: String id - the id of the view to be removed.
	  */
	 private void removeView(String id){
		 for (ViewPresenter presenter : _presenters) {
				if (presenter.getId().equals(id)) {
					_presenters.remove(presenter);
					ViewBase view = (ViewBase)presenter.getView();
					getWorkspace().removeView(view);
					removePresenterIdFromEventBus(id);
					removeViewFilters(view);
					break;
				}
		}
		removeTool(id);
	 }
	 
	 /*
	  * Clears old views before restoring the new ones.
	  * (For example when changing the work directory - 
	  * have to clear the old work directory tools, before loading the tools of the current one.
	  */
	 private void removeOldViews(){
		 List<ViewPresenter> presenters = new ArrayList<>(_presenters);
		 for (ViewPresenter presenter : presenters) {
			 removeView(presenter.getId());
			 //If the presenter itself is a workspace - clear it's contents as well.
			 if(presenter instanceof VisWorkspacePresenter){
				 ((VisWorkspacePresenter) presenter).clearWorkspace();
			 }
		 }
	 }
	 
	 /*
	  * Clears old filters before restoring the new ones.
	  * (For example when changing the work directory - 
	  * have to clear the old work directory filters, before loading the filters of the current one.
	  */
	 private void removeOldFilters(){
		 List<Filter> filters = getView().filters();
		 for(Filter filter: filters){
			 removeFilterFromPanel(filter);
		 }
		 filters.clear();
		 
	 }

	 /* 
	  * Calls the local event bus to remove all the handlers of the specified target.
	  * @param String target - the target id to be removed.
	  */
	  private void removePresenterIdFromEventBus(String target){
		 _localBus.removeAllTargetHandlers(target);
	 }

	 public void addLocalNotificationHandler(String type, CyclistNotificationHandler handler) {
		 _localBus.addHandler(type, getId(), handler);
	 }
	 
	 /*
	 * Returns the dirty flag - which signals whether or not there were changes in the view.
	 * @return Boolean - the flag value.
	 */
	 @Override
	 public Boolean getDirtyFlag(){
		if(super.getDirtyFlag()){
			return true;
		}
		for(Tool tool: _tools){
			if(tool.getPresenter(_localBus).getDirtyFlag()){
				return true;
			}
		}
		return false;
	}

}