/**
 * 
 */
package org.ejs.eulang.llvm;

/**
 * @author ejs
 *
 */
public class LLAttrs {
	private String[] attrs;

	/**
	 * @param string
	 */
	public LLAttrs(String... attrs) {
		this.attrs = attrs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = false;
		for (String attr : attrs) {
			if (first) first = false; else sb.append(' ');
			sb.append(attr);
		}
		return sb.toString();
	}
	
}
