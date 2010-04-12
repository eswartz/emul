/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;

import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstStmtListExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * TODO: unify with TypeInference; these both contain a lot of duplicate logic for ensuring
 * that we get an accurate idea of a function call's type
 * 
 * @author ejs
 *
 */
public class ExpandAST {
	
	private final TypeEngine typeEngine;

	public ExpandAST(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		
	}

	public boolean expand(List<Message> messages, IAstNode node) {
		boolean changed = false;
		
		if (!(node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())) {
			// go deep first, since node parenting changes as expansion occurs
			IAstNode[] kids = node.getChildren();
			for (int i = 0; i < kids.length; i++) {
				changed |= expand(messages, kids[i]);
			}
		}
		
		try {
			if (node instanceof IAstSymbolExpr) {
				//if (node.getParent() instanceof IAstFuncCallExpr)
				//	return changed;
				
				IAstSymbolExpr symExpr = (IAstSymbolExpr)node;
				IAstNode symDef = symExpr.getDefinition();
				if (symDef == null) {
					// handle later
					if (!symExpr.getSymbol().getScope().encloses(node.getOwnerScope()))
						throw new ASTException(node, "no definition found for " + symExpr.getSymbol().getName());
					return false;
				}
				if (symDef == node.getParent() || !(symDef instanceof IAstDefineStmt))
					return false;
				
				IAstTypedExpr value = null;
				if (symExpr.getParent() instanceof IAstFuncCallExpr) {
					IAstFuncCallExpr funcParent = (IAstFuncCallExpr) symExpr.getParent();
					LLType type = funcParent.inferExpansion(typeEngine, null);
					IAstTypedExpr body = symExpr.getDefinition().getMatchingBodyExpr(type);
					if (body != null) {
						value = symExpr.getDefinition().getMatchingInstance(body.getType(), type);
					}
					
				} else {
					value = symExpr.getInstance();
				}
				if (value != null) {
					
					if (value instanceof IAstCodeExpr) {
						IAstCodeExpr codeExpr = (IAstCodeExpr) value;
						if (codeExpr.isMacro()) {
							// directly replace
							IAstNode copy = value.copy(node);
							removeGenerics(copy);
							node.getParent().replaceChild(node, copy);
							changed = true;
						}
					}
					else {
						// directly replace
						IAstNode copy = value.copy(node);
						removeGenerics(copy);
						node.getParent().replaceChild(node, copy);
						changed = true;
					}
				}
				
				/*
				if (symDef instanceof IAstDefineStmt && ((IAstDefineStmt) symDef).getExpr() instanceof IAstCodeExpr) {
					IAstCodeExpr symDefCode = (IAstCodeExpr) ((IAstDefineStmt) symDef).getExpr();
					
				}
				*/
			} else if (node instanceof IAstFuncCallExpr) {
				IAstFuncCallExpr funcCallExpr = (IAstFuncCallExpr) node;
				IAstTypedExpr funcExpr = funcCallExpr.getFunction();
				if (funcExpr instanceof IAstSymbolExpr) {
					// Call to a define?
					
					IAstSymbolExpr symExpr = (IAstSymbolExpr)funcExpr;
					IAstNode symDef = symExpr.getSymbol().getDefinition();
					if (symDef == null) {
						// handle later
						if (!symExpr.getSymbol().getScope().encloses(node.getOwnerScope()))
							throw new ASTException(node, "no definition found for " + symExpr.getSymbol().getName());
						return false;
					}
					if (symDef == node.getParent() /*|| !(symDef instanceof IAstDefineStmt)*/)
						return false;
					
					IAstTypedExpr value = null;
					if (symExpr.getDefinition() != null) {
						// be sure bodies have types
						for (IAstTypedExpr bodyExpr : symExpr.getDefinition().bodyList()) {
							try {
								changed |= bodyExpr.inferTypeFromChildren(typeEngine);
							} catch (TypeException e) {
								messages.add(new Error(bodyExpr, e.getMessage()));
							}
						}
						
						// and pick an appropriate replacement
						IAstFuncCallExpr funcParent = (IAstFuncCallExpr) symExpr.getParent();
						
						LLType type = funcParent.inferExpansion(typeEngine, null);
						IAstTypedExpr body = symExpr.getDefinition().getMatchingBodyExpr(type);
						if (body != null) {
							value = symExpr.getDefinition().getMatchingInstance(body.getType(), type);
						}
					} else {
						value = symExpr.getInstance();
					}
					if (value != null) {
						if (value instanceof IAstCodeExpr) {
							IAstCodeExpr codeExpr = (IAstCodeExpr) value;
							if (codeExpr.isMacro()) {
								// "call" it
								IAstNode copy = value.copy(node);
								removeGenerics(copy);
								funcCallExpr.replaceChild(funcExpr, copy);
								funcCallExpr.getFunction().setType(codeExpr.getType());
								/*
								IAstStmtListExpr stmtListExpr  = doExpandFuncCallExpr(funcCallExpr, funcCallExpr.arguments(),
										null,
										(IAstCodeExpr) value.copy(funcCallExpr),
										node.getOwnerScope());
								
								if (stmtListExpr != null)
									funcCallExpr.getParent().replaceChild(funcCallExpr, stmtListExpr);
								else
									funcCallExpr.getParent().replaceChild(funcCallExpr, null);
								*/
								changed = true;
							}
						}
						
					}
					
					
					/*
					IAstSymbolExpr symExpr = (IAstSymbolExpr) funcExpr;
					if (symExpr.getSymbol().getDefinition() instanceof IAstDefineStmt) {
						IAstDefineStmt defineStmt = (IAstDefineStmt) symExpr.getSymbol().getDefinition();
						IAstNode def = defineStmt.getExpr();
						if (def instanceof IAstCodeExpr) {
							IAstCodeExpr codeExpr = (IAstCodeExpr) def;
							if (codeExpr.getType() != null && codeExpr.getType().isComplete()) {
								// fine
							} else if (funcExpr.getType() != null && funcExpr.getType().isComplete()) {
								// create a variant for the given type
								IAstTypedExpr instance = instantiateFuncCall(defineStmt, funcCallExpr);
								funcExpr.getParent().replaceChild(funcExpr, instance);
							}

						}
					}*/
				}
				else if (funcExpr instanceof IAstCodeExpr) {
					// Direct expansion of call, e.g.:   code () { } ()
					//
					// Replace the arguments and statements in place of the call.
					//
					// (We may have produced this ourselves.)
					
					IAstNode copy = funcExpr.copy(funcCallExpr);
					removeGenerics(copy);

					IAstStmtListExpr stmtListExpr  = doExpandFuncCallExpr(funcCallExpr, funcCallExpr.arguments(),
							null,
							(IAstCodeExpr) copy,
							node.getOwnerScope());
					
					if (stmtListExpr != null)
						funcCallExpr.getParent().replaceChild(funcCallExpr, stmtListExpr);
					else
						funcCallExpr.getParent().replaceChild(funcCallExpr, null);
					return true;
				}
			} 
		} catch (ASTException e) {
			messages.add(new Error(e.getNode(), e.getMessage()));
		}
		
		/*
		if (node instanceof IAstCodeExpr && changed) {
			((IAstCodeExpr) node).setType(null);
			if (node.getParent() instanceof IAstDefineStmt) {
				((IAstDefineStmt) node.getParent()).getSymbol().setType(null);
			}
		}*/
		
		
		if (changed) {
			System.out.println("after expansion:");
			DumpAST dump = new DumpAST(System.out);
			node.accept(dump);
		}
		return changed;
	}
	
