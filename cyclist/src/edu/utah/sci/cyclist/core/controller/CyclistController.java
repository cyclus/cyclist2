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
package edu.utah.sci.cyclist.core.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.log4j.Logger;
import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.model.Context;
import edu.utah.sci.cyclist.core.model.CyclistDatasource;
import edu.utah.sci.cyclist.core.model.Field;
import edu.utah.sci.cyclist.core.model.Model;
import edu.utah.sci.cyclist.core.model.Perspective;
import edu.utah.sci.cyclist.core.model.Preferences;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.presenter.DatasourcesPresenter;
import edu.utah.sci.cyclist.core.presenter.InputPresenter;
import edu.utah.sci.cyclist.core.presenter.SchemaPresenter;
import edu.utah.sci.cyclist.core.presenter.SimulationPresenter;
import edu.utah.sci.cyclist.core.presenter.ToolsPresenter;
import edu.utah.sci.cyclist.core.presenter.VisWorkspacePresenter;
import edu.utah.sci.cyclist.core.services.CyclusService;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.ui.MainScreen;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import edu.utah.sci.cyclist.core.ui.panels.JobsPanel;
import edu.utah.sci.cyclist.core.ui.views.VisWorkspace;
import edu.utah.sci.cyclist.core.ui.wizards.DatatableWizard;
import edu.utah.sci.cyclist.core.ui.wizards.PreferencesWizard;
import edu.utah.sci.cyclist.core.ui.wizards.SaveWsWizard;
import edu.utah.sci.cyclist.core.ui.wizards.SimulationWizard;
import edu.utah.sci.cyclist.core.ui.wizards.SqliteLoaderWizard;
import edu.utah.sci.cyclist.core.util.LoadSqlite;


public class CyclistController {
	
	Logger log = Logger.getLogger(CyclistController.class);

	private final EventBus _eventBus;
	private MainScreen _screen;
	public static VisWorkspacePresenter _presenter;
	private Model _model = new Model();
	private String SAVE_FILE = "workspace-config.xml";
	private SessionController _sessionController;
	private Boolean _dirtyFlag = false;
    public static CyclusService _cyclusService;
	
	private static final String SIMULATIONS_TABLES_FILE = "SimulationTablesDef.xml";
	
	private Perspective _perspectives[] = {
			new Perspective(0, "Scenario Builder", ToolsLibrary.SCENARIO_TOOL, Arrays.asList("Builder", "Jobs")),
			new Perspective(1, "Data Exploration", ToolsLibrary.VIS_TOOL, Arrays.asList("Simulations", "Tables", "Fields", "Filters", "Jobs"))
	};
	private Perspective _currentPerspective = null;
	
	/**
	 * Constructor
	 * 
	 * @param eventBus
	 */
	public CyclistController(EventBus eventBus) {
		this._eventBus = eventBus;
		_cyclusService = new CyclusService();
		_cyclusService.jobs().addListener(new InvalidationListener() {	
			@Override
			public void invalidated(Observable observable) {
				_dirtyFlag = true;		
			}
		});
		
		// TODO: fix this hack. We need a better way of passing around the list of datasources
		LoadSqlite.setSources(_model.getSources());
		
		_sessionController = new SessionController();
		
		// If the save directory does not exist, create it
		File saveDir = new File(SessionController.CYCLIST_DIR);
		if (!saveDir.exists())	
			saveDir.mkdir();  
	
		File defaultWs = new File(SessionController.DEFAULT_WORKSPACE);
		if (!defaultWs.exists())	
			defaultWs.mkdir();
		
		if (_sessionController.initGeneralConfigFile())
		{
			_sessionController.restoreGeneralConfigFile();
		}
	}

