/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.Error;
import org.ejs.eulang.ast.ExpandAST;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSelfReferentialType;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This class infers types in an AST. We allow code to have unspecified types
 * for variables (in the prototype and in variable allocations) as well as
 * missing cast operators in expressions. By a consequence of the initial parse,
 * the temporary/oeprator nodes in the tree will be lacking types as well.
 * <p>
 * There are several pieces to inference:
 * <p>
 * <b>First</b>, we try to fill in any obvious missing types from bottom up,
 * using known type information. Some leaf nodes may have types:
 * {@link IAstLitExpr} has a known type. {@link IAstSymbolExpr} has a type if
 * its symbol has one. If these are set, then parent nodes can fill in the
 * appropriate types based on the semantics of operations.
 * <p>
 * "Appropriate" types may be fuzzy here; e.g., for adding two integers, rather
 * than filling in completely equal integer types, the addition node and
 * children may merely indicate "some integer is needed here".
 * <p>
 * When a definition (IAstDefinition) is referenced, it may have more than
 * one expansion.  Either it has been explicitly declared with several variants
 * or it has generic types.  If it has a generic type, we ensure that all the
 * unknown types in the declaration are named and replaced throughout the tree,
 * so we can distinguish unnamed generic types versus unknown types, which could
 * not be deduced (yet).  We create concrete instances for the generic instances
 * if we can infer the types.
 * <p>
 * Otherwise, the outcome of this phase is a tree where types may still be
 * incomplete, due to symbols that lack types.
 * 
 * 
 * @author ejs
 * 
 */
public class TypeInference {

	public static boolean DUMP = false;
	
	private final TypeEngine typeEngine;
	private Set<Message> messages;

	private Set<IAstNode> instantiationSet = new HashSet<IAstNode>();
	private Set<Integer> genericizedSet = new HashSet<Integer>();
	
	public TypeInference(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		messages = new LinkedHashSet<Message>();
	}
	
	public TypeInference subInferenceJob() {
		TypeInference inference = new TypeInference(typeEngine);
		inference.instantiationSet = instantiationSet;
		return inference;
	}
	
	/**
	 * @return the messages
	 */
	public Collection<Message> getMessages() {
		return messages;
	}
	
	
	
	/**
	 * Infer the types in the tree from known types.
	 * @param validateTypes if true, make sure all types are concrete after inferring 
	 */
	public boolean infer(IAstNode node, boolean validateTypes) {
		boolean anyChange = false;
		boolean changed = false;
		
		/*
		do {
			messages.clear();
			try {
				changed = inferDown(node);
			} catch (TypeException e) {
				messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
				break;
			}
			anyChange |= changed;
		} while (changed);
		
		if (messages.isEmpty())
			*/
		{
			changed = false;
			do {
				messages.clear();
				changed = inferUp(node);
				anyChange |= changed;
			} while (changed);
			
			if (validateTypes && messages.isEmpty())
				validateTypes(node);
		}
		
		return anyChange;
	}
	

