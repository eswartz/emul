/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.naming.OperationNotSupportedException;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.EulangParser;
import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstInitializer;
import v9t9.tools.ast.expr.IAstInitializerExpression;
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
	
	private void error(Tree tree, String msg) {
		errors.add(new Error(getSourceRef(tree), msg));
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
					module.add(define.getName(), define);
					
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
			return constructDefineOrAssign(tree);
		case EulangParser.PROTO:
			return constructPrototype(tree);
		case EulangParser.CODE:
			return constructCodeExpr(tree);
		case EulangParser.ARGDEF:
			return constructArgDef(tree);
		default:
			unhandled(tree);
			return null;
		}
		
	}

	/**
	 * @param tree
	 * @return
	 */
	public IAstNode constructArgDef(Tree tree) {
		IAstName name = new AstName(tree.getChild(0).getText(), currentScope); 
		LLType type = null;
		IAstTypedExpression defaultVal = null;
		
		if (tree.getChildCount() > 0) {
			int argIdx = 1;
			if (tree.getChild(0).getType() == EulangParser.TYPE) {
				type = constructType(tree.getChild(1));
				argIdx++;
			} 
			if (argIdx < tree.getChildCount()) {
				defaultVal = check(tree.getChild(argIdx), IAstTypedExpression.class);
			}
		}
		
		IAstVariableDefinition argDef = new AstVariableDefinition(name, type, defaultVal);
		return argDef;
	}

	/**
	 * @param construct
	 * @param class1
	 * @return
	 */
	private <T extends IAstNode> T check(Tree tree, Class<T> klass) {
		IAstNode node = construct(tree);
		if (node == null)
			return null;
		if (klass.isInstance(node))
			return (T) node;
		error(tree, "unexpected node " + tree.getClass().getSimpleName() 
				+ " created, expected " + klass.getSimpleName());
		return null;
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
		
		IAstVariableDefinition[] argTypes = new IAstVariableDefinition[tree.getChildCount() - start];
		int idx = 0;
		while (start < tree.getChildCount()) {
			argTypes[idx++] = check(tree.getChild(start++), IAstVariableDefinition.class);
		}
		
		IAstPrototype proto = new AstPrototype(retType, argTypes);
		getSource(tree, proto);
		return proto;
	}

	/**
	 * @param child
	 * @return
	 */
	public LLType constructType(Tree child) {
		unhandled(child);
		return null;
	}

	public IAstTopLevelNode constructDefineOrAssign(Tree tree) {
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
		IScope oldScope = currentScope;
		try {
			currentScope = new Scope(currentScope);
			IAstPrototype proto = check(tree.getChild(0), IAstPrototype.class);
			IAstNodeList list = check(tree.getChild(1), IAstNodeList.class);
			IAstCodeExpression codeExpr = new AstCodeExpression(proto, currentScope, list);
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
	private IAstLiteralExpression constructLiteral(Tree tree) {
		assert tree.getType() == EulangParser.LIT;
		assert tree.getChildCount() == 1;
		
		String lit = tree.getChild(0).getText();
		
		try {
			Long l = Long.parseLong(lit);
			IAstLiteralExpression litNode = new AstIntegerLiteralExpression(lit, typeEngine.INT, l);
			getSource(tree, litNode);
			return litNode;
		} catch (NumberFormatException e) {
			unhandled(tree);
			return null;
		}
	}

	/**
	 * @return
	 */
	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
	

}
