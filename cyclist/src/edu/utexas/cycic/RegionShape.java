package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utexas.cycic.tools.FormBuilderTool;
import edu.utexas.cycic.tools.RegionViewTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * This class extends the Java Circle class. Used to represent a region
 * in the RegionCorralView and contains all of the information associated with
 * the region.
 * @author birdybird
 *
 */

public class RegionShape extends Rectangle {

	protected static double mousey;
	protected static double mousex;
	protected static double x;
	protected static double y;
	protected static double deltax;
	protected static double deltay;
	Object name;
	Label text = new Label("");
	MenuBar menuBar = new MenuBar();
	static regionNode regionBackTrace;
	ArrayList<Integer> rgbColor = new ArrayList<Integer>();

	static RegionShape addRegion(final String name, final regionNode region) {
		final RegionShape rect = new RegionShape();
		
		RegionCorralView.workingRegion = region;
		regionBackTrace = region;
		// Set properties of regionNode
		region.name = name;
		
		rect.setWidth(80);
		rect.setHeight(80);
		rect.setX(50);
		rect.setY(50);
		rect.setStroke(Color.DARKGRAY);
		rect.setStrokeWidth(5);
		
		rect.name = name;
		rect.text.setText(name);
		rect.text.setLayoutX(rect.getX()+rect.getHeight()*0.2);
		rect.text.setLayoutY(rect.getY()+rect.getHeight()*0.2);	
		rect.text.setMaxWidth(rect.getWidth()*0.8);
		rect.text.setMaxHeight(rect.getHeight()*0.8);
		rect.text.setMouseTransparent(true);
		rect.text.setFont(new Font(14));
		rect.text.setWrapText(true);
		
		// Set circle color
		rect.rgbColor=VisFunctions.stringToColor(region.type);
		rect.setFill(Color.rgb(rect.rgbColor.get(0), rect.rgbColor.get(1), rect.rgbColor.get(2), 0.8));
		// Setting font color for visibility //
		if(VisFunctions.colorTest(rect.rgbColor) == true){
			rect.text.setTextFill(Color.BLACK);
		}else{
			rect.text.setTextFill(Color.WHITE);
		}
		
		rect.setEffect(VisFunctions.lighting);

		//Adding the circle's menu and its functions.

		MenuItem regionForm = new MenuItem("Region Form");
		regionForm.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				CyclistController._presenter.addTool(new RegionViewTool());
				rect.menuBar.setVisible(false);
			}
		});

		EventHandler<ActionEvent> deleteEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent deleteEvent) {
				deleteRegion(rect, region);
				rect.menuBar.setVisible(false);
			}
		};
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(deleteEvent);

		EventHandler<ActionEvent> exitEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent exitEvent) {
				rect.menuBar.setVisible(false);
			}
		};
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(exitEvent);
		
		final Menu menu = new Menu("Options");
		menu.getItems().addAll(regionForm, delete, exit);		

		rect.menuBar.getMenus().add(menu);
		rect.menuBar.setLayoutX(rect.getX());
		rect.menuBar.setLayoutY(rect.getY());
		rect.menuBar.setVisible(false);

		rect.onMouseClickedProperty().set(new EventHandler <MouseEvent>(){
			@Override
			public void handle(MouseEvent menuEvent){
				if(menuEvent.getButton().equals(MouseButton.SECONDARY)){
					rect.menuBar.setVisible(true);
					rect.menuBar.setLayoutX(rect.getX());
					rect.menuBar.setLayoutY(rect.getY());
				}
				
				if(menuEvent.getClickCount() == 2){
                    menuEvent.consume();
					CyclistController._presenter.addTool(new RegionViewTool());
				}
				for(int i = 0; i < RegionCorralView.corralPane.getChildren().size(); i++){
					if(RegionCorralView.corralPane.getChildren().get(i).getId() == "this"){
						((Shape) RegionCorralView.corralPane.getChildren().get(i)).setStroke(Color.BLACK);
						((Shape) RegionCorralView.corralPane.getChildren().get(i)).setStrokeWidth(1);
					}
				}
				
				rect.setEffect(VisFunctions.lighting);
				rect.setStrokeWidth(5);
				rect.setStroke(Color.DARKGRAY);
				
			}
		});
		
		// Allows a shift + (drag and drop) to start a new RegionView for this RegionShape.
		rect.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent regionViewEvent){
				if(regionViewEvent.isShiftDown() == true){

					RegionView.workingRegion = region;
					
					DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
					clipboard.put(DnD.TOOL_FORMAT, Tool.class, new RegionViewTool());
					
					Dragboard db = rect.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();				
					content.put( DnD.TOOL_FORMAT, "Region View");
					db.setContent(content);
					
					regionViewEvent.consume();
				}
			}
		});
		
		return rect;	
	};

	static void deleteRegion(RegionShape circle, regionNode region){
		DataArrays.regionNodes.remove(region);
		RegionCorralView.corralPane.getChildren().removeAll(circle, circle.menuBar, circle.text);
	};

	{	

		onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				x = getX() - event.getX();
				y = getY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});

		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){

				setX(x+event.getX());
				setY(y+event.getY());

				if(getX() <= RegionCorralView.corralPane.getLayoutBounds().getMinX()){
					setX(RegionCorralView.corralPane.getLayoutBounds().getMinX());
				}
				if(getY() <= RegionCorralView.corralPane.getLayoutBounds().getMinY()){
					setY(RegionCorralView.corralPane.getLayoutBounds().getMinY());
				}
				if(getY() >= RegionCorralView.corralPane.getLayoutBounds().getMaxY()-getHeight()){
					setY(RegionCorralView.corralPane.getLayoutBounds().getMaxY()-getHeight());
				}
				if(getX() >= RegionCorralView.corralPane.getLayoutBounds().getMaxX()-getWidth()){
					setX(RegionCorralView.corralPane.getLayoutBounds().getMaxX()-getWidth());
				}

				text.setLayoutX(getX()+getHeight()*0.2);
				text.setLayoutY(getY()+getHeight()*0.2);	

				menuBar.setLayoutX(getX()+getHeight()*0.2);
				menuBar.setLayoutY(getY()+getHeight()*0.2);
			}
		});

	}


};
