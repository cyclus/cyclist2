package edu.utah.sci.cyclist.core.ui.views;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.converter.TimeStringConverter;

import javax.imageio.ImageIO;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.mo.closure.v1.Closure;

import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.controller.IMemento;
import edu.utah.sci.cyclist.core.event.Pair;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.event.ui.FilterEvent;
import edu.utah.sci.cyclist.core.model.Context;
import edu.utah.sci.cyclist.core.model.CyclistData;
import edu.utah.sci.cyclist.core.model.CyclistDatasource;
import edu.utah.sci.cyclist.core.model.DataType.Classification;
import edu.utah.sci.cyclist.core.model.DataType.Role;
import edu.utah.sci.cyclist.core.model.Field;
import edu.utah.sci.cyclist.core.model.Filter;
import edu.utah.sci.cyclist.core.model.Indicator;
import edu.utah.sci.cyclist.core.model.Simulation;
import edu.utah.sci.cyclist.core.model.Table;
import edu.utah.sci.cyclist.core.model.TableRow;
import edu.utah.sci.cyclist.core.model.proxy.TableProxy;
import edu.utah.sci.cyclist.core.ui.components.CyclistAxis;
import edu.utah.sci.cyclist.core.ui.components.CyclistViewBase;
import edu.utah.sci.cyclist.core.ui.components.DistanceIndicator;
import edu.utah.sci.cyclist.core.ui.components.DropArea;
import edu.utah.sci.cyclist.core.ui.components.LineIndicator;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import edu.utah.sci.cyclist.core.ui.panels.SchemaPanel;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;
import edu.utah.sci.cyclist.core.util.QueryBuilder;

public class ChartView extends CyclistViewBase {
	public static final String TITLE = "Plot";
	static Logger log = Logger.getLogger(ChartView.class);

	public static Double CYCLUS_INFINITY = 1e50;
	
	enum ViewType { CROSS_TAB, BAR, LINE, SCATTER_PLOT, GANTT, NA }

	enum MarkType { TEXT, BAR, LINE, SHAPE, GANTT, NA }

	public static double CYCLIST_MAX_VALUE = 1e90;
	
	private boolean _active = true;

	private TableProxy _tableProxy = null;
	
	private ObjectProperty<XYChart<?,?>> _chartProperty = new SimpleObjectProperty<>();
	
	@SuppressWarnings("rawtypes")
    private Axis _xAxis = null;
	
	@SuppressWarnings("rawtypes")
    private Axis _yAxis = null;
	private ObjectProperty<CyclistAxis.Mode> _xAxisMode = new SimpleObjectProperty<>(CyclistAxis.Mode.LINEAR);
	private ObjectProperty<CyclistAxis.Mode> _yAxisMode = new SimpleObjectProperty<>(CyclistAxis.Mode.LINEAR);
	private BooleanProperty _xForceZero = new SimpleBooleanProperty(false);
	private BooleanProperty _yForceZero = new SimpleBooleanProperty(false);
	
	private ObservableList<Indicator> _indicators = FXCollections.observableArrayList();
	private Map<Indicator, LineIndicator> _lineIndicators = new HashMap<>();
	private List<DistanceIndicator> _distanceIndicators = new ArrayList<>();
	private Set<MultiKey> _seriesWithInfinity = new HashSet<MultiKey>();
	private Closure.V0 _onDuplicate = null;
	
	private Spec _currentSpec = null;

	private BorderPane _pane;
	private DropArea _xArea;
	private DropArea _yArea;
	private DropArea _lodArea;
	private Text _warningText = new Text();
	private Text _warningLabel = new Text("Warning");

	private ObjectProperty<Table> _currentTableProperty = new SimpleObjectProperty<>();

	private StackPane _stackPane;
	private Pane _glassPane;

	public ChartView() {
		super();
		build();
		setChart(null);
	}

	@Override 
	public ViewBase clone() {
		ChartView copy = new ChartView();
		copy.setActive(false);

		return copy;
	}

	public void copy(ChartView other) {
		for (Field field : other._xArea.getFields()) {
			_xArea.getFields().add(field.clone());
		}

		for (Field field : other._yArea.getFields()) {
			_yArea.getFields().add(field.clone());
		}

		for (Field field : other._lodArea.getFields()) {
			_lodArea.getFields().add(field.clone());
		}

		getFiltersArea().copy(other.getFiltersArea());
	
		_xAxisMode.set(other._xAxisMode.get());
		_xForceZero.set(other._xForceZero.get());
		_yAxisMode.set(other._yAxisMode.get());
		_yForceZero.set(other._yForceZero.get());
		}

	public void setActive(boolean state) {
		_active = state;
		if (_active) {
			//invalidateChart();
			fetchData();
		}
	}

	public void setOnDuplicate(Closure.V0 action) {
		_onDuplicate = action;
	}


	public ObjectProperty<XYChart<?,?>> chartProperty() {
		return _chartProperty;
	}

	@SuppressWarnings("rawtypes")
    public XYChart getChart() {
		return _chartProperty.get();
	}

	public void setChart(XYChart<?,?> chart) {
		_chartProperty.set(chart);
	}
	public Table getCurrentTable() {
		return _currentTableProperty.get();
	}
	
	public void updateSimulationData(){
		fetchData();
	}
	
	public void removeSimulationData(){
		releaseChart();
	}

	@Override
	public void selectTable(Table table, boolean active) {
		super.selectTable(table, active);

		if (!active) {
			if (table == getCurrentTable()) 
				releaseChart();
			return;
		}

		if (table != getCurrentTable()) {
			_tableProxy = new TableProxy(table);
			//invalidateChart();
			_currentTableProperty.set(table);
		}

		fetchData();
	}

	@Override
	public void selectSimulation(Simulation sim, boolean active) {
		super.selectSimulation(sim, active);	
		updateFilters();
	}
	
