/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import org.ejs.v9t9.forthcomp.words.TargetContext;
import org.ejs.v9t9.forthcomp.words.TargetWord;

/**
 * @author ejs
 *
 */
public class F99InlineWord extends TargetWord {

	private final int[] opcodes;

	/**
	 * @param entry
	 */
	public F99InlineWord(DictEntry entry, int[] opcodes) {
		super(entry);
		this.opcodes = opcodes;
	}

	/**
	 * @return the opcode
	 */
	public int[] getOpcodes() {
		return opcodes;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		throw hostContext.abort("cannot execute " + entry.getName() + " in compiler");
	}

}
