package cmsc420.meeshquest.part2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.meeshquest.part2.QuadTreeNode.NodeColor;

//Used to build a quadtree
public class QuadTree {

	private QuadTreeNode root = null;
	CanvasPlus canvas = null;

	public QuadTree(float maxX, float maxY, City c, CanvasPlus canvas){
		this.root = new QuadTreeNode(0,0,maxX,maxY,c);
		this.canvas = canvas;
	}

	public City insert(City c){
		return insert(c,root);
	}
	public QuadTreeNode getRoot(){
		return this.root;
	}

	public boolean isEmpty(){
		if (this.root == null){
			return true;
		}
		else if (this.root.getNodeColor() == NodeColor.White){
			return true;
		}
		else return false;
	}


	public void print(Document results, Element result){
		ArrayList<QuadTreeNode> prQuadTree = new ArrayList<QuadTreeNode>();

		Element success = results.createElement("success");
		Element printCommand = results.createElement("command");
		Element printParams = results.createElement("parameters");
		Element output = results.createElement("output");
		Element quadTree = results.createElement("quadtree");

		printCommand.setAttribute("name", "printPRQuadtree");

		result.appendChild(success);
		success.appendChild(printCommand);
		success.appendChild(printParams);
		success.appendChild(output);
		output.appendChild(quadTree);

		prQuadTree.add(this.root);
		printResults(results, quadTree, this.root);
	}

	private QuadTreeNode printResults(Document results, Element prev, QuadTreeNode startNode){

		switch(startNode.getNodeColor()){
			case White: Element white = results.createElement("white");
			prev.appendChild(white);
			return null;
			case Black: Element black = results.createElement("black");
			black.setAttribute("name", startNode.getCity().getName());
			black.setAttribute("x", Integer.toString((int) startNode.getCity().getX()));
			black.setAttribute("y", Integer.toString((int) startNode.getCity().getY()));
			prev.appendChild(black);
			return null;
			default:
			Element gray = results.createElement("gray");
			int x = (int) ((startNode.getMaxX()+startNode.getMinX())/2);
			int y = (int) ((startNode.getMaxY()+startNode.getMinY())/2);
			gray.setAttribute("x", Integer.toString(x));
			gray.setAttribute("y", Integer.toString(y));
			prev.appendChild(gray);

			printResults(results, gray, startNode.getQuadrantNode("nw"));
			printResults(results, gray, startNode.getQuadrantNode("ne"));
			printResults(results, gray, startNode.getQuadrantNode("sw"));		
			printResults(results, gray, startNode.getQuadrantNode("se"));
			return null;
		}
	}

	public void delete(City c){
		delete(c, this.root);

	}

	private void trimTree(QuadTreeNode q){
		int countOfBlackNodes = 0;
		String node = "";

		if (q != null){

			if (q.getQuadrantNode("ne").getNodeColor() == NodeColor.Black){
				countOfBlackNodes++;
				node = "ne";
			}
			else if(q.getQuadrantNode("ne").getNodeColor() == NodeColor.Grey)
				countOfBlackNodes++;
			if (q.getQuadrantNode("nw").getNodeColor() == NodeColor.Black){
				countOfBlackNodes++;
				node = "nw";

			}
			else if(q.getQuadrantNode("nw").getNodeColor() == NodeColor.Grey)
				countOfBlackNodes++;
			if (q.getQuadrantNode("se").getNodeColor() == NodeColor.Black){
				countOfBlackNodes++;
				node = "se";

			}
			else if(q.getQuadrantNode("se").getNodeColor() == NodeColor.Grey)
				countOfBlackNodes++;
			if (q.getQuadrantNode("sw").getNodeColor() == NodeColor.Black){
				countOfBlackNodes++;
				node = "sw";

			}
			else if(q.getQuadrantNode("sw").getNodeColor() == NodeColor.Grey)
				countOfBlackNodes++;

			if (countOfBlackNodes == 1 && !node.equals("")){
				
				City c = q.getQuadrantNode(node).getCity();

				q.clearAll();
				q.setCity(c);
				q.setNodeColor(NodeColor.Black);
				if (q.getParent() != null)
					trimTree(q.getParent());		
			}
		}
	}