	@Override
	public void save(IMemento memento) {
		if (_xArea.getFields().size() > 0) {
			IMemento xMemento = memento.createChild("xArea");
			for (Field f : _xArea.getFields()) {
				f.save(xMemento.createChild("field"));
			}
		}
		if (_yArea.getFields().size() > 0) {
			IMemento yMemento = memento.createChild("yArea");
			for (Field f : _yArea.getFields()) {
				f.save(yMemento.createChild("field"));
			}
		}
		if (_lodArea.getFields().size() > 0) {
			IMemento lodMemento = memento.createChild("lod");
			for (Field f : _lodArea.getFields()) {
				f.save(lodMemento.createChild("field"));
			}
		}
		
		// options
		IMemento child = memento.createChild("axis-opt");
		IMemento x = child.createChild("x");
		x.putBoolean("mode", _xAxisMode.get() == CyclistAxis.Mode.LINEAR);
		x.putBoolean("force-zero", _xForceZero.get());
		
		IMemento y = child.createChild("y");
		y.putBoolean("mode", _yAxisMode.get() == CyclistAxis.Mode.LINEAR);
		y.putBoolean("force-zero", _yForceZero.get());
	}

	@Override
	public void restore(IMemento memento, Context ctx) {
		boolean wasActive = _active;
		_active = false;
		
		IMemento child = memento.getChild("xArea");
		if (child != null) {
			for (IMemento im : child.getChildren("field")) {
				Field field = new Field();
				field.restore(im, ctx);
				_xArea.getFields().add(field);
			}
		}
		
		child = memento.getChild("yArea");
		if (child != null) {
			for (IMemento im : child.getChildren("field")) {
				Field field = new Field();
				field.restore(im, ctx);
				_yArea.getFields().add(field);
			}
		}
		
		child = memento.getChild("lod");
		if (child != null) {
			for (IMemento im : child.getChildren("field")) {
				Field field = new Field();
				field.restore(im, ctx);
				_lodArea.getFields().add(field);
			}
		}
		
		// options
		child = memento.getChild("axis-opt");
		if (child != null) {
			IMemento x = child.getChild("x");
			_xAxisMode.set( x.getBoolean("mode") ? CyclistAxis.Mode.LINEAR : CyclistAxis.Mode.LOG);
			_xForceZero.set( x.getBoolean("force-zero"));
			
			IMemento y = child.getChild("y");
			_yAxisMode.set( y.getBoolean("mode") ? CyclistAxis.Mode.LINEAR : CyclistAxis.Mode.LOG);
			_yForceZero.set( y.getBoolean("force-zero"));
		}
		
		if (wasActive)
			setActive(true);
	}
	
	private Field getXField() {
		return _xArea.getFields().get(0);
	}

	private Field getYField() {
		return _yArea.getFields().get(0);
	}

	private void updateFilters() {
		
		CyclistDatasource ds = getAvailableDatasource();
		
		if (ds != null)
		for (Filter filter : filters()) {
			filter.setDatasource(ds);
		}
	}

	private void fetchData() {
		final Spec spec = determineSpec();
		
		if (!spec.valid) {
			releaseChart();
			return;
		}
		
		QueryBuilder builder = createBuilder();
		if (builder == null) {
			releaseChart();
			return;
		}
		
		List<Field> order = builder.getOrder();

		for (Field field : _xArea.getFields()) {
			spec.xFields.add(new FieldInfo(field, order.indexOf(field)));
		}
		for (Field field : _yArea.getFields()) {
			spec.yFields.add(new FieldInfo(field, order.indexOf(field)));
		}

		for (Field field : _lodArea.getFields()) {
			spec.lod.add(new FieldInfo(field, order.indexOf(field)));
		}
		
        Simulation currentSim = getCurrentSimulation();
		CyclistDatasource ds = currentSim != null ? currentSim.getDataSource() : null;
		
		Task<ObservableList<TableRow>> task = new Task<ObservableList<TableRow>>() {
			@Override
			protected ObservableList<TableRow> call() throws Exception {
				return _tableProxy.getRows(ds, builder.toString());
			}
		};
		
		task.valueProperty().addListener( o->{
			ObjectProperty<ObservableList<TableRow>> op = (ObjectProperty<ObservableList<TableRow>>) o;			
			ObservableList<TableRow> data = op.get();
			if (data != null) {
				processData(data, spec);
			}
		});
		
		setCurrentTask(task);
		
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}
	
	private QueryBuilder createBuilder() {
		List<Field> fields = new ArrayList<>();
		List<Field> aggregators = new ArrayList<>();
		List<Field> grouping = new ArrayList<>();

		Table table = getCurrentTable();

		int n = 0;
		for (Field field : _xArea.getFields()) {
			if (table.hasField(field)) {
				if (field.getRole() == Role.DIMENSION || field.getRole() == Role.INT_TIME)
					fields.add(field);
				else
					aggregators.add(field);
				n++;
			}
		}

		// is there at least one valid x field? 
		if (n == 0) return null;

		n=0;
		for (Field field : _yArea.getFields()) {
			if (table.hasField(field)) {
				if (field.getRole() == Role.DIMENSION || field.getRole() == Role.INT_TIME)
					fields.add(field);
				else
					aggregators.add(field);
				n++;
			}
		}

		// is there at least one valid y field?
		if (n == 0) return null;

		for (Field field : _lodArea.getFields()) {
			if (table.hasField(field)) {
				grouping.add(field);
			}
		}

		List<Filter> filtersList = new ArrayList<Filter>();

		//Check the filters current validity
		for(Filter filter : filters()){
			if(getCurrentTable().hasField(filter.getField())){
				filtersList.add(filter);
			}
		}

		//Check the remote filters current validity
		for(Filter filter : remoteFilters()){
			if(getCurrentTable().hasField(filter.getField())){
				filtersList.add(filter);
			}
		}

		// build the query
		QueryBuilder builder = 
				getCurrentTable().queryBuilder()
				.fields(fields)
				.aggregates(aggregators)
				.grouping(grouping)
				.filters(filtersList);

		return builder;
	}
		
