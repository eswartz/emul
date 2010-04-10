/**
 * 
 */
package org.ejs.eulang.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.Error;
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

	private final TypeEngine typeEngine;
	private List<Message> messages;

	public TypeInference(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		messages = new ArrayList<Message>();
		
	}
	
	/**
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return messages;
	}
	/**
	 * Infer the types in the tree from known types.
	 */
	public boolean infer(IAstNode node) {
		boolean anyChange = false;
		boolean changed = false;
		
		do {
			messages.clear();
			changed = inferUp(node);
			anyChange |= changed;
		} while (changed);
		
		validateTypes(node);
		return anyChange;
	}
	

	/**
	 * @param node
	 */
	private boolean inferUp(IAstNode node) {
		
		boolean changed = false;
		
		 if (node instanceof IAstDefineStmt) {
			IAstDefineStmt defineStmt = (IAstDefineStmt) node;
			IAstTypedExpr defineExpr = defineStmt.getExpr();
			LLType origDefineType = defineExpr.getType();
			if (origDefineType == null || !origDefineType.isComplete()) {
				TypeInference inference = new TypeInference(typeEngine);
				boolean defineChanged = inference.infer(defineExpr);
				
				System.out.println("Inferring on define:");
				DumpAST dump = new DumpAST(System.out);
				defineStmt.accept(dump);
				
				if (!defineChanged || (defineExpr.getType() == null || !defineExpr.getType().isComplete())) {
					// a standalone define should have a known type based on predecessors,
					// so this must be a generic
					boolean madeGeneric = genericize(defineExpr);
					if (madeGeneric) {
						return true;
					}
				}
				messages.addAll(inference.getMessages());
				//if (defineExpr.getType() != null && defineExpr.getType().isMoreComplete(origDefineType))
				//	changed = true;
			} else if (origDefineType.isGeneric()) {
				return changed;
			}
		}
		 
		for (IAstNode kid : node.getChildren()) {
			changed |= inferUp(kid);
		}
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typed = (IAstTypedNode) node;
			
			// instantiate generic defines
			if (typed instanceof IAstSymbolExpr && typed.getType() != null && typed.getType().isGeneric()
					&& ((IAstSymbolExpr) typed).getSymbol().getDefinition() instanceof IAstDefineStmt) {
				changed |= instantiate((IAstSymbolExpr) typed, (IAstDefineStmt) ((IAstSymbolExpr) typed).getSymbol().getDefinition());
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
	private boolean instantiate(IAstSymbolExpr site, IAstDefineStmt define) {
		IAstNode context = site;
		while (context != null) {
			if (context instanceof IAstTypedNode) {
				IAstTypedNode typed = (IAstTypedNode) context;
				if (typed.getType() != null && (!typed.getType().isComplete() || typed.getType().isGeneric())) {
					LLType expandedType = typed.inferExpansion(typeEngine, define.getExpr());
					if (expandedType != null) {

						ISymbol expansionSym = null;
						IAstTypedExpr expansion = null;

						// the expanded type may yet have unknowns or generics in it
						for (Map.Entry<ISymbol, IAstTypedExpr> entry : define.expansions().entrySet()) {
							if (entry.getValue().getType().isMoreComplete(expandedType)
									|| expandedType.equals(entry.getValue().getType())) {
								site.setSymbol(entry.getKey());
								return true;
							}
							if (expandedType.isMoreComplete(entry.getValue().getType())) {
								// we can improve the expansion
								expansionSym = entry.getKey();
								expansion = entry.getValue();
								break;
							}
						}
						
						if (expansion == null) {
							// nothing matched; make a new one
							System.out.println("Creating expansion of " + define.getSymbol() +  " for " + expandedType + ":");
							
							expansion = (IAstTypedExpr) define.getExpr().copy(null);
							replaceGenericTypes(expansion, expandedType);
							
							System.out.println("Initial expansion:");
							DumpAST dump = new DumpAST(System.out);
							expansion.accept(dump);
							
							expansionSym = define.getSymbol().getScope().addTemporary(define.getSymbol().getName() + "$instance", true);
							define.expansions().put(expansionSym, expansion);
						}
						
						expansion.setType(expandedType);
						
						TypeInference inference = new TypeInference(typeEngine);
						boolean updated = inference.infer(expansion);
						expandedType = expansion.getType();
						if (updated) {
							System.out.println("Updated expansion of " + expansionSym + ":");
							DumpAST dump = new DumpAST(System.out);
							expansion.accept(dump);
						}
						
						expansionSym.setType(expandedType);
						site.setSymbol(expansionSym);
						return true;
					}
				} else {
					break;
				}
			}
			context = context.getParent();
		}
		return false;
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
		if (currentType.isGeneric())
			expansionMap.put(currentType, expandedType);
		if (currentType instanceof LLAggregateType) {
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
		
		TypeInference inference = new TypeInference(typeEngine);
		boolean changed = inference.infer(defineExpr);
		
		if (changed) {
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
				if (((IAstDefineStmt) node).getExpr().getType().isGeneric())
					return;
			}
			if (node instanceof IAstTypedNode && ((IAstTypedNode) node).getType() != null) {
				
				if (node instanceof IAstTypedNode && ((IAstTypedNode) node).getType().isGeneric()) {
					messages.add(new Error(node, "generic types not expanded; add some type declarations"));
				}
			}
			
			node.validateType(typeEngine);
			
			try {
				node.validateChildTypes(typeEngine);
			} catch (ASTException e) {
				messages.add(new Error(node, e.getMessage()));
			}
			
			// continue validating kids if node succeeded on its own
			for (IAstNode kid : node.getChildren()) {
				validateTypes(kid);
			}
		} catch (ASTException e) {
			// node failed, stop here
			messages.add(new Error(node, e.getMessage()));
		}
		
	}

}
