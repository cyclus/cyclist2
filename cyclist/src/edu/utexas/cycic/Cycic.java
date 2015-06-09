package edu.utexas.cycic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.event.dnd.DnD.LocalClipboard;
import edu.utah.sci.cyclist.core.event.notification.CyclistNotification;
import edu.utah.sci.cyclist.core.model.CyclusJob;
import edu.utah.sci.cyclist.core.model.CyclusJob.Status;
import edu.utah.sci.cyclist.core.model.Preferences;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;
import edu.utexas.cycic.presenter.CycicPresenter;

public class Cycic extends ViewBase{
	static Logger log = Logger.getLogger(Cycic.class);
	
	/**
	 * Function for building the CYCIC Pane and GridPane of this view. 
	 */
	public Cycic(){
		super();
		if (monthList.size() == 0){
			months();
		}
		if (CycicScenarios.cycicScenarios.size() < 1){
			DataArrays scenario = new DataArrays();
			workingScenario = scenario;
			CycicScenarios.cycicScenarios.add(scenario);
		}
		init();
	}
	/**
	 * 
	 */
	public static final String TITLE = "Cycic";
	static Pane pane = new Pane();
	Pane nodesPane = new Pane();
	static facilityNode workingNode = null;
	static DataArrays workingScenario;
	static boolean marketHideBool = true;
	static Window window;
	static ToggleGroup opSwitch = new ToggleGroup();
    static CyclusJob _remoteDashA;
    private static Object monitor = new Object();
    static String currentSkin = "Default Skin";
    static String currentServer = "";
    static TextField duration = VisFunctions.numberField();
	static ComboBox<String> startMonth = new ComboBox<String>();
	static TextField startYear = VisFunctions.numberField();
	static TextArea description = new TextArea();
	static ComboBox<String> decay = new ComboBox<String>();
	static TextField simHandle = new TextField();
	
    
	/**
	 * 
	 */
	static GridPane simInfo = new GridPane(){
		{
			setHgap(4);
			setVgap(4);
			setPadding(new Insets(10, 10, 10, 10));			
		}
	};
	
	/**
	 * 
	 */
	static VBox simDetailBox = new VBox(){
		{
			getChildren().add(simInfo);
			setStyle("-fx-border-style: solid;"
	                + "-fx-border-width: 1;"
	                + "-fx-border-color: black");
		}
	};
	
	/**
	 * 
	 */
	static GridPane commodGrid = new GridPane(){
		{
			setVgap(5);
			setHgap(5);
		}
	};
	