	private void processData(List<TableRow> data, Spec spec) {
		if (!compatible(spec, _currentSpec)) {
			releaseChart();
			setChart(createChart(spec), spec);
		}		
		assignData(data, spec);
	}
		
	private boolean compatible(Spec spec, Spec current) {
		return current != null 
				&& spec.type == current.type 
				&& compatible(spec.x, current.x) 
				&& compatible(spec.y, current.y);
	}
	
	private boolean compatible(AxisSpec spec, AxisSpec current) {
		return spec.classification == current.classification
				|| (spec.classification == Classification.Qd 
					&& current.classification == Classification.Qi)
				|| (spec.classification == Classification.Qi 
						&& current.classification == Classification.Qd);
	}
	
	@SuppressWarnings("unchecked")
    private void assignData(List<TableRow> list, Spec spec) {
		log.debug("chart data has "+list.size()+" rows");
		_seriesWithInfinity.clear();
		if (list.size() == 0) {
			log.debug("no data");
			getChart().getData().clear();
			_currentSpec = spec;
			checkForInfinities(null);
			return;
		}
				

		Map<MultiKey, ObservableList<XYChart.Data<Object, Object>>> dataMap = split(list, spec);
		
		// remove current series that are not part of the new data
		for (MultiKey key : _currentSpec.seriesMap.keySet()) {
			if (!dataMap.containsKey(key)) {
				getChart().getData().remove(_currentSpec.seriesMap.get(key));
			}
		}
		
		List<XYChart.Series<Object, Object>> add = new ArrayList<>();
		
		for (MultiKey key : dataMap.keySet()) {							
			XYChart.Series<Object, Object> series = _currentSpec.seriesMap.get(key);
			if (series == null) {
				series = new XYChart.Series<Object, Object>();
				series.setName(createLabel(key));
				add.add(series);
			}
			ObservableList<XYChart.Data<Object, Object>> data = dataMap.get(key);
			series.setData(data);
			spec.seriesMap.put(key, series);			
		}
		spec.dataMap = dataMap;
		getChart().getData().addAll(add);
		getChart().setLegendVisible(spec.seriesMap.size() > 1);
		_currentSpec = spec;
		
		checkForInfinities(dataMap.keySet());
		
	}
	
	private void checkForInfinities(Collection<MultiKey> keys ) {
		String s = "";
		if (keys != null) {
			for (MultiKey bad : _seriesWithInfinity) {
				if (keys.contains(bad)) {
					s += createLabel(bad) + " ";
				}
			}
		}

		if (s.equals("")) {
			_warningLabel.setVisible(false);
			_warningText.setText("");
		}
		else {
			s = s.equals(" ") ? "Infinity values removed" :
				("Infinity values removed from: "+s);		
			_warningText.setText(s);
			_warningLabel.setVisible(true);
		}
	}
	
	private String createLabel(MultiKey key) {
		// key = x, y, attributes
		int n =key.size();
		
		if (n==2) return "";
		if (n==3) return key.getKey(2).toString();

		StringBuilder builder = new StringBuilder("[").append(key.getKey(2));
		for (int i=3; i<n; i++)
			builder.append(",").append(key.getKey(i));
		builder.append("]");
		return builder.toString();
	}
	
	private Map<MultiKey, ObservableList<XYChart.Data<Object, Object>>> split(List<TableRow> list, Spec spec) {
		Map<MultiKey, ObservableList<XYChart.Data<Object, Object>>> map = new HashMap<>();

		int nx = spec.numX();
		int ny = spec.numY();
		int cols = spec.cols();

		for (FieldInfo xInfo : spec.xFields) {
			int ix = xInfo.index;
			Classification cx = xInfo.field.getClassification();

			for (FieldInfo yInfo : spec.yFields) {
				int iy = yInfo.index;
				Classification cy = yInfo.field.getClassification();

				for (TableRow row : list) {
					// pt
					XYChart.Data<Object, Object> pt = createPoint(row.value[ix], row.value[iy], cx, cy);
					
					// key
					Object [] index = Arrays.copyOfRange(row.value, nx+ny-2, cols); // copy two extra to the left. 
					index[0] = xInfo.field.getName();
					index[1] = yInfo.field.getName();
					
					MultiKey key = new MultiKey(index, false);
					
					ObservableList<XYChart.Data<Object, Object>> series = map.get(key);
					if (series == null) {
						series = FXCollections.observableArrayList();
						map.put(key, series);
					}
					
					if (pt == null) {
						_seriesWithInfinity.add(key); 
					} else {
						series.add(pt);
					}
				}
			}
		}
		return map;
	}
	
	
	private XYChart.Data<Object, Object> createPoint(Object x, Object y, Classification cx, Classification cy) {
		x = convert(x, cx);
		y = convert(y, cy);
		if (x == null || y == null) return null;
		XYChart.Data<Object, Object> pt = new XYChart.Data<Object, Object>(x, y);
		return pt;
	}

	NumberFormat numFormater = NumberFormat.getInstance();
	
	private Object convert(Object v, Classification c) {
		switch (c) {
		case C:
			if (v instanceof String) {
				// ignore
			} else if (v instanceof Number) {
				v = numFormater.format(v);
			} else {
				v = v.toString();
			}
			break;
		case Cdate:
			if (v.getClass() == Date.class) {
				v = ((Date)v).getTime();
			}
			break;
		case Qi:
		case Qd:
			if (v instanceof Number) {
				if (v instanceof Double && ((Double) v) > CYCLUS_INFINITY) return null;
			} else if (v instanceof CyclistData) {
				v = ((CyclistData)v).toNumber();
			} else {
				// Data can not be visualize 
				// Don't throw an exception
				v = 0;
			}
		}
		return v;
	}	

