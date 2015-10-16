package edu.utexas.cycic;

import java.io.File;

import edu.utah.sci.cyclist.Cyclist;
import edu.utah.sci.cyclist.core.Resources1;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class CyclusHelp {

	
	/**
	 * 
	 * @param help
	 */
    public static void showHelpDialog() {
    	VBox viewBox = new VBox();
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Regions");
        Label text = new Label("This is designed to give you a brief overview of the Cyclus fuel cycle" +
        		" simulator system. Cyclus uses an agent based approach for modeling specific parts of " +
        		"the nuclear fuel cycle. The three main types of agents are: REGIONS, INSTITUTIONS and FACILITIES." +
        		" Facilities may trade RESOURCES to each other through the use of COMMODITIES. Each of these topics" +
        		" will be covered in this guide. You may advance to the next topic using the 'Next' button " +
        		"or return to a previous topic using the 'Previous' button.");
        text.setWrapText(true);
        Hyperlink link = new Hyperlink("Cyclus");
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        link.setOnAction(new EventHandler<ActionEvent>(){
        	public void handle(ActionEvent e){
        			//webEngine.load("fuelcycle.org");
        	}
        });

        viewBox.getChildren().addAll(text);
        viewBox.setSpacing(10);
        viewBox.setPrefWidth(200);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogCyclus();
            }
        });
    }
 
    public static void helpDialogCyclus() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Cyclus");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/CyclusOverview.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogRegions();
            } else if (response == ButtonType.PREVIOUS){
            	showHelpDialog();
            }
        });
    }
    
    public static void helpDialogRegions() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Regions");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/Regions.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogInstitutions();
            } else if (response == ButtonType.PREVIOUS){
            	helpDialogCyclus();
            }
        });
    }
    
    public static void helpDialogInstitutions() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Institutions");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/Institutions.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogFacilities();
            } else if (response == ButtonType.PREVIOUS){
            	helpDialogRegions();
            }
        });
    }
    
    public static void helpDialogFacilities() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Facilities");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/Facilities.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogResources();
            } else if (response == ButtonType.PREVIOUS){
            	helpDialogInstitutions();
            }
        });
    }
    
    public static void helpDialogResources() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Resources");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/Resources.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.NEXT, ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.NEXT){
            	helpDialogCommodities();
            } else if (response == ButtonType.PREVIOUS){
            	helpDialogFacilities();
            }
        });
    }
    
    public static void helpDialogCommodities() {
        Dialog<?> dg = new Dialog();
        dg.setResizable(true);
        dg.setTitle("Commodities");
    	VBox viewBox = new VBox();
        Image igm = new Image(Cyclist.class.getResource("assets/HelpImages/Commodities.png").toExternalForm());
        ImageView igmView = new ImageView(igm);
        viewBox.getChildren().addAll(igmView);
        dg.getDialogPane().setContent(viewBox);
        dg.getDialogPane().getButtonTypes().addAll(ButtonType.PREVIOUS, ButtonType.OK);
        dg.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
            	dg.close();
            } else if (response == ButtonType.PREVIOUS){
            	helpDialogResources();
            }
        });
    }
    
   
    
    /**
     * 
     * @param help
     * @return
     */
    public static EventHandler<ActionEvent> helpDialogHandler() {
        return new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e){
        		showHelpDialog();
        	}
        };
    }
}
