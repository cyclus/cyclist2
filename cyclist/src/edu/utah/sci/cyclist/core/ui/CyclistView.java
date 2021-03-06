package edu.utah.sci.cyclist.core.ui;

import javafx.collections.ObservableList;

import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.core.model.Filter;
import edu.utah.sci.cyclist.core.model.Resource;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;

public interface CyclistView extends View, Resource {
	void setOnTableDrop(Closure.V1<Table> action);
	void setOnTableRemoved(Closure.V1<Table> action);
	void setOnTableSelectedAction(Closure.V2<Table, Boolean> action);
	void setOnSelectAction(Closure.V0 action);
	void setOnShowFilter(Closure.V1<Filter> action);
	void setOnRemoveFilter(Closure.V1<Filter> action);
	
	void addTable(Table table, boolean remote, boolean active);
	void removeTable(Table table);
	void selectTable(Table table, boolean value);
	
	void addFilter(Filter filter);
	
	
	void setOnSimulationDrop(Closure.V1<Simulation> action);
	public Closure.V1<Simulation> getOnSimulationDrop();
	
	void addSimulation(Simulation simulation, boolean remote, boolean active);
	void removeSimulation(Simulation simulation);
	void selectSimulation(Simulation simulation, boolean value);
	
	void setOnSimulationSelectedAction(Closure.V2<Simulation, Boolean> action);
	void setOnSimulationRemoved(Closure.V1<Simulation> action);
	Closure.V1<Simulation> getOnSimulationRemoved();
	
	ObservableList<Filter> remoteFilters();
	ObservableList<Filter> filters();
}
