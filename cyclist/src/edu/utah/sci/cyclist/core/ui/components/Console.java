package edu.utah.sci.cyclist.core.ui.components;


import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class Console extends ScrollPane {
	private TextFlow _textFlow;
    private Map<Level, Color> _colors = new HashMap<>();
    private Map<Level, String> _prefix = new HashMap<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[hh:mm]");
    private Font regular = Font.getDefault();
    private Font bold = Font.font(regular.getFamily(), FontWeight.BOLD, regular.getSize());

    private boolean active = true;
    
	public Console() {
		super();
		build();
		init();
	}
	
	public void setActive(boolean value) {
		active = value;
	}
	
	//	TODO: upgrade to Log4j2.x
	private void init() {
		Logger.getRootLogger().addAppender(new CyclistAppender());
		
		_colors.put(Level.FATAL, Color.RED);
		_colors.put(Level.ERROR, Color.RED);
		_colors.put(Level.WARN, Color.BLACK);
		_colors.put(Level.INFO, Color.BLACK);
		_colors.put(Level.DEBUG, Color.BLUE);
		_colors.put(Level.TRACE, Color.GRAY);
		_colors.put(Level.ALL, Color.GRAY);	
		
		_prefix.put(Level.FATAL, "FATAL: ");
		_prefix.put(Level.ERROR, "Error: ");
		_prefix.put(Level.WARN, "Warning: ");
		_prefix.put(Level.INFO, "");
		_prefix.put(Level.DEBUG, "");
		_prefix.put(Level.TRACE, "");
		_prefix.put(Level.ALL, "");	
	}

	private void build() {
		getStyleClass().add("console");
        setFitToWidth(true);
        setFitToHeight(true);
		setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
		_textFlow = new TextFlow();
		setContent(_textFlow);
	}
	
	private Color color(Level level) {
		Color c = _colors.get(level);
		return c != null ? c : _colors.get(Level.ALL);  
	}
	
	class CyclistAppender implements Appender {

		public CyclistAppender() {
		}

		@Override
		public void addFilter(Filter filter) {
			System.out.println("addFilter");
			
		}

		@Override
		public void clearFilters() {
			System.out.println("clearFilters");			
		}

		@Override
		public void close() {
			System.out.println("close");			
		}

        @Override
        public void doAppend(LoggingEvent e) {
        	if (!active) return;
        	
            StringBuilder builder = new StringBuilder()
            	.append(" [")
            	.append(LocalTime.now().format(formatter))
            	.append("] ")
            	.append(_prefix.get(e.getLevel()))
            	.append(": ")
            	.append(e.getMessage())
            	.append("\n");
            
        	Text text = new Text(builder.toString());
        	text.setFill(color(e.getLevel()));
        	text.setFont(e.getLevel() == Level.WARN ? bold : regular);
        	
        	_textFlow.getChildren().add(text);
        	
        	final Console self = Console.this;
    		Platform.runLater(new Runnable() {	
    			@Override
    			public void run() {
    				self.setVvalue(1);
    			}
    		});
        	
        }

		@Override
		public ErrorHandler getErrorHandler() {
			System.out.println("getErrorHandler");
			return null;
		}

		@Override
		public boolean requiresLayout() {
			System.out.println("requiresLayout?");
			return false;
		}

		@Override
		public void setErrorHandler(ErrorHandler handler) {
			System.out.println("setErrorHandler");
			
		}

		@Override
		public void setLayout(Layout layout) {
			System.out.println("setLayout");
			
		}

		@Override
		public void setName(String name) {
			System.out.println("setName"+name);			
		}

		@Override
		public Filter getFilter() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Layout getLayout() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
