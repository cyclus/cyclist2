package edu.utexas.cycic;

import java.util.ArrayList;

import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RegionRectangle extends Rectangle {

	Object name;
	Label text = new Label();
	ArrayList<String> regionModel = new ArrayList<String>();
	ArrayList<String> containedInstitutions = new ArrayList<String>();
	public String archetype;
	{
		setId((String)name);
	}
}
