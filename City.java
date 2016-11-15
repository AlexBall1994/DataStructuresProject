package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;


public class City extends Point2D.Float {
	private String name;
	private String color;
	private float radius;
	private boolean isolated = false;
	
public City (String n, float x, float y, String c, float rad){
	super(x,y);
	this.name = n;
	this.color = c;
	this.radius = rad;
	
}

public String getName() {
	return name;
}

public String getColor() {
	return color;
}

public float getRadius(){
	return radius;
}

public double getX(){
	return super.x;
}

public double getY(){
	return super.y;
}

public boolean isIsolated(){
	return isolated;
}

public void isolate(){
	isolated = true;
}


}
