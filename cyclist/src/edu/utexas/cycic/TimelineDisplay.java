package edu.utexas.cycic;

import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBoxBuilder;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import edu.utah.sci.cyclist.core.ui.components.ViewBase;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.dialog.Dialogs;

/*
 * Class used by CYCIC that expands the Ellipse.Java class. The class 
 * is used to visualize the facilities and events in the simulation.
 * @author Alfred
 */
public class TimelineDisplay extends ViewBase {

	public TimelineDisplay(){
		super();
		setWidth(900);
		setMinWidth(900);
		//setResizable(false);
		setMaxWidth(900);
		setHeight(600);
		setMaxHeight(900);
		init ();
	}

	static BorderPane pane= new BorderPane();
	static ArrayList <Object> structure = new ArrayList<>();
	static ArrayList <Object> facilityInformation = new ArrayList <>();
	static ArrayList <Object> facilityParentNode = new ArrayList <>();
	static Integer MaxRange = 0;
	static Integer MinRange = 0;
	static ArrayList <Object> timelineDB= new ArrayList<>();

	static ArrayList<instituteNode> displayNodes = new ArrayList <>();
	// name, arch
	static ArrayList <Object> institutionList = new ArrayList<>();
	
	
	//what
	static ArrayList <Object> getDB = new ArrayList <>();
	//start of the stage
	
	private Scene addNewScene;
	//private Scene detailScene;
	private Stage stage; 

	private Scene formatMainScene (final Stage s) {
		VBox layout = new VBox();
		final TextField nameText = new TextField();
		final TextField startText = new TextField();
		final TextField endText = new TextField();
		final String [] inputData;
		/*inputData format
		 * name facility name
		 * institution name
		 * deployed (true or false)
		 * facilityType
		 * startTime (if deployed)
		 */
		inputData = new String [5];
		
		final ComboBox facility = new ComboBox();
		final ComboBox facilityType = new ComboBox();
		final VBox checkTime = new VBox();
		
		
		for (int i = 0; i < institutionList.size(); i ++) {
			facility.getItems().add(((ArrayList<Object>) institutionList.get(i)).get(0));
		}
		facility.valueProperty().addListener(new ChangeListener<String>(){

			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
				// TODO Auto-generated method stub
				inputData[1] = arg2;
				//detailScene = createSubScene(inputData[2]); 
				//stage.setScene (detailScene);
				
				for (int i = 0; i < institutionList.size(); i++) {
					if (((String) ((ArrayList<Object>) institutionList.get(i)).get(0)).equalsIgnoreCase(arg2)) {
						checkTime.getChildren().clear();
						inputData[2] = Boolean.toString(false);
						
						if (((String) ((ArrayList<Object>) institutionList.get(i)).get(1)).equalsIgnoreCase("DEPLOYEDINSTITUTION")){
							checkTime.getChildren().clear();
							inputData[2] = Boolean.toString(true);
						}
					}
				}
				
				
			}
			
		} );
		
