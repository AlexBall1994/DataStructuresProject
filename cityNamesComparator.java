package cmsc420.meeshquest.part2;

import java.util.Comparator;

public class cityNamesComparator implements Comparator<String>{

	
	public int compare(String city1, String city2){
		
		if (city1.compareTo(city2) == 0)
			return 0;
		else if (city1.compareTo(city2) < 0)
			return 1;
		else
			return -1;
	}


}
