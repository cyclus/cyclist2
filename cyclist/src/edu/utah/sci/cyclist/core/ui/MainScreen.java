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
package edu.utah.sci.cyclist.core.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import edu.utah.sci.cyclist.ToolsLibrary;
import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.model.Context;
import edu.utah.sci.cyclist.core.model.Resource;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.tools.ToolFactory;
import edu.utah.sci.cyclist.core.ui.panels.FiltersListPanel;
import edu.utah.sci.cyclist.core.ui.panels.InputPanel;
import edu.utah.sci.cyclist.core.ui.panels.JobsPanel;
import edu.utah.sci.cyclist.core.ui.panels.SchemaPanel;
import edu.utah.sci.cyclist.core.ui.panels.SimulationsPanel;
import edu.utah.sci.cyclist.core.ui.panels.TablesPanel;
import edu.utah.sci.cyclist.core.ui.panels.TitledPanel;
import edu.utah.sci.cyclist.core.ui.panels.ToolsPanel;
import edu.utah.sci.cyclist.core.ui.wizards.WorkspaceWizard;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;

public class MainScreen extends VBox implements Resource {
	public static final String ID = "main-screen";
	
	private SplitPane _sp;
	private SplitPane _toolsPane;
	private TablesPanel _datasourcesPanel;
	private SchemaPanel _fieldsPanel;
    private ToolsPanel _toolsPanel;
    private InputPanel _inputPanel;
	private TabPane _tabsPane;
	private SimulationsPanel _simulationPanel;
	private JobsPanel _jobsPanel;
	private Map<String, TitledPanel> _panels = new HashMap<>();

	private Menu _perspectiveMenu;
    private Menu _toolsMenu;
//    private Menu _inputMenu;
//	private Menu _runMenu;
	
	private ObjectProperty<EventHandler<WindowEvent>> _stageCloseProperty;
		
	/**
	 */
	public  MainScreen(Stage stage) {
		super();
		setId(ID);
		
		build(stage);
	}
	
	public Window getWindow() {
		return getScene().getWindow();
	}
	
	@Override
    public String getUID() {
		// not used
	    return null;
    }
	
	public ObservableList<String> selectWorkspace(ObservableList<String> list, int chosenIndex) {
		WorkspaceWizard wizard = new WorkspaceWizard();
		wizard.setItems(list,chosenIndex);
		return wizard.show(getScene().getWindow());
	}
	
	public TabPane getTabPane() {
		return _tabsPane;
	}
	
	public void showPanels(List<String> list, double[] pos) {
		_toolsPane.getItems().clear();
		for (String name : list) {
			TitledPanel panel = _panels.get(name);
			if (panel != null) 
				_toolsPane.getItems().add(panel);
			else
				System.out.println("MainScreen error: unknow panel ["+name+"]");
		}
		
		Platform.runLater(new Runnable() {	
			@Override
			public void run() {
				_toolsPane.setDividerPositions(pos);
			}
		});
	}
	
	public TablesPanel getDatasourcesPanel() {
		return _datasourcesPanel;
	}
	
	public SchemaPanel getFieldsPanel(){
		return _fieldsPanel;
	}
	
    public ToolsPanel getToolsPanel() {
        return _toolsPanel;
    }
    
    public InputPanel getInputPanel() {
        return _inputPanel;
    }
    
	public SimulationsPanel getSimulationPanel(){
		return _simulationPanel;
	}
	
	public JobsPanel getJobsPanel() {
		return _jobsPanel;
	}
	
	private double TOOLS_WIDTH = 120; 
	
