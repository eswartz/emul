/**
 * 
 */
package v9t9.emulator.clients.builtin.video.image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Octree color quantization algorithm, after
 * "A Simple Method for Color Quantization: Octree Quantization." (M. Gervautz
 * and W. Purgathofer of Austria's Technische UniversitÅt Wien) as described in
 * &lt;http://www.microsoft.com/msj/archive/S3F1.aspx&gt;
 * 
 * Uses Gervautz's & Purgathofer's recommendations for reducing the tree
 * when the maximum node count is reached 
 * (as in &lt;http://web.cs.wpi.edu/~matt/courses/cs563/talks/color_quant/CQindex.html&gt;)
 * 
 * @author ejs
 * 
 */
public class ColorOctree {
	static class Node {
		final InnerNode parent;
		public Node(InnerNode parent) {
			this.parent = parent;
		}
	}
	static class LeafNode extends Node {
		int pixelCount;
		int reds, greens, blues;
		public LeafNode(InnerNode parent) {
			super(parent);
		}
		@Override
		public String toString() {
			return "Leaf: #" + pixelCount + "; r="+(reds/pixelCount)+ ";g=" + (greens/pixelCount) + ";b="+(blues/pixelCount);
		}
		public int[] reprRGB() {
			return new int[] { 
				(reds / pixelCount),
				(greens / pixelCount),
				(blues / pixelCount) 
			};
		}
		public void add(int[] prgb) {
			reds += prgb[0];
			greens += prgb[1];
			blues += prgb[2];
			pixelCount++;
			
			Node n = parent;
			while (n instanceof InnerNode) {
				((InnerNode) n).pixelCount++;
				n = n.parent;
			}
		}
		public void add(LeafNode node) {
			pixelCount += node.pixelCount;
			reds += node.reds;
			greens += node.greens;
			blues += node.blues;
		}
		/**
		 * @return
		 */
		public int getPixelCount() {
			return pixelCount;
		}
	}

	static class InnerNode extends Node {
		Node[] kids;
		int pixelCount;
		public InnerNode(InnerNode parent) {
			super(parent);
			kids = new Node[8];
		}
		@Override
		public String toString() {
			return "Inner Node: #" + pixelCount;
		}
		public int depth() {
			Node n = this;
			int depth = 0;
			while (n.parent != null) {
				depth++;
				n = n.parent;
			}
			return depth;
		}
	}

	private final InnerNode root;
	private final int maxLeafCount;
	private int leafCount;
	private final int maxDepth;

	private LinkedList<InnerNode>[] reducibleLists;
	private Comparator<InnerNode> comparator;
	private final boolean usingHSV;
	private int minRed;
	private int minGreen;
	private int minBlue;
	private int maxRed;
	private int maxGreen;
	private int maxBlue;

	@SuppressWarnings("unchecked")
	public ColorOctree(int maxDepth, int maxLeafCount, boolean removeDetail, boolean usingHSV) {
		if (maxDepth < 2 || maxLeafCount < 1)
			throw new IllegalArgumentException();
		
		this.maxDepth = maxDepth;
		this.maxLeafCount = maxLeafCount;
		root = new InnerNode(null);
		comparator = (removeDetail ?
				createLeastUsedFirstComparator() : createMostUsedFirstComparator());
		this.usingHSV = usingHSV;
		
		reducibleLists = new LinkedList[maxDepth];
		for (int i = 0; i < maxDepth; i++)
			reducibleLists[i] = new LinkedList<ColorOctree.InnerNode>();
		
		minRed = minGreen = minBlue = Integer.MAX_VALUE;
		maxRed = maxGreen = maxBlue = Integer.MIN_VALUE;
	}

	private Comparator<InnerNode> createMostUsedFirstComparator() {
		return new Comparator<InnerNode>() {
			@Override
			public int compare(InnerNode o1, InnerNode o2) {
				return o2.pixelCount - o1.pixelCount;
			}
		};
	}
	private Comparator<InnerNode> createLeastUsedFirstComparator() {
		return new Comparator<InnerNode>() {
			@Override
			public int compare(InnerNode o1, InnerNode o2) {
				return o1.pixelCount - o2.pixelCount;
			}
		};
	}

	public void addColor(int[] prgb) {
		Node traverse = root;
		int depth = 0;
		while (!(traverse instanceof LeafNode)) {
			traverse = addToTreeLevel((InnerNode) traverse, depth, prgb);
			depth++;
		}
		
		if (prgb[0] < minRed && prgb[1] < minGreen && prgb[2] < minBlue) {
			minRed = prgb[0];
			minGreen = prgb[1];
			minBlue = prgb[2];
		}
		if (prgb[0] > maxRed && prgb[1] > maxGreen && prgb[2] > maxBlue) {
			maxRed = prgb[0];
			maxGreen = prgb[1];
			maxBlue = prgb[2];
		}
		// don't reduce serially; loses detail at bottom of image
		//if (leafCount > maxLeafCount * maxLeafCount)
		//reduceTree();
	}

	/** Get the octree child index from the given bit of the RGB index */
	private static int getIndex(int[] prgb, int depth) {
		int mask = 0x80 >> depth;
		return ((prgb[0] & mask) != 0 ? 4 : 0)
				| ((prgb[1] & mask) != 0 ? 2 : 0) 
				| ((prgb[2] & mask) != 0 ? 1 : 0);
	}

	/**
	 * Add a color to the tree at the given depth.  This may either
	 * traverse or add an inner node along a path, returning an inner node,
	 * or if may add the color to a leaf, or it may return null if the
	 * tree is full.
	 * @param traverse
	 * @param depth
	 * @param index
	 * @param prgb
	 * @return InnerNode if color is added to a path, LeafNode if color
	 * was entered into a leaf, or root if the tree must be reduced.
	 */
	private Node addToTreeLevel(InnerNode traverse, int depth, int[] prgb) {
		int index = getIndex(prgb, depth);
		Node kid = traverse.kids[index];
		if (kid == null) {
			if (depth < maxDepth - 1) {
				kid = new InnerNode(traverse);
				traverse.kids[index] = kid;
				reducibleLists[depth].add((InnerNode) kid);
			} else {
				LeafNode leaf = new LeafNode(traverse);
				leaf.add(prgb);
				leafCount++;
				traverse.kids[index]= leaf;
				kid = leaf;
			}
		}
		else if (kid instanceof LeafNode) {
			LeafNode leaf = (LeafNode) kid;
			leaf.add(prgb);
		} else {
			// is inner node
		}
		return kid;
	}
	
	/**
	 * Reduce the tree by merging candidate representative colors.
	 * When dithering is desired, most-often-used colors are removed
	 * (since their error can be corrected later).  When dithering is
	 * not desired, least-often-used colors are removed (to allow for
	 * smoother color selection).
	 */
	public void reduceTree() {
		while (leafCount > maxLeafCount) {
			LinkedList<InnerNode> list = null;
			int depth;
			for (depth = maxDepth - 1; depth >= 0; depth--) {
				if (!(list = reducibleLists[depth]).isEmpty())
					break;
			}
			if (list == null || depth < 0) {
				// the root must have only LeafNodes -- remove one
				collapseRoot();
				continue;
			}
			
			while (leafCount > maxLeafCount &&  !list.isEmpty()) {
				TreeSet<InnerNode> sorted = new TreeSet<InnerNode>(comparator);
				sorted.addAll(list);
				
				Iterator<InnerNode> iter = sorted.iterator();
				InnerNode candidate = iter.next();
				
				mergeInnerNode(depth, candidate);
				
				iter.remove();
				//if (leafCount != gatherLeaves().size())
				//	throw new IllegalStateException();
			}
		}
	}

	private void collapseRoot() {
		int leafIndex = getMinRootLeaf();
		leafCount--;
		
		LeafNode leaf = (LeafNode) root.kids[leafIndex];
		root.kids[leafIndex] = null;
		
		// distribute pixels to closest color
		LeafNode minNeighbor = null;
		
		int[] prgb = leaf.reprRGB();
		int bit;
		if ((prgb[0] & 0x80) != 0) {
			bit = 4;
		} else if ((prgb[1] & 0x80) != 0) {
			bit = 2;
		} else {
			bit = 1;
		}
		
		if ((root.kids[leafIndex ^ bit]) instanceof LeafNode) {
			minNeighbor = (LeafNode) (root.kids[leafIndex ^ bit]);
		} else {
			// just find one with least representation
			for (int idx = 0; idx < 8; idx++) {
				if (root.kids[idx] instanceof LeafNode) {
					LeafNode neighbor = (LeafNode) root.kids[idx];
			
					if (minNeighbor == null || neighbor.pixelCount < minNeighbor.pixelCount) {
						minNeighbor = neighbor;
					}
				}
			}
		}
		if (minNeighbor != null) {
			minNeighbor.add(leaf);
		}
		
		
	}

	private int getMinRootLeaf() {
		int minIndex = -1;
		int minCount = Integer.MAX_VALUE;
		
		// avoid just-added node, or else its color will never
		// have a chance to be unique
		for (int i = 0; i < 8; i++) {
			if (root.kids[i] instanceof LeafNode) {
				LeafNode leafNode = (LeafNode) root.kids[i];
				if (leafNode.pixelCount < minCount && leafNode.pixelCount > 1) {
					minCount = leafNode.pixelCount;
					minIndex = i;
				}
			}
		}
		if (minIndex == -1 || minCount > root.pixelCount / 16) {
			// ok, use minimum
			for (int i = 0; i < 8; i++) {
				if (root.kids[i] instanceof LeafNode) {
					LeafNode leafNode = (LeafNode) root.kids[i];
					if (leafNode.pixelCount < minCount) {
						minCount = leafNode.pixelCount;
						minIndex = i;
					}
				}
			}
		}
		if (minIndex == -1)
			throw new IllegalStateException();
		return minIndex;
	}

	/**
	 * Collapse an inner node into a leaf, by merging all the children together.
	 * @param inner
	 * @return 
	 */
	private LeafNode mergeInnerNode(int depth, InnerNode parent) {
		LeafNode newLeaf = new LeafNode(parent.parent);
		for (int i = 0; i < 8; i++) {
			Node n = parent.kids[i];
			if (n instanceof LeafNode) {
				newLeaf.add((LeafNode) n);
				parent.kids[i] = null;
				leafCount--;
			}
			else if (n instanceof InnerNode) {
				LeafNode rec = mergeInnerNode(depth + 1, (InnerNode) n);
				newLeaf.add(rec);
				parent.kids[i] = null;
				leafCount--;
			}
		}

		int index = 0;
		while (index < 8 && parent.parent.kids[index] != parent) {
			index++;
		}

		// bias towards extremes so dark and light are not lost
		if (usingHSV && depth == 0) {
			if (index == 0) {
				//newLeaf.reds = (minRed * newLeaf.pixelCount + newLeaf.reds) / 2; 
				newLeaf.greens = (minGreen * newLeaf.pixelCount + newLeaf.greens) / 2; 
				newLeaf.blues = (minBlue * newLeaf.pixelCount + newLeaf.blues) / 2; 
			} else if (index == 7) {  
				//newLeaf.reds = (maxRed * newLeaf.pixelCount + newLeaf.reds) / 2; 
				newLeaf.greens = (maxGreen * newLeaf.pixelCount + newLeaf.greens) / 2; 
				newLeaf.blues = (maxBlue * newLeaf.pixelCount + newLeaf.blues) / 2; 
			}
		}
		
		parent.parent.kids[index] = newLeaf;

		leafCount++;

		reducibleLists[depth].remove(parent);

		// System.out.println("merged into " + newLeaf);
		return newLeaf;
	}

	public List<LeafNode> gatherLeaves() {
		List<LeafNode> nodes = new ArrayList<ColorOctree.LeafNode>();
		gatherLeaves(nodes, root, 0);
		Collections.sort(nodes, new Comparator<LeafNode>() {

			@Override
			public int compare(LeafNode o1, LeafNode o2) {
				return o2.getPixelCount() - o1.getPixelCount();
			}
		});
		return nodes;
	}

	private void gatherLeaves(List<LeafNode> nodes, InnerNode node, int depth) {
		InnerNode inner = (InnerNode) node;
		for (Node k : inner.kids) {
			if (k instanceof InnerNode)
				gatherLeaves(nodes, (InnerNode) k, depth + 1);
			else if (k instanceof LeafNode)
				nodes.add((LeafNode) k);
		}
	}
}