	/**
	 * initialize the main screen
	 * @param screen
	 */
	public void setScreen(final MainScreen screen) {
		this._screen = screen;
		addActions();
			
		/*
		 *  wire panels
		 */
		
		// Tables panel
		DatasourcesPresenter ds = new DatasourcesPresenter(_eventBus);
		ds.setSources(_model.getSources());
		ds.setTables(_model.getTables());
		ds.setPanel(screen.getDatasourcesPanel());
		
		screen.getDatasourcesPanel().setTableActions(Arrays.asList("Plot"), 
			action -> { 
				try {
					_presenter
						.addTool(ToolsLibrary.getFactory(action.v1).create())
						.addTable(action.v2);
				} catch (Exception e) {
					log.error("internal error: ",e);
				}	
			});
		
		// Schema panel
		SchemaPresenter sp = new SchemaPresenter(_eventBus);
		sp.setPanel(screen.getFieldsPanel());
		
		//Simulation panel
		SimulationPresenter sip = new SimulationPresenter(_eventBus);
		sip.setSimIds(_model.getSimulations());
		sip.setSimPanel(screen.getSimulationPanel());
		
		// Jobs panel
		JobsPanel jobsPanel = screen.getJobsPanel();
		jobsPanel.jobsProperty().bindBidirectional(_cyclusService.jobs());
		jobsPanel.setOnLoadSimulations(new Closure.V1<List<Simulation>>() {
			@Override
            public void call(List<Simulation> list) {
				_model.getSimulations().addAll(list);	            
            }	
		});
		
		
        // ToolsLibrary panel
        ToolsPresenter tp = new ToolsPresenter(_eventBus);
        tp.setPanel(screen.getToolsPanel());
        tp.setFactories(ToolsLibrary.getFactoriesOfType(ToolsLibrary.VIS_TOOL, true));
       
        // InputLibrary panel
        InputPresenter ip = new InputPresenter(_eventBus);
        ip.setPanel(screen.getInputPanel());
        ip.setFactories(ToolsLibrary.getFactoriesOfType(ToolsLibrary.SCENARIO_TOOL, true));
        
        // Builder perspectives
       
		for (Perspective p : _perspectives) {
			ViewBase workspace = new VisWorkspace(true);
			p.presenter = new VisWorkspacePresenter(_eventBus);
			p.presenter.setView(workspace);

			Tab tab = new Tab();
			tab.setText(p.name);
			tab.setClosable(false);
			tab.setContent(workspace);
			screen.getTabPane().getTabs().add(tab);
			
//			
		}
		
		screen.getTabPane().getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> o, Number prev, Number id) {
				perspectiveChanged(id.intValue());

			}
		});
		
		restore();

		selectPerspective(_currentPerspective.id);   
		if (_currentPerspective.id == 0)
			perspectiveChanged(_currentPerspective.id);  
	}
	
	private void selectPerspective(int id) {
		_screen.getTabPane().getSelectionModel().select(id);
	}
	
	private void perspectiveChanged(int id) {
		if (_currentPerspective != null && _currentPerspective.id != id) {
			_currentPerspective.setToolsPositions(_screen.getToolsPositions());
			_presenter.getWorkspace().getConsole().setActive(false);
		}
		
		_currentPerspective = _perspectives[id];
		if (!_currentPerspective.initialized) {
			_currentPerspective.init();
			if (id == 0) {
				try {
					if (!_perspectives[0].presenter.hasTool("Cycic")) {
						ToolFactory factory = ToolsLibrary.getFactory("Cycic");
						_perspectives[0].presenter.addTool(factory.create(), 0, 0);	 
					}
				} catch (Exception e) {
					log.error("Can create scenario builder");
				}
			}
		}
		_presenter = _currentPerspective.presenter;
		_presenter.getWorkspace().getConsole().setActive(true);
		_screen.showPanels(_currentPerspective.tools, _currentPerspective.toolsPositions);
		_screen.selectTools( _currentPerspective.type);
	}
	
	/**
	 * selectWorkspace
	 * 
	 */
	public void selectWorkspace() {
		
		if(_sessionController == null){
			return;
		}
		
		ObservableList<String> selection = _screen.selectWorkspace(_sessionController.getWorkDirectories(),
																   _sessionController.getLastChosenIndex());
		
		selection.addListener(new ListChangeListener<String>(){

			@Override
			public void onChanged(Change<? extends String> list ){
				if(_sessionController != null){
					if(_sessionController.handleWorkDirectoriesListChangedEvent(list)){
						restore();
						
						//Set all the views to match the new tables.
						ObservableList<Field> emptyList = FXCollections.observableArrayList();
						_screen.getFieldsPanel().setFields(emptyList);
					}
				}
				
			}
		});
		
	}
		
	private void addActions() {
		
		_screen.onAddDatasource().set(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				final DatatableWizard wizard = new DatatableWizard();
				wizard.setItems(_model.getSources());
				if(_model.getSelectedDatasource() != null){
					wizard.setSelectedSource(_model.getSelectedDatasource());
				}
				String currDirectory = getLastChosenWorkDirectory();
				wizard.setWorkDir(currDirectory);
				ObjectProperty<Table> selection = wizard.show(_screen.getWindow());
				
			//	wizard.getDataSources()
				
				selection.addListener(new ChangeListener<Table>() {
					@Override
					public void changed(ObservableValue<? extends Table> arg0, Table oldVal, Table newVal) {
						if(newVal != null)
						{
							Table tbl = new Table(newVal);
							_model.getTables().add(tbl);
							_model.setSelectedDatasource(wizard.getSelectedSource());
							_dirtyFlag = true;
						}
					}
				});
				
			}
		});
		
		_screen.onAddSimulation().set(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final SimulationWizard wizard = new SimulationWizard();
				
				wizard.setItems(_model.getSources(), _model.getSimAliases().keySet());
				if(_model.getSelectedDatasource() != null){
					wizard.setSelectedSource(_model.getSelectedDatasource());
				}
				String currDirectory = getLastChosenWorkDirectory();
				wizard.setWorkDir(currDirectory);
				ObservableList<Simulation> selection = wizard.show(_screen.getWindow());
				
				
				selection.addListener(new ListChangeListener<Simulation>() {
					@Override
					public void onChanged(ListChangeListener.Change<? extends Simulation> newList) {
						if(newList != null)
						{
							List<Simulation> newSimulations = new ArrayList<>();
							for(Simulation simulation:newList.getList()){
								Simulation sim = simulation.clone();
								Simulation existingSim = _model.simExists(simulation);
								if(existingSim==null){
									newSimulations.add(sim);
									_dirtyFlag = true;
								}else if(!existingSim.getAlias().equals(simulation.getAlias())){
									existingSim.setAlias(simulation.getAlias());
									_model.addNewSimALias(existingSim, "");
									_dirtyFlag = true;
								}
							}
							if(newSimulations.size()>0){
								_model.getSimulations().addAll(newSimulations);
							}
							_model.setSelectedDatasource(wizard.getSelectedSource());
							
						}
					}
				});
			}
		});
		
		_screen.onLoadSqlite().set(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final SqliteLoaderWizard wizard = new SqliteLoaderWizard(_model.getSources());
				
				ObservableList<Simulation> selection = wizard.show(_screen.getWindow());
				
				selection.addListener(new ListChangeListener<Simulation>() {
					@Override
					public void onChanged(ListChangeListener.Change<? extends Simulation> newList) {
						if(newList != null)
						{
							//Add also the datasource of the simulations to the data sources list, if not exists.
							Simulation firstSim = newList.getList().get(0);
							CyclistDatasource ds = firstSim.getDataSource();
							if(!_model.dataSourceExists(ds)){
								_model.getSources().add(ds);
								_dirtyFlag = true;
							}
							
							List<Simulation> newSimulations = new ArrayList<Simulation>();
							for(Simulation simulation:newList.getList()){
								Simulation sim = simulation.clone();
								Simulation existingSim = _model.simExists(simulation);
								if(existingSim == null){
									newSimulations.add(sim);
									_dirtyFlag = true;
								}else if(!existingSim.getAlias().equals(simulation.getAlias())){
									existingSim.setAlias(simulation.getAlias());
									_model.addNewSimALias(existingSim, "");
									_dirtyFlag = true;
								}
							}
							if(newSimulations.size()>0){
								_model.getSimulations().addAll(newSimulations);
							}
							_model.setSelectedDatasource(ds);
						}
					}
				});
				
				wizard.runOperations();
			}
		});
		
