package edu.utexas.cycic;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.Resources1;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;
import edu.utah.sci.cyclist.core.util.AwesomeIcon;
import edu.utah.sci.cyclist.core.util.GlyphRegistry;
import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.model.CyclusJob;
import edu.utah.sci.cyclist.core.model.CyclusJob.Status;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.swing.AbstractAction;

import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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
	ComboBox<String> skins = new ComboBox<String>();
	static ToggleGroup opSwitch = new ToggleGroup();
	static ToggleButton localToggle = new ToggleButton("Local");
	static ToggleButton remoteToggle = new ToggleButton("Remote");
    static CyclusJob _remoteDashA;
    private static Object monitor = new Object();
	
    
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
	

	/**
	 * Initiates the Pane and GridPane.
	 */
	private void init(){
		Resources1 resource = new Resources1();
		File file = new File(resource.getCurrentPath());
		String path = "/" + file.getParent();
		try {
			defaultJsonReader(path);
			log.info("Meta data loaded for default archetypes. If you wish to add others, please use the DISCOVER ARCHETYPES button. Thanks!");
		} catch (IOException e1) {
			log.warn("Could not read default meta data. Please use DISCOVER ARCHETYPES button. Thanks!");
		}
		pane.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				if(e.getButton().equals(MouseButton.SECONDARY)){
					/** TODO Turn this into a menu. Should menu move with cursor or rebuild menu each time? 
					 * Also this should save the previous color and cancel should return you to that color.*/

					if (e.isShiftDown() == true){
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
					} else {
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
						dg.show();
					}
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
							facility.archetype = DataArrays.simFacilities.get(i).facilityArch;
						}
					}
					event.consume();
					facility.name = "";
					facility.cycicCircle = CycicCircles.addNode((String)facility.name, facility);
					facility.cycicCircle.setCenterX(event.getX());
					facility.cycicCircle.setCenterY(event.getY());
					facility.cycicCircle.text.setLayoutX(event.getX()-facility.cycicCircle.getRadius()*0.7);
					facility.cycicCircle.text.setLayoutY(event.getY()-facility.cycicCircle.getRadius()*0.6);
					facility.cycicCircle.menu.setLayoutX(event.getX());
					facility.cycicCircle.menu.setLayoutY(event.getY());
					
					for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
						if(DataArrays.visualizationSkins.get(i).name.equalsIgnoreCase(skins.getValue())){
							facility.cycicCircle.image.setImage(DataArrays.visualizationSkins.get(i).images.get(facility.niche));
							facility.cycicCircle.image.setVisible(true);
							facility.cycicCircle.setOpacity(0);
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
				facilityNode tempNode = new facilityNode();
				tempNode.facilityType = structureCB.getValue();
				for (int i = 0; i < DataArrays.simFacilities.size(); i++){
					if (DataArrays.simFacilities.get(i).facilityName == structureCB.getValue()){
						tempNode.facilityStructure = DataArrays.simFacilities.get(i).facStruct;
					}				
				}
				tempNode.name = facNameField.getText();
				tempNode.cycicCircle = CycicCircles.addNode((String)tempNode.name, tempNode);
				tempNode.sorterCircle = SorterCircles.addNode((String)tempNode.name, tempNode, tempNode);
				FormBuilderFunctions.formArrayBuilder(tempNode.facilityStructure, tempNode.facilityData);
			}
		});
		grid.add(submit1, 4, 0);
		
		ScrollPane scroll = new ScrollPane();
		scroll.setMinHeight(120);
		scroll.setContent(nodesPane);
		
		skins.getItems().add("Default Skin");
		skins.setValue("Default Skin");
		DataArrays.visualizationSkins.add(XMLReader.SC2);
		DataArrays.visualizationSkins.add(XMLReader.loadSkin(path));
		for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
			skins.getItems().add(DataArrays.visualizationSkins.get(i).name);
		}
		skins.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				if(skins.getValue().equalsIgnoreCase("Default Skin")){
					for(int j = 0; j < DataArrays.FacilityNodes.size(); j++){
						DataArrays.FacilityNodes.get(j).cycicCircle.image.setVisible(false);
						DataArrays.FacilityNodes.get(j).cycicCircle.setOpacity(100);
					}
				} else {
					for(int i = 0; i < DataArrays.visualizationSkins.size(); i++){
						skinSet skin = DataArrays.visualizationSkins.get(i);
						if(skin.name.equalsIgnoreCase(skins.getValue())){
							for(int j = 0; j < DataArrays.FacilityNodes.size(); j++){
								DataArrays.FacilityNodes.get(j).cycicCircle.image.setImage(skin.images.getOrDefault(DataArrays.FacilityNodes.get(j).niche, skin.images.get("facility")));
								DataArrays.FacilityNodes.get(j).cycicCircle.image.setVisible(true);
								DataArrays.FacilityNodes.get(j).cycicCircle.setOpacity(0);
							}
						}
					}
				}
			}
		});
		grid.add(new Label("Node Skins"){
			{
				setTooltip(new Tooltip("Use this drop down to select the skin set to use for your nodes."));
				setFont(new Font("Time", 14));
			}
		}, 0, 1);
		grid.add(skins, 1, 1);
		
        Button imageButton = new Button("Save fuel cycle diagram");
		imageButton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				export();
			}
		});
		grid.add(imageButton, 2, 1);
		opSwitch.getToggles().clear();
        opSwitch.getToggles().addAll(localToggle, remoteToggle);
        try {
            Process readproc = Runtime.getRuntime().exec("cyclus -V");
            new BufferedReader(new InputStreamReader(readproc.getInputStream()));
            localToggle.setSelected(true);
        } catch (RuntimeException | IOException e) {
            localToggle.setSelected(false);
            remoteToggle.setSelected(true);
        };
		grid.add(localToggle, 7, 0);
		grid.add(remoteToggle, 8, 0);

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
	 */
    public void retrieveSchema(String rawMetadata) {
        // rawMetadata is a JSON string.
        Reader metaReader = new StringReader(rawMetadata);
        JsonReader metaJsonReader = Json.createReader(metaReader);
        JsonObject metadata = metaJsonReader.readObject();
        metaJsonReader.close();
        JsonObject schemas = metadata.getJsonObject("schema");
        JsonObject annotations = metadata.getJsonObject("annotations");
            
        DataArrays.simFacilities.clear();
        DataArrays.simRegions.clear();
        DataArrays.simInstitutions.clear();
        for(javax.json.JsonString specVal : metadata.getJsonArray("specs").getValuesAs(JsonString.class)){
        	String spec = specVal.getString();
            boolean test = true;
            for(int j = 0; j < XMLReader.blackList.size(); j++){
                if(spec.equalsIgnoreCase(XMLReader.blackList.get(j))){
                    test = false;
                }
            }
            if(test == false){
                continue;
            }
            
            
            String schema = schemas.getString(spec);
            String pattern1 = "<!--.*?-->";
            Pattern p = Pattern.compile(pattern1, Pattern.DOTALL);
            schema = p.matcher(schema).replaceAll("");
            if(schema.length() > 12){
            	if(!schema.substring(0, 12).equals("<interleave>")){
                	schema = "<interleave>" + schema + "</interleave>"; 
                }
            }
            JsonObject anno = annotations.getJsonObject(spec);
            switch(anno.getString("entity")){
            case "facility":
                log.info("Adding archetype "+spec);
                facilityStructure node = new facilityStructure();
                node.facAnnotations = anno.toString();
                node.facilityArch = spec;
                node.niche = anno.getString("niche", "facility");
                JsonObject facVars = anno.getJsonObject("vars");
                ArrayList<Object> facArray = new ArrayList<Object>();
                node.facStruct = XMLReader.nodeBuilder(facVars, facArray, XMLReader.readSchema_new(schema));
                node.facilityName = spec.replace(":", " ");
                DataArrays.simFacilities.add(node);
                break;
            case "region":
                log.info("Adding archetype "+spec);
                regionStructure rNode = new regionStructure();
                rNode.regionAnnotations = anno.toString();
                rNode.regionArch = spec;
                JsonObject regionVars = anno.getJsonObject("vars");
                ArrayList<Object> regionArray = new ArrayList<Object>();
                rNode.regionStruct = XMLReader.nodeBuilder(regionVars, regionArray, XMLReader.readSchema_new(schema));
                rNode.regionName = spec.replace(":", " ");
                DataArrays.simRegions.add(rNode);
                break;
            case "institution":
                log.info("Adding archetype "+spec);
                institutionStructure iNode = new institutionStructure();
                iNode.institArch = spec;
                iNode.institAnnotations = anno.toString();
                JsonObject instVars = anno.getJsonObject("vars");
                ArrayList<Object> instArray = new ArrayList<Object>();
                iNode.institStruct = XMLReader.nodeBuilder(instVars, instArray, XMLReader.readSchema_new(schema));
                iNode.institName = spec.replace(":", " ");
                DataArrays.simInstitutions.add(iNode);
                break;
            default:
                log.error(spec+" is not of the 'facility', 'region' or 'institution' type. "
                    + "Please check the entity value in the archetype annotation.");
            break;
            };  
        }
        log.info("Schema discovery complete");
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
		circle.setFill(Color.rgb(circle.rgbColor.get(0),circle.rgbColor.get(1),circle.rgbColor.get(2)));
		circle.setCenterX(45+(i*90));
		circle.setCenterY(50);
		circle.text.setText(name.split(" ")[2] + " (" + name.split(" ")[1] + ")");
		circle.text.setWrapText(true);
		circle.text.setMaxWidth(60);
		circle.text.setLayoutX(circle.getCenterX()-circle.getRadius()*0.7);
		circle.text.setLayoutY(circle.getCenterY()-circle.getRadius()*0.6);	
		circle.text.setTextAlignment(TextAlignment.CENTER);
		circle.text.setMouseTransparent(true);
		circle.text.setMaxWidth(circle.getRadius()*1.4);
		circle.text.setMaxHeight(circle.getRadius()*1.2);
		circle.setOnDragDetected(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				Dragboard db = circle.startDragAndDrop(TransferMode.COPY);
				ClipboardContent content = new ClipboardContent();				
				content.put(DnD.VALUE_FORMAT, name);
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
	ArrayList<String> monthList = new ArrayList<String>();
	/**
	 * Quick hack to convert months into their integer values.
	 * i.e. January = 0, Feb = 1, etc...
	 */
	public void months(){
		
		Cycic.workingScenario.simulationData.startMonth = "0";
		
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
	public void details(){
		Label simDets = new Label("Simulation Details");
		simDets.setTooltip(new Tooltip("The top level details of the simulation."));
		simDets.setFont(new Font("Times", 16));
		simInfo.add(simDets, 0, 0);
		TextField duration = VisFunctions.numberField();
		duration.setMaxWidth(150);
		duration.setPromptText("Length of Simulation");
		duration.setText(Cycic.workingScenario.simulationData.duration);
		Cycic.workingScenario.simulationData.duration = "0";
		duration.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.duration = newValue;
			}
		});
		simInfo.add(new Label("Duration (Months)"), 0, 1);
		simInfo.add(duration, 1, 1);
		

		final ComboBox<String> startMonth = new ComboBox<String>();
		startMonth.setValue(months.get(Cycic.workingScenario.simulationData.startMonth));
		for(int i = 0; i < 12; i++ ){
			startMonth.getItems().add(monthList.get(i));
		}
		Cycic.workingScenario.simulationData.startMonth = "1";
		startMonth.setValue(monthList.get(Integer.parseInt(Cycic.workingScenario.simulationData.startMonth)));
		startMonth.valueProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.startMonth = months.get(newValue);
			}
		});
		startMonth.setPromptText("Select Month");
		simInfo.add(new Label("Start Month"), 0, 2);
		simInfo.add(startMonth, 1, 2);
		TextField startYear = VisFunctions.numberField();
		startYear.setText(Cycic.workingScenario.simulationData.startYear);
		Cycic.workingScenario.simulationData.startYear = "0";
		startYear.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.startYear = newValue;
			}
		});
		startYear.setPromptText("Starting Year");
		startYear.setMaxWidth(150);
		simInfo.add(new Label("Start Year"), 0, 3);
		simInfo.add(startYear, 1, 3);
				
		TextArea description = new TextArea();
		description.setMaxSize(250, 50);
		description.setWrapText(true);
		description.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.description = newValue;
			}
		});
		simInfo.add(new Label("Description"), 0, 4);
		simInfo.add(description, 1, 4);
		
		TextArea notes = new TextArea();
		notes.setMaxSize(250, 50);
		notes.setWrapText(true);
		notes.textProperty().addListener(new ChangeListener<String>(){
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue){
				Cycic.workingScenario.simulationData.notes = newValue;
			}
		});
		simInfo.add(new Label("Notes"), 0, 5);
		simInfo.add(notes, 1, 5);
		
	
		// Prints the Cyclus input associated with this simulator. 
		Button output = new Button("Generate Cyclus Input");
		output.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				if(OutPut.inputTest()){
					FileChooser fileChooser = new FileChooser();
					//Set extension filter
					FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
					fileChooser.getExtensionFilters().add(extFilter);
					fileChooser.setTitle("Please save as Cyclus input file.");
					fileChooser.setInitialFileName("*.xml");
					//Show save file dialog
					File file = fileChooser.showSaveDialog(window);
					OutPut.output(file);
				}
			}
		});
		simInfo.add(output, 0, 6);
		
        Button runCyclus = new Button("Run Cyclus!");
        runCyclus.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                if(!OutPut.inputTest()){
                    log.info("Cyclus Input Not Well Formed!");
                    return;  // safety dance
                }
                if (localToggle.isSelected()) {
                    // local execution
                    String tempHash = Integer.toString(OutPut.xmlStringGen().hashCode());
                    String cycicTemp = "cycic"+tempHash;
                    try {
                        File temp = File.createTempFile(cycicTemp, ".xml");
                        FileWriter fileOutput = new FileWriter(temp);
                        new BufferedWriter(fileOutput);
                        Process p = Runtime.getRuntime().exec("cyclus -o "+cycicTemp +".sqlite "+cycicTemp);
                        p.waitFor();
                        String line = null;
                        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        while ((line = input.readLine()) != null) {        
                            log.info(line);
                        }
                        input.close();
                        log.info("Cyclus run complete");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    // remote execution
            		String cycicXml = OutPut.xmlStringGen();
            		CyclistController._cyclusService.submit(cycicXml);
            	}
            }
        });;
        simInfo.add(runCyclus, 1,6);    
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
				for(int i = 0; i < DataArrays.simFacilities.size(); i++){
					if(DataArrays.simFacilities.get(i).facilityName.equalsIgnoreCase(archetypes.getValue())){
						if(DataArrays.simFacilities.get(i).loaded == true){
							return;
						}
						facilityStructure test = DataArrays.simFacilities.get(i);
						new StringBuilder();
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
							nodesPane.getChildren().addAll(circle,circle.text);
						} catch (Exception eq) {
							
						}
					}
				}
			}
		});

        Button cyclusDashM = new Button("Discover Archetypes");
        cyclusDashM.setTooltip(new Tooltip("Use this button to search for all local Cyclus modules."));
        cyclusDashM.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                if (localToggle.isSelected()) {
                    // Local metadata collection
                    try {
                        Process readproc = Runtime.getRuntime().exec("cyclus -m");
                        BufferedReader metaBuf = new BufferedReader(new InputStreamReader(readproc.getInputStream()));
                        String line=null;
                        String metadata = new String();
                        while ((line = metaBuf.readLine()) != null) {metadata += line;}
                        metaBuf.close();
                        retrieveSchema(metadata);
                    } catch (IOException ex) {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                    }
                } else {
                    // Remote metadata collection
                    CyclistController._cyclusService.submitCmd("cyclus", "-m");
                    _remoteDashA = CyclistController._cyclusService.latestJob();
                    _remoteDashA.statusProperty()
                        .addListener(new ChangeListener<CyclusJob.Status>() {
                        @Override
                        public void changed(ObservableValue<? extends Status> observable,
                            Status oldValue, Status newValue) {
                            if (newValue == Status.READY) {
                                retrieveSchema(_remoteDashA.getStdout());
                            }
                        }
                    });
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
			System.out.println("File did not generate correctly.");
		}
	}
	
	private void defaultJsonReader(String path) throws IOException{
	    BufferedReader reader = new BufferedReader( new FileReader (path + "/default-metadata.json"));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    reader.close();
	    
		retrieveSchema(stringBuilder.toString());
	}
}
