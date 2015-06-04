package edu.utexas.cycic;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
/**
 * A view used to build and develop institutions for the simulation 
 * currently being built. 
 * @author Robert
 *
 */
public class InstitutionCorralView extends ViewBase{
	/**
	 * 
	 */
	static instituteNode workingInstitution = null;
	
	/**
	 * 
	 */
	ArrayList<String> prototypeList;
	
	/**
	 * 
	 */
	MemoryScroll facilityScroll = new MemoryScroll(35, 35, 65, prototypeList);

	/**
	 * 
	 */
	static Pane institutionPane = new Pane(){
		{
			setPrefHeight(375);
			setPrefWidth(630);
			setStyle("-fx-background-color: #555555");
			setOnDragDropped(new EventHandler<DragEvent>(){
				public void handle(DragEvent event){
					if(event.getDragboard().hasContent(DnD.VALUE_FORMAT)){
						instituteNode institute = new instituteNode();
						institute.type = event.getDragboard().getContent(DnD.VALUE_FORMAT).toString();
						institute.type.trim();
						for (int i = 0; i < DataArrays.simInstitutions.size(); i++){
							if(DataArrays.simInstitutions.get(i).institName.equalsIgnoreCase(institute.type)){
								institute.institStruct = DataArrays.simInstitutions.get(i).institStruct;
								institute.archetype = DataArrays.simInstitutions.get(i).institArch;
							}
						}
						event.consume();
						institute.name = "";
						workingInstitution = institute;
						FormBuilderFunctions.formArrayBuilder(institute.institStruct, institute.institData);
						institute.institutionShape = InstitutionShape.addInst((String)institute.name, institute);
						institute.institutionShape.setCenterX(event.getX());
						institute.institutionShape.setCenterY(event.getY());
						VisFunctions.placeTextOnEllipse(institute.institutionShape, "middle");
						DataArrays.institNodes.add(institute);
						institutionPane.getChildren().addAll(institute.institutionShape, institute.institutionShape.text, institute.institutionShape.menuBar);
					} else {
						event.consume();
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
	 * Initiates a new window for building and modifying institutions. 
	 */
	public InstitutionCorralView(){
		super();

		DataArrays.cycicInitLoader();

		TextField institName = new TextField();
		
		final ComboBox<String> typeOptions = new ComboBox<String>();
		typeOptions.getItems().clear();
		for(int i = 0; i < DataArrays.simInstitutions.size(); i++){
			typeOptions.getItems().add(DataArrays.simInstitutions.get(i).institName);
		}
		
		Button institButton = new Button("Add Institution");
		/** TODO This literally does nothing. */
		institButton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				instituteNode tempInstit = new instituteNode();
				tempInstit.name = institName.getText();
				workingInstit = tempInstit;
				for(int i = 0; i < DataArrays.simInstitutions.size(); i++){
					if(DataArrays.simInstitutions.get(i).institName.equalsIgnoreCase((String) typeOptions.getValue())){
						tempInstit.institStruct = DataArrays.simInstitutions.get(i).institStruct;
						tempInstit.archetype = DataArrays.simInstitutions.get(i).institArch;
					}
				}
				tempInstit.type = (String) typeOptions.getValue();
				FormBuilderFunctions.formArrayBuilder(workingInstit.institStruct, workingInstit.institData);
				DataArrays.institNodes.add(tempInstit);
				new Label("Name");
				FormBuilderFunctions.institNameBuilder(workingInstit);				
			}
		});
		
		
		topGrid.add(new Label("New Institution"), 0, 0);
		topGrid.add(institName, 1, 0);
		topGrid.add(typeOptions, 2, 0);
		topGrid.add(institButton, 3, 0);
		
				
		
		//ComboBox for adding a new facility to the initial facility array of the institution.
		
		// Building the grids for the views.
		topGrid.setHgap(10);
		topGrid.setVgap(5);
		
		ScrollPane scroll = new ScrollPane();
		scroll.autosize();
		Pane nodesPane = new Pane();
		nodesPane.autosize();
		for(int i = 0; i < DataArrays.simInstitutions.size(); i++){
			InstitutionEllipse instit = new InstitutionEllipse();
			String instName = DataArrays.simInstitutions.get(i).institName;
			instit.name = instName;
			String instLabel = instName.split(" ")[2] + " (" + instName.split(" ")[1] + ")";
			ArrayList<Integer> rgbColor = VisFunctions.stringToColor(instName);
			instit.setFill(VisFunctions.pastelize(Color.rgb(rgbColor.get(0),rgbColor.get(1),rgbColor.get(2))));
			instit.setLayoutX(60 + (i*110));
			instit.setLayoutY(40);
			instit.setRadiusX(50);
			instit.setRadiusY(30);
			instit.setStroke(Color.BLACK);
			instit.text.setText(instLabel);
			VisFunctions.placeTextOnEllipse(instit,"middle");
			nodesPane.getChildren().addAll(instit, instit.text);
		}
		scroll.setContent(nodesPane);
		
		topGrid.autosize();
		
		VBox institBox = new VBox(15);
		institBox.getChildren().addAll(topGrid, scroll, institutionPane);	
		setContent(institBox);

	}
	
	private GridPane topGrid = new GridPane(){
		{
			setHgap(10);
			setVgap(5);
		}
	};
	static instituteNode workingInstit;
	public static String TITLE;


}
