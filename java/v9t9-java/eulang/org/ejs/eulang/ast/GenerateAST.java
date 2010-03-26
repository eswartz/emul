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
import org.ejs.eulang.EulangParser;
import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;
import v9t9.tools.ast.expr.ISourceRef;
import v9t9.tools.ast.expr.impl.AstName;
import v9t9.tools.ast.expr.impl.SourceRef;

/**
 * @author ejs
 *
 */
public class GenerateAST {
	public static class Error {
		
		public Error(ISourceRef ref, String msg) {
			super();
			this.ref = ref;
			this.msg = msg;
		}
		ISourceRef ref;
		String msg;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ref.toString() + ": " + msg;
		}
	}
	
	private final Map<CharStream, String> fileMap;
	private final String defaultFile;
	private IScope currentScope;
	private List<Error> errors = new ArrayList<Error>();
	private TypeEngine typeEngine;
	
	public GenerateAST(String defaultFile, Map<CharStream, String> fileMap) {
		this.defaultFile = defaultFile;
		this.fileMap = fileMap;
		this.currentScope = new Scope(null);
		this.typeEngine = new TypeEngine();
	}
	
	public List<Error> getErrors() {
		return errors;
	}
	protected ISourceRef getSourceRef(Tree tree) {
		if (tree instanceof CommonTree) {
			Token token = ((CommonTree) tree).getToken();
			if (token != null) {
				return new TokenSourceRef(fileMap.get(token.getChannel()), token);
			}
		}
		return new SourceRef(defaultFile, tree.getTokenStartIndex(), tree.getLine(), tree.getCharPositionInLine()+1);
		
	}
	/** Copy source info into node */
	protected void getSource(Tree tree, IAstNode node) {
		node.setSourceRef(getSourceRef(tree));
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
	private <T extends IAstNode> T constructCheck(Tree tree, Class<T> klass) {
		IAstNode node = construct(tree);
		if (node == null)
			return null;
		if (klass.isInstance(node))
			return (T) node;
		error(tree, "unexpected node " + tree.getClass().getSimpleName() 
				+ " created, expected " + klass.getSimpleName());
		return null;
	}

	private void error(Tree tree, String msg) {
		Error e = new Error(getSourceRef(tree), msg);
		System.err.println(e);
		errors.add(e);
	}

	/**
	 * @param tree
	 */
	private void unhandled(Tree tree) {
		Exception e = new Exception("Unhandled tree: " + tree.toStringTree());
		System.err.println(e.getMessage());
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			if (stackTrace[i].getFileName().contains("GenerateAST")) {
				System.err.println(stackTrace[i].toString());
			} else {
				break;
			}
		}
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstModule constructModule(Tree tree) {
		
		IScope oldScope = currentScope;
		try {
			currentScope = new Scope(currentScope);
			IAstModule module = new AstModule(currentScope);
			
			IAstNodeList stmtList = constructStmtList(tree);
			
			for (IAstNode node : stmtList.list()) {
				node.setParent(null);
				if (node instanceof IAstDefine) {
					IAstDefine define = (IAstDefine) node;
					define.setParent(module);
					module.getScope().add(define.getName(), define);
					
				} else {
					module.initCode().add(node);
				}
			}
			
			return module;
		} finally {
			currentScope = oldScope;
		}
	}

	
	public IAstNode construct(Tree tree) {
		switch (tree.getType()) {
		case EulangParser.STMTLIST:
			return constructStmtList(tree);
		case EulangParser.LIT:
			return constructLiteral(tree);
		case EulangParser.DEFINE:
		case EulangParser.DEFINE_ASSIGN:
			if (currentScope.getOwner() instanceof IAstModule)
				return constructTopLevelDefineOrAssign(tree);
			else
				return constructVarAssign(tree);
		case EulangParser.PROTO:
			return constructPrototype(tree);
		case EulangParser.TYPE:
			return constructTypeExpr(tree);
			
		case EulangParser.CODE:
		case EulangParser.MACRO:
			return constructCodeExpr(tree);
		case EulangParser.ARGDEF:
			return constructArgDef(tree);
		case EulangParser.RETURN:
			return constructReturn(tree);
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
		case EulangParser.IDREF:
			return constructIdRef(tree);
		case EulangParser.ASSIGN:
			return constructAssign(tree);
		default:
			unhandled(tree);
			return null;
		}
		
	}
	public IAstAssignStmt constructVarAssign(Tree tree) {
		IAstType type = null;
		String name = tree.getChild(0).getText();
		
		IAstName nameNode = currentScope.search(name);
		if (nameNode != null && tree.getType() == EulangParser.DEFINE) {
			error(tree, "redefining " + name);
		}
		nameNode = new AstName(name, currentScope);
		getSource(tree.getChild(0), nameNode);
		
		IAstTypedExpr expr = null;
		if (tree.getType() == EulangParser.DEFINE) {
			assert tree.getChildCount() == 3;
			type = constructCheck(tree.getChild(1), IAstType.class);
			expr = constructCheck(tree.getChild(2), IAstTypedExpr.class);
		} else {
			expr = constructCheck(tree.getChild(2), IAstTypedExpr.class);
		}
		IAstIdExpr idExpr = new AstIdExpr(nameNode);
		IAstAssignStmt define = new AstAssignStmt(idExpr, type, expr);
		getSource(tree.getChild(1), define);
		
		return define;
	}
	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructAssign(Tree tree) {
		assert(tree.getChildCount() == 2);
		IAstIdExpr left = constructCheck(tree.getChild(0), IAstIdExpr.class);
		IAstTypedExpr right = constructCheck(tree.getChild(1), IAstTypedExpr.class);
		IAstAssignStmt assign = new AstAssignStmt(left, null, right);
		
		return assign;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstIdExpr constructIdRef(Tree tree) {
		// could have ':'s
		IScope startScope = currentScope;
		int idx = 0;
		IAstName name = null;
		boolean inScope = false;
		while (idx < tree.getChildCount()) {
			Tree kid = tree.getChild(idx);
			if (kid.getType() == EulangParser.COLON) {
				if (startScope.getParent() == null) {
					error(tree, "Cannot go out of module scope");
				} else {
					startScope = startScope.getParent();
				}
				inScope = true;
			} else if (kid.getType() == EulangParser.ID) {
				if (inScope) {
					name = startScope.find(kid.getText());
				} else {
					name = startScope.search(kid.getText());
				}
				if (name == null) {
					error(tree, "Cannot find name '" + kid.getText() + "'");
					break;
				}
				startScope = name.getScope();
			} else {
				unhandled(kid);
				return null;
			}
			idx++;
		}
		if (name == null) {
			error(tree, "Cannot resolve name");
			name = new AstName(tree.getChild(tree.getChildCount() - 1).getText(), currentScope);
		}
		IAstIdExpr idExpr = new AstIdExpr(name);
		return idExpr;
	}

	public IAstBinExpr constructBinaryExpr(Tree tree) {
		assert(tree.getChildCount() == 2);
		IAstTypedExpr left = constructCheck(tree.getChild(0), IAstTypedExpr.class);
		IAstTypedExpr right = constructCheck(tree.getChild(1), IAstTypedExpr.class);
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
		}
		return binop;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstReturnStmt constructReturn(Tree tree) {
		IAstExpression expr = null;
		if (tree.getChildCount() == 1) {
			expr = constructCheck(tree.getChild(0), IAstExpression.class);
		}
		IAstReturnStmt stmt = new AstReturnStmt(expr);
		return stmt;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructArgDef(Tree tree) {
		IAstName name = new AstName(tree.getChild(0).getText(), currentScope); 
		
		LLType type = null;
		IAstTypedExpr defaultVal = null;
		
		if (tree.getChildCount() > 0) {
			int argIdx = 1;
			if (tree.getChild(0).getType() == EulangParser.TYPE) {
				type = constructType(tree.getChild(1));
				argIdx++;
			} 
			if (argIdx < tree.getChildCount()) {
				defaultVal = constructCheck(tree.getChild(argIdx), IAstTypedExpr.class);
			}
		}
		
		IAstVariableDefinition argDef = new AstVariableDefinition(name, type, defaultVal);
		currentScope.add(name, argDef);
		return argDef;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNodeList constructStmtList(Tree tree) {
		IAstNodeList list = new AstNodeList();
		
		assert tree.getType() == EulangParser.STMTLIST;
		
		for (Tree kid : iter(tree)) {
			IAstNode node = construct(kid);
			if (node != null)
				list.list().add(node);
		}
		return list;
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructPrototype(Tree tree) {
		LLType retType = null;
		int start = 1;
		if (tree.getChildCount() == 0 || tree.getChild(0).getType() != EulangParser.RETURN) {
			retType = typeEngine.UNSPECIFIED;
			start = 0;
		} else {
			retType = constructType(tree.getChild(0).getChild(0));
		}
		IAstType retTypeNode = new AstType(retType);
		
		IAstVariableDefinition[] argTypes = new IAstVariableDefinition[tree.getChildCount() - start];
		int idx = 0;
		while (start < tree.getChildCount()) {
			argTypes[idx++] = constructCheck(tree.getChild(start++), IAstVariableDefinition.class);
		}
		
		IAstPrototype proto = new AstPrototype(retTypeNode, argTypes);
		getSource(tree, proto);
		return proto;
	}

	/**
	 * @param child
	 * @return
	 */
	public LLType constructType(Tree tree) {
		unhandled(tree);
		return null;
	}

	public IAstType constructTypeExpr(Tree tree) {
		LLType type = null;
		if (tree.getChildCount() == 1)
			type = constructType(tree.getChild(0));
		IAstType typeExpr = new AstType(type);
		getSource(tree, typeExpr);
		return typeExpr;
	}
	public IAstTopLevelNode constructTopLevelDefineOrAssign(Tree tree) {
		assert tree.getChildCount() == 2;
		
		String name = tree.getChild(0).getText();
		
		IAstName nameNode = currentScope.search(name);
		if (nameNode != null && tree.getType() == EulangParser.DEFINE) {
			error(tree, "redefining " + name);
		}
		nameNode = new AstName(name, currentScope);
		getSource(tree.getChild(0), nameNode);
		AstTopLevelDefine define = new AstTopLevelDefine(nameNode, construct(tree.getChild(1)));
		getSource(tree.getChild(1), define);
		
		return define;
	}
	
	/**
	 * @param tree
	 * @return
	 */
	public IAstExpression constructCodeExpr(Tree tree) {
		assert tree.getChildCount() == 2;
		boolean isMacro = tree.getType() == EulangParser.MACRO;
		IScope oldScope = currentScope;
		try {
			currentScope = new Scope(currentScope);
			IAstPrototype proto = constructCheck(tree.getChild(0), IAstPrototype.class);
			IAstNodeList list = constructCheck(tree.getChild(1), IAstNodeList.class);
			IAstCodeExpression codeExpr = new AstCodeExpression(proto, currentScope, list, isMacro);
			getSource(tree, codeExpr);
			return codeExpr;
		} finally {
			currentScope = oldScope;
		}
	}

	/**
	 * @param tree
	 * @return
	 */
	private IAstLitExpr constructLiteral(Tree tree) {
		assert tree.getType() == EulangParser.LIT;
		assert tree.getChildCount() == 1;
		
		String lit = tree.getChild(0).getText();
		
		try {
			Long l = Long.parseLong(lit);
			IAstLitExpr litNode = new AstIntLitExpr(lit, typeEngine.INT, l);
			getSource(tree, litNode);
			return litNode;
		} catch (NumberFormatException e) {
			try {
				Double d = Double.parseDouble(lit);
				IAstLitExpr litNode = new AstFloatLitExpr(lit, typeEngine.FLOAT, d);
				getSource(tree, litNode);
				return litNode;
			} catch (NumberFormatException e2) {
				unhandled(tree);
				return null;
			}
		}
	}

	/**
	 * @return
	 */
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
	

}
