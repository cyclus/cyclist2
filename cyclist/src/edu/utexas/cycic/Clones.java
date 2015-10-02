package edu.utexas.cycic;

import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utexas.cycic.tools.FormBuilderTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.shape.Shape;

/**
 * The Clones class deals with all functions regarding generating and 
 * removing clones.
 * @author Robert
 *
 */
public class Clones {
	protected static double mousey;
	protected static double mousex;
	protected static double x;
	protected static double y;
	
	/**
	 * Builds a child facilityCircle and adds it to the parent facilityCircle's childrenList.
	 * @param name Name of the new child
	 * @param parent facilityCircle node the new child will be added to.
	 * @param parentChildShow Boolean to indicate whether or not to show the child when it is built.
	 */
	static void addClone(String name, final facilityNode parent, Boolean parentChildShow) {
		// Building the facilityCircle and adding the basic information.
		final facilityNode cloneNode = new facilityNode();
		final FacilityCircle clone = cloneNode.cycicCircle;
		clone.setRadius(30);
		clone.parent = (String) parent.name;
		clone.type = "Child";
		clone.name = name;
		
		// Providing the child with the index of its parent node in the dataArrays.facilityNodes ArrayList.
		for(int i = 0; i < DataArrays.FacilityNodes.size(); i++){
			if(DataArrays.FacilityNodes.get(i) == parent){
				cloneNode.parentIndex = i;
				clone.parentIndex = i;
			}
		}
		// Copying important information from parent to child.
		cloneNode.facilityStructure = parent.facilityStructure;
		parent.cycicCircle.childrenList.add(clone);
		parent.facilityClones.add(cloneNode);
		cloneNode.sorterCircle = SorterCircles.addNode(name, parent, cloneNode);
		clone.facTypeIndex = parent.facTypeIndex;
		clone.setCenterX(parent.cycicCircle.getCenterX()+80);
		clone.setCenterY(parent.cycicCircle.getCenterY()+80);
		clone.facilityType = parent.facilityType;
		// Building the child's facility data ArrayList
		FormBuilderFunctions.formArrayBuilder(cloneNode.facilityStructure, cloneNode.facilityData);
		
		// Setting the Fill Color //
		clone.rgbColor = VisFunctions.stringToColor((String) parent.name);
		clone.rgbColor.set(0, (int)(clone.rgbColor.get(0)*VisFunctions.colorMultiplierTest(clone.rgbColor.get(0))));
		clone.rgbColor.set(1, (int)(clone.rgbColor.get(1)*VisFunctions.colorMultiplierTest(clone.rgbColor.get(1))));
		clone.rgbColor.set(2, (int)(clone.rgbColor.get(2)*VisFunctions.colorMultiplierTest(clone.rgbColor.get(2))));
		clone.setFill(VisFunctions.pastelize(Color.rgb(clone.rgbColor.get(0), clone.rgbColor.get(1), clone.rgbColor.get(2))));
		
		// Setting font color for visibility //
		if(VisFunctions.colorTest(clone.rgbColor) == true){
			clone.text.setTextFill(Color.WHITE);
		}else{
			clone.text.setTextFill(Color.BLACK);
		}
		for(int i = 0; i < Cycic.pane.getChildren().size(); i++){
			if(Cycic.pane.getChildren().get(i).getId() == "cycicNode"){
				((Shape) Cycic.pane.getChildren().get(i)).setStroke(Color.BLACK);
				((Shape) Cycic.pane.getChildren().get(i)).setStrokeWidth(1);
			}
		}
		clone.setEffect(VisFunctions.lighting);
		clone.setStrokeWidth(5);
		clone.setStroke(Color.DARKGRAY);
		
		// Adding the facility menu //
		final Menu menu1 = new Menu((String) clone.name);
		MenuItem facForm = new MenuItem("Configure");
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				removeClone(cloneNode, parent);
			}
		});
		clone.menu.getItems().addAll(facForm, delete);
		// Adding circle.text to be shown in the CYCIC pane.
		clone.text.setText(name.toString());
		clone.text.setLayoutX(clone.getCenterX()-clone.getRadius()*0.6);
		clone.text.setLayoutY(clone.getCenterY());
		clone.text.setMaxWidth(clone.getRadius()*1.4);
		clone.text.setMouseTransparent(true);
		clone.text.setFont(new Font(14));
		
		// Adding the Parent Child Link different from normal market links.//
		final nodeLink parentChild = new nodeLink();
		parentChild.source = parent.cycicCircle;
		parentChild.target = name;
		parentChild.line.setStroke(Color.GRAY);
		parentChild.line.setStrokeWidth(1.5);
		parentChild.line.getStrokeDashArray().addAll(15d, 5d);
		parentChild.line.setStartX(parent.cycicCircle.getCenterX());
		parentChild.line.setStartY(parent.cycicCircle.getCenterY());
		parentChild.line.setEndX(parent.cycicCircle.childrenList.get(parent.cycicCircle.childrenList.size()-1).getCenterX());
		parentChild.line.setEndY(parent.cycicCircle.childrenList.get(parent.cycicCircle.childrenList.size()-1).getCenterY());
		
		// Mouse drag detection function for initiating the construction of a new view. 
		clone.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(event.isShiftDown() == true){
					DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
					clipboard.put(DnD.TOOL_FORMAT, Tool.class, new FormBuilderTool());
					
					Dragboard db = clone.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();				
					content.put( DnD.TOOL_FORMAT, "Configure");
					db.setContent(content);
					
//					DnDIcon.getInstance().show(icon, title);
					event.consume();
				}
			}
		});
		
		// Recording informtaion for making the node move.
		clone.onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				Cycic.workingNode = cloneNode;
				x = clone.getCenterX() - event.getX();
				y = clone.getCenterY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});
		// Handles the movement controls of the facilityNode.
		clone.onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				clone.setCenterX(mousex+x);
				clone.setCenterY(mousey+y);
				
				if(clone.getCenterX() <= Cycic.pane.getLayoutBounds().getMinX()+clone.getRadius()){
					clone.setCenterX(Cycic.pane.getLayoutBounds().getMinX()+clone.getRadius());
				}
				if(clone.getCenterY() <= Cycic.pane.getLayoutBounds().getMinY()+clone.getRadius()){
					clone.setCenterY(Cycic.pane.getLayoutBounds().getMinY()+clone.getRadius());
				}
				if(clone.getCenterY() >= Cycic.pane.getLayoutBounds().getMaxY()-clone.getRadius()){
					clone.setCenterY(Cycic.pane.getLayoutBounds().getMaxY()-clone.getRadius());
				}
				if(clone.getCenterX() >= Cycic.pane.getLayoutBounds().getMaxX()-clone.getRadius()){
					clone.setCenterX(Cycic.pane.getLayoutBounds().getMaxX()-clone.getRadius());
				}

				clone.text.setLayoutX(clone.getCenterX()-clone.getRadius()*0.6);
				clone.text.setLayoutY(clone.getCenterY());
				parentChild.line.setEndX(clone.getCenterX());
				parentChild.line.setEndY(clone.getCenterY());
				for(int i = 0; i < DataArrays.Links.size();i++){
					if(DataArrays.Links.get(i).source == clone){
						DataArrays.Links.get(i).line.setStartX(clone.getCenterX());
						DataArrays.Links.get(i).line.setStartY(clone.getCenterY());
					}
				}
				mousex = event.getX();
				mousey = event.getY();
			}
		});
		// Double click test && menu handing.
		clone.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(event.getButton().equals(MouseButton.SECONDARY)){
					clone.menu.show(clone, event.getX(), event.getY());
				}
				if(event.getButton().equals(MouseButton.PRIMARY)){
					for(int i = 0; i < Cycic.pane.getChildren().size(); i++){
						if(Cycic.pane.getChildren().get(i).getId() == "cycicNode"){
							((Shape) Cycic.pane.getChildren().get(i)).setStroke(Color.BLACK);
							((Shape) Cycic.pane.getChildren().get(i)).setStrokeWidth(1);
						}
					}
					clone.setStrokeWidth(5);
					clone.setStroke(Color.DARKGRAY);
				}
			}
		});
		
		// Used for tracking the objects //
		parent.cycicCircle.childrenLinks.add(parentChild);	
		
		// If applicable adding node to CYCIC pane.
		int childIndex = parent.cycicCircle.childrenList.size();
		if(parentChildShow == true){	
			Cycic.pane.getChildren().add(parent.cycicCircle.childrenList.get(childIndex-1));
			Cycic.pane.getChildren().add(parent.cycicCircle.childrenList.get(childIndex-1).text);
			Cycic.pane.getChildren().add(parentChild.line);
			parentChild.line.toBack();
		}
	}
	
	/**
	 * Function to completely remove a child facilityCircle from the simulation.
	 * @param child This is the name of the clone to be removed. 
	 * @param parent Parent of the clone being removed.
	 */
	static void removeClone(facilityNode child, facilityNode parent){
		for(int i = 0; i < DataArrays.Links.size(); i++){
			if(DataArrays.Links.get(i).source == child.cycicCircle){
				DataArrays.Links.remove(i);
			}
		}
		for(int i = 0; i < parent.cycicCircle.childrenList.size(); i++){
			if(parent.cycicCircle.childrenList.get(i) == child.cycicCircle){
				parent.cycicCircle.childrenList.remove(i);
				parent.cycicCircle.childrenLinks.remove(i);
			}
		}
		for( int i = 0; i < parent.facilityClones.size(); i++){
			if(parent.facilityClones.get(i) == child){
				parent.facilityClones.remove(i);
			}
		}
		VisFunctions.redrawPane();
	}
}
