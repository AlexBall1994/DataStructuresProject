package cmsc420.meeshquest.part2;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

public abstract class Node {
	private Rectangle dimensions;

	public Node(int x, int y, int width, int height){
		dimensions = new Rectangle(x,y,width,height);
		
	}
	
	public abstract void insertRoad(Road r);
	public abstract Node insert(City c);
	public abstract String color();
		
	public double getMinX(){
		return dimensions.getMinX();
	}
	
	public double getMinY(){
		return dimensions.getMinY();
	}
	
	public double getMaxX(){
		return dimensions.getMaxX();
	}
	
	public double getMaxY(){
		return  dimensions.getMaxY();
	}
	
	public int getWidth(){
		return (int) dimensions.getWidth();
	}
	
	public int getHeight(){
		return (int) dimensions.getHeight();
	}
}