	private void build(Stage stage){
		getStyleClass().add("main-screen");		
		double [] mainDividers = {TOOLS_WIDTH/600.0};
		
		this.setPrefWidth(600);
		this.setPrefHeight(400);
		this.setPadding(new Insets(0));
		this.setSpacing(0);

		_panels.put("Builder", _inputPanel = new InputPanel());
		_panels.put("Simulations", _simulationPanel = new SimulationsPanel());
		_panels.put("Tables", _datasourcesPanel = new TablesPanel());
		_panels.put("Fields", _fieldsPanel = new SchemaPanel("Fields"));
		_panels.put("Tools", _toolsPanel = new ToolsPanel());
		_panels.put("Jobs", _jobsPanel = new JobsPanel());
		_panels.put("Filters", new FiltersListPanel());
		
		_sp = new SplitPane();
		_sp.setOrientation(Orientation.HORIZONTAL);
		_sp.setDividerPositions(mainDividers);
		
		_toolsPane = new SplitPane();
		_toolsPane.getStyleClass().add("hiddenSplitter");
		_toolsPane.setPrefWidth(USE_COMPUTED_SIZE);
		_toolsPane.setPrefHeight(USE_COMPUTED_SIZE);
		_toolsPane.setOrientation(Orientation.VERTICAL);
		
		_sp.getItems().addAll(
				_toolsPane,
				_tabsPane = new TabPane());
		
		this.getChildren().addAll(
				createMenuBar(stage),
				_sp
				);
			
		VBox.setVgrow(_sp, Priority.ALWAYS);
		
		SplitPane.setResizableWithParent(_toolsPane, false);
		
		_stageCloseProperty = stage.onCloseRequestProperty();
//		_remoteServers = FXCollections.observableArrayList();
//		_remoteServers.addListener(new ListChangeListener<MenuItem>() {
//			@Override
//			public void onChanged(ListChangeListener.Change<? extends MenuItem> change) {
//				List<MenuItem> newServers = new ArrayList<MenuItem>();
//				List<MenuItem> deletedServers = new ArrayList<MenuItem>();
//				while (change.next()) {
//					for (MenuItem item : change.getRemoved()) {
//						deletedServers.add(item);
//					}
//					for (MenuItem item : change.getAddedSubList()) {
//						item.setUserData(item.getText());
//						item.onActionProperty().set(_runMenuItem.getOnAction());
//						newServers.add(item);
//					}
//					updateRunOnMenu(newServers,deletedServers);
//				}
//			}
//		});
	}
	
	/*
	 * Menus & Actions
	 */
	
    public Menu getToolsMenu() {
        return _toolsMenu;
    }
    
//    public Menu getInputMenu() {
//        return _inputMenu;
//    }
    
	public Menu getPerspectiveMenu() {
		return _perspectiveMenu;
	}
	
//	public Menu getRunMenu() {
//		return _runMenu;
//	}
	
	private MenuItem _datasourceMenuItem;
	private MenuItem _workspaceMenuItem;
	private MenuItem _quitMenuItem;
	private MenuItem _saveMenuItem;
	private MenuItem _saveAsMenuItem;
	private MenuItem _simulationMenuItem;
	private MenuItem _sqliteLoaderMenuItem;
//	private MenuItem _runMenuItem;
//	private MenuItem _manageMenuItem;
//	private Menu     _runOnMenu;
//	private MenuItem _runOnOtherItem;
	private MenuItem _preferencesMenuItem;
//	private ObservableList<MenuItem> _remoteServers;
	