	/**
	 * Infer types from top down to establish the types of definition bodies and
	 * allocations.
	 * @param node
	 * @throws TypeException 
	 */
	/*
	private boolean inferDown(IAstNode node) throws TypeException {
		
		boolean changed = false;
		boolean recurse = true;
		
		if (node instanceof IAstDefineStmt) {
			IAstDefineStmt defineStmt = (IAstDefineStmt) node;
			
			Set<Integer> visited = new TreeSet<Integer>();
			changed = inferDefinitions(defineStmt, visited);
			
			Collection<IAstTypedExpr> concreteInstances = defineStmt.getConcreteInstances();
			while (visited.size() < concreteInstances.size()) {
				changed = inferDefinitionInstances(defineStmt, concreteInstances, visited);
				concreteInstances = defineStmt.getConcreteInstances();
			}
			
			changed |= inferDown(defineStmt.getSymbolExpr());
			
			recurse = false;
			
			return changed;
		}
		
		// don't infer on macros (until we know how to remove/instantiate generic args)
		if (node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())
			return changed;
		
		
		if (node instanceof IAstSelfReferentialType) {
			recurse = false;
		}
		
		if (recurse) {
			for (IAstNode kid : node.getChildren()) {
				changed |= inferDown(kid);
			}
		}
		if (node instanceof IAstSymbolExpr) {
			IAstSymbolExpr typed = (IAstSymbolExpr) node;
			
			// instantiate generic defines
			//if (typed instanceof IAstSymbolExpr) {
			//	changed |= instantiate((IAstSymbolExpr) typed);
			//}
			
			try {
				changed |= typed.inferTypeFromChildren(typeEngine);
			} catch (TypeException e) {
				messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
			}
		}		

		return changed;
	}
*/
	/**
	 * Infer types from bottom up.  This assumes that all symbols in a scope have
	 * a known type.
	 * @param node
	 */
	private boolean inferUp(IAstNode node) {
		
		boolean changed = false;
		boolean recurse = true;
		
		if (node instanceof IAstDefineStmt) {
			IAstDefineStmt defineStmt = (IAstDefineStmt) node;
			
			Set<Integer> visited = new TreeSet<Integer>();
			changed = inferDefinitions(defineStmt, visited);
			
			Collection<IAstTypedExpr> concreteInstances = defineStmt.getConcreteInstances();
			visited.clear();
			while (visited.size() < concreteInstances.size()) {
				changed = inferDefinitionInstances(defineStmt, concreteInstances, visited);
				concreteInstances = defineStmt.getConcreteInstances();
			}
			
			changed |= inferUp(defineStmt.getSymbolExpr());
			
			recurse = false;
			
			return changed;
		}
		
		if (node instanceof IAstSymbolExpr) {
			
		}
		// don't infer on macros (until we know how to remove/instantiate generic args)
		if (node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())
			return changed;
		
		
		if (node instanceof IAstSelfReferentialType) {
			recurse = false;
		}
		
		if (recurse) {
			for (IAstNode kid : node.getChildren()) {
				changed |= inferUp(kid);
			}
		}
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typed = (IAstTypedNode) node;
			
			// instantiate generic defines
			if (typed instanceof IAstSymbolExpr) {
				changed |= instantiate((IAstSymbolExpr) typed);
			}
			
			try {
				changed |= typed.inferTypeFromChildren(typeEngine);
			} catch (TypeException e) {
				messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
			}
		}		
		
