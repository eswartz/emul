/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstArgDef;
import org.ejs.eulang.ast.impl.AstAssignStmt;
import org.ejs.eulang.ast.impl.AstBinExpr;
import org.ejs.eulang.ast.impl.AstBlockStmt;
import org.ejs.eulang.ast.impl.AstBoolLitExpr;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstCondExpr;
import org.ejs.eulang.ast.impl.AstCondList;
import org.ejs.eulang.ast.impl.AstDefineStmt;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstFloatLitExpr;
import org.ejs.eulang.ast.impl.AstFuncCallExpr;
import org.ejs.eulang.ast.impl.AstGotoStmt;
import org.ejs.eulang.ast.impl.AstIntLitExpr;
import org.ejs.eulang.ast.impl.AstInvokeExpr;
import org.ejs.eulang.ast.impl.AstLabelStmt;
import org.ejs.eulang.ast.impl.AstModule;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstNullLitExpr;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.ast.impl.AstUnaryExpr;
import org.ejs.eulang.ast.impl.SourceRef;
import org.ejs.eulang.ast.impl.TokenSourceRef;
import org.ejs.eulang.parser.EulangParser;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.LLType;


/**
 * Transform from the syntax tree to an AST with proper node types, type information, and
 * source references.
 * @author ejs
 *
 */
public class GenerateAST {
	static class GenerateException extends Exception {
		private static final long serialVersionUID = -2510488670733387859L;
		private Tree tree;
		private ISourceRef ref;
		
		public GenerateException(Tree tree, String msg) {
			super(msg);
			this.tree = tree;
		}
		public GenerateException(ISourceRef ref, String msg) {
			super(msg);
			this.ref = ref;
		}
		
		public Tree getTree() {
			return tree;
		}

		public ISourceRef getSourceRef() {
			return ref;
		}
		
	}
	private final Map<CharStream, String> fileMap;
	private final String defaultFile;
	private IScope currentScope;
	private List<Error> errors = new ArrayList<Error>();
	private TypeEngine typeEngine;
	private GlobalScope globalScope;
	
	public GenerateAST(String defaultFile, Map<CharStream, String> fileMap) {
		this.defaultFile = defaultFile;
		this.fileMap = fileMap;
		this.globalScope = new GlobalScope();
		this.typeEngine = new TypeEngine();
		
		typeEngine.populateTypes(globalScope);
		
	}
	
	public List<Error> getErrors() {
		return errors;
	}
	protected ISourceRef getSourceRef(Tree tree) {
		if (tree instanceof CommonTree) {
			Token token = ((CommonTree) tree).getToken();
			if (token != null) {
				String file = fileMap.get(token.getChannel());
				if (file == null)
					file = defaultFile;
				return new TokenSourceRef(file, token,
						tree.getTokenStopIndex() - tree.getTokenStartIndex());
			}
		}
		return new SourceRef(defaultFile, tree.getTokenStartIndex(), 
				tree.getTokenStopIndex() - tree.getTokenStartIndex(),
				tree.getLine(), tree.getCharPositionInLine()+1);
		
	}
	/** Copy source info into node */
	protected void getSource(Tree tree, IAstNode node) {
		node.setSourceRef(getSourceRef(tree));
	}
	
	protected ISourceRef getEmptySourceRef(Tree tree) {
		if (tree instanceof CommonTree) {
			Token token = ((CommonTree) tree).getToken();
			if (token != null) {
				return new SourceRef(fileMap.get(token.getChannel()), tree.getTokenStartIndex(), 0,
						tree.getLine(), tree.getCharPositionInLine() + 1);
			}
		}
		return new SourceRef(defaultFile, tree.getTokenStartIndex(), 0,
				tree.getLine(), tree.getCharPositionInLine()+1);
		
	}
	/** Copy empty source info into node */
	protected void getEmptySource(Tree tree, IAstNode node) {
		node.setSourceRef(getEmptySourceRef(tree));
	}
	
