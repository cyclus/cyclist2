package edu.utah.sci.cyclist.core.ui.views;


import java.text.DecimalFormat;
import java.util.Comparator;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.NumberStringConverter;

import org.apache.log4j.Logger;
import org.controlsfx.control.RangeSlider;

import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.model.Field;
import edu.utah.sci.cyclist.core.model.Filter;
import edu.utah.sci.cyclist.core.model.Range;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.ui.components.Spring;
import edu.utah.sci.cyclist.core.ui.panels.TitledPanel;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;

public class FilterPanel extends TitledPanel {
	static Logger log = Logger.getLogger(FilterPanel.class);
	
	private Filter _filter;
	private ListProperty<Object> _valuesProperty = new SimpleListProperty<>();
	private ObjectProperty<Range> _valueRangeProperty = new SimpleObjectProperty<>();
	private VBox _cbBox;
	private ProgressIndicator _indicator;
	private Button _closeButton;
	private Task<?> _task;
	private boolean _reportChange = true;
	private BooleanProperty _highlight = new SimpleBooleanProperty(false);
//	MapProperty<Object, Object> _map = new SimpleMapProperty<>();
	private RangeSlider _rangeSlider;

	private ObjectProperty<EventHandler<ActionEvent>> _closeAction = new SimpleObjectProperty<>();
			
	
	public BooleanProperty highlight() {
		return _highlight;
	}
	
	public void setHighlight(boolean value) {
		_highlight.set(value);
	}
	
	public boolean getHighlight() {
		return _highlight.get();
	}
	
	public FilterPanel(Filter filter) {
		//super(filter.getName());
		super(getTitle(filter), GlyphRegistry.get(AwesomeIcon.FILTER));//"FontAwesome|FILTER"));
		_filter = filter;
		build();
	}
	
	public Filter getFilter() {
		return _filter;
	}
	
	public void setTask(Task<?> task) {
		if (_task != null && _task.isRunning()) {
			_task.cancel();
		}
		
		_task = task;
		
		if (_task == null) {
			_indicator.visibleProperty().unbind();
			_indicator.setVisible(false);
		} else {
			_indicator.visibleProperty().bind(_task.runningProperty());	
			_indicator.setOnMouseClicked(new EventHandler<Event>() {
				
				@Override
				public void handle(Event event) {
					log.info("Canceling task: "+_task.cancel());				
				}
			});
		}
	}
	
	/*
	 * Close 
	 */
	public ObjectProperty<EventHandler<ActionEvent>> onCloseProperty() {
		return _closeAction;
	}
	
	public EventHandler<ActionEvent> getOnClose() {
		return _closeAction.get();
	}
	
	public void setOnClose(EventHandler<ActionEvent> handler) {
		_closeAction.set(handler);
	}
	
