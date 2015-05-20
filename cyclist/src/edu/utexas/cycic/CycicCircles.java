package edu.utexas.cycic;

import java.util.Optional;

import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utexas.cycic.tools.FormBuilderTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

/**
 * The class used to build a new Prototype Facility node.
 * @author Robert
 *
 */
public class CycicCircles{

	protected static double mousey;
	protected static double mousex;
	protected static double x;
	protected static double y;
	static Window window;
	
	/**
	 * Function to build a prototype facility node. This node will contain
	 * the facilities available to institutions and regions. All children 
	 * built from this node will mimic its structure. 
	 * @param name Name of the new prototype facility.
	 */
	static FacilityCircle addNode(String name, final facilityNode parent) {

		final FacilityCircle circle = parent.cycicCircle;
		circle.setRadius(45);
		circle.setCenterX(60);
		circle.setCenterY(60);
		circle.type = "Parent";
		circle.childrenShow = true;
		
		//Setting up the name and naming structure of the circle.
		circle.text.setText(name);
		circle.tooltip.setText(name);
		circle.text.setTooltip(circle.tooltip);
		circle.text.setWrapText(true);
		circle.text.setLayoutX(circle.getCenterX()-circle.getRadius()*0.6);
		circle.text.setLayoutY(circle.getCenterY()-circle.getRadius()*0.6);	
		circle.text.setTextAlignment(TextAlignment.CENTER);
		circle.text.setMaxWidth(circle.getRadius()*1.4);
		circle.text.setMouseTransparent(true);
		circle.text.setFont(new Font("ComicSans", 14));
		circle.text.setMaxHeight(circle.getRadius()*1.2);
		
		
		
		// Setting the circle color //
		circle.setStroke(Color.BLACK);
		circle.rgbColor=VisFunctions.stringToColor(parent.facilityType);
		circle.setFill(Color.rgb(circle.rgbColor.get(0), circle.rgbColor.get(1), circle.rgbColor.get(2), 0.9));
		// Setting font color for visibility //
		if(VisFunctions.colorTest(circle.rgbColor) == true){
			circle.text.setTextFill(Color.BLACK);
		}else{
			circle.text.setTextFill(Color.WHITE);
		}
		for(int i = 0; i < Cycic.pane.getChildren().size(); i++){
			if(Cycic.pane.getChildren().get(i).getId() == "cycicNode"){
				((Shape) Cycic.pane.getChildren().get(i)).setStroke(Color.BLACK);
				((Shape) Cycic.pane.getChildren().get(i)).setStrokeWidth(1);
			}
		}
		circle.setEffect(VisFunctions.lighting);
		circle.setStrokeWidth(5);
		circle.setStroke(Color.DARKGRAY);
		
		// Adding the menu and it's menu items.
		final Menu menu1 = new Menu("Options");
		
		MenuItem facForm = new MenuItem("Facility Form");
		EventHandler<ActionEvent> circleAction = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try{
					CyclistController._presenter.addTool(new FormBuilderTool());
					circle.menu.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		facForm.setOnAction(circleAction);
		
		circle.image.setLayoutX(circle.getCenterX()-60);
		circle.image.setLayoutY(circle.getCenterY()-60);
		
		MenuItem delete = new MenuItem("Delete");
		
		delete.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				deleteNode(parent);
				circle.menu.setVisible(false);
			}
		});
		
		MenuItem changeNiche = new MenuItem("Change Niche");
		changeNiche.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				/** TODO CHANGE NICHE OF CIRCLE */
				TextInputDialog dg = new TextInputDialog("Test");
				dg.setContentText("ADSFA");
				Optional<String> result = dg.showAndWait();
				if (result.isPresent()){
				    System.out.println("Your name: " + result.get());
				}
				circle.menu.setVisible(false);
			}
		});

		MenuItem showImage = new MenuItem("Show Image");
		showImage.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				circle.image.setVisible(true);
				circle.image.toBack();
				circle.setOpacity(0);	
				circle.menu.setVisible(false);
			}
		});
		
		MenuItem hideImage = new MenuItem("Hide Image");
		hideImage.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				circle.image.setVisible(false);
				circle.setOpacity(100);
				circle.menu.setVisible(false);			}
		});
		
		menu1.getItems().addAll(facForm, changeNiche, delete, showImage, hideImage);
		circle.menu.getMenus().add(menu1);
		circle.menu.setLayoutX(circle.getCenterX());
		circle.menu.setLayoutY(circle.getCenterY());
		circle.menu.setVisible(false);
		
		// Piece of test code for changing the look of the facility circles.
		//circle.image.setImage(new Image("reactor.png"));
		circle.image.setLayoutX(circle.getCenterX()-60);
		circle.image.setLayoutY(circle.getCenterY()-60);
		circle.image.setMouseTransparent(true);
		circle.image.setVisible(false);
		
		// Mouse pressed to add in movement of the facilityCircle.
		circle.onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				Cycic.workingNode = parent;
				circle.childrenDeltaX.clear();
				circle.childrenDeltaY.clear();
				for(int i = 0; i < circle.childrenList.size(); i++){
					circle.childrenDeltaX.add(circle.getCenterX() - circle.childrenList.get(i).getCenterX());
					circle.childrenDeltaY.add(circle.getCenterY() - circle.childrenList.get(i).getCenterY());
				}
				x = circle.getCenterX() - event.getX();
				y = circle.getCenterY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});
		
		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		circle.onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event){
				Line line = new Line();
				if(event.isShiftDown() == true){
					Cycic.pane.getChildren().add(line);
					line.setEndX(event.getX());
					line.setEndY(event.getY());
					line.setStartX(circle.getCenterX());
					line.setStartY(circle.getCenterY());
				} else {
					circle.setCenterX(mousex+x);
					circle.setCenterY(mousey+y);

					if(circle.getCenterX() <= Cycic.pane.getLayoutBounds().getMinX()+circle.getRadius()){
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
					}

					circle.menu.setLayoutX(circle.getCenterX());
					circle.menu.setLayoutY(circle.getCenterY());

					circle.image.setLayoutX(circle.getCenterX()-60);
					circle.image.setLayoutY(circle.getCenterY()-50);

					circle.text.setLayoutX(circle.getCenterX()-circle.getRadius()*0.6);
					circle.text.setLayoutY(circle.getCenterY()-circle.getRadius()*0.6);	

					for(int i = 0; i < CycicScenarios.workingCycicScenario.Links.size(); i++){
						if(CycicScenarios.workingCycicScenario.Links.get(i).source == circle){
							CycicScenarios.workingCycicScenario.Links.get(i).line.setStartX(circle.getCenterX());
							CycicScenarios.workingCycicScenario.Links.get(i).line.setStartY(circle.getCenterY());
							CycicScenarios.workingCycicScenario.Links.get(i).line.updatePosition();
						}
						if(CycicScenarios.workingCycicScenario.Links.get(i).target == circle){
							CycicScenarios.workingCycicScenario.Links.get(i).line.setEndX(circle.getCenterX());
							CycicScenarios.workingCycicScenario.Links.get(i).line.setEndY(circle.getCenterY());
							CycicScenarios.workingCycicScenario.Links.get(i).line.updatePosition();
						}
					}
					for(int i = 0; i < circle.childrenLinks.size(); i++){
						circle.childrenLinks.get(i).line.setStartX(circle.getCenterX());
						circle.childrenLinks.get(i).line.setStartY(circle.getCenterY());
						circle.childrenList.get(i).setCenterX(mousex-circle.childrenDeltaX.get(i)+x);
						circle.childrenList.get(i).setCenterY(mousey-circle.childrenDeltaY.get(i)+y);
						circle.childrenLinks.get(i).line.setEndX(circle.childrenList.get(i).getCenterX());
						circle.childrenLinks.get(i).line.setEndY(circle.childrenList.get(i).getCenterY());
						circle.childrenList.get(i).menu.setLayoutX(circle.childrenList.get(i).getCenterX());
						circle.childrenList.get(i).menu.setLayoutY(circle.childrenList.get(i).getCenterY());
						circle.childrenList.get(i).text.setLayoutX(circle.childrenList.get(i).getCenterX()-circle.childrenList.get(i).getRadius()*0.6);
						circle.childrenList.get(i).text.setLayoutY(circle.childrenList.get(i).getCenterY()-circle.childrenList.get(i).getRadius()*0.6);
						for(int ii = 0; ii < CycicScenarios.workingCycicScenario.Links.size(); ii++){
							if(circle.childrenList.get(i) == CycicScenarios.workingCycicScenario.Links.get(ii).source){
								CycicScenarios.workingCycicScenario.Links.get(ii).line.setStartX(circle.childrenList.get(i).getCenterX());
								CycicScenarios.workingCycicScenario.Links.get(ii).line.setStartY(circle.childrenList.get(i).getCenterY());
								CycicScenarios.workingCycicScenario.Links.get(ii).line.updatePosition();
							}
						}
					}
					mousex = event.getX();
					mousey = event.getY();
				}
				event.consume();
				/*if(event.isConsumed()){
					Cycic.pane.getChildren().remove(line);	
				}*/
			}
		});
		// Double click functionality to show/hide children. As well as a bloom feature to show which node is selected.
		circle.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(event.getButton().equals(MouseButton.SECONDARY)){
					circle.menu.setVisible(true);
					event.consume();
				}
				if(event.isAltDown() && event.isControlDown()){
					
				}
				if (event.getClickCount() >= 2){
                    event.consume();
					CyclistController._presenter.addTool(new FormBuilderTool());
				} else if (event.getButton().equals(MouseButton.PRIMARY)){
					for(int i = 0; i < Cycic.pane.getChildren().size(); i++){
						if(Cycic.pane.getChildren().get(i).getId() == "cycicNode"){
							((Shape) Cycic.pane.getChildren().get(i)).setStroke(Color.BLACK);
							((Shape) Cycic.pane.getChildren().get(i)).setStrokeWidth(1);
						}
					}
					circle.setStrokeWidth(5);
					circle.setStroke(Color.DARKGRAY);
				}
			}
		});
		
		Cycic.workingScenario.FacilityNodes.add(parent);
		
		// Code for allow a shift + (drag and drop) to start a new facility form for this facilityCircle.
		/*circle.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				if(event.isShiftDown() == true){
					System.out.print("YAY");
					/*DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
					clipboard.put(DnD.TOOL_FORMAT, Tool.class, new FormBuilderTool());
					
					Dragboard db = circle.startDragAndDrop(TransferMode.COPY);
					//Dragboard db = circle.startDragAndDrop(TransferMode.NONE);
					ClipboardContent content = new ClipboardContent();				
					content.put(DnD.TOOL_FORMAT, "Facility Form");
					db.setContent(content);
					Line line = new Line();
					Cycic.pane.getChildren().add(line);
					line.setEndX(event.getX());
					line.setEndY(event.getY());
					line.setStartX(circle.getCenterX());
					line.setStartY(circle.getCenterY());
					event.consume();
					Cycic.pane.getChildren().remove(line);
				}
			}
		});*/
		
		circle.setOnMouseEntered(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				circle.setRadius(52);
			}
		});
		
		circle.setOnMouseExited(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				circle.setRadius(45);
			}
		});
		
		// Adding facilityCircle to the pane. 
		Cycic.pane.getChildren().add(circle);
		Cycic.pane.getChildren().add(circle.menu);
		Cycic.pane.getChildren().add(circle.text);
		Cycic.pane.getChildren().add(circle.image);
		circle.image.toBack();
		
		return circle;
	}
	
	/**
	 * Removes a facilityCircle from the simulation.
	 * @param circle The facilityCircle to be removed. 
	 */
	static void deleteNode(facilityNode node){
		for(int i = 0; i < CycicScenarios.workingCycicScenario.Links.size(); i++){
			if(CycicScenarios.workingCycicScenario.Links.get(i).source == node.cycicCircle){
				CycicScenarios.workingCycicScenario.Links.remove(i);
			}
		}
		for(int i = 0; i < CycicScenarios.workingCycicScenario.FacilityNodes.size(); i++){
			if(CycicScenarios.workingCycicScenario.FacilityNodes.get(i) == node){
				for(int ii = 0; ii < CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.size();ii++){
					for(int iii = 0; iii < CycicScenarios.workingCycicScenario.Links.size(); iii++){
						if(CycicScenarios.workingCycicScenario.Links.get(i).source == CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(iii)){
							CycicScenarios.workingCycicScenario.Links.remove(i);
						}
					}
				}
				CycicScenarios.workingCycicScenario.FacilityNodes.remove(i);
			}
		}
		for(int i = 0; i < CycicScenarios.workingCycicScenario.FacilityNodes.size(); i++){
			for(int ii = 0; ii < CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.size(); ii++){
				CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(ii).parentIndex = i;
			}
		}
		VisFunctions.marketHide();

	}
}