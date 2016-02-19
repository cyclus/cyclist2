package edu.utexas.cycic;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

/**
 * View for building recipies inside of cyclus.
 * @author Robert
 *
 */
public class RecipeForm extends ViewBase{
	/**
	 * View function for placing the forms onto the view.
	 */
	public RecipeForm(){
		super();
		setTitle("Recipe Builder");
		init();
		recipeGrid.setHgap(5);
		recipeGrid.setVgap(8);
		topRecipeGrid.setHgap(5);
		topRecipeGrid.setVgap(8);
		VBox recipeBox = new VBox();
		recipeBox.setPadding(new Insets(5,5,5,5));
		recipeBox.setSpacing(10);
		recipeBox.getChildren().addAll(topRecipeGrid, recipeGrid);
		setContent(recipeBox);
	}
	
	Button addRecipe = new Button("Add Recipe"){
		{
			setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent e){
					if (cur_.Name != null && !cur_.Name.isEmpty()) {
						for(Nrecipe recipe: CycicScenarios.workingCycicScenario.Recipes){
							if(recipe.Name.equals(cur_.Name)){
								return; // already exists in list, just return
							}
						}
						CycicScenarios.workingCycicScenario.Recipes.add(cur_);
					}
				}
			});
		}
	};
	
	private GridPane topRecipeGrid = new GridPane();
	private GridPane recipeGrid = new GridPane();
	private int rowNumber;
    private Nrecipe cur_;
	
	/**
	 * Places the top grid onto the function. For selecting an existing recipe
	 * or adding a new recipe.
	 */
	public void init(){
		ComboBox<String> recipiesList = new ComboBox<String>();
		Label isoSelect = new Label("Select Recipe");
		topRecipeGrid.add(isoSelect, 0, 0);
		recipiesList.setOnMousePressed(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent e){
				recipiesList.getItems().clear();
				for(int i = 0; i < CycicScenarios.workingCycicScenario.Recipes.size(); i++){
					recipiesList.getItems().add(CycicScenarios.workingCycicScenario.Recipes.get(i).Name);
				}
			}
		});
		recipiesList.setPromptText("Select a Recipe");
		recipiesList.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				if(recipiesList.getValue() != null){
					if(!recipiesList.getValue().equals("")){
						for(Nrecipe recipe: CycicScenarios.workingCycicScenario.Recipes) {
							if (recipe.Name.equals(recipiesList.getValue())) {
								loadRecipe(recipe);
							}
						}
					}
				}
			}	
		});
		topRecipeGrid.add(recipiesList, 1, 0);
		
		Button newRecipe = new Button("New Recipe");
		newRecipe.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				rowNumber = 3;
				Nrecipe recipe = new Nrecipe();
				recipe.Composition.add(new isotopeData());
				loadRecipe(recipe);
				cur_ = recipe;
			}
		});
		topRecipeGrid.add(newRecipe, 2, 0);
	}
  
	/**
	 * Builds the form structures required for each recipe. 
	 */
	public void recipeGenInfo(final Nrecipe recipe){
		rowNumber = 3;
		recipeGrid.getChildren().clear();
		Label name = new Label("Recipe Name");
		TextField recipeName = new TextField();
		recipeName.setText(recipe.Name);
		recipeName.setMinHeight(10);
		recipeName.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				recipe.Name = newValue;
			}
		});
		recipeGrid.add(name, 0, 0);
		recipeGrid.add(recipeName, 1, 0);
		Button removeRecipeButt = new Button();
		removeRecipeButt.setText("Remove Recipe");
		removeRecipeButt.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				for(Nrecipe recipe: CycicScenarios.workingCycicScenario.Recipes){
					if(recipe.Name.equals(recipeName.getText())){
						CycicScenarios.workingCycicScenario.Recipes.remove(recipe);
						break;
					}
				}
				recipeGrid.getChildren().clear();
			}
		});
		recipeGrid.add(removeRecipeButt, 2, 0);
		ComboBox<String> basisBox = new ComboBox<String>();
		String saveBasis = recipe.Basis;  // cache basis so it doesn't get rest by the clear()
		Label basis = new Label("Basis");
		recipeGrid.add(basis, 0, 1);
		basisBox.getItems().clear();
		basisBox.getItems().add("atom");
		basisBox.getItems().add("mass");
		basisBox.setPromptText("Select Basis");
		basisBox.setValue(saveBasis);
		basisBox.valueProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				if(newValue != null){
					recipe.Basis = basisBox.getValue();	
				}
			}
		});
		recipeGrid.add(basisBox, 1, 1);
		
		Label isotope = new Label("Isotopes");
		recipeGrid.add(isotope, 0, 2);
		
		Button addIso = new Button();
		addIso.setText("Add Isotope");
		addIso.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				isotopeData isoData = new isotopeData();
				recipe.Composition.add(isoData);
				loadRecipe(recipe);
			}
		});
		recipeGrid.add(addIso, 1, 2);	
	}
	
	/**
	 * Removes an isotope for a recipe.
	 * @param recipe Recipe the isotope will be removed from.
	 * @param isoData isotopeData to be removed.
	 * @return Button that will remove the isotope on click.
	 */
	public Button removeIsotope(final Nrecipe recipe, final isotopeData isoData){
		Button removeIso = new Button();
		removeIso.setText("Remove");
		removeIso.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				recipe.Composition.remove(isoData);
				loadRecipe(recipe);
			}
		});
		
		return removeIso;
	}
	
	/**
	 * Loads a recipe stored to the simulation
	 * @param recipe Recipe to be loaded.
	 */
	public void loadRecipe(final Nrecipe recipe){
		recipeGenInfo(recipe);
		
		for(final isotopeData iso : recipe.Composition){
			Label isotopeStored = new Label("Isotope");
			recipeGrid.add(isotopeStored, 0, rowNumber);
			
			TextField isotopeNumber = new TextField();
			
			isotopeNumber.setText(iso.Name);
			isotopeNumber.setMinSize(40, 20);
			isotopeNumber.setMaxSize(100, 20);
			
			isotopeNumber.textProperty().addListener(new ChangeListener<String>(){
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
					iso.Name = newValue;
				}
			});
			recipeGrid.add(isotopeNumber, 1, rowNumber);
			
			Label isotopeValue = new Label("Amount");
			recipeGrid.add(isotopeValue, 2, rowNumber);
			
			TextField isoWeightFrac = new TextField(){
				@Override public void replaceText(int start, int end, String text) {
					if (!text.matches("[a-z]")){
						super.replaceText(start, end, text);
					}
				}
			};
			isoWeightFrac.textProperty().addListener(new ChangeListener<String>(){
				@Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
					iso.value = Double.parseDouble(newValue);
				}
			});
			isoWeightFrac.setText(String.valueOf(iso.value));	
			isoWeightFrac.setMaxSize(80, 20);
			recipeGrid.add(isoWeightFrac, 3, rowNumber);
			recipeGrid.add(removeIsotope(recipe, iso), 4, rowNumber);
			
			rowNumber += 1;
		}
		if(recipe.Composition.size() >0){
			recipeGrid.add(addRecipe, 4, rowNumber+1);
		}
	}
}

