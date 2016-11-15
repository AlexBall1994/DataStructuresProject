package cmsc420.meeshquest.part2;

public class Utilities {
	
	public double distance(double cityX, double cityY, double city2X, double city2Y){
			
		double xDist = Math.pow((cityX-city2X),2.0);
		double yDist = Math.pow((cityY-city2Y), 2.0);
		
		return Math.sqrt((xDist+yDist));			
	}
}