    private void releaseChart() {
		
    	// TODO: sometimes a Text msg gets left out. For now, remove all.
//		if (_stackPane.getChildren().size() > 1) {
//			_stackPane.getChildren().remove(0);
//		}
    	_stackPane.getChildren().clear();
		
    	setCurrentTask(null);
		if (getChart() == null) return;
		
		if (_xAxis instanceof NumberAxis) {
			((NumberAxis) _xAxis).forceZeroInRangeProperty().unbind();
		} else if (_xAxis instanceof CyclistAxis) {
			((CyclistAxis) _xAxis).mode().unbind();
			((CyclistAxis) _xAxis).forceZeroInRangeProperty().unbind();
		}
		
		if (_yAxis instanceof NumberAxis) {
			((NumberAxis) _yAxis).forceZeroInRangeProperty().unbind();
		} else  if (_yAxis instanceof CyclistAxis) {
			((CyclistAxis) _yAxis).mode().unbind();
			((CyclistAxis) _yAxis).forceZeroInRangeProperty().unbind();
		}
		
		setChart(null, null);
	}
	
    
	class AxisSpec {
		Classification classification;
		Role role;
		BooleanProperty forceZero;
		ObjectProperty<CyclistAxis.Mode> mode;
		String label;
	}
	
	class Spec {
		AxisSpec x = new AxisSpec();
		AxisSpec y = new AxisSpec();
		
		List<FieldInfo> xFields = new ArrayList<>();
		List<FieldInfo> yFields = new ArrayList<>();
		List<FieldInfo> lod = new ArrayList<>();
		
		ViewType type = ViewType.NA;
		int cols;	
		boolean valid = true;
		
		public int numX() { return xFields.size(); }
		public int numY() { return yFields.size(); }
		public int cols() { return numX() + numY() +lod.size(); }
		
		Map<MultiKey, XYChart.Series<Object, Object>> seriesMap = new HashMap<>();
		Map<MultiKey, ObservableList<XYChart.Data<Object, Object>>> dataMap = new HashMap<>();
	}
	
	private Spec determineSpec() {
		Spec spec = new Spec();
		
		spec.valid =
				_active &&
				getCurrentTable() != null &&
				_xArea.getFields().size() == 1 && 
				_yArea.getFields().size() > 0 &&
				_xArea.isValid() &&
				_yArea.isValid();
				
		if (spec.valid) {
    		spec.x.classification = getXField().getClassification();
    		spec.x.role = getXField().getRole();
    		spec.x.forceZero = _xForceZero;
    		spec.x.mode = _xAxisMode;
    		spec.x.label = _xArea.getFieldTitle(0);
    		
    		spec.y.classification = getYField().getClassification();
    		spec.y.role = getYField().getRole();
    		spec.y.forceZero = _yForceZero;
    		spec.y.mode = _yAxisMode;
    		spec.y.label = _yArea.getFieldTitle(0);
    		
    		spec.type = determineViewType(spec.x.classification, spec.y.classification);
    		switch (spec.type) {
    		case CROSS_TAB:
    		case GANTT:
    		case NA:
    			spec.valid = false;
    			break;
    		case BAR:
    		case LINE:
    		case SCATTER_PLOT:
    			spec.valid = true;
    			break;
    		}
		}	
		return spec;
	}
	
	private boolean match(Classification x, Classification y, Classification c1, Classification c2) {
		return (x == c1 && y == c2) || (x==c2 && y==c1); 
	}
	
	private ViewType determineViewType(Classification x, Classification y) {
		ViewType type;
		if (match(x, y, Classification.C, Classification.C)) {
			type = ViewType.CROSS_TAB;
		} else if (match(x, y, Classification.Qd, Classification.C)) {
			type = ViewType.BAR;
		} else if (match(x, y, Classification.Qd, Classification.Cdate)) {
			type = ViewType.LINE;
		} else if (match(x, y, Classification.Qd, Classification.Qd)) {
			type = ViewType.SCATTER_PLOT;
		} else if (match(x, y, Classification.Qi, Classification.C)) {
			type = ViewType.BAR;
		} else if (match(x, y, Classification.Qi, Classification.Qd)) {
			type = ViewType.LINE;
		} else if (match(x, y, Classification.Qi, Classification.Qi)) {
			type = ViewType.SCATTER_PLOT;
		} else if (match(x, y, Classification.C, Classification.Cdate)) {
			type = ViewType.SCATTER_PLOT;
		} else {
			type = ViewType.NA;
		}
		return type;
	}
	
	private void setChart(XYChart<?, ?> chart, Spec spec) {
		// for now ensure there are no other children
		_stackPane.getChildren().clear();
		if (chart != null) {
			_stackPane.getChildren().add(0, chart);
			_currentSpec = spec != null? spec : new Spec();
			setChart(chart);
		} else {
			Text text = new Text("No data");
			_stackPane.getChildren().add(0, text);			
			_currentSpec = new Spec();
		}
	}
	
    private XYChart<?, ?> createChart(Spec spec) {
		Axis<?> x = createAxis(spec.x);
		Axis<?> y = createAxis(spec.y);
		XYChart<?, ?> chart = null;
		
		switch (spec.type) {
		case CROSS_TAB:
			break;
		case BAR:
			BarChart<?, ?> bar = new BarChart<>(x, y);
			bar.setBarGap(1);
			bar.setCategoryGap(4);
			chart = bar;
			break;
		case LINE:
			LineChart<?,?> lineChart = new LineChart<>(x, y);
			lineChart.setCreateSymbols(false);
			lineChart.getStyleClass().add("line-chart");
			chart = lineChart;
			break;
		case SCATTER_PLOT:
			chart = new ScatterChart<>(x, y);
			break;
		case GANTT:
			break;
		case NA:
		}
		
		if (chart != null) {
			chart.setAnimated(false);
			chart.setHorizontalZeroLineVisible(false);
			chart.setVerticalZeroLineVisible(false);
		}
		return chart;
	}
	
