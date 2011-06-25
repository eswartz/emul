/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.ITarget;

/**
 * @author ejs
 *
 */
public class LLTargetTripleDirective extends LLTargetDirective {

	private final ITarget target;

	public LLTargetTripleDirective(ITarget target) {
		this.target = target;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "target triple = \"" + target.getTriple() + "\"\n";
	}
}
