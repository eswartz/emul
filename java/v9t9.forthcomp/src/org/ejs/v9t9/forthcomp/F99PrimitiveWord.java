/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class F99PrimitiveWord extends TargetWord {

	private final int opcode;

	/**
	 * @param entry
	 */
	public F99PrimitiveWord(DictEntry entry, int opcode) {
		super(entry);
		this.opcode = opcode;
	}

	/**
	 * @return the opcode
	 */
	public int getOpcode() {
		return opcode;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.TargetContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext)
			throws AbortException {
		throw hostContext.abort("cannot execute " + entry.getName() + " in compiler");
	}

}
