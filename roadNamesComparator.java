package cmsc420.meeshquest.part2;

import java.util.Comparator;

public class roadNamesComparator implements Comparator<Road> {

	@Override
	public int compare(Road o1, Road o2) {

		if ((o1.getStart().compareTo(o2.getStart()) == 0) && (o1.getEnd().compareTo(o2.getEnd()) == 0))
			return 0;
		else if (o1.getStart().compareTo(o2.getStart()) < 0)
			return 1;
		else if (o1.getStart().compareTo(o2.getStart()) == 0){
			if (o1.getEnd().compareTo(o2.getEnd()) < 0)
				return 1;
			else return -1;
		}
		else
			return -1;
	}
}