//		_screen.onRun().set(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				String server = "";
//				if(event.getSource() instanceof MenuItem){
//					server = (String) ((MenuItem)event.getSource()).getUserData();
//					if(server.isEmpty()){
//						server = Preferences.getInstance().getDefaultServer();
//					}
//				}
//				FileChooser chooser = new FileChooser();
//				chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("Cyclus files (*.xml)", "*.xml") );
//				File file = chooser.showOpenDialog(Cyclist.cyclistStage);
//				if (file != null) {
//					_cyclusService.submit(file, server);
//				}
//			}
//		});
		
//		_screen.onManage().set(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				ManageRemoteServersWizard wizard = new ManageRemoteServersWizard(_model.getRemoteServersList());
//				ObservableList<String> deletedServers = wizard.show(_screen.getParent().getScene().getWindow());
//				deletedServers.addListener(new ListChangeListener<String>() {
//					@Override
//					public void onChanged(ListChangeListener.Change<? extends String> listChange) {
//						List<MenuItem> items = new ArrayList<MenuItem>();
//						for(String server:listChange.getList()){
//							_model.getRemoteServersList().remove(server);
//							for(MenuItem item : _screen.getRemoteServers()){
//								if(item.getText().equals(server)){
//									items.add(item);
//								}
//							}
//						}
//						for(MenuItem item : items){
//							_screen.getRemoteServers().remove(item);
//						}
//					}
//				});
//				
//			}
//		});
		