	/**
	 * @param tree
	 * @return
	 */
	private Iterable<Tree> iter(final Tree tree) {
		return new Iterable<Tree>() {
			
			@Override
			public Iterator<Tree> iterator() {
				return new Iterator<Tree>() {
					int index = 0;
					@Override
					public boolean hasNext() {
						return index < tree.getChildCount();
					}

					@Override
					public Tree next() {
						if (index >= tree.getChildCount())
							throw new NoSuchElementException(); 
						return tree.getChild(index++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
				};
			}
		};
	}
	

	/**
	 * @param construct
	 * @param class1
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T extends IAstNode> T checkConstruct(Tree tree, Class<T> klass) throws GenerateException {
		if (tree == null)
			throw new GenerateException(tree, "no tree to create " + klass.getSimpleName());
		IAstNode node = construct(tree);
		if (node == null)
			return null;
		if (klass.isInstance(node))
			return (T) node;
		throw new GenerateException(tree, "unexpected node " + node.getClass().getSimpleName() 
				+ " created, expected " + klass.getSimpleName());
	}

	private void error(Tree tree, String msg) {
		Error e = new Error(getSourceRef(tree), msg);
		System.err.println(e);
		errors.add(e);
	}
	private void error(ISourceRef ref, String msg) {
		Error e = new Error(ref, msg);
		System.err.println(e);
		errors.add(e);
	}

	/**
	 * @param tree
	 */
	private void unhandled(Tree tree) throws GenerateException {
		GenerateException e = new GenerateException(tree, "Unhandled tree: " + tree.toStringTree());
		System.err.println(e.getMessage());
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			if (stackTrace[i].getFileName().contains("GenerateAST")) {
				System.err.println(stackTrace[i].toString());
			} else {
				break;
			}
		}
		throw e;
	}

	/*
	public IAstSymbolExpr constructId(Tree tree) {
		assert tree.getChildCount() == 0;
		
		String name = tree.getText();
		
		IAstName nameNode = new AstName(name, currentScope);
		getSource(tree, nameNode);
		
		ISymbol symbol = 
		IAstSymbolExpr id = new AstSymbolExpr(symbol);
		getSource(tree, id);
		
		return id;
	}*/
	
	protected IScope pushScope(IScope newScope) {
		newScope.setParent(currentScope);
		currentScope = newScope;
		return newScope;
	}
	
	protected IScope popScope(Tree tree) throws GenerateException {
		if (currentScope == null)
			throw new GenerateException(tree, "no current scope");
		try {
			// do this later
			/*
			for (ISymbol symbol : currentScope) {
				if (symbol.getDefinition() == null)
					throw new GenerateException(tree, "undefined symbol '" + symbol.getName() + "'");
			}
			*/
			return currentScope;
		} finally {
			currentScope = currentScope.getParent();
		}
		
	}
	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	public IAstModule constructModule(Tree tree) throws GenerateException {
		
		// don't push/pop, since globals are not defined at all
		currentScope = new ModuleScope(globalScope);
		
		IAstModule module = new AstModule(currentScope);
		
		IAstNodeList<IAstStmt> stmtList = constructStmtList(tree);
		
		module.setStmtList(stmtList);
		
		getSource(tree, module);
		
		return module;
	}

	
	public IAstNode construct(Tree tree) throws GenerateException {
		switch (tree.getType()) {
		//case EulangParser.ID:
		//	return constructId(tree);
		case EulangParser.STMTLIST:
			return constructStmtList(tree);
		case EulangParser.LIT:
			return constructLiteral(tree);
		case EulangParser.DEFINE:
			return constructDefine(tree);
		case EulangParser.ALLOC:
			return constructAlloc(tree);
		case EulangParser.PROTO:
			return constructPrototype(tree);
		case EulangParser.TYPE:
			return constructTypeExpr(tree);
		case EulangParser.CODE:
		case EulangParser.MACRO:
			return constructCodeExpr(tree);
		case EulangParser.ARGDEF:
			return constructArgDef(tree);
		//case EulangParser.RETURN:
		//	return constructReturn(tree);
		case EulangParser.ADD:
		case EulangParser.SUB:
		case EulangParser.MUL:
		case EulangParser.DIV:
		case EulangParser.MOD:
		case EulangParser.UDIV:
		case EulangParser.UMOD:
		case EulangParser.LSHIFT:
		case EulangParser.RSHIFT:
		case EulangParser.URSHIFT:
		case EulangParser.BITAND:
		case EulangParser.BITOR:
		case EulangParser.BITXOR:
		case EulangParser.COMPAND:
		case EulangParser.COMPOR:
		case EulangParser.COMPEQ:
		case EulangParser.COMPNE:
		case EulangParser.COMPLE:
		case EulangParser.COMPGE:
		case EulangParser.LESS:
		case EulangParser.GREATER:
			return constructBinaryExpr(tree);
		case EulangParser.INV:
		case EulangParser.NEG:
			return constructUnaryExpr(tree);
		case EulangParser.NOT:
			return constructLogicalNot(tree);

		case EulangParser.IDREF:
			return constructIdRef(tree);
		case EulangParser.ASSIGN:
			return constructAssign(tree);
		case EulangParser.EXPR:
			return construct(tree.getChild(0));
			
		case EulangParser.CALL:
			return constructCallOrCast(tree);
		case EulangParser.ARGLIST:
			return constructArgList(tree);

		case EulangParser.INVOKE:
			return constructInvoke(tree);

		case EulangParser.STMTEXPR:
			return constructStmtExpr(tree);
		//case EulangParser.GOTO:
		//	return constructGotoStmt(tree);
		case EulangParser.LABEL:
			return constructLabelStmt(tree);
		case EulangParser.BLOCK:
			return constructBlockStmt(tree);
			
		case EulangParser.CONDLIST:
			return constructCondList(tree);
		case EulangParser.CONDTEST:
			return constructCondExpr(tree);
			
		default:
			unhandled(tree);
			return null;
		}
		
	}
	
	/**
	 * @param tree
	 * @return
	 */
	private IAstNode constructInvoke(Tree tree) {
		IAstInvokeExpr expr = new AstInvokeExpr();
		getSource(tree, expr);
		return expr;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	private IAstNode constructCondList(Tree tree) throws GenerateException {
		IAstNodeList<IAstCondExpr> condExprList = new AstNodeList<IAstCondExpr>();
		for (Tree kid : iter(tree)) {
			IAstCondExpr arg = checkConstruct(kid, IAstCondExpr.class);
			condExprList.add(arg);
			arg.setParent(condExprList);
		}
		getSource(tree, condExprList);
		IAstCondList condList = new AstCondList(condExprList);
		getSource(tree, condList);
		return condList;
	}
	
	private IAstNode constructCondExpr(Tree tree) throws GenerateException {
		assert tree.getChildCount() == 2;
		IAstTypedExpr test = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		IAstTypedExpr expr = checkConstruct(tree.getChild(1), IAstTypedExpr.class);
		IAstCondExpr condExpr = new AstCondExpr(test, expr);
		getSource(tree, condExpr);
		return condExpr;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	@SuppressWarnings("unchecked")
	private IAstNode constructBlockStmt(Tree tree) throws GenerateException {
		pushScope(new LocalScope(currentScope));
		try {
			IAstNodeList<IAstStmt> stmtList = checkConstruct(tree.getChild(0), IAstNodeList.class);
			
			IAstBlockStmt block = new AstBlockStmt(stmtList, currentScope);
			getSource(tree, block);
			return block;
		} finally {
			popScope(tree);
		}
	}

	private IAstNode constructGotoStmt(Tree tree) throws GenerateException {
		IAstSymbolExpr label = checkConstruct(tree.getChild(0), IAstSymbolExpr.class);
		label.getSymbol().setType(typeEngine.LABEL);
		
		IAstTypedExpr test = null;
		if (tree.getChildCount() == 2)
			test = checkConstruct(tree.getChild(1), IAstTypedExpr.class);
		
		IAstGotoStmt gotoStmt = new AstGotoStmt(label, test);
		getSource(tree, gotoStmt);
		return gotoStmt;
	}

	private IAstNode constructLabelStmt(Tree tree) throws GenerateException {
		IAstSymbolExpr label = createSymbol(tree.getChild(0));
		label.setType(typeEngine.LABEL);
		
		IAstLabelStmt labelStmt = new AstLabelStmt(label);
		getSource(tree, labelStmt);
		
		label.getSymbol().setDefinition(labelStmt);
		
		return labelStmt;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	private IAstNode constructStmtExpr(Tree tree) throws GenerateException {
		assert tree.getChildCount() == 1;
		
		IAstTypedExpr expr = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		IAstStmt stmt = new AstExprStmt(expr);
		getSource(tree, stmt);
		return stmt;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructArgList(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedExpr> argList = new AstNodeList<IAstTypedExpr>();
		for (Tree kid : iter(tree)) {
			IAstTypedExpr arg = checkConstruct(kid, IAstTypedExpr.class);
			argList.add(arg);
			arg.setParent(argList);
		}
		getSource(tree, argList);
		return argList;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	@SuppressWarnings("unchecked")
	public IAstNode constructCallOrCast(Tree tree) throws GenerateException {
		assert tree.getChildCount() == 2;
		
		IAstTypedExpr function = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		
		IAstNodeList<IAstTypedExpr> args = checkConstruct(tree.getChild(1), IAstNodeList.class);
		
		// check for a cast
		if (args.nodeCount() == 1 && function instanceof IAstSymbolExpr) {
			ISymbol funcSym = ((IAstSymbolExpr) function).getSymbol();
			IAstNode symdef = funcSym.getDefinition();
			if (symdef instanceof IAstType && ((IAstType) symdef).getType() != null) {
				IAstTypedExpr[] argNodes = args.getNodes(IAstTypedExpr.class);
				argNodes[0].setParent(null);
				IAstUnaryExpr castExpr = new AstUnaryExpr(IOperation.CAST, argNodes[0]);
				castExpr.setType(((IAstType) symdef).getType());
				getSource(tree, castExpr);
				return castExpr;
			}
		}
		
		IAstFuncCallExpr funcCall = new AstFuncCallExpr(function, args);
		getSource(tree, funcCall);
		return funcCall;
	}

	public IAstAllocStmt constructAlloc(Tree tree) throws GenerateException {
		IAstSymbolExpr symbolExpr = createSymbol(tree.getChild(0));
		
		IAstType type = checkConstruct(tree.getChild(1), IAstType.class);
		if (type != null) {
			symbolExpr.getSymbol().setType(type.getType());
		}

		IAstTypedExpr expr = null;
		if (tree.getChildCount() == 3)
			expr = checkConstruct(tree.getChild(2), IAstTypedExpr.class);
		
		IAstAllocStmt define = new AstAllocStmt(symbolExpr, type, expr);
		getSource(tree, define);

		symbolExpr.getSymbol().setDefinition(define);

		return define;
	}
	/**
	 * @param child
	 * @return
	 * @throws GenerateException 
	 */
	private IAstSymbolExpr createSymbol(Tree id) throws GenerateException {
		String name = id.getText();
		
		ISymbol symbol = currentScope.get(name);
		if (symbol != null && symbol.getDefinition() != null && !isMacroArg(symbol.getDefinition())) {
			throw new GenerateException(id, "redefining " + name);
		}
		IAstName nameNode = new AstName(name, currentScope);
		getSource(id, nameNode);
		
		
		if (symbol == null) {
			symbol = currentScope.add(nameNode); 
			System.out.println("Creating " +  symbol);
		}

		IAstSymbolExpr symbolExpr = new AstSymbolExpr(symbol);
		symbolExpr.setSourceRef(nameNode.getSourceRef());

		return symbolExpr;
	}

	/**
	 * @param definition
	 * @return
	 */
	private boolean isMacroArg(IAstNode definition) {
		return definition instanceof IAstArgDef && ((IAstArgDef) definition).isMacro();
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructAssign(Tree tree) throws GenerateException {
		assert(tree.getChildCount() == 2);
		IAstSymbolExpr left = checkConstruct(tree.getChild(0), IAstSymbolExpr.class);
		IAstTypedExpr right = checkConstruct(tree.getChild(1), IAstTypedExpr.class);
		IAstAssignStmt assign = new AstAssignStmt(left, right);
		getSource(tree, assign);
		return assign;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstSymbolExpr constructIdRef(Tree tree) throws GenerateException {
		// could have ':'s
		IScope startScope = currentScope;
		int idx = 0;
		ISymbol symbol = null;
		boolean inScope = false;
		while (idx < tree.getChildCount()) {
			Tree kid = tree.getChild(idx);
			if (kid.getType() == EulangParser.COLON) {
				if (startScope.getParent() == null) {
					throw new GenerateException(tree, "Cannot go out of module scope");
				} else {
					startScope = startScope.getParent();
				}
				inScope = true;
			} else if (kid.getType() == EulangParser.ID) {
				
				if (inScope) {
					symbol = startScope.get(kid.getText());
				} else {
					symbol = startScope.search(kid.getText());
				}
				if (symbol == null) {
					// make forward
					symbol = startScope.add(new AstName(kid.getText()));
				}
				startScope = symbol.getScope();
			} else {
				unhandled(kid);
				return null;
			}
			idx++;
		}
		if (symbol == null) {
			throw new GenerateException(tree, "Cannot resolve name: " + tree.toStringTree());
		}
		IAstSymbolExpr symbolExpr = new AstSymbolExpr(symbol);
		getSource(tree, symbolExpr);
		return symbolExpr;
	}

	public IAstBinExpr constructBinaryExpr(Tree tree) throws GenerateException {
		assert(tree.getChildCount() == 2);
		IAstTypedExpr left = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		IAstTypedExpr right = checkConstruct(tree.getChild(1), IAstTypedExpr.class);
		IAstBinExpr binop = null;
		
		switch (tree.getType()) {
		case EulangParser.ADD:
			binop = new AstBinExpr(IOperation.ADD, left, right);
			break;
		case EulangParser.SUB:
			binop = new AstBinExpr(IOperation.SUB, left, right);
			break;
		case EulangParser.MUL:
			binop = new AstBinExpr(IOperation.MUL, left, right);
			break;
		case EulangParser.DIV:
			binop = new AstBinExpr(IOperation.DIV, left, right);
			break;
		case EulangParser.UDIV:
			binop = new AstBinExpr(IOperation.UDIV, left, right);
			break;
		case EulangParser.MOD:
			binop = new AstBinExpr(IOperation.MOD, left, right);
			break;
		case EulangParser.UMOD:
			binop = new AstBinExpr(IOperation.UMOD, left, right);
			break;
			
		case EulangParser.URSHIFT:
			binop = new AstBinExpr(IOperation.SHR, left, right);
			break;
		case EulangParser.RSHIFT:
			binop = new AstBinExpr(IOperation.SAR, left, right);
			break;
		case EulangParser.LSHIFT:
			binop = new AstBinExpr(IOperation.SHL, left, right);
			break;
			
		case EulangParser.COMPEQ:
			binop = new AstBinExpr(IOperation.COMPEQ, left, right);
			break;
		case EulangParser.COMPNE:
			binop = new AstBinExpr(IOperation.COMPNE, left, right);
			break;
		case EulangParser.GREATER:
			binop = new AstBinExpr(IOperation.COMPGT, left, right);
			break;
		case EulangParser.COMPGE:
			binop = new AstBinExpr(IOperation.COMPGE, left, right);
			break;
		case EulangParser.LESS:
			binop = new AstBinExpr(IOperation.COMPLT, left, right);
			break;
		case EulangParser.COMPLE:
			binop = new AstBinExpr(IOperation.COMPLE, left, right);
			break;
			
		case EulangParser.COMPAND:
			binop = new AstBinExpr(IOperation.COMPAND, left, right);
			break;
		case EulangParser.COMPOR:
			binop = new AstBinExpr(IOperation.COMPOR, left, right);
			break;
			
		case EulangParser.BITOR:
			binop = new AstBinExpr(IOperation.BITOR, left, right);
			break;
		case EulangParser.BITXOR:
			binop = new AstBinExpr(IOperation.BITXOR, left, right);
			break;
		case EulangParser.BITAND:
			binop = new AstBinExpr(IOperation.BITAND, left, right);
			break;
			
		default:
			unhandled(tree);
			return null;
		}
		getSource(tree, binop);
		return binop;
	}

	public IAstUnaryExpr constructUnaryExpr(Tree tree) throws GenerateException {
		assert(tree.getChildCount() == 1);
		IAstTypedExpr expr = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		IAstUnaryExpr unary = null;
		
		switch (tree.getType()) {
		case EulangParser.INV:
			unary = new AstUnaryExpr(IOperation.INV, expr);
			break;
		//case EulangParser.NOT:
		//	unary = new AstUnaryExpr(IOperation.NOT, expr);
		//	break;
		case EulangParser.NEG:
			unary = new AstUnaryExpr(IOperation.NEG, expr);
			break;
			
		default:
			unhandled(tree);
			return null;
		}
		getSource(tree, unary);
		return unary;
	}
	
	public IAstBinExpr constructLogicalNot(Tree tree) throws GenerateException {
		assert(tree.getChildCount() == 1);
		IAstTypedExpr expr = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		IAstLitExpr zero = createZero(expr.getType());
		getEmptySource(tree, zero);
		IAstBinExpr binary = new AstBinExpr(IOperation.COMPNE, expr, zero);
		
		getSource(tree, binary);
		return binary;
	}

	/**
	 * @param type
	 * @return
	 */
	private IAstLitExpr createZero(LLType type) {
		if (type != null) {
			switch (type.getBasicType()) {
			case BOOL:
				return new AstBoolLitExpr("false", type, false);
			case INTEGRAL:
				return new AstIntLitExpr("0", type, 0);
			case FLOATING:
				return new AstFloatLitExpr("0", type, 0.0);
			//$FALL-THROUGH$
			}
		}
		return new AstIntLitExpr("0", typeEngine.INT_ANY, 0);
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstReturnStmt constructReturn(Tree tree) throws GenerateException {
		IAstTypedExpr expr = null;
		if (tree.getChildCount() == 1) {
			expr = checkConstruct(tree.getChild(0), IAstTypedExpr.class);
		}
		IAstReturnStmt stmt = new AstReturnStmt(expr);
		getSource(tree, stmt);
		return stmt;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructArgDef(Tree tree) throws GenerateException {
		int argIdx = 0;
		
		boolean isMacro = false;
		if (tree.getChild(argIdx).getType() == EulangParser.MACRO) {
			isMacro = true;
			argIdx++;
		}
		
		IAstSymbolExpr symExpr = createSymbol(tree.getChild(argIdx++));
		
		IAstType type = null;
		IAstTypedExpr defaultVal = null;
		
		if (tree.getChildCount() > argIdx) {
			if (tree.getChild(argIdx).getType() == EulangParser.TYPE) {
				type = checkConstruct(tree.getChild(argIdx), IAstType.class);
				argIdx++;
			} 
			if (argIdx < tree.getChildCount()) {
				defaultVal = checkConstruct(tree.getChild(argIdx), IAstTypedExpr.class);
			}
		}
		
		IAstArgDef argDef = new AstArgDef(symExpr, type, defaultVal, isMacro);
		getSource(tree, argDef);
		
		symExpr.getSymbol().setDefinition(argDef);
		
		if (type != null && type.getType() != null) {
			symExpr.getSymbol().setType(type.getType());
		}
		
		return argDef;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNodeList<IAstStmt> constructStmtList(Tree tree) {
		IAstNodeList<IAstStmt> list = new AstNodeList<IAstStmt>();
		
		assert tree.getType() == EulangParser.STMTLIST;
		
		for (Tree kid : iter(tree)) {
			try {
				IAstStmt node = checkConstruct(kid, IAstStmt.class);
				/*if (node instanceof IAstBlockStmt) {
					addBlock(list.list(), ((IAstBlockStmt) node).stmtList());
					list.list().add(node);
					node.setParent(list);
				}
				else*/ 
				if (node != null) { 
					list.add(node);
					node.setParent(list);
				}
			} catch (GenerateException e) {
				if (e.getTree() != null)
					error(e.getTree(), e.getMessage());
				else
					error(e.getSourceRef(), e.getMessage());
			}
		}
		
		getSource(tree, list);
		return list;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstPrototype constructPrototype(Tree tree) throws GenerateException {
		LLType retType = null;
		IAstType retTypeNode;
		int start = 1;
		if (tree.getChildCount() == 0 || tree.getChild(0).getType() != EulangParser.TYPE) {
			retType = typeEngine.UNSPECIFIED;
			retTypeNode = new AstType(retType);
			getEmptySource(tree, retTypeNode); 
			start = 0;
		} else {
			retType = constructType(tree.getChild(0).getChild(0));
			retTypeNode = new AstType(retType);
			getSource(tree, retTypeNode); 
		}
		
		IAstArgDef[] argTypes = new IAstArgDef[tree.getChildCount() - start];
		int idx = 0;
		while (start < tree.getChildCount()) {
			argTypes[idx++] = checkConstruct(tree.getChild(start++), IAstArgDef.class);
		}
		
		IAstPrototype proto = new AstPrototype(typeEngine, retTypeNode, argTypes);
		getSource(tree, proto);
		return proto;
	}

	/**
	 * @param child
	 * @return
	 */
	public LLType constructType(Tree tree) throws GenerateException {
		if (tree.getType() == EulangParser.TYPE) {
			return checkConstruct(tree, IAstType.class).getType();
		}
		else if (tree.getType() == EulangParser.CODE) {
			if (tree.getChildCount() == 0) {
				return typeEngine.getCodeType(null, new LLType[0]);
			}
			if (tree.getChildCount() > 1)
				throw new GenerateException(tree.getChild(2), "did not expect code block here");
			IAstPrototype proto = checkConstruct(tree.getChild(0), IAstPrototype.class);
			if (proto.hasDefaultArguments()) {
				throw new GenerateException(tree.getChild(2), "cannot use default arguments in code type");
			}
			return proto.getType();
		}
		else if (tree.getType() == EulangParser.IDREF) {
			return checkConstruct(tree, IAstSymbolExpr.class).getType();
		}
		unhandled(tree);
		return null;
	}

	public IAstType constructTypeExpr(Tree tree) throws GenerateException {
		LLType type = null;
		if (tree.getChildCount() == 1)
			type = constructType(tree.getChild(0));
		IAstType typeExpr = new AstType(type);
		getSource(tree, typeExpr);
		return typeExpr;
	}
	
	public IAstDefineStmt constructDefine(Tree tree) throws GenerateException {
		assert tree.getChildCount() == 2;
		
		IAstSymbolExpr symbolExpr = createSymbol(tree.getChild(0));
		
		IAstDefineStmt stmt = new AstDefineStmt(symbolExpr, checkConstruct(tree.getChild(1), IAstTypedExpr.class));
		getSource(tree.getChild(1), stmt);
		
		symbolExpr.getSymbol().setDefinition(stmt);
		
		return stmt;
	}
	
	/**
	 * @param tree
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IAstTypedExpr constructCodeExpr(Tree tree) throws GenerateException {
		assert tree.getChildCount() > 0;
		boolean isMacro = tree.getType() == EulangParser.MACRO;
		pushScope(new LocalScope(currentScope));
		try {
			IAstPrototype proto;
			int idx = 0;
			if (tree.getChild(idx).getType() == EulangParser.PROTO) {
				proto = checkConstruct(tree.getChild(idx), IAstPrototype.class);

				if (!isMacro) {
					for (IAstArgDef argDef : proto.argumentTypes()) {
						if (argDef.isMacro()) {
							throw new GenerateException(argDef.getSourceRef(), "cannot use macro arguments outside macro code");
						}
					}
				}
				idx++;
			} else {
				IAstType unspecified = new AstType(null);
				getEmptySource(tree, unspecified);
				proto = new AstPrototype(typeEngine, unspecified, new IAstArgDef[0]);
				getEmptySource(tree, proto);
			}
			IAstNodeList<IAstStmt> list = checkConstruct(tree.getChild(idx++), IAstNodeList.class);
			IAstCodeExpr codeExpr = new AstCodeExpr(proto, currentScope, list, isMacro);
			getSource(tree, codeExpr);
			return codeExpr;
		} finally {
			popScope(tree);
		}
	}

	/**
	 * @param tree
	 * @return
	 */
	private IAstLitExpr constructLiteral(Tree tree) throws GenerateException {
		assert tree.getType() == EulangParser.LIT;
		assert tree.getChildCount() == 1;

		IAstLitExpr litExpr = null;
		
		String lit = tree.getChild(0).getText();
		switch (tree.getChild(0).getType()) {
		case EulangParser.NULL:
			litExpr = new AstNullLitExpr(lit, typeEngine.NULL);
			break;
		case EulangParser.TRUE:
			litExpr = new AstBoolLitExpr(lit, typeEngine.BOOL, true);
			break;
		case EulangParser.FALSE:
			litExpr = new AstBoolLitExpr(lit, typeEngine.BOOL, false);
			break;
		case EulangParser.NUMBER:
			try {
				Long l = Long.parseLong(lit);
				litExpr = new AstIntLitExpr(lit, typeEngine.INT, l);
			} catch (NumberFormatException e) {
				try {
					Double d = Double.parseDouble(lit);
					litExpr = new AstFloatLitExpr(lit, typeEngine.FLOAT, d);
				} catch (NumberFormatException e2) {
				}
			}
		}

		if (litExpr == null) {
			unhandled(tree);
			return null;
		}
		getSource(tree, litExpr);
		return litExpr;

	}

	/**
	 * @return
	 */
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
	

}
