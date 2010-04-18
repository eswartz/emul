/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;

import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.Message;
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

/**
 * TODO: unify with TypeInference; these both contain a lot of duplicate logic for ensuring
 * that we get an accurate idea of a function call's type
 * 
 * @author ejs
 *
 */
public class ExpandAST {
	
	public ExpandAST() {
	}

	public boolean expand(List<Message> messages, IAstNode node) {
		boolean changed = doExpand(messages, node);
		
		
		if (changed) {
			System.out.println("after expansion:");
			DumpAST dump = new DumpAST(System.out);
			node.accept(dump);
		}
		return changed;
	}

	private boolean doExpand(List<Message> messages, IAstNode node) {
		boolean changed = false;
		
		if (!(node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())) {
			// go deep first, since node parenting changes as expansion occurs
			IAstNode[] kids = node.getChildren();
			for (int i = 0; i < kids.length; i++) {
				changed |= doExpand(messages, kids[i]);
			}
		}
		
		try {
			if (node instanceof IAstSymbolExpr) {
				IAstSymbolExpr symExpr = (IAstSymbolExpr)node;
				IAstNode symDef = symExpr.getDefinition();
				if (symDef == null) {
					// handle later
					//if (!symExpr.getSymbol().getScope().encloses(node.getOwnerScope()))
					//	throw new ASTException(node, "no definition found for " + symExpr.getSymbol().getName());
					return false;
				}
				if (symDef == node.getParent() || !(symDef instanceof IAstDefineStmt))
					return false;
				
				IAstTypedExpr value = symExpr.getInstance();
				if (value != null) {
					
					if (value instanceof IAstCodeExpr) {
						IAstCodeExpr codeExpr = (IAstCodeExpr) value;
						if (codeExpr.isMacro()) {
							// directly replace
							IAstNode copy = value.copy(node);
							copy.uniquifyIds();
							removeGenerics(copy);
							node.getParent().replaceChild(node, copy);
							changed = true;
						}
					}
					else {
						// directly replace
						IAstNode copy = value.copy(node);
						copy.uniquifyIds();
						removeGenerics(copy);
						node.getParent().replaceChild(node, copy);
						changed = true;
					}
				}
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
					
					IAstTypedExpr value = symExpr.getInstance();
					if (value != null) {
						if (value instanceof IAstCodeExpr) {
							IAstCodeExpr codeExpr = (IAstCodeExpr) value;
							if (codeExpr.isMacro()) {
								// "call" it
								IAstNode copy = value.copy(node);
								copy.uniquifyIds();
								removeGenerics(copy);
								funcCallExpr.replaceChild(funcExpr, copy);
								funcCallExpr.getFunction().setType(codeExpr.getType());
								changed = true;
							}
						}
						
					}
					
				}
				else if (funcExpr instanceof IAstCodeExpr) {
					// Direct expansion of call, e.g.:   code () { } ()
					//
					// Replace the arguments and statements in place of the call.
					//
					// (We may have produced this ourselves.)
					
					IAstNode copy = funcExpr.copy(funcCallExpr);
					copy.uniquifyIds();
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
		
		IAstNodeList<IAstStmt> blockList = new AstNodeList<IAstStmt>(IAstStmt.class);
		
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
				IAstNodeList<IAstStmt> stmtlist = new AstNodeList<IAstStmt>(IAstStmt.class);
				
				IAstTypedExpr retVal = (IAstTypedExpr) realArg.copy(null);
				retVal.uniquifyIds();
				if (!protoArg.isMacro())
					stmtlist.add(new AstReturnStmt(retVal));
				else
					stmtlist.add(new AstExprStmt(retVal));
				IAstCodeExpr implCode = new AstCodeExpr(new AstPrototype(argCode.getRetType()), new LocalScope(nodeScope), stmtlist, 
						protoArg.isMacro());
				
				setSourceInTree(implCode, realArg.getSourceRef());
				realArg = implCode;
			}
			
			if (!protoArg.isMacro()) {
				realArg.setParent(null);	// deleting call
				IAstSymbolExpr symCopy = protoArg.getSymbolExpr();
				symCopy.setParent(null);
				//symCopy.uniquifyIds();
				IAstType typeExprCopy = protoArg.getTypeExpr();
				if (typeExprCopy != null) {
					typeExprCopy.setParent(null);
					//typeExprCopy.uniquifyIds();
				}
				
				IAstNodeList<IAstSymbolExpr> idList = AstNodeList.<IAstSymbolExpr>singletonList(
						IAstSymbolExpr.class, symCopy);
				IAstNodeList<IAstTypedExpr> exprList = AstNodeList.<IAstTypedExpr>singletonList(
						IAstTypedExpr.class, realArg);
				
				IAstAllocStmt argAlloc = new AstAllocStmt(
						idList, 
						typeExprCopy,
						exprList, 
						false);
				blockList.add(realArgIdx++, argAlloc);
			} else {
				// For macro arguments, the actual argument is directly replaced
				if (realArg instanceof IAstSymbolExpr) {
					IAstNode rootDef = protoArg.getSymbolExpr().getSymbol().getDefinition();
					assert rootDef != null;
					((IAstSymbolExpr)realArg).getSymbol().setDefinition(rootDef);
				}
				replaceInTree(codeExpr.stmts(), protoArg.getSymbolExpr(), realArg);
			}
		}
		
		for (IAstStmt stmt : codeExpr.stmts().list()) {
			
			stmt.setParent(null);
			blockList.add(stmt);
		}
		
		// replace invoke with reference to self
		
		IAstStmtListExpr stmtListExpr = new AstStmtListExpr(/*returnValSymExpr,*/ blockList);
		setSourceInTree(stmtListExpr, codeExpr.getSourceRef());
		return stmtListExpr;
	}

	private void setSourceInTree(IAstNode node, ISourceRef sourceRef) {
		if (node.getSourceRef() == null)
			node.setSourceRef(sourceRef);
		for (IAstNode kid : node.getChildren()) {
			setSourceInTree(kid, sourceRef);
		}
	}

	private void replaceInTree(IAstNode root,
			IAstSymbolExpr symbolExpr, IAstNode replacement) throws ASTException {
		for (IAstNode kid : root.getChildren()) {
			replaceInTree(kid, symbolExpr, replacement);
		}
		if (root.equals(symbolExpr)) {
			IAstNode copy = replacement.copy(null);
			copy.uniquifyIds();
			
			try {
				root.getParent().replaceChild(root, copy);
			} catch (ClassCastException e) {
				throw new ASTException(replacement, "cannot macro-substitute an argument of this syntax type in place of " + symbolExpr.getSymbol().getName());
			}
		} 
		
	}

	/**
	 * Ensure that the tree is proper.  Whether or not any macros were expanded, we need
	 * to validate against undefined symbol references (which were allowed up til now
	 * in case they were variables defined by macros).
	 * @param node
	 */
	public void validate(List<Message> messages, IAstNode node)  {
		if (node instanceof IAstScope) {
			validateScope(messages, (IAstScope) node);
		}
		for (IAstNode kid : node.getChildren())
			validate(messages, kid);
	}

	private void validateScope(List<Message> messages, IAstScope node) {
		for (ISymbol symbol : node.getScope()) {
			if (symbol.getDefinition() == null) {
				messages.add(new Error(node, "undefined symbol '" + symbol.getName() + "'"));
			}
		}
	}

}
