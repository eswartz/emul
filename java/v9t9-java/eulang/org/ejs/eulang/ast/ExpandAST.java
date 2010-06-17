/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstNamedType;
import org.ejs.eulang.ast.impl.AstNode;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstStmtListExpr;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLGenericType;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;

/**
 * TODO: unify with TypeInference; these both contain a lot of duplicate logic for ensuring
 * that we get an accurate idea of a function call's type
 * 
 * @author ejs
 *
 */
public class ExpandAST {

	public static boolean DUMP = false;
	
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

	public IAstNode expand(Collection<Message> messages, IAstNode node) {
		this.changed = false;
		IAstNode root = doExpand(messages, node, Collections.<ISymbol, IAstNode>emptyMap());
		

		// handle any leftover types
		boolean globalChanged = false;
		//do {
		globalChanged = AstNode.replaceTypesInTree(typeEngine, root, globalTypeReplacementMap);
		changed |= globalChanged;
		//globalChanged = AstNode.replaceTypesInTree(typeEngine, root, typeEngine.getInstanceToRealTypeMap());
		//changed |= globalChanged;
		//} while (globalChanged);
			
		//typeEngine.replaceTypes(globalTypeReplacementMap);	
		
		if (changed && DUMP) {
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
		
		if (changed && DUMP) {
			System.out.println("after expansion:");
			DumpAST dump = new DumpAST(System.out);
			root.accept(dump);
		}
		return root;
	}
	private IAstNode doExpand(Collection<Message> messages, IAstNode node, Map<ISymbol, IAstNode> replacementMap) {
		if (!(node instanceof IAstCodeExpr && ((IAstCodeExpr) node).hasAttr(IAstCodeExpr.MACRO))
				&& !(node instanceof IAstInstanceExpr)
				//&& !(node instanceof IAstDefineStmt && ((IAstDefineStmt) node).isGeneric())
				) {
			// go deep first, since node parenting changes as expansion occurs
			IAstNode[] kids = node.getChildren();
			for (int i = 0; i < kids.length; i++) {
				doExpand(messages, kids[i], replacementMap);
			}
		}
		
		if (node instanceof IAstDefineStmt) {
			IAstDefineStmt def = ((IAstDefineStmt) node);
			
			if (def.isGeneric()) {
				recordInstances(def);
			}
		}
		try {
			IAstNode newNode = node;
			if (node instanceof IAstInstanceExpr) {
				newNode = expandInstance(messages, (IAstInstanceExpr) node);
			}
			else if (node instanceof IAstSymbolExpr) {
				IAstSymbolExpr symbolExpr = (IAstSymbolExpr) node;
				
				if (!onlyInstances) {
					newNode = expandSymbolExpr(symbolExpr, replacementMap);
				}
				
			} else if (!onlyInstances && node instanceof IAstFuncCallExpr) {
				newNode = expandFuncCallExpr(messages, node);
			} 
			
			//replaceConcreteSymbolRefs(newNode);
			
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
					//globalTypeReplacementMap.put(entry.getKey(), new LLUpType(entry.getValue(), 1, entry.getValue().getType()));
					globalTypeReplacementMap.put(entry.getKey(), new LLSymbolType(entry.getValue()));
				}
			}
		}
	}
	/**
	 * @param node
	 * @return
	 * @throws ASTException 
	 */
	private IAstNode expandInstance(Collection<Message> messages, IAstInstanceExpr instanceExpr) throws ASTException {
		IAstDefineStmt defineStmt = instanceExpr.getSymbolExpr().getDefinition();
		if (defineStmt == null) {
			throw new ASTException(instanceExpr.getSymbolExpr(), "can only instantiate definitions");
		}
		
		IAstTypedExpr body;
		body = defineStmt.getMatchingBodyExpr(instanceExpr.getType());
		if (body == null)  {
			body = defineStmt.getMatchingBodyExpr(instanceExpr.getSymbolExpr().getType());
			if (body == null) {
				throw new ASTException(instanceExpr.getSymbolExpr(), 
						"could not find matching body for instance");
			}
		}
		
		ISymbol instanceSymbol;
		IAstTypedExpr expansion;
		if (defineStmt.isGeneric()) {
			boolean any = false;
			boolean allGeneric = true;
			for (IAstTypedExpr expr : instanceExpr.getExprs().list()) {
				any = true;
				if (expr.getType() != null && !expr.getType().isGeneric()) {
					allGeneric = false;
				}
			}
			
			if (any && allGeneric) {
				return instanceExpr;
			}
			instanceSymbol = defineStmt.getInstanceForParameters(
					typeEngine, body.getType(), instanceExpr.getExprs().list());
			expansion = (IAstTypedExpr) instanceSymbol.getDefinition();
		} else {
			instanceSymbol = defineStmt.getSymbol();
			expansion = defineStmt.getMatchingBodyExpr(instanceExpr.getType());
			if (expansion == null)
				return instanceExpr;
		}
		
		
		IAstSymbolExpr symbolExpr = new AstSymbolExpr(true, instanceSymbol);
		//symbolExpr.setOriginalSymbol(defineStmt.getSymbol());
		symbolExpr.setSourceRef(instanceExpr.getSourceRef());
		
		IAstTypedExpr ret = null;
		try {
			// try plain AST node
			instanceExpr.getParent().replaceChild(instanceExpr, symbolExpr);
			ret = symbolExpr;
		} catch (ClassCastException e) {
			// it's probably a type
			IAstType typeExpr = new AstNamedType(symbolExpr.getType(), symbolExpr);
			typeExpr.setSourceRef(instanceExpr.getSourceRef());
			instanceExpr.getParent().replaceChild(instanceExpr, typeExpr);
			ret = typeExpr;
		}
		
		// TODO: some type-holding nodes like IAstPointerType reset their own type
		// when we change the child; but for this case we know what is wanted
		//if (parent instanceof IAstTypedNode && !(parent instanceof IAstFuncCallExpr))
		//	((IAstTypedNode) parent).setType(instanceSymbol.getType());
		
		// recursively expand
		IAstTypedExpr recursiveExpansion = (IAstTypedExpr) doExpand(messages, expansion, Collections.<ISymbol, IAstNode>emptyMap());
		if (recursiveExpansion != expansion) {
			expansion.getParent().replaceChild(expansion, recursiveExpansion);
		}
		
		changed = true;

		expansions.add(ret);
		
		return ret;
	}

	public ISymbol expandInstance(ISymbol symbol, IAstTypedExpr body, ISymbol[] varSymbols, List<IAstTypedExpr> instanceExprs)
			throws ASTException {
		DumpAST dump = new DumpAST(System.out);
		
		IAstTypedExpr instance = (IAstTypedExpr) body.copy();

		String paramStr = "";
		int idx = 0;
		for (IAstTypedExpr expr : instanceExprs)
			paramStr += (idx <varSymbols.length ? varSymbols[idx].getName() + "." + varSymbols[idx].getNumber() : "?") + "="+ DumpAST.dumpString(expr)+ " ";
		
		if (DUMP) {
			System.out.println("Before expanding generic of " + symbol + " for type " + body.getType() + " with " + paramStr + ":");
			instance.accept(dump);
		}
		
		Map<LLType, LLType> typeReplacementMap = new HashMap<LLType, LLType>();
		LLType[] types = new LLType[varSymbols.length];
		
		int index = 0;
		for (IAstTypedExpr expr : instanceExprs) {
			if (true) {
				ISymbol vsymbol = varSymbols[index];
				typeReplacementMap.put(vsymbol.getType(), expr.getType());
				
				types[index] = expr.getType();
				
				// replace contents
				replaceInTree(instance, vsymbol, expr);
			}			
			index++;
		}
		
		// then replace types
		AstNode.replaceTypesInTree(typeEngine, instance, typeReplacementMap);

		

		ISymbol instanceSymbol = symbol.getScope().addTemporary(symbol.getName());
		instanceSymbol.setType(instance.getType());
		
		// replace self-refs to symbol
		ISymbol theSymbol = symbol;
		if (instance instanceof IAstDataType) {
			((IAstDataType) instance).setTypeName(instanceSymbol);
		}
		
		if (DUMP) {
			System.out.println("before expanding types/symbols:");
			instance.accept(dump);
		}
		
		AstNode.replaceSymbols(typeEngine, instance, theSymbol.getScope(), Collections.singletonMap(theSymbol.getNumber(), instanceSymbol));
		AstNode.replaceTypesInTree(typeEngine, instance, Collections.singletonMap(body.getType(), instance.getType()));
		instanceSymbol.setDefinition(instance);

		
		// finally, replace the effective type where it appears inside
		
		typeReplacementMap.clear();
		LLInstanceType instanceType = typeEngine.getInstanceType(symbol, types);
		typeEngine.registerInstanceType(instanceType, instance.getType());
		
		typeReplacementMap.put(instanceType, new LLSymbolType(instanceSymbol));

		if (DUMP) {
			System.out.println("before replacing LLSymbolType:");
			instance.accept(dump);
		}
		AstNode.replaceTypesInTree(typeEngine, instance, typeReplacementMap);

		// this type may be different now 
		instanceSymbol.setType(instance.getType());
		
		if (DUMP) {
			System.out.println("After expanding generic of " + symbol + " as " + instanceSymbol + ":");
			dump = new DumpAST(System.out);
			instance.accept(dump);
		}
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

	private IAstNode expandFuncCallExpr(Collection<Message> messages, IAstNode node) throws ASTException {
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
					if (codeExpr.hasAttr(IAstCodeExpr.MACRO)) {
						// "call" it
						IAstNode copy = value.copy();
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
			
			IAstNode copy = funcExpr.copy();
			copy.uniquifyIds();
			removeGenerics(copy);

			IAstStmtListExpr stmtListExpr  = doExpandFuncCallExpr(messages, funcCallExpr, funcCallExpr.arguments(),
					(IAstCodeExpr) copy);
			
			if (stmtListExpr != null) 
				funcCallExpr.getParent().replaceChild(funcCallExpr, stmtListExpr);
			else
				funcCallExpr.getParent().replaceChild(funcCallExpr, null);
			changed = true;
			return stmtListExpr;
		}
		return node;
	}

	private IAstNode expandSymbolExpr(IAstSymbolExpr symExpr,
			Map<ISymbol, IAstNode> replacementMap)
			throws ASTException {
		
		IAstNode value = null;
		IAstNode symDef = symExpr.getDefinition();
		if (symDef == null) {
			value = replacementMap.get(symExpr.getSymbol());
			if (value == null)
				return symExpr;
		} else {
			if (symDef == symExpr.getParent() || !(symDef instanceof IAstDefineStmt))
				return symExpr;
			
			value = symExpr.getInstance();
			if (value instanceof IAstType)
				return symExpr;
		}
		
		if (value != null) {
			
			if (value instanceof IAstCodeExpr) {
				IAstCodeExpr codeExpr = (IAstCodeExpr) value;
				if (codeExpr.hasAttr(IAstCodeExpr.MACRO)) {
					// directly replace
					IAstNode copy = value.copy();
					copy.uniquifyIds();
					removeGenerics(copy);
					symExpr.getParent().replaceChild(symExpr, copy);
					changed = true;
				}
			}
			else {
				// directly replace
				
				IAstNode copy = value.copy();
				copy.uniquifyIds();
				removeGenerics(copy);
				try {
					symExpr.getParent().replaceChild(symExpr, copy);
					changed = true;
					return symExpr;
				} catch (ClassCastException e) {
					throw new ASTException(copy, "cannot macro-substitute an argument of this syntax type in place of " + symExpr.getSymbol().getName());
				}
			}
		}
		return symExpr;
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
			Collection<Message> messages, IAstNode node, IAstNodeList<IAstTypedExpr> args,
			IAstCodeExpr codeExpr
			) throws ASTException {
		
		// get the scope into which new temporaries go
		IScope nodeScope = node.getOwnerScope();
		if (nodeScope == null) {
			throw new ASTException(node, "no scope found");
		}
		
		IAstNodeList<IAstStmt> blockList = new AstNodeList<IAstStmt>(IAstStmt.class);
		
		// Substitute arguments
		IAstArgDef[] protoArgs = codeExpr.getPrototype().argumentTypes();
		
		int defaultArgIndex = 0;
		while (defaultArgIndex < protoArgs.length) {
			if (protoArgs[defaultArgIndex].getDefaultValue() != null
					|| protoArgs[defaultArgIndex].hasAttr(IAttrs.LIST))
				break;
			defaultArgIndex++;
		}
		
		if (args.nodeCount() < defaultArgIndex) {
			throw new ASTException(args, "argument count does not match prototype " + codeExpr.getPrototype().toString());
		}
		IAstTypedExpr[] realArgs = args.getNodes(IAstTypedExpr.class);
		int realArgIdx = 0;
		
		Map<ISymbol, IAstNode> expandedArgs = new HashMap<ISymbol, IAstNode>();
		
		for (int i = 0; i < protoArgs.length; i++) {
			IAstArgDef protoArg = protoArgs[i];
			IAstNode realArg;
			if (i < realArgs.length) {
				realArg = realArgs[i];
				
				ISymbol argSym = codeExpr.getScope().get(protoArg.getName());
				assert argSym != null;
				
				if (protoArg.hasAttr(IAttrs.LIST)) {
					if (!protoArg.hasAttr(IAttrs.MACRO))
						throw new ASTException(protoArg, "#list only valid on #macro arguments");
					
					IAstNodeList<IAstNode> argList = new AstNodeList<IAstNode>(IAstNode.class);
					argList.add(realArg.copy());
					while (++i < realArgs.length) {
						realArg = realArgs[i];
						argList.add(realArg.copy());
					}
					realArg = argList;
				} else {
					if (realArg instanceof IAstDerefExpr)
						realArg = ((IAstDerefExpr) realArg).getExpr();
				}
				argSym.setDefinition(realArg);
				
				expandedArgs.put(argSym, realArg);
			}
			else {
				if (protoArg.hasAttr(IAttrs.LIST))
					realArg = new AstNodeList<IAstNode>(IAstNode.class);
				else
					realArg = protoArg.getDefaultValue().copy();
				
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
				
				IAstTypedExpr retVal = (IAstTypedExpr) realArg.copy();
				retVal.uniquifyIds();
				if (!protoArg.hasAttr(IAstCodeExpr.MACRO))
					stmtlist.add(new AstReturnStmt(retVal));
				else
					stmtlist.add(new AstExprStmt(retVal));
				
				Set<String> attrs = new HashSet<String>(codeExpr.getAttrs());
				if (protoArg.hasAttr(IAstCodeExpr.MACRO))
					attrs.add(IAttrs.MACRO);
				IAstCodeExpr implCode = new AstCodeExpr(new AstPrototype(argCode, null, null), 
						new LocalScope(nodeScope), stmtlist, 
						attrs);
				
				implCode.setSourceRefTree(realArg.getSourceRef());
				realArg = implCode;
			}
			
			if (!protoArg.hasAttr(IAstCodeExpr.MACRO)) {
				realArg.setParent(null);	// deleting call
				IAstSymbolExpr symCopy = new AstSymbolExpr(true, codeExpr.getScope().get(protoArg.getName()));
				symCopy.setSourceRef(realArg.getSourceRef());
				IAstType typeExprCopy = protoArg.getTypeExpr();
				if (typeExprCopy != null) {
					typeExprCopy.setParent(null);
				}
				
				IAstNodeList<IAstSymbolExpr> idList = AstNodeList.<IAstSymbolExpr>singletonList(
						IAstSymbolExpr.class, symCopy);
				IAstNodeList<IAstTypedExpr> exprList = AstNodeList.<IAstTypedExpr>singletonList(
						IAstTypedExpr.class, (IAstTypedExpr) realArg);
				
				IAstAllocStmt argAlloc = new AstAllocStmt(
						idList, 
						typeExprCopy,
						exprList, 
						false,
						Collections.<String>emptySet());
				blockList.add(realArgIdx++, argAlloc);
			} else {
				// For macro arguments, the actual argument is directly replaced
				ISymbol argSym = codeExpr.getScope().get(protoArg.getName());
				if (realArg instanceof IAstSymbolExpr) {
					// define from argument if not a known symbol ref
					if (((IAstSymbolExpr) realArg).getSymbol().getDefinition() == null) {
						IAstNode rootDef = argSym.getDefinition();
						assert rootDef != null;
						((IAstSymbolExpr)realArg).getSymbol().setDefinition(rootDef);
					}
				}
				replaceInTree(codeExpr.stmts(), argSym, realArg);
			}
		}
		
		for (IAstStmt stmt : codeExpr.stmts().list()) {
			
			stmt.setParent(null);
			blockList.add(stmt);
		}
		
		// replace invoke with reference to self
		
		IAstStmtListExpr stmtListExpr = new AstStmtListExpr(/*returnValSymExpr,*/ blockList);
		stmtListExpr.setSourceRefTree(codeExpr.getSourceRef());
		return stmtListExpr;
	}

	@SuppressWarnings("unchecked")
	public void replaceInTree(IAstNode root,
			ISymbol symbol, IAstNode replacement) throws ASTException {
		for (IAstNode kid : root.getChildren()) {
			replaceInTree(kid, symbol, replacement);
		}
		if (root instanceof IAstSymbolExpr && ((IAstSymbolExpr) root).getSymbol().equals(symbol)) {
			IAstNode copy = replacement.copy();
			copy.uniquifyIds();
			
			if (copy instanceof IAstNodeList) {
				if (!(root.getParent() instanceof IAstNodeList)) {
					throw new ASTException(root, "cannot reference #list argument outside of a list");
				}
				
				IAstNodeList<IAstNode> parentList = (IAstNodeList) root.getParent();
				int pos = parentList.list().indexOf(root);
				assert pos >= 0;
				
				boolean first = true;
				for (IAstNode node : ((IAstNodeList<IAstNode>) copy).list()) {
					node.setParent(null);
					if (first) {
						try {
							parentList.replaceChild(root, node);
						} catch (ClassCastException e) {
							throw new ASTException(node, "cannot macro-substitute an argument of this syntax type in place of " + symbol.getName());
						}
						first = false;
					} else {
						parentList.add(pos, node);
					}
					pos++;
				}
				if (first) {
					// no args
					parentList.remove(root);
				}
			} else {
				replaceNode(root, symbol, replacement, copy);
			}
		} 
		
	}

	private void replaceNode(IAstNode root, ISymbol symbol,
			IAstNode replacement, IAstNode copy) throws ASTException {
		if (copy instanceof IAstTypedNode)
			((IAstTypedNode) copy).setType(((IAstTypedNode)replacement).getType());
		
		
		try {
			root.getParent().replaceChild(root, copy);
		} catch (ClassCastException e) {
			// if a slot refers to a type, directly replace the type
			try {
				if (root.getParent() instanceof IAstNamedType)
					root.getParent().getParent().replaceChild(root.getParent(), copy);
				else
					throw e;
			} catch (ClassCastException e2) {
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
