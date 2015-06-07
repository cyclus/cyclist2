package edu.utexas.cycic;

import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utexas.cycic.tools.FormBuilderTool;
import edu.utexas.cycic.tools.FormBuilderToolFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Light.Distant;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The class used to build a new Prototype Facility node.
 * @author Robert
 *
 */
public class SorterCircles{

	protected static double mousey;
	protected static double mousex;
	protected static double x;
	protected static double y;
	
	/**
	 * Function to build a prototype facility node. This node will contain
	 * the facilities available to institutions and regions. All children 
	 * built from this node will mimic its structure. 
	 * @param name Name of the new prototype facility.
	 */
	static FacilityCircle addNode(String name, final facilityNode parent, final facilityNode cloneNode) {
		final FacilityCircle circle = new FacilityCircle();
		circle.setRadius(30);
		circle.setCenterX(40);
		circle.setCenterY(40);
		circle.name = name;
		//Setting up the name and nameing structure of the circle.
		circle.text.setText(name);
		circle.text.setTooltip(new Tooltip(name));
		circle.text.setWrapText(true);
		circle.text.setLayoutX(circle.getCenterX()-circle.getRadius()*0.6);
		circle.text.setLayoutY(circle.getCenterY());	
		circle.text.setMaxWidth(circle.getRadius()*1.6);
		circle.text.setMouseTransparent(true);
		
		// Setting the circle color //
		circle.setStroke(Color.BLACK);
		circle.rgbColor=VisFunctions.stringToColor((String)parent.name);
		circle.setFill(VisFunctions.pastelize(Color.rgb(circle.rgbColor.get(0), circle.rgbColor.get(1), circle.rgbColor.get(2))));
		circle.setEffect(VisFunctions.lighting);
		
		// Setting font color for visibility //
		if(VisFunctions.colorTest(circle.rgbColor) == true){
			circle.text.setTextFill(Color.WHITE);
		}else{
			circle.text.setTextFill(Color.WHITE);
		}
			
		// Adding the menu and it's menu items.
		final Menu menu1 = new Menu("Options");
		MenuItem facForm = new MenuItem("Facility Form");
		MenuItem delete = new MenuItem("Delete");
		
		delete.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				CycicCircles.deleteNode(parent);
			}
		});
		
		MenuItem showImage = new MenuItem("Show Image");
		showImage.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				circle.image.setVisible(true);
				circle.image.toBack();
				circle.setOpacity(0);			
			}
		});
		
		MenuItem hideImage = new MenuItem("Hide Image");
		hideImage.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				circle.image.setVisible(false);
				circle.setOpacity(100);
			}
		});
		
		circle.menu.getItems().addAll(facForm, delete, showImage, hideImage);

		
		// Piece of test code for changing the look of the facility circles.
		circle.image.setLayoutX(circle.getCenterX()-60);
		circle.image.setLayoutY(circle.getCenterY()-60);
		circle.image.setMouseTransparent(true);
		circle.image.setVisible(false);
		
		// Mouse pressed to add in movement of the facilityCircle.
		circle.onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				Cycic.workingNode = cloneNode;
				x = circle.getCenterX() - event.getX();
				y = circle.getCenterY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});
		
		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		circle.onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				circle.setCenterX(mousex+x);
				circle.setCenterY(mousey+y);
				
				/*if(circle.getCenterX() <= Cycic.pane.getLayoutBounds().getMinX()+circle.getRadius()){
					circle.setCenterX(Cycic.pane.getLayoutBounds().getMinX()+circle.getRadius());
				}
				if(circle.getCenterY() <= Cycic.pane.getLayoutBounds().getMinY()+circle.getRadius()){
					circle.setCenterY(Cycic.pane.getLayoutBounds().getMinY()+circle.getRadius());
				}
				if(circle.getCenterY() >= Cycic.pane.getLayoutBounds().getMaxY()-circle.getRadius()){
					circle.setCenterY(Cycic.pane.getLayoutBounds().getMaxY()-circle.getRadius());
				}
				if(circle.getCenterX() >= Cycic.pane.getLayoutBounds().getMaxX()-circle.getRadius()){
					circle.setCenterX(Cycic.pane.getLayoutBounds().getMaxX()-circle.getRadius());
				}*/

				
				circle.image.setLayoutX(circle.getCenterX()-60);
				circle.image.setLayoutY(circle.getCenterY()-50);
				
				circle.text.setLayoutX(circle.getCenterX()-circle.getRadius()*0.6);
				circle.text.setLayoutY(circle.getCenterY());

				mousex = event.getX();
				mousey = event.getY();
			}
		});
		
		// Code for allow a shift + (drag and drop) to start a new facility form for this facilityCircle.
		circle.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(event.isShiftDown() == true){
					DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
					clipboard.put(DnD.TOOL_FORMAT, Tool.class, new FormBuilderTool());
					
					Dragboard db = circle.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();				
					content.put( DnD.TOOL_FORMAT, "Facility Form");
					db.setContent(content);
					
					event.consume();
				}
			}
		});		
		return circle;
	}
}
