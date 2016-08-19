package edu.utah.sci.cyclist.neup.ui.views.flow;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import edu.utah.sci.cyclist.core.event.Pair;
import edu.utah.sci.cyclist.core.ui.components.CyclistAxis;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;
import edu.utah.sci.cyclist.neup.model.Range;

public class FlowChart extends VBox {
	private static final String NET_CHART_LABEL = "Shiped - Recieved";
	private static final String COMMULATIVE_CHART_LABEL = "Total Shiped/Receieved";
	
	private HBox _header;
	private StackPane _stack;
	private Pane _glass;
	private Rectangle _rec;
	
	private LineChart<Number, Number> _chart;
	private NumberAxis _xAxis;
	private CyclistAxis _yAxis;
	private double _scale = 1;
	private boolean _opened = true;
	private int _upperBound;
	private boolean _updating = false;
	
	private Map<InventoryEntry, ChartInfo> _info = new HashMap<>();
	private ObjectProperty<Range<Integer>> _timeRangeProperty = new SimpleObjectProperty<>();
	
	private StringProperty _chartType = new SimpleStringProperty();
	
	public StringProperty chartType() {
		return _chartType;
	}
	
	public String getChartType() {
		return _chartType.get();
	}
	
	public void setChartType(String type) {
		_chartType.set(type);
	}
	
	public class ChartInfo {
		public Label title;
		public Collection<Pair<Integer, Double>> values;
		public XYChart.Series<Number, Number> series;
		public double scale;
		public int last;
	}
	
	public FlowChart() {
		super();
		build();
		
		chartType().addListener(e->{
			selectChartType();
		});
	}
	
	public ObjectProperty<Range<Integer>> timeRangeProperty() {
		return _timeRangeProperty;
	}
	
	private void selectChartType() {
//		double s = 1;
//		for (ChartInfo info : _info.values()) {
//			s = Math.max(s, computeScale(info.values));
//		}
//		if (s != _scale) {
//			_scale = s;
//			updateYAxis();
//		}
		updateAll();
	}
	
	public void add(InventoryEntry entry, String title, Collection<Pair<Integer, Double>> values) {
		if (values.size() == 0) return;
		
		int last = 0;
		for (Pair<Integer, Double> p : values) {
			last = Math.max(last, p.v1);
		}
		if (last > _upperBound) {
			_upperBound = last;
		}
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		Color c = entry.getColor();
		String style = c.toString().replace("0x", "#");
		series.nodeProperty().addListener(o->{
			series.getNode().setStyle("-fx-stroke:"+style);
		});

		double scale = computeScale(values); // relative to the current chart type
		
//		if (scale > _scale) {
//			updateScale(scale);
//		}
		updateSeries(series, values);

		ChartInfo info = new ChartInfo();
		info.values = values;
		info.title = new Label(title);
		info.title.setStyle("-fx-background-color:"+style);
		info.title.getStyleClass().add("header");
		info.series = series;
		info.scale = scale;
		info.last = last;
		_info.put(entry, info);
		
		_header.getChildren().add(info.title);
		_chart.getData().add(series);
		_rec.setVisible(true);
	}
	
	public void remove(InventoryEntry entry) {
		ChartInfo info = _info.remove(entry);
		if (info == null) {
			return;
		}
				
		_header.getChildren().remove(info.title);
		_chart.getData().remove(info.series);
		double s = 1;
		_upperBound = 0;
		for (ChartInfo ci : _info.values()) {
			s = Math.max(ci.scale, s);
			_upperBound = Math.max(_upperBound, ci.last);
		}
//		if (s != _scale) {
//			updateScale(s);
//			updateYAxis();
//		}
		if (_info.isEmpty()) {
			_rec.setVisible(false);
		}
	}
	
	private void updateAll() {
		for (ChartInfo info : _info.values()) {
			updateSeries(info.series, info.values);
		}
	}
	
	private void updateScale(double value) {
		_scale = value;
		updateYAxis();
		updateAll();
	}

	private void updateYAxis() {
//		String label = _scale == 1 ? "kg"
//				: _scale == 1000 ? "x 1000 kg"
//				: String.format("x %.0e kg", _scale);
//	
//		_yAxis.setLabel(label);
	}

	
	private double computeScale(Collection<Pair<Integer, Double>> values) {
		double max = 0;
		if (getChartType().equals(COMMULATIVE_CHART_LABEL)) {
			double sum = 0;
			for (Pair<Integer, Double> value : values) {
				sum += Math.abs(value.v2);
			}
			max = sum;
		} else {
			double prev = 0;
			for (Pair<Integer, Double> value : values) {
				max = Math.max(max, Math.abs(value.v2-prev));
				prev = value.v2;
			}
		}
		if (max == 0) max = 1;
		double s = Math.pow(10, 3*Math.floor(Math.log10(max)/3));
		return s;
	}
	
	private void updateSeries(XYChart.Series<Number, Number> series, Collection<Pair<Integer, Double>> values) {
		series.getData().clear();

		if (getChartType().equals(COMMULATIVE_CHART_LABEL)) {
			double sum = 0;
			for (Pair<Integer, Double> value : values) {
				sum += value.v2/_scale;
				series.getData().add(new XYChart.Data<Number, Number>(value.v1, sum));
			}
		} else {
			double prev = 0;
			for (Pair<Integer, Double> value : values) {
				double v = value.v2/_scale;
				series.getData().add(new XYChart.Data<Number, Number>(value.v1, v-prev));
				prev = v;
			}
		}
	}

			
	private void build() {
		getStyleClass().add("fchart");
		
		buildHeader();
		
		getChildren().addAll(
			_header,
			buildChart()
		);
		
		addListeners();
	}
	
