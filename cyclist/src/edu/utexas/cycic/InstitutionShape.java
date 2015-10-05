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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
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
                    Integer numFac = 1;
                    if (institBackTrace.availFacilities.containsKey(facName)) {
                            numFac = numFac + institBackTrace.availFacilities.get(facName);
                    }
                    institBackTrace.availFacilities.put(facName, numFac);
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
	ContextMenu menu;
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
		
		// Set circle color
		institution.rgbColor=VisFunctions.stringToColor(instit.type);
		institution.setFill(VisFunctions.pastelize(Color.rgb(institution.rgbColor.get(0), institution.rgbColor.get(1), institution.rgbColor.get(2), 0.8)));

		VisFunctions.placeTextOnEllipse(institution,"middle");
		
		institution.setEffect(VisFunctions.lighting);

		//Adding the circle's menu and its functions.

		MenuItem regionForm = new MenuItem("Configure");
		regionForm.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				CyclistController._presenter.addTool(new InstitutionViewTool());
			}
		});

        MenuItem helpDialog = new MenuItem("Institution Documentation");
        helpDialog.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e){
                FormBuilderFunctions.showHelpDialog(instit.doc);
            }
        });
            

		EventHandler<ActionEvent> deleteEvent = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent deleteEvent) {
				deleteInstitution(institution, instit);
			}
		};
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(deleteEvent);

		institution.menu = new ContextMenu();
		institution.menu.getItems().addAll(regionForm, helpDialog, delete);		


		institution.onMouseClickedProperty().set(new EventHandler <MouseEvent>(){
			@Override
			public void handle(MouseEvent menuEvent){
				if(menuEvent.getButton().equals(MouseButton.SECONDARY)){
					institution.menu.show(institution,menuEvent.getScreenX(),menuEvent.getScreenY());
					menuEvent.consume();
				}
				
				if(menuEvent.getClickCount() == 2){
                    menuEvent.consume();
					CyclistController._presenter.addTool(new InstitutionViewTool());
				}
				for(int i = 0; i < InstitutionCorralView.institutionPane.getChildren().size(); i++){
					if(InstitutionCorralView.institutionPane.getChildren().get(i).getId() == "this"){
						((Shape) InstitutionCorralView.institutionPane.getChildren().get(i)).setStroke(Color.BLACK);
						((Shape) InstitutionCorralView.institutionPane.getChildren().get(i)).setStrokeWidth(1);
					}
				}
				InstitutionCorralView.workingInstitution = instit;
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
		

		// To allow the facilityCircle to be moved through the pane and setting bounding regions.
		institution.onMouseDraggedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				//double tempX = institution.getCenterX();
				//double tempY = institution.getCenterY();
				institution.setCenterX(x+mousex);
				institution.setCenterY(y+mousey);

				if(institution.getCenterX() <= InstitutionCorralView.institutionPane.getLayoutBounds().getMinX()+institution.getRadiusX()*0.5){
					institution.setCenterX(InstitutionCorralView.institutionPane.getLayoutBounds().getMinX()+institution.getRadiusX()*0.5);
				}
				if(institution.getCenterY() <= InstitutionCorralView.institutionPane.getLayoutBounds().getMinY()-institution.getRadiusY()*0.2){
					institution.setCenterY(InstitutionCorralView.institutionPane.getLayoutBounds().getMinY()-institution.getRadiusY()*0.2);
				}
				if(institution.getCenterY() >= InstitutionCorralView.institutionPane.getLayoutBounds().getMaxY()-institution.getRadiusY()*2.3){
					institution.setCenterY(InstitutionCorralView.institutionPane.getLayoutBounds().getMaxY()-institution.getRadiusY()*2.3);
				}
				if(institution.getCenterX() >= InstitutionCorralView.institutionPane.getLayoutBounds().getMaxX()-institution.getRadiusX()*1.8){
					institution.setCenterX(InstitutionCorralView.institutionPane.getLayoutBounds().getMaxX()-institution.getRadiusX()*1.8);
				}
                
				VisFunctions.placeTextOnEllipse(institution,"middle");

				mousex = event.getX();
				mousey = event.getY();
			}
		});


		return institution;	
        
        

	};

	static void deleteInstitution(InstitutionShape circle, instituteNode instit){
		DataArrays.institNodes.remove(instit);
		InstitutionCorralView.institutionPane.getChildren().removeAll(circle, circle.text);
	};

	{
		onMousePressedProperty().set(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent event){
				x = getCenterX() - event.getX();
				y = getCenterY() - event.getY();
				mousex = event.getX();
				mousey = event.getY();
			}
		});

	}
}
