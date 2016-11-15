package cmsc420.meeshquest.part2;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Hashtable;

//Class to represent roads
public class Road {
	private final String start;
	private final String end;
	private final Line2D.Double line;

	public Road(String start, String end, Line2D.Double line){
		this.start = start;
		this.end = end;
		this.line = line;
	}

	public Line2D.Double getLine(){
		return this.line;
	}

	public String getStart(){
		return this.start;
	}

	public String getEnd(){
		return this.end;
	}

	public boolean alreadyMapped(ArrayList<Road> rList, Road r){
		for(int i = 0; i < rList.size(); i++){
			if(r.getStart().equals(rList.get(i).getStart()) && r.getEnd().equals(rList.get(i).getEnd())){
				return true;
			}

		}
		return false;
	}

	//Dijkstra's by yours truly
	public ArrayList<Road> Dijkstra(ArrayList<String> citiesMapped, ArrayList<Road> roadsMapped, String startCity, String endCity){

		Hashtable<String,Hashtable<String,Double>> table = new Hashtable<String,Hashtable<String,Double>>();
		ArrayList<String> unprocessed = new ArrayList<String>();
		Hashtable<String, String> prev = new Hashtable<String,String>();
		Hashtable<String, Double> dist = new Hashtable<String, Double>();

		for(int i = 0; i < citiesMapped.size(); i++){
			unprocessed.add(citiesMapped.get(i));
		}

		for(int i = 0; i < citiesMapped.size(); i++){
			dist.put(citiesMapped.get(i), Double.MAX_VALUE);
			table.put(citiesMapped.get(i), new Hashtable<String,Double>());

			for(int j = 0; j < citiesMapped.size(); j++){
				table.get(citiesMapped.get(i)).put(citiesMapped.get(j), Double.MAX_VALUE);

			}
		}

		dist.put(startCity,  0.0);
		String current = startCity;

		while(!unprocessed.isEmpty()){
			unprocessed.remove(current);

			for(int i = 0; i < roadsMapped.size(); i++){
				String end = "";
				if (roadsMapped.get(i).getStart().equals(current)){
					end = roadsMapped.get(i).getEnd();
					Point2D p1 = roadsMapped.get(i).getLine().getP1();
					Point2D p2 = roadsMapped.get(i).getLine().getP2();
					double alt = p1.distance(p2)+dist.get(current);

					if(alt < dist.get(end)){

						dist.put(end, alt);
						prev.put(end, current);
					}		
				}
				else if (roadsMapped.get(i).getEnd().equals(current)){
					end = roadsMapped.get(i).getStart();
					Point2D p1 = roadsMapped.get(i).getLine().getP1();
					Point2D p2 = roadsMapped.get(i).getLine().getP2();
					double alt = p1.distance(p2)+dist.get(current);

					if(alt < dist.get(end)){
						dist.put(end, alt);
						prev.put(end, current);
					}
				}			
			}

			double best = Double.MAX_VALUE;
			if (unprocessed.size() > 0)current = unprocessed.get(0);
			for(int i = 0; i < unprocessed.size(); i++){
				if(dist.get(unprocessed.get(i)) < best){
					current = unprocessed.get(i);
					best = dist.get(unprocessed.get(i));
				}				
			}
		}

		ArrayList<Road> path = new ArrayList<Road>();
		String cur = endCity;
		String next = "";

		if(!prev.containsKey(endCity) && !endCity.equals(startCity)){
			return null;
		}
		while(!cur.equals(startCity)){
			next = prev.get(cur);

			for(int i = 0; i < roadsMapped.size(); i++){
				if(roadsMapped.get(i).getStart().equals(cur) && roadsMapped.get(i).getEnd().equals(next)){
					path.add(roadsMapped.get(i));
				}
				else if (roadsMapped.get(i).getStart().equals(next) && roadsMapped.get(i).getEnd().equals(cur)){
					path.add(roadsMapped.get(i));
				}
				
			}
			cur = prev.get(cur);
		}

		return path;
	}
}