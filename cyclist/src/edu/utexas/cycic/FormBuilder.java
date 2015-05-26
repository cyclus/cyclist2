package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Class that extends the View class used to build the forms for 
 * representing a facility and its input fields within Cyclus.
 * @author Robert
 */
public class FormBuilder extends ViewBase {
	/**
	 * Initiates the structures at the top of the form and the GridPane
	 * used in the form view.
	 */
	public FormBuilder(){
		super();
		formNode = Cycic.workingNode;
		TITLE = (String) Cycic.workingNode.name;
		//System.out.println(formNode.facilityStructure);
		formBuilder(grid, formNode.facilityStructure, formNode.facilityData);
		
		Button button = new Button();
		button.setText(formNode.facilityType);
		for(int i = 0; i < 4; i++){
			userLevelBox.getItems().add(String.format("%d", i));
		}
		userLevelBox.setValue(Integer.toString(userLevel));
		userLevelBox.valueProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				userLevelBox.setValue(newValue);
				userLevel = Integer.parseInt(newValue);
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(grid, formNode.facilityStructure, formNode.facilityData);
			}
		});
		
		topGrid.add(new Label("User Level"), 0, 0);
		topGrid.add(userLevelBox, 1, 0);
		topGrid.add(button, 2, 0);
		topGrid.setPadding(new Insets(10, 10, 20, 10));
		topGrid.setHgap(10);
		topGrid.setVgap(10);
		
		topGrid.add(new Label("Name"), 0, 1);
		topGrid.add(FormBuilderFunctions.nameFieldBuilder(formNode), 1, 1);
		
		grid.setAlignment(Pos.BASELINE_CENTER);
		grid.setVgap(15);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.setStyle("-fx-background-color: silver;");
		
		VBox formGrid = new VBox();
		ScrollPane scroll = new ScrollPane();
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setMaxHeight(600);
		scroll.setContent(grid);
		formGrid.getChildren().addAll(topGrid, scroll);
		
		// This is a quick hack. 
		setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				Cycic.workingNode = formNode;
			}
		});
		setTitle(TITLE);
		setContent(formGrid);
	}
	
	private ComboBox<String> userLevelBox = new ComboBox<String>();
	private GridPane grid = new GridPane();
	private GridPane topGrid = new GridPane();
	private facilityNode formNode = null;
	private int rowNumber = 0;
	private int columnNumber = 0;
	private int columnEnd = 0;
	private int userLevel= 0;
	public static String TITLE;
	
	/**
	 * Function builds a button to add a orMore button to the facility form.
	 * @param grid GridPane that the button will be added to. Also the same
	 * used to build the facility form.
	 * @param facArray ArrayList<Object> containing the data structure of the
	 * facility.
	 * @param dataArray ArrayList<Object> containing the data of the facility.
	 * @return Button that adds an orMore to said facility structure.
	 */
	public Button orMoreAddButton(final GridPane grid, final ArrayList<Object> facArray,final ArrayList<Object> dataArray){
		Button button = new Button();
		button.setText("Add " + facArray.get(0));
		
		button.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				facArray.set(10, true);
 				FormBuilderFunctions.formArrayBuilder(facArray, (ArrayList<Object>) dataArray);
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(grid, formNode.facilityStructure, formNode.facilityData);
			}
		});
		return button;
	}
	
	/**
	 * Function removes a data structure from the facility visualized by this form.
	 * @param grid GridPane that supports the form in this view.
	 * @param dataArray ArrayList<Object> containing the information to be
	 * removed.
	 * @param dataArrayNumber Index of the object to be removed.
	 * @return Button that is used to remove the structure, and redraw
	 * the GridPane to update the form.
	 */
	public Button arrayListRemove(final GridPane grid, final ArrayList<Object> dataArray, final int dataArrayNumber, ArrayList<Object> facArray){
		Button button = new Button();
		button.setText("Remove");
		
		button.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e) {
				if(facArray.get(2).toString().toLowerCase() == "incommodity"){
					System.out.println("in commodity");
					for(int i = 0; i < formNode.cycicCircle.incommods.size(); i++){
						System.out.println(formNode.cycicCircle.incommods.get(i));
						if(formNode.cycicCircle.incommods.get(i) == dataArray.get(0)){
							System.out.println(dataArray.get(0));
							formNode.cycicCircle.incommods.remove(i);
							break;
						}
					}
					VisFunctions.redrawPane();
				} else if(facArray.get(2).toString().toLowerCase() == "outcommodity"){
					System.out.println("Outcommodity");
					for(int i = 0; i < formNode.cycicCircle.outcommods.size(); i++){
						System.out.println(formNode.cycicCircle.outcommods.get(i));
						if(formNode.cycicCircle.outcommods.get(i) == dataArray.get(0)){
							formNode.cycicCircle.outcommods.remove(i);
							break;
						}
					}
					VisFunctions.redrawPane();
				}
				dataArray.remove(dataArrayNumber);
				if(dataArray.size() == 0){
					System.out.println("TEST");
					facArray.set(10, false);
				}
				grid.getChildren().clear();
				rowNumber = 0;
				formBuilder(grid, formNode.facilityStructure, formNode.facilityData);
			}
		});		
		
		return button;
	}
	
	/**
	 * This function builds an input form from the data structures associated
	 * with a facility.
	 * @param grid GridPane that the form is built upon.
	 * @param facArray ArrayList<Object> with the data structure
	 * for the facility.
	 * @param dataArray ArrayList<Object> that contains the data for the
	 * facility.
	 */
	@SuppressWarnings("unchecked")
	public void formBuilder(GridPane grid, ArrayList<Object> facArray, ArrayList<Object> dataArray){
		//System.out.println(facArray);
		//System.out.println(dataArray);
		for (int i = 0; i < facArray.size(); i++){
			if (facArray.get(i) instanceof ArrayList && facArray.get(0) instanceof ArrayList) {
				formBuilder(grid, (ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i));
			} else if (i == 0){
				if (facArray.get(2) == "oneOrMore"){
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = new Label((String) facArray.get(0));
						if(facArray.get(9) != null && !facArray.get(9).toString().equalsIgnoreCase("")){
							name.setText((String) facArray.get(9));
						} else {
							name.setText((String) facArray.get(0));	
						}
						name.setTooltip(new Tooltip((String)facArray.get(7)));
						String help = (String) facArray.get(8);
						name.setOnMouseClicked(new EventHandler<MouseEvent>(){
							public void handle(MouseEvent e){
								if(e.getClickCount() == 2){
									Dialog dg = new Dialog();
									ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
									dg.setContentText(help);
									dg.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
									dg.show();
								}
							}
						});
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								System.out.println(facArray);
								grid.add(arrayListRemove(grid, dataArray, ii, facArray), columnNumber+2, rowNumber);
							}
							formBuilder(grid, (ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
						
					}
				} else if (facArray.get(2) == "oneOrMoreMap"){
					//facArray = (ArrayList<Object>) facArray.get(1);
					//dataArray = (ArrayList<Object>) dataArray.get(0);
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = new Label((String) facArray.get(0));
						if(facArray.get(9) != null && !facArray.get(9).toString().equalsIgnoreCase("")){
							name.setText((String) facArray.get(9));
						} else {
							name.setText((String) facArray.get(0));	
						}
						name.setTooltip(new Tooltip((String)facArray.get(7)));
						String help = (String) facArray.get(8);
						name.setOnMouseClicked(new EventHandler<MouseEvent>(){
							public void handle(MouseEvent e){
								if(e.getClickCount() == 2){
									Dialog dg = new Dialog();
									ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
									dg.setContentText(help);
									dg.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
									dg.show();
								}
							}
						});
						grid.add(name, columnNumber, rowNumber);
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber+1, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							if ( ii > 0 ) {
								System.out.println(facArray);
								grid.add(arrayListRemove(grid, dataArray, ii, facArray), columnNumber+2, rowNumber);
							}
							formBuilder(grid, (ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else if (facArray.get(2) == "zeroOrMore") {
					if ((int)facArray.get(6) <= userLevel && i == 0){
						Label name = new Label((String) facArray.get(0));
						if(facArray.get(9) != null && !facArray.get(9).toString().equalsIgnoreCase("")){
							name.setText((String) facArray.get(9));
						} else {
							name.setText((String) facArray.get(0));	
						}
						grid.add(name, columnNumber, rowNumber);
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							System.out.println(facArray);
							grid.add(arrayListRemove(grid, dataArray, ii, facArray), columnNumber+2, rowNumber);
							formBuilder(grid, (ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));	
							rowNumber += 1;
						}
						// resetting the indent
						columnNumber -= 1;
						grid.add(orMoreAddButton(grid, (ArrayList<Object>) facArray, (ArrayList<Object>) dataArray), columnNumber, rowNumber);
						rowNumber += 1;
					}
				} else if (facArray.get(1) instanceof ArrayList) {
					if ((int)facArray.get(6) <= userLevel){
						Label name = new Label((String) facArray.get(0));
						name.setTooltip(new Tooltip ((String)facArray.get(7)));
						grid.add(name, columnNumber, rowNumber);
						rowNumber += 1;
						// Indenting a sub structure
						columnNumber += 1;
						for(int ii = 0; ii < dataArray.size(); ii ++){
							formBuilder(grid, (ArrayList<Object>)facArray.get(1), (ArrayList<Object>) dataArray.get(ii));						
						}
						// resetting the indent
						columnNumber -= 1;
					}
				} else if ((int) facArray.get(6) <= userLevel){
					// Adding the label
					Label name = new Label();
					if(facArray.get(9) != null && !facArray.get(9).toString().equalsIgnoreCase("")){
						name.setText((String) facArray.get(9));
					} else {
						name.setText((String) facArray.get(0));	
					}
					name.setTooltip(new Tooltip((String) facArray.get(7)));
					String help = (String) facArray.get(8);
					name.setOnMouseClicked(new EventHandler<MouseEvent>(){
						public void handle(MouseEvent e){
							if(e.getClickCount() == 2){
								Dialog dg = new Dialog();
								ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
								dg.setContentText(help);
								dg.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
								dg.show();
							}
						}
					});
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
						// Special form building functions that are used for specific tags
						FormBuilderFunctions.cycicTypeTest(grid, formNode, facArray, dataArray, columnNumber, rowNumber);
					}
					columnEnd = 2 + columnNumber;
					grid.add(FormBuilderFunctions.unitsBuilder((String)facArray.get(3)), columnEnd, rowNumber);
					columnEnd = 0;
					rowNumber += 1;
				}
			}
		}
	}
}

