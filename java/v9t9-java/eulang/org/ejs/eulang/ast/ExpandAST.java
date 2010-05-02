/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstDefineStmt;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstNamedType;
import org.ejs.eulang.ast.impl.AstNode;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstStmtListExpr;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLGenericType;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLUpType;

/**
 * TODO: unify with TypeInference; these both contain a lot of duplicate logic for ensuring
 * that we get an accurate idea of a function call's type
 * 
 * @author ejs
 *
 */
public class ExpandAST {
	
	private static final LLType ALL_GENERICS = new LLGenericType(null);
	private final TypeEngine typeEngine;
	private final boolean onlyInstances;
	private boolean changed;

	private List<IAstTypedExpr> expansions = new ArrayList<IAstTypedExpr>();
	private Map<LLType, LLType> globalTypeReplacementMap = new HashMap<LLType, LLType>();


	public ExpandAST(TypeEngine typeEngine, boolean onlyInstances) {
		this.typeEngine = typeEngine;
		this.onlyInstances = onlyInstances;
	}

	public IAstNode expand(List<Message> messages, IAstNode node) {
		this.changed = false;
		IAstNode root = doExpand(messages, node, Collections.<ISymbol, IAstNode>emptyMap());
		

		// handle any leftover types
		AstNode.replaceTypesInTree(typeEngine, root, globalTypeReplacementMap);
		
		if (changed) {
			System.out.println("after expansion:");
			DumpAST dump = new DumpAST(System.out);
			root.accept(dump);
		}
		return root;
	}
	
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}

	public IAstNode expand(List<Message> messages, IAstNode node, Map<ISymbol, IAstNode> replacementMap) {
		this.changed = false;
		IAstNode root = doExpand(messages, node, replacementMap);
		
		// handle any leftover types
		AstNode.replaceTypesInTree(typeEngine, root, globalTypeReplacementMap);
		
		if (changed) {
			System.out.println("after expansion:");
			DumpAST dump = new DumpAST(System.out);
			root.accept(dump);
		}
		return root;
	}
	private IAstNode doExpand(List<Message> messages, IAstNode node, Map<ISymbol, IAstNode> replacementMap) {
		if (!(node instanceof IAstCodeExpr && ((IAstCodeExpr) node).isMacro())
				&& !(node instanceof IAstInstanceExpr)
				&& !(node instanceof IAstDefineStmt && ((IAstDefineStmt) node).isGeneric())) {
			// go deep first, since node parenting changes as expansion occurs
			IAstNode[] kids = node.getChildren();
			for (int i = 0; i < kids.length; i++) {
				doExpand(messages, kids[i], replacementMap);
			}
		}
		
		if (node instanceof IAstDefineStmt && ((IAstDefineStmt) node).isGeneric()) {
			recordInstances((IAstDefineStmt) node);
		}
		try {
			IAstNode newNode = node;
			if (node instanceof IAstInstanceExpr) {
				newNode = expandInstance(messages, (IAstInstanceExpr) node);
			}
			else if (!onlyInstances && node instanceof IAstSymbolExpr) {
				newNode = expandSymbolExpr(node, replacementMap);
				
			} else if (!onlyInstances && node instanceof IAstFuncCallExpr) {
				newNode = expandFuncCallExpr(messages, node);
			} 
			
			return newNode;
		} catch (ASTException e) {
			messages.add(new Error(e.getNode(), e.getMessage()));
			return node;
		}
		
	}

	/**
	 * @param node
	 */
	private void recordInstances(IAstDefineStmt define) {
		for (IAstTypedExpr body : define.bodyList()) {
			if (body.getType() != null && body.getType().isGeneric()) {
				Map<LLInstanceType, ISymbol> instanceMap = define.getInstanceMap(typeEngine, body.getType());
				for (Map.Entry<LLInstanceType, ISymbol> entry : instanceMap.entrySet()) {
					globalTypeReplacementMap.put(entry.getKey(), new LLUpType(entry.getValue(), 1, entry.getValue().getType()));
				}
			}
		}
				
	}

	/**
	 * @param node
	 */
	private void expandUnresolvedInstances(IAstDefineStmt define) {
		// finally, replace the effective type where it appears inside
		HashMap<LLType, LLType> typeReplacementMap = new HashMap<LLType, LLType>();

		for (IAstTypedExpr body : define.bodyList()) {
			Map<LLInstanceType, ISymbol> instanceMap = define.getInstanceMap(typeEngine, body.getType());
			for (Map.Entry<LLInstanceType, ISymbol> entry : instanceMap.entrySet()) {
				typeReplacementMap.put(entry.getKey(), entry.getValue().getType());
			}
		}
		
		for (IAstTypedExpr body : define.bodyList()) {
			Map<LLInstanceType, ISymbol> instanceMap = define.getInstanceMap(typeEngine, body.getType());
			
			for (Map.Entry<LLInstanceType, ISymbol> entry : instanceMap.entrySet()) {
				
				IAstTypedExpr instance = (IAstTypedExpr) entry.getValue().getDefinition();
				
				if (containsInstanceTypes(instance)) {
					System.out.println("before replacing LLInstanceType:");
					DumpAST dump = new DumpAST(System.out);
					instance.accept(dump);
					
					if (AstNode.replaceTypesInTree(typeEngine, instance, typeReplacementMap)) {
						System.out.println("after replacing LLInstanceType:");
						instance.accept(dump);
					}
				}
			}
		}
		
		for (IAstTypedExpr expansion : expansions) {

			if (containsInstanceTypes(expansion)) {
				System.out.println("before replacing LLInstanceType:");
				DumpAST dump = new DumpAST(System.out);
				expansion.accept(dump);
				
				if (AstNode.replaceTypesInTree(typeEngine, expansion, typeReplacementMap)) {
					System.out.println("after replacing LLInstanceType:");
					expansion.accept(dump);
				}
			}
		}
	}

	/**
	 * @param instance
	 * @return
	 */
	private boolean containsInstanceTypes(IAstNode node) {
		if (node instanceof IAstTypedNode) {
			LLType type = ((IAstTypedNode) node).getType();
			if (containsInstanceTypes(type))
				return true;
		}
		if (node instanceof IAstSymbolExpr) {
			LLType type = ((IAstSymbolExpr) node).getSymbol().getType();
			if (containsInstanceTypes(type))
				return true;
		}
		return false;
	}

	/**
	 * @param type
	 * @return
	 */
	private boolean containsInstanceTypes(LLType type) {
		if (type == null)
			return false;
		if (type instanceof LLInstanceType)
			return true;
		for (LLType kid : type.getTypes()) {
			if (containsInstanceTypes(kid))
				return true;
		}
		return false;
	}

	/**
	 * @param node
	 * @return
	 * @throws ASTException 
	 */
	private IAstNode expandInstance(List<Message> messages, IAstInstanceExpr instanceExpr) throws ASTException {
		IAstDefineStmt defineStmt = instanceExpr.getSymbolExpr().getDefinition();
		if (defineStmt == null) {
			throw new ASTException(instanceExpr.getSymbolExpr(), "can only instantiate definitions");
		}
		
		IAstTypedExpr body = defineStmt.getMatchingBodyExpr(instanceExpr.getType());
		if (body == null) 
			throw new ASTException(instanceExpr.getSymbolExpr(), 
					"could not find matching body for instance");
		
		ISymbol instanceSymbol = defineStmt.getInstanceForParameters(
				typeEngine, body.getType(), instanceExpr.getExprs().list());
		
		IAstTypedExpr expansion = (IAstTypedExpr) instanceSymbol.getDefinition();
		
		IAstSymbolExpr symbolExpr = new AstSymbolExpr(instanceSymbol);
		symbolExpr.setSourceRef(instanceExpr.getSourceRef());
		
		IAstTypedExpr ret = null;
		IAstNode parent;
		try {
			// try plain AST node
			parent = instanceExpr.getParent();
			instanceExpr.getParent().replaceChild(instanceExpr, symbolExpr);
			ret = symbolExpr;
		} catch (ClassCastException e) {
			// it's probably a type
			IAstType typeExpr = new AstNamedType(symbolExpr.getType(), symbolExpr);
			typeExpr.setSourceRef(instanceExpr.getSourceRef());
			parent = instanceExpr.getParent();
			instanceExpr.getParent().replaceChild(instanceExpr, typeExpr);
			ret = typeExpr;
		}
		
		// TODO: some type-holding nodes like IAstPointerType reset their own type
		// when we change the child; but for this case we know what is wanted
		if (parent instanceof IAstTypedNode)
			((IAstTypedNode) parent).setType(instanceSymbol.getType());
		
		// recursively expand
		IAstTypedExpr recursiveExpansion = (IAstTypedExpr) doExpand(messages, expansion, Collections.<ISymbol, IAstNode>emptyMap());
		if (recursiveExpansion != expansion) {
			expansion.getParent().replaceChild(expansion, recursiveExpansion);
		}
		
		changed = true;

		expansions.add(ret);
		
		return ret;
	}

	public ISymbol expandInstance(AstDefineStmt define, ISymbol symbol, IAstTypedExpr body, ISymbol[] varSymbols, List<IAstTypedExpr> instanceExprs)
			throws ASTException {
		IAstTypedExpr instance = (IAstTypedExpr) body.copy(null);

		Map<LLType, LLType> typeReplacementMap = new HashMap<LLType, LLType>();
		LLType[] types = new LLType[varSymbols.length];
		int index = 0;
		for (IAstTypedExpr expr : instanceExprs) {
			ISymbol vsymbol = varSymbols[index];
			typeReplacementMap.put(vsymbol.getType(), expr.getType());
			
			types[index] = expr.getType();
			
			// replace contents
			replaceInTree(instance, vsymbol, expr);
			
			index++;
		}
		
		
		// then replace types
		AstNode.replaceTypesInTree(typeEngine, instance, typeReplacementMap);

		

		ISymbol instanceSymbol = symbol.getScope().addTemporary(symbol.getName());
		instanceSymbol.setType(instance.getType());
		
		// replace self-refs to symbol
		ISymbol theSymbol = symbol;
		if (instance instanceof IAstDataType) {
			//theSymbol = ((IAstDataType) ((IAstDataType) body).getScope().getOwner()).getTypeName();
			((IAstDataType) instance).setTypeName(instanceSymbol);
		}
		
		AstNode.replaceSymbols(typeEngine, instance, theSymbol.getScope(), Collections.singletonMap(theSymbol.getNumber(), instanceSymbol));
		AstNode.replaceTypesInTree(typeEngine, instance, Collections.singletonMap(body.getType(), instance.getType()));
		instanceSymbol.setDefinition(instance);
		
		//if (instance instanceof IAstDataType) {
		//	((IAstDataType) instance).setTypeName(instanceSymbol);
		//}
		
		
		// finally, replace the effective type where it appears inside
		typeReplacementMap.clear();
		LLInstanceType instanceType = typeEngine.getInstanceType(symbol, types);
		typeReplacementMap.put(instanceType, new LLUpType(instanceSymbol, 1, instance.getType()));
		LLSymbolType symbolType = new LLSymbolType(symbol);
		typeReplacementMap.put(symbolType, new LLUpType(instanceSymbol, 1, instance.getType()));

//		Map<LLInstanceType, ISymbol> instanceMap = define.getInstanceMap(typeEngine, body.getType()); 
//		for (Map.Entry<LLInstanceType, ISymbol> entry : instanceMap.entrySet()) {
//			typeReplacementMap.put(entry.getKey(), entry.getValue().getType());
//		}
//		
		System.out.println("before replacing LLSymbolType:");
		DumpAST dump = new DumpAST(System.out);
		instance.accept(dump);
		
		AstNode.replaceTypesInTree(typeEngine, instance, typeReplacementMap);
		

		instanceSymbol.setType(instance.getType());
		
		System.out.println("after replacing LLSymbolType:");
		instance.accept(dump);
		
		
		//removeGenerics(bodyCopy);
		
		instance.uniquifyIds();

		return instanceSymbol;
	}

	/**
	 * @param copy
	 */
	private void removeGenerics(IAstNode node) {
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typedNode = (IAstTypedNode) node;
			LLType type = typedNode.getType();
			if (type != null) {
				LLType noGeneric = type.substitute(typeEngine, ALL_GENERICS, null);
				if (noGeneric != type)
					typedNode.setType(noGeneric);
			}
			if (typedNode instanceof IAstSymbolExpr) {
				IAstSymbolExpr symbolExpr = (IAstSymbolExpr) typedNode;
				//if (node.getOwnerScope().encloses(symbolExpr.getSymbol().getScope()))
				ISymbol symbol = symbolExpr.getSymbol();
				LLType symbolType = symbol.getType();
				if (symbolType != null) {
					LLType noGeneric = symbolType.substitute(typeEngine, ALL_GENERICS, null);
					if (noGeneric != symbolType)
						symbol.setType(noGeneric);
				}
			}
		}
		for (IAstNode kid : node.getChildren()) {
			removeGenerics(kid);
		}
	}

	private IAstNode expandFuncCallExpr(List<Message> messages, IAstNode node) throws ASTException {
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
				return node;
			}
			if (symDef == node.getParent() /*|| !(symDef instanceof IAstDefineStmt)*/)
				return node;
			
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

			IAstStmtListExpr stmtListExpr  = doExpandFuncCallExpr(messages, funcCallExpr, funcCallExpr.arguments(),
					null,
					(IAstCodeExpr) copy,
					node.getOwnerScope());
			
			if (stmtListExpr != null) 
				funcCallExpr.getParent().replaceChild(funcCallExpr, stmtListExpr);
			else
				funcCallExpr.getParent().replaceChild(funcCallExpr, null);
			changed = true;
			return stmtListExpr;
		}
		return node;
	}

	private IAstNode expandSymbolExpr(IAstNode node,
			Map<ISymbol, IAstNode> replacementMap)
			throws ASTException {
		
		IAstNode value = null;
		IAstSymbolExpr symExpr = (IAstSymbolExpr)node;
		IAstNode symDef = symExpr.getDefinition();
		if (symDef == null) {
			value = replacementMap.get(symExpr.getSymbol());
			if (value == null)
				return node;
		} else {
			if (symDef == node.getParent() || !(symDef instanceof IAstDefineStmt))
				return node;
			
			value = symExpr.getInstance();
			if (value instanceof IAstType)
				return node;
		}
		
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
				try {
					node.getParent().replaceChild(node, copy);
					changed = true;
					return node;
				} catch (ClassCastException e) {
					throw new ASTException(copy, "cannot macro-substitute an argument of this syntax type in place of " + symExpr.getSymbol().getName());
				}
			}
		}
		return node;
	}
	
	/**
	 * Expand a function or macro into the tree 
	 * @param messages 
	 * @param node
	 * @param args 
	 * @param codeExpr copy of tree
	 * @param blockList 
	 * @param symDef
	 * @return node containing the return value, or <code>null</code>
	 */
	private IAstStmtListExpr doExpandFuncCallExpr(
			List<Message> messages, IAstNode node, IAstNodeList<IAstTypedExpr> args,
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
		if (args.nodeCount() < codeExpr.getPrototype().getDefaultArgumentIndex()) {
			throw new ASTException(args, "argument count does not match prototype " + codeExpr.getPrototype().toString());
		}
		IAstTypedExpr[] realArgs = args.getNodes(IAstTypedExpr.class);
		int realArgIdx = 0;
		
		Map<ISymbol, IAstNode> expandedArgs = new HashMap<ISymbol, IAstNode>();
		
		for (int i = 0; i < protoArgs.length; i++) {
			IAstArgDef protoArg = protoArgs[i];
			IAstTypedExpr realArg;
			if (i < realArgs.length) {
				realArg = realArgs[i];
				if (realArg instanceof IAstDerefExpr)
					realArg = ((IAstDerefExpr) realArg).getExpr();
				protoArg.getSymbolExpr().getSymbol().setDefinition(realArg);
				expandedArgs.put(protoArg.getSymbolExpr().getSymbol(), realArg);
			}
			else {
				realArg = (IAstTypedExpr) protoArg.getDefaultValue().copy(codeExpr);
				
				// allow defaults to reference other arguments
				doExpand(messages, realArg, expandedArgs);
			}
			
			
			// coerce expression argument to code if needed
			LLCodeType argCode = null;
			if (protoArg.getTypeExpr() != null && protoArg.getTypeExpr().getType() instanceof LLCodeType)
				argCode = ((LLCodeType) protoArg.getTypeExpr().getType());
			else if (protoArg.getDefaultValue() instanceof IAstCodeExpr) {
				IAstPrototype proto =((IAstCodeExpr) protoArg.getDefaultValue()).getPrototype(); 
				argCode = typeEngine.getCodeType(proto.returnType(), proto.argumentTypes());
			}
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
				replaceInTree(codeExpr.stmts(), protoArg.getSymbolExpr().getSymbol(), realArg);
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

	public void replaceInTree(IAstNode root,
			ISymbol symbol, IAstNode replacement) throws ASTException {
		for (IAstNode kid : root.getChildren()) {
			replaceInTree(kid, symbol, replacement);
		}
		if (root instanceof IAstSymbolExpr && ((IAstSymbolExpr) root).getSymbol().equals(symbol)) {
			//if (replacement instanceof IAstDerefExpr)
//				replacement = ((IAstDerefExpr) replacement).getExpr();
			
			IAstNode copy = replacement.copy(null);
			if (copy instanceof IAstTypedNode)
				((IAstTypedNode) copy).setType(((IAstTypedNode)replacement).getType());
			copy.uniquifyIds();
			
			try {
				// if a slot refers to a type, directly replace the type
				if (root.getParent() instanceof IAstNamedType)
					root.getParent().getParent().replaceChild(root.getParent(), copy);
				else
					root.getParent().replaceChild(root, copy);
			} catch (ClassCastException e) {
				throw new ASTException(replacement, "cannot macro-substitute an argument of this syntax type in place of " + symbol.getName());
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