		facilityType.getItems().add("Reactor");
		facilityType.getItems().add("Sink Facility");
		facilityType.getItems().add("Enrichment");
		facilityType.valueProperty().addListener(new ChangeListener<String>(){

			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
				// TODO Auto-generated method stub
				inputData[3] = arg2;
				
				
			}
			
		} );
		// layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");
		    layout.getChildren().setAll(
		      LabelBuilder.create()
		        .text("add New Facility ")
		        .style("-fx-font-weight: bold;") 
		        .build(),
		      HBoxBuilder.create()
		        .spacing(5)
		        .children(
		          new Label("FacilityName:"),
		         nameText
		         )
		        .build(),

		        new Label("Institution Name"),
		        HBoxBuilder.create()
		        .spacing(5)
		        .children(facility)
		        .build()
		      ,
		      new Label("Facility Type"),
		      HBoxBuilder.create().spacing(5)
		      .children(facilityType).build(),
		     HBoxBuilder.create()
		     .spacing(5)
		     .children(checkTime).build(),
		      ButtonBuilder.create()
		        .text("add")
		        .defaultButton(true)
		        .onMouseClicked(new EventHandler<MouseEvent>(){
					@Override
					public void handle(MouseEvent event){
						
							s.hide();
							setPane();

						
						
					}
				})
		        .build(),
		        
		        ButtonBuilder.create()
		        .text("cancel").defaultButton(true).onMouseClicked(new EventHandler<MouseEvent>(){
					@Override
					public void handle(MouseEvent event){
						s.hide();

					}
				}).build()
				
				
		    );
		    
		    
		    
		    return new Scene(layout);
		  }

	/*
	void dbInit() {
		/*
		 * db structure 
		 * [
		 * 		[type, 
		 * 			[facilityName, startday, end day, duration, [
		 * 					[eventname, event dates, detailed information],
		 * 													],
		 * 			]
		 * ]
		 * 
		 * 
		 */
	/*	for(int i = 0; i < DataArrays.simFacilities.size(); i++){
			type.add((String) DataArrays.simFacilities.get(i).facilityName);	
		}*/
	/*	institutionList.clear();
		institutionList.add("insitution A");
		institutionList.add("InstitutionB");
		institutionList.add("InstitutionC");
		*/
		/*
		for (int i = 0; i<institutionList.size();i++) {
			ArrayList <Object> element = new ArrayList <>();
			element.add(institutionList.get(i));
			for (int ii=0; ii< CycicScenarios.workingCycicScenario.FacilityNodes.size();ii++){
				if (institutionList.get(i).equalsIgnoreCase(CycicScenarios.workingCycicScenario.FacilityNodes.get(ii).facilityType)) {
					for (int iii = 0; iii<CycicScenarios.workingCycicScenario.FacilityNodes.get(ii).facilityClones.size(); iii++){
					/*	ArrayList <Object> individualElement = new ArrayList <>();
						individualElement.add(CycicScenarios.workingCycicScenario.FacilityNodes.get(ii).facilityClones.get(iii).name);
						individualElement.add(1995); //startime
						individualElement.add(2056);//endtime
						individualElement.add(61); //duration (usually only one piece of data available
						ArrayList <Object>event = new ArrayList <>();
						eventGeneration (event, individualElement.get(0));
					*/	
		/*				
					}
				}
			}
			*/
		//	timelineDB.add(element);
