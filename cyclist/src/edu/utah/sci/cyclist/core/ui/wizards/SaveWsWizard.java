package edu.utah.sci.cyclist.core.ui.wizards;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import edu.utah.sci.cyclist.Cyclist;

public class SaveWsWizard extends VBox {
	
	// GUI elements
	private Stage                 _dialog;
	private ObjectProperty<Boolean> _selection = new SimpleObjectProperty<>();
	// * * * Constructor creates a new stage * * * //
	public SaveWsWizard() {	
		createDialog();
	}

	// * * * Show the dialog * * * //
	public ObjectProperty<Boolean> show(Window window) {
		_dialog.initOwner	(window);
		_dialog.show();
		_dialog.setX(window.getX() + (window.getWidth() - _dialog.getWidth())*0.5);
		_dialog.setY(window.getY() + (window.getHeight() - _dialog.getHeight())*0.5);
		return _selection;
	}
		
	// * * * Create the dialog
	private void createDialog(){	

		_dialog = new Stage();
		_dialog.setTitle("Save WorkSpace");
		_dialog.setHeight(100);
		
		
		_dialog.initModality(Modality.WINDOW_MODAL);
		_dialog.setScene( createScene(_dialog) );
	}

		// * * * Create scene creates the GUI * * * //
		private Scene createScene(final Stage dialog) {

			VBox vbox = new VBox();
			vbox.setSpacing(10);
			vbox.setAlignment(Pos.CENTER);
			
			Text text = new Text("Workspace has been modified. Save changed? ");  
			text.setFont(new Font(12));
			
			HBox hbox = new HBox();
			hbox.setAlignment(Pos.BOTTOM_CENTER);
			hbox.setSpacing(10);
			hbox.setPadding(new Insets(5));
			hbox.setMinWidth(300);
			
			Button yes = new Button("Yes");
//			yes.setDefaultButton(true);
			yes.setMinWidth(50);
			yes.setAlignment(Pos.CENTER);
			Button no = new Button("No");
			no.setMinWidth(50);
			no.setAlignment(Pos.CENTER);
			Button cancel = new Button("Cancel");
			cancel.setCancelButton(true);
			cancel.setMinWidth(50);
			cancel.setAlignment(Pos.CENTER);
			hbox.getChildren().addAll(yes,no,cancel);
			cancel.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					_dialog.hide();
				};
			});
			cancel.setOnKeyPressed((ke)-> { if (ke.getCode() == KeyCode.ENTER) { cancel.fire(); } } );

			yes.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					_selection.set(true);
					_dialog.hide();
				};
			});
			yes.setOnKeyPressed((ke)-> {if (ke.getCode() == KeyCode.ENTER) yes.fire(); } );
			
			no.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					_selection.set(false);
					_dialog.hide();
				};
			});
			no.setOnKeyPressed((ke)-> { if (ke.getCode() == KeyCode.ENTER) no.fire(); } );
			
			vbox.getChildren().addAll(text,hbox);
			
			// Create the scene
			Scene scene = new Scene(vbox);
			
			scene.getStylesheets().add(Cyclist.class.getResource("assets/Cyclist.css").toExternalForm());
			return scene;
		}	
}