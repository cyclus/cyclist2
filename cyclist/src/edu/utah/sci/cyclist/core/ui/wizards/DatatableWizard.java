package edu.utah.sci.cyclist.core.ui.wizards;
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
 *     Kristi Potter
 *******************************************************************************/

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.controller.SessionController;
import edu.utah.sci.cyclist.core.model.CyclistDatasource;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.ui.components.DatasourceSelector;
import edu.utah.sci.cyclist.core.ui.components.UpdateDbDialog;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;
import edu.utah.sci.cyclist.core.util.SimulationTablesPostProcessor;

/*
 * Class to allow the user to create or edit a data table.
 * Also controls the creation/editing of data sources.
 */
public class DatatableWizard extends TilePane {
	static Logger log = Logger.getLogger(DatatableWizard.class);
	
	// GUI elements
	private Stage                       _dialog;
	private ListView<CyclistDatasource> _sourcesView;
	private ListView<String>            _tablesView;
//	private ImageView                   _statusDisplay;
	private Label						_status;
	
	private ObservableList<CyclistDatasource> _sources = FXCollections.observableArrayList();
	
	// DataType elements
	private CyclistDatasource     _current;
	private ObjectProperty<Table> _selection =  new SimpleObjectProperty<>();
	private DatasourceSelector    _selector;
	private String               _workDir = SessionController.CYCLIST_DIR;
	private UpdateDbDialog		 _updateDialog;
	private ObjectProperty<Boolean> _dsIsValid  = new SimpleObjectProperty<>();
	private TextArea _statusText;
	private RotateTransition _animation;
	
	// * * * Constructor creates a new stage * * * //
	public DatatableWizard() {
		createDialog(new Table());
	}

	public DatatableWizard(Table tableProperty){
		createDialog(tableProperty);
	}
	
	// * * * Set the existing data sources in the combo box * * * //
	public void setItems(final ObservableList<CyclistDatasource> sources) {
		_sources = sources;
		_sourcesView.setItems(_sources);
		_sourcesView.getSelectionModel().selectFirst();
	}
			
	// * * * Show the dialog * * * //
	public  ObjectProperty<Table>  show(Window window) {

		 _dialog.initOwner(window);
		 _dialog.show();	
		 
		// TODO: hopefully in JAVA 8 moving this to be BEFORE the show() will make it not flash, but at the moment it doesn't work
		// Moves window to be in the middle of the main window 
		_dialog.setX(window.getX() + (window.getWidth() - _dialog.getWidth())*0.5);
		_dialog.setY(window.getY() + (window.getHeight() - _dialog.getHeight())*0.5);
		 	
		return _selection;
	}
	
	// * * * Create the dialog
	private void createDialog(Table tableProperty){
		
		_dialog = new Stage();
		_dialog.setTitle("Data Tables");
		
		_dialog.initModality(Modality.WINDOW_MODAL);
		_dialog.setScene( createScene(_dialog, tableProperty) );	
	
		log.debug("changed" + _sourcesView.getSelectionModel().selectedItemProperty()) ;
	}
	
	// * * * Create scene creates the GUI * * * //
	private Scene createScene(final Stage dialog, final Table table) {
		Button selectionButton;
		Button addButton;
		Button editButton;
		Button removeButton;
		
		// Get the name of the table, if we have one
		String tableName = table.getName();
		if (tableName == null) tableName = "";
			
		// * * * The connection settings group
		VBox connectionBox = new VBox();
		connectionBox.setSpacing(5);
		connectionBox.setAlignment(Pos.CENTER_LEFT);
		
		HBox connectionSources = new HBox();
		connectionSources.setSpacing(5);
		connectionSources.setAlignment(Pos.CENTER_LEFT);
	
		VBox sourcesBox = new VBox();
		sourcesBox.setSpacing(5);
		sourcesBox.setAlignment(Pos.CENTER_LEFT);
		_sourcesView = new ListView<CyclistDatasource>();
		
		sourcesBox.getChildren().addAll(new Text("Data Sources"),_sourcesView);
		
		VBox sourcesButtons = new VBox();
		sourcesButtons.setSpacing(5);
		sourcesButtons.setAlignment(Pos.CENTER);
		
		addButton = new Button("Add");
		addButton.setMinWidth(75);
		
		editButton = new Button("Edit");
		editButton.setMinWidth(75);
		
		removeButton = new Button("Remove");
		removeButton.setMinWidth(75);
			
	    sourcesButtons.getChildren().addAll(addButton,editButton,removeButton);
	    
	    connectionSources.getChildren().addAll(sourcesBox,sourcesButtons);
	    
	    HBox.setHgrow(sourcesBox, Priority.ALWAYS);
		HBox.setHgrow(_sourcesView, Priority.ALWAYS);
		    
	    HBox connectBox = new HBox();
	    connectBox.setSpacing(10);
	    connectBox.setPadding(new Insets(5));
	    connectBox.setAlignment(Pos.CENTER_LEFT);
	    
	    selectionButton = new Button("Connect");
	    
	    selectionButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {	
				if(SimulationTablesPostProcessor.isDbUpdateRequired(_current)){
					setDbUpdate(true,_current);
	 				_dsIsValid.addListener(new ChangeListener<Boolean>(){
	 					@Override
	 					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal, Boolean newVal) {
	 						selectConnection(_current,newVal);
	 					}
	 				});
				}else{
					selectConnection(_current,true);
				}
			};	
	    });
		    
	    _status = new Label();
	    
	    connectBox.getChildren().addAll(selectionButton,_status);
	    
	    connectionBox.getChildren().addAll(connectionSources,connectBox);
		
		_sourcesView.setId("datasources-list");