/*		}
	}
	*/
	static GridPane outputPanel=new GridPane(){
		{
			setWidth(740);
			setHeight(300);
			setMinWidth(740);
			setMaxWidth(740);
			setVgap (1);
			setHgap (1);

		}
	};
	
	static GridPane namePanel = new GridPane() {
		{
		setWidth(150);
		//setHeight(outputPanel.getHeight());
		setVgap(1);
		setHgap(1);
		
		}
	};
	
	

	
	static ScrollPane spdisplay = new ScrollPane() {
		{
			setPrefViewportHeight(outputPanel.getHeight());
			setWidth(740);
			setMinWidth(740);
			setMaxWidth(740);
			setHbarPolicy(ScrollBarPolicy.NEVER);
			//setPrefViewportWidth(2000);
			setFitToWidth(true);
			setVbarPolicy(ScrollBarPolicy.NEVER);
			setContent(outputPanel);
			
			//setHvalue(fnPane.getHvalue());
			
		}

	};
	
	static ScrollPane fnPane = new ScrollPane() {
		{
			setPrefViewportHeight(spdisplay.getHvalue());
			setVbarPolicy(ScrollBarPolicy.ALWAYS);
			setHbarPolicy(ScrollBarPolicy.NEVER);
			//setHvalue(spdisplay.getHvalue());
			setPrefViewportHeight(300);
			setFitToWidth(false);
			setWidth(150);
			setMaxWidth(150);
			setMaxHeight(300);
			setContent(namePanel);
			
			
		}
	};

	static GridPane tlp = new GridPane(){
		{
			setWidth(740);
			setHeight(40);
			setVgap(1);
			setHgap(1);
			setMaxHeight(40);
		}
	};

	private void init (){
		for(int i = 0; i < DataArrays.simInstitutions.size(); i++){
			ArrayList <Object> temp = new ArrayList <>();
				temp.add(DataArrays.simInstitutions.get(i).institStruct);
				temp.add( DataArrays.simInstitutions.get(i).institArch);
		}
		
	/*	ArrayList <Object> structureOrder = new ArrayList<>();
		ArrayList <Object> displayData = new ArrayList<>();

		for (int i = 0; i < CycicScenarios.workingCycicScenario.FacilityNodes.size();i++){
			structure.add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).facilityStructure);
			System.out.println(structure.get(i));
			facilityParentNode.add((String)CycicScenarios.workingCycicScenario.FacilityNodes.get(i).name
					+ "  ("+CycicScenarios.workingCycicScenario.FacilityNodes.get(i).facilityType +")");
		}
*/
	//	System.out.println((String) DataArrays.simFacilities.get(0).facilityName);
		demoDev.initializeSample(getDB);
		for(int i = 0; i < DataArrays.simFacilities.size(); i++){
			System.out.println((String) DataArrays.simFacilities.get(i).facilityName);	
		}
		
		displayNodes = DataArrays.institNodes;
		for (int i = 0; i < displayNodes.size(); i++){
			
		}
		//int startYear = Integer.parseInt(displayNodes.);
		//int endYear = (int) (startYear +Math.ceil(Double.parseDouble(DataArrays.simulationData.duration)/12));
		
		//System.out.println(startYear+endYear);
		VBox panel=new VBox(){
			{
				setWidth(getWidth());
				setHeight(getHeight());
			}
		};

		HBox interAction =new HBox(){
			{
				setWidth(getWidth());
				setSpacing(20);
			}
		};

		Label output=new Label();
		output.setText("OutputPanel");
		outputPanel.getChildren().add(output);
		final RangeSlider yearRange = new RangeSlider(0,2200,1910,1990);
		yearRange.setMin(1900);
		yearRange.setShowTickMarks(true);
		yearRange.setShowTickLabels(true);
		yearRange.setBlockIncrement(10);

		final ObservableList<String> displayType = FXCollections.observableArrayList();
		if (displayType.isEmpty()){
			displayType.add("All Nodes");
			displayType.add("rec1");
			displayType.add("storage");
		}
		for (int ii = 0; ii < facilityParentNode.size(); ii++){

			displayType.add((String) facilityParentNode.get(ii));
		}
		HBox minField=new HBox();
		minField.setSpacing(5);
		Label min=new Label();
		min.setText("Min");
		HBox maxField=new HBox();
		maxField.setSpacing(5);
		Label max=new Label();
		max.setText("Max");

		final TextField minNumber = new TextField();
		final TextField maxNumber = new TextField();
		minNumber.textProperty().bind(yearRange.lowValueProperty().asString("%.0f"));
		MinRange= Integer.parseInt(minNumber.getText ());
		maxNumber.textProperty().bind(yearRange.highValueProperty().asString("%.0f"));
		MaxRange= Integer.parseInt(maxNumber.getText ());
		minField.getChildren().add(min);
		minField.getChildren().add(minNumber);
		maxField.getChildren().add(max);
		maxField.getChildren().add(maxNumber);
		final CheckComboBox<String> checkBox = new CheckComboBox<String>(displayType);
		checkBox.setMaxWidth(200);
		checkBox.setMinWidth(200);
		
		
		
		
		checkBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
			public void onChanged(ListChangeListener.Change<? extends String> c) {
				System.out.println(checkBox.getCheckModel().getCheckedItems());
				setPane();

			}
		});

		interAction.getChildren().add(checkBox);
		interAction.getChildren().add(minField);
		interAction.getChildren().add(maxField);
		panel.getChildren().add(yearRange);
		panel.getChildren().add(interAction);
		
		yearRange.setOnMouseDragged(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				MinRange= Integer.parseInt(minNumber.getText ());
				MaxRange= Integer.parseInt(maxNumber.getText ());
				System.out.println (MinRange+"+"+MaxRange);
				setPane();

			}
		});
		Button addNew = new Button();
		addNew.setId("0");
		addNew.setText("Add");
		
		addNew.setOnMouseClicked(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				Stage eStage = new Stage();
				/*		eventDialogStage.initModality(Modality.WINDOW_MODAL);
						eventDialogStage.setScene(new Scene(VBoxBuilder.create().
						    children(new Text(eventDetails)).
						    alignment(Pos.BOTTOM_CENTER).padding(new Insets(50)).build()));
						eventDialogStage.show();
						*/
		/*				Dialogs.create()
				        .owner(eventDialogStage)
				        .title("addNewFacility")
				        .masthead(null)
				        ..showInformation()
				        ;
				*/	
				//this.stage = stage;
				eStage.setScene(formatMainScene(eStage));
				eStage.show();
			
			}
		});
		
		//useless code
		Line useless = new Line (0,0,0,39);
		useless.setVisible(false);
		tlp.add(useless, 0, 0, 1, 39);





		//useless4
		
		
		
		HBox box = new HBox();
		VBox subBox = new VBox();
		subBox.getChildren().add(fnPane);
		subBox.getChildren().add(addNew);
		box.getChildren().add(subBox);
		//box.getChildren().add(addNew);
		//box.getChildren().add(spdisplay);
		box.setMaxWidth(900);
		box.setMinWidth(900);
		
		VBox stack = new VBox();
		stack.getChildren().add(spdisplay);
		stack.getChildren().add(tlp);
		box.getChildren().add(stack);
		
		pane.setTop(box);
		//pane.setCenter(tlp);
		pane.setBottom(panel);
		setContent(pane);


	}
	/**
	   * This method is initizalize the display panel 
	   */
	protected void setPane () {
		//dbInit();
		outputPanel.getChildren().clear();
		namePanel.getChildren().clear();
		tlp.getChildren().clear();
		

		DoubleProperty vPosition = new SimpleDoubleProperty();
		vPosition.bind(fnPane.vvalueProperty());
		vPosition.addListener(new ChangeListener () {


			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				spdisplay.setVvalue((Double)arg2);
			}
			
		});
		
		int yearDifference = MaxRange-MinRange;
		double unitWidth = tlp.getWidth()/yearDifference;

		Line xLine= new Line (0,0,(int)tlp.getWidth()-1,0);


		tlp.add(xLine, 0, 35,(int)tlp.getWidth()-1,2);
		for (int ii = MinRange; ii < MaxRange-1 ; ii ++){
			/*Line line = new Line (0,0,0,220);
			line.getStrokeDashArray().addAll(5d, 15d);
			outputPanel.add (line,0,0,1,230);
			 */
			if (ii%50 == 0) {
				Line fiftyTick = new Line (0,0,0,35);
				int loc = (int) ((ii-MinRange)*unitWidth);
				Text fifties = new Text (Integer.toString(ii));
				if (loc <= tlp.getWidth()-2){
					tlp.add (fiftyTick,loc,0,2,35);
				}
				else {
					tlp.add (fiftyTick,loc-2,0,2,35);
				}
				if (loc <=15){
					tlp.add (fifties ,loc, 36, 29, 4);
				} else if((loc > 15) && 
						(tlp.getWidth()-loc >= 15)){
					tlp.add (fifties, loc-15, 36, 29, 4);

				} else {
					tlp.add (fifties ,(int)(outputPanel.getWidth()-30), 36, 29, 4);

				}

			} else if (ii%10 == 0) {

				Line tenTick = new Line (0, 0, 0, 15);
				int loc =  (int) ((ii-MinRange)*unitWidth);
				if (loc <= tlp.getWidth()-2){

					tlp.add(tenTick, loc, 20,2,15);
				}
				else {
					tlp.add(tenTick, loc-2, 20,2, 15);
				}
				if (yearDifference <= 100) {

					Text ten = new Text (Integer.toString(ii));

					if (loc <= 15){
						tlp.add (ten ,loc, 36, 29, 4);
					} else if((loc > 15) && 
							(tlp.getWidth()-loc >=15)){
						tlp.add (ten, loc-15, 36, 29, 4);

					} else {
						tlp.add (ten ,(int)(outputPanel.getWidth()-30), 36, 29, 4);

					}

				}

			} 
			if (yearDifference <=20) {

				if ((ii%2 ==0)&&(ii%10 !=0)) {
					Line twoTick = new Line (0, 0, 0, 10);
					int loc = (int) ((ii- MinRange)*unitWidth);
					if (loc <= tlp.getWidth()-2){

						tlp.add(twoTick, loc, 25,2,10);
					}
					else {
						tlp.add(twoTick, loc-2, 25,2, 10);
					}

					Text two = new Text (Integer.toString(ii));

					if (loc <= 15){
						tlp.add (two ,loc, 36, 29, 4);
					} else if((loc > 15) && 
							(tlp.getWidth()-loc >= 15)){
						tlp.add (two, loc- 15, 36, 29, 4);

					} else {
						tlp.add (two ,(int)(outputPanel.getWidth()-30), 36, 29, 4);

					}

				}

			}
		}

	displayNodes ();


	}
	/**
	   * This method is used to add facility events and its label onto
	   * corresponding facility lines. 
	   * @param event the node to display
	   * @param y the order of facility it belongs
	   */
	protected void addEvents(Object event, final int y) {
		ArrayList <Object> facilityEvent = (ArrayList<Object>) event; 
		int radius = 4; 
		int yearDifference = MaxRange-MinRange;
		double unitWidth = outputPanel.getWidth()/yearDifference;


		for (int i = 0; i < facilityEvent.size(); i++) {
			ArrayList <Object> singleEvent = (ArrayList<Object>) facilityEvent.get(i);
			int eventYear = Integer.parseInt( singleEvent.get(1).toString());
			final String eventName = (String) singleEvent.get(0);
			final String eventDetails = (String) singleEvent.get(2);
			Circle eventCircle = new Circle();

			final Rectangle eventInformation = new Rectangle (0,0,75,45);
			
			eventInformation.setVisible(true);
			eventInformation.setFill(Color.WHEAT);

			eventCircle.setFill(Color.DARKORANGE);
			final int x;
			final int xx;


			if ((eventYear < MinRange)||(eventYear > MaxRange)){
				x = 0;
				xx = 0;

			} else if (((eventYear-4)<= MinRange)&&(eventYear-4)>0) {
				x = 4;
				xx = 4;


			} else if ((eventYear+75)>= MaxRange) {

				if ((eventYear+4)>= MaxRange){
					x = (int) (outputPanel.getWidth()-5);
				}
				else {
					x = (int) ((eventYear-MinRange)*unitWidth-4);
				}

				xx = (int) (outputPanel.getWidth()-76);


			} else {

				x = (int) ((eventYear-MinRange)*unitWidth-4);
				xx = (int) (outputPanel.getWidth()-76);


			}

			if (x !=0){
				eventCircle.setCenterX(x);
				eventCircle.setCenterY(y);
				eventCircle.setRadius (radius);
				Text newLabel = new Text (eventName);
				if (x >=4) {
					outputPanel.add(eventCircle,x-4,y-12,8,8);
				} else if (x>0&&x<4) {
					outputPanel.add(eventCircle,0,y-12,8,8);
				}
				//outputPanel.add(eventCircle,x-4,y-12,8,8);
				if (xx >=20){
					outputPanel.add(newLabel,x-20,y-20,40,40);
				} else if (xx > 0 && xx<20){
					outputPanel.add(newLabel,0,y-20,40,40);
				}
				//outputPanel.add(newLabel,x-20,y-20,40,40);
				//outputPanel.add(info,xx-20,y-60, 74, 45);

				eventCircle.setOnMousePressed(new EventHandler <MouseEvent>(){
					@Override
					public void handle(MouseEvent e) {
						Stage eventDialogStage = new Stage();
				
						Dialogs.create()
				        .owner(eventDialogStage)
				        .title(eventName)
				        .masthead(null)
				        .message(eventDetails)
				        .showInformation();
					}
				});

				 

			}
		}
	}
	
	
	/**
	   * This method is used to add facility lines
	   * into the grid pane. 
	   */
	protected void displayNodes () {

		int spacing = 35;
		int yearDifference = MaxRange-MinRange;
		double unitWidth = outputPanel.getWidth()/yearDifference;
		double width = outputPanel.getWidth();
		//System.out.println(unitWidth);
		//test	
		//	grid add(Node child, int columnIndex, int rowIndex, int colspan, int rowspan)
		
		int ii=0;
		for (int i = 0; i < getDB.size(); i++){
			ArrayList <Object> singleEntry = (ArrayList<Object>) getDB.get(i);
			Rectangle facilityLine;
			ii++;

			int reserveSpace = spacing*(i+1)-10;
			if ((Integer.parseInt((String) singleEntry.get(1)) <= MinRange) 
					&& (Integer.parseInt((String) singleEntry.get(2))>= MaxRange)){
				//1
				facilityLine = new Rectangle (0,0,width-1,4);
				facilityLine.setFill(Color.LIGHTGRAY);
				outputPanel.add(facilityLine,0,reserveSpace,(int)width-1,4);
				namePanel.add(new Text( singleEntry.get(0).toString()), 1, reserveSpace-6, 130, 12);				
				addEvents (singleEntry.get(3),spacing*(i+1));

			} else if ((Integer.parseInt((String) singleEntry.get(1)) <= MinRange)&&
					((Integer.parseInt((String)singleEntry.get(2))<= MaxRange)
							&& (Integer.parseInt((String) singleEntry.get(2))> MinRange))){
				//2
				int range = Integer.parseInt((String)singleEntry.get(2))-MinRange;
				int High = (int) (range*unitWidth)+1;
				facilityLine = new Rectangle (0, 0, High-1, 4);
				facilityLine.setFill(Color.LIGHTGRAY);
				outputPanel.add(facilityLine, 0, reserveSpace,High,4);
				namePanel.add(new Text( singleEntry.get(0).toString()), 1, reserveSpace-6, 130, 12);				
				addEvents (singleEntry.get(3),spacing*(i+1));

			} else if ((Integer.parseInt((String) singleEntry.get(1)) > MinRange) 
					&&(Integer.parseInt((String) singleEntry.get(2)) >= MaxRange
					&&(Integer.parseInt((String)singleEntry.get(1)) < MaxRange))){
				//3
				int Low = (int) (Integer.parseInt((String) singleEntry.get(1))-MinRange);
				int startLocation = (int) (Low * unitWidth);
				facilityLine = new Rectangle (0,0,width-startLocation-1, 4);
				facilityLine.setFill(Color.LIGHTGRAY);
				int range = (int) (width-startLocation)+1;
				outputPanel.add(facilityLine, startLocation, reserveSpace, range,4);
				namePanel.add(new Text( singleEntry.get(0).toString()), 1, reserveSpace-6, 130, 12);								
				addEvents (singleEntry.get(3),spacing*(i+1));

			} else if ((Integer.parseInt((String) singleEntry.get(1)) > MinRange)
					&&(Integer.parseInt((String) singleEntry.get(2)) < MaxRange
							&&(Integer.parseInt((String)singleEntry.get(1)) < MaxRange))) {
				//4
				int High = Integer.parseInt((String) singleEntry.get(2))-MinRange;
				int Low = Integer.parseInt((String) singleEntry.get(1))-MinRange;
				int startLocation = (int) ( Low*unitWidth);
				int range = (int) ((High-Low)*unitWidth)+1;
				facilityLine = new Rectangle (0,0,range-1, 4);
				facilityLine.setFill(Color.LIGHTGRAY);
				outputPanel.add (facilityLine, startLocation, reserveSpace, range, 4);
				namePanel.add(new Text( singleEntry.get(0).toString()), 1, reserveSpace-6, 130, 12);
				
				addEvents (singleEntry.get(3),spacing*(i+1));



			} else {
				//5
				facilityLine = new Rectangle (0,0,1,1);
				facilityLine.setFill(Color.WHITE);
				outputPanel.add (facilityLine, 0, reserveSpace, 1, 4);
				namePanel.add(new Text( singleEntry.get(0).toString()), 1, reserveSpace-6, 130, 12);
			}

			
			facilityLine.setOnMousePressed(new EventHandler <MouseEvent>(){
				@Override
				public void handle(MouseEvent e) {
					Stage dialogStage = new Stage();
					dialogStage.initModality(Modality.WINDOW_MODAL);
					dialogStage.setScene(new Scene(VBoxBuilder.create().
					    children(new Text("facility Information")).
					    alignment(Pos.BOTTOM_CENTER).padding(new Insets(100)).build()));
					dialogStage.show();
					
				}




			});
			namePanel.add(new Text( ""), 1, spacing*(ii+1)-6, 130, 12);
			
		

		}



	}
	public static boolean isInteger(String s) {
		try { 
			Integer.parseInt(s); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		// only got here if we didn't return false
		return true;
	}

}