	//Searches and deletes the city from the tree. It then adjusts the tree after deletion
	private void delete(City c, QuadTreeNode q){

		String name = c.getName();

		if (q.getNodeColor() == NodeColor.Black){
			if (q.getCity().getName().equals(name)){
				City city = q.getCity();
				canvas.removePoint(city.getName(), city.getX(), city.getY(), Color.BLACK);
				q.setCity(null);
				q.setNodeColor(NodeColor.White);
				trimTree(q.getParent());
			}
		}
		else if (q.getNodeColor() == NodeColor.White){
			System.out.println("YOU MESSED UP");
		}
		else {
			QuadTreeNode nw = q.getQuadrantNode("nw");
			QuadTreeNode sw = q.getQuadrantNode("sw");
			QuadTreeNode ne = q.getQuadrantNode("ne");
			QuadTreeNode se = q.getQuadrantNode("se");

			if (q.viableQuadrant(c, nw)){

				nw.setParent(q);
				delete(c, nw);

			}
			else if (q.viableQuadrant(c, sw)){

				sw.setParent(q);
				delete(c,sw);

			}
			else if (q.viableQuadrant(c, ne)){

				ne.setParent(q);
				delete(c,ne);

			}
			else {
				se.setParent(q);
				delete(c,se);
			}
		}
	}


	public void rangeCities(TreeMap<String, City> cityInRangeList, City rangeCity){
		rangeCities(this.root, cityInRangeList, rangeCity);
	}

	//finds ands adds cities in range to the TreeMap
	private void rangeCities(QuadTreeNode node, TreeMap<String, City> cityInRangeList, City rangeCity){
		Utilities util = new Utilities();

		if (node.getNodeColor() == NodeColor.Black){
			City c = node.getCity();
			double distance = util.distance(rangeCity.x, rangeCity.y, c.getX(), c.getY());
			if (distance <= rangeCity.getRadius()){
				cityInRangeList.put(c.getName(), c);
			}
		}
		else if (node.getNodeColor() == NodeColor.Grey){
			rangeCities(node.getQuadrantNode("nw"), cityInRangeList, rangeCity);
			rangeCities(node.getQuadrantNode("ne"), cityInRangeList, rangeCity);
			rangeCities(node.getQuadrantNode("se"), cityInRangeList, rangeCity);
			rangeCities(node.getQuadrantNode("sw"), cityInRangeList, rangeCity);

		}
		else {

		}
	}


	//Inserts a City into the quadtree
	private City insert(City c, QuadTreeNode q){

		if (q.getNodeColor() == NodeColor.White){
			q.setCity(c);
			q.setNodeColor(NodeColor.Black);

			canvas.addPoint(c.getName(), c.getX(), c.getY(), Color.BLACK);
		}
		else if (q.getNodeColor() == NodeColor.Grey){
			QuadTreeNode nw = q.getQuadrantNode("nw");
			QuadTreeNode sw = q.getQuadrantNode("sw");
			QuadTreeNode ne = q.getQuadrantNode("ne");
			QuadTreeNode se = q.getQuadrantNode("se");

			if (q.viableQuadrant(c, nw)){
				insert(c, nw);

			}
			else if (q.viableQuadrant(c, sw)){
				insert(c,sw);

			}
			else if (q.viableQuadrant(c, ne)){
				insert(c,ne);

			}
			else {
				insert(c,se);

			}
		}
		else {
			City oldCity = q.getCity();
			q.setCity(null);
			q.setNodeColor(NodeColor.Grey);

			float xSplit = (q.getMaxX() - q.getMinX())/2;
			float ySplit = (q.getMaxY() - q.getMinY())/2;

			q.setQuadTreeNode("nw", q.getMinX(), q.getMinY() + ySplit, q.getMinX()+xSplit, q.getMaxY());
			q.setQuadTreeNode("sw", q.getMinX(), q.getMinY(), q.getMinX()+xSplit, q.getMinY()+ySplit);
			q.setQuadTreeNode("ne", q.getMinX() + xSplit, q.getMinY() + ySplit, q.getMaxX(), q.getMaxY());
			q.setQuadTreeNode("se", q.getMinX() + xSplit, q.getMinY(),q.getMaxX(),q.getMinY()+ySplit);

			canvas.addLine(q.getMinX(), q.getMinY()+ySplit, q.getMaxX(), q.getMinY()+ySplit, Color.BLACK);
			canvas.addLine(q.getMinX()+xSplit, q.getMinY(), q.getMinX()+xSplit, q.getMaxY(), Color.BLACK);

			insert(oldCity,q);		
			return insert(c, q);

		}
		return null;
	}
}