//		_sourcesView.setMaxSize(100, 100);
		_sourcesView.setMinSize(100, 50);

		// Keep track of the currently selected data source
		_sourcesView.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<CyclistDatasource>() {
					public void changed(ObservableValue<? extends CyclistDatasource> ov, 
							CyclistDatasource old_val, CyclistDatasource new_val) {
						
						_current = _sourcesView.getSelectionModel().getSelectedItem();
						_tablesView.getItems().clear();
//						_statusDisplay.setImage(null);
						_status.setGraphic(null);
						
					}
				});
		
		
	
		// Disable edit/remove until we have something selected
		editButton.disableProperty().bind( _sourcesView.getSelectionModel().selectedItemProperty().isNull());
		removeButton.disableProperty().bind( _sourcesView.getSelectionModel().selectedItemProperty().isNull());
		selectionButton.disableProperty().bind( _sourcesView.getSelectionModel().selectedItemProperty().isNull());
		
		// add button actions
		addButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				selectDatasource(new CyclistDatasource());		
			}	
		});	

		editButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				selectDatasource(_current);		
			}	
		});	
		
		removeButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {			
				_sources.remove(_current);		
			}	
		});	
		
		// The connection schema	
		VBox schemaBox = new VBox();
		schemaBox.setSpacing(1);
		schemaBox.setPadding(new Insets(5));
		schemaBox.setMaxHeight(Double.MAX_VALUE);
		
		_tablesView = new ListView<String>();
		
		schemaBox.getChildren().addAll(new Text("Select Table:"),_tablesView);
		
		_tablesView.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(_tablesView, Priority.ALWAYS);
		VBox.setVgrow(schemaBox, Priority.ALWAYS);
		schemaBox.disableProperty().bind(_sourcesView.getSelectionModel().selectedItemProperty().isNull());
	
		// The ok/cancel buttons
		Button ok = new Button("Ok");
		ok.setOnAction(new EventHandler<ActionEvent>() {	
							@Override
							public void handle(ActionEvent arg0) {
								updateTable(table);
								_selection.setValue(table);
								//(table.getName());
								dialog.hide();
							};
						});
		
		Button cancel = new Button("Cancel");
		cancel.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								dialog.hide();
							};
						});
		
		HBox buttonsBox = new HBox();
		buttonsBox.setSpacing(10);
		buttonsBox.setAlignment(Pos.CENTER_RIGHT);
		buttonsBox.setPadding(new Insets(5));
		
		buttonsBox.getChildren().addAll(ok,cancel);
		
	    
		HBox.setHgrow(buttonsBox,  Priority.ALWAYS);
		ok.disableProperty().bind(_tablesView.getSelectionModel().selectedItemProperty().isNull());
	
		
		// Create the scene
		VBox wizardVbox = new VBox();
		wizardVbox.setSpacing(5);
		wizardVbox.setPadding(new Insets(5));
		wizardVbox.setPrefHeight(500);
		wizardVbox.setId("datatable-wizard");
		wizardVbox.getChildren().addAll(connectionBox, schemaBox, _selector = new DatasourceSelector(table), buttonsBox);
		
		Scene scene = new Scene(wizardVbox);
			
		scene.getStylesheets().add(Cyclist.class.getResource("assets/Cyclist.css").toExternalForm());
		_selector.disableProperty().bind(_tablesView.getSelectionModel().selectedItemProperty().isNull());
		
		_statusText = new TextArea();
		_animation = new RotateTransition();
		
	/*if(_current != null)
			_sourcesView.getSelectionModel().select(_current);
		else if(_sourcesView.getItems().size() == 1)
			_sourcesView.getSelectionModel().select(0);
		*/	
		_sourcesView.getSelectionModel().selectFirst();
		//_sourcesView.getSelectionModel().clearAndSelect(0);
		