	private void buildHeader() {
		_header = new HBox();
		_header.getStyleClass().add("infobar");

		Label caret = new Label("", GlyphRegistry.get(AwesomeIcon.CARET_DOWN));
		
		ChoiceBox<String> type = new ChoiceBox<>();
		type.getStyleClass().add("choice");
		type.getItems().addAll(COMMULATIVE_CHART_LABEL, NET_CHART_LABEL);
	
		_header.getChildren().addAll(caret, type);
		
		caret.setOnMouseClicked(e->{
			_opened = !_opened;
			if (_opened) {
				caret.setGraphic(GlyphRegistry.get(AwesomeIcon.CARET_DOWN));
			} else {
				caret.setGraphic(GlyphRegistry.get(AwesomeIcon.CARET_RIGHT));			
			}
			_stack.setVisible(_opened);
			_stack.setManaged(_opened);
		});
		
		type.valueProperty().bindBidirectional(chartType());
		
		type.setValue(COMMULATIVE_CHART_LABEL);
	}
	

	private Node buildChart() {
		_xAxis = new NumberAxis();
		_xAxis.setLabel("time");
		_xAxis.setAnimated(false);
		_xAxis.setForceZeroInRange(false);
		
		_yAxis = new CyclistAxis(CyclistAxis.Mode.LOG);
		_yAxis.setLabel("Cummulative");
		_yAxis.setForceZeroInRange(false);
		_yAxis.setAnimated(false);
		
		_chart = new LineChart<>(_xAxis, _yAxis);
		_chart.getStyleClass().add("chart");
		_chart.setCreateSymbols(false);
		
		_rec = new Rectangle();
		_rec.getStyleClass().add("range");
		_rec.yProperty().bind(_yAxis.layoutYProperty().add(5));
		_rec.heightProperty().bind(_yAxis.heightProperty());
		_rec.setVisible(false);
		
		_glass = new Pane();
		_glass.getChildren().addAll(/*_fromLine*/_rec/*, _popup*/);
		
		_xAxis.layoutBoundsProperty().addListener(e->{
			updateRangeLater();		
		});
		
		_xAxis.lowerBoundProperty().addListener(o->{
			updateRangeLater();				
		});
		
		_xAxis.upperBoundProperty().addListener(o->{
			updateRangeLater();		
		});
		
		_stack = new StackPane();
		_stack.getChildren().addAll( _chart, _glass);
		_stack.setStyle("-fx-pref-height: 100px");
		
//		VBox.setVgrow(_stack, Priority.ALWAYS);
		return _stack;
	}
	
	// HACK? : 
	// The info regarding the x and y Axes does not seem to be correct when their layout just changed.
	// Need to wait until it is updated.
	private void updateRangeLater() {
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	updateRange();
            }
		});
	}
	
	private void updateRange() {
		Range<Integer> r = timeRangeProperty().get();
		double t1 = r.from;
		double t2 = r.to+1;
		
		double x1 = _xAxis.getDisplayPosition(t1);
		double x2 = _xAxis.getDisplayPosition(t2);
		
		Point2D p1 = _glass.sceneToLocal(_xAxis.localToScene(x1, 0));
		Point2D p2 = _glass.sceneToLocal(_xAxis.localToScene(x2, 0));
		
		_rec.setX(p1.getX());
		_rec.setWidth(Math.max(2, p2.getX() - p1.getX()));
	}
	
	double _mx;
	double _offset;
	double _right;
	double _left;
	
	private void addListeners() {
		_timeRangeProperty.addListener(o->{
			if (_updating) return;
			updateRange();
		});
		
		_rec.setOnMousePressed(e->{
			_mx = e.getX();
			_offset = _xAxis.getDisplayPosition(_timeRangeProperty.get().from);
			_left = _glass.sceneToLocal(_xAxis.localToScene(_xAxis.getDisplayPosition(0), 0)).getX();
			_right = _glass.sceneToLocal(_xAxis.localToScene(_xAxis.getDisplayPosition(_upperBound), 0)).getX();
		});
		
		_rec.setOnMouseDragged(e->{
			double dx = e.getX() - _mx;
			double p = _rec.getX()+dx;
			if (p < _left) 
				p = _left;
			else if (p+_rec.getWidth() > _right)
				p = _right - _rec.getWidth();
		
			_rec.setX(p);
			_mx = e.getX(); 
		});
		
		_rec.setOnMouseReleased(e->{
			Range<Integer> r = _timeRangeProperty.get();
			int dt = r.to - r.from+1;
			Point2D p = _xAxis.sceneToLocal(_glass.localToScene(_rec.getX(), 0));
			Number v = _xAxis.getValueForDisplay(p.getX());
			int i = v.intValue();
			i = Math.max(0, Math.min(_upperBound-dt, i));
			// nudge rectangle to an integer boundry
			double xi = _xAxis.getDisplayPosition(i);
			Point2D p1 = _xAxis.localToScene(xi,0);
			Point2D p2 = _glass.sceneToLocal(p1);
			_rec.setX(p2.getX());
			
			Range<Integer> nr = new Range<>(i, i+r.to-r.from);
			_updating = true;
			_timeRangeProperty.set(nr);
			_updating = false;
		});
	
	}
}