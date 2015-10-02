/*******************************************************************************
 * Copyright (c) 2013 SCI Institute, University of Utah.
 * All rights reserved.
 *
 * License for the specific language governing rights and limitations under Permission
 * is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: The above copyright notice 
 * and this permission notice shall be included in all copies  or substantial portions of the Software. 
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR 
 *  A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Yarden Livnat  
 *******************************************************************************/
package edu.utah.sci.cyclist;

import org.apache.log4j.Logger;

import edu.utah.sci.cyclist.core.controller.CyclistController;
import edu.utah.sci.cyclist.core.event.dnd.DnDIcon;
import edu.utah.sci.cyclist.core.event.notification.EventBus;
import edu.utah.sci.cyclist.core.event.notification.SimpleEventBus;
import edu.utah.sci.cyclist.core.ui.MainScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Cyclist extends Application {
	public static final String TITLE = "Cyclist";
    static Logger log = Logger.getLogger(Cyclist.class);

    public static Stage cyclistStage;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
	@Override
	public void start(Stage primaryStage) throws Exception {
		Cyclist.cyclistStage = primaryStage;
		
		StackPane stack = new StackPane();
		
		Pane glass = new Pane();
		glass.setStyle("-fx-background-color: rgba(0, 100, 100, 0)");
		glass.setMouseTransparent(true);
		
		Scene scene = new Scene(stack, 800, 600);
		
		primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(Cyclist.class.getResourceAsStream("assets/cyclist.png")));
        
		MainScreen root = new MainScreen(primaryStage);
        
	 	stack.getChildren().addAll(root, glass);
	 	
	 	DnDIcon.getInstance().setGlass(glass);
	 	
		EventBus eventBus = new SimpleEventBus();
		CyclistController controller = new CyclistController(eventBus);
		controller.setScreen(root);
        
        scene.getStylesheets().add(Cyclist.class.getResource("assets/Cyclist.css").toExternalForm());
        scene.getStylesheets().add(Cyclist.class.getResource("assets/Views.css").toExternalForm());
       // scene.getStylesheets().add(Cyclist.class.getResource("assets/Wizards.css").toExternalForm());
      
        primaryStage.show();
	}

}
