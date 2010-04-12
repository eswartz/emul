/**
 * 
 */
package org.ejs.eulang.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.Error;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
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
 * Otherwise, the outcome of this phase is a tree where types may still be
 * incomplete, due to symbols that lack types.
 * <p>
 *  * <p>
 * <b>First</b>, if the existing types have incorrect basic type classes (e.g.
 * shifting by a float or adding a boolean), then these are due to user error,
 * and errors are thrown and the inference fails.

 * 
 * 
 * @author ejs
 * 
 */
public class TypeInference {

	public static boolean DUMP = true;
	
	private final TypeEngine typeEngine;
	private List<Message> messages;

	private Set<IAstNode> instantiationSet = new HashSet<IAstNode>();
	
	public TypeInference(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		messages = new ArrayList<Message>();
		
	}
	
	public TypeInference subInferenceJob() {
		TypeInference inference = new TypeInference(typeEngine);
		inference.instantiationSet = instantiationSet;
		return inference;
	}
	
	/**
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return messages;
	}
	/**
	 * Infer the types in the tree from known types.
	 * @param validateTypes if true, make sure all types are concrete after inferring 
	 */
	public boolean infer(IAstNode node, boolean validateTypes) {
		boolean anyChange = false;
		boolean changed = false;
		
		do {
			messages.clear();
			changed = inferUp(node);
			anyChange |= changed;
		} while (changed);
		
		if (validateTypes)
			validateTypes(node);
		
		return anyChange;
	}
	

	/**
	 * @param node
	 */
	private boolean inferUp(IAstNode node) {
		
		boolean changed = false;
		boolean recurse = true;
		
		if (node instanceof IAstDefineStmt) {
			IAstDefineStmt defineStmt = (IAstDefineStmt) node;
			
			for (IAstTypedExpr bodyExpr : defineStmt.bodyList()) {
				// don't infer on macros
				if (bodyExpr instanceof IAstCodeExpr && ((IAstCodeExpr) bodyExpr).isMacro())
					continue;
				
				// first, see if the top-level type can be inferred, for the case
				// of overloaded functions calling each other.
				try {
					changed |= bodyExpr.inferTypeFromChildren(typeEngine);
				} catch (TypeException e) {
					messages.add(new Error(bodyExpr, e.getMessage()));
				}

				

				LLType origDefineType = bodyExpr.getType();
				if (origDefineType == null || !origDefineType.isComplete()) {
					
					boolean defineChanged = false;
					
					
					TypeInference inference = subInferenceJob();
					defineChanged = inference.infer(bodyExpr, false);
					
					if (DUMP) {
						System.out.println("Inferring on define:");
						DumpAST dump = new DumpAST(System.out);
						defineStmt.accept(dump);
					}
					
					if (!defineChanged || (bodyExpr.getType() == null || !bodyExpr.getType().isComplete())) {
						// a standalone define should have a known type based on predecessors,
						// so this must be a generic
						boolean madeGeneric = genericize(bodyExpr);
						if (madeGeneric) {
							return true;
						}
					}
					messages.addAll(inference.getMessages());
					//if (defineExpr.getType() != null && defineExpr.getType().isMoreComplete(origDefineType))
					//	changed = true;
					
					changed |= inferUp(bodyExpr);
				} else if (origDefineType.isGeneric()) {
					// okay, don't infer again here
				} else {
					// infer in case expansions introduced more unknown nodes inside
					changed |= inferUp(bodyExpr);
				}
			}
			
			changed |= inferUp(defineStmt.getSymbolExpr());
			
			recurse = false;
		}

		// don't infer on macros (until we know how to remove/instantiate generic args)
		if (node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())
			return changed;
		
		
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
			
			if (/*changed || typed.getType() == null || !typed.getType().isComplete()*/ true) {
				try {
					changed |= typed.inferTypeFromChildren(typeEngine);
				} catch (TypeException e) {
					messages.add(new Error(node, e.getMessage()));
				}
			}
		}		

