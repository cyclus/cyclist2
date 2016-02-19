package edu.utexas.cycic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

public class InstitutionView extends ViewBase {

    private String facNameFromListItem(String listItem) {
        // This assumes that list items always have the form "facName - ##"
        // where ## is an integer and facName may contain spaces, hyphens and numbers
        
        List<String> facNameSplit = Arrays.asList(listItem.split(" - "));
        String facName = facNameSplit.get(0);
        for (int i=1; i < facNameSplit.size()-1; i++) {
            facName = facName + " - " + facNameSplit.get(i);
        }
        return facName;

    }

	public InstitutionView() {
		super();
		TITLE = (String) InstitutionCorralView.workingInstitution.name;
		workingInstitution = InstitutionCorralView.workingInstitution;
		GridPane grid = new GridPane();
		GridPane topGrid = new GridPane();
		
		GridPane titleGrid = new GridPane();
		setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				InstitutionCorralView.workingInstitution = workingInstitution;
			}
		});

		//Initial facility list view for the institution.

		ListView<String> facilityList = new ListView<String>();
        facilityList.setOrientation(Orientation.VERTICAL);
        facilityList.autosize();
        facilityList.setMinWidth(100);

        ContextMenu listCtxtMenu = new ContextMenu();
        MenuItem removeFac = new MenuItem("Remove Initial Facility");
        removeFac.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    String selectedFac = facNameFromListItem(facilityList.getSelectionModel().getSelectedItem());
                    workingInstitution.availFacilities.remove(selectedFac);
                    facilityList.getItems().remove(facilityList.getSelectionModel().getSelectedItem());
                }
            });
		listCtxtMenu.getItems().add(removeFac);

        facilityList.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    if (event.isSecondaryButtonDown()){
                        listCtxtMenu.show(facilityList,event.getScreenX(),event.getScreenY());
                    }
                }
            });

        for(Map.Entry<String, Integer> fac : workingInstitution.availFacilities.entrySet()) {
            facilityList.getItems().add(fac.getKey() + " - " + fac.getValue());
        }

		topGrid.add(new Label(InstitutionCorralView.workingInstitution.type), 4, 0);

		// Setting up the view visuals.
		topGrid.setHgap(10);
		topGrid.setVgap(2);

		topGrid.add(new Label("Name"), 0, 0);
		topGrid.add(FormBuilderFunctions.institNameBuilder(InstitutionCorralView.workingInstitution), 1, 0, 2, 1);
		Label initFac = new Label("Add Intital Facilities"){
			{
				setTooltip(new Tooltip("Add intitial facilities to the institution here."));
				setOnMouseClicked(FormBuilderFunctions.helpDialogHandler("Initital facilities are facilities that are built into the simulation at the first timestep. " +
				"These facilities will operate during the first timestep. Adding a new intital facility will show up as <Prototype> - <Number>. This indicates " +
						"that <number> facilities of <prototype> will be built at the start of the simulation."));
				setFont(new Font(14));				
			}
		};
		topGrid.add(initFac, 0, 1);
		topGrid.add(new Label("Prototype"), 0, 2);
		ComboBox<String> protoName = new ComboBox<String>();
		protoName.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				protoName.getItems().clear();
				for(facilityNode fac:DataArrays.FacilityNodes){
					protoName.getItems().add((String) fac.name);
				}
			}
		});
		topGrid.add(protoName, 1, 2);
		topGrid.add(new Label("Number"), 2, 2);
		TextField protoNumber = new TextField(){
			@Override public void replaceText(int start, int end, String text) {
				if (!text.matches("[a-z]")){
					super.replaceText(start, end, text);
				}
			}
			
			public void replaceSelection(String text) {
				if (!text.matches("[a-z]")){
					super.replaceSelection(text);
				}
			}
		};
		protoNumber.setMinWidth(20);
		topGrid.add(protoNumber, 3, 2);
		Button protoButton = new Button("Add");
		protoButton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
                String facName = protoName.getValue();
                Integer numFac = 0;
                if (workingInstitution.availFacilities.containsKey(facName)) {
                    numFac = workingInstitution.availFacilities.get(facName);
                    facilityList.getItems().remove(facName + " - " + numFac);
                }
                numFac = numFac + Integer.parseInt(protoNumber.getText());
                workingInstitution.availFacilities.put(facName, numFac );
				facilityList.getItems().add(facName + " - " + numFac);
			}
		});
		topGrid.add(protoButton, 4, 2);
		Label protoDetails = new Label("Institution Details"){
			{
				setTooltip(new Tooltip("Use to set the details of this prototype"));
				setOnMouseClicked(FormBuilderFunctions.helpDialogHandler("In this form the user can enter information into the institution archetype to make it a cyclus institution. " +
						"The institution will take on the properties listed in the table below during the cyclus simulation. This form is derived from the module developer's code. " +
						"If you have questions about the form fields you may hover over or double click on the form field name for more information. If this is not enough help you may "+
						"the module developer / cyclus developer team. "));
				setFont(new Font(14));				
			}
		};
		titleGrid.add(protoDetails, 0, 5);
		
		titleGrid.setVgap(1);
		titleGrid.setStyle("-fx-background-color:transparent;");
		topGrid.setVgap(5);
		topGrid.setPadding(new Insets(5, 5, 5, 5));
		topGrid.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: black;");
		grid.autosize();
		grid.setAlignment(Pos.BASELINE_CENTER);
		grid.setVgap(10);
		grid.setHgap(5);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.setStyle("-fx-background-color:transparent;");
		
		ScrollPane scroll = new ScrollPane();
		scroll.setContent(grid);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setStyle("-fx-background-color:transparent;");
		scroll.setMaxHeight(500);
		VBox gridBox = new VBox(titleGrid, scroll);
		gridBox.setStyle("-fx-background-color: #CCCCCC; -fx-border-color: black;");
		VBox institutionGridBox = new VBox(topGrid, gridBox);
		scroll.setPrefWidth(gridBox.getWidth());
		VBox listBox = new VBox();
		listBox.getChildren().addAll(new Label("Initial Facilities"), facilityList);
		facilityList.setStyle("-fx-background-color:transparent;");
		listBox.setStyle("-fx-border-color: black;");
		HBox overView = new HBox(listBox, institutionGridBox);
		
		setTitle(TITLE);
		setContent(overView);	
		formBuilder(InstitutionCorralView.workingInstitution.institStruct, InstitutionCorralView.workingInstitution.institData, grid);
	}

	//private ComboBox<String> structureCB = new ComboBox<String>();
	private int rowNumber = 0;
	private int columnNumber = 0;
	private int columnEnd = 0;
	private int userLevel = 0;
	public static String TITLE;
	static instituteNode workingInstitution;

	/**
	 * This function takes a constructed data array and it's corresponding facility structure array and creates
	 * a form in for the structure and data array and facility structure.
	 * @param facArray This is the structure of the data array. Included in this array should be all of the information
	 * needed to fully describe the data structure of a facility.
	 * @param dataArray The empty data array that is associated with this facility. It should be built to match the structure
	 * of the facility structure passed to the form. 
	 */
	@SuppressWarnings("unchecked")
	public void formBuilder(ArrayList<Object> facArray, ArrayList<Object> dataArray, GridPane grid){
		if (facArray.size() == 0){
			grid.add(new Label("This archetype has no form to fill out."), 0, 0);
			return;
		}
		for (int i = 0; i < facArray.size(); i++){
			if (facArray.get(i) instanceof ArrayList && facArray.get(0) instanceof ArrayList) {
				formBuilder((ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i), grid);
			} else if (i == 0){
				if (facArray.get(2).toString().equalsIgnoreCase("oneOrMore")){
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						if(facArray.get(9) != null && !facArray.get(9).toString().equalsIgnoreCase("")){
							name.setText((String) facArray.get(9));
						} else {
							name.setText((String) facArray.get(0));	
						}
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), 1+columnNumber, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								grid.add(arrayListRemove(dataArray, ii, grid), columnNumber-1, rowNumber);
							}
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii), grid);	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else if (facArray.get(2).toString().equalsIgnoreCase("oneOrMoreMap")){
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								grid.add(arrayListRemove(dataArray, ii, grid), columnNumber-1, rowNumber);
							}
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii), grid);	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else if (facArray.get(2).toString().equalsIgnoreCase("zeroOrMore")) {
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						// Indenting a sub structure
						rowNumber += 1;
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							grid.add(arrayListRemove(dataArray, ii, grid), columnNumber-1, rowNumber);
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii), grid);	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
						rowNumber += 1;
					} 
				}else if (facArray.get(1) instanceof ArrayList) {
					if ((int)facArray.get(6) <= userLevel){
						Label name = FormBuilderFunctions.nameLabelMaker(facArray);
						grid.add(name, columnNumber, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii), grid);						
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
						switch ((String) facArray.get(2).toString().toLowerCase()) {
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
				formBuilder(workingInstitution.institStruct, workingInstitution.institData, grid);
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
	public Button arrayListRemove(final ArrayList<Object> dataArray, final int dataArrayNumber, GridPane grid){
		Button button = new Button();
		button.setText("Remove");

		button.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e) {
				dataArray.remove(dataArrayNumber);
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(workingInstitution.institStruct, workingInstitution.institData, grid);
			}
		});		

		return button;
	}
}
