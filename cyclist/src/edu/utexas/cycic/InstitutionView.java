package edu.utexas.cycic;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

public class InstitutionView extends ViewBase {
	public InstitutionView() {
		super();
		TITLE = (String) InstitutionCorralView.workingInstitution.name;
		workingInstitution = InstitutionCorralView.workingInstitution;
		//Institution list view for the region.

		ListView<String> facilityList = new ListView<String>(){
			{
				setOrientation(Orientation.VERTICAL);
				setOnMousePressed(new EventHandler<MouseEvent>(){
					public void handle(MouseEvent event){
						if (event.isSecondaryButtonDown()){
							workingInstitution.availFacilities.remove(getSelectionModel().getSelectedItem());
							getItems().remove(getSelectionModel().getSelectedItem());
						}
					}
				});
				for(facilityItem fac: workingInstitution.availFacilities){
					getItems().add(fac.name + " - " + fac.number);
				}
			}
		};
		
		topGrid.add(new Label(InstitutionCorralView.workingInstitution.type), 2, 0);

		// Setting up the view visuals.
		topGrid.setHgap(10);
		topGrid.setVgap(2);

		topGrid.add(new Label("Name"), 0, 0);
		topGrid.add(FormBuilderFunctions.institNameBuilder(InstitutionCorralView.workingInstitution), 1, 0);
		
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
		topGrid.add(protoNumber, 3, 2);
		Button protoButton = new Button("Add");
		protoButton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				facilityItem fac = new facilityItem(protoName.getValue(), Integer.parseInt(protoNumber.getText()));
				workingInstitution.availFacilities.add(fac);
				facilityList.getItems().add(fac.name + " - " + fac.number);
			}
		});
		topGrid.add(protoButton, 4, 2);
		grid.autosize();
		grid.setAlignment(Pos.BASELINE_CENTER);
		grid.setVgap(10);
		grid.setHgap(5);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.setStyle("-fx-background-color: silver;");

		VBox regionGridBox = new VBox();
		regionGridBox.getChildren().addAll(topGrid, grid);		
		


		VBox listBox = new VBox();
		listBox.getChildren().addAll(new Label("Initial Facilities"), facilityList);
		HBox overView = new HBox();
		overView.getChildren().addAll(listBox, regionGridBox);
		
		setTitle(TITLE);
		setContent(overView);
		setPrefSize(600,400);		
		formBuilder(InstitutionCorralView.workingInstitution.institStruct, InstitutionCorralView.workingInstitution.institData);

	}

	//private ComboBox<String> structureCB = new ComboBox<String>();
	private GridPane grid = new GridPane();
	private GridPane topGrid = new GridPane();
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
	public void formBuilder(ArrayList<Object> facArray, ArrayList<Object> dataArray){
		if (facArray.size() == 0){
			grid.add(new Label("This archetype is empty."), 0, 0);
			return;
		}
		for (int i = 0; i < facArray.size(); i++){
			if (facArray.get(i) instanceof ArrayList && facArray.get(0) instanceof ArrayList) {
				formBuilder((ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i));
			} else if (i == 0){
				if (facArray.get(2) == "oneOrMore"){
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = new Label((String) facArray.get(0));
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), 1+columnNumber, rowNumber);
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
						Label name = new Label((String) facArray.get(0));
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), 1+columnNumber, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							grid.add(arrayListRemove(dataArray, ii), columnNumber-1, rowNumber);
							formBuilder((ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else if (facArray.get(2) == "input" || facArray.get(2) == "output") {
					if ((int)facArray.get(6) <= userLevel){
						Label name = new Label((String) facArray.get(0));
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
					Label name = new Label((String) facArray.get(0));
					name.setTooltip(new Tooltip((String) facArray.get(7)));
					grid.add(name, columnNumber, rowNumber);
					// Setting up the input type for the label
					if (facArray.get(4) != null){
						// If statement to test for a continuous range for sliders.
						if (facArray.get(4).toString().split("[...]").length > 1){
							Slider slider = FormBuilderFunctions.sliderBuilder(facArray.get(4).toString(), dataArray.get(0).toString());
							TextField textField = FormBuilderFunctions.sliderTextFieldBuilder(slider, dataArray);
							grid.add(slider, 1+columnNumber, rowNumber);
							grid.add(textField, 2+columnNumber, rowNumber);
							columnEnd = 2+columnNumber+1;
							// Slider with discrete steps
						} else {
							ComboBox<String> cb = FormBuilderFunctions.comboBoxBuilder(facArray.get(4).toString(), dataArray);
							grid.add(cb, 1+columnNumber, rowNumber);
							columnEnd = 2 + columnNumber;
						}
					} else {
						switch ((String) facArray.get(2).toString().toLowerCase()) {
						case "prototype":
							grid.add(FormBuilderFunctions.comboBoxFac(dataArray), 1+columnNumber, rowNumber);
							break;
						case "commodity":
							grid.add(FormBuilderFunctions.comboBoxCommod(dataArray), 1+columnNumber, rowNumber);
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
				formBuilder(workingInstitution.institStruct, workingInstitution.institData);
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
				formBuilder(workingInstitution.institStruct, workingInstitution.institData);
			}
		});		

		return button;
	}
}
