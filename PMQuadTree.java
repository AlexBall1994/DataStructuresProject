package cmsc420.meeshquest.part2;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PMQuadTree {
	private final int order;
	private Line2D lines[];
	private Node root = null;
	private final int width;
	private final int height;

	public PMQuadTree(int order, double width, double height){
		this.order = order;
		this.root = new WhiteNode(0,0, (int)width,(int) height);
		this.width = (int) width;
		this.height = (int)height;

	}

	private class WhiteNode extends Node {

		public WhiteNode(int x, int y, int width, int height) {
			super(x, y, width, height);

			// TODO Auto-generated constructor stub
		}

		public String color(){
			return "white";
		}

		@Override
		public Node insert(City c) {
			// TODO Auto-generated method stub

			return new BlackNode((int)super.getMinX(),(int) super.getMinY(), super.getWidth(), super.getHeight(), c);
		}

		@Override
		public void insertRoad(Road r) {
			// TODO Auto-generated method stub

		}
	}

	private class BlackNode extends Node {
		private City c;
		private ArrayList<Road> roadList = new ArrayList<Road>();

		public BlackNode(int x, int y, int width, int height, City c) {
			super(x, y, width, height);
			this.c = c;
		}

		public Node insert(City city) {
			if (this.c == null){
				this.c = city;
				return this;
			}
			else {
				GreyNode g = new GreyNode((int)super.getMinX(), (int) super.getMinY(), super.getWidth(), super.getHeight());
				g.insert(this.c);
				g.insert(city);

				return g;
			}
		}

		public int cardinality(){
			int city = c == null ? 0 : 1;
			return city + roadList.size();
		}

		public ArrayList<Road> getRoads(){
			return this.roadList;
		}

		public City getCity(){
			return this.c;
		}

		public String color(){
			return "black";
		}

		@Override
		public void insertRoad(Road r) {
			if (!roadList.contains(r))
				roadList.add(r);
		}
	}

	public Node insert(City c){
		if (this.root == null)
			root = new WhiteNode(0,0, (int)width,(int) height);
		return this.root = this.root.insert(c);
	}

	private class GreyNode extends Node {
		Node[] children = new Node[4];
		Rectangle[] regions = new Rectangle[4];
		public GreyNode(int x, int y, int width, int height) {
			super(x, y, width, height);

			children[0] = new WhiteNode(x,y+height/2,width/2,height/2);
			children[1] = new WhiteNode(x,y,width/2,height/2);
			children[2] = new WhiteNode(x+width/2,y+height/2,width/2,height/2);
			children[3] = new WhiteNode(x+width/2,y, width/2, height/2);

			regions[0] = new Rectangle(x,y+height/2,width/2,height/2);
			regions[1] = new Rectangle(x,y,width/2,height/2);
			regions[2] = new Rectangle(x+width/2,y+height/2,width/2,height/2);
			regions[3] = new Rectangle(x+width/2,y, width/2, height/2);
		}

		public Node insert(City c) {

			for(int i = 0; i < 4; i++){

				if(c.getX() >= children[i].getMinX() && c.getX() <= children[i].getMaxX() && c.getY() >= children[i].getMinY() && c.getY() <= children[i].getMaxY()){
					children[i] = children[i].insert(c);

				}
			}
			return this;
		}	

		public Node getNode(String dir){
			if (dir == "nw"){
				return children[0];
			}
			else if (dir == "sw"){
				return children[1];
			}
			else if (dir == "ne"){
				return children[2];
			}
			else {
				return children[3];
			}
		}

		public String color(){
			return "grey";
		}

		@Override
		public void insertRoad(Road r) {
			for(int i = 0; i < 4; i++){
				if (regions[i].intersectsLine(r.getLine())){
					if (children[i].color().equals("white")){
						children[i] = children[i].insert(null);
					}

					children[i].insertRoad(r);
				}
			}
		}
	}

	public void insertRoad(Road r){

		this.root.insertRoad(r);
	}

	public void print(Document results, Element result){
		Element success = results.createElement("success");
		Element printCommand = results.createElement("command");
		Element printParams = results.createElement("parameters");
		Element output = results.createElement("output");
		Element quadTree = results.createElement("quadtree");

		printCommand.setAttribute("name", "printPMQuadtree");
		quadTree.setAttribute("order", Integer.toString(this.order));

		result.appendChild(success);
		success.appendChild(printCommand);
		success.appendChild(printParams);
		success.appendChild(output);
		output.appendChild(quadTree);

		printResults(results, quadTree, this.root);
	}

	private QuadTreeNode printResults(Document results, Element prev, Node startNode){

		if (startNode != null){
			String color = startNode.color();

			if (color.equals("white")){
				Element white = results.createElement("white");
				prev.appendChild(white);

			}
			else if (color.equals("black")){
				Element black = results.createElement("black");
				BlackNode b = (BlackNode) startNode;
				ArrayList<Road> roadList = b.getRoads();
				black.setAttribute("cardinality", Integer.toString(b.cardinality()));

				if (b.getCity() == null){

				}
				else if (b.getCity().isIsolated()){
					Element isolatedCity = results.createElement("isolatedCity");
					isolatedCity.setAttribute("name", b.getCity().getName());
					isolatedCity.setAttribute("x", Integer.toString((int) b.getCity().getX()));
					isolatedCity.setAttribute("y", Integer.toString((int) b.getCity().getY()));
					isolatedCity.setAttribute("color", b.getCity().getColor());
					isolatedCity.setAttribute("radius", Integer.toString((int) b.getCity().getRadius()));

					black.appendChild(isolatedCity);
				}
				else	{ 

					Element city = results.createElement("city");
					city.setAttribute("name", b.getCity().getName());
					city.setAttribute("x", Integer.toString((int) b.getCity().getX()));
					city.setAttribute("y", Integer.toString((int) b.getCity().getY()));
					city.setAttribute("color", b.getCity().getColor());
					city.setAttribute("radius", Integer.toString((int) b.getCity().getRadius()));

					black.appendChild(city);
				}

				if (!roadList.isEmpty()){
					int bestI = 0;
					int i = 0;
					ArrayList<Road> temp = roadList;

					while(!temp.isEmpty()){
						if(roadList.get(bestI).getStart().compareTo(roadList.get(i).getStart()) < 0){
							bestI = i;
						}
						else if(roadList.get(bestI).getStart().compareTo(roadList.get(i).getStart()) == 0){
							if (roadList.get(bestI).getEnd().compareTo(roadList.get(i).getEnd()) < 0)
								bestI = i;
						}
						i++;
						if(i == roadList.size()){
							
							Element road = results.createElement("road");
							Road r = roadList.get(bestI);

							road.setAttribute("start", r.getStart());
							road.setAttribute("end", r.getEnd());
							
							black.appendChild(road);
							temp.remove(bestI);
							i = 0;
							bestI = 0;
							
						}
					}	
				}

				prev.appendChild(black);
			}
			else {
				Element gray = results.createElement("gray");
				int x = (int) ((startNode.getMaxX()+startNode.getMinX())/2);
				int y = (int) ((startNode.getMaxY()+startNode.getMinY())/2);
				gray.setAttribute("x", Integer.toString(x));
				gray.setAttribute("y", Integer.toString(y));
				prev.appendChild(gray);

				GreyNode g = (GreyNode) startNode;

				printResults(results, gray, g.getNode("nw"));
				printResults(results, gray, g.getNode("ne"));
				printResults(results, gray, g.getNode("sw"));		
				printResults(results, gray, g.getNode("se"));
			}
		}

		return null;
	}

	public boolean isEmpty(){
		return root.color().equals("white");
	}
}
