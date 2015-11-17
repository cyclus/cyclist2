package edu.utexas.cycic;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ConnectorLine extends Line {
	{
		setStroke(Color.BLACK);
	}
	Line right = new Line(){
		{
			setStrokeWidth(2);
			setStroke(Color.BLACK);
		}
	};
	Line left = new Line(){
		{
			setStrokeWidth(2);
			setStroke(Color.BLACK);
		}
	};
	
	Line right1 = new Line(){
		{
			setStrokeWidth(2);
			setStroke(Color.BLACK);
		}
	};
	Line left1 = new Line(){
		{
			setStrokeWidth(2);
			setStroke(Color.BLACK);
		}
	};
	Text text = new Text(){
		{
			setFill(Color.BLACK);
			setFont(new Font(20));
		}
	};
	public void updatePosition(){
		double x1 = getEndX();
		double y1 = getEndY();
		
		double x2 = getStartX();
		double y2 = getStartY();
		
		right1.setStartX(x1 + (x2-x1)*0.33);
		right1.setStartY(y1 + (y2-y1)*0.33);
		
		right1.setEndX((x1 + (x2-x1)*0.38)+5.0*(y2-y1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		right1.setEndY((y1 + (y2-y1)*0.38)-5.0*(x2-x1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		
		left1.setStartX(x1 + (x2-x1)*0.33);
		left1.setStartY(y1 + (y2-y1)*0.33);

		left1.setEndX((x1 + (x2-x1)*0.38)-5.0*(y2-y1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		left1.setEndY((y1 + (y2-y1)*0.38)+5.0*(x2-x1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		  	
		right.setStartX(x1 + (x2-x1)*0.66);
		right.setStartY(y1 + (y2-y1)*0.66);
		
		right.setEndX((x1 + (x2-x1)*0.71)+5.0*(y2-y1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		right.setEndY((y1 + (y2-y1)*0.71)-5.0*(x2-x1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		
		left.setStartX(x1 + (x2-x1)*0.66);
		left.setStartY(y1 + (y2-y1)*0.66);

		left.setEndX((x1 + (x2-x1)*0.71)-5.0*(y2-y1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		left.setEndY((y1 + (y2-y1)*0.71)+5.0*(x2-x1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		
		text.setX(x1 + (x2-x1)*0.55+10.0*(y2-y1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
		text.setY(y1 + (y2-y1)*0.55+10.0*(x2-x1)/Math.sqrt(Math.pow((y2-y1), 2)+Math.pow(x2-x1, 2)));
	}
	public void updateColor(Color color){
		this.setStroke(color);
		right1.setStroke(color);
		right.setStroke(color);
		left1.setStroke(color);
		left.setStroke(color);
		text.setFill(color);
	}
	public void hideText(){
		Cycic.pane.getChildren().remove(text);
	}
	public void showText(){
		Cycic.pane.getChildren().add(text);
	}
}
