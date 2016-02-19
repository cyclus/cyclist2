package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utexas.cycic.tools.RegionViewTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

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
    ContextMenu menu;
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
		
		// Set circle color
		rect.rgbColor=VisFunctions.stringToColor(region.type);
		rect.setFill(VisFunctions.pastelize(Color.rgb(rect.rgbColor.get(0), rect.rgbColor.get(1), rect.rgbColor.get(2), 0.8)));

		VisFunctions.placeTextOnRectangle(rect,"middle");
		
		rect.setEffect(VisFunctions.lighting);

		//Adding the circle's menu and its functions.

		MenuItem regionForm = new MenuItem("Configure");
		regionForm.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				CyclistController._presenter.addTool(new RegionViewTool());
			}
		});

        MenuItem helpDialog = new MenuItem("Region Documentation");
        helpDialog.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e){
                FormBuilderFunctions.showHelpDialog(region.doc);
            }
        });
            

		EventHandler<ActionEvent> deleteEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent deleteEvent) {
				deleteRegion(rect, region);
			}
		};
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(deleteEvent);

        rect.menu = new ContextMenu();
		rect.menu.getItems().addAll(regionForm, helpDialog, delete);		

		rect.onMouseClickedProperty().set(new EventHandler <MouseEvent>(){
			@Override
			public void handle(MouseEvent menuEvent){
				if(menuEvent.getButton().equals(MouseButton.SECONDARY)){
                    rect.menu.show(rect, menuEvent.getScreenX(), menuEvent.getScreenY());
                    menuEvent.consume();
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
				RegionCorralView.workingRegion = region;
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

		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		rect.onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){

				rect.setX(x+event.getX());
				rect.setY(y+event.getY());

				if(rect.getX() <= RegionCorralView.corralPane.getLayoutBounds().getMinX()){
					rect.setX(RegionCorralView.corralPane.getLayoutBounds().getMinX());
				}
				if(rect.getY() <= RegionCorralView.corralPane.getLayoutBounds().getMinY()){
					rect.setY(RegionCorralView.corralPane.getLayoutBounds().getMinY());
				}
				if(rect.getY() >= RegionCorralView.corralPane.getLayoutBounds().getMaxY()-rect.getHeight()){
					rect.setY(RegionCorralView.corralPane.getLayoutBounds().getMaxY()-rect.getHeight());
				}
				if(rect.getX() >= RegionCorralView.corralPane.getLayoutBounds().getMaxX()-rect.getWidth()){
					rect.setX(RegionCorralView.corralPane.getLayoutBounds().getMaxX()-rect.getWidth());
				}

				VisFunctions.placeTextOnRectangle(rect,"middle");

			}
		});


		
		return rect;	
	};

	static void deleteRegion(RegionShape circle, regionNode region){
		DataArrays.regionNodes.remove(region);
		RegionCorralView.corralPane.getChildren().removeAll(circle,  circle.text);
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


	}


};