		return changed;
	}

	/**
	 * @param context 
	 */
	private boolean instantiate(IAstSymbolExpr site) {
		
		// Does this refer to a definition (still)?
		// 
		// We will replace all symbol exprs with references to actual definitions if possible.
		//
		IAstDefineStmt define = site.getDefinition();
		if (define == null)
			return false;
		
		if (instantiationSet.contains(site))
			return false;

		// Get the actual type expected for the site (don't use the symbol's site, since that aliases
		// other definitions and uses)
		//
		
		LLType expandedType = null;
		IAstNode context = site;
		while (context != null) {
			if (context instanceof IAstTypedNode) {
				IAstTypedNode typed = (IAstTypedNode) context;
				if (true /*typed.getType() != null  && (!typed.getType().isComplete() || typed.getType().isGeneric())*/) {
					expandedType = typed.inferExpansion(typeEngine, null);
					if (expandedType != null) {
						break;
					}
				}
			}
			context = context.getParent();
		}
		
		if (expandedType == null) {
			// look for another body in this definition
			/*if (site.getParent() == define) {
				for (IAstTypedExpr expr : define.bodyList()) {
					if (expr.getType() != null && expr.getType().matchesExactly(site.getType())) {
						expandedType = expr.getType();
						break;
					}
				}
			}*/
			if (expandedType == null) {
				return false;
			}
		}
		
		IAstTypedExpr body = define.getMatchingBodyExpr(expandedType);
		if (body == null) {
			body = define.getMatchingBodyExpr(site.getType());
			if (body == null) {
				return false;
			}
		}

		
		try {
			instantiationSet.add(site);
			if (site.getType() != null && (expandedType.isGeneric() || body.getType().isGeneric())) {
				return doInstantiateGeneric(site, define, expandedType, body);
			} else {
				return doInstantiateBody(site, define, expandedType, body);
			}
		} finally {
			instantiationSet.remove(site);
		}
	}

	private boolean doInstantiateGeneric(IAstSymbolExpr site,
			IAstDefineStmt define, LLType expandedType, IAstTypedExpr body) {
		IAstTypedExpr expansion = define.getMatchingInstance(body.getType(), expandedType);

		/*
		// the expanded type may yet have unknowns or generics in it
		for (Map.Entry<ISymbol, IAstTypedExpr> entry : define.instances().entrySet()) {
			if (entry.getValue().getType().isMoreComplete(expandedType)
					|| expandedType.equals(entry.getValue().getType())) {
				site.setSymbol(entry.getKey());
				return true;
			}
			if (expandedType.isMoreComplete(entry.getValue().getType())) {
				// we can improve the expansion
				//expansionSym = entry.getKey();
				expansion = entry.getValue();
				break;
			}
		}
		*/
		
		ISymbol expansionSym = site.getSymbol();
		
		if (expansion == null || expansion.getType().isGeneric()) {
			// nothing matched; make a new one
			if (DUMP) 
				System.out.println("Creating expansion of " + define.getSymbol() +  " for " + expandedType + ":");
			
			expansion = (IAstTypedExpr) body.copy(null);
			replaceGenericTypes(expansion, expandedType);
			
			if (DUMP) {
				System.out.println("Initial expansion:");
				DumpAST dump = new DumpAST(System.out);
				expansion.accept(dump);
			}
			
			expansionSym = define.getSymbol().getScope().addTemporary(define.getSymbol().getName(),
					false);
			expansionSym.setDefinition(expansion);
		}
		
		
		
		TypeInference inference = subInferenceJob();
		boolean updated = inference.infer(expansion, false);
		expandedType = expansion.getType();
		if (updated && DUMP) {
			System.out.println("Updated expansion of " + define.getSymbol() + " for " + expandedType + ":");
			DumpAST dump = new DumpAST(System.out);
			expansion.accept(dump);
		}
		
		expansionSym.setType(expandedType);
		
		site.setSymbol(expansionSym);
		site.setType(expandedType);
		
		define.registerInstance(body.getType(), expansion);
		return true;
	}

	private boolean doInstantiateBody(IAstSymbolExpr site,
			IAstDefineStmt define, LLType expandedType, IAstTypedExpr body) {
		if (define.bodyList().size() == 1)
			return false;
		
		ISymbol bodySym = define.getSymbol().getScope().addTemporary(define.getSymbol().getName(),
				false);
		bodySym.setDefinition(body);


		// replace immediately in case of recursion when inferring below,
		// which will recurse
		//site.setSymbol(bodySym);
		//site.setType(expandedType);
			
		
		TypeInference inference = subInferenceJob();
		boolean updated = inference.infer(body, false);
		
		LLType bodyType = body.getType();
		
		if (updated && DUMP) {
			System.out.println("Updated body of " + define.getSymbol() + " for " + bodyType + ":");
			DumpAST dump = new DumpAST(System.out);
			body.accept(dump);
		}
		
		site.setType(bodyType);

		bodySym.setDefinition(body);
		bodySym.setType(site.getType());
		
		site.setSymbol(bodySym);
		site.setType(bodyType);
			
		//define.registerInstance(body.getType(), body);
		return true;
	}

	/**
	 * @param expansion
	 * @param expandedType
	 */
	private void replaceGenericTypes(IAstTypedExpr expansion,
			LLType expandedType) {
		Map<LLType, LLType> expansionMap = new HashMap<LLType, LLType>();
		getTypeInstanceMap(expansion.getType(), expandedType, expansionMap);
		
		replaceTypes(expansion, expansionMap);
	}

	/**
	 * @param expansion
	 * @param expansionMap
	 */
	private void replaceTypes(IAstNode expansion,
			Map<LLType, LLType> expansionMap) {
		if (expansion instanceof IAstTypedNode) {
			LLType repl = expansionMap.get(((IAstTypedNode)expansion).getType());
			if (repl != null)
				((IAstTypedNode)expansion).setType(repl);
		}
		for (IAstNode kid : expansion.getChildren()) {
			replaceTypes(kid, expansionMap);
		}
	}

	private void getTypeInstanceMap(LLType currentType,
			LLType expandedType, Map<LLType, LLType> expansionMap) {
		if (currentType == null)
			return;
		if (currentType.isGeneric())
			expansionMap.put(currentType, expandedType);
		if (currentType instanceof LLAggregateType && expandedType != null) {
			LLType[] types = ((LLAggregateType) currentType).getTypes();
			LLType[] expandedTypes = ((LLAggregateType) expandedType).getTypes();
			for (int i = 0; i < types.length; i++) {
				getTypeInstanceMap(types[i], expandedTypes[i], expansionMap);
			}
		}
	}

	/**
	 * Make the given expression generic.  Replace types with variables.
	 * @param expr
	 */
	public boolean genericize(IAstTypedExpr defineExpr) {
		LLType type = defineExpr.getType();
		if (!(type instanceof LLAggregateType))
			return false;
		LLAggregateType aggregate = (LLAggregateType) type;
		
		LLType[] types = new LLType[aggregate.getCount()];
		
		boolean anyGeneric = false;
		char ch = 'T';
		String prefix = "";
		for (int i = 0; i < aggregate.getCount(); i++) {
			if ((types[i] = aggregate.getType(i)) == null) {
				anyGeneric = true;
				types[i] = new LLGenericType(prefix + ch);
				ch++;
				if (ch > 'Z')
					ch = 'A';
				else if (ch == 'T')
					prefix += "T";
			}
		}
		
		if (!anyGeneric)
			return false;
		
		LLType newType = aggregate.updateTypes(types);
		defineExpr.setType(newType);
		
		TypeInference inference = subInferenceJob();
		boolean changed = inference.infer(defineExpr, false);
		
		if (changed && DUMP) {
			System.out.println("Replacing generic types in: ");
			DumpAST dump = new DumpAST(System.out);
			defineExpr.accept(dump);
		}
		return changed;
	}


	/**
	 * @param node
	 */
	private void validateTypes(IAstNode node) {
		try {
			if (node instanceof IAstDefineStmt) {
				IAstDefineStmt define = (IAstDefineStmt) node;
				for (IAstTypedExpr expr : define.getConcreteInstances()) {
					validateTypes(expr);
				}
				return;
			}

			if (node instanceof IAstTypedNode) {
				IAstTypedNode typed = (IAstTypedNode) node;
				if (typed.getType() == null ) {
					throw new TypeException(node, "unknown types encountered; add some type specifications");
				}
			}
			node.validateType(typeEngine);
			
			try {
				node.validateChildTypes(typeEngine);
			} catch (TypeException e) {
				messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
			}
			
			// continue validating kids if node succeeded on its own
			for (IAstNode kid : node.getChildren()) {
				validateTypes(kid);
			}
		} catch (TypeException e) {
			// node failed, stop here
			messages.add(new Error(e.getNode() != null ? e.getNode() : node, e.getMessage()));
		}
		
	}

}
