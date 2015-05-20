package edu.utexas.cycic;

import java.util.ArrayList;

import edu.utah.sci.cyclist.core.event.dnd.DnD;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Ellipse;

public class InstitutionEllipse extends Ellipse{
	Object name;
	Label text = new Label();
	ArrayList<String> institModel = new ArrayList<String>();
	ArrayList<String> containedFacilities = new ArrayList<String>();
	{
		setId((String)name);
		setOnDragDetected(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				Dragboard db = startDragAndDrop(TransferMode.COPY);
				ClipboardContent content = new ClipboardContent();				
				content.put(DnD.VALUE_FORMAT, text.getText());
				db.setContent(content);
				e.consume();
			}
		});
	}
}
