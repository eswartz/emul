/**
 * 
 */
package org.ejs.eulang.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.Error;
import org.ejs.eulang.ast.IAstNode;

/**
 * @author ejs
 *
 */
public class InferenceGraph {

	private Set<ITyped> heads = new HashSet<ITyped>();
	private Set<ITyped> tails = new HashSet<ITyped>();
	
	private List<IRelation> edges = new LinkedList<IRelation>();
	private Map<ITyped, List<IRelation>> relationMap = new HashMap<ITyped, List<IRelation>>();
	private final TypeEngine typeEngine;
	
	public InferenceGraph(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
	}
	
	public void addEquivalence(ITyped from, ITyped to) {
		if (from != null && to != null) {
			add(new EquivalenceRelation(from, to));
		}
	}
	public void addEquivalence(ITyped from, ITyped[] to) {
		if (from != null && to != null) {
			add(new EquivalenceRelation(from, to));
		}
	}
	public void addCompatibility(ITyped from, ITyped to) {
		if (from != null && to != null) {
			add(new CompatibilityRelation(from, to));
		}
	}
	public void addCompatibility(ITyped from, ITyped[] to) {
		if (from != null && to != null) {
			add(new CompatibilityRelation(from, to));
		}
	}
	
	public void add(IRelation relation) {
		ITyped head = relation.getHead();
		
		if (!(head instanceof IAstNode)
				|| ((IAstNode) head).getParent() == null)
			heads.add(head);

		for (ITyped tail : relation.getTails())
			if (tail instanceof IAstNode && ((IAstNode) tail).getChildren().length == 0)
				tails.add(tail);
		
		edges.add(relation);

		List<IRelation> relations = relationMap.get(head);
		if (relations == null) {
			relations = new ArrayList<IRelation>();
			relationMap.put(head, relations);
		}
		relations.add(relation);
	}

	/**
	 * Pass types down in the tree
	 * @return true if any changes were made
	 */
	public boolean inferDown(List<Message> messages, IAstNode node) {
		
		boolean changed = false;

		List<IRelation> relations = relationMap.get(node);
		if (relations != null) {
			for (IRelation relation : relations) {
				// propagate types down
				if (relation.isComplete())
					continue;
				if (relation.getHead().getType() != null) {
					System.out.println("Infer Down: " + relation);
					try {
						if (relation.inferDown(typeEngine)) {
							System.out.println(" --> " + relation);
							changed = true;
						}
					} catch (TypeException e) {
						IAstNode errNode = e.getNode();
						if (errNode == null)
							errNode = relation.getHead().getNode();
						messages.add(new Error(errNode, e.getMessage()));
					}
				}
			}
		}
			
		for (IAstNode kid : node.getChildren()) {
			changed |= inferDown(messages, kid);
		}
		
		return changed;
	}

	/**
	 * Pass types up the tree, by mapping N types to 1 type.
	 * @return true if any changes were made
	 */
	public boolean inferUp(List<Message> messages, IAstNode node) {
		boolean changed = false;
		
		for (IAstNode kid : node.getChildren()) {
			changed |= inferUp(messages, kid);
		}
		
		List<IRelation> relations = relationMap.get(node);
		if (relations != null) {
			for (IRelation relation : relations) {
				if (relation.isComplete())
					continue;

				System.out.println("Infer Up: " + relation);
				
				// propagate types up if anything is available to use
				boolean allNull = true;
				for (ITyped tail : relation.getTails()) {
					if (tail != null && tail.getType() != null) {
						allNull = false;
						break;
					}
				}
				if (!allNull) {
					try {
						if (relation.inferUp(typeEngine)) {
							System.out.println(" --> " + relation);
							changed = true;
						}
					} catch (TypeException e) {
						messages.add(new Error(relation.getHead().getNode(), e.getMessage()));
					}
				}
			}
		}
		
		return changed;
	}
	

	/**
	 * Finalize the types in the tree
	 * @return true if any changes were made
	 */
	public void finalizeTypes(List<Message> messages, IAstNode node) {
		for (IAstNode kid : node.getChildren()) {
			finalizeTypes(messages, kid);
		}
		
		List<IRelation> relations = relationMap.get(node);
		if (relations != null) {
			for (IRelation relation : relations) {
				System.out.println("Finalize: " + relation);
				
				try {
					relation.finalize(typeEngine);
				} catch (TypeException e) {
					messages.add(new Error(relation.getHead().getNode(), e.getMessage()));
				}
			}
		}
		
		
	}
}