    private Axis<?> createAxis(AxisSpec spec) {
        Axis<?> axis = null;
		
		switch (spec.classification) {
		case C:
			axis = new CategoryAxis();
			break;
		case Cdate:
			NumberAxis cd = new NumberAxis();
			cd.forceZeroInRangeProperty().bind(spec.forceZero);
			if(spec.role!= Role.INT_TIME) {
				NumberAxis.DefaultFormatter f = new NumberAxis.DefaultFormatter(cd) {
					TimeStringConverter converter = new TimeStringConverter("dd-MM-yyyy");
					@Override
					public String toString(Number n) {
						return converter.toString(new Date(n.longValue()));
					}
				};
				cd.setTickLabelFormatter(f);
			}
			axis = cd;
			break;
		case Qd:
		case Qi:
			CyclistAxis ca = new CyclistAxis();
			ca.forceZeroInRangeProperty().bind(spec.forceZero);
			ca.mode().bind(spec.mode);
			axis = ca;
			break;
		}
		
		axis.setLabel(spec.label);
		return axis;
	}
	
	long t0;

	private void build() {
		setTitle(TITLE);

		// main view
		_pane = new BorderPane();
		_pane.setPrefSize(600, 300);

		Rectangle clip = new Rectangle(0, 0, 100, 100);
		clip.widthProperty().bind(_pane.widthProperty());
		clip.heightProperty().bind(_pane.heightProperty());
		_pane.setClip(clip);

		// Glass pane for indicators
		_glassPane = new Pane();
		// _glassPane.setStyle("-fx-background-color: rgba(200, 200, 200, 0.1)");
		setupGlassPaneListeners();

		_stackPane = new StackPane();
		_stackPane.getChildren().add(_glassPane);

		_pane.setCenter(_stackPane);
		_pane.setBottom(createControl());

		setContent(_pane);

		// add actions
		setupActions();

		_indicators.addListener(new ListChangeListener<Indicator>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Indicator> change) {
				while (change.next()) {
					// remove
					for (Indicator indicator : change.getRemoved()) {
						LineIndicator lineIndicator = _lineIndicators.remove(indicator);
						if (lineIndicator != null) {
							_glassPane.getChildren().remove(lineIndicator.getNode());
						}
					}

					// add
					for (final Indicator indicator : change.getAddedSubList()) {
						final LineIndicator li = new LineIndicator(indicator, _glassPane);
						li.chartProperty().bind(chartProperty());
						li.setOnRemoveAction(new Closure.V1<LineIndicator>() {

							@Override
							public void call(LineIndicator li) {
								_indicators.remove(li.getIndicator());
							}

						});
						_lineIndicators.put(indicator, li);
						indicator.selectedProperty().addListener( new ChangeListener<Boolean>() {

							@Override
							public void changed(ObservableValue<? extends Boolean> arg0,
									Boolean prevValue, Boolean select) {
								if (select) {
									showDistances(indicator);
								} else {
									clearDistances();                                                                                
								}
							}
						});
					}
				}
			}
		});

		filters().addListener(new ListChangeListener<Filter>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Filter> change) {
				boolean update = false;
				
				CyclistDatasource ds = getAvailableDatasource();
				
				while (change.next()) {
					for (Filter f : change.getRemoved()) {
						update = update || f.isActive();
						f.removeListener(_filterListener);
					}
					for (Filter f : change.getAddedSubList()) {
						//For a new filter - add the current data source as its data source.
						f.setDatasource(ds);
							
						update = update || f.isActive();
						f.addListener(_filterListener);
					}
				}

				if (update) {
					//invalidateChart();
					fetchData();
				}
			}
		});

		remoteFilters().addListener(new ListChangeListener<Filter>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Filter> change) {
				while (change.next()) {
					for (Filter filter : change.getRemoved()) {
						filter.removeListener(_filterListener);
					}
					for (Filter filter : change.getAddedSubList()) {
						filter.addListener(_filterListener);
					}
				}
				//invalidateChart();
				fetchData();
			}
		});		
	}

	private void setupActions() {
		List<Node> actions = new ArrayList<>();
		actions.add(createAxisOptions());
		actions.add(createExportActions());
		actions.add(createOptions());
		addActions(actions);
	}
	
	private Node createAxisOptions() {
		final Button btn = new Button("Axis", GlyphRegistry.get(AwesomeIcon.CARET_DOWN));
		btn.getStyleClass().add("flat-button");
		
		final ContextMenu menu = new ContextMenu();
		btn.setOnMousePressed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				menu.show(btn, Side.BOTTOM, 0, 0);
			}
		});
		
		MenuItem item = new MenuItem("Y linear", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
            public void handle(ActionEvent event) {
				_yAxisMode.set(CyclistAxis.Mode.LINEAR);
            }
		});
		item.getGraphic().visibleProperty().bind(Bindings.equal(_yAxisMode, CyclistAxis.Mode.LINEAR));
		menu.getItems().add(item);
		
		item = new MenuItem("Y log", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
            public void handle(ActionEvent event) {
				_yAxisMode.set(CyclistAxis.Mode.LOG);
            }
		});
		item.getGraphic().visibleProperty().bind(Bindings.equal(_yAxisMode, CyclistAxis.Mode.LOG));
		menu.getItems().add(item);
		
		item = new MenuItem("Y force zero", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.getGraphic().visibleProperty().bind(_yForceZero);
		menu.getItems().add(item);
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				_yForceZero.set(!_yForceZero.get());
			}
		});

		
		menu.getItems().add(new SeparatorMenuItem());

		item = new MenuItem("X linear", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.getGraphic().visibleProperty().bind(Bindings.equal(_xAxisMode, CyclistAxis.Mode.LINEAR));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
            public void handle(ActionEvent event) {
				_xAxisMode.set(CyclistAxis.Mode.LINEAR);
            }
		});
		menu.getItems().add(item);

		item = new MenuItem("X log", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.getGraphic().visibleProperty().bind(Bindings.equal(_xAxisMode, CyclistAxis.Mode.LOG));
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
            public void handle(ActionEvent event) {
				_xAxisMode.set(CyclistAxis.Mode.LOG);
            }
		});
		menu.getItems().add(item);
		
		item = new MenuItem("X force zero", GlyphRegistry.get(AwesomeIcon.CHECK));
		item.getGraphic().visibleProperty().bind(_xForceZero);
		menu.getItems().add(item);
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				_xForceZero.set(!_xForceZero.get());
			}
		});

		return btn;
	}
	
	private Node createOptions() {
		final Button options = new Button("Options", GlyphRegistry.get(AwesomeIcon.CARET_DOWN));
		options.getStyleClass().add("flat-button");

		// create menu
		final ContextMenu contextMenu = new ContextMenu();

		MenuItem item = new MenuItem("Add indicator");
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				createIndicator();
			}
		});
		contextMenu.getItems().add(item);

		MenuItem duplicateItem = new MenuItem("Duplicate plot");
		duplicateItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (_onDuplicate != null) {
					_onDuplicate.call();
				}
			}
		});

		contextMenu.getItems().add(duplicateItem);

		options.setOnMousePressed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				contextMenu.show(options, Side.BOTTOM, 0, 0);
			}
		});

