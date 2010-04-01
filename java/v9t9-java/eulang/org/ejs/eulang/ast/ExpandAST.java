/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstAssignStmt;
import org.ejs.eulang.ast.impl.AstBlockStmt;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstStmtListExpr;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.LLCodeType;

/**
 * @author ejs
 *
 */
public class ExpandAST {
	
	public ExpandAST() {
		
	}

	@SuppressWarnings("unchecked")
	public boolean expand(List<Message> messages, IAstNode node) {
		boolean changed = false;
		
		IAstNode[] kids = node.getChildren();
		for (int i = 0; i < kids.length; i++) {
			changed |= expand(messages, kids[i]);
		}
		
		try {
			if (node instanceof IAstSymbolExpr) {
				IAstSymbolExpr symExpr = (IAstSymbolExpr)node;
				IAstNode symDef = symExpr.getSymbol().getDefinition();
				if (symDef == null)
					throw new ASTException(node, "no definition found for " + symExpr.getSymbol().getName());
				if (symDef == node.getParent())
					return false;
				
				if (symDef instanceof IAstDefineStmt) {
					IAstNode value = ((IAstDefineStmt) symDef).getExpr();
					node.getParent().replaceChild(node, value.copy(node));
				}
				
				/*
				if (symDef instanceof IAstDefineStmt && ((IAstDefineStmt) symDef).getExpr() instanceof IAstCodeExpr) {
					IAstCodeExpr symDefCode = (IAstCodeExpr) ((IAstDefineStmt) symDef).getExpr();
					
				}
				*/
			} else if (node instanceof IAstFuncCallExpr) {
				IAstFuncCallExpr funcCallExpr = (IAstFuncCallExpr) node;
				IAstTypedExpr funcExpr = funcCallExpr.getFunction();
				if (funcExpr instanceof IAstCodeExpr) {
					// Direct expansion of call, e.g.:   code () { } () 
					// We may have produced this ourselves.
					
					IAstStmtListExpr stmtListExpr  = doExpandFuncCallExpr(funcCallExpr, funcCallExpr.arguments(),
							null,
							(IAstCodeExpr) funcExpr.copy(funcCallExpr),
							node.getOwnerScope());
					
					//if (!(node.getParent() instanceof IAstStmt))
					//	throw new ASTException(node, "cannot replace macro expansion except in statement (yet)");
					
					/*
					IAstNodeList<IAstStmt> topStmtList = null;
					int nodeStmtIdx = -1;
					IAstNode stmtParent = node;
					while (stmtParent != null) {
						if (stmtParent.getParent() instanceof IAstNodeList) {
							topStmtList = (IAstNodeList<IAstStmt>) stmtParent.getParent();
							nodeStmtIdx = topStmtList.list().indexOf(stmtParent);
							break;
						}
						stmtParent = stmtParent.getParent();
					}
					if (topStmtList == null)
						throw new ASTException(node, "cannot find function into which to replace macro expansion");
					
					for (IAstStmt stmt : blockList.list()) {
						stmt.setParent(null);
						topStmtList.add(nodeStmtIdx++, stmt);
					}
					blockList.list().clear();
					*/
					
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
		for (ISymbol sym : codeExpr.getScope()) {
			// this refers to the copied definition
			sym.setTemporary(true);
			sym.setScope(null);
			parentScope.add(sym);
		}
		
		// Substitute arguments
		IAstArgDef[] protoArgs = codeExpr.getPrototype().argumentTypes();
		if (protoArgs.length != args.nodeCount()) {
			// TODO: default values...
			throw new ASTException(args, "argument count does not match prototype " + codeExpr.getPrototype().toString());
		}
		IAstTypedExpr[] realArgs = args.getNodes(IAstTypedExpr.class);
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
				IAstAllocStmt argAlloc = new AstAllocStmt(
						protoArg.getSymbolExpr(), 
						protoArg.getTypeExpr(),
						realArg);
				blockList.add(i, argAlloc);
			} else {
				// For macro arguments, the actual argument is directly replaced
				replaceInTree(codeExpr.stmts(), protoArg.getSymbolExpr(), realArg);
			}
		}
		
		IAstAllocStmt allocReturnStmt = null;
		IAstSymbolExpr returnValSymExpr = null;
		ISymbol returnValSym = null;
		for (IAstStmt stmt : codeExpr.stmts().list()) {
			// when inlining functions, replace returns
			if (!codeExpr.isMacro() && stmt instanceof IAstReturnStmt) {
				IAstReturnStmt retStmt = (IAstReturnStmt) stmt;
				
				if (retStmt.getExpr() == null)
					continue;
				
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
				
				continue;
			}
			
			stmt.setParent(null);
			blockList.add(stmt);
		}
		codeExpr.stmts().list().clear();
		
		IAstStmtListExpr stmtListExpr = new AstStmtListExpr(returnValSymExpr, blockList);
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
