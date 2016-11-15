package cmsc420.meeshquest.part2;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AvlGTree {
	private class TreeNode {
		City c;
		private TreeNode leftChild;
		private TreeNode rightChild;
		private int height;

		public TreeNode (City c, TreeNode leftChild, TreeNode rightChild){
			this.c = c;
			this.leftChild = leftChild;
			this.rightChild = rightChild;
			height = 0;
		}

		public TreeNode(City c){
			this(c, null, null);
		}
	}

	private TreeNode root;
	private int size;
	private final int g;

	public AvlGTree(int g){
		this.g = g;
		this.root = null;
		this.size = 0;
	}
	

	public boolean isEmpty(){
		return root == null;
	}
	
	public void print(Document results, Element parent){
		
		print(root, results, parent);
	}
	
	private void print(TreeNode t, Document results, Element parent){
		if (t == null){
			Element emptyChild = results.createElement("emptyChild");
			parent.appendChild(emptyChild);
		}
		else {
			Element node = results.createElement("node");
		
			
			node.setAttribute("key", t.c.getName());
			node.setAttribute("value", "("+Integer.toString((int) t.c.getX())+","+Integer.toString((int) t.c.getY())+")");
			parent.appendChild(node);
			print(t.rightChild, results, node);
			print(t.leftChild, results, node);
		}	
	}

	public boolean insert(City c){
		if (c == null){
			return false;
		}
		
		Stack<TreeNode> nodePath = new Stack<TreeNode>();
		TreeNode current = root;

		while (current != null) {
			nodePath.push(current);

			int cmp = c.getName().compareTo(current.c.getName());

			if (cmp < 0) {
				if (current.leftChild == null) {
					current.leftChild = new TreeNode(c);

					size++;

					rebalanceTree(nodePath, true);

					return true;
				}

				current = current.leftChild;
			} else if (cmp > 0) {
				if (current.rightChild == null) {
					current.rightChild = new TreeNode(c);

					size++;

					rebalanceTree(nodePath, true);

					return true;
				}

				current = current.rightChild;
			} else {
				return false; // Element is already stored in tree
			}
		}

		// Tree is empty:
			root = new TreeNode(c);

		size++;

		return true;
	}

	private int height(TreeNode node) {

		return (node == null) ? -1 : node.height;
	}
	
	public int getHeight(){
		return height(root)+1;
	}
	
	public int size(){
		return this.size;
	}

	private void rebalanceTree(Stack<TreeNode> nodePath, boolean isInsertion) {

		TreeNode current;

		while (!nodePath.empty()) {
			current = nodePath.pop();

			// Check for an imbalance at the current node:
			if (height(current.leftChild) - height(current.rightChild) == g+1) {
				// Compare heights of subtrees of left child node of
				// imbalanced node (check for single or double rotation
				// case):
				if (height(current.leftChild.leftChild) >= height(current.leftChild.rightChild)) {
					// Check if imbalance is internal or at the tree root:
					if (!nodePath.empty()) {
						// Compare current element with element of parent
						// node (check which child reference to update for the
						// parent node):
						if (current.c.getName().compareTo(nodePath.peek().c.getName()) < 0) {
							nodePath.peek().leftChild = rotateWithLeftChild(current);
						} else {
							nodePath.peek().rightChild = rotateWithLeftChild(current);
						}
					} else {
						root = rotateWithLeftChild(current);
					}
				} else {
					if (!nodePath.empty()) {
						if (current.c.getName().compareTo(nodePath.peek().c.getName()) < 0) {
							nodePath.peek().leftChild = doubleRotateWithLeftChild(current);
						} else {
							nodePath.peek().rightChild = doubleRotateWithLeftChild(current);
						}
					} else {
						root = doubleRotateWithLeftChild(current);
					}
				}

				current.height = Math.max(height(current.leftChild),
						height(current.rightChild)) + 1;

				if (isInsertion) {
					break;
				}
			} else if (height(current.rightChild) - height(current.leftChild) == g+1) {
				if (height(current.rightChild.rightChild) >= height(current.rightChild.leftChild)) {
					if (!nodePath.empty()) {
						if (current.c.getName().compareTo(nodePath.peek().c.getName()) < 0) {
							nodePath.peek().leftChild = rotateWithRightChild(current);
						} else {
							nodePath.peek().rightChild = rotateWithRightChild(current);
						}
					} else {
						root = rotateWithRightChild(current);
					}
				} else {
					if (!nodePath.empty()) {
						if (current.c.getName().compareTo(nodePath.peek().c.getName()) < 0) {
							nodePath.peek().leftChild = doubleRotateWithRightChild(current);
						} else {
							nodePath.peek().rightChild = doubleRotateWithRightChild(current);
						}
					} else {
						root = doubleRotateWithRightChild(current);
					}
				}

				current.height = Math.max(height(current.leftChild),
						height(current.rightChild)) + 1;

				if (isInsertion) {
					break;
				}
			} else {
				current.height = Math.max(height(current.leftChild),
						height(current.rightChild)) + 1;
			}
		}
	}

	private TreeNode rotateWithLeftChild(TreeNode sRoot) {

		TreeNode newRoot = sRoot.leftChild;

		sRoot.leftChild = newRoot.rightChild;
		newRoot.rightChild = sRoot;

		sRoot.height = Math.max(height(sRoot.leftChild),
				height(sRoot.rightChild)) + 1;
		newRoot.height = Math.max(height(newRoot.leftChild), sRoot.height) + 1;

		return newRoot;
	}

	private TreeNode rotateWithRightChild(TreeNode sRoot) {

		TreeNode newRoot = sRoot.rightChild;

		sRoot.rightChild = newRoot.leftChild;
		newRoot.leftChild = sRoot;

		sRoot.height = Math.max(height(sRoot.leftChild),
				height(sRoot.rightChild)) + 1;
		newRoot.height = Math.max(sRoot.height, height(newRoot.rightChild)) + 1;

		return newRoot;
	}

	private TreeNode doubleRotateWithLeftChild(TreeNode sRoot) {

		sRoot.leftChild = rotateWithRightChild(sRoot.leftChild);

		return rotateWithLeftChild(sRoot);
	}

	private TreeNode doubleRotateWithRightChild(TreeNode sRoot) {

		sRoot.rightChild = rotateWithLeftChild(sRoot.rightChild);

		return rotateWithRightChild(sRoot);
	}

}