	/**
	 * 
	 */
	static Button addNewCommod = new Button(){
		{
			setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent e){
					addNewCommodity();
				}
			});
			setText("+");
			setStyle("-fx-font-size: 12;");
		}
	};
	static VBox commodBox = new VBox(){
		{
			getChildren().add(new HBox(){
				{
					getChildren().add(new Label(){
						{
							setText("Simulation Commodities");
							setTooltip(new Tooltip("Commodities facilitate the transfer of materials from one facility to another."
									+ "Facilities with the same commodities are allowed to trade with each other."));
							setFont(new Font("Times", 16));
						}
					});
					getChildren().add(addNewCommod);
					setSpacing(5);
				}
			});
			getChildren().add(commodGrid);
			setPadding(new Insets(10, 10, 10, 10));
			setSpacing(5);
			setStyle("-fx-border-style: solid;"
	                + "-fx-border-width: 1;"
	                + "-fx-border-color: black");
			
		}
	};
	GridPane archetypeGrid = new GridPane(){
		{
			add(new Label(){
				{
					setText("Add Available Archetypes");
					setTooltip(new Tooltip("Use the drop down menu to select archetypes to add to the simulation."));
					setFont(new Font("Times", 16));
				}
			}, 0, 0);
			setStyle("-fx-border-style: solid;"
	                + "-fx-border-width: 1;"
	                + "-fx-border-color: black");
			setVgap(5);
			setHgap(10);
			setPadding(new Insets(10, 10, 10, 10));
		}
	};
	

    private void updateLinkColor(){
        ColorPicker cP = new ColorPicker();
        cP.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    for(nodeLink node: DataArrays.Links){
                        node.line.updateColor(cP.getValue());
                    }
                }
            });
        Dialog dg = new Dialog();
        ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
        dg.getDialogPane().setContent(cP);
        dg.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        dg.setResizable(true);
        dg.show();
        
    }
    private void updateBgColor(){
        ColorPicker cP = new ColorPicker();
        cP.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    String background = "-fx-background-color: #";
                    background += cP.getValue().toString().substring(2, 8);
                    pane.setStyle(background);
                }
            });
        Dialog dg = new Dialog();
        ButtonType loginButtonType = new ButtonType("Ok", ButtonData.OK_DONE);
        dg.getDialogPane().setContent(cP);
        dg.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        dg.setResizable(true);
        dg.show();
        
    }

    private void setSkin() {
        
        if(currentSkin.equalsIgnoreCase("Default Skin")){
            for(int j = 0; j < DataArrays.FacilityNodes.size(); j++){
                DataArrays.FacilityNodes.get(j).cycicCircle.image.setVisible(false);
                DataArrays.FacilityNodes.get(j).cycicCircle.setOpacity(100);
            }
        } else {
            for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
                skinSet skin = DataArrays.visualizationSkins.get(i);
                if(skin.name.equalsIgnoreCase(currentSkin)){
                    for(int j = 0; j < DataArrays.FacilityNodes.size(); j++){
                        DataArrays.FacilityNodes.get(j).cycicCircle.image.setImage(skin.images.getOrDefault(DataArrays.FacilityNodes.get(j).niche, skin.images.get("facility")));
                        DataArrays.FacilityNodes.get(j).cycicCircle.image.setVisible(true);
                        DataArrays.FacilityNodes.get(j).cycicCircle.setOpacity(0);
                    }
                }
            }
        }
        
    }

	/**
	 * Initiates the Pane and GridPane.
	 */
	private void init(){

        DataArrays.cycicInitLoader();

        final ContextMenu paneMenu = new ContextMenu();
        MenuItem linkColor = new MenuItem("Change Link Color...");
        linkColor.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                updateLinkColor();
            }
            });
        MenuItem bgColor = new MenuItem("Change Background Color...");
        bgColor.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                updateBgColor();
            }
            });

        Menu skinMenu = new Menu("Change skin");
        MenuItem defSkin = new MenuItem("Default Skin");
        defSkin.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    currentSkin = "Default Skin";
                    setSkin();
                }
            });
        skinMenu.getItems().add(defSkin);
		for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
            final String skinName = DataArrays.visualizationSkins.get(i).name;
            if (skinName.equals("DSARR")) {
                currentSkin = skinName;
            }
            MenuItem item = new MenuItem(skinName);
            item.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent e){
                        currentSkin = skinName;
                        setSkin();
                    }
                });
			skinMenu.getItems().add(item);
		}
        
        
        MenuItem imageExport = new MenuItem("Save fuel cycle diagram");
		imageExport.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent e){
                    export();
                }
            });

        paneMenu.getItems().addAll(linkColor,bgColor,skinMenu,new SeparatorMenuItem(), imageExport);
 
		pane.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				if(e.getButton().equals(MouseButton.SECONDARY)){
                    paneMenu.show(pane,e.getScreenX(),e.getScreenY());
				}
			}
		});
		pane.setOnDragOver(new EventHandler <DragEvent>(){
			public void handle(DragEvent event){
				event.acceptTransferModes(TransferMode.ANY);
			}
		});
		pane.setOnDragDropped(new EventHandler<DragEvent>(){
			public void handle(DragEvent event){
				if(event.getDragboard().hasContent(DnD.VALUE_FORMAT)){
					facilityNode facility = new facilityNode();
					facility.facilityType = event.getDragboard().getContent(DnD.VALUE_FORMAT).toString();
					facility.facilityType.trim();
					for (int i = 0; i < DataArrays.simFacilities.size(); i++){
						if(DataArrays.simFacilities.get(i).facilityName.equalsIgnoreCase(facility.facilityType)){
							facility.facilityStructure = DataArrays.simFacilities.get(i).facStruct;
							facility.niche = DataArrays.simFacilities.get(i).niche;
							facility.doc = DataArrays.simFacilities.get(i).doc;
							facility.archetype = DataArrays.simFacilities.get(i).facilityArch;
						}
					}
					event.consume();
					facility.name = "";
					facility.facLifetime = "";
					facility.cycicCircle = CycicCircles.addNode((String)facility.name, facility);
					facility.cycicCircle.setCenterX(event.getX());
					facility.cycicCircle.setCenterY(event.getY());
					VisFunctions.placeTextOnCircle(facility.cycicCircle, "middle");

					
					for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
						if(DataArrays.visualizationSkins.get(i).name.equalsIgnoreCase(currentSkin)){
							facility.cycicCircle.image.setImage(DataArrays.visualizationSkins.get(i).images.getOrDefault(facility.niche,DataArrays.visualizationSkins.get(i).images.get("facility")));
							facility.cycicCircle.image.setVisible(true);
							facility.cycicCircle.setOpacity(0);
							facility.cycicCircle.setRadius(DataArrays.visualizationSkins.get(i).radius);
							VisFunctions.placeTextOnCircle(facility.cycicCircle, DataArrays.visualizationSkins.get(i).textPlacement);
						}
					}
					facility.cycicCircle.image.setLayoutX(facility.cycicCircle.getCenterX()-60);
					facility.cycicCircle.image.setLayoutY(facility.cycicCircle.getCenterY()-60);
					
					facility.sorterCircle = SorterCircles.addNode((String)facility.name, facility, facility);
					FormBuilderFunctions.formArrayBuilder(facility.facilityStructure, facility.facilityData);			
				} else {
					event.consume();
				}
			}
		});
		setTitle(TITLE);
		setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				CycicScenarios.workingCycicScenario = workingScenario;
			}
		});
		
		VBox cycicBox = new VBox();
		cycicBox.autosize();

		pane.autosize();
		pane.setId("cycicPane");
		pane.setPrefSize(1000, 600);
		pane.setStyle("-fx-background-color: white;");
		
		// Temp Toolbar //
		final GridPane grid = new GridPane();
		grid.setStyle("-fx-background-color: #d6d6d6;"
					+ "-fx-font-size: 14;");
		grid.setHgap(10);
		grid.setVgap(5);
		createArchetypeBar(grid);
		// Adding a new Facility //
		Label scenetitle1 = new Label("Add Prototype");
		grid.add(scenetitle1, 0, 0);
		Label facName = new Label("Name");
		grid.add(facName, 1, 0);
		// Name Field
		final TextField facNameField = new TextField();

		grid.add(facNameField, 2, 0);
		// Facility Type
		final ComboBox<String> structureCB = new ComboBox<String>();
		structureCB.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent m){
				structureCB.getItems().clear();	
				for(int i = 0; i < DataArrays.simFacilities.size(); i++){
					structureCB.getItems().add((String) DataArrays.simFacilities.get(i).facilityName);	
				}
			}
		});

		structureCB.setPromptText("Select Facility Archetype");
		grid.add(structureCB, 3, 0);
		//Submit Button
		Button submit1 = new Button("Add");
		submit1.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				if (structureCB.getValue() == null){
					return;
				}
				facilityNode facility = new facilityNode();
				facility.facilityType = structureCB.getValue();
				facility.facilityType.trim();
				addArcheToBar(facility.facilityType);
				for (int i = 0; i < DataArrays.simFacilities.size(); i++){
					if(DataArrays.simFacilities.get(i).facilityName.equalsIgnoreCase(facility.facilityType)){
						facility.facilityStructure = DataArrays.simFacilities.get(i).facStruct;
						facility.niche = DataArrays.simFacilities.get(i).niche;
						facility.doc = DataArrays.simFacilities.get(i).doc;
						facility.archetype = DataArrays.simFacilities.get(i).facilityArch;
					}
				}
				event.consume();
				facility.name = facNameField.getText();
				facility.facLifetime = "";
				facility.cycicCircle = CycicCircles.addNode((String)facility.name, facility);
				facility.cycicCircle.setCenterX(80);
				facility.cycicCircle.setCenterY(80);
				VisFunctions.placeTextOnCircle(facility.cycicCircle, "middle");

				
				for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
					if(DataArrays.visualizationSkins.get(i).name.equalsIgnoreCase(currentSkin)){
						facility.cycicCircle.image.setImage(DataArrays.visualizationSkins.get(i).images.getOrDefault(facility.niche,DataArrays.visualizationSkins.get(i).images.get("facility")));
						facility.cycicCircle.image.setVisible(true);
						facility.cycicCircle.setOpacity(0);
						facility.cycicCircle.setRadius(DataArrays.visualizationSkins.get(i).radius);
						VisFunctions.placeTextOnCircle(facility.cycicCircle, DataArrays.visualizationSkins.get(i).textPlacement);
					}
				}
				facility.cycicCircle.image.setLayoutX(facility.cycicCircle.getCenterX()-60);
				facility.cycicCircle.image.setLayoutY(facility.cycicCircle.getCenterY()-60);
				
				facility.sorterCircle = SorterCircles.addNode((String)facility.name, facility, facility);
				FormBuilderFunctions.formArrayBuilder(facility.facilityStructure, facility.facilityData);		
			}
		});
		grid.add(submit1, 4, 0);
		
		ScrollPane scroll = new ScrollPane();
		scroll.setMinHeight(120);
		scroll.setContent(nodesPane);
		
