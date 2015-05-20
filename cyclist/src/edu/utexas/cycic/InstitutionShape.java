package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.event.dnd.DnD;
import edu.utah.sci.cyclist.core.tools.Tool;
import edu.utexas.cycic.tools.InstitutionViewTool;
import edu.utexas.cycic.tools.RegionViewTool;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;

public class InstitutionShape extends Ellipse {
	{
		setOnDragOver(new EventHandler <DragEvent>(){
			public void handle(DragEvent event){
				event.acceptTransferModes(TransferMode.ANY);
			}
		});
		setOnDragDropped(new EventHandler <DragEvent>(){
			public void handle(DragEvent event){
				boolean test = false;
				if(event.getDragboard().hasContent(CycICDnD.UNASSOC_FAC)){
					String facName = event.getDragboard().getContent(CycICDnD.UNASSOC_FAC).toString();
					if(institBackTrace.availFacilities.size() > 0){
						for(facilityItem fac: institBackTrace.availFacilities){
							if(facName.equalsIgnoreCase(fac.name)){
								int temp  = Integer.parseInt(fac.number);
								temp += 1;
								fac.number = String.valueOf(temp);
								test = true;
							}
							if (test == false){
								facilityItem temp_fac = new facilityItem(facName, 1);
								institBackTrace.availFacilities.add(temp_fac);
							}
						}
					} else {
						facilityItem temp_fac = new facilityItem(facName, 1);
						institBackTrace.availFacilities.add(temp_fac);
					}
					event.consume();
				} else {
					event.consume();
				}
			}
		});
	}
	protected static double mousey;
	protected static double mousex;
	protected static double x;
	protected static double y;
	protected static double deltax;
	protected static double deltay;
	Object name;
	Label text = new Label("");
	MenuBar menuBar = new MenuBar();
	static instituteNode institBackTrace;
	ArrayList<Integer> rgbColor = new ArrayList<Integer>();
	


