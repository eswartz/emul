/**
 * 
 */
package org.ejs.eulang.ast;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;


/**
 * @author ejs
 *
 */
public class DumpAST extends AstVisitor {
	private int indent = 0;
	private final PrintStream str;
	private final boolean showSourceRef;
	
	public DumpAST(PrintStream str) {
		this.visitDumpChildren = true;
		showSourceRef = true;
		this.str = str;
	}
	public DumpAST(PrintStream str, boolean showSourceRef) {
		this.showSourceRef = showSourceRef;
		this.visitDumpChildren = true;
		this.str = str;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.AstVisitor#visit(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public int visit(IAstNode node) {
		printIndent(node);
		str.print(node.toString());
		str.println();
		if (node instanceof IAstScope)
			dumpScope(((IAstScope) node).getScope());
		if (node instanceof IAstPrototype)
			return PROCESS_SKIP;
		return PROCESS_CONTINUE;
	}
	
	/**
	 * @param scope 
	 * 
	 */
	private void dumpScope(IScope scope) {
		indent++;
		printIndent(null);
		ISymbol[] symbols = scope.getSymbols();
		if (symbols.length > 0) {
			str.println("=== Symbols:");
			for (ISymbol symbol : symbols) {
				printIndent(null); str.print("=== ");
				str.println(symbol);
			}
		} else {
			str.println("=== (no symbols)"); 
		}
		indent--;
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
	private void printIndent(IAstNode node) {
		if (showSourceRef) {
			String src = "";
			//src = (node != null ? node.getId() : "") + "";
			//while (src.length() < 5)
			//	src += " ";
			if (node != null && node.getSourceRef() != null) {
				src += node.getSourceRef().getLine() + ":" + node.getSourceRef().getColumn();
			}
			str.print(src);
			int left = 16 - src.length();
			while (left-- > 0)
				str.print(' ');
		}
		for (int i = 0; i < indent; i++)
			str.print("  ");
	}

	/**
	 * @param object
	 * @return
	 */
	public static String dumpString(IAstNode node) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream strStream = new PrintStream(out);
		DumpAST dump = new DumpAST(strStream, false);
		node.accept(dump);
		String dumpstr = out.toString();
		dumpstr = dumpstr.trim().replaceAll("\n", " // ");
		return dumpstr;
	}
}