//		opSwitch.getToggles().clear();
//        opSwitch.getToggles().addAll(localToggle, remoteToggle);
//        try {
//            Process readproc = Runtime.getRuntime().exec("cyclus -V");
//            new BufferedReader(new InputStreamReader(readproc.getInputStream()));
//            localToggle.setSelected(true);
//        } catch (RuntimeException | IOException e) {
//            localToggle.setSelected(false);
//            remoteToggle.setSelected(true);
//        };

		cycicBox.getChildren().addAll(grid, scroll, pane);
		
		HBox mainView = new HBox(){
			{
				setSpacing(5);
			}
		};
		details();
		VBox sideView = new VBox(){
			{
				setSpacing(5);
				setStyle("-fx-border-style: solid;"
		                + "-fx-border-width: 1;"
		                + "-fx-border-color: black");
			}
		};

		sideView.getChildren().addAll(simDetailBox, archetypeGrid, commodBox);
		mainView.getChildren().addAll(sideView, cycicBox);
		
		setContent(mainView);
	}
	
	
	/**
	 * 
	 * @param circle
	 * @param i
	 * @param name
	 */
	public void buildDnDCircle(FacilityCircle circle, int i, String name){
		circle.setRadius(40);
		circle.setStroke(Color.BLACK);
		circle.rgbColor=VisFunctions.stringToColor(name);
		circle.setFill(VisFunctions.pastelize(Color.rgb(circle.rgbColor.get(0),circle.rgbColor.get(1),circle.rgbColor.get(2))));
		circle.setCenterX(45+(i*90));
		circle.setCenterY(50);
		circle.text.setText(name.split(" ")[2] + " (" + name.split(" ")[1] + ")");
		VisFunctions.placeTextOnCircle(circle, "middle");
		circle.setOnDragDetected(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				Dragboard db = circle.startDragAndDrop(TransferMode.COPY);
				ClipboardContent content = new ClipboardContent();				
				content.put(DnD.VALUE_FORMAT, name);
			
				SnapshotParameters snapParams = new SnapshotParameters();
				snapParams.setFill(Color.TRANSPARENT);

				content.putImage(circle.snapshot(snapParams, null));	    
				db.setContent(content);
				e.consume();
			}
		});
	}
	
	/**
	 * 
	 */
	public static void buildCommodPane(){
		commodGrid.getChildren().clear();
		for (int i = 0; i < Cycic.workingScenario.CommoditiesList.size(); i++){
			CommodityNode commod = Cycic.workingScenario.CommoditiesList.get(i);
			TextField commodity = new TextField();
			commodity.setText(commod.name.getText());
			commodGrid.add(commodity, 0, i );
			final int index = i;
			commodity.setPromptText("Enter Commodity Name");
			commodity.textProperty().addListener(new ChangeListener<String>(){
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
					commod.name.setText(newValue);
				}
			});
			commodGrid.add(new Label("Priority"), 1, index);
			TextField priority = VisFunctions.numberField();
			priority.textProperty().addListener(new ChangeListener<String>(){
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
					commod.priority = Double.parseDouble(newValue);
				}
			});
			priority.setPromptText("Enter Commodity Prioirty");
			priority.setText("1");
			commodGrid.add(priority, 2, index);
			Button removeCommod = new Button();
			removeCommod.setGraphic(GlyphRegistry.get(AwesomeIcon.TRASH_ALT, "10px"));
			removeCommod.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent e){
					String commod = Cycic.workingScenario.CommoditiesList.get(index).name.getText();
					Cycic.workingScenario.CommoditiesList.remove(index);
					for(facilityNode facility: DataArrays.FacilityNodes){
						for(int i =0; i < facility.cycicCircle.incommods.size(); i++){
							if(facility.cycicCircle.incommods.get(i) == commod){
								facility.cycicCircle.incommods.remove(i);
							}
						}
						for(int i =0; i < facility.cycicCircle.outcommods.size(); i++){
							if(facility.cycicCircle.outcommods.get(i) == commod){
								facility.cycicCircle.outcommods.remove(i);
							}
						}
						commodListRm(facility.facilityStructure, facility.facilityData, commod);
					}
					VisFunctions.redrawPane();
					buildCommodPane();
				}
			});	
			commodGrid.add(removeCommod, 3, index);
		}
	}
	
	/**
	 * Adds a new TextField to the commodity GridPane tied to a new commodity in the 
	 * simulation.
	 */
	static public void addNewCommodity(){
		CommodityNode commodity = new CommodityNode();
		commodity.name.setText("");
		Cycic.workingScenario.CommoditiesList.add(commodity);
		TextField newCommod = new TextField();
		newCommod.autosize();
		newCommod.setPromptText("Enter Commodity Name");
		newCommod.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				commodity.name.setText(newValue);
			}
		});
		buildCommodPane();
	}
	
	HashMap<String, String> months = new HashMap<String, String>();
	static ArrayList<String> monthList = new ArrayList<String>();
	/**
	 * Quick hack to convert months into their integer values.
	 * i.e. January = 0, Feb = 1, etc...
	 */
	public void months(){
		monthList.add("January");
		monthList.add("February");
		monthList.add("March");
		monthList.add("April");
		monthList.add("May");
		monthList.add("June");
		monthList.add("July");
		monthList.add("August");
		monthList.add("September");
		monthList.add("October");
		monthList.add("November");
		monthList.add("December");
		
		months.put("January", "1");
		months.put("Febuary", "2");
		months.put("March", "3");
		months.put("April", "4");
		months.put("May", "5");
		months.put("June", "6");
		months.put("July", "7");
		months.put("August", "8");
		months.put("September", "9");
		months.put("October", "10");
		months.put("November", "11");
		months.put("December", "12");
	}
	
	/**
	 * 
	 */
	public void details(){
		Label simDets = new Label("Simulation Details");
		simDets.setTooltip(new Tooltip("The top level details of the simulation."));
		simDets.setFont(new Font("Times", 16));
		simInfo.add(simDets, 0, 0, 2, 1);
		duration.setMaxWidth(150);
		duration.setPromptText("Length of Simulation");
		duration.setText(Cycic.workingScenario.simulationData.duration);
		Cycic.workingScenario.simulationData.duration = "0";
		duration.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.duration = newValue;
			}
		});
		simInfo.add(new Label("Duration (Months)"), 0, 1, 2, 1);
		simInfo.add(duration, 2, 1);
		
		for(int i = 0; i < 12; i++ ){
			startMonth.getItems().add(monthList.get(i));
		}
		Cycic.workingScenario.simulationData.startMonth = "1";
		startMonth.setValue(monthList.get(Integer.parseInt(Cycic.workingScenario.simulationData.startMonth)-1));
		startMonth.valueProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.startMonth = months.get(newValue);
			}
		});
		startMonth.setPromptText("Select Month");
		simInfo.add(new Label("Start Month"), 0, 2, 2, 1);
		simInfo.add(startMonth, 2, 2);
		
		startYear.setText(Cycic.workingScenario.simulationData.startYear);
		Cycic.workingScenario.simulationData.startYear = "0";
		startYear.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.startYear = newValue;
			}
		});
		startYear.setPromptText("Starting Year");
		startYear.setMaxWidth(150);
		simInfo.add(new Label("Start Year"), 0, 3, 2, 1);
		simInfo.add(startYear, 2, 3);
				
		decay.getItems().addAll("manual", "never");
		decay.setValue("Never");
		Cycic.workingScenario.simulationData.decay = "never";
		decay.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				Cycic.workingScenario.simulationData.decay = decay.getValue();
			}
		});
		simInfo.add(new Label("Decay"), 0, 4);
		simInfo.add(decay, 2, 4);
		
		simHandle.setPromptText("Optional Simulation Name");
		simHandle.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.simHandle = newValue;
			}
		});
		simInfo.add(new Label("Simulation Handle"), 0, 5, 2, 1);
		simInfo.add(simHandle, 2, 5);
		
		description.setMaxSize(250, 50);
		description.setWrapText(true);
		description.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.notes = newValue;
			}
		});
		simInfo.add(new Label("Description"), 0, 6, 2, 1);
		simInfo.add(description, 2, 6);
		
	
		// Prints the Cyclus input associated with this simulator. 
		Button output = new Button("Generate");
		output.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
                          OutPut.CheckInjection();
                          if(OutPut.inputTest()){
                            FileChooser fileChooser = new FileChooser();
                            //Set extension filter
                            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
                            fileChooser.getExtensionFilters().add(extFilter);
                            fileChooser.setTitle("Save Cyclus input file");
                            fileChooser.setInitialFileName("*.xml");
                            //Show save file dialog
                            File file = fileChooser.showSaveDialog(window);
                            OutPut.output(file);
                          }
			}
		});
		simInfo.add(output, 0, 7, 2, 1);
		
		Button load = new Button("Load");
		load.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				FileChooser fileChooser = new FileChooser();
				//Set extension filter
				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
				fileChooser.getExtensionFilters().add(extFilter);
				fileChooser.setTitle("Select input file to load.");
				fileChooser.setInitialFileName("*.xml");
				//Show save file dialog
				File file = fileChooser.showOpenDialog(window);
				OutPut.loadFile(file);   
			}
		});
		simInfo.add(load, 2, 7);
		
        Button runCyclus = new Button("Execute");
        simInfo.add(runCyclus, 0, 8, 1, 1);    
        
        
        final List<String> serversList = FXCollections.observableArrayList(Preferences.getInstance().servers());
        serversList.add(0, "-- add new --");
        ComboBox<String> serverBox = new ComboBox<>();
        serverBox.getItems().addAll(serversList);
        serverBox.setVisibleRowCount(Math.min(6,  serverBox.getItems().size()));
        
        int currentIndex = Preferences.getInstance().getCurrentServerIndex()+1;
        currentServer = serversList.get(currentIndex);
        
        serverBox.setPromptText("server");
        serverBox.setEditable(true);
        serverBox.getSelectionModel().select(currentIndex);
        
        serverBox.valueProperty().addListener(
        		(ov, from, to)-> {
        			int idx = serverBox.getSelectionModel().getSelectedIndex();
					if (idx-1 != Preferences.getInstance().getCurrentServerIndex()) {
						Preferences.getInstance().setCurrentServerIndex(idx-1);
					} else if (Preferences.LOCAL_SERVER.equals(from)) {
						serverBox.getSelectionModel().select(idx);
    				} else if ("".equals(to)) {
    					serverBox.getItems().remove(from);
    					Preferences.getInstance().servers().remove(from);

    				} else if (serverBox.getItems().indexOf(to) == -1) {
						serverBox.getItems().add(to);
						Preferences.getInstance().servers().add(to);
						Preferences.getInstance().setCurrentServerIndex(serverBox.getItems().size()-2);
						serverBox.setVisibleRowCount(Math.min(6,  serverBox.getItems().size()));
    				}
					currentServer = serverBox.getValue();
    			});

        Label serverLabel = new Label("Server:");
        GridPane.setHalignment(serverLabel, HPos.RIGHT);
    	simInfo.add(serverLabel, 1, 8, 1, 1);
        simInfo.add(serverBox, 2, 8);
        
        runCyclus.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
              OutPut.CheckInjection();
              if(!OutPut.inputTest()){
                log.error("Cyclus Input Not Well Formed!");
                return;  // safety dance
              }
              String server = serverBox.getValue();
              if (Preferences.LOCAL_SERVER.equals(server)) {
                // local execution
                String tempHash = Integer.toString(OutPut.xmlStringGen().hashCode());
                String prefix = "cycic" + tempHash;
                String infile = prefix + ".xml";
                String outfile = prefix + ".sqlite";
                try {
                  File temp = new File(infile);
                  log.trace("Writing file " + temp.getName());
                  log.trace("lines:\n" + OutPut.xmlStringGen());
                  OutPut.output(temp);
                  // BufferedWriter output = new BufferedWriter(new FileWriter(temp));
                  // output.write(OutPut.xmlStringGen());
                  // output.close();
                  Process p = Runtime.getRuntime().exec("cyclus -o " + outfile + " " + infile);
                  p.waitFor();
                  String line = null;
                  BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                  while ((line = input.readLine()) != null) {        
                    log.info(line);
                  }
                  input.close();
                  input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                  while ((line = input.readLine()) != null) {        
                    log.warn(line);
                  }
                  input.close();
                  log.info("Cyclus run complete");
                } catch (Exception e1) {
                  //                        e1.printStackTrace();
                  log.error(e1.getMessage());
                }
              } else {
                // remote execution
                String cycicXml = OutPut.xmlStringGen();
                CyclistController._cyclusService.submit(cycicXml, server);
              }
            }
          });;
   
    }


    public void addArcheToBar(String archeType){
        
		for(int i = 0; i < DataArrays.simFacilities.size(); i++){
			if(DataArrays.simFacilities.get(i).facilityName.equalsIgnoreCase(archeType)){
				if(DataArrays.simFacilities.get(i).loaded == true){
					return;
				}
				facilityStructure test = DataArrays.simFacilities.get(i);
				try {
					test.loaded = true;
					FacilityCircle circle = new FacilityCircle();
					int pos = 0;
					for(int k = 0; k < DataArrays.simFacilities.size(); k++){
						if(DataArrays.simFacilities.get(k).loaded == true){
							pos+=1;
						}
					}
					buildDnDCircle(circle, pos-1, test.facilityName);
					nodesPane.getChildren().addAll(circle, circle.text);
				} catch (Exception eq) {
					
				}
			}
		}

    }

	public void createArchetypeBar(GridPane grid){
		ComboBox<String> archetypes = new ComboBox<String>();
		for(int i = 0; i < DataArrays.simFacilities.size(); i++){
			archetypes.getItems().add(DataArrays.simFacilities.get(i).facilityName);
		}
		archetypes.setOnMousePressed(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				archetypes.getItems().clear();
				for(int i = 0; i < DataArrays.simFacilities.size(); i++){
					archetypes.getItems().add(DataArrays.simFacilities.get(i).facilityName);
				}
			}
		});
		archetypes.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				addArcheToBar(archetypes.getValue());
			}
		});

        Button cyclusDashM = new Button("Discover Archetypes");
        cyclusDashM.setTooltip(new Tooltip("Use this button to search for all local Cyclus modules."));
        cyclusDashM.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                if (Preferences.LOCAL_SERVER.equals(currentServer)) {
                    // Local metadata collection
                    try {
                        Process readproc = Runtime.getRuntime().exec("cyclus -m");
                        BufferedReader metaBuf = new BufferedReader(new InputStreamReader(readproc.getInputStream()));
                        String line=null;
                        String metadata = new String();
                        while ((line = metaBuf.readLine()) != null) {metadata += line;}
                        metaBuf.close();
                        DataArrays.retrieveSchema(metadata);
                    } catch (IOException ex) {
                        log.error(ex.getMessage());
//                        ex.printStackTrace();
                    }
                } else {
                    // Remote metadata collection
                	_remoteDashA = CyclistController._cyclusService.submitCmdToRemote(currentServer, "cyclus", "-m");
//                    _remoteDashA = CyclistController._cyclusService.latestJob();
                	CyclusJob job = _remoteDashA;
                	if (_remoteDashA.getStatus() == Status.FAILED) {
                		// TODO: an error msg was already sent to the console. Do we want to pop up an error msg?
                	} else {
	                    _remoteDashA.statusProperty()
	                        .addListener(new ChangeListener<CyclusJob.Status>() {
		                        @Override
		                        public void changed(ObservableValue<? extends Status> observable,
		                            Status oldValue, Status newValue) {
		                            if (newValue == Status.READY) {
		                                DataArrays.retrieveSchema(_remoteDashA.getStdout());
		                            }
		                        }
	                    });
                	}
                }
            }
        });
        archetypeGrid.add(cyclusDashM, 1, 0);

        archetypeGrid.add(new Label("Add Archetype to Simulation"), 0, 1);
		archetypeGrid.add(archetypes, 1, 1);
	}
	
	private void export() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add( new FileChooser.ExtensionFilter("Image file (png, jpg, gif)", "*.png", "*.jpg", "'*.gif") );
		File file = chooser.showSaveDialog(Cyclist.cyclistStage);
		if (file != null) {
			WritableImage image = pane.snapshot(new SnapshotParameters(), null);
			String name = file.getName();
			String ext = name.substring(name.indexOf(".")+1, name.length());
		    try {
		        ImageIO.write(SwingFXUtils.fromFXImage(image, null), ext, file);
		    } catch (IOException e) {
		        log.error("Error writing image to file: "+e.getMessage());
		    }
		} else {
			log.error("File did not generate correctly.");
			System.out.println("File did not generate correctly.");
		}
	}
	
	public static void commodListRm(ArrayList<Object> facArray, ArrayList<Object> dataArray, String commod){
		if(facArray.get(0) instanceof ArrayList){
			for(int i = 0; i < facArray.size(); i++){
				commodListRm((ArrayList<Object>) facArray.get(i), (ArrayList<Object>) dataArray.get(i), commod);
			}
		} else if(facArray.get(1) instanceof ArrayList){
			for(int i = 0; i < dataArray.size(); i++){
				commodListRm((ArrayList<Object>) facArray.get(1), (ArrayList<Object>) dataArray.get(i), commod);
			}
		} else {
			switch ((String) facArray.get(2).toString().toLowerCase()){
			case "incommodity":
				if(dataArray.get(0).toString().equalsIgnoreCase(commod)){
					dataArray.set(0, "");
				}
				break;
			case "outcommodity":
				if(dataArray.get(0).toString().equalsIgnoreCase(commod)){
					dataArray.set(0, "");
				}
				break;
			}
		}
	}
	
}
