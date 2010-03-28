/**
 * 
 */
package org.ejs.eulang.ast;

public class Error extends Message {
	
	public Error(ISourceRef ref, String msg) {
		super(ref, msg != null ? msg : "");
	}
	public Error(IAstNode node, String msg) {
		super(getRef(node), msg != null ? msg : "");
	}
	/**
	 * @param node
	 * @return
	 */
	private static ISourceRef getRef(IAstNode node) {
		ISourceRef last = null;
		do {
			if (node.getSourceRef() != null) {
				last = node.getSourceRef();
				if (last.getLine() > 0)
					return last;
			} 
			node = node.getParent();
		} while (node != null);
		return last;
	}
}