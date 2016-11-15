package cmsc420.meeshquest.part2;
import cmsc420.drawing.CanvasPlus;


//This class represents a node in the quadtree
public class QuadTreeNode {
	public enum NodeColor {
		White,Grey,Black
		
	}
	private float minX;
	private float minY;
	private float maxX;
	private float maxY;
	private City c;
	private QuadTreeNode nw = null;
	private QuadTreeNode sw = null;
	private QuadTreeNode ne = null;
	private QuadTreeNode se = null;
	private QuadTreeNode parent;
	private NodeColor nodeColor = NodeColor.White;
	
	CanvasPlus canvas = null;
	
	public QuadTreeNode(float x, float y,float maxX, float maxY, City c){
		this.minX = x;
		this.minY = y;
		this.maxX = maxX;
		this.maxY = maxY;
		this.c = c;
		parent = null;
		
	}
	
	public void setQuadTreeNode(String direction, float x, float y, float maxX, float maxY){
		switch(direction){
		case "nw": nw = new QuadTreeNode(x,y,maxX,maxY,null);	
			break;
		case "sw": sw = new QuadTreeNode(x,y,maxX,maxY,null);
			break;
		case "ne":ne = new QuadTreeNode(x,y,maxX,maxY,null);
			break;
		default: se = new QuadTreeNode(x,y,maxX,maxY,null);
			break;
		}
	}
	
	public boolean viableQuadrant(City c, QuadTreeNode n){
		
		return (c.getX() >= n.getMinX()) && (c.getX() < n.getMaxX()) && (c.getY() >= n.getMinY()) && (c.getY() <  n.getMaxY());
	}
	
	public QuadTreeNode getQuadrantNode(String direction){
		switch(direction){
		case "nw": return nw;
		case "sw": return sw;
		case "ne": return ne;
		default: return se;
		}
	}
	
	public void clearAll(){
		nw = null;
		sw = null;
		ne = null;
		se = null;
	}
	
	public void setParent(QuadTreeNode n){
		parent = n;
	}
	
	public NodeColor getNodeColor() {
		return nodeColor;
	}
	
	public void setNodeColor(NodeColor c){
		nodeColor = c;
	}
	
	public void setCity(City c){
		this.c = c;
	}
	
	public City getCity() {
		return this.c;
	}
	
	public float getMinX(){
		return this.minX;
	}
	
	public float getMinY(){
		return this.minY;
	}
	
	public float getMaxX(){
		return this.maxX;
	}
	
	public float getMaxY(){
		return this.maxY;
	}
	
	public QuadTreeNode getParent(){
		return this.parent;
	}
}
