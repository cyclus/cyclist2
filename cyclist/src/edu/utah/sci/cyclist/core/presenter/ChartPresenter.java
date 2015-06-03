package edu.utah.sci.cyclist.core.presenter;

import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.core.event.notification.CyclistNotification;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotificationHandler;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotifications;
import edu.utah.sci.cyclist.core.event.notification.CyclistTableNotification;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.ui.View;
import edu.utah.sci.cyclist.core.ui.views.ChartView;

public class ChartPresenter extends CyclistViewPresenter {

	public ChartPresenter(EventBus bus) {
		super(bus);
		SingleSelection<Table> selectionModelTbl = new SingleSelection<Table>();
		SingleSelection<Simulation> selectionModelSim = new SingleSelection<Simulation>();
		selectionModelTbl.setOnSelectItemAction(new Closure.V2<Table, Boolean>() {

			@Override
			public void call(Table table, Boolean value) {
				getView().selectTable(table, value);
				
			}
		
		});
		
		setSelectionModelTbl(selectionModelTbl);
		setSelectionModelSim(selectionModelSim);
		addNotificationHandlers();
	}
	
	
	@Override
	public ViewPresenter clone(View view) {
		ChartView chartView = (ChartView) view;
		ChartPresenter copy = new ChartPresenter(getEventBus());
		copy.setView(view);
		
		for (SelectionModel<Simulation>.Entry entry: getSimulationRecords()) {
			chartView.addSimulation(entry.item, entry.remote, entry.active);
			copy.getSelectionModelSim().addItem(entry.item, entry.remote, entry.active, entry.remoteActive);
		}
		chartView.selectSimulation(getView().getCurrentSimulation(), true);
		
		// copy tables
		for (SelectionModel<Table>.Entry entry : getSelectionModelTbl().getEntries()) {
			copy.addTable(entry.item, entry.remote, entry.active, entry.remoteActive);
		}
		
		chartView.copy(getView());
		chartView.setActive(true);
		return copy;
	}
	
	@Override 
	public void setActive(boolean active) {
		getView().setActive(active);
	}
	
	@Override
	public ChartView getView() {
		return (ChartView) super.getView();
	}
	
	@Override
	public void updateSimulationData(){
		getView().updateSimulationData();
	}
	
	@Override
	public void removeSimulationData(){
		getView().removeSimulationData();
	}
	
	/**
	 * @parm Table
	 */
	public void addTable(Table table) {
		getView().addTable(table, false, true);
	}
	
	/**
	 * setView
	 * @param view
	 */
	
	public void setView(View view) {
		super.setView(view);
		
		getView().setOnTableDrop(new Closure.V1<Table>() {
			
			@Override
			public void call(Table table) {
				addTable(table, false /* remote */, true /* active */, false /* remoteActive */);
			}
		});
		
		getView().setOnTableRemoved(new Closure.V1<Table>() {

			@Override
			public void call(Table table) {
				removeTable(table);			
			}
		});
		
		getView().setOnDuplicate(new Closure.V0() {
			@Override
			public void call() {
				broadcast(new CyclistNotification(CyclistNotifications.DUPLICATE_VIEW));
			}
		});
	}
	
	/**
	 * addNotficicationHandlers
	 */
	
	public void addNotificationHandlers() {
		addNotificationHandler(CyclistNotifications.DATASOURCE_ADD, new CyclistNotificationHandler() {
			
			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				
				addTable(notification.getTable(), true /*remote*/, false /* active */, false /* remoteActive */);			
			}
		});
		
		addNotificationHandler(CyclistNotifications.DATASOURCE_REMOVE, new CyclistNotificationHandler() {
			
			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				removeTable(notification.getTable());			
			}
		});
		
		addNotificationHandler(CyclistNotifications.DATASOURCE_SELECTED, new CyclistNotificationHandler() {
			
			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				getSelectionModelTbl().selectItem(notification.getTable(), true);
			}
		});
		
		addNotificationHandler(CyclistNotifications.DATASOURCE_UNSELECTED, new CyclistNotificationHandler() {
			
			@Override
			public void handle(CyclistNotification event) {
				CyclistTableNotification notification = (CyclistTableNotification) event;
				getSelectionModelTbl().selectItem(notification.getTable(), false);			
			}
		});
	}
	
}