		return changed;
	}
	
	private boolean inferDefinitions(IAstDefineStmt defineStmt, Set<Integer> visited) {
		
		boolean changed = false;
		
		for (IAstTypedExpr bodyExpr : defineStmt.bodyList()) {
			visited.add(bodyExpr.getId());
			
			// don't infer on macros
			if (bodyExpr instanceof IAstCodeExpr && ((IAstCodeExpr) bodyExpr).isMacro())
				continue;
			

			// first, see if the top-level type can be inferred, for the case
			// of overloaded functions calling each other.
			if (bodyExpr.getType() == null) {
				try {
					changed |= bodyExpr.inferTypeFromChildren(typeEngine);
				} catch (TypeException e) {
					messages.add(new Error(bodyExpr, e.getMessage()));
				}
			}
			
			
			if (defineStmt.isGeneric()) {
				boolean defineChanged = false;
				
				// see if we can still infer some types
				if (!genericizedSet.contains(bodyExpr.getId())) {
					
					//if (bodyExpr.getType() == null || !bodyExpr.getType().isComplete()) 
					{
						TypeInference inference = subInferenceJob();
						defineChanged = inference.infer(bodyExpr, false);
					}
					
					boolean madeGeneric = false;
					madeGeneric = genericize(defineStmt.getScope(), bodyExpr);
					genericizedSet.add(bodyExpr.getId());
					if (madeGeneric) {
						defineChanged = true;
						
						//inference = subInferenceJob();
						//defineChanged |= inference.infer(bodyExpr, false);
						
						if (DUMP) {
							System.out.println("After genericizing define:");
							DumpAST dump = new DumpAST(System.out);
							bodyExpr.accept(dump);
						}
						
					}
				}
				
				//bodyExpr.setType(origType);
				changed |= defineChanged;
				continue;
			}
			
			// Try to determine if the body is generic or not
			LLType origDefineType = bodyExpr.getType();
			if (origDefineType == null || !origDefineType.isComplete() ) {
				
				//boolean wasGeneric = origDefineType != null && origDefineType.isGeneric();
				
				TypeInference inference = subInferenceJob();
				
				changed|= inference.infer(bodyExpr, false);
				
				
				if (DUMP) {
					System.out.println("Inferring on define " + defineStmt.getSymbol() + " for body " + bodyExpr.getType() + ":");
					DumpAST dump = new DumpAST(System.out);
					bodyExpr.accept(dump);
				}
				
				/*
				if (!defineChanged || (bodyExpr.getType() == null || !bodyExpr.getType().isComplete())) {
					// a standalone define should have a known type based on its references,
					// so this must be a generic
					if (defineChanged && bodyExpr.getType() != null && bodyExpr.getType().isGeneric())
						return false;
					boolean madeGeneric = genericize(bodyExpr);
					if (madeGeneric) {
						return true;
					}
				}
				*/
				messages.addAll(inference.getMessages());
				
				changed |= inferUp(bodyExpr);
				//bodyExpr.setType(origType);
			} else if (origDefineType.isGeneric()) {
				
			} else {
				// infer in case expansions introduced more unknown nodes inside
				//LLType origType = bodyExpr.getType();
				changed |= inferUp(bodyExpr);
				//bodyExpr.setType(origType);
			}
		}
		return changed;
	}

	private boolean inferDefinitionInstances(IAstDefineStmt defineStmt,
			Collection<IAstTypedExpr> concreteInstances, Set<Integer> visited) {
		
		boolean changed = false;
		
		for (IAstTypedExpr bodyExpr : concreteInstances) {
			if (visited.contains(bodyExpr.getId())) {
				System.out.println("skipping " +bodyExpr.getId());
				continue;
			}
			
			visited.add(bodyExpr.getId());
			
			//if (bodyExpr.getType() != null && bodyExpr.getType().isGeneric())
			//	continue;
			
			TypeInference subInference = subInferenceJob();
			changed |= subInference.inferUp(bodyExpr);
			
			Collection<Message> subMessages = subInference.getMessages();
			if (!subMessages.isEmpty()) {
				messages.addAll(subMessages);
				messages.add(new Error(defineStmt, "Could not resolve definition " + defineStmt.getSymbol()));
			}
				
			if (DUMP) {
				System.out.println("Inferring on define " + defineStmt.getSymbol() + " for body " + bodyExpr.getType() + ":");
				DumpAST dump = new DumpAST(System.out);
				bodyExpr.accept(dump);
			}
		}
		return changed;
	}

	/**
	 * Ensure the given generic define has a generic expression where all the top-level types are
	 * generic.
	 * @param expr
	 */
	private boolean genericize(IScope scope, IAstTypedExpr defineExpr) {
		LLType type = defineExpr.getType();
		if (!(type instanceof LLAggregateType))
			return false;
		LLAggregateType aggregate = (LLAggregateType) type;
		
		LLType[] types = new LLType[aggregate.getCount()];
		
		boolean anyGeneric = false;
		
		int unique = 0;
		for (int i = 0; i < aggregate.getCount(); i++) {
			if ((types[i] = aggregate.getType(i)) == null) {
				
				// add new type variables
				anyGeneric = true;
				String name = "T" + unique;
				while (scope.get(name) != null) {
					unique++;
					name = "T" + unique;
				}
				
				// the AST <-> Symbol <-> LLType stanza...
				IAstName genericName = new AstName(name);
				ISymbol genericSym = scope.add(genericName);
				//genericSym.setTemporary(true);
				genericSym.setDefinition(genericName);
				
				types[i] = new LLGenericType(genericSym);
				genericSym.setType(types[i]);
				
			}
		}
		
		if (!anyGeneric)
			return false;
		
		LLType newType = aggregate.updateTypes(typeEngine, types);
		defineExpr.setType(newType);
		//boolean changed = AstTypedNode.updateType(defineExpr, newType);
		
		
		boolean changed = true;
		/*
		TypeInference inference = subInferenceJob();
		changed = inference.infer(defineExpr, false);
		
		if (changed && DUMP) {
			System.out.println("Replacing generic types in: ");
			DumpAST dump = new DumpAST(System.out);
			defineExpr.accept(dump);
		}*/
		return changed;
	}

	private boolean instantiate(IAstSymbolExpr site) {
		// Get the actual type expected for the site (don't use the symbol's site, since that aliases
		// other definitions and uses)
		//
		
		LLType expandedType = site.getType();
	
		if (expandedType == null) {
			return false;
		}
		
		// Did this symbol once refer to a definition?  We won't instantiate anything else. 
		//
		IAstDefineStmt define = site.getDefinition();
		if (define == null)
			return false;
		
		// Now, does the symbol *still* refer to the definition?  If so, 
		// it's a candidate for expansion.
		
		IAstNode definition = site.getSymbol().getDefinition();
		if (!(definition instanceof IAstDefineStmt))
			return false;
		
	
		// See if it's still generic..,
		//
		IAstTypedExpr body = site.getBody();
		
		if (body == null)
			return false;
	
		if (body.getType() == null || !body.getType().isGeneric() || !expandedType.isMoreComplete(body.getType()))
			return false;
		
		// Ok, we can progress.
		//
		
		if (instantiationSet.contains(site))
			return false;

		try {
			instantiationSet.add(site);
			if (site.getType() != null && expandedType.getBasicType() != BasicType.VOID
					&&(expandedType.isMoreComplete(body.getType()))) {
				return doInstantiateGeneric(site, define, expandedType, body);
			} else {
				return false;
			}
		} finally {
			instantiationSet.remove(site);
		}
	}
	private boolean doInstantiateGeneric(IAstSymbolExpr site,
			IAstDefineStmt define, LLType expandedType, IAstTypedExpr body) {
		ISymbol expansionSym = define.getMatchingInstance(body.getType(), expandedType);
		
		if (expansionSym == null) {
			IAstTypedExpr expansion = null;
	
			// nothing matched; make a new one
			if (DUMP) 
				System.out.println("Creating expansion of " + define.getSymbol() +  " for " + expandedType + ":");
			
			ExpandAST expander = new ExpandAST(typeEngine, true);
			expansion = (IAstTypedExpr) body.copy();
			expansion = (IAstTypedExpr) expander.expand(messages, expansion);
			expansion.uniquifyIds();
			replaceGenericTypes(define, expansion, expandedType);
			
			if (DUMP) {
				System.out.println("Initial expansion:");
				DumpAST dump = new DumpAST(System.out);
				expansion.accept(dump);
			}
			
			//expansionSym = define.getSymbol().getScope().addTemporary(define.getSymbol().getName());
			//expansionSym.setDefinition(expansion);
			
			
			// infer types now that generic types are real
			TypeInference inference = subInferenceJob();
			boolean updated = inference.infer(expansion, false);
			expandedType = expansion.getType();
			if (updated && DUMP) {
				System.out.println("Updated expansion of " + define.getSymbol() + " for " + expandedType + ":");
				DumpAST dump = new DumpAST(System.out);
				expansion.accept(dump);
			}
			
			expansionSym = define.registerInstance(body, expansion);
			//expansionSym.setType(expandedType);
		}
		site.setSymbol(expansionSym);
		site.setType(expansionSym.getType());
		
		return true;
	}

	/**
	 * Replace generics whose names match the same positions in the expanded type.
	 * This is not the same as expanding an instance since (1) we replace only
	 * types, not arbitrary ASTS, and (2) we don't know the names of the generic
	 * type variables, but we already have generic types to replace. 
	 * @param define 
	 * @param expansion
	 * @param expandedType
	 */
	private void replaceGenericTypes(IAstDefineStmt define, IAstTypedExpr expansion, LLType expandedType) {
		Map<LLType, LLType> expansionMap = new HashMap<LLType, LLType>();
		getTypeInstanceMap(expansion.getType(), expandedType, expansionMap);
		
		AstNode.replaceTypesInTree(typeEngine, expansion, expansionMap);
	}
	private void getTypeInstanceMap(LLType currentType,
			LLType expandedType, Map<LLType, LLType> expansionMap) {
		if (currentType == null)
			return;
		if (currentType.isGeneric())
			expansionMap.put(currentType, expandedType);
		if (currentType instanceof LLAggregateType && expandedType instanceof LLAggregateType) {
			LLType[] types = ((LLAggregateType) currentType).getTypes();
			LLType[] expandedTypes = ((LLAggregateType) expandedType).getTypes();
			for (int i = 0; i < types.length; i++) {
				getTypeInstanceMap(types[i], expandedTypes[i], expansionMap);
			}
		}
	}

	private void validateTypes(IAstNode node) {
		try {
			boolean recurse = true;
			//if (node.getParent() == null && !(node instanceof IAstModule))
			//	return;
				
			if (node instanceof IAstDefineStmt) {
				IAstDefineStmt define = (IAstDefineStmt) node;
				Collection<IAstTypedExpr> concreteInstances = define.getConcreteInstances();
				if (!define.isGeneric() && concreteInstances.isEmpty() && !define.bodyList().isEmpty()) {
					concreteInstances = define.bodyList();
					//throw new TypeException(define, "unresolved define instances encountered; add some type specifications");
				}
				for (IAstTypedExpr expr : concreteInstances) {
					validateTypes(expr);
				}
				return;
			}

			if (node instanceof IAstSelfReferentialType) {
				recurse = false;
			}
			if (node instanceof IAstTypedNode) {
				IAstTypedNode typed = (IAstTypedNode) node;
				if (typed instanceof IAstCodeExpr && ((IAstCodeExpr) typed).isMacro()) {
					// okay 
					recurse = false;
				} else {
					node.validateType(typeEngine);
				}
			}
			
			
			try {
				node.validateChildTypes(typeEngine);
			} catch (TypeException e) {
				messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
			}
			
			// continue validating kids if node succeeded on its own
			if (recurse) {
				for (IAstNode kid : node.getChildren()) {
					validateTypes(kid);
				}
			}
		} catch (TypeException e) {
			// node failed, stop here
			messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
		}
		
	}

}