	/**
	 * @param copy
	 */
	private void removeGenerics(IAstNode node) {
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typedNode = (IAstTypedNode) node;
			if (typedNode.getType() != null && typedNode.getType().isGeneric()) {
				typedNode.setType(null);
			}
		}
		for (IAstNode kid : node.getChildren()) {
			removeGenerics(kid);
		}
	}

	/**
	 * @param codeExpr
	 * @param funcCallExpr
	 * @return
	 */
	/*
	private IAstTypedExpr instantiateFuncCall(IAstDefineStmt defineStmt,
			IAstFuncCallExpr funcCallExpr) {
		

		// TODO
		//LLCodeType callType = (LLCodeType) funcCallExpr.getFunction().getType();
		//IAstNode typedExpr = defineStmt.instances().get(callType);
		//if (typedExpr != null)
		//	return (IAstSymbolExpr) typedExpr;
		
		IAstCodeExpr codeExpr = (IAstCodeExpr) defineStmt.getExpr();
		IAstCodeExpr instExpr = codeExpr.copy(null);
		
		return instExpr;
		
		
	}
*/
	
	/**
	 * Expand a function or macro into the tree 
	 * @param node
	 * @param args 
	 * @param codeExpr copy of tree
	 * @param blockList 
	 * @param symDef
	 * @return node containing the return value, or <code>null</code>
	 */
	private IAstStmtListExpr doExpandFuncCallExpr(IAstNode node, IAstNodeList<IAstTypedExpr> args,
			ISymbol funcName,
			IAstCodeExpr codeExpr, 
			IScope parentScope
			) throws ASTException {
		
		// get the scope into which new temporaries go
		IScope nodeScope = node.getOwnerScope();
		if (nodeScope == null) {
			throw new ASTException(node, "no scope found");
		}
		
		IAstNodeList<IAstStmt> blockList = new AstNodeList<IAstStmt>();
		
		// mark all the symbols temporary so they don't collide,
		// and move them into the other scope
		
		// TODO: rename to "@" syntax and remap symbols... or allow remapping as temporaries when copying a scope
		ISymbol[] origSyms = codeExpr.getScope().getSymbols();
		for (ISymbol sym : origSyms) {
			// this refers to the copied definition
			sym.setTemporary(true);
			//sym.setScope(null);
			sym.getScope().remove(sym);
			parentScope.add(sym);
		}
		
		// Substitute arguments
		IAstArgDef[] protoArgs = codeExpr.getPrototype().argumentTypes();
		if (protoArgs.length != args.nodeCount()) {
			// TODO: default values...
			throw new ASTException(args, "argument count does not match prototype " + codeExpr.getPrototype().toString());
		}
		IAstTypedExpr[] realArgs = args.getNodes(IAstTypedExpr.class);
		int realArgIdx = 0;
		for (int i = 0; i < protoArgs.length; i++) {
			IAstTypedExpr realArg = realArgs[i];
			IAstArgDef protoArg = protoArgs[i];
			
			// coerce expression argument to code if needed
			LLCodeType argCode = null;
			if (protoArg.getTypeExpr() != null && protoArg.getTypeExpr().getType() instanceof LLCodeType)
				argCode = ((LLCodeType) protoArg.getTypeExpr().getType());
			else if (protoArg.getType() instanceof LLCodeType)
				argCode = (LLCodeType) protoArg.getType();
			
			if (argCode != null && !(realArg instanceof IAstCodeExpr)) {
				if (argCode.getArgTypes().length > 0)
					throw new ASTException(realArg, "cannot pass expression as an implicit code block since named arguments are required");
				IAstNodeList<IAstStmt> stmtlist = new AstNodeList<IAstStmt>();
				
				if (!protoArg.isMacro())
					stmtlist.add(new AstReturnStmt((IAstTypedExpr) realArg.copy(null)));
				else
					stmtlist.add(new AstExprStmt((IAstTypedExpr) realArg.copy(null)));
				IAstCodeExpr implCode = new AstCodeExpr(new AstPrototype(argCode.getRetType()), new LocalScope(nodeScope), stmtlist, 
						protoArg.isMacro());
				
				setSourceInTree(implCode, realArg.getSourceRef());
				realArg = implCode;
			}
			
			if (!protoArg.isMacro()) {
				realArg.setParent(null);	// deleting call
				// For non-macro arguments, make a single assignment to a new variable
				// using the proto arg's symbol 
				protoArg.getSymbolExpr().setParent(null);
				if (protoArg.getTypeExpr() != null)
					protoArg.getTypeExpr().setParent(null);
				IAstAllocStmt argAlloc = new AstAllocStmt(
						protoArg.getSymbolExpr(), 
						protoArg.getTypeExpr(),
						realArg);
				blockList.add(realArgIdx++, argAlloc);
			} else {
				// For macro arguments, the actual argument is directly replaced
				replaceInTree(codeExpr.stmts(), protoArg.getSymbolExpr(), realArg);
			}
		}
		
		//IAstAllocStmt allocReturnStmt = null;
		//IAstSymbolExpr returnValSymExpr = null;
		//ISymbol returnValSym = null;
		
		//IAstSymbolExpr returnLabelSymExpr = null;
		//ISymbol returnLabelSym = null;
		
		for (IAstStmt stmt : codeExpr.stmts().list()) {
			// when inlining functions, replace returns
			/*
			if (!codeExpr.isMacro() && stmt instanceof IAstReturnStmt) {
				IAstReturnStmt retStmt = (IAstReturnStmt) stmt;
				
				if (retStmt.getExpr() != null) {
					retStmt.getExpr().setParent(null);
					
					if (allocReturnStmt == null) {
						//throw new ASTException(stmt, "cannot return from more than one place in macro expanded function (yet)");
						if (funcName != null)
							returnValSym = parentScope.addTemporary(funcName.getName());
						else
							returnValSym = parentScope.addTemporary("$return");
						
						returnValSymExpr = new AstSymbolExpr(returnValSym);
						allocReturnStmt = new AstAllocStmt(returnValSymExpr, null, null);
						blockList.add(0, allocReturnStmt);
						
					} 
					
					IAstAssignStmt assignStmt = new AstAssignStmt(returnValSymExpr, retStmt.getExpr());
					blockList.add(assignStmt);
				}

				if (returnLabelSymExpr == null) {
					if (funcName != null)
						returnLabelSym = parentScope.addTemporary("$end$" + funcName.getName());
					else
						returnLabelSym = parentScope.addTemporary("$end");
					
					returnLabelSymExpr = new AstSymbolExpr(returnLabelSym);
				}

				
				IAstGotoStmt gotoEndStmt = new AstGotoStmt(returnLabelSymExpr, null);
				blockList.add(gotoEndStmt);
				continue;
			}*/
			
			stmt.setParent(null);
			blockList.add(stmt);
		}
		
		/*
		if (returnLabelSymExpr != null) {
			AstLabelStmt returnLabelStmt = new AstLabelStmt(returnLabelSymExpr);
			blockList.add(returnLabelStmt);
		}*/
		//returnLabelSymExpr.getSymbol().setDefinition(returnLabelStmt);
		
		//codeExpr.stmts().list().clear();
		
		// replace invoke with reference to self
		
		IAstStmtListExpr stmtListExpr = new AstStmtListExpr(/*returnValSymExpr,*/ blockList);
		setSourceInTree(stmtListExpr, codeExpr.getSourceRef());
		return stmtListExpr;
	}

	/**
	 * @param implCode
	 * @param sourceRef
	 */
	private void setSourceInTree(IAstNode node, ISourceRef sourceRef) {
		if (node.getSourceRef() == null)
			node.setSourceRef(sourceRef);
		for (IAstNode kid : node.getChildren()) {
			setSourceInTree(kid, sourceRef);
		}
	}

	/**
	 * @param stmts
	 * @param symbolExpr
	 * @param realArg
	 */
	private void replaceInTree(IAstNode root,
			IAstSymbolExpr symbolExpr, IAstNode replacement) {
		for (IAstNode kid : root.getChildren()) {
			replaceInTree(kid, symbolExpr, replacement);
		}
		if (root.equals(symbolExpr)) {
			//if (root.getParent() instanceof IAstFuncCallExpr)
			//	root.getParent().getParent().replaceChild(root.getParent(), replacement.copy(null));
			//else
				root.getParent().replaceChild(root, replacement.copy(null));
		} 
		
	}

}