	public ObjectProperty<EventHandler<ActionEvent>> onAddDatasource() {
		return _datasourceMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onAddSimulation() {
		return _simulationMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onLoadSqlite() {
		return _sqliteLoaderMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSelectWorkspace() {
		return _workspaceMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSave() {
		return _saveMenuItem.onActionProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSaveAs() {
		return _saveAsMenuItem.onActionProperty();
	}
	public ObjectProperty<EventHandler<ActionEvent>> onQuit() {
		return _quitMenuItem.onActionProperty();
	}
	
//	public ObjectProperty<EventHandler<ActionEvent>> onRun() {
//		return _runMenuItem.onActionProperty();
//	}
	
//	public ObjectProperty<EventHandler<ActionEvent>> onManage() {
//		return _manageMenuItem.onActionProperty();
//	}
//	
//	public ObjectProperty<EventHandler<ActionEvent>> onRunOnOther() {
//		return _runOnOtherItem.onActionProperty();
//	}
	
	public ObjectProperty<Boolean> editDataSourceProperty() {
		return _datasourcesPanel.editTableProperty();
	}
	
	public ObjectProperty<EventHandler<ActionEvent>> onSetPreferences() {
		return _preferencesMenuItem.onActionProperty();
	}
	
//	public ObservableList<MenuItem> getRemoteServers(){
//		return _remoteServers; 
//	}

	
	/**
	 * Property which is changed when user wants to edit a simulation entry in the simulations panel.
	 * (Changes the simulation alias field).
	 * (keyboard - escape or enter pressed)
	 * @return ObjectProperty<Simulation> - property is set to the new Simulation value on enter/escape pressed.
	 */
	public ObjectProperty<Simulation> editSimulationProperty() {
		return _simulationPanel.editSimulationProperty();
	}
	
	/**
	 * Property which is changed when the user selects a simulation entry in the simulations panel.
	 * (trigger - mouse click)
	 * @return ObjectProperty<Simulation> - the selected simulation.
	 */
	public ObjectProperty<Simulation> selectSimulationProperty() {
		return _simulationPanel.selectedItemProperty();
	}
	
	
	public ObjectProperty<EventHandler<WindowEvent>> onSystemClose(){
		return _stageCloseProperty;
	}
	
	public double[] getToolsPositions() {
		return _toolsPane.getDividerPositions();
	}
	
	public double[] getSplitPositions() {
		return _sp.getDividerPositions();
	}
	
	public void save(IMemento memento) {
		memento.createChild("sp-pos").putString("values", Arrays.toString(_sp.getDividerPositions()));
	}
	
	public void restore(IMemento memento, Context ctx) {
		if (memento.getChild("sp-pos") != null) {
			final double [] pos = parseArray(memento.getChild("sp-pos").getString("values"));

			Platform.runLater(new Runnable() {	
				@Override
				public void run() {
					_sp.setDividerPositions(pos);
				}
			});
		}
	}
	
	private double[] parseArray(String str) {
		String[] fields = str.substring(1, str.length()-1).split(",");
		double v[] = new double[fields.length];
		for (int i=0; i<fields.length; i++) {
			v[i] = Double.valueOf(fields[i]);
		}
		return v;
		
	}
	private MenuBar createMenuBar(Stage stage) {
		MenuBar menubar = new MenuBar();
		
		Menu fileMenu = createFileMenu();
		Menu dataMenu = createDataMenu();
        _toolsMenu = createToolsMenu();
//        _inputMenu = createInputMenu();
//		_runMenu = createRunMenu();
		
        menubar.getMenus().addAll(fileMenu, dataMenu, _toolsMenu /*, _inputMenu , _runMenu*/);
		
		return menubar;
	}
	
	private Menu createFileMenu() {
		
		_workspaceMenuItem = new MenuItem("Work Directory",GlyphRegistry.get(AwesomeIcon.FOLDER_OPEN_ALT)); 
		
		_saveMenuItem = new MenuItem("Save", GlyphRegistry.get(AwesomeIcon.SAVE));
		_saveMenuItem.setAccelerator(KeyCombination.keyCombination("Meta+S"));
		
		_saveAsMenuItem = new MenuItem("Save As", GlyphRegistry.get(AwesomeIcon.SAVE));
		
		_preferencesMenuItem = new MenuItem("Preferences", GlyphRegistry.get(AwesomeIcon.COG));
		
		// -- Quit
		_quitMenuItem = new MenuItem("Quit", GlyphRegistry.get(AwesomeIcon.SIGN_OUT));
		_quitMenuItem.setAccelerator(KeyCombination.keyCombination("Meta+Q"));
		
		// -- setup the menu 
		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(
					new SeparatorMenuItem(), 
					_workspaceMenuItem, 
					_saveMenuItem,
					_saveAsMenuItem,
					new SeparatorMenuItem(),
					_preferencesMenuItem,
					new SeparatorMenuItem(),
					_quitMenuItem);
		return fileMenu;
	}
	
	private Menu createDataMenu() {
		_datasourceMenuItem = new MenuItem("Datatable", GlyphRegistry.get(AwesomeIcon.FOLDER_OPEN_ALT));	
		_simulationMenuItem = new MenuItem("Simulation", GlyphRegistry.get(AwesomeIcon.FOLDER_OPEN_ALT));
		_sqliteLoaderMenuItem = new MenuItem("Load Sqlite", GlyphRegistry.get(AwesomeIcon.FOLDER_OPEN_ALT));
		
		// -- setup the menu 
		Menu dataMenu = new Menu("Data");
		dataMenu.getItems().addAll(
				_datasourceMenuItem,_simulationMenuItem,_sqliteLoaderMenuItem);
		return dataMenu;
	}

    private Menu createToolsMenu() {
        Menu menu = new Menu("Tools");          

        for (final ToolFactory factory : ToolsLibrary.getFactoriesOfType(ToolsLibrary.SCENARIO_TOOL)) {
            MenuItem item = new MenuItem(factory.getToolName(), GlyphRegistry.get(factory.getIcon()));
            item.getProperties().put("type", ToolsLibrary.SCENARIO_TOOL);
            menu.getItems().add(item);
        }
        
        for (final ToolFactory factory : ToolsLibrary.getFactoriesOfType(ToolsLibrary.VIS_TOOL)) {
            MenuItem item = new MenuItem(factory.getToolName(), GlyphRegistry.get(factory.getIcon()));
            item.getProperties().put("type", ToolsLibrary.VIS_TOOL);
            menu.getItems().add(item);
        }

        return menu;
    }
	
    public void selectTools(String type) {
    	for (MenuItem item : _toolsMenu.getItems()) {
    		item.setDisable(item.getProperties().get("type") != type);
    	}
    }
    
//    private Menu createInputMenu() {
//        Menu menu = new Menu("Scenario Builder");          
//
//        for (final ToolFactory factory : ToolsLibrary.getFactoriesOfType(ToolsLibrary.SCENARIO_TOOL)) {
//            MenuItem item = new MenuItem(factory.getToolName(), GlyphRegistry.get(factory.getIcon()));
//            menu.getItems().add(item);
//        }
//
//        return menu;
//    }

//	private Menu createRunMenu() {
//		Menu menu= new Menu("Run");
//		_runMenuItem = new MenuItem("Submit file"/*, GlyphRegistry.get(AwesomeIcon.EXCHANGE))*/);
//		_runMenuItem.setAccelerator(KeyCombination.keyCombination("Meta+R"));
//		//Let the controller retrieve the current default server each time the menu item is selected.
//		_runMenuItem.setUserData("");
//		
//		_runOnMenu = new Menu("Run on");
//	    _runOnOtherItem = new MenuItem("other...");
//		   
//		_runOnMenu.getItems().add(_runOnOtherItem);
//		
//		_manageMenuItem = new MenuItem("Manage list");
//
//		menu.getItems().addAll(_runMenuItem,_runOnMenu,_manageMenuItem);
//	
//		return menu;
//	}
	
	/*
	 * Updates the remote servers menu after the list of remote addresses has changed.
	 * Each address is represented by one menu item. Adds or removes menu items according to the
	 * changes in the remote servers list. 
	 * @param List<MenuItem> newServers - list of new addresses to add.
	 * @param List<MenuItem> deletedServers - list of addresses to remove.
	 *  
	 */
//	private void updateRunOnMenu(List<MenuItem> newServers, List<MenuItem> deletedServers){
//		
//		List<MenuItem> deleted = new ArrayList<MenuItem>();
//		//First deleted old items
//		for(MenuItem item : deletedServers){
//			for(MenuItem menuItem :_runOnMenu.getItems()){
//				if(menuItem.getText() != null &&  menuItem.getText().equals(item.getText())){
//					deleted.add(menuItem);
//				}
//			}
//		}
//		
//		for(MenuItem menuItem : deleted){
//			_runOnMenu.getItems().remove(menuItem);
//		}
//		
//		//If only the separator and "other" where left
//		if(_runOnMenu.getItems().size()==2 && _runOnMenu.getItems().get(0) instanceof SeparatorMenuItem){
//			 _runOnMenu.getItems().remove(0);
//		}
//		
//		//If only "other" menu item exists - add a separator
//		if(_runOnMenu.getItems().size()==1 && newServers.size()>0 ){
//			_runOnMenu.getItems().add(0,new SeparatorMenuItem());
//		}
//		if(newServers.size()>0){
//			_runOnMenu.getItems().addAll(0, newServers);
//		}
//	}
}
