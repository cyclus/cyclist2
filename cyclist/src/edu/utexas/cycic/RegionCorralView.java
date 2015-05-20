package edu.utexas.cycic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

public class RegionCorralView extends ViewBase {

	/**
	 * 
	 */
	static regionNode workingRegion = null; 

	/**
	 * 
	 */
	static Pane corralPane = new Pane(){
		{
			setPrefHeight(375);
			setPrefWidth(630);
			setOnDragDropped(new EventHandler<DragEvent>(){
				public void handle(DragEvent event){
					if(event.getDragboard().hasContent(DnD.VALUE_FORMAT)){
						regionNode region = new regionNode();
						region.type = event.getDragboard().getContent(DnD.VALUE_FORMAT).toString();
						region.type.trim();
						for (int i = 0; i < DataArrays.simRegions.size(); i++){
							if(DataArrays.simRegions.get(i).regionName.equalsIgnoreCase(region.type)){
								region.regionStruct = DataArrays.simRegions.get(i).regionStruct;
								region.archetype = DataArrays.simRegions.get(i).regionArch;
							}
						}
						event.consume();
						region.name = "";
						workingRegion = region;
						FormBuilderFunctions.formArrayBuilder(region.regionStruct, region.regionData);
						regionNode.regionCircle = RegionShape.addRegion((String)region.name, region);
						regionNode.regionCircle.setX(event.getX());
						regionNode.regionCircle.setY(event.getY());
						regionNode.regionCircle.text.setLayoutX(event.getX()+regionNode.regionCircle.getHeight()*0.2);
						regionNode.regionCircle.text.setLayoutY(event.getY()+regionNode.regionCircle.getHeight()*0.2);
						DataArrays.regionNodes.add(region);
						corralPane.getChildren().addAll(regionNode.regionCircle, regionNode.regionCircle.text, regionNode.regionCircle.menuBar);
					}
				}
			});
			setOnDragOver(new EventHandler <DragEvent>(){
				public void handle(DragEvent event){
					event.acceptTransferModes(TransferMode.ANY);
				}
			});
		}
	};

	/**
	 * 
	 */
	static GridPane regionCorralGrid = new GridPane(){
		{
			setHgap(10);
			setVgap(5);
		}
	};
	
	/**
	 * 
	 */
	static HBox unassociatedFacilityList = new HBox(10);
	
	/**
	 * 
	 */
	public static void addUnassInstit(){
		unassociatedFacilityList.getChildren().add(new Circle());
	}
	
	/**
	 * 
	 */
	public RegionCorralView() {
		
		// Create content for RegionCorralView header 
		final Label regionLabel = new Label("Region Name:");
		regionLabel.setFont(new Font(12));
		regionCorralGrid.add(regionLabel, 0, 0);

		final TextField regionText = new TextField();
		regionCorralGrid.add(regionText, 1, 0);

		final ComboBox<String> typeOptions = new ComboBox<String>();
		typeOptions.getItems().clear();
		for(int i = 0; i < DataArrays.simRegions.size(); i++){
			typeOptions.getItems().add(DataArrays.simRegions.get(i).regionName);
		}
		regionCorralGrid.add(typeOptions, 2, 0);

		final Button corralButton = new Button();
		corralButton.setText("Add");
		/** TODO THIS BUTTON DOES NOTHING AT ALL */
		regionCorralGrid.add(corralButton, 3, 0);

		final Label regionPrototypeLabel = new Label("Region Prototypes:");
		regionCorralGrid.add(regionPrototypeLabel, 0, 1);

		ScrollPane scroll = new ScrollPane();
		scroll.autosize();
		Pane nodesPane = new Pane();
		nodesPane.autosize();
		for(int i = 0; i < DataArrays.simRegions.size(); i++){
			RegionRectangle region = new RegionRectangle();
			region.setFill(Color.web("#CF5300"));
			region.setX(10 + (i*75));
			region.setY(5);
			region.setWidth(70);
			region.setHeight(70);
			region.setStroke(Color.BLACK);
			region.text.setText(DataArrays.simRegions.get(i).regionName);
			region.text.setWrapText(true);
			region.text.setMaxWidth(region.getWidth()*0.8);
			region.text.setLayoutX(region.getX()+region.getWidth()*0.1);
			region.text.setLayoutY(region.getY()+region.getHeight()*0.1);	
			region.text.setTextAlignment(TextAlignment.CENTER);
			region.text.setMouseTransparent(true);
			region.setOnDragDetected(new EventHandler<MouseEvent>(){
				public void handle(MouseEvent e){
					Dragboard db = region.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();				
					content.put(DnD.VALUE_FORMAT, region.text.getText());
					db.setContent(content);
					e.consume();
				}
			});
			nodesPane.getChildren().addAll(region,region.text);
		}
		scroll.setContent(nodesPane);

		regionCorralGrid.autosize();

		/* Place RegionCorralView header, corralPane, and footer on main corralVBox */

		VBox mainCorralVBox = new VBox(15);
		mainCorralVBox.getChildren().addAll(regionCorralGrid, scroll, corralPane);
		setContent(mainCorralVBox);

		EventHandler<MouseEvent> addRegion = new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event) {
				final regionNode region = new regionNode();
				region.type = (String) typeOptions.getValue();
				for(int i = 0; i < DataArrays.simRegions.size(); i++){
					if(DataArrays.simRegions.get(i).regionName.equalsIgnoreCase(region.type)){
						region.regionStruct = DataArrays.simRegions.get(i).regionStruct;
					}
				}
				FormBuilderFunctions.formArrayBuilder(region.regionStruct, region.regionData);
				regionNode.regionCircle = RegionShape.addRegion(regionText.getText(), region);
				
				DataArrays.regionNodes.add(region);

				corralPane.getChildren().addAll(regionNode.regionCircle, regionNode.regionCircle.text, regionNode.regionCircle.menuBar);


			}	//ends definition of EventHandler addRegion  
		};	//ends EventHandler addRegion

		corralButton.setOnMouseClicked(addRegion);
	}
}
