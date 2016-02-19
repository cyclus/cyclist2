package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * A View Class for generating the form required for interacting with a Cyclus region.
 * @author Robert
 *
 */
public class RegionView extends ViewBase{

	/**
	 * Init function for this class. Generates the top grids and form gridpane. 
	 */
	public RegionView(){
		super();
		TITLE = (String) RegionCorralView.workingRegion.name;
		workingRegion = RegionCorralView.workingRegion;
		
		setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				RegionCorralView.workingRegion = workingRegion;
			}
		});
		
		//Institution list view for the region.
		final ListView<String> institList = new ListView<String>();
		institList.setOrientation(Orientation.VERTICAL);
		institList.setMinHeight(25);
		institList.setMaxWidth(120);

        ContextMenu listCtxtMenu = new ContextMenu();
        MenuItem removeInst = new MenuItem("Remove Institution");
        removeInst.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    workingRegion.institutions.remove(institList.getSelectionModel().getSelectedItem());
                    institList.getItems().remove(institList.getSelectionModel().getSelectedItem());
                }
            });
        listCtxtMenu.getItems().add(removeInst);

		institList.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
				if (event.isSecondaryButtonDown()){
                    listCtxtMenu.show(institList,event.getScreenX(),event.getScreenY());
				}
			}
		});		
		for(int i = 0; i < workingRegion.institutions.size(); i++){
			institList.getItems().add(workingRegion.institutions.get(i));
		}
		Label button = new Label(RegionCorralView.workingRegion.type);
		button.setText(RegionCorralView.workingRegion.type);

		topGrid.add(button, 2, 0);
		
		// Code to add new institution to region.
		final ComboBox<String> addNewInstitBox = new ComboBox<String>();
		addNewInstitBox.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent event){
				addNewInstitBox.getItems().clear();
				for (instituteNode instit: CycicScenarios.workingCycicScenario.institNodes){
					addNewInstitBox.getItems().add(instit.name);
				}
			}
		});
		Button addInstit = new Button();
		
		addInstit.setText("Add Institution");
		addInstit.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				if (!addNewInstitBox.getValue().equals("")) {
					institList.getItems().clear();
					workingRegion.institutions.add(addNewInstitBox.getValue());
					for (String instit: workingRegion.institutions){
						institList.getItems().add(instit);
					}
				}
			}
		});
		Label addInst = new Label("Adding Institutions"){
			{
				setTooltip(new Tooltip("Use to add institutions to a region."));
				setOnMouseClicked(FormBuilderFunctions.helpDialogHandler("To add an institution to a region select an institution from the drop down menu and " +
						"click the 'Add Institution' button. An institution may exist in only one region. If you wish to have identical institution in regions " +
						"you will need to add two of the exact same institution. Ideally you will name each institution <institutionname - region>."));
				setFont(new Font(14));				
			}
		};
		
		Label protoDetails = new Label("Region Details"){
			{
				setTooltip(new Tooltip("Use to set the details of this region"));
				setOnMouseClicked(FormBuilderFunctions.helpDialogHandler("In this form the user can enter information into the region archetype to make it a cyclus region. " +
						"The region will take on the properties listed in the table below during the cyclus simulation. This form is derived from the module developer's code. " +
						"If you have questions about the form fields you may hover over or double click on the form field name for more information. If this is not enough help you may "+
						"the module developer / cyclus developer team. "));
				setFont(new Font(14));							
			}
		};
		titleGrid.add(protoDetails, 0, 5);
		titleGrid.setVgap(1);
		titleGrid.setStyle("-fx-background-color:transparent;");
		
		// Setting up the view visuals.
		topGrid.add(new Label("Name"), 0, 0);
		topGrid.add(FormBuilderFunctions.regionNameBuilder(RegionCorralView.workingRegion), 1, 0);
		topGrid.add(addInst, 0, 2);
		topGrid.add(addNewInstitBox, 0, 3);
		topGrid.add(addInstit, 1, 3);
		topGrid.setPadding(new Insets(0, 0, 5, 0));
		topGrid.setHgap(10);
		topGrid.setVgap(2);
		topGrid.setStyle("-fx-border-color: black;");

		
		grid.autosize();
		grid.setAlignment(Pos.BASELINE_CENTER);
		grid.setVgap(10);
		grid.setHgap(5);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.setStyle("-fx-background-color: transparent;");
		
		ScrollPane scroll = new ScrollPane();
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setMaxHeight(500);
		scroll.setContent(grid);
		VBox institBox = new VBox(new Label("Institutions"), institList);
		institBox.setStyle("-fx-border-color: black;");
		VBox gridBox = new VBox(titleGrid, scroll);
		gridBox.setStyle("-fx-background-color: #CCCCCC; -fx-border-color: black;");
		VBox regionGridBox = new VBox(topGrid, gridBox);
		
		HBox regionBox = new HBox(institBox, regionGridBox);
		
		setTitle(TITLE);
		setContent(regionBox);
		formBuilder(workingRegion.regionStruct, workingRegion.regionData);
		
	}
	
	//private ComboBox<String> structureCB = new ComboBox<String>();
	GridPane titleGrid = new GridPane();
	GridPane grid = new GridPane();
	GridPane topGrid = new GridPane();
	private int rowNumber = 0;
	private int columnNumber = 0;
	private int columnEnd = 0;
	private int userLevel = 0;
	public static String TITLE;
	static regionNode workingRegion;

	/**
	 * This function takes a constructed data array and it's corresponding facility structure array and creates
	 * a form in for the structure and data array and facility structure.
	 * @param facArray This is the structure of the data array. Included in this array should be all of the information
	 * needed to fully describe the data structure of a facility.
	 * @param dataArray The empty data array that is associated with this facility. It should be built to match the structure
	 * of the facility structure passed to the form. 
	 */
	@SuppressWarnings("unchecked")
	public void formBuilder(ArrayList<Object> facArray, ArrayList<Object> dataArray){
		if (facArray.size() == 0){
			grid.add(new Label("This archetype has no form to fill out."), 0, 0);
			return;
		}
		for (int i = 0; i < facArray.size(); i++){
			if (facArray.get(i) instanceof ArrayList && facArray.get(0) instanceof ArrayList) {
				formBuilder((ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i));
			} else if (i == 0){
				if (facArray.get(2) == "oneOrMore"){
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								grid.add(arrayListRemove(dataArray, ii), columnNumber-1, rowNumber);
							}
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
						
					}
				} else if (facArray.get(2) == "oneOrMoreMap"){
					//facArray = (ArrayList<Object>) facArray.get(1);
					//dataArray = (ArrayList<Object>) dataArray.get(0);
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								grid.add(arrayListRemove(dataArray, ii), columnNumber-1, rowNumber);
							}
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
					}				
				} else if (facArray.get(2) == "zeroOrMore") {
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						// Indenting a sub structure
						rowNumber += 1;
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							grid.add(arrayListRemove(dataArray, ii), columnNumber-1, rowNumber);
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
						rowNumber += 1;
					}
				} else if (facArray.get(1) instanceof ArrayList) {
					if ((int)facArray.get(6) <= userLevel){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));						
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else {
					// Adding the label
					Label name = FormBuilderFunctions.nameLabelMaker(facArray);
					grid.add(name, columnNumber, rowNumber);
					// Setting up the input type for the label
					if (facArray.get(4) != null){
						// If statement to test for a continuous range for sliders.
						if (facArray.get(4).toString().split("[...]").length > 1){
							Slider slider = FormBuilderFunctions.sliderBuilder(facArray.get(4).toString(), dataArray.get(0).toString());
							TextField textField = FormBuilderFunctions.sliderTextFieldBuilder(slider, facArray, dataArray);
							grid.add(slider, 1+columnNumber, rowNumber);
							grid.add(textField, 2+columnNumber, rowNumber);
							columnEnd = 2+columnNumber+1;
						// Slider with discrete steps
						} else {
							ComboBox<String> cb = FormBuilderFunctions.comboBoxBuilder(facArray.get(4).toString(), facArray, dataArray);
							grid.add(cb, 1+columnNumber, rowNumber);
							columnEnd = 2 + columnNumber;
						}
					} else {
						switch ((String) facArray.get(0)) {
						case "prototype":
							grid.add(FormBuilderFunctions.comboBoxFac(facArray, dataArray), 1+columnNumber, rowNumber);
							break;
						case "commodity":
							grid.add(FormBuilderFunctions.comboBoxCommod(facArray, dataArray), 1+columnNumber, rowNumber);
						default:
							grid.add(FormBuilderFunctions.textFieldBuilder(facArray, (ArrayList<Object>)dataArray), 1+columnNumber, rowNumber);
							columnEnd = 2 + columnNumber;
							break;
						}
					}
					grid.add(FormBuilderFunctions.unitsBuilder((String)facArray.get(3)), columnEnd, rowNumber);
					columnEnd = 0;
					rowNumber += 1;
				}
			}
		}
	}
	
	
	
	/**
	 * Function to add an orMore button to the form. This button allows the user to add additional fields to zeroOrMore or oneOrMore form inputs.
	 * @param grid This is the grid of the current view. 
	 * @param facArray The ArrayList<Object> used to make a copy of the one or more field. 
	 * @param dataArray The ArrayList<Object> the new orMore field will be added to.
	 * @return Button that will add the orMore field to the dataArray and reload the form.
	 */
	public Button orMoreAddButton(final GridPane grid, final ArrayList<Object> facArray,final ArrayList<Object> dataArray){
		Button button = new Button();
		button.setText("Add");
		
		button.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
 				FormBuilderFunctions.formArrayBuilder(facArray, (ArrayList<Object>) dataArray);
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(workingRegion.regionStruct, workingRegion.regionData);
			}
		});
		return button;
	}

	/**
	 * This function removes a orMore that has been added to a particular field.
	 * @param dataArray The ArrayList<Object> containing the orMore field
	 * @param dataArrayNumber the index number of the orMore field that is to be removed.
	 * @return Button for executing the commands in this function.
	 */
	public Button arrayListRemove(final ArrayList<Object> dataArray, final int dataArrayNumber){
		Button button = new Button();
		button.setText("Remove");
		
		button.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e) {
				dataArray.remove(dataArrayNumber);
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(workingRegion.regionStruct, workingRegion.regionData);
			}
		});		
		
		return button;
	}
}