	static InstitutionShape addInst(final String name, final instituteNode instit) {
		final InstitutionShape institution = new InstitutionShape();
		
		institBackTrace = instit;
		InstitutionCorralView.workingInstitution = instit;
		
		// Set properties of regionNode
		instit.name = name;
		
		institution.setRadiusX(80);
		institution.setRadiusY(40);
		institution.setLayoutX(50);
		institution.setLayoutY(50);
		institution.setStroke(Color.DARKGRAY);
		institution.setStrokeWidth(5);
		
		institution.name = name;
		institution.text.setText(name);
		institution.text.setLayoutX(institution.getLayoutX()-institution.getRadiusX()*0.5);
		institution.text.setLayoutY(institution.getLayoutY()-institution.getRadiusY()*0.5);	
		institution.text.setMaxWidth(institution.getRadiusX()*0.8);
		institution.text.setMaxHeight(institution.getRadiusY()*0.8);
		institution.text.setMouseTransparent(true);
		institution.text.setFont(new Font(14));
		institution.text.setWrapText(true);
		
		// Set circle color
		institution.rgbColor=VisFunctions.stringToColor(instit.type);
		institution.setFill(Color.rgb(institution.rgbColor.get(0), institution.rgbColor.get(1), institution.rgbColor.get(2), 0.8));
		// Setting font color for visibility //
		if(VisFunctions.colorTest(institution.rgbColor) == true){
			institution.text.setTextFill(Color.BLACK);
		}else{
			institution.text.setTextFill(Color.WHITE);
		}
		
		institution.setEffect(VisFunctions.lighting);

		//Adding the circle's menu and its functions.

		MenuItem regionForm = new MenuItem("Region Form");
		regionForm.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				CyclistController._presenter.addTool(new RegionViewTool());
				institution.menuBar.setVisible(false);
			}
		});

		EventHandler<ActionEvent> deleteEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent deleteEvent) {
				deleteInstitution(institution, instit);
				institution.menuBar.setVisible(false);
			}
		};
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(deleteEvent);

		EventHandler<ActionEvent> exitEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent exitEvent) {
				institution.menuBar.setVisible(false);
			}
		};
		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(exitEvent);
		
		final Menu menu = new Menu("Options");
		menu.getItems().addAll(regionForm, delete, exit);		

		institution.menuBar.getMenus().add(menu);
		institution.menuBar.setLayoutX(institution.getLayoutX());
		institution.menuBar.setLayoutY(institution.getLayoutY());
		institution.menuBar.setVisible(false);

		institution.onMouseClickedProperty().set(new EventHandler <MouseEvent>(){
			@Override
			public void handle(MouseEvent menuEvent){
				if(menuEvent.getButton().equals(MouseButton.SECONDARY)){
					institution.menuBar.setVisible(true);
					institution.menuBar.setLayoutX(institution.getLayoutX());
					institution.menuBar.setLayoutY(institution.getLayoutY());
				}
				
				if(menuEvent.getClickCount() == 2){
                    menuEvent.consume();
					CyclistController._presenter.addTool(new InstitutionViewTool());
				}
				for(int i = 0; i < RegionCorralView.corralPane.getChildren().size(); i++){
					if(RegionCorralView.corralPane.getChildren().get(i).getId() == "this"){
						((Shape) RegionCorralView.corralPane.getChildren().get(i)).setStroke(Color.BLACK);
						((Shape) RegionCorralView.corralPane.getChildren().get(i)).setStrokeWidth(1);
					}
				}
				
				institution.setEffect(VisFunctions.lighting);
				institution.setStrokeWidth(5);
				institution.setStroke(Color.DARKGRAY);
				
			}
		});
		
		// Allows a shift + (drag and drop) to start a new RegionView for this RegionShape.
		institution.setOnDragDetected(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent e){
				if(e.isShiftDown() == true){

					InstitutionCorralView.workingInstitution = instit;
					
					DnD.LocalClipboard clipboard = DnD.getInstance().createLocalClipboard();
					clipboard.put(DnD.TOOL_FORMAT, Tool.class, new InstitutionViewTool());
					
					Dragboard db = institution.startDragAndDrop(TransferMode.COPY);
					ClipboardContent content = new ClipboardContent();				
					content.put( DnD.TOOL_FORMAT, "Institution View");
					db.setContent(content);
					
					e.consume();
				}
			}
		});
		
		return institution;	
	};

	static void deleteInstitution(InstitutionShape circle, instituteNode instit){
		DataArrays.institNodes.remove(instit);
		InstitutionCorralView.institutionPane.getChildren().removeAll(circle, circle.menuBar, circle.text);
	};

	{
		onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				x = getLayoutX() - event.getX();
				y = getLayoutY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});

		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				double tempX = getLayoutX();
				double tempY = getLayoutY();
				setLayoutX(x+event.getX());
				setLayoutY(y+event.getY());

				if(tempX <= InstitutionCorralView.institutionPane.getLayoutBounds().getMinX()){
					setLayoutX(InstitutionCorralView.institutionPane.getLayoutBounds().getMinX());
				}
				if(tempY <= InstitutionCorralView.institutionPane.getLayoutBounds().getMinY()){
					setLayoutY(InstitutionCorralView.institutionPane.getLayoutBounds().getMinY());
				}
				if(tempY >= InstitutionCorralView.institutionPane.getLayoutBounds().getMaxY()-getRadiusY()){
					setLayoutY(InstitutionCorralView.institutionPane.getLayoutBounds().getMaxY()-getRadiusY());
				}
				if(tempX >= InstitutionCorralView.institutionPane.getLayoutBounds().getMaxX()-getRadiusX()){
					setLayoutX(InstitutionCorralView.institutionPane.getLayoutBounds().getMaxX()-getRadiusX());
				}

				text.setLayoutX(getLayoutX()+getRadiusX()*0.2);
				text.setLayoutY(getLayoutY()+getRadiusY()*0.2);	

				menuBar.setLayoutX(getLayoutX()+getRadiusX()*0.2);
				menuBar.setLayoutY(getLayoutY()+getRadiusY()*0.2);

			}
		});
	}
}
