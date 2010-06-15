/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstAddrOfExpr;
import org.ejs.eulang.ast.impl.AstAllocStmt;
import org.ejs.eulang.ast.impl.AstAllocTupleStmt;
import org.ejs.eulang.ast.impl.AstArgDef;
import org.ejs.eulang.ast.impl.AstArrayType;
import org.ejs.eulang.ast.impl.AstAssignStmt;
import org.ejs.eulang.ast.impl.AstAssignTupleStmt;
import org.ejs.eulang.ast.impl.AstBinExpr;
import org.ejs.eulang.ast.impl.AstBlockStmt;
import org.ejs.eulang.ast.impl.AstBoolLitExpr;
import org.ejs.eulang.ast.impl.AstBreakStmt;
import org.ejs.eulang.ast.impl.AstCastNamedTypeExpr;
import org.ejs.eulang.ast.impl.AstCodeExpr;
import org.ejs.eulang.ast.impl.AstCondExpr;
import org.ejs.eulang.ast.impl.AstCondList;
import org.ejs.eulang.ast.impl.AstDataType;
import org.ejs.eulang.ast.impl.AstDefineStmt;
import org.ejs.eulang.ast.impl.AstDoWhileExpr;
import org.ejs.eulang.ast.impl.AstExprStmt;
import org.ejs.eulang.ast.impl.AstFieldExpr;
import org.ejs.eulang.ast.impl.AstFloatLitExpr;
import org.ejs.eulang.ast.impl.AstForExpr;
import org.ejs.eulang.ast.impl.AstFuncCallExpr;
import org.ejs.eulang.ast.impl.AstGotoStmt;
import org.ejs.eulang.ast.impl.AstInitIndexExpr;
import org.ejs.eulang.ast.impl.AstInitListExpr;
import org.ejs.eulang.ast.impl.AstInitNodeExpr;
import org.ejs.eulang.ast.impl.AstInstanceExpr;
import org.ejs.eulang.ast.impl.AstIntLitExpr;
import org.ejs.eulang.ast.impl.AstLabelStmt;
import org.ejs.eulang.ast.impl.AstModule;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.ast.impl.AstNamedType;
import org.ejs.eulang.ast.impl.AstNestedScope;
import org.ejs.eulang.ast.impl.AstNilLitExpr;
import org.ejs.eulang.ast.impl.AstNodeList;
import org.ejs.eulang.ast.impl.AstPointerType;
import org.ejs.eulang.ast.impl.AstPrototype;
import org.ejs.eulang.ast.impl.AstRedefinition;
import org.ejs.eulang.ast.impl.AstRefType;
import org.ejs.eulang.ast.impl.AstRepeatExpr;
import org.ejs.eulang.ast.impl.AstReturnStmt;
import org.ejs.eulang.ast.impl.AstSizeOfExpr;
import org.ejs.eulang.ast.impl.AstStatement;
import org.ejs.eulang.ast.impl.AstStmtListExpr;
import org.ejs.eulang.ast.impl.AstStringLitExpr;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.ast.impl.AstTupleExpr;
import org.ejs.eulang.ast.impl.AstTupleNode;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.ast.impl.AstTypeOfExpr;
import org.ejs.eulang.ast.impl.AstUnaryExpr;
import org.ejs.eulang.ast.impl.AstDerefExpr;
import org.ejs.eulang.ast.impl.AstWhileExpr;
import org.ejs.eulang.ast.impl.SourceRef;
import org.ejs.eulang.parser.EulangParser;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.symbols.NamespaceScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLGenericType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * Transform from the syntax tree to an AST with proper node types, type
 * information, and source references.
 * <p>
 * The outcome of parsing is a representation of the AST as it appears in
 * source, with types (predefined, referenced through symbols, or generic)
 * represented both as AST subtrees (IAstType).
 * <p>
 * When generic types are involved, any references to generic names will appear
 * 
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

		public GenerateException() {
			super((String) null);
		}

		public Tree getTree() {
			return tree;
		}

		public ISourceRef getSourceRef() {
			return ref;
		}

	}

	static class TempLabelStmt extends AstStatement {

		private final IAstLabelStmt label;
		private final IAstStmt stmt;

		/**
		 * @param label
		 * @param stmt
		 */
		public TempLabelStmt(IAstLabelStmt label, IAstStmt stmt) {
			this.label = label;
			this.stmt = stmt;
		}

		/**
		 * @return the label
		 */
		public IAstLabelStmt getLabel() {
			return label;
		}

		/**
		 * @return the stmt
		 */
		public IAstStmt getStmt() {
			return stmt;
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public IAstNode copy() {
			return null;
		}

		@Override
		public IAstNode[] getChildren() {
			return NO_CHILDREN;
		}

		@Override
		public void replaceChild(IAstNode existing, IAstNode another) {
			throw new UnsupportedOperationException();
		}
	}

	public boolean DUMP = false;

	private final Map<CharStream, String> fileMap;
	private final String defaultFile;
	private IScope currentScope;
	private List<Error> errors = new ArrayList<Error>();
	private TypeEngine typeEngine;
	private GlobalScope globalScope;
	private ISymbol currentName;

	public GenerateAST(TypeEngine typeEngine, String defaultFile,
			Map<CharStream, String> fileMap) {
		this.defaultFile = defaultFile;
		this.fileMap = fileMap;
		this.globalScope = new GlobalScope();
		this.typeEngine = typeEngine;

		typeEngine.populateTypes(globalScope);

	}

	public List<Error> getErrors() {
		return errors;
	}

	protected ISourceRef getSourceRef(Tree tree) {
		if (tree instanceof CommonTree) {

			CommonTree cTree = (CommonTree) tree;
			Token token = cTree.getToken();
			if (token != null) {
				String file = fileMap.get(token.getChannel());
				if (file == null)
					file = defaultFile;
				// return new TokenSourceRef(file, token, tree.toStringTree()
				// .length());
				String string = cTree.toStringTree();
				return new SourceRef(file, string.length(), tree.getLine(),
						tree.getCharPositionInLine() + 1, tree.getLine()
								+ countNls(string), string.length()
								- string.lastIndexOf('\n'));
			}

		}
		String string = tree.toString();
		return new SourceRef(defaultFile, string.length(), tree.getLine(), tree
				.getCharPositionInLine() + 1,
				tree.getLine() + countNls(string), string.length()
						- string.lastIndexOf('\n'));

	}

	/**
	 * @param string
	 * @return
	 */
	private int countNls(String string) {
		int cnt = 0;
		int idx = 0;
		while (idx < string.length()) {
			if (string.charAt(idx) == '\n')
				cnt++;
			idx++;
		}
		return cnt;
	}

	/** Copy source info into node */
	protected void getSource(Tree tree, IAstNode node) {
		node.setSourceRef(getSourceRef(tree));
	}

	protected ISourceRef getEmptySourceRef(Tree tree) {
		if (tree instanceof CommonTree) {
			Token token = ((CommonTree) tree).getToken();
			if (token != null) {
				return new SourceRef(fileMap.get(token.getChannel()), 0, tree
						.getLine(), tree.getCharPositionInLine() + 1, tree
						.getLine(), tree.getCharPositionInLine() + 1);
			}
		}
		return new SourceRef(defaultFile, 0, tree.getLine(), tree
				.getCharPositionInLine() + 1, tree.getLine(), tree
				.getCharPositionInLine() + 1);

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
	private <T extends IAstNode> T checkConstruct(Tree tree, Class<T> klass)
			throws GenerateException {
		if (tree == null)
			throw new GenerateException(tree, "no tree to create "
					+ klass.getSimpleName());
		IAstNode node = construct(tree);
		if (node == null)
			return null;
		if (klass.isInstance(node))
			return (T) node;
		throw new GenerateException(tree, "unexpected node "
				+ node.getClass().getSimpleName() + " created, expected "
				+ klass.getSimpleName());
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
		GenerateException e = new GenerateException(tree, "Unhandled tree: "
				+ tree.toStringTree());
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

	protected IScope pushScope(IScope newScope) {
		newScope.setParent(currentScope);
		currentScope = newScope;
		return newScope;
	}

	protected IScope popScope(Tree tree) throws GenerateException {
		if (currentScope == null)
			throw new GenerateException(tree, "no current scope");
		try {
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
	@SuppressWarnings("unchecked")
	public IAstModule constructModule(Tree tree) throws GenerateException {

		// don't push/pop, since globals are not defined at all
		currentScope = new ModuleScope(globalScope);

		IAstNodeList<IAstStmt> stmtList = checkConstruct(tree,
				IAstNodeList.class);

		IAstModule module = new AstModule(currentScope, stmtList);

		getSource(tree, module);

		return module;
	}

	public IAstNode construct(Tree tree) throws GenerateException {
		switch (tree.getType()) {
		case EulangParser.SCOPE:
			return constructScope(tree);
		case EulangParser.ADDSCOPE:
			return constructAddScope(tree);

		case EulangParser.DATA:
			return constructData(tree);

		case EulangParser.STMTLIST:
			return constructStmtList(tree);
		case EulangParser.LIT:
			return constructLiteral(tree);
		case EulangParser.DEFINE:
			return constructDefine(tree);
		case EulangParser.REDEF:
			return constructRedefinition(tree);
		case EulangParser.FORWARD:
			return constructForward(tree);
		case EulangParser.ALLOC:
			return constructAlloc(tree);
		case EulangParser.ALLOC_TUPLE:
			return constructAllocTuple(tree);
		case EulangParser.PROTO:
			return constructPrototype(tree);
		case EulangParser.TYPE:
			return constructTypeExpr(tree);
		case EulangParser.CODE:
			return constructCodeExpr(tree, null);
		case EulangParser.ARGDEF:
			return constructArgDef(tree);
			// case EulangParser.RETURN:
			// return constructReturn(tree);
		case EulangParser.CAST:
			return constructCast(tree);
		case EulangParser.ADD:
		case EulangParser.SUB:
		case EulangParser.MUL:
		case EulangParser.DIV:
		case EulangParser.REM:
		case EulangParser.UDIV:
		case EulangParser.UREM:
		case EulangParser.MOD:
		case EulangParser.LSHIFT:
		case EulangParser.RSHIFT:
		case EulangParser.URSHIFT:
		case EulangParser.CRSHIFT:
		case EulangParser.CLSHIFT:
		case EulangParser.BITAND:
		case EulangParser.BITOR:
		case EulangParser.BITXOR:
		case EulangParser.AND:
		case EulangParser.OR:
		case EulangParser.COMPEQ:
		case EulangParser.COMPNE:
		case EulangParser.COMPLE:
		case EulangParser.COMPGE:
		case EulangParser.COMPULE:
		case EulangParser.COMPUGE:
		case EulangParser.LESS:
		case EulangParser.GREATER:
		case EulangParser.ULESS:
		case EulangParser.UGREATER:
			return constructBinaryExpr(tree);
		case EulangParser.INV:
		case EulangParser.NEG:
		case EulangParser.POSTINC:
		case EulangParser.POSTDEC:
		case EulangParser.PREINC:
		case EulangParser.PREDEC:
			return constructUnaryExpr(tree);
		case EulangParser.NOT:
			return constructLogicalNot(tree);

		case EulangParser.IDREF:
			return constructIdRef(tree);
		case EulangParser.INSTANCE:
			return constructInstance(tree);

		case EulangParser.ASSIGN:
			return constructAssign(tree);
		case EulangParser.EXPR:
			return construct(tree.getChild(0));

		case EulangParser.ADDROF:
			return constructAddrOf(tree);
		case EulangParser.SIZEOF:
			return constructSizeOf(tree);
		case EulangParser.TYPEOF:
			return constructTypeOf(tree);

		case EulangParser.INITLIST:
			return constructInitList(tree);
		case EulangParser.INITEXPR:
			return constructInitNodeExpr(tree);

		case EulangParser.CALL:
			return constructCallOrConstruct(tree);
		case EulangParser.ARGLIST:
			return constructArgList(tree);

			// case EulangParser.INVOKE:
			// return constructInvoke(tree);

		case EulangParser.LABELSTMT:
			return constructLabelStmt(tree);
		case EulangParser.LABEL:
			return constructLabel(tree);

		case EulangParser.STMTEXPR:
			return constructStmtExpr(tree);
		case EulangParser.GOTO:
			return constructGotoStmt(tree);
		case EulangParser.BLOCK:
			return constructBlockStmt(tree);

		case EulangParser.CONDLIST:
			return constructCondList(tree);
		case EulangParser.CONDTEST:
			return constructCondExpr(tree);

		case EulangParser.TUPLE:
			return constructTuple(tree);
		case EulangParser.TUPLETYPE:
			return constructType(tree);

		case EulangParser.LIST:
			return constructList(tree);

		case EulangParser.INDEX:
			return constructIndex(tree);
		case EulangParser.DEREF:
			return constructDeref(tree);
		case EulangParser.FIELDREF:
			return constructFieldRef(tree);

		case EulangParser.REPEAT:
			return constructRepeat(tree);
		case EulangParser.WHILE:
			return constructWhile(tree);
		case EulangParser.DO:
			return constructDoWhile(tree);
		case EulangParser.BREAK:
			return constructBreak(tree);
		case EulangParser.FOR:
			return constructFor(tree);

		case EulangParser.EXTENDSCOPE:
			return constructExtendScope(tree);
		default:
			unhandled(tree);
			return null;
		}

	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	private IAstRedefinition constructRedefinition(Tree tree) throws GenerateException {
		
		String symExpr = tree.getChild(0).getText();
		IAstTypedExpr expr = null;

		ISymbol sym = currentScope.search(symExpr);
		if (sym == null) {
			throw new GenerateException(tree, "cannot redefine unknown symbol '" + symExpr + "'");
		}
		
		// if redefining code, get the original context
		if (sym.getDefinition() instanceof IAstAllocStmt) {
			IAstAllocStmt existAlloc = (IAstAllocStmt) sym.getDefinition();
			IAstType existType = existAlloc.getTypeExpr();
			IAstPrototype proto = null;
			if (existType instanceof IAstPointerType && ((IAstPointerType) existType).getBaseType() instanceof IAstPrototype)
				proto = (IAstPrototype) ((IAstPointerType) existType).getBaseType();
			else if (existType instanceof IAstPrototype)
				proto = (IAstPrototype) existType;
			
			if (proto != null) {
				try {
					proto = proto.copy();
					//if (proto.hasAttr(IAttrs.THIS))
					//	adjustPrototypeForThisCall(tree, proto);
					pushScope(new LocalScope(currentScope));
					expr = constructCodeExpr(tree.getChild(1), proto);
				} finally {
					popScope(tree.getChild(1));
				}
			}
		}
		
		if (expr == null) {
			expr = checkConstruct(tree.getChild(1), IAstTypedExpr.class);
		}
		
		IAstRedefinition redef = new AstRedefinition(symExpr, expr);
		getSource(tree, redef);
		
		return redef;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException 
	 */
	private IAstNode constructCast(Tree tree) throws GenerateException {
		boolean isUnsigned = false;
		
		int idx = 0;
		if (tree.getChild(idx).getType() == EulangParser.PLUS) {
			isUnsigned = true;
			idx++;
		}
		IAstType type = checkConstruct( tree.getChild(idx++), IAstType.class);
		IAstTypedExpr expr = checkConstruct(tree.getChild(idx++), IAstTypedExpr.class);
		
		IAstTypedExpr castExpr;
		/*if (type.getType() != null && type.getType().isComplete()) {
			castExpr = new AstUnaryExpr(IOperation.CAST,
					expr);
			castExpr.setType(type.getType());
			castExpr.setTypeFixed(true);
		} else*/ {
			castExpr = new AstCastNamedTypeExpr(type, expr, isUnsigned);
		}
		getSource(tree, castExpr);
		return castExpr;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	@SuppressWarnings("unchecked")
	private IAstNode constructInstance(Tree tree) throws GenerateException {
		IAstSymbolExpr idExpr = checkConstruct(tree.getChild(0),
				IAstSymbolExpr.class);
		IAstNodeList<IAstTypedExpr> exprs = checkConstruct(tree.getChild(1),
				IAstNodeList.class);
		IAstInstanceExpr instance = new AstInstanceExpr(typeEngine, idExpr,
				exprs);
		getSource(tree, instance);

		return instance;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructExtendScope(Tree tree) throws GenerateException {
		IAstSymbolExpr symExpr = checkConstruct(tree.getChild(0), IAstSymbolExpr.class);
		
		String name =  symExpr.getSymbol().getUniqueName();
		
		IAstScope astScope = (IAstScope) symExpr.getSymbol().getDefinition();
		
		IAstNode def = symExpr.getSymbol().getDefinition();

		if (def instanceof IAstDefineStmt) {
			if (((IAstDefineStmt) def).bodyList().size() != 1)
				throw new GenerateException(tree.getChild(0),
						"symbol is not a simple scope: " + name);
			def = ((IAstDefineStmt) def).getMatchingBodyExpr(null);
		}
		if (!(def instanceof IAstStmtScope)) {
			throw new GenerateException(tree.getChild(0),
					"symbol is not a scope: " + name);
		}
		
		IAstStmtScope stmtScope = (IAstStmtScope) def;

		IScope save = currentScope;
		currentScope = astScope.getScope();

		try {
			// make the new scope
			IAstStmtScope added = checkConstruct(tree.getChild(1),
					IAstStmtScope.class);

			try {
				stmtScope.merge(added, typeEngine);
			} catch (ASTException e) {
				throw new GenerateException(e.getNode().getSourceRef(), e
						.getMessage());
			}

			return null;
		} finally {
			currentScope = save;
		}

	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructAddScope(Tree tree) throws GenerateException {
		IAstSymbolExpr symExpr = checkConstruct(tree.getChild(0), IAstSymbolExpr.class);
		
		String name =  symExpr.getSymbol().getUniqueName();
		
		IAstNode def = symExpr.getSymbol().getDefinition();

		if (def instanceof IAstDefineStmt) {
			if (((IAstDefineStmt) def).bodyList().size() != 1)
				throw new GenerateException(tree.getChild(0),
						"symbol is not a simple scope: " + name);
			def = ((IAstDefineStmt) def).getMatchingBodyExpr(null);
		}
		if (!(def instanceof IAstStmtScope)) {
			throw new GenerateException(tree.getChild(0),
					"symbol is not a scope: " + name);
		}

		IAstStmtScope combined = (IAstStmtScope) def.copy();
		
		// make sure the new scope can see the original one...
		pushScope(combined.getScope());
		try {
			// make the new scope
			IAstStmtScope added = checkConstruct(tree.getChild(1),
					IAstStmtScope.class);

			// merge
			try {
				combined.merge(added, typeEngine);
			} catch (ASTException e) {
				throw new GenerateException(e.getNode().getSourceRef(), e
						.getMessage());
			}

			combined.uniquifyIds();
			getSource(tree, combined);
			return combined;
		} finally {
			popScope(tree);
		}

	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstScope constructScope(Tree tree) throws GenerateException {
		pushScope(new NamespaceScope(currentScope));
		try {
			IAstNodeList<IAstStmt> stmts = new AstNodeList<IAstStmt>(
					IAstStmt.class);
			constructScopeEntries(stmts, tree);

			IAstStmtScope scope = new AstNestedScope(stmts, currentScope, null);
			getSource(tree, scope);

			return scope;
		} finally {
			popScope(tree);
		}
	}

	private void constructScopeEntries(IAstNodeList<IAstStmt> stmts, Tree tree)
			throws GenerateException {
		Tree start = tree.getChild(0);
		assert (tree.getChildCount() == 1 && tree.getChild(0).getType() == EulangParser.STMTLIST);
		for (Tree kid : iter(start)) {
			IAstStmt item = checkConstruct(kid, IAstStmt.class);
			stmts.add(item);
		}
		getSource(tree, stmts);
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	
	private IAstNode constructDeref(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstDerefExpr value = new AstDerefExpr(expr, false);
		getSource(tree, value);
		return value;
	}
	
	private IAstNode constructFieldRef(Tree tree) throws GenerateException {
		// this may be a static member, in which case just directly go there
		IAstTypedExpr idExpr = checkConstruct(tree.getChild(0),IAstTypedExpr.class);
		IScope theScope = null;
		
		if (idExpr instanceof IAstSymbolExpr) {
			IAstSymbolExpr symExpr = (IAstSymbolExpr) idExpr;
			IAstNode def = symExpr.getBody();
			if (def == null) {
				def = symExpr.getSymbol().getDefinition();
			}
			if (def instanceof IAstScope)
				theScope = ((IAstScope) def).getScope();
		}
		
		String name = tree.getChild(1).getText();
		if (theScope != null && theScope.get(name) != null) {
			idExpr = new AstSymbolExpr(false, theScope.get(name));
			getSource(tree, idExpr);
		} else {
			IAstName nameNode = new AstName(name, null);
			getSource(tree, nameNode);
			idExpr = new AstDerefExpr(idExpr, false);
			getSource(tree, idExpr);
			idExpr = new AstFieldExpr(idExpr, nameNode);
			getSource(tree, idExpr);
		}
		return idExpr;
	}
	
	private IAstTypedExpr constructAddrOf(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstAddrOfExpr addr = new AstAddrOfExpr(expr);
		getSource(tree, addr);
		return addr;
	}
	private IAstTypedExpr constructSizeOf(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstSizeOfExpr addr = new AstSizeOfExpr(expr);
		getSource(tree, addr);
		return addr;
	}
	private IAstTypedExpr constructTypeOf(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstTypeOfExpr addr = new AstTypeOfExpr(expr);
		getSource(tree, addr);
		return addr;
	}

	/**
	 * An initializer list.
	 * 
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructInitList(Tree tree) throws GenerateException {
		IAstNodeList<IAstInitNodeExpr> initExprs = new AstNodeList<IAstInitNodeExpr>(
				IAstInitNodeExpr.class);
		int index = 0;
		for (Tree kid : iter(tree)) {
			IAstInitNodeExpr initNode = checkConstruct(kid,
					IAstInitNodeExpr.class);
			initExprs.add(initNode);

			if (initNode.getContext() == null) {
				IAstIntLitExpr context = new AstIntLitExpr("" + index,
						typeEngine.INT, index);
				getEmptySource(tree, context);
				initNode.setContext(context);
			} else if (initNode.getContext() instanceof IAstInitIndexExpr) {
				index = (int) ((IAstIntLitExpr) ((IAstInitIndexExpr) initNode
						.getContext()).getIndex()).getValue();
			} else if (initNode.getContext() instanceof IAstIntLitExpr) {
				index = (int) ((IAstIntLitExpr) initNode.getContext())
						.getValue();
			}
			index++;
		}
		IAstInitListExpr list = new AstInitListExpr(null, initExprs);
		getSource(tree, initExprs);
		getSource(tree, list);

		return list;
	}

	private IAstInitNodeExpr constructInitNodeExpr(Tree tree)
			throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstTypedExpr context = null;
		if (tree.getChildCount() == 1) {
			context = null;
		} else if (tree.getChild(1).getType() == EulangParser.ID) {
			IAstName name = new AstName(tree.getChild(1).getText());
			getSource(tree, name);
			context = new AstFieldExpr(null, name);
			getSource(tree, context);
		} else {
			IAstTypedExpr indexExpr = checkConstruct(tree.getChild(1),
					IAstTypedExpr.class);
			indexExpr.simplify(typeEngine);
			if (!(indexExpr instanceof IAstIntLitExpr))
				throw new GenerateException(tree.getChild(1),
						"an index expression must be a compile-time constant");
			context = new AstInitIndexExpr(indexExpr);
			getSource(tree, context);
			// index = (int) ((IAstIntLitExpr) indexExpr).getValue();
		}
		IAstInitNodeExpr initNode = new AstInitNodeExpr(context, expr);
		getSource(tree, initNode);
		return initNode;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructForward(Tree tree) throws GenerateException {
		for (Tree id : iter(tree)) {
			String name = id.getText();

			ISymbol symbol = currentScope.get(name);
			if (symbol != null)
				throw new GenerateException(id, "redefining " + name);

			IAstName nameNode = new AstName(name, currentScope);
			getSource(id, nameNode);

			if (symbol == null) {
				symbol = currentScope.add(nameNode);
				if (DUMP)
					System.out.println("Creating " + symbol + " #"
							+ symbol.getNumber());
			}
		}
		return null;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructData(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedNode> fields = new AstNodeList<IAstTypedNode>(
				IAstTypedNode.class);
		IAstNodeList<IAstTypedNode> statics = new AstNodeList<IAstTypedNode>(
				IAstTypedNode.class);
		IAstNodeList<IAstStmt> stmts = new AstNodeList<IAstStmt>(
				IAstStmt.class);
		List<IAstRedefinition> redefs = new ArrayList<IAstRedefinition>();

		pushScope(new NamespaceScope(currentScope));
		
		// make type early so we can identify it 
		AstDataType dataType = new AstDataType(typeEngine, currentName,
				stmts, fields, statics, redefs, currentScope);

		try {
			for (Tree kid : iter(tree)) {
				if (kid.getType() == EulangParser.DEFINE) {
					IAstDefineStmt define = constructDefine(kid);
					if (define != null)
						stmts.add(define);
					continue;
				}
				if (kid.getType() == EulangParser.REDEF) {
					IAstRedefinition redef = constructRedefinition(kid);
					redefs.add(redef);
					continue;
				}

				IAstTypedNode item = checkConstruct(kid, IAstTypedNode.class);

				IAstNodeList<IAstTypedNode> theList = fields;
				if (item instanceof IAstAttributes && ((IAstAttributes) item).hasAttr(IAttrs.STATIC)) 
					theList = statics;
				
				// XXX codeptr
				if (item instanceof IAstAllocStmt) {
					IAstAllocStmt alloc = (IAstAllocStmt) item;
					
					// push attrs from default into field
					for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
						IAstTypedExpr val = alloc.getDefaultFor(i);
						if (val instanceof IAstAttributes) {
							alloc.attrs().addAll(((IAstAttributes) val).getAttrs());
						}
						IAstType type = alloc.getTypeExpr();
						if (type instanceof IAstPointerType && ((IAstPointerType) type).getBaseType() instanceof IAstPrototype)
							type = ((IAstPointerType) type).getBaseType();
 						if (type instanceof IAstAttributes) {
							alloc.attrs().addAll(((IAstAttributes) type).getAttrs());
							
							if (val == null && type instanceof IAstPrototype && alloc.hasAttr(IAstAttributes.THIS)) {
								adjustPrototypeForThisCall(tree, (IAstPrototype) type);
								// destroy the types so we re-discover them (TODO: cleanup) 
								type.setType(null);
								alloc.getTypeExpr().setType(null);
								alloc.getSymbolExprs().list().get(i).getSymbol().setType(null);
								alloc.getSymbolExprs().list().get(i).setType(null);
							}
 						}
					}
					
					// convert any prototypes to pointers
					if (alloc.getTypeExpr() != null
							&& alloc.getTypeExpr() instanceof IAstPrototype) {
						alloc.getTypeExpr().setParent(null);
						IAstPointerType ptr = new AstPointerType(typeEngine,
								alloc.getTypeExpr());
						ptr.setSourceRef(alloc.getTypeExpr().getSourceRef());
						alloc.setTypeExpr(ptr);
					}
				}
				if (item != null)
					theList.add(item);
			}
			getSource(tree, statics);
			getSource(tree, fields);

			getEmptySource(tree, stmts);

			getSource(tree, dataType);

			// ping to notice the change
			dataType.setFields(fields);
			if (dataType.getTypeName() != null)
				dataType.setType(dataType.createDataType(typeEngine));			
			return dataType;
		} finally {
			popScope(tree);
		}
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructIndex(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		expr = new AstDerefExpr(expr, false);
		getSource(tree.getChild(0), expr);

		for (int  i = 1; i < tree.getChildCount(); i++) {
			IAstTypedExpr at = checkConstruct(tree.getChild(i), IAstTypedExpr.class);
			//expr = new AstIndexExpr(expr, at);
			expr = new AstBinExpr(IOperation.INDEX, expr, at);
			
			getSource(tree, expr);
		}
		expr = new AstDerefExpr(expr, false);
		getSource(tree, expr);
		return expr;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructBreak(Tree tree) throws GenerateException {
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstBreakStmt breakStmt = new AstBreakStmt(expr);
		getSource(tree, breakStmt);
		return breakStmt;
	}

	private IAstNode constructWhile(Tree tree) throws GenerateException {
		pushScope(new LocalScope(currentScope));
		try {
			IAstTypedExpr expr = checkConstruct(tree.getChild(0),
					IAstTypedExpr.class);
			IAstTypedExpr body = checkConstruct(tree.getChild(1),
					IAstTypedExpr.class);
			IAstWhileExpr while_ = new AstWhileExpr(currentScope, expr, body);
			getSource(tree, while_);
			return while_;
		} finally {
			popScope(tree);
		}
	}

	private IAstNode constructFor(Tree tree) throws GenerateException {
		pushScope(new LocalScope(currentScope));
		try {
			IAstNodeList<IAstSymbolExpr> symExprs = new AstNodeList<IAstSymbolExpr>(
					IAstSymbolExpr.class);

			for (Tree symtree : iter(tree.getChild(0))) {
				IAstSymbolExpr sym = createSymbol(symtree);
				sym.getSymbol().setDefinition(sym);
				symExprs.add(sym);
			}
			getSource(tree, symExprs);
			int count = symExprs.nodeCount();

			int idx = 1;
			IAstTypedExpr byExpr = null;
			if (tree.getChild(idx).getType() == EulangParser.AT) {
				unhandled(tree.getChild(idx));
			} else if (tree.getChild(idx).getType() == EulangParser.BY) {
				byExpr = checkConstruct(tree.getChild(idx++).getChild(0),
						IAstTypedExpr.class);
			}
			if (byExpr == null) {
				byExpr = new AstIntLitExpr("" + count, typeEngine.INT, count);
				getEmptySource(tree.getChild(idx), byExpr);
			}

			IAstTypedExpr expr = checkConstruct(tree.getChild(idx++),
					IAstTypedExpr.class);
			IAstTypedExpr body = checkConstruct(tree.getChild(idx++),
					IAstTypedExpr.class);
			IAstForExpr forEx = new AstForExpr(currentScope, symExprs, byExpr,
					expr, body);
			getSource(tree, forEx);
			return forEx;
		} finally {
			popScope(tree);
		}
	}

	private IAstNode constructDoWhile(Tree tree) throws GenerateException {
		pushScope(new LocalScope(currentScope));
		try {
			IAstTypedExpr body = checkConstruct(tree.getChild(0),
					IAstTypedExpr.class);
			IAstTypedExpr expr = checkConstruct(tree.getChild(1),
					IAstTypedExpr.class);
			IAstDoWhileExpr dowhile = new AstDoWhileExpr(currentScope, body,
					expr);
			getSource(tree, dowhile);
			return dowhile;
		} finally {
			popScope(tree);
		}
	}

	private IAstNode constructRepeat(Tree tree) throws GenerateException {
		pushScope(new LocalScope(currentScope));
		try {
			IAstTypedExpr expr = checkConstruct(tree.getChild(0),
					IAstTypedExpr.class);
			IAstTypedExpr body = checkConstruct(tree.getChild(1),
					IAstTypedExpr.class);
			IAstRepeatExpr repeat = new AstRepeatExpr(currentScope, expr, body);
			getSource(tree, repeat);
			return repeat;
		} finally {
			popScope(tree);
		}
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructList(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedExpr> list = new AstNodeList<IAstTypedExpr>(
				IAstTypedExpr.class);

		for (Tree kid : iter(tree)) {
			list.add(checkConstruct(kid, IAstTypedExpr.class));
		}
		getSource(tree, list);
		return list;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstTupleExpr constructTuple(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedExpr> elements = new AstNodeList<IAstTypedExpr>(
				IAstTypedExpr.class);
		for (Tree kid : iter(tree)) {
			elements.add(checkConstruct(kid, IAstTypedExpr.class));
		}
		getSource(tree, elements);
		IAstTupleExpr node = new AstTupleExpr(elements);
		getSource(tree, node);
		return node;
	}

	/**
	 * @param tree
	 * @return
	 * @throws GenerateException
	 */
	private IAstNode constructCondList(Tree tree) throws GenerateException {
		IAstNodeList<IAstCondExpr> condExprList = new AstNodeList<IAstCondExpr>(
				IAstCondExpr.class);
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
		IAstTypedExpr test = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstTypedExpr expr = checkConstruct(tree.getChild(1),
				IAstTypedExpr.class);
		// flatten code blocks
		if (expr instanceof IAstCodeExpr) {
			// TODO: we need to shove the scope somewhere!
			IAstCodeExpr origCode = ((IAstCodeExpr) expr);
			origCode.stmts().setParent(null);
			expr = new AstStmtListExpr(origCode.stmts());
			expr.setSourceRef(origCode.getSourceRef());
			/*
			 * IAstNodeList<IAstTypedExpr> args = new
			 * AstNodeList<IAstTypedExpr>(); getEmptySource(tree.getChild(1),
			 * args); expr = new AstFuncCallExpr(expr, args); getSource(tree,
			 * expr);
			 */
		}
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
			IAstNodeList<IAstStmt> stmtList = checkConstruct(tree.getChild(0),
					IAstNodeList.class);

			IAstBlockStmt block = new AstBlockStmt(stmtList, currentScope);
			getSource(tree, block);
			return block;
		} finally {
			popScope(tree);
		}
	}

	private IAstNode constructGotoStmt(Tree tree) throws GenerateException {
		IAstSymbolExpr label = checkConstruct(tree.getChild(0),
				IAstSymbolExpr.class);
		label.getSymbol().setType(typeEngine.LABEL);

		IAstTypedExpr test = null;
		if (tree.getChildCount() == 2)
			test = checkConstruct(tree.getChild(1), IAstTypedExpr.class);

		IAstGotoStmt gotoStmt = new AstGotoStmt(label, test);
		getSource(tree, gotoStmt);
		return gotoStmt;
	}

	private TempLabelStmt constructLabelStmt(Tree tree)
			throws GenerateException {
		// ^(LABELSTMT ^(LABEL idRef) codeStmt)
		IAstLabelStmt label = checkConstruct(tree.getChild(0),
				IAstLabelStmt.class);
		IAstStmt stmt = checkConstruct(tree.getChild(1), IAstStmt.class);
		return new TempLabelStmt(label, stmt);
	}

	private IAstNode constructLabel(Tree tree) throws GenerateException {
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

		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstStmt stmt = new AstExprStmt(expr);
		getSource(tree, stmt);
		return stmt;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructArgList(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedExpr> argList = new AstNodeList<IAstTypedExpr>(
				IAstTypedExpr.class);
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
	public IAstNode constructCallOrConstruct(Tree tree) throws GenerateException {
		assert tree.getChildCount() == 2;

		IAstTypedExpr function = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);

		return constructCallOrConstruct(tree.getChild(1), function);
	}

	/**
	 * @param child
	 * @param function
	 * @return
	 * @throws GenerateException
	 */
	@SuppressWarnings("unchecked")
	private IAstTypedExpr constructCallOrConstruct(Tree tree, IAstTypedExpr function)
			throws GenerateException {
		IAstNodeList<IAstTypedExpr> args = checkConstruct(tree,
				IAstNodeList.class);

		// check for a constructor
		IAstTypedExpr functionSym = function;
		if (functionSym instanceof IAstDerefExpr)
			functionSym = ((IAstDerefExpr) functionSym).getExpr();

		if (args.nodeCount() == 1 && function instanceof IAstSymbolExpr) {
			ISymbol funcSym = ((IAstSymbolExpr) function).getSymbol();
			IAstNode symdef = funcSym.getDefinition();
			if (symdef instanceof IAstType
					&& ((IAstType) symdef).getType() != null) {
				IAstTypedExpr[] argNodes = args.getNodes(IAstTypedExpr.class);
				argNodes[0].setParent(null);
				IAstUnaryExpr castExpr = new AstUnaryExpr(IOperation.CAST,
						argNodes[0]);
				castExpr.setType(((IAstType) symdef).getType());
				castExpr.setTypeFixed(true);
				getSource(tree, castExpr);
				return castExpr;
			}
		}

		IAstFuncCallExpr funcCall = new AstFuncCallExpr(function, args);
		getSource(tree, funcCall);
		return funcCall;
	}

	@SuppressWarnings("unchecked")
	public IAstNode constructAlloc(Tree tree) throws GenerateException {
		int idx = 0;
		
		Set<String> attrs = Collections.emptySet();
		
		if (tree.getChildCount() > idx && tree.getChild(idx).getType() == EulangParser.ATTRS) {
			attrs = constructAttrs(tree.getChild(idx++));
		}
		
		IAstType type;
		if (tree.getChildCount() > idx) {
			type = checkConstruct(tree
					.getChild(idx++), IAstType.class);
		}
		else
			type = null;
		

		// XXX codeptr
		// promote code allocs to pointers to function
		boolean isCode = false;
		if (type instanceof IAstPrototype) {
			isCode = true;
			type.setParent(null);
			
			/*
			if (((IAstPrototype) type).hasAttr(IAttrs.THIS)) {
				adjustPrototypeForThisCall(tree, (IAstPrototype) type);
			}
			*/
			
			
			IAstPointerType ptr = new AstPointerType(typeEngine,
					type);
			ptr.setSourceRef(type.getSourceRef());
			type = ptr;
		}
		
		IAstAllocStmt alloc = null;

		IAstNodeList<IAstTypedExpr> exprlist = null;
		
		if (tree.getChild(idx).getType() == EulangParser.ID) {
			IAstSymbolExpr symbolExpr = createSymbol(tree.getChild(idx++));

			if (type != null) {
				symbolExpr.getSymbol().setType(type.getType());
			}

			IAstNodeList<IAstSymbolExpr> idlist = AstNodeList
					.<IAstSymbolExpr> singletonList(IAstSymbolExpr.class,
							symbolExpr);

			if (tree.getChildCount() > idx) {
				
				IAstNode init = null;

				if (isCode) {
					init = constructCodeForAlloc(type, tree.getChild(idx++));
				} else {
					init = construct(tree.getChild(idx++));
				}
					
				if (init instanceof IAstNodeList)
					exprlist = (IAstNodeList<IAstTypedExpr>) init;
				else {
					IAstTypedExpr expr = (IAstTypedExpr) init;
					exprlist = AstNodeList.<IAstTypedExpr> singletonList(
							IAstTypedExpr.class, expr);
				}
					
					/*
					if (isCode) {
						for (IAstTypedExpr expr : exprlist.list()) {
							if (expr instanceof IAstCodeExpr) {
								LLType exprType = expr.getType();
								IAstPrototype proto = exprType instanceof IAstPointerType  && ((IAstPointerType) exprType).getBaseType() instanceof IAstPrototype ?
										(IAstPrototype)((IAstPointerType) exprType).getBaseType() : exprType instanceof IAstPrototype ? (IAstPrototype) exprType : typeProto;
										
								if ((attrs.contains(IAttrs.THIS) || (proto != null && !proto.hasAttr(IAttrs.THIS)))  
										&& !((IAstCodeExpr) expr).hasAttr(IAttrs.THIS)) {
									adjustPrototypeForThisCall(tree, ((IAstCodeExpr) expr).getPrototype());
								}
							}
						}
					}
				} finally {
					if (isCode) {
						popScope(tree.getChild(idx-1));
					}
				}
					 */
			}

			alloc = new AstAllocStmt(idlist, type, exprlist, false, attrs);
			getSource(tree, alloc);

			symbolExpr.getSymbol().setDefinition(alloc);

		} else if (tree.getChild(idx).getType() == EulangParser.LIST) {
			IAstNodeList<IAstSymbolExpr> idlist = new AstNodeList<IAstSymbolExpr>(
					IAstSymbolExpr.class);

			boolean expand = false;

			for (Tree kid : iter(tree.getChild(idx))) {
				IAstSymbolExpr symbolExpr = createSymbol(kid);

				if (type != null) {
					symbolExpr.getSymbol().setType(type.getType());
				}
				idlist.add(symbolExpr);
			}
			getSource(tree.getChild(idx++), idlist);
			
			if (tree.getChildCount() > idx) {
				if (tree.getChild(idx).getType() == EulangParser.PLUS) {
					expand = true;
					idx++;
				}
			}

			if (tree.getChildCount() > idx) {
				exprlist = new AstNodeList<IAstTypedExpr>(IAstTypedExpr.class);
				for (Tree kid : iter(tree.getChild(idx))) {
					IAstTypedExpr expr = checkConstruct(kid,
							IAstTypedExpr.class);
					exprlist.add(expr);
				}
				getSource(tree.getChild(idx), exprlist);
				idx++;
			}

			if (exprlist != null && exprlist.nodeCount() != idlist.nodeCount()
					&& exprlist.nodeCount() != 1) {
				throw new GenerateException(
						tree,
						"multi-allocation statement has incompatible number of identifiers and expressions "
								+ idlist.nodeCount()
								+ " != "
								+ exprlist.nodeCount());
			}
			if (expand && (exprlist == null || exprlist.nodeCount() != 1))
				throw new GenerateException(tree,
						"expand modifier ('+') makes no sense without a singular expression");

			alloc = new AstAllocStmt(idlist, type, exprlist, expand, attrs);
			getSource(tree, alloc);

			for (IAstSymbolExpr symbolExpr : idlist.list())
				symbolExpr.getSymbol().setDefinition(alloc);

		} else {
			unhandled(tree);
			return null;
		}

		if (alloc.getTypeExpr() != null
				&& alloc.getTypeExpr() instanceof IAstArrayType
				&& isZero(((IAstArrayType) alloc.getTypeExpr()).getCount())
				&& (alloc.getExprs() == null || alloc.getExprs().nodeCount() == 0)) {
			throw new GenerateException(tree,
					"cannot allocate unsized array without an initializer");
		}
		
		if (exprlist != null) {
			for (IAstTypedExpr expr : exprlist.list()) {
				validateAllocExpr(expr);
			}
		}

		setLHS(alloc.getSymbolExprs());
		return alloc;
	}

	/**
	 * @param type
	 * @param tree 
	 * @throws GenerateException 
	 */
	private IAstNode constructCodeForAlloc(IAstType type, Tree tree) throws GenerateException {
		IAstNode init = null;
		try {
			IAstPrototype typeProto = null;
			pushScope(new LocalScope(currentScope));
			
			typeProto = type instanceof IAstPointerType  && ((IAstPointerType) type).getBaseType() instanceof IAstPrototype ?
					(IAstPrototype)((IAstPointerType) type).getBaseType() : type instanceof IAstPrototype ? (IAstPrototype) type : null;

			if (typeProto != null && tree.getType() == EulangParser.CODE) {
				
				//if (typeProto.hasAttr(IAttrs.THIS)) {
				//	adjustPrototypeForThisCall(tree, typeProto);
				//}
				
				init = constructCodeExpr(tree, typeProto);
			} else {
				init = construct(tree);
			}

			/*
			if (isCode) {
				for (IAstTypedExpr expr : exprlist.list()) {
					if (expr instanceof IAstCodeExpr) {
						LLType exprType = expr.getType();
						IAstPrototype proto = exprType instanceof IAstPointerType  && ((IAstPointerType) exprType).getBaseType() instanceof IAstPrototype ?
								(IAstPrototype)((IAstPointerType) exprType).getBaseType() : exprType instanceof IAstPrototype ? (IAstPrototype) exprType : typeProto;
								
						if ((attrs.contains(IAttrs.THIS) || (proto != null && !proto.hasAttr(IAttrs.THIS)))  
								&& !((IAstCodeExpr) expr).hasAttr(IAttrs.THIS)) {
							adjustPrototypeForThisCall(tree, ((IAstCodeExpr) expr).getPrototype());
						}
					}
				}
			}*/
			
		} finally {
			popScope(tree);

		}
		return init;
	}

	public IAstNode constructAllocTuple(Tree tree) throws GenerateException {
		int idx = 0;

		Set<String> attrs = Collections.emptySet();
		if (tree.getChild(idx).getType() == EulangParser.ATTRS) {
			attrs = constructAttrs(tree.getChild(idx++));
		}
			
		IAstType type = tree.getChildCount() > idx ? checkConstruct(tree
				.getChild(idx++), IAstType.class) : null;

		// XXX codeptr
		// promote code allocs to pointers to function
		if (type instanceof IAstPrototype) {
			type.setParent(null);
			IAstPointerType ptr = new AstPointerType(typeEngine,
					type);
			ptr.setSourceRef(type.getSourceRef());
			type = ptr;
		}
		
		if (tree.getChild(idx).getType() == EulangParser.TUPLE) {
			int symIdx = idx;
			IAstTupleNode syms = constructIdTuple(tree.getChild(idx++));

			IAstTypedExpr expr = null;
			if (tree.getChildCount() > idx)
				expr = checkConstruct(tree.getChild(idx++), IAstTypedExpr.class);

			validateAllocExpr(expr);
			
			IAstAllocTupleStmt tupleAlloc = new AstAllocTupleStmt(syms, type,
					expr, attrs);
			getSource(tree, tupleAlloc);

			if (type != null) {
				for (IAstTypedExpr elExpr : syms.elements().list()) {
					if (!(elExpr instanceof IAstSymbolExpr))
						throw new GenerateException(tree.getChild(symIdx),
								"can only tuple-allocate locals");
					IAstSymbolExpr symExpr = (IAstSymbolExpr) elExpr;
					symExpr.getSymbol().setType(type.getType());
					symExpr.getSymbol().setDefinition(tupleAlloc.getExpr());
				}
			}

			setLHS(tupleAlloc.getSymbols());
			return tupleAlloc;
		} else {
			unhandled(tree);
			return null;
		}
	}

	/**
	 * @param expr
	 */
	private void validateAllocExpr(IAstTypedExpr expr) throws GenerateException {
		if (expr instanceof IAstCodeExpr && ((IAstCodeExpr) expr).hasAttr(IAttrs.MACRO)) {
			throw new GenerateException(expr.getSourceRef(), "cannot define #macro allocations");
		}
	}

	/**
	 * @param symbolExprs
	 */
	private void setLHS(IAstNode node) {
		if (node instanceof IAstDerefExpr) {
			((IAstDerefExpr) node).setLHS(true);
		}
		for (IAstNode kid : node.getChildren())
			setLHS(kid);
	}

	/**
	 * @param count
	 * @return
	 */
	private boolean isZero(IAstTypedExpr count) {
		return count == null;
				/*|| (count instanceof IAstIntLitExpr && ((IAstIntLitExpr) count)
						.getValue() == 0)*/
	}

	/**
	 * @param child
	 * @return
	 * @throws GenerateException
	 */
	private IAstSymbolExpr createSymbol(Tree id) throws GenerateException {
		String name = id.getText();

		ISymbol symbol = currentScope.get(name);
		IAstNode def = symbol != null ? symbol.getDefinition() : null;
		if (symbol != null && def != null && !isMacroArg(def)) {
			throw new GenerateException(id, "redefining " + name);
		}
		IAstName nameNode = new AstName(name, currentScope);
		getSource(id, nameNode);

		if (symbol == null) {
			symbol = currentScope.add(nameNode);
			if (DUMP)
				System.out.println("Creating " + symbol + " #"
						+ symbol.getNumber());
		}

		IAstSymbolExpr symbolExpr = new AstSymbolExpr(true, symbol);
		symbolExpr.setSourceRef(nameNode.getSourceRef());

		return symbolExpr;
	}

	/**
	 * @param definition
	 * @return
	 */
	private boolean isMacroArg(IAstNode definition) {
		return definition instanceof IAstArgDef
				&& ((IAstArgDef) definition).hasAttr(IAstAttributes.MACRO);
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructAssign(Tree tree) throws GenerateException {
		int child1Type = tree.getChild(1).getType();
		if (child1Type == EulangParser.LIST) {
			IOperation op = getOperation(tree.getChild(0));
			IAstNodeList<IAstTypedExpr> symbols = new AstNodeList<IAstTypedExpr>(
					IAstTypedExpr.class);
			IAstNodeList<IAstTypedExpr> exprs = new AstNodeList<IAstTypedExpr>(
					IAstTypedExpr.class);

			boolean expand = false;
			int idx = 1;
			for (Tree kid : iter(tree.getChild(idx))) {
				IAstTypedExpr left = checkConstruct(kid, IAstTypedExpr.class);
				symbols.add(left);
			}
			getSource(tree.getChild(idx), symbols);
			++idx;
			if (tree.getChild(idx).getType() == EulangParser.PLUS) {
				expand = true;
				idx++;
			}
			for (Tree kid : iter(tree.getChild(idx))) {
				IAstTypedExpr right = checkConstruct(kid, IAstTypedExpr.class);
				exprs.add(right);
			}
			getSource(tree.getChild(idx), exprs);
			IAstAssignStmt assign = new AstAssignStmt(op, symbols, exprs,
					expand);
			setLHS(exprs);
			getSource(tree, assign);
			return assign;
		} else if (child1Type == EulangParser.TUPLE) {
			// no operation
			IAstTupleNode left = constructIdTuple(tree.getChild(1));
			IAstTypedExpr right = checkConstruct(tree.getChild(2),
					IAstTypedExpr.class);
			IAstAssignTupleStmt assign = new AstAssignTupleStmt(left, right);
			setLHS(left);
			getSource(tree, assign);
			return assign;

		} else {
			//if (child1Type == EulangParser.IDEXPR || child1Type == EulangParser.IDREF) {
			
				IOperation op = getOperation(tree.getChild(0));
				IAstTypedExpr left = checkConstruct(tree.getChild(1),
						IAstTypedExpr.class);
				IAstTypedExpr right = checkConstruct(tree.getChild(2),
						IAstTypedExpr.class);
				IAstAssignStmt assign = new AstAssignStmt(op, AstNodeList
						.<IAstTypedExpr> singletonList(IAstTypedExpr.class, left),
						AstNodeList.<IAstTypedExpr> singletonList(
								IAstTypedExpr.class, right), false);
				setLHS(assign.getSymbolExprs());
				getSource(tree, assign);
				return assign;
			//} else 
			//unhandled(tree);
			//return null;
		}
	}

	/**
	 * @param child
	 * @return
	 * @throws GenerateException
	 */
	private IOperation getOperation(Tree child) throws GenerateException {
		switch (child.getType()) {
		case EulangParser.EQUALS:
			return IOperation.MOV;
		case EulangParser.PLUS_EQ:
			return IOperation.ADD;
		case EulangParser.MINUS_EQ:
			return IOperation.SUB;
		case EulangParser.STAR_EQ:
			return IOperation.MUL;
		case EulangParser.SLASH_EQ:
			return IOperation.DIV;
		case EulangParser.REM_EQ:
			return IOperation.REM;
		case EulangParser.UDIV_EQ:
			return IOperation.UDIV;
		case EulangParser.UREM_EQ:
			return IOperation.UREM;
		case EulangParser.MOD_EQ:
			return IOperation.MOD;
		case EulangParser.LSHIFT_EQ:
			return IOperation.SHL;
		case EulangParser.RSHIFT_EQ:
			return IOperation.SAR;
		case EulangParser.URSHIFT_EQ:
			return IOperation.SHR;
		case EulangParser.CRSHIFT_EQ:
			return IOperation.SRC;
		case EulangParser.CLSHIFT_EQ:
			return IOperation.SLC;
		case EulangParser.XOR_EQ:
			return IOperation.BITXOR;
		case EulangParser.OR_EQ:
			return IOperation.BITOR;
		case EulangParser.AND_EQ:
			return IOperation.BITAND;
		default:
			throw new GenerateException(child, "unknown assign operation");
		}
	}

	/**
	 * @param child
	 * @return
	 * @throws GenerateException
	 */
	private IAstTupleNode constructIdTuple(Tree tree) throws GenerateException {
		IAstNodeList<IAstTypedExpr> elements = new AstNodeList<IAstTypedExpr>(
				IAstTypedExpr.class);
		for (Tree kid : iter(tree)) {
			elements.add(checkConstruct(kid, IAstTypedExpr.class));
		}
		getSource(tree, elements);
		IAstTupleNode node = new AstTupleNode(elements);
		getSource(tree, node);
		return node;
	}

	public IAstTypedExpr constructIdRef(Tree tree) throws GenerateException {
		// could have ':'s
		IScope startScope = currentScope;
		int idx = 0;
		boolean inScope = false;

		// go up through scope backtracks...
		while (idx < tree.getChildCount()) {
			Tree kid = tree.getChild(idx);
			if (kid.getType() == EulangParser.COLON) {
				if (startScope.getParent() == null) {
					throw new GenerateException(tree,
							"Cannot go out of module scope");
				} else {
					startScope = startScope.getParent();
					
					// these have a dummy scope for use by generics; not a valid search scope
					if (startScope.getOwner() instanceof IAstDefineStmt)
						startScope = startScope.getParent();
				}
				inScope = true;
			} else {
				break;
			}
			idx++;
		}

		// find a symbol
		IAstTypedExpr idExpr = null;
		IAstSymbolExpr symExpr = null;

		if (idx < tree.getChildCount()) {
			Tree kid = tree.getChild(idx);
			if (kid.getType() == EulangParser.ID || kid.getType() == EulangParser.IDREF) {
				ISymbol symbol = null;
				if (inScope) {
					symbol = startScope.get(kid.getText());
				} else {
					symbol = startScope.search(kid.getText());
				}
				if (symbol == null) {
					if (inScope) {
						throw new GenerateException(kid,
								"Cannot resolve name in scope: "
										+ tree.toStringTree());
					}
					// make forward
					symbol = startScope.add(new AstName(kid.getText()));
				}
				startScope = symbol.getScope();

				// for 'this' refs, break now and handle below
				if (startScope.getOwner() instanceof IAstDataType /*&& symbol.getDefinition() instanceof IAstAllocStmt*/
						/*&& TODO: is a non-static field */) {
					
					LLDataType type = (LLDataType) ((IAstDataType) startScope.getOwner()).getType();
					boolean needsThis = false;
					needsThis = (type.getField(symbol.getName()) instanceof LLInstanceField);
					
					ISymbol thisSym = currentScope.search("this");
					if (thisSym != null) {
						symbol = thisSym;
						idx--;
					} else if (needsThis) {
						throw new GenerateException(tree, "cannot reference instance field ' "+symbol.getName() + "' from this scope (try #this)");
					}
				}
				symExpr = new AstSymbolExpr(false, symbol);
				getSource(tree, symExpr);

				idExpr = symExpr;
			} else {
				unhandled(kid);
				return null;
			}
			idx++;
		}

		if (idExpr == null) {
			throw new GenerateException(tree,
					"Cannot resolve symbol or expression: "
							+ tree.toStringTree());
		}

		if (idExpr instanceof IAstSymbolExpr
				&& idExpr.getType() instanceof LLGenericType) {
			// shunt type variables into the scope below the define's
			idExpr = moveSymbolOutOfDefine((IAstSymbolExpr) idExpr);
		}

		// idExpr = new AstValueExpr(idExpr);
		// getSource(tree.getChild(0), idExpr);

		// may have field references
		while (idx < tree.getChildCount()) {
			Tree kid = tree.getChild(idx++);
			String name = kid.getText();
			if (symExpr != null) {
				IAstNode node = symExpr.getBody();
				if (node instanceof IAstScope) {
					ISymbol sym = ((IAstScope) node).getScope().get(name);
					if (sym != null) {
						symExpr = new AstSymbolExpr(false, sym);
						getSource(kid, symExpr);
						idExpr = symExpr;
						continue;
					}
				}
			}
			IAstName nameNode = new AstName(name,
					symExpr != null ? symExpr.getSymbol().getScope() : null);
			getSource(kid, nameNode);
			idExpr = new AstDerefExpr(idExpr, false);
			getSource(tree.getChild(0), idExpr);
			idExpr = new AstFieldExpr(idExpr, nameNode);
			getSource(tree, idExpr);
			symExpr = null;
		}
		return idExpr;
	}

	public IAstBinExpr constructBinaryExpr(Tree tree) throws GenerateException {
		assert (tree.getChildCount() == 2);
		IAstTypedExpr left = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstTypedExpr right = checkConstruct(tree.getChild(1),
				IAstTypedExpr.class);
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
		case EulangParser.REM:
			binop = new AstBinExpr(IOperation.REM, left, right);
			break;
		case EulangParser.UDIV:
			binop = new AstBinExpr(IOperation.UDIV, left, right);
			break;
		case EulangParser.UREM:
			binop = new AstBinExpr(IOperation.UREM, left, right);
			break;
		case EulangParser.MOD:
			binop = new AstBinExpr(IOperation.MOD, left, right);
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
		case EulangParser.CRSHIFT:
			binop = new AstBinExpr(IOperation.SRC, left, right);
			break;
		case EulangParser.CLSHIFT:
			binop = new AstBinExpr(IOperation.SLC, left, right);
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
		case EulangParser.UGREATER:
			binop = new AstBinExpr(IOperation.COMPUGT, left, right);
			break;
		case EulangParser.COMPGE:
			binop = new AstBinExpr(IOperation.COMPGE, left, right);
			break;
		case EulangParser.COMPUGE:
			binop = new AstBinExpr(IOperation.COMPUGE, left, right);
			break;
		case EulangParser.LESS:
			binop = new AstBinExpr(IOperation.COMPLT, left, right);
			break;
		case EulangParser.ULESS:
			binop = new AstBinExpr(IOperation.COMPULT, left, right);
			break;
		case EulangParser.COMPLE:
			binop = new AstBinExpr(IOperation.COMPLE, left, right);
			break;
		case EulangParser.COMPULE:
			binop = new AstBinExpr(IOperation.COMPULE, left, right);
			break;

		case EulangParser.AND:
			binop = new AstBinExpr(IOperation.COMPAND, left, right);
			break;
		case EulangParser.OR:
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
		assert (tree.getChildCount() == 1);
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstUnaryExpr unary = null;

		switch (tree.getType()) {
		case EulangParser.INV:
			unary = new AstUnaryExpr(IOperation.INV, expr);
			break;
		// case EulangParser.NOT:
		// unary = new AstUnaryExpr(IOperation.NOT, expr);
		// break;
		case EulangParser.NEG:
			unary = new AstUnaryExpr(IOperation.NEG, expr);
			break;
		case EulangParser.POSTINC:
			unary = new AstUnaryExpr(IOperation.POSTINC, expr);
			break;
		case EulangParser.POSTDEC:
			unary = new AstUnaryExpr(IOperation.POSTDEC, expr);
			break;
		case EulangParser.PREINC:
			unary = new AstUnaryExpr(IOperation.PREINC, expr);
			break;
		case EulangParser.PREDEC:
			unary = new AstUnaryExpr(IOperation.PREDEC, expr);
			break;

		default:
			unhandled(tree);
			return null;
		}
		getSource(tree, unary);
		return unary;
	}

	public IAstBinExpr constructLogicalNot(Tree tree) throws GenerateException {
		assert (tree.getChildCount() == 1);
		IAstTypedExpr expr = checkConstruct(tree.getChild(0),
				IAstTypedExpr.class);
		IAstLitExpr zero = createZero(expr.getType());
		getEmptySource(tree, zero);
		IAstBinExpr binary = new AstBinExpr(IOperation.COMPEQ, expr, zero);

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
		return new AstIntLitExpr("0", typeEngine.getIntType(type != null ? type
				.getBits() : 1), 0);
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

		Set<String> attrs = Collections.emptySet();

		IAstSymbolExpr symExpr = createSymbol(tree.getChild(argIdx++));
		
		if (argIdx < tree.getChildCount() && tree.getChild(argIdx).getType() == EulangParser.ATTRS) {
			attrs = constructAttrs(tree.getChild(argIdx++));
		}

		IAstType type = null;
		IAstTypedExpr defaultVal = null;

		if (tree.getChildCount() > argIdx) {
			IAstTypedExpr expr = checkConstruct(tree.getChild(argIdx),
					IAstTypedExpr.class);
			if (expr instanceof IAstType) {
				// if (expr instanceof IAst)
				// if (tree.getChild(argIdx).getType() == EulangParser.TYPE) {
				// type = checkConstruct(tree.getChild(argIdx), IAstType.class);
				type = (IAstType) expr;
				argIdx++;
			}
			if (argIdx < tree.getChildCount()) {
				defaultVal = checkConstruct(tree.getChild(argIdx),
						IAstTypedExpr.class);
			}
		}

		boolean isMacro = attrs.contains(IAstAttributes.MACRO);
		boolean isVar = attrs.contains(IAstAttributes.VAR);
		
		if (isMacro && isVar)
			throw new GenerateException(tree,
					"cannot have a macro and '@' argument");

		IAstArgDef argDef = new AstArgDef(symExpr, type, defaultVal, attrs);
		getSource(tree, argDef);

		symExpr.getSymbol().setDefinition(argDef);

		return argDef;
	}

	/**
	 * @param tree
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IAstNodeList<IAstStmt> constructStmtList(Tree tree) {
		IAstNodeList<IAstStmt> list = new AstNodeList<IAstStmt>(IAstStmt.class);

		assert tree.getType() == EulangParser.STMTLIST;

		for (Tree kid : iter(tree)) {
			try {
				IAstNode node;
				if (kid.getType() == EulangParser.STMTLIST) {
					// top-level code
				
					currentScope = pushScope(new LocalScope(currentScope));
					try {
						IAstNodeList<IAstStmt> stmts = checkConstruct(kid, IAstNodeList.class);
						
						IAstPrototype proto = new AstPrototype(typeEngine.getCodeType(typeEngine.VOID, new LLType[0]), currentScope, null);
						IAstCodeExpr codeExpr = new AstCodeExpr(proto, currentScope, stmts, Collections.<String>emptySet());
						
						// get any errors
						ExpandAST expand = new ExpandAST(typeEngine, true);
						List<Message> messages = new ArrayList<Message>();
						expand.validate(messages, codeExpr);

						for (Message msg : messages)
							if (msg instanceof Error)
								errors.add((Error) msg);
						
						IAstFuncCallExpr funcCall = new AstFuncCallExpr(codeExpr, new AstNodeList<IAstTypedExpr>(IAstTypedExpr.class));

						IAstExprStmt stmt = new AstExprStmt(funcCall);
						stmt.setSourceRefTree(stmts.getSourceRef());
						
						list.add(stmt);
					} finally {
						popScope(kid);
					}
					continue;
				}
				
				node = construct(kid);
				if (node instanceof TempLabelStmt) {
					list.add(((TempLabelStmt) node).getLabel());
					list.add(((TempLabelStmt) node).getStmt());
				} else if (node instanceof IAstStmt) {
					list.add((IAstStmt) node);
				} else if (node != null) {
					throw new GenerateException(node.getSourceRef(), "unexpected content at module scope");
				}
				 
			} catch (GenerateException e) {
				emitExceptionError(e);
			}
		}

		getSource(tree, list);
		return list;
	}

	private void emitExceptionError(GenerateException e) {
		if (e.getTree() != null)
			error(e.getTree(), e.getMessage());
		else
			error(e.getSourceRef(), e.getMessage());
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstPrototype constructPrototype(Tree tree) throws GenerateException {
		IAstType retTypeNode;
		int start = 1;
		if (tree.getChildCount() == 0
				|| tree.getChild(0).getType() == EulangParser.ARGDEF) {
			retTypeNode = new AstType(typeEngine.UNSPECIFIED);
			getEmptySource(tree, retTypeNode);
			start = 0;
		} else if (tree.getChild(0).getType() == EulangParser.TUPLETYPE) {
			retTypeNode = constructType(tree.getChild(0));
			getSource(tree, retTypeNode);
		} else {
			retTypeNode = constructType(tree.getChild(0).getChild(0));
			getSource(tree, retTypeNode);
		}

		IAstArgDef[] argTypes = new IAstArgDef[tree.getChildCount() - start];
		int idx = 0;
		while (start < tree.getChildCount()) {
			argTypes[idx++] = checkConstruct(tree.getChild(start++),
					IAstArgDef.class);
		}

		IAstPrototype proto = new AstPrototype(typeEngine, retTypeNode,
				argTypes, Collections.<String>emptySet());
		getSource(tree, proto);
		return proto;
	}

	/**
	 * @param child
	 * @return
	 */
	public IAstType constructType(Tree tree) throws GenerateException {
		IAstType type = null;
		if (tree.getType() == EulangParser.NIL) {
			type = new AstType(typeEngine.VOID);
		} else if (tree.getType() == EulangParser.INSTANCE) {
			type = checkConstruct(tree, IAstType.class);
		} else {
			if (tree.getType() == EulangParser.TUPLETYPE) {
				LLType[] tupleTypes = new LLType[tree.getChildCount()];
				for (int idx = 0; idx < tree.getChildCount(); idx++) {
					// assert tree.getChild(idx).getType() == EulangParser.TYPE;
					IAstType stype = checkConstruct(tree.getChild(idx),
							IAstType.class);
					if (stype.getType() == null)
						throw new GenerateException(tree.getChild(idx),
								"tuple types must be concrete");
					tupleTypes[idx] = stype.getType();
				}
				type = new AstType(typeEngine.getTupleType(tupleTypes));
			} else if (tree.getType() == EulangParser.ARRAY) {
				type = checkConstruct(tree.getChild(0), IAstType.class);
				for (int idx = tree.getChildCount(); idx-- > 1;) {
					IAstTypedExpr countExpr = null;
					Tree kid = tree.getChild(idx);
					if (kid.getType() == EulangParser.FALSE) {
						if (idx != 1)
							throw new GenerateException(kid,
									"only the first array element can be variable");
					} else {
						countExpr = checkConstruct(kid, IAstTypedExpr.class);
					}
					try {
						type = new AstArrayType(typeEngine, type, countExpr);
					} catch (TypeException e) {
						throw new GenerateException(kid, e.getMessage());
					}
					getSource(tree, type);
				}

			} else if (tree.getType() == EulangParser.POINTER) {
				type = checkConstruct(tree.getChild(0), IAstType.class);
				type = new AstPointerType(typeEngine, type);
				getSource(tree, type);
			} else if (tree.getType() == EulangParser.REF) {
				type = checkConstruct(tree.getChild(0), IAstType.class);
				type = new AstRefType(typeEngine, type);
				getSource(tree, type);
			} else if (tree.getType() == EulangParser.CODE) {
				if (tree.getChildCount() == 0) {
					type = new AstType(typeEngine.getCodeType(null,
							(LLType[]) null));
				} else {
					int idx = 0;
					Set<String> attrs = Collections.emptySet();
					if (idx < tree.getChildCount() && tree.getChild(idx).getType() == EulangParser.ATTRS)
						attrs = constructAttrs(tree.getChild(idx++));
					
					// make scope for the arg names
					pushScope(new LocalScope(currentScope));
					try {
						IAstPrototype proto = checkConstruct(tree.getChild(idx++),
								IAstPrototype.class);
						if (proto.hasDefaultArguments()) {
							throw new GenerateException(tree.getChild(2),
									"cannot use default arguments in code type");
						}
						
						if (tree.getChildCount() > idx)
							throw new GenerateException(tree.getChild(idx),
									"did not expect code block here");
	
						// type = new
						// AstType(typeEngine.getCodeType(proto.returnType(), proto
						// .argumentTypes()));
						if (!attrs.isEmpty())
							proto.attrs().addAll(attrs);
						
						type = proto;
					} finally {
						popScope(tree.getChild(idx-1));
					}
				}
			} else if (tree.getType() == EulangParser.DATA) {
				IAstDataType dataType = checkConstruct(tree, IAstDataType.class);
				// ensure there's a good name (ignore an existing name lifted from the current parent)
				ISymbol anonName = currentScope.add("$anon", true);
				anonName.setDefinition(dataType);
				dataType.setTypeName(anonName);
				dataType.setType(null);
				type = dataType;
				getSource(tree, type);
			} else if (tree.getType() == EulangParser.IDREF || tree.getType() == EulangParser.FIELDREF) {
				IAstTypedExpr typedExpr = checkConstruct(tree, IAstTypedExpr.class);
				
				if (typedExpr instanceof IAstSymbolExpr) {
					IAstSymbolExpr symbolExpr = (IAstSymbolExpr) typedExpr;

					if (symbolExpr.getType() instanceof LLGenericType) {
						// shunt type variables into the scope below the define's
						symbolExpr = moveSymbolOutOfDefine(symbolExpr);
						type = new AstNamedType(symbolExpr.getType(), symbolExpr);
					} else if (symbolExpr.getType() != null)
						type = new AstType(symbolExpr.getType());
					else {
						// assert false;
						// type = new AstInstanceExpr(typeEngine,
						// symbolExpr, null);
						type = new AstNamedType(null, symbolExpr);
					}
				} else {
					unhandled(tree);
				}
			}
		}
		if (type == null)
			unhandled(tree);

		// assert type.getType() != null;
		getSource(tree, type);
		return type;
	}

	/**
	 * If a top level value references a generic name from a define, that name
	 * needs to be copied locally (or else, when instantiating the body, the
	 * types will be set on the define's version).
	 * 
	 * @param symbolExpr
	 * @return
	 */
	private IAstSymbolExpr moveSymbolOutOfDefine(IAstSymbolExpr symbolExpr) {
		ISymbol typeSym = symbolExpr.getSymbol();
		if (typeSym.getScope().getOwner() != null
				&& !(typeSym.getScope().getOwner() instanceof IAstDefineStmt))
			return symbolExpr;

		ISymbol typeVar = currentScope.get(typeSym.getName());
		if (typeVar == null) {
			assert currentScope != typeSym.getScope();
			typeVar = currentScope.copySymbol(typeSym, true);
			typeVar.setDefinition(symbolExpr);
		}
		symbolExpr.setSymbol(typeVar);
		return symbolExpr;
	}

	public IAstType constructTypeExpr(Tree tree) throws GenerateException {
		IAstType typeExpr = null;
		if (tree.getChildCount() == 1)
			typeExpr = constructType(tree.getChild(0));
		else {
			typeExpr = new AstType(null);
			getSource(tree, typeExpr);

		}
		return typeExpr;
	}

	public IAstDefineStmt constructDefine(Tree tree) throws GenerateException {
		IAstSymbolExpr symbolExpr = createSymbol(tree.getChild(0));

		int idx = 1;
		IAstNodeList<IAstSymbolExpr> typeVars = null;
		// initial guess
		boolean generic = tree.getChildCount() == 3;

		IAstNodeList<IAstTypedExpr> bodyList = new AstNodeList<IAstTypedExpr>(
				IAstTypedExpr.class);

		pushScope(new LocalScope(currentScope));
		
		// set owner right away so we can skip its scope
		AstDefineStmt stmt = new AstDefineStmt(symbolExpr, generic, currentScope, bodyList);
		currentScope.setOwner(stmt);
		
		ISymbol previousName = currentName;
		currentName = symbolExpr.getSymbol();
		try {
			if (generic) {
				// get type vars into the scope
				typeVars = new AstNodeList<IAstSymbolExpr>(IAstSymbolExpr.class);
				for (Tree kid : iter(tree.getChild(1))) {
					IAstSymbolExpr typeVar = createSymbol(kid);
					typeVars.add(typeVar);
					typeVar.setType(new LLGenericType(typeVar.getSymbol()));
					typeVar.getSymbol().setDefinition(typeVar);
				}
				idx++;
			}

			Tree exprTree = tree.getChild(idx);
			if (exprTree.getType() == EulangParser.LIST) {
				for (Tree kid : iter(exprTree)) {
					IAstTypedExpr expr = constructDefineValue(kid, generic,
							symbolExpr);
					bodyList.add(expr);
				}
			} else {
				IAstTypedExpr expr = constructDefineValue(exprTree, generic,
						symbolExpr);
				bodyList.add(expr);
			}
			getSource(exprTree, bodyList);
			getSource(tree, stmt);

			symbolExpr.getSymbol().setDefinition(stmt);
			return stmt;
		} finally {
			currentName = previousName;
			popScope(tree);
		}

	}

	/**
	 * Construct a value in a define. Each has its own scope to hold any generic
	 * variables, when they are first referenced.
	 * 
	 * @param exprTree
	 * @param generic
	 * @param symbolExpr
	 * @return
	 * @throws GenerateException
	 */
	private IAstTypedExpr constructDefineValue(Tree exprTree, boolean generic,
			IAstSymbolExpr symbolExpr) throws GenerateException {
		if (generic)
			pushScope(new LocalScope(currentScope));
		try {
			IAstTypedExpr expr = checkConstruct(exprTree, IAstTypedExpr.class);
			/*
			 * if (expr instanceof IAstDataType) { // TODO: other types ISymbol
			 * symbol = symbolExpr.getSymbol(); ((IAstDataType)
			 * expr).setTypeName(symbol); }
			 */
			return expr;
		} finally {
			if (generic)
				popScope(exprTree);
		}
	}

	/**
	 * @param tree
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IAstTypedExpr constructCodeExpr(Tree tree, final IAstPrototype forProto) throws GenerateException {
		assert tree.getChildCount() > 0;
		Set<String> attrs = Collections.emptySet();
		
		boolean isMacro = false;
		
		int idx = 0;
		if (tree.getChild(idx).getType() == EulangParser.ATTRS) {
			attrs = constructAttrs(tree.getChild(idx));
			idx++;
		}
		
		if (attrs.contains(IAttrs.MACRO))
			isMacro = true;
		
		/*
		if (attrs.contains(IAttrs.THIS) && !(currentScope.getOwner() instanceof IAstDataType)) {
			throw new GenerateException(tree, "cannot define methods outside data");
		}
		*/

		
		if (forProto == null) {
			pushScope(new LocalScope(currentScope));
		}
		try {
			IAstPrototype proto;
			if (tree.getChild(idx).getType() == EulangParser.PROTO) {
				proto = checkConstruct(tree.getChild(idx), IAstPrototype.class);

				boolean hitDefaults = false;
				for (IAstArgDef argDef : proto.argumentTypes()) {
					if (!isMacro && argDef.hasAttr(IAstAttributes.MACRO)) {
						throw new GenerateException(argDef.getSourceRef(),
								"cannot use macro arguments outside code #macro");
					}
					if (argDef.getDefaultValue() != null) {
						if (!isMacro)
							throw new GenerateException(argDef.getSourceRef(),
									"cannot use default arguments outside code #macro");
						hitDefaults = true;
					} else if (hitDefaults) {
						throw new GenerateException(argDef.getSourceRef(),
								"non-default argument follows default argument");
					}
				}
				idx++;
			} else if (forProto != null) {
				proto = forProto.copy();
				proto.uniquifyIds();
				for (IAstArgDef argDef : proto.argumentTypes()) {
					ISymbol argSym = currentScope.add(argDef.getName(), false);
					LLType type = argDef.getSymbolExpr() != null ? argDef.getSymbolExpr().getSymbol().getType() : argDef.getType();
					argSym.setType(type);
					argSym.setDefinition(argDef);
				}
				
			} else {
				IAstType unspecified = new AstType(null);
				getEmptySource(tree, unspecified);
				proto = new AstPrototype(typeEngine, unspecified,
						new IAstArgDef[0], Collections.<String>emptySet());
				getEmptySource(tree, proto);
			}
			
			// adjust for method
			if (attrs.contains(IAttrs.THIS) || proto.hasAttr(IAttrs.THIS)) {
				adjustPrototypeForThisCall(tree, proto);
				if (attrs.size() == 0)
					attrs = new HashSet<String>();
				attrs.add(IAttrs.THIS);
			}
			
			// now parse the code
			IAstNodeList<IAstStmt> list = null;
			if (idx < tree.getChildCount())
				list = checkConstruct(tree.getChild(idx++), IAstNodeList.class);
			
			IAstCodeExpr codeExpr = new AstCodeExpr(proto, currentScope, list, attrs);
			getSource(tree, codeExpr);
			return codeExpr;
		} finally {
			if (forProto == null) {
				popScope(tree);
			}
		}
	}

	private void adjustPrototypeForThisCall(Tree tree, 
			IAstPrototype proto) throws GenerateException {
		IScope data = currentScope;
		while (data != null && !(data.getOwner() instanceof IAstDataType)) {
			data = data.getParent();
		}
		if (data == null) {
			throw new GenerateException(tree, "cannot mark #this on code outside of data");
		}
		IScope localScope = currentScope;
		
		adjustPrototypeForThisCall(typeEngine, proto, data,
				localScope);
		
	}

	public static IAstArgDef adjustPrototypeForThisCall(TypeEngine typeEngine,
			IAstPrototype proto,
			IScope data, IScope localScope) {
		
		if (localScope == data) {
			localScope = new LocalScope(data);
		}
		if (proto.getArgCount() > 0 && proto.argumentTypes()[0].getName().equals("this"))
			return proto.argumentTypes()[0];
		
		ISymbol thisSym = localScope.add("this", false);
		IAstSymbolExpr thisName = new AstSymbolExpr(true, thisSym);
		IAstArgDef thisArgDef = new AstArgDef(thisName, new AstType(null), null, Collections.<String>emptySet());
		proto.addArgument(0, thisArgDef);
		thisSym.setDefinition(thisArgDef);
		proto.setType(null);
		
		LLSymbolType symType = new LLSymbolType(((IAstDataType) data.getOwner()).getTypeName());
		thisSym.setType(typeEngine.getPointerType(symType));

		thisArgDef.setSourceRefTree(proto.getSourceRef());
		
		return thisArgDef;
	}

	/**
	 * @param child
	 * @return
	 */
	private Set<String> constructAttrs(Tree child) {
		HashSet<String> attrs = new HashSet<String>();
		for (Tree kid : iter(child)) {
			if (kid.getType() == EulangParser.ATTR)
				attrs.add(kid.getText().substring(1));
			else
				attrs.add(kid.getText());
		}
		return attrs;
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
		case EulangParser.NIL:
			litExpr = new AstNilLitExpr(lit, null);
			break;
		case EulangParser.TRUE:
			litExpr = new AstBoolLitExpr(lit, typeEngine.BOOL, true);
			break;
		case EulangParser.FALSE:
			litExpr = new AstBoolLitExpr(lit, typeEngine.BOOL, false);
			break;
		case EulangParser.CHAR_LITERAL: {
			assert (lit.startsWith("'") && lit.endsWith("'"));
			boolean interpret = true;
			LLType type = null;
			String origLit = lit;
			lit = lit.substring(1, lit.length() - 1);
			int chval = 0;
			for (int idx = 0; idx < lit.length(); ) {
				Pair<Integer, Integer> next = parseCharacter(tree, lit, idx, interpret);
				idx = next.first;
				int cur = next.second;
				chval = (chval << 8) | (cur & 0xff);
				if (type == null)
					type = typeEngine.CHAR;
				else 
					type = typeEngine.INT;
			}
			litExpr = new AstIntLitExpr(origLit, type, chval);
			break;
		}
		case EulangParser.STRING_LITERAL: {
			assert (lit.startsWith("\"") && lit.endsWith("\""));
			boolean interpret = true;
			LLType type = typeEngine.STR;
			lit = lit.substring(1, lit.length() - 1);
			StringBuilder sb = new StringBuilder(); 
			for (int idx = 0; idx < lit.length(); ) {
				Pair<Integer, Integer> next = parseCharacter(tree, lit, idx, interpret);
				idx = next.first;
				sb.append((char) (int) next.second);
			}
			litExpr = new AstStringLitExpr(sb.toString(), type);
			break;
		}
		case EulangParser.NUMBER: {
			int radix = 10;
			if (lit.startsWith("0x") || lit.startsWith("0X")) {
				radix = 16;
				lit = lit.substring(2);
			} else if (lit.startsWith("0b")) {
				radix = 2;
				lit = lit.substring(2);
			} else if (lit.length() > 1 && lit.startsWith("0")) {
				radix = 8;
			}
			try {
				Long l = Long.parseLong(lit, radix);
				LLType type = typeEngine.INT;
				// if (l < 256 && l >= -128)
				// type = typeEngine.BYTE;
				litExpr = new AstIntLitExpr(lit, type, l);
			} catch (NumberFormatException e) {
				try {
					Double d = Double.parseDouble(lit);
					litExpr = new AstFloatLitExpr(lit, typeEngine.FLOAT, d);
				} catch (NumberFormatException e2) {
				}
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
	 * @param lit
	 * @param idx
	 * @return
	 * @throws GenerateException 
	 */
	private Pair<Integer, Integer> parseCharacter(Tree tree, String lit, int idx, boolean interpret) throws GenerateException {
		int cur = 0;
		char ch = lit.charAt(idx++);
		if (interpret) {
			if (ch == '\\') {
				ch = lit.charAt(idx++);
				switch (ch) {
				case '\\': cur = ch; break;
				case '\'': cur = ch; break;
				case 'x':
					if (idx + 2 > lit.length())
						throw new GenerateException(tree, "expected two characters for \\xNN");
					cur = fromHex(tree, lit.charAt(idx++)) << 4;
					cur |= fromHex(tree, lit.charAt(idx++));
					break;
				case 'r':
					cur = 13; break;
				case 'n':
					cur = 10; break;
				case 't':
					cur = 9; break;
				default:
					throw new GenerateException(tree, "unknown escape sequence \\" + ch);
				}
			} else {
				cur = ch;
			}
		} else {
			cur = ch;
		}
		return new Pair<Integer, Integer>(idx, cur);
	}

	/**
	 * @param charAt
	 * @return
	 */
	private int fromHex(Tree tree, char ch) throws GenerateException {
		int idx = "0123456789ABCDEF".indexOf(Character.toUpperCase(ch));
		if (idx < 0)
			throw new GenerateException(tree, "invalid hex constant at '" + ch+ "'");
		return idx;
	}

	/**
	 * @return
	 */
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}

}
