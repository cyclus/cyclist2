package edu.utexas.cycic;

import java.util.ArrayList;

import javafx.scene.control.TextField;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light.Distant;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

/**
 * Contains all of the generic and misc visualization functions 
 * for the CYCIC fuel cycle simulator.
 * @author Robert
 *
 */
public class VisFunctions {
	
	public static TextField numberField(){
		TextField numberField = new TextField(){
			@Override public void replaceText(int start, int end, String text) {
				if (!text.matches("[a-z]")){
					super.replaceText(start, end, text);
				}
			}
			
			public void replaceSelection(String text) {
				if (!text.matches("[a-z]")){
					super.replaceSelection(text);
				}
			}
		};
		return numberField;
	}
	/**
	 * Converts a string to a color.
	 * @param string String to be converted.
	 * @return rbg triple stored in an ArrayList<Integer>
	 */
	public static ArrayList<Integer> stringToColor(String string){
		String hashCode;
		int red=0;
		int blue=0;
		int green=0;
		int p=0;
		ArrayList<Integer> rgbArray = new ArrayList<Integer>();

		hashCode = Integer.toString(Math.abs(string.hashCode()));
		if(hashCode.length()>(p+3)){
			red=Integer.parseInt(hashCode.substring(p,p+3));
			p+=3;
			if(hashCode.length()>(p+3)){
				green=Integer.parseInt(hashCode.substring(p,p+3));
				p+=3;
				if(hashCode.length()>(p+3)){
					blue=Integer.parseInt(hashCode.substring(p,p+3));
					p+=3;
				}else{
					blue=Integer.parseInt(hashCode.substring(p,hashCode.length()));
				}	
			}else{
				green=Integer.parseInt(hashCode.substring(p,hashCode.length()));
			}
		}else{
			red=Integer.parseInt(hashCode);
		}

		red = red % 256;
		green = green % 256;
		blue = blue % 256;

		rgbArray.add(red);
		rgbArray.add(green);
		rgbArray.add(blue);
		return rgbArray;
	}

	/**
	 * Test to determine which font color to use, based on the color
	 * of the node the text belongs to.
	 * @param array rbg color ArrayList<Integer>
	 * @return
	 */
	public static boolean colorTest(ArrayList<Integer> array){
		for (Integer v : array) {
			if(v < 120){
				return true;
			}
		}
		return false;
	}

	/**
	 * Test determines if the a color should be made darker or lighter
	 * based on the initial color being used.
	 * @param color rbg Integer to test.
	 * @return
	 */
	public static double colorMultiplierTest(Integer color){
		return color < 185 ? 1.3 : 0.7;
	}

	/**
	 * Adjusts the color of a node when highlighted
	 */
	static ColorAdjust colorAdjust = new ColorAdjust(){
		{
			setBrightness(0.2);
			setHue(-0.05);
		}
	};

	/**
	 * Depricated
	 */
	static Bloom bloom = new Bloom(){
		{
			setThreshold(1.0);
		}
	};

	// Really cool effect, not ready for testing yet.
	static Distant light = new Distant(){
		{
			setAzimuth(-90);
			setElevation(55);
		}
	};
	static Lighting lighting = new Lighting(){
		{
			setLight(light);
			setSurfaceScale(2);
		}
	};

	/**
	 * 
	 * @param source
	 * @param target
	 */
	static void linkFacToFac(FacilityCircle source, FacilityCircle target, String commod){
		nodeLink link = new nodeLink();
		link.source = source;
		link.target = target;
		Line line = link.line;
		link.line.text.setText(commod);
		line.setStartX(source.getCenterX());
		line.setStartY(source.getCenterY());
		line.setEndX(target.getCenterX());
		line.setEndY(target.getCenterY());
		link.line.updatePosition();
		CycicScenarios.workingCycicScenario.Links.add(link);
		Cycic.pane.getChildren().addAll(link.line, link.line.left, link.line.right);
		Cycic.pane.getChildren().addAll(link.line.left1, link.line.right1);
		Cycic.pane.getChildren().add(link.line.text);
		link.line.toBack();
	}

	/**
	 * Reloads the CYCIC pane without Markets.
	 */
	static void redrawPane(){
		Cycic.pane.getChildren().remove(0, Cycic.pane.getChildren().size());
		for(int i = 0; i < CycicScenarios.workingCycicScenario.FacilityNodes.size(); i++){
			Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle);
			Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.menu);
			Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.text);
			Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.image);
			for(int ii = 0; ii < CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.size(); ii++){
				Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(ii));
				Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(ii).menu);
				Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(ii).text);
				Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenList.get(ii).image);
			}
			for(int n = 0; n < CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenLinks.size(); n++){
				Cycic.pane.getChildren().add(CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenLinks.get(n).line);
				CycicScenarios.workingCycicScenario.FacilityNodes.get(i).cycicCircle.childrenLinks.get(n).line.toBack();
			}
		}
		CycicScenarios.workingCycicScenario.Links.clear();
		for (facilityNode outFac: CycicScenarios.workingCycicScenario.FacilityNodes) {
			for (int i = 0; i < outFac.cycicCircle.outcommods.size(); i++) {
				for (facilityNode inFac: CycicScenarios.workingCycicScenario.FacilityNodes) {
					for (int j = 0; j < inFac.cycicCircle.incommods.size(); j++) {
						if (outFac.cycicCircle.outcommods.get(i) == inFac.cycicCircle.incommods.get(j)){
							linkFacToFac(outFac.cycicCircle, inFac.cycicCircle, inFac.cycicCircle.incommods.get(j));
						}
					}
				}
			}
		}
	}
}