	private void build() {
		setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
		
		final HBox header = getHeader();
		
		_indicator = new ProgressIndicator();
		_indicator.setProgress(-1);
		_indicator.setMaxWidth(20);
		_indicator.setMaxHeight(20);
		_indicator.setVisible(false);
				
		_closeButton = new Button();
		_closeButton.getStyleClass().add("flat-button");
		_closeButton.setGraphic(GlyphRegistry.get(AwesomeIcon.TIMES));//"FontAwesome|TIMES"));
		
		_closeButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				setTask(null);
				if(getOnClose() != null){
					getOnClose().handle(e);
				}
			}
		});
	
		header.getChildren().addAll(_indicator, new Spring(), _closeButton);		
		
		header.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Dragboard db = header.startDragAndDrop(TransferMode.MOVE);
				
				DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
				clipboard.put(DnD.FILTER_FORMAT, Filter.class, _filter);
				
				ClipboardContent content = new ClipboardContent();
				content.putString(_filter.getName());
				
				SnapshotParameters snapParams = new SnapshotParameters();
	            snapParams.setFill(Color.TRANSPARENT);
	            
	            content.putImage(header.getChildren().get(0).snapshot(snapParams, null));	
				
				db.setContent(content);
			}
		});
		
		header.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				_highlight.set(!_highlight.get());
			}
		});
		
		_highlight.addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean oldValue, Boolean newValue) {
				if (newValue)
					getHeader().setStyle("-fx-background-color: #beffbf");
				else
					getHeader().setStyle("-fx-background-color: #e0e0ef");
				
			}
		});
		
		configure();
		_filter.setOnDatasouceChanged((Void)->configure());	
		_filter.setOnChanged((Void)->configure());
	}
	
	private void configure() {
//		System.out.println("FilterPaneL: configure");
		if (_filter.isRange())	
			createRange();
		else
			createList();
	}
	
	private void createList() {
		_cbBox = new VBox();
		_cbBox.setSpacing(4);
		_cbBox.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
		
		setContent(_cbBox);
		_valuesProperty.addListener(new InvalidationListener() {
			
			@Override
			public void invalidated(Observable arg0) {
//				System.out.println("FilterPanel: valuesProperty invalidated. call populateValues");
				populateValues();	
			}
		});
		
//		System.out.println("FilterPanel: bind");
		_valuesProperty.bind(_filter.valuesProperty());		
//		System.out.println("FilterPanel: after bind");
		
		if (!_filter.isValid()) {
//			System.out.println("FilterPanel: filter is not valid. Fetch data");
			Field field = _filter.getField();
			Table table = field.getTable();
			
			Task<ObservableList<Object>> task = table.getFieldValues(_filter.getDatasource(),field);
			setTask(task);
//			task.valueProperty().addListener(l->{
//				
//			});
			field.valuesProperty().bind(task.valueProperty());
			Thread th = new Thread(task);
			th.setDaemon(true);
			th.start();
			
		} else {
//			System.out.println("FilterPanel: filter is valid: populateValues()");
			populateValues();
		}
		
		_filter.selectedItems().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				_reportChange = false;
				
				boolean all = true;
				int n = _cbBox.getChildren().size();
				for (int i=1; i<n; i++) {
					CheckBox cb = (CheckBox)_cbBox.getChildren().get(i);
					boolean selected = _filter.selectedItems().contains(cb.getUserData());
					cb.setSelected(selected);
					if (!selected) all = false;
				}
				if (n > 0) {
    				CheckBox cbAll = (CheckBox)_cbBox.getChildren().get(0);
    				cbAll.setSelected(all);
				}
				
				_reportChange = true;
			}
		});
	}
	
	private void populateValues() {
		_cbBox.getChildren().clear();
		if (_valuesProperty.get() != null) {
			_cbBox.getChildren().add(createAllEntry(_filter.selectedItems().size() == _filter.getValues().size()));
			SortedList<Object> sorted = new SortedList<Object>(_valuesProperty.get(), new Comparator<Object>() {
				@Override
                public int compare(Object o1, Object o2) {
					if (o1 == null || o2 == null) {
						return o1 != null ? -1 :o2 != null ? 1 : 0;
					}	
					return o1.toString().compareToIgnoreCase(o2.toString());
                }
			});
			for (Object item: sorted) {
//				System.out.println("FilterPanel: populate value:"+item.toString());
				if (item != null)
					_cbBox.getChildren().add(createEntry(item));
			}
		}
	}
	
	private Node createAllEntry(boolean on) {
		CheckBox cb = new CheckBox("All");
		cb.setSelected(on);
		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean oldValue, Boolean newValue) {
				if (_reportChange) {
    				_reportChange = false;
    				for (Node node : _cbBox.getChildren()) {
    					((CheckBox) node).setSelected(newValue);
    				}
    				_reportChange = true;
    				_filter.selectAll(newValue);
				}
			}
		});
		return cb;
	}
	
	private Node createEntry(final Object item) {
		CheckBox cb = new CheckBox(item.toString());
		cb.setSelected(_filter.isSelected(item));
		cb.setUserData(item);
		cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean oldValue, Boolean newValue) {
				if (_reportChange) {
					_filter.selectValue(item, newValue);
				}
			}
		});
		return cb;
	}
	
	
	/* Name: createRange
	 * For numerical Fields - get the possible minimum and maximum values of the field 
	 * and creates a filter within that range */
	private void createRange() {
		
		if (_rangeSlider != null) {
			Range range = _filter.getValueRange();
			_rangeSlider.setMin(0); //range.min);
			_rangeSlider.setMax(range.max);
			Range selected = _filter.getSelectedRange();
			_rangeSlider.setLowValue(selected.min);
			_rangeSlider.setHighValue(selected.max);
			double majorTicks = range.max/5; //(_rangeSlider.getMax()-_rangeSlider.getMin())/4;
			if(majorTicks > 0){
				_rangeSlider.setMajorTickUnit(majorTicks);
			}
			return;
		}
		double min  = 0;
		double max = 100;
		
		_cbBox = new VBox();
		_cbBox.setSpacing(4);
		_cbBox.setPrefSize(USE_COMPUTED_SIZE,USE_COMPUTED_SIZE);
		setContent(_cbBox);
		_cbBox.setPadding(new Insets(0,8,0,8));
		
		_rangeSlider = new RangeSlider(min, max, min, max);
		_rangeSlider.setShowTickLabels(true);
		_rangeSlider.setShowTickMarks(true);
		//rangeSlider.setOrientation(Orientation.VERTICAL);
		
		double currentWidth = this.widthProperty().doubleValue();
		_cbBox.setPrefWidth(0.95*currentWidth);
		
		double majorTicks = (_rangeSlider.getMax()-_rangeSlider.getMin())/4;
		if(majorTicks > 0){
			_rangeSlider.setMajorTickUnit(majorTicks);
		}
		
		final HBox hbox = new HBox();
		hbox.setSpacing(20);
		final TextField minTxt = new TextField();
		final TextField maxTxt = new TextField();
		hbox.getChildren().addAll(minTxt, maxTxt);
		
		hbox.setPrefWidth(currentWidth);
		hbox.setSpacing(currentWidth-120);
		
		minTxt.setPrefSize(80, 18);
		maxTxt.setPrefSize(80, 18);
		minTxt.setEditable(true);
		maxTxt.setEditable(true);
		minTxt.setAlignment(Pos.CENTER_LEFT);
		maxTxt.setAlignment(Pos.CENTER_LEFT);
		minTxt.setText(Double.toString(min));
		maxTxt.setText(Double.toString(max));

		_cbBox.getChildren().addAll(_rangeSlider,hbox);
		
		//Listen to the width property of the parent - so the slider width can be changed accordingly.
		this.widthProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, 
                    Number oldValue, Number newValue) {
					double width = newValue.doubleValue();
				    _cbBox.setPrefWidth(0.95*width);
			}
		});
		
		_cbBox.widthProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, 
                    Number oldValue, Number newValue) {
					double width = newValue.doubleValue();
				    hbox.setPrefWidth(width);
				    hbox.setSpacing(width-160);
			}
		});
			
		
		// Change the filter's values according to the user new choice.
		// Must use user's actions events since using the RangeSlider lowValueProperty and highValueProperty
		// are changing too rapidly when sliding, and it causes a database driver exception.
		_rangeSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				_filter.selectRange(new Range(_rangeSlider.getLowValue(), _rangeSlider.getHighValue()));
			}
		}); 
		
		minTxt.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if(!minTxt.getText().isEmpty()){
					_filter.selectRange(new Range(_rangeSlider.getLowValue(), _rangeSlider.getHighValue()));
				}
			}
		});
		
		maxTxt.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if(!maxTxt.getText().isEmpty()){
					_filter.selectRange(new Range(_rangeSlider.getLowValue(), _rangeSlider.getHighValue()));
				}
			}
		});
		
		// Update the text field to show the current value on the slider. and vice versa
		Bindings.bindBidirectional(minTxt.textProperty(), _rangeSlider.lowValueProperty(), new EmptyStrNumberStringConverter("0.####E00"));
		Bindings.bindBidirectional(maxTxt.textProperty(), _rangeSlider.highValueProperty(), new EmptyStrNumberStringConverter("0.####E00"));
	
		_valueRangeProperty.addListener(new InvalidationListener() {	
			@Override
			public void invalidated(Observable arg0) {
				populateRangeValues();
			}
		});
		
		_valueRangeProperty.bind(_filter.valueRangeProperty());
		
		if (!_filter.isRangeValid()) {
			final Field field = _filter.getField();
			Table table = field.getTable();
			
			Task<ObservableValue<Range>> task = table.getFieldRange( _filter.getDatasource(), field);
			setTask(task);
			task.valueProperty().addListener(new ChangeListener<ObservableValue<Range>>() {

				@Override
                public void changed(
                        ObservableValue<? extends ObservableValue<Range>> observable,
                        ObservableValue<Range> oldValue,
                        ObservableValue<Range> newValue) {
					if (newValue != null) {
						field.setValueRange(newValue.getValue());
	                }	                
                }
			});
		}
		else{
			populateRangeValues();
		}
		
		_filter.valueRangeProperty().addListener(new InvalidationListener() {		
			@Override
			public void invalidated(Observable observable) {
				Range range = _filter.getValueRange();
				double min = (double) range.min;
				double max = (double) range.max;
				_rangeSlider.setMin(min);
				_rangeSlider.setMax(max);
			}
		});
	}
	
	/* Name: populateRangeValues
	 * Populates a filter panel for a "measure" type filter.
	 * In this case there would be a range slider which displays the possible values in the range 
	 * and the user can change the minimum and maximum values which are displayed */
	private void populateRangeValues() {
		Range range = _filter.getValueRange();
		log.debug("FilterPanel populateValueRange "+range.min+","+range.max);
		if (range.min > range.max)
			range = new Range(0, 100);
		_rangeSlider.setMin((double) range.min); 
		_rangeSlider.setMax((double) range.max);
	}		
	
	/* Name: getTitle
	 * Gets the filter name and function (if exists).
	 * If the filter is connected to a field, and the field has an aggregation function
	 * sets the filter panel header to display both the filter name and the function
	 */
	
	
	private static String getTitle(Filter filter) {
//		String title = null;
//		if (filter.getRole() == Role.DIMENSION || filter.getRole() == Role.INT_TIME) {
//			title = filter.getName();
//		} else {
//			String funcName = filter.getField().get(FieldProperties.AGGREGATION_FUNC, String.class);
//			if (funcName == null){
//				funcName = filter.getField().get(FieldProperties.AGGREGATION_DEFAULT_FUNC, String.class);
//				filter.getField().set(FieldProperties.AGGREGATION_FUNC, funcName);
//			}
//			SQL.Function func = SQL.getFunction(funcName);
//			if (func == null) 
//				title = filter.getName();
//			else
//				title = func.getLabel(filter.getName());
//		}
//		
//		return title;
		return filter.getName();
	}
	
	/*
	 * A converter from String to Number which handles the case when the string is empty.
	 */
	public class EmptyStrNumberStringConverter extends NumberStringConverter{
		
		DecimalFormat df = new DecimalFormat("#.##");
		
		public EmptyStrNumberStringConverter(String pattern) {
			super(pattern);
		}
		
		@Override
		public Number fromString(String value){
			if(!value.isEmpty()){
				return super.fromString(value);
			}else{
				return 0.0;
			}
		}
		
		@Override
		public String toString(Number value){
			if(value.doubleValue() > 9999){
				return super.toString(value);
			}else{
				return df.format(value);
			}
		}
	}
}
