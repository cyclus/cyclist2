package edu.utah.sci.cyclist.core.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.event.ui.FilterEvent;
import edu.utah.sci.cyclist.core.model.Field;
import edu.utah.sci.cyclist.core.model.FieldProperties;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.model.DataType.Role;

public class DropArea extends HBox implements Observable {
	static Logger log = Logger.getLogger(DropArea.class);
	
	public enum Policy {SINGLE, MULTIPLE};
	public enum AcceptedRoles{ALL,DIMENSION};
	
	private ObjectProperty<ObservableList<Field>> _fieldsProperty = new SimpleObjectProperty<>();
	private Policy _policy;
	private AcceptedRoles _acceptedRoles;
	private ObjectProperty<Table> _tableProperty = new SimpleObjectProperty<>();
	private List<InvalidationListener> _listeners = new ArrayList<>();
	private List<Field> _preOccupiedFields = new ArrayList<>();
	private ObjectProperty<EventHandler<FilterEvent>> _action = new SimpleObjectProperty<>();
	private Map<Class<?>, TransferMode[]> _sourcesTransferModes;
	
	public DropArea(Policy policy, AcceptedRoles acceptedRoles) {
		_policy = policy;
		_acceptedRoles = acceptedRoles;
		build();
		init();
	}
	
	
	public ObjectProperty<EventHandler<FilterEvent>> onAction() {
		return _action;
	}
	
	public void setOnAction( EventHandler<FilterEvent> handler) {
		_action.set(handler);
	}
	
	public EventHandler<FilterEvent> getOnAction() {
		return _action.get();
	}
	
	public ObjectProperty<ObservableList<Field>> fieldsProperty() {
		return _fieldsProperty;
	}
	
	
	public Policy getPolicy() {
		return _policy;
	}
	
	public void setPolicy(Policy policy) {
		// TODO: deal with the case where policy == MULTIPLE and there are already more than one Filter
		_policy = policy;
	}
	
	public ObjectProperty<Table> tableProperty() {
		return _tableProperty;
	}
	
	public Table getTable() {
		return _tableProperty.get();
	}
	public ObservableList<Field> getFields() {
		return _fieldsProperty.get();
	}
	
	public boolean isValid() {
		for (Node node : getChildren()) {
			FieldGlyph glyph = (FieldGlyph) node;
			if (glyph.isDisabled())
				return false;
		}		
		return true;
	}
	
	public void removeFilterFromGlyph(Object filter){
		for (Node node : getChildren()) {
			FieldGlyph glyph = (FieldGlyph) node;
			if(glyph.removeFieldFilter(filter)){
				break;
			}
		}
	}
	
	public String getFieldTitle(int index) {
		FieldGlyph glyph = (FieldGlyph) getChildren().get(index);
		return glyph.getTitle();
	}
	
	/* Name: updatePreOccupiedField
     * Saves all the fields already used by other drop areas.
     * (Cannot add them to the current drop area as long as they are used by the other drop areas) */
	public void updatePreOccupiedField(List<Field> fields){
		_preOccupiedFields.clear();
		_preOccupiedFields.addAll(fields);
	}
	
	private boolean compatible() {
		Field field = getLocalClipboard().get(DnD.FIELD_FORMAT, Field.class);
		if (field == null
			|| (_acceptedRoles == AcceptedRoles.DIMENSION && (field.getRole() != Role.DIMENSION && field.getRole() != Role.INT_TIME))
			|| isPreOccupiedField(field)
			|| (getFields().size() > 0 && _policy == Policy.MULTIPLE && field.getRole() != getFields().get(0).getRole())) 
		{
			return false;
		}
		return true;
	}
	
	/**
     * @name setDragAndDropModes
     * @param sourcesTransferModes - Maps for each possible source the accepted drag and drop transfer modes.
     */
	public void setDragAndDropModes( Map<Class<?>, TransferMode[]> sourcesTransferModes) {
		_sourcesTransferModes = sourcesTransferModes;
	}
	
	private void build() {	
		setSpacing(0);
		setPadding(new Insets(2));
		setMinWidth(30);
		setPrefHeight(18);
		getStyleClass().add("drop-area");
				
		/*
		 * DnD handlers
		 */
		setOnDragEntered(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {	
//				if (_policy == Policy.MUTLIPLE || getFilters().size() == 0) {
					if (getLocalClipboard().hasContent(DnD.FIELD_FORMAT))
						setStyle("-fx-border-color: #c0c0c0");
					event.consume();
//				}
				
			}
		});
		
		setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
//				if (_policy == Policy.MUTLIPLE || getFilters().size() == 0) {
					if (getLocalClipboard().hasContent(DnD.FIELD_FORMAT)) {
						
						//Accepts the drag and drop transfer mode according to the source and the 
                    	//predefined accepted transfer modes.
						if(getLocalClipboard().hasContent(DnD.DnD_SOURCE_FORMAT)){
                        	Class<?> key = getLocalClipboard().getType(DnD.DnD_SOURCE_FORMAT);
                    	    if(key != null && _sourcesTransferModes!= null && _sourcesTransferModes.containsKey(key)  && compatible()) {
                    	    	event.acceptTransferModes(_sourcesTransferModes.get(key));
                    	    }
                        }
					}
					event.consume();
//				}
			}
		});
		
		setOnDragExited(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				if (getLocalClipboard().hasContent(DnD.FIELD_FORMAT)) {
					setEffect(null);
					//setStyle("-fx-border-color: -fx-body-color");
					setStyle("-fx-box-border: transparent");
					event.consume();
				}
				
			}
		});
		
		setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {
				boolean status = false;

				Field field = getLocalClipboard().get(DnD.FIELD_FORMAT, Field.class);
				//if (event.getAcceptedTransferMode() == TransferMode.COPY) {
					field = field.clone();
					if (field.getString(FieldProperties.AGGREGATION_FUNC) == null) {
						field.set(FieldProperties.AGGREGATION_FUNC, field.getString(FieldProperties.AGGREGATION_DEFAULT_FUNC));
					}
				//}
				if (field != null) {
					if(_acceptedRoles == AcceptedRoles.DIMENSION && (field.getRole() != Role.DIMENSION && field.getRole() != Role.INT_TIME)){
						log.warn("Cannot add non-discrete field to this drop area");
						status = false;
					} else if(isPreOccupiedField(field)) {
						log.warn("Field already exists in another drop area");
						status = false;
					}else if (getFields().size() == 0) {
						getFields().add(field);
						status = true;
					} else if(_policy == Policy.SINGLE){
						getFields().set(0, field);
						status = true;
					} else if (field.getRole() != getFields().get(0).getRole()) {
						log.warn("Field is incompatible with existing fields");
						status = false;
					} else {
						getFields().add(field);
						status = true;
					}			
				}
				
				event.setDropCompleted(status);
				event.consume();				
			}
			
		});
		
		
		tableProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				Table table = tableProperty().get();
				if (table == null) {
					
				} else {
					for (Node node : getChildren()) {
						if (node instanceof FieldGlyph) {
							FieldGlyph glyph = (FieldGlyph) node;
							glyph.validProperty().set(table.hasField(glyph.getField()));
						}
					}
				}
				
			}
		});
		/*
		 * Handle changes in the list of Filters
		 */
		
		fieldsProperty().addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable observable) {
				
				getFields().addListener(new InvalidationListener() {
			
						@Override
			            public void invalidated(Observable observable) {				
							getChildren().clear();
							for (final Field field : getFields()) {
								final FieldGlyph glyph = new FieldGlyph(field);
								getChildren().add(glyph);
								
								glyph.setOnDragDetected(new EventHandler<MouseEvent>() {

									@Override
									public void handle(MouseEvent event) {
										Dragboard db = glyph.startDragAndDrop(TransferMode.COPY_OR_MOVE);
										
										DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
										clipboard.put(DnD.FIELD_FORMAT, Field.class, field);
										
										ClipboardContent content = new ClipboardContent();
										
										content.putString(field.getName());
										
										SnapshotParameters snapParams = new SnapshotParameters();
							            snapParams.setFill(Color.TRANSPARENT);
							            
							            content.putImage(glyph.snapshot(snapParams, null));	
										db.setContent(content);
										clipboard.put(DnD.DnD_SOURCE_FORMAT, DropArea.class, DropArea.this);
										
										event.consume();
										
//										glyph.setManaged(false);
//										glyph.setVisible(false);
//										getChildren().remove(glyph);
										//getFields().remove(field);
									}
								});
								
								glyph.setOnDragDone(new EventHandler<DragEvent>() {

									@Override
									public void handle(DragEvent event) {
//										glyph.setCursor(Cursor.DEFAULT);
										if (event.isAccepted() && event.getAcceptedTransferMode() == TransferMode.COPY) {
											glyph.setVisible(true);
											glyph.setManaged(true);
										} else {
											getChildren().remove(glyph);
											//getFilters().remove(Filter);
											getFields().remove(field);
										}
									}
								});
								
								glyph.setOnAction(new EventHandler<ActionEvent>() {

									@Override
									public void handle(ActionEvent event) {
										fireInvalidationEvent();
									}
								});
								

								glyph.setOnFilterAction(new EventHandler<FilterEvent>() {
								
									@Override
									public void handle(FilterEvent event) {
										if (getOnAction() != null) {
											getOnAction().handle(event);
										}
									}
								});
								
//								glyph.validProperty().bind(tableProperty().isEqualTo(field.tableProperty()));
								if (getTable() != null)
									glyph.validProperty().set(getTable().hasField(field));
							}
							
							fireInvalidationEvent();
						}
				});
			}
		});
	}
	
	private void fireInvalidationEvent() {
		for (InvalidationListener listener : _listeners) {
			listener.invalidated(DropArea.this);
		}
	}
	
	private void init() {
		fieldsProperty().set(FXCollections.<Field>observableArrayList());
	}

	private DnD.LocalClipboard getLocalClipboard() {
		return DnD.getInstance().getLocalClipboard();
	}
	
	/* Name: isPreOccupiedField
	 * Parameter: Field - the dragged field to test.
	 * Parameter: event - the current drag event.
	 * Returns: Boolean. Returns true if the field already exists in another drop area, false if not.
     * Description: Checks if the specified field is already used by other drop areas.
     */
	private Boolean isPreOccupiedField(Field testedField){
		
		//Check if the field was dragged from another drop area.
		//If it came from another drop area - if the transfer mode is "Move" the source drop area is going to remove the field anyway at the end of the drag and drop.
		//So no need to worry about a duplicate field.
		Class<?> key = getLocalClipboard().getType(DnD.DnD_SOURCE_FORMAT);
	    if(key != null && key.equals(DropArea.class)){
	    	TransferMode[] transferMode = _sourcesTransferModes.get(key);
	    	if(transferMode.length == 1 && transferMode[0].equals(TransferMode.MOVE)){
	    		return false;
			}
	    }
	    
	    //In any other case - check for duplicate fields in the other drop areas.
	    for(Field field : _preOccupiedFields){
			if(testedField.getName().equals(field.getName()) && testedField.getTable().getName().equals(field.getTable().getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public void addListener(InvalidationListener listener) {
		if (!_listeners.contains(listener))
			_listeners.add(listener);
		
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		_listeners.remove(listener);
	}
}
