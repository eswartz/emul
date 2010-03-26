/**
 * 
 */
package org.ejs.eulang.ast;

import java.io.PrintStream;

import v9t9.tools.ast.expr.AstVisitor;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class DumpAST extends AstVisitor {
	private int indent = 0;
	private final PrintStream str;
	
	public DumpAST(PrintStream str) {
		this.str = str;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.AstVisitor#visit(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public int visit(IAstNode node) {
		printIndent();
		str.print(node.toString());
		str.println();
		if (node instanceof IAstPrototype)
			return PROCESS_SKIP;
		return PROCESS_CONTINUE;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.AstVisitor#visitChildren(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public void visitChildren(IAstNode node) {
		indent++;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.AstVisitor#visitEnd(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public int visitEnd(IAstNode node) {
		indent--;
		return PROCESS_CONTINUE;
	}
	
	/**
	 * @param str2
	 */
	private void printIndent() {
		for (int i = 0; i < indent; i++)
			str.print("  ");
	}
}
