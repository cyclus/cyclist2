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
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;

/**
 * Contains all of the generic and misc visualization functions 
 * for the CYCIC fuel cycle simulator.
 * @author Robert
 *
 */
public class VisFunctions {

    public static void placeTextOnRectangle(RegionRectangle rect, String location) {
        
        rect.text.setWrapText(true);
        rect.text.setMouseTransparent(true);
        rect.text.setFont(new Font("ComicSans", 10));

        Double shiftX, shiftY, heightRatio, widthRatio;

        if (location.equals("bottom")) {
            shiftX = -0.2;
            shiftY = 1.2;
            heightRatio = 1.0;
            widthRatio = 1.4;
        } else if (location.equals("top")) {
            shiftX = -0.2;
            shiftY = -0.2;
            heightRatio = 1.0;
            widthRatio = 1.4;
        } else {
            shiftX = 0.1;
            shiftY = 0.1;
            heightRatio = 0.8;
            widthRatio = 0.8;
        }
        
        rect.text.setLayoutX(rect.getX()+rect.getWidth()*shiftX);
        rect.text.setLayoutY(rect.getY()+rect.getHeight()*shiftY);	
        rect.text.setMaxHeight(rect.getHeight()*heightRatio);
        rect.text.setMaxWidth(rect.getWidth()*widthRatio);
        rect.text.setTextAlignment(TextAlignment.CENTER);
        rect.text.setTextFill(Color.BLACK);

        return;
    }

    public static void placeTextOnRectangle(RegionShape rect, String location) {
        
        rect.text.setWrapText(true);
        rect.text.setMouseTransparent(true);
        rect.text.setFont(new Font("ComicSans", 10));

        Double shiftX, shiftY, heightRatio, widthRatio;

        if (location.equals("bottom")) {
            shiftX = -0.2;
            shiftY = 1.2;
            heightRatio = 1.0;
            widthRatio = 1.4;
        } else if (location.equals("top")) {
            shiftX = -0.2;
            shiftY = -0.2;
            heightRatio = 1.0;
            widthRatio = 1.4;
        } else {
            shiftX = 0.1;
            shiftY = 0.1;
            heightRatio = 0.8;
            widthRatio = 0.8;
        }
        
        rect.text.setLayoutX(rect.getX()+rect.getWidth()*shiftX);
        rect.text.setLayoutY(rect.getY()+rect.getHeight()*shiftY);	
        rect.text.setMaxHeight(rect.getHeight()*heightRatio);
        rect.text.setMaxWidth(rect.getWidth()*widthRatio);
        rect.text.setTextAlignment(TextAlignment.CENTER);
        rect.text.setTextFill(Color.BLACK);

        return;
    }

    public static void placeTextOnCircle(FacilityCircle circle, String location) {
        
        circle.text.setWrapText(true);
        circle.text.setMouseTransparent(true);
        circle.text.setFont(new Font("ComicSans", 10));

        Double shiftX, shiftY, heightRatio, widthRatio;

        if (location.equals("bottom")) {
            shiftX = -1.5;
            shiftY = 1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else if (location.equals("top")) {
            shiftX = -1.5;
            shiftY = -1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else {
            shiftX = -0.8;
            shiftY = -0.6;
            heightRatio = 1.2;
            widthRatio = -2.*shiftX;
        }
        
        circle.text.setLayoutX(circle.getCenterX()+circle.getRadius()*shiftX);
        circle.text.setLayoutY(circle.getCenterY()+circle.getRadius()*shiftY);	
        circle.text.setMaxHeight(circle.getRadius()*heightRatio);
        circle.text.setMaxWidth(circle.getRadius()*widthRatio);
        circle.text.setTextAlignment(TextAlignment.CENTER);
        circle.text.setTextFill(Color.BLACK);

        return;
    }

    public static void placeTextOnEllipse(InstitutionShape ellipse, String location) {
        
        ellipse.text.setWrapText(true);
        ellipse.text.setMouseTransparent(true);
        ellipse.text.setFont(new Font("ComicSans", 10));

        Double shiftX, shiftY, heightRatio, widthRatio;

        if (location.equals("bottom")) {
            shiftX = -1.5;
            shiftY = 1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else if (location.equals("top")) {
            shiftX = -1.5;
            shiftY = -1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else {
            shiftX = -1.0;
            shiftY = -0.6;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        }
        
        ellipse.text.setLayoutX(ellipse.getLayoutX()+ellipse.getRadiusX()*shiftX);
        ellipse.text.setLayoutY(ellipse.getLayoutY()+ellipse.getRadiusY()*shiftY);	
        ellipse.text.setMaxHeight(ellipse.getRadiusY()*heightRatio);
        ellipse.text.setMaxWidth(ellipse.getRadiusX()*widthRatio);
        ellipse.text.setTextAlignment(TextAlignment.CENTER);
        ellipse.text.setTextFill(Color.BLACK);

        return;
    }

	
    public static void placeTextOnEllipse(InstitutionEllipse ellipse, String location) {
        
        ellipse.text.setWrapText(true);
        ellipse.text.setMouseTransparent(true);
        ellipse.text.setFont(new Font("ComicSans", 10));

        Double shiftX, shiftY, heightRatio, widthRatio;

        if (location.equals("bottom")) {
            shiftX = -1.5;
            shiftY = 1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else if (location.equals("top")) {
            shiftX = -1.5;
            shiftY = -1.2;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        } else {
            shiftX = -1.0;
            shiftY = -0.6;
            heightRatio = 1.2;
            widthRatio = -2*shiftX;
        }
        
        ellipse.text.setLayoutX(ellipse.getLayoutX()+ellipse.getRadiusX()*shiftX);
        ellipse.text.setLayoutY(ellipse.getLayoutY()+ellipse.getRadiusY()*shiftY);	
        ellipse.text.setMaxHeight(ellipse.getRadiusY()*heightRatio);
        ellipse.text.setMaxWidth(ellipse.getRadiusX()*widthRatio);
        ellipse.text.setTextAlignment(TextAlignment.CENTER);
        ellipse.text.setTextFill(Color.BLACK);

        return;
    }

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

    public static Color pastelize(Color color) {

        double h = color.getHue();
        double s = color.getSaturation();
        double b = color.getBrightness();

        s = s * (0.4f);
        b = 1.0;

        return Color.hsb(h,s,b);
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