//		chartProperty().addListener(new InvalidationListener() {
//
//			@Override
//			public void invalidated(Observable observable) {
//				if(chartProperty().get() != null){
//					Axis<?> yAxis = chartProperty().get().getYAxis();
//					if (! (yAxis instanceof NumberAxis)){
//						forceZeroItem.setVisible(false);
//					} else{
//						forceZeroItem.setVisible(true);
//					}
//				}
//			}
//		});


//		forceZeroProperty().set(false);
		return options;
	}

	private Node createExportActions() {
		final ContextMenu contextMenu = new ContextMenu();
		
		// screen shot
		MenuItem item = new MenuItem("Plot");
		item.setOnAction(new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent event) {
				exportScreenshot();
			}
		});
		item.disableProperty().bind(Bindings.isNull(_chartProperty));
		contextMenu.getItems().add(item);
		
		// csv
		item = new MenuItem("csv");
		item.setOnAction(new EventHandler<ActionEvent>() {		
			@Override
			public void handle(ActionEvent event) {
				exportCSV();
			}
		});

		item.disableProperty().bind(Bindings.isNull(_chartProperty));
		contextMenu.getItems().add(item);
		
		final Button button = new Button("Export", GlyphRegistry.get(AwesomeIcon.CARET_DOWN));
		button.getStyleClass().add("flat-button");
		
		button.setOnMousePressed(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				contextMenu.show(button, Side.BOTTOM, 0, 0);
			}
		});
		
		
		return button;
	}
	
	private void exportScreenshot() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("Image file (png, jpg, gif)", "*.png", "*.jpg", "'*.gif") );
		File file = chooser.showSaveDialog(Cyclist.cyclistStage);
		if (file != null) {
			WritableImage image = _chartProperty.get().snapshot(new SnapshotParameters(), null);
			String name = file.getName();
			String ext = name.substring(name.indexOf(".")+1, name.length());
		    try {
		        ImageIO.write(SwingFXUtils.fromFXImage(image, null), ext, file);
		    } catch (IOException e) {
		        log.error("Error writing image to file: "+e.getMessage());
		    }
		}
	}
	
	private void exportCSV() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("CSV file (*.csv)", "*.csv") );
		File file = chooser.showSaveDialog(Cyclist.cyclistStage);
		if (file != null) {
			try {
	            FileWriter f = new FileWriter(file);
	            
	            if (_currentSpec.valid) {
    	            // header
    	            f.write(_currentSpec.xFields.get(0).field.getName());
    	            for (FieldInfo info : _currentSpec.yFields) {
    	            	f.write(", ");
    	            	f.write(info.field.getName());
    	            }
    	            for (FieldInfo info : _currentSpec.lod) {
    	            	f.write(",");
    	            	f.write(info.field.getName());
    	            }
    	            f.write("\n");
    	           
    	            if (_currentSpec.dataMap.size() > 0) {
    	            	for (MultiKey multikey : _currentSpec.dataMap.keySet()) {
    	            		StringBuilder sb = new StringBuilder();
    	            		for (int i=2; i<multikey.size(); i++) {
    	            			sb.append(",").append(multikey.getKey(i).toString());
    	            		}
    	            		String str = sb.append("\n").toString();
    	            		for (XYChart.Data<Object, Object> value : _currentSpec.dataMap.get(multikey)) {
    	            			f.write(value.getXValue().toString());
    	            			f.write(",");
    	            			f.write(value.getYValue().toString());
    	            			f.write(str);
    	            		}
    	            	}		
    	            }

	            }
    			f.close();
			} catch (IOException e) {
	            log.error("Error: Can not write to file ["+e.getMessage()+"]");
            }
		}
	}
	
	private void createIndicator() {
		Indicator indicator = new Indicator();

		if (getChart() != null) {
			Axis<?> axis = getChart().getXAxis();
			if (axis instanceof NumberAxis) {
				NumberAxis x = (NumberAxis) axis;
				double value = (x.getUpperBound() - x.getLowerBound())/2;
				indicator.valueProperty().set(value);
			}

		}
		_indicators.add(indicator);
	}

	private Node createControl() {
		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setPadding(new Insets(0, 0, 0, 0));

		grid.getColumnConstraints().add(new ColumnConstraints(10));
		ColumnConstraints cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		grid.getColumnConstraints().add(cc);

		//                grid.getColumnConstraints().add(new ColumnConstraints(17));

		grid.getColumnConstraints().add(new ColumnConstraints(50));
		cc = new ColumnConstraints();
		cc.setHgrow(Priority.SOMETIMES);
		grid.getColumnConstraints().add(cc);

		_xArea = createControlArea(grid, "X", 0, 0, 1, DropArea.Policy.SINGLE, DropArea.AcceptedRoles.ALL);
		_yArea = createControlArea(grid, "Y", 1, 0, 1, DropArea.Policy.MULTIPLE, DropArea.AcceptedRoles.ALL);
		_lodArea = createControlArea(grid, "Group by", 0, 2, 2, DropArea.Policy.MULTIPLE, DropArea.AcceptedRoles.DIMENSION);
		
		_warningLabel.setVisible(false);
		grid.add(_warningLabel, 2, 1);
		grid.add(_warningText, 3, 1);

		return grid;
	}

	/*
	 * Name: createDragAndDropModes
	 * Returns: Map<Class<?>, TransferMode[]>
	 * Description: Maps for each possible drag and drop source the accepted transfer modes.
	 */
	private Map<Class<?>, TransferMode[]> createDragAndDropModes() {
		Map<Class<?>, TransferMode[]> sourcesTransferModes = new HashMap<Class<?>, TransferMode[]>();
		sourcesTransferModes.put(DropArea.class, new TransferMode[]{TransferMode.MOVE});
		sourcesTransferModes.put(SchemaPanel.class, TransferMode.COPY_OR_MOVE);
		return sourcesTransferModes;
	}
	private void showDistances(Indicator selected) {
		clearDistances();

		Axis<?> axis = getChart().getXAxis();
		NumberAxis.DefaultFormatter formater = new NumberAxis.DefaultFormatter((NumberAxis)axis);
		LineIndicator current = _lineIndicators.get(selected);
		double y = 10;
		for (LineIndicator to : _lineIndicators.values()) {
			if (to.getIndicator() != selected) {
				DistanceIndicator di = new DistanceIndicator(current, to, y, formater);
				_distanceIndicators.add(di);
				_glassPane.getChildren().add(di);
				y += 10;
			}
		}
	}

	private void clearDistances() {
		for (DistanceIndicator di : _distanceIndicators)
			_glassPane.getChildren().remove(di);
		_distanceIndicators.clear();
	}

	private InvalidationListener _filterListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable o) {
			Filter filter = (Filter) o;		
			// LOD filters only show/hide data. No need to fetch new data 
			// TODO: is this true only for classification == C? Seems to be true for any field that is not range
			if(_currentSpec != null && isInLodArea(filter.getField()) && filter.getField().getClassification() == Classification.C ) {
//				invalidateLODFilters(filter);
				filter.setValid(true);
				reassignData(filter);
			} else {
				fetchData();
			}
		}
	};

	private InvalidationListener _areaListener = new InvalidationListener() {

		@Override
		public void invalidated(Observable observable) {                        
			DropArea area = (DropArea) observable;
			if (getCurrentTable() == null
				&& area.getFields().size() == 1
				&& getOnTableDrop() != null)
			{
				getOnTableDrop().call(area.getFields().get(0).getTable());
			}
			updatePreOccupiedFields(area);
			fetchData();
		}
	};


	/* Name: updatePreOccupiedFields
	 * This method prevents the same field to appear in more than one drop area.
	 * Each drop area gets the list of fields already used by the other drop areas.
	 * When there is a change in the fields of one of the drop areas, update all the other drop area with the updated fields list. */

	private void updatePreOccupiedFields(DropArea area)
	{
		if (area == _lodArea){
			List<Field> newList = new ArrayList<Field>(_lodArea.getFields());
			newList.addAll(_yArea.getFields());
			_xArea.updatePreOccupiedField(newList);

			newList.clear();
			newList.addAll(_lodArea.getFields());
			newList.addAll(_xArea.getFields());
			_yArea.updatePreOccupiedField(newList);


		} else if(area == _xArea){
			List<Field> newList = new ArrayList<Field>(_xArea.getFields());
			newList.addAll(_lodArea.getFields());
			_yArea.updatePreOccupiedField(newList);

			newList.clear();
			newList.addAll(_xArea.getFields());
			newList.addAll(_yArea.getFields());
			_lodArea.updatePreOccupiedField(newList);


		} else if(area == _yArea){
			List<Field> newList = new ArrayList<Field>(_yArea.getFields());
			newList.addAll(_lodArea.getFields());
			_xArea.updatePreOccupiedField(newList);

			newList.clear();
			newList.addAll(_yArea.getFields());
			newList.addAll(_xArea.getFields());
			_lodArea.updatePreOccupiedField(newList);
		}
	}
	
	private boolean invalidateLODFilters(Filter ref) {
		// LOD filters are not checked for validity during the query build phase.Set all the LOD filters validity to false 
		
		for(Filter filter : filters()){
			if(filter.getField().getClassification() == Classification.C && isInLodArea(filter.getField())){
				filter.setValid(false);
			}
		}
		for(Filter filter : remoteFilters()){
			if(filter.getField().getClassification() == Classification.C && isInLodArea(filter.getField())){
				filter.setValid(false);
			}
		}
		return false;
	}

	/* 
	 * Contained or has the same name and table as a field in the lod area  
	 */
	private Boolean isInLodArea(Field field){
		if (_lodArea.getFields().contains(field)) return true;
		
		if (_xArea.getFields().contains(field) || _yArea.getFields().contains(field)) return false;
		
		
		for(Field lodField : _lodArea.getFields()) {
			if (lodField.getName().equals(field.getName()) && lodField.getTable().getName().equals(field.getTable().getName())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
    private void reassignData(Filter filter) {
		int idx = 0;
		String name = filter.getField().getName();
		for (FieldInfo info : _currentSpec.lod) {
			if (info.field.getName().equals(name)) break;
			idx++;
		}
		idx += 2; // the multikey has two additional keys (x and y)
		
		List<MultiKey> keys = new ArrayList<>();
		
		// remove series
		for (MultiKey multikey : _currentSpec.seriesMap.keySet()) {
			Object keyValue = multikey.getKey(idx);
			if (!filter.getSelectedValues().contains(keyValue)) {
				keys.add(multikey);
			} 
		}
		
		for (MultiKey multikey : keys) {
			getChart().getData().remove(_currentSpec.seriesMap.get(multikey));
			_currentSpec.seriesMap.remove(multikey);
		}
		
		keys.clear();
		// add series
		
		keys = new ArrayList<>(_currentSpec.dataMap.keySet());
		for (Pair<Integer, Filter> p : getLODFilters()) {
			Iterator<MultiKey> i = keys.iterator();
			while (i.hasNext()) {
				MultiKey key = i.next();
				Object keyValue = key.getKey(p.v1+2);
				if (!p.v2.getSelectedValues().contains(keyValue)
					|| _currentSpec.seriesMap.containsKey(key)) 
				{ 
					i.remove();
				}	
			}	
		}
		
		for (MultiKey multikey : keys) {
			XYChart.Series<Object, Object> series = new XYChart.Series<Object, Object>();
			series.setData(_currentSpec.dataMap.get(multikey));
			series.setName(createLabel(multikey));
			getChart().getData().add(series);
			_currentSpec.seriesMap.put(multikey, series);
		}
		
		getChart().setLegendVisible(_currentSpec.seriesMap.size() > 1);
		checkForInfinities(_currentSpec.seriesMap.keySet());
	}
	
	private List<Pair<Integer, Filter>> getLODFilters() {
		List<Pair<Integer, Filter>> list = new ArrayList<>();
		int idx = -1;
		for (FieldInfo info : _currentSpec.lod) {
			idx++;
			boolean found = false;
			String name = info.field.getName();
			for (Filter f : filters()) {
				if (f.getField().getName().equals(name)) {
					list.add(new Pair<Integer, Filter>(idx, f));
					found = true;
					break;
				}
			}
			if (!found) {
				for (Filter f : remoteFilters()) {
					if (f.getField().getName().equals(name)) {
						list.add(new Pair<Integer, Filter>(idx, f));
						found = true;
						break;
					}
				}
			}
		}
		return list;
	}
	
	/*Name: setAreaFiltersListeners 
	 * This method handles fields which are connected to a filter
	 * If the field SQL function has changed the filter has to be changed accordingly 
	 * and the filter panel has to be adjusted. */
	private void setAreaFiltersListeners(DropArea area) {
		area.setOnAction(new EventHandler<FilterEvent>() {
			@Override
			public void handle(FilterEvent event) {
				Filter filter = event.getFilter();
				if(event.getEventType() == FilterEvent.DELETE){
					filters().remove(filter);
					getOnRemoveFilter().call(event.getFilter());
				}else if(event.getEventType() == FilterEvent.SHOW){
					filters().add(filter);
					getOnShowFilter().call(event.getFilter());
				}
			}
		});
	}

	private DropArea createControlArea(GridPane grid, String title, int  row, int col, int colspan, DropArea.Policy policy, DropArea.AcceptedRoles acceptedRoles) {                
		Text text = new Text(title);
		text.getStyleClass().add("input-area-header");

		DropArea area = new DropArea(policy, acceptedRoles);
		area.tableProperty().bind(_currentTableProperty);
		area.addListener(_areaListener);

		//Sets for the drop area all the possible drag and drop sources and their accepted transfer modes.
		Map<Class<?>, TransferMode[]> sourcesTransferModes = createDragAndDropModes();
		area.setDragAndDropModes(sourcesTransferModes);

		//Let the filters area know when the selected table is changed.
		getFiltersArea().tableProperty().bind(_currentTableProperty);

		setAreaFiltersListeners(area);

		grid.add(text, col, row);
		grid.add(area, col+1, row, colspan, 1);

		return area;
	}

//	private DropArea createIndicatorArea(GridPane grid, String title, int  row, int col, DropArea.Policy policy, DropArea.AcceptedRoles acceptedRoles) {
//		Text text = new Text(title);
//		text.getStyleClass().add("input-area-header");
//
//		DropArea area = new DropArea(policy, acceptedRoles);
//		area.tableProperty().bind(_currentTableProperty);
//		area.addListener(_areaListener);
//
//		Button addButton = new Button("+");
//		addButton.getStyleClass().add("flat-button");
//		addButton.setOnAction(new EventHandler<ActionEvent>() {
//
//			@Override
//			public void handle(ActionEvent arg0) {
//				createIndicator();
//			}
//		});
//
//		grid.add(text, col, row);
//		grid.add(area, col+1, row);
//		grid.add(addButton, col+2, row);
//
//		return area;
//	}

	private void setupGlassPaneListeners() {
		_glassPane.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (getLocalClipboard().hasContent(DnD.INDICATOR_FORMAT)) {
					event.acceptTransferModes(TransferMode.COPY);
					event.consume();
				}
			}
		});

		_glassPane.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Indicator indicator = getLocalClipboard().get(DnD.INDICATOR_FORMAT, Indicator.class);
				_indicators.add(indicator);
			}
		});
	}
	
	/*
	 * Tries to find an available data source either from the current table or the current simulation.
	 * @return CyclistDatasource - the data source which has been found.
	 */
	private CyclistDatasource getAvailableDatasource(){
		Table currentTable = getCurrentTable();
		Simulation currentSim = getCurrentSimulation();
		
		CyclistDatasource ds = (currentTable != null && currentTable.getDataSource()!= null) ? 
				currentTable.getDataSource() : 
				currentSim != null ? currentSim.getDataSource() : null;
		return ds;
				
	}

	/*Name: removeFilterFromDropArea
	 * If a filter is removed - check if it is connected to a numeric field. By searching this field in the drop areas.
	 * If so - change also the field display to indicate that is doesn't connect to a filter anymore */
	@Override 
	public void removeFilterFromDropArea(Filter filter){
		_xArea.removeFilterFromGlyph(filter);
		_yArea.removeFilterFromGlyph(filter);
	}

	private class FieldInfo {
		Field field;
		int index;

		public FieldInfo(Field field, int index) {
			this.field = field;
			this.index = index;
		}
	}

}