//		_updateDialog = new UpdateDbDialog(null,null);
		
		// Return the scene
		return scene;
	}
	
	/**
	 * selectDataSource
	 */
	private void selectDatasource(CyclistDatasource datasource){
		DatasourceWizard wizard = new DatasourceWizard(datasource);
		ObjectProperty<CyclistDatasource> selection = wizard.show(_dialog.getScene().getWindow());
		selection.addListener(new ChangeListener<CyclistDatasource>(){
			@Override
			public void changed(ObservableValue<? extends CyclistDatasource> arg0, CyclistDatasource oldVal, CyclistDatasource newVal) {
				if (!_sourcesView.getItems().contains(newVal)){
					_sourcesView.getItems().add(newVal);
				}else{
					//A ListView hack: in order to refresh the displayed items, Should change the number of items in the list.
					CyclistDatasource demoDs = new CyclistDatasource();
					demoDs.setName("demo");
					_sourcesView.getItems().add(demoDs);
					_sourcesView.getItems().remove(demoDs);
				}
				_sourcesView.getSelectionModel().select(newVal);
			}
		});
	}
	
	private void selectConnection(CyclistDatasource ds, Boolean dsIsValid) {
		
		_tablesView.getItems().clear();
		
		//If database has to be updated but the update process has failed.
		if(!dsIsValid){
			_status.setGraphic(GlyphRegistry.get(AwesomeIcon.WARNING));
		}else{
		
			try (Connection conn = ds.getConnection()) {
				_status.setGraphic(GlyphRegistry.get(AwesomeIcon.CHECK));//"FontAwesome|OK"));
				
				DatabaseMetaData md = conn.getMetaData();
				ResultSet rs = md.getTables(null, null, "%", null);
				while (rs.next()) {
					_tablesView.getItems().add(rs.getString(3));
				}
				
			} catch (Exception e) {
				_status.setGraphic(GlyphRegistry.get(AwesomeIcon.WARNING));//"FontAwesome|WARNING"));
	
			}finally {
				ds.releaseConnection();
			}
		}
	}
	
	private void updateTable(Table table) {
		// for now table name is the same as the remote name
		String name = (String) _tablesView.getSelectionModel().getSelectedItem();
		
		table.setName(name); 
		table.setAlias(_selector.getAlias());
		table.setDataSource(_current);
		table.setLocalDatafile(_workDir);
		table.setProperty(Table.REMOTE_TABLE_NAME, name);
		//table.extractSchema();
	}
	
	public CyclistDatasource getSelectedSource() {
		return _current;
	}

	public void setSelectedSource(CyclistDatasource source) {
		_current = source;	
		_sourcesView.getSelectionModel().select(_current);
	}
	
	public void setWorkDir(String workDir){
		_workDir = workDir;
	}
	
	/*
	 * Checks the argument "isRunning":
	 * If true - 
	 * 	it's the beginning of the database update process. 
	 * 	Display the updateDb dialog and ask the user whether or not to update the database.
	 * 	If user approves - start the update process.
	 * 	If user cancels - hide the dialog and set the datasource validity to false.
	 * 
	 * If false - the data base update is done - close the update dialog.
	 * @param isStart - is it the start or the end of the process.
	 * @CyclistDatasource ds - the datasource to update.
	 * 
	 */
	private void setDbUpdate(Boolean isRunning, CyclistDatasource ds){
		if(isRunning){
				_updateDialog = new UpdateDbDialog(_statusText, _animation);
				_statusText.clear();  //in case it is not the first time it is called.
				ObjectProperty<Boolean> selection = _updateDialog.show(_dialog.getScene().getWindow());
				selection.addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldVal,Boolean newVal) {
						if(newVal){
							runDbUpdate(ds);
						}else{
							_dsIsValid.set(false);
							_updateDialog.hide();
						}
					}
				});
		}else{
			_statusText.textProperty().unbind();
			_updateDialog.hide();
		}
	}
	
	/*
	 * Calls the post processing utility to perform a database update.
	 * Updates the animation and the status text to display the database update status to the user.
	 * @param CyclistDatasource ds - the data source to update 
	 */
	private void runDbUpdate(final CyclistDatasource ds){
		SimulationTablesPostProcessor postProcessor = new SimulationTablesPostProcessor();
		Task<Boolean> task = postProcessor.process(ds);
		if(task != null){	
			task.valueProperty().addListener(new ChangeListener<Boolean>() {
				 
		        @Override 
		        public void changed(ObservableValue<? extends Boolean> arg0,Boolean oldVal, Boolean newVal) {
		        	_animation.stop();
		        	_dsIsValid.set(newVal);
		        	setDbUpdate(false, ds);
		        }
		    });
		
			_statusText.textProperty().bind(task.messageProperty());
			_animation.play();
		}
}
	
}
