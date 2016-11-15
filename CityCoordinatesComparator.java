package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.Comparator;



public class CityCoordinatesComparator implements Comparator <Point2D.Float> {

	@Override
	public int compare(Point2D.Float o1, Point2D.Float o2) {
		if(o1.getY() > o2.getY())
			return 1;
		else if (o1.getY() < o2.getY())
			return -1;
		else if (o1.getX() > o2.getX())
			return 1;
		else if (o1.getX() < o2.getX())
			return -1;
		else return 0;
		
	}





}