//		_screen.onRunOnOther().set(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				RemoteServerWizard wizard = new RemoteServerWizard();
//				ObjectProperty<String> selection = wizard.show(_screen.getParent().getScene().getWindow());
//				selection.addListener(new ChangeListener<String>(){
//					@Override
//					public void changed(ObservableValue<? extends String> arg0, String oldVal,String newVal) {
//						if(!newVal.isEmpty()){
//							if(_model.addNewRemoteServer(newVal)){
//								MenuItem item = new MenuItem(newVal);
//								_screen.getRemoteServers().add(item);
//								_dirtyFlag = true;
//								item.fire();
//							}else{
//								//Item already exists
//								for(MenuItem menuItem :_screen.getRemoteServers()){
//									if(menuItem.getText().equals(newVal)){
//										menuItem.fire();
//										break;
//									}
//								}
//							}
//							
//						}
//					}
//				});
//			}
//		});
		
		_screen.onSelectWorkspace().set(new EventHandler<ActionEvent>(){

			@Override
			public void handle(ActionEvent arg0) {
				
				//If need to save the current workspace data - ask the user whether to save it or not
				boolean dirty = _dirtyFlag;
				for (Perspective p : _perspectives) {
					dirty |= p.presenter.getDirtyFlag();
				}
				if (dirty) {
					SaveWsWizard wizard = new SaveWsWizard();
					ObjectProperty<Boolean> selection = wizard.show(_screen.getParent().getScene().getWindow());
					selection.addListener(new ChangeListener<Boolean>(){
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal,Boolean newVal) {
							if(newVal){
								save();
							}
							selectWorkspace();
						}
					});
				}else{
					selectWorkspace();
				}
			}
		});
		
		_screen.onSave().set(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				save();
			}
		});
		
		_screen.onSaveAs().set(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
//				FileChooser chooser = new FileChooser();
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setInitialDirectory(new File(getLastChosenWorkDirectory()+"/.."));
				File dir = new File(getLastChosenWorkDirectory());
				String parent="";
				if(!dir.exists()){
					parent = SessionController.DEFAULT_WORKSPACE;
				}else{
					parent = dir.getParent();
				}
				chooser.setInitialDirectory(new File(parent));
//				chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("directories", "*") );
//				File file = chooser.showSaveDialog(Cyclist.cyclistStage);
				File file = chooser.showDialog(Cyclist.cyclistStage);
				if (file != null) {
					saveAs(file.getAbsolutePath());
					//Display the new directory in the work directories dialog.
					_sessionController.addNewDir(file.getAbsolutePath());
				}
			}
		});
		
		_screen.onSetPreferences().set(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				final PreferencesWizard wizard = new PreferencesWizard();
				ObjectProperty<Boolean> selection = wizard.show(_screen.getWindow());
				selection.addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
							_dirtyFlag = true;
					}
				});
			}
		});
		
		_screen.onQuit().set(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				quit();
			}
		});
		
		_screen.editDataSourceProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
				if(newVal){
					_dirtyFlag = true;
					_screen.editDataSourceProperty().setValue(false);
				}
			}
		});
		
		_screen.editSimulationProperty().addListener(new ChangeListener<Simulation>() {
			@Override
			public void changed(ObservableValue<? extends Simulation> arg0, Simulation oldVal, Simulation newVal) {
				if(newVal != oldVal && newVal != null){
					if(newVal.getSimulationId() != null){
						_model.addNewSimALias(newVal, "");
					}
					_dirtyFlag = true;
					_screen.editSimulationProperty().setValue(null);
				}
			}
		});
		
		_screen.selectSimulationProperty().addListener(new ChangeListener<Simulation>() {
			@Override
			public void changed(ObservableValue<? extends Simulation> arg0, Simulation oldVal, Simulation newVal) {
				
				//Meanwhile - all the tables are the same for all the simulation - so load them once,
				//when the first simulation is selected. In the future - can compare between old and new simulations
				//and each time a different simulation is selected - change the tables accordingly.
				
				if(oldVal==null && newVal!=null){
//					readSimulationsTables();
//					_presenter.addFirstSelectedSimulation(newVal);
				}
				_model.setLastSelectedSimulation(newVal);
				_dirtyFlag = true;
			}
		});
		
		_screen.onSystemClose().set(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				event.consume();
				quit();
			}
		});
		
		EventHandler<ActionEvent> viewAction = new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent event) {
				MenuItem item = (MenuItem) event.getSource();
				try {
					ToolFactory factory = ToolsLibrary.getFactory(item.getText());
					_presenter.addTool(factory.create());
				} catch (Exception e) {
					log.error("Internal error: Can't find tool "+item.getText());
				}
			}
		};
		
		for (MenuItem item : _screen.getToolsMenu().getItems()) {
			item.setOnAction(viewAction);
		}
		
		_model.getSimulations().addListener(new ListChangeListener<Simulation>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Simulation> listChange) {
				if (_presenter instanceof VisWorkspacePresenter) {
					VisWorkspacePresenter p = (VisWorkspacePresenter) _presenter;
	
					while (listChange.next()) {
						for(Simulation sim : listChange.getRemoved()){
							p.removeSimulation(sim);
							_model.markSimALiasAsRemoved(sim);
						}
						boolean select = true;
						for(Simulation sim : listChange.getAddedSubList()){
							_model.addNewSimALias(sim,"");
							//If there are a few simulations added at the same time - select only the first.
							p.addFirstSelectedSimulation(sim,select);
							if(select){
								select = false;
							}
						}
					}
				}
			}
		});
		
	}
	
		
	private void quit() {
		_currentPerspective.setToolsPositions(_screen.getToolsPositions());
		
		boolean dirty = _dirtyFlag || Preferences.getInstance().isDirty();
		for (Perspective p : _perspectives) {
			dirty |= p.presenter.getDirtyFlag();
		}
		
		if(dirty){
			SaveWsWizard wizard = new SaveWsWizard();
			ObjectProperty<Boolean> selection = wizard.show(_screen.getParent().getScene().getWindow());
			selection.addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal,Boolean newVal) {
					if(newVal){
						save();
					}
					System.exit(0);
				}
			});
		}else{
			System.exit(0);
		}
	}
	
	private void save() {	
		saveAs(getLastChosenWorkDirectory());
	}
	
	private void saveAs(String workdir) {
		
		_currentPerspective.setToolsPositions(_screen.getToolsPositions());
		
		IMemento child;
		File saveDir = new File(workdir);
		if (!saveDir.exists())	
			saveDir.mkdir();  
	
		File saveFile = new File(workdir+"/"+SAVE_FILE);

		XMLMemento memento = XMLMemento.createWriteRoot("root");
			
		// Save the data sources
		for(CyclistDatasource source: _model.getSources()){			
			source.save(memento.createChild("CyclistDatasource"));
		}
		
		// Save the data tables
		// Saves only tables added by the user (not loaded by the simulation configuration file).
		for(Table table: _model.getTables()){
			if(!table.getIsStandardSimulation()){
				table.save(memento.createChild("Table"));
			}
		}
		
		//Save the last selected simulation
		if(_model.getLastSelectedSimulation() != null && _model.getSimulations().size() >0){
			child = memento.createChild("LastSelectedSimulation");
			child.putString("UID", _model.getLastSelectedSimulation().getUID());
		}
		//Save the Simulations
		for(Simulation simulation: _model.getSimulations()){
			simulation.save(memento.createChild("Simulation"));
		}
		
		//Save the simulations aliases
		IMemento aliases = memento.createChild("Aliases");
		for (Map.Entry<Simulation, String> entry : _model.getSimAliases().entrySet()){
			IMemento a_memento = aliases.createChild("Alias");
			Simulation sim = entry.getKey();
			
			a_memento.putString("alias", sim.getAlias());
			a_memento.putString("simulation-id", sim.getSimulationId().toString());		
			a_memento.putString("date", entry.getValue());
		}
		
		//Save the preferences.
		Preferences.getInstance().save(memento.createChild("Preferences"));
		
		//Save the remote servers
//		saveRemoteServers(memento);
		
		_cyclusService.save(memento.createChild("Jobs"));
		
		IMemento perspectives_memento = memento.createChild("Perspectives");
		perspectives_memento.putInteger("current", _currentPerspective.id);
		for (Perspective p : _perspectives) {
			p.save(perspectives_memento.createChild("Perspective"));
		}
		
		//First save the main workspace
		IMemento mainWs = memento.createChild("main-window");	
		saveMainScreen(mainWs.createChild("geom"));
		_screen.save(mainWs);
			
		
		try {
			memento.save(new PrintWriter(saveFile));
			_dirtyFlag = false;

		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
	}
	
	private void restore() {
	
		String currDirectory = _sessionController.getWorkDirectories().get(_sessionController.getLastChosenIndex());
		
		// Check if the save file exists
		File saveFile = new File(currDirectory+"/"+SAVE_FILE);
		
		//Clear the previous data
		clearModel();
		
		//Clears the main workspace.
		for(Perspective p : _perspectives) {
			p.presenter.clearWorkspace();
		}
		
//		_screen.getRemoteServers().clear();
			
		Context ctx = new Context();
		readSimulationsTables(ctx);
		
		// If we have a save file, read it in
		if(saveFile.exists()){			
			Reader reader;
			try {
				reader = new FileReader(saveFile);
				try {
					// Create the root memento
					XMLMemento memento = XMLMemento.createReadRoot(reader);
				
					// Read in the data sources
					IMemento[] sources = memento.getChildren("CyclistDatasource");
					for(IMemento source: sources){
						CyclistDatasource datasource = new CyclistDatasource();
						datasource.restore(source, ctx);
						_model.getSources().add(datasource);
					}
					
					// Read in the tables
					IMemento[] tables = memento.getChildren("Table");
					for(IMemento t_memento: tables){
						Table table = new Table();
						table.restore(t_memento, ctx);
						table.setLocalDatafile(getLastChosenWorkDirectory());
						_model.getTables().add(table);
					}
					
					//Read the simulations
					IMemento[] simulations = memento.getChildren("Simulation");
					for(IMemento simulation:simulations){
						Simulation sim = new Simulation();
						sim.restore(simulation, ctx);
						_model.getSimulations().add(sim);
					}
					
					//Find if there is last selected simulation
					IMemento lastSimulation = memento.getChild("LastSelectedSimulation");
					String lastSimulationId = "";
					if(lastSimulation != null){
						lastSimulationId = lastSimulation.getString("simulation-id");
					}
					_screen.getSimulationPanel().selectSimulation(lastSimulationId);
					
					IMemento aliasesTitle = memento.getChild("Aliases");
					if(aliasesTitle != null){
						IMemento[]aliases = aliasesTitle.getChildren("Alias");
						if(aliases != null){
							for(IMemento alias:aliases){
								Simulation sim = new Simulation();
								sim.restoreAlias(alias);
								String date = alias.getString("date");
								_model.addNewSimALias(sim,date);
							}
						}
					}
					
					//Read the preferences.
					Preferences.getInstance().restore(memento.getChild("Preferences"));
					
//					//Read the remote servers
//					restoreRemoteServers(memento);
					
					_cyclusService.restore(memento.getChild("Jobs"));
					
					_dirtyFlag = false;
					
					//Read the main workspace
					IMemento mainWs = memento.getChild("main-window");
					restoreMainScreen(mainWs.getChild("geom"));
					_screen.restore(mainWs, ctx);
					
					 
					IMemento p_memento = memento.getChild("Perspectives");
					if (p_memento != null) {
						for (IMemento child : p_memento.getChildren("Perspective")) {
							Perspective p = _perspectives[child.getInteger("id")];
							p.restore(child, ctx);
						}
	
						int i = 0;
						try {
							i = p_memento.getInteger("current");
						} catch(Exception e) {
							// ignore
						}
						_currentPerspective= _perspectives[i];
					} else {
						_currentPerspective= _perspectives[0];
						if (memento.getChild("workspace") != null) {
							_perspectives[0].restore(memento, ctx);
						}
					}
								
				} catch (Exception e) {
					log.error("Error during restore: "+e.getMessage());
				}
			} catch (FileNotFoundException e) {
				log.error("Error during restore: "+e.getMessage());
			} 		
		}
		
		if (_currentPerspective == null) 
			_currentPerspective = _perspectives[0];
	}
	
	/*
	 * Reads the configuration file which defines the tables required for simulation.
	 * The tables are loaded with a null database property.
	 * The tables are added to the model tables list.
	 */
	private void readSimulationsTables(Context ctx){
		try {
			Path path = FileSystems.getDefault().getPath(SessionController.CYCLIST_DIR, SIMULATIONS_TABLES_FILE);
			if (!Files.exists(path)) {
				InputStream in = Cyclist.class.getResourceAsStream("assets/"+SIMULATIONS_TABLES_FILE);
				Files.copy(in, path);
			}
			File simulationsFile = path.toFile();
			Reader reader = new FileReader(simulationsFile);
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			
			// Restore tables
			for(IMemento node: memento.getChildren("Table")){
				Table table = new Table();
				table.restoreSimulated(node, ctx);
				table.setLocalDatafile(getLastChosenWorkDirectory());
				_model.getSimulationsTablesDef().add(table);
				_model.getTables().add(table);	
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	/*
	 * Gets the path of the last chosen work directory.
	 * If not available - return the default work directory
	 */
	private String getLastChosenWorkDirectory(){
		if(_sessionController == null){
			return SessionController.CYCLIST_DIR;
		}
		return _sessionController.getWorkDirectories().get(_sessionController.getLastChosenIndex());
	}
	
	/*
	 * Saves the data of the main window: size and location
	 * @param IMemento mainWs 
	 */
	private void saveMainScreen(IMemento mainWs){
		Stage stage = (Stage)_screen.getParent().getScene().getWindow();
		mainWs.putString("x", Double.toString(stage.getX()));
		mainWs.putString("y", Double.toString(stage.getY()));
		mainWs.putString("width", Double.toString(stage.getWidth()));
		mainWs.putString("height", Double.toString(stage.getHeight()));
	}
	
	/*
	 * Restores the data of the main window:size and location.
	 * @param IMemento mainWs
	 */
	private void restoreMainScreen(IMemento mainWs){
		double width = Double.parseDouble(mainWs.getString("width"));
		double height = Double.parseDouble(mainWs.getString("height"));
		double x = Double.parseDouble(mainWs.getString("x"));
		double y = Double.parseDouble(mainWs.getString("y"));
		Stage stage = (Stage)_screen.getParent().getScene().getWindow();
		stage.setWidth(width);
		stage.setHeight(height);
		stage.setX(x);
		stage.setY(y);
	}
	
	
	/*
	 * Clears the model from an old data.
	 */
	private void clearModel(){
		_model.getSources().clear();
		_model.getTables().clear();
		_model.getSimulations().clear();
		_model.setSelectedDatasource(null);
//		_model.getRemoteServersList().clear();
	}
	
	/*
	 * Saves the list of remote servers from the model.
	 * @param IMemento memento
	 */
//	private void saveRemoteServers(IMemento memento){
//		IMemento remotes = memento.createChild("remote-servers");
//		for(String remote : _model.getRemoteServersList()){
//			IMemento server = remotes.createChild("Server");
//			server.putString("address", remote);
//		}
//	}
	
	/*
	 * Restores the list of remote servers.
	 * Adds the list to the model and creates corresponding menu items in the main screen menu.
	 * @param IMemento memento.
	 */
//	private void restoreRemoteServers(IMemento memento){
//		IMemento remotesTitle = memento.getChild("remote-servers");
//		if(remotesTitle != null){
//			IMemento[] servers = remotesTitle.getChildren("Server");
//			if(servers != null){
//				for(IMemento server : servers){
//					String address = server.getString("address");
//					_model.addNewRemoteServer(address);
//					MenuItem item = new MenuItem(address);
//					_screen.getRemoteServers().add(item);
//				}
//			}
//		}
//	}
	
	
	
}
