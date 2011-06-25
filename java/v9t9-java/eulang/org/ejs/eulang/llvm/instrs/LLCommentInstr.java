/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

/**
 * @author ejs
 *
 */
public class LLCommentInstr extends LLBaseInstr {

	private final String comment;
	
	public LLCommentInstr(String comment) {
		super(";");
		this.comment = comment;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLCommentInstr other = (LLCommentInstr) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		return "; " + comment.replaceAll("\r?\n", " ");
	}

}
