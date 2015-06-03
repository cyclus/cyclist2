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
package edu.utah.sci.cyclist.core.ui.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.mo.closure.v1.Closure;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import edu.utah.sci.cyclist.core.event.Pair;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.ui.wizards.TableEditorWizard;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;

public class TablesPanel extends TitledPanel  {
	public static final String ID 		= "cyclist-panel";
	public static final String TITLE	= "Tables";
	
	public static final String SELECTED_STYLE = "-fx-background-color: #99ccff";
	public static final String UNSELECTED_STYLE = "-fx-background-color: #f0f0f0";
	
	
	private List<Entry> _entries;
	private ObservableList<Table> _items;
	private ObjectProperty<Table> _tableProperty = new SimpleObjectProperty<>();
	private Entry _selected = null;
	private ObjectProperty<Boolean> _editTableProperty = new SimpleObjectProperty<>();	
	private ContextMenu _menu = new ContextMenu();
	private Entry _selectedForEdit = null;
	private Consumer<Pair<String, Table>> _actionOnTable;

	
	private InvalidationListener _listener = new InvalidationListener() {
		
		@Override
		public void invalidated(Observable observable) {
			resetContent();

		}
	};
	
	public TablesPanel() {
		super(TITLE, GlyphRegistry.get(AwesomeIcon.COLUMNS)); 
		createMenu();
	}
	
//	@Override
	public void setTitle(String title) {
		setTitle(title);
	}

	
	public void setItems(final ObservableList<Table> items) {
		if (_items != items) {
			if (_items != null) {
				_items.removeListener(_listener);
			}
			
			items.addListener(_listener);	
			_items = items;
		}
		
		resetContent();
	}
	
	public void focus(Table table) {
		for (Entry entry : _entries) {
			if (entry.table == table) {
				select(entry);
				break;
			}
		}
	}
	
	public ObjectProperty<Boolean> editTableProperty() {
	         return _editTableProperty;
	}
	 
	public Boolean getEditTable() {
	         return _editTableProperty.get();
	}
	 
	public void setEditTable(Boolean value) {
		 _editTableProperty.set(value);
	}
	
	public void setTableActions(List<String> actions, Consumer<Pair<String, Table>> callback) {
		_actionOnTable = callback;
		int pos = 0;
		for (String name: actions) {
			MenuItem item = new MenuItem(name);
			 item.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) { 
					_actionOnTable.accept(new Pair<String, Table>(name, _selectedForEdit.table));
				}
			 });
			 _menu.getItems().add(pos++, item);
		}
		 
		_menu.getItems().add(pos, new SeparatorMenuItem());
	}
	
	private void resetContent() {
		VBox vbox = (VBox) getContent();
		vbox.getChildren().clear();
		
		_entries = new ArrayList<>();
		List<Table> sortedTables = sortRealAndSimulatedTables();
		for (Table table : sortedTables) {
			Entry entry = createEntry(table);
			_entries.add(entry);
			vbox.getChildren().add(entry.title);
		}
	}
	
	private void select(Entry entry) {
		if (_selected != null) 
			_selected.title.setStyle(UNSELECTED_STYLE);
		_selected = entry;
		_selected.title.setStyle(SELECTED_STYLE);
	}
	
	public ReadOnlyObjectProperty<Table> selectedItemProperty() {
		return _tableProperty;
	}
	
	private Entry createEntry(Table table) {
		final Entry entry = new Entry();
		entry.table = table;
		entry.title = new Label(table.getAlias()/*, GlyphFontRegistry.glyph("FontAwesome|LIST_ALT")*/);
		
		//Separate "real" tables (not from the simulation), by using an Italic font.
		if(table.getDataSource() != null){
			Font font = entry.title.getFont();
			entry.title.setFont(Font.font(font.getFamily(),FontPosture.ITALIC, font.getSize()));
		}
		
		entry.title.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
			    
				MouseEvent mouseEvent = (MouseEvent)event;
				if( mouseEvent.getButton()   == MouseButton.SECONDARY){
					_selectedForEdit = entry;
					_menu.show(entry.title, Side.BOTTOM, 0, 0);
				}
				else{
					if(mouseEvent.getClickCount() == 1){
						_tableProperty.set(entry.table);
						select(entry);
					}
					else if(mouseEvent.getClickCount() == 2){
						_selectedForEdit = entry;
						_menu.show(entry.title, Side.BOTTOM, 0, 0);
					}
				
				}
			}
		});
		
		
		
		entry.title.setOnDragDetected(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {					
				_tableProperty.set(entry.table);
		    	select(entry);
		    	
				DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
				clipboard.put(DnD.TABLE_FORMAT, Table.class, entry.table);
				
				Dragboard db = entry.title.startDragAndDrop(TransferMode.COPY);
				
				ClipboardContent content = new ClipboardContent();
				content.put(DnD.TABLE_FORMAT, entry.title.getText());
				
				SnapshotParameters snapParams = new SnapshotParameters();
	            snapParams.setFill(Color.TRANSPARENT);
	            
	            content.putImage(entry.title.snapshot(snapParams, null)); 
				db.setContent(content);
			}
		});		
		return entry;
	}
	
		
	public void removeTable(Entry entry) {
		_items.remove(entry.table);
	}

		
	private void editTable(final Entry entry) {
		TableEditorWizard wizard = new TableEditorWizard(entry.table);
			
		Bounds bounds = this.localToScene(this.getBoundsInLocal());
		ObjectProperty<Table> selection = wizard.show(this.getParent().getScene().getWindow(), bounds);
		
		selection.addListener(new ChangeListener<Table>(){
			@Override
			public void changed(ObservableValue<? extends Table> arg0, Table oldVal,Table newVal) {
					entry.table = newVal;
					entry.title.setText(entry.table.getAlias());
					setEditTable(true);
			}	
		});
	}
	
	/*
	 * Sort the list of tables so that tables which belong to simulation are the first
	 * and "real" tables (which came from a database and not from a simulation) are at the
	 * end of the list.
	 * 
	 * @return List<Table> - the list of sorted tables.
	 */
	private List<Table> sortRealAndSimulatedTables(){
		List<Table> sortedList = new ArrayList<>();
		int numOfSimulatedTables = 0;
		for(Table tbl: _items){
			if(tbl.getDataSource() == null){
				sortedList.add(numOfSimulatedTables,tbl);
				numOfSimulatedTables++;
			}else{
				sortedList.add(tbl);
			}
		}
		return sortedList;
	}
	
	/*
	 * Creates a menu to edit or delete a table.
	 */
	private void createMenu(){
		 
		 MenuItem deleteTable = new MenuItem("Delete Table");
		 deleteTable.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) { 
				removeTable(_selectedForEdit);
				setEditTable(true);
				_selectedForEdit = null;
			}
		 });
		 _menu.getItems().add(deleteTable);

		 MenuItem editTable = new MenuItem("Edit Table");
		 editTable.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) { 
				editTable(_selectedForEdit);
				_selectedForEdit = null;
			}
		 });
		 _menu.getItems().add(editTable);
	}
	
	class Entry {
		Label title;
		Table table;
	}
}
