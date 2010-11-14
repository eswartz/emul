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
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int[] opcodes = getOpcodes();
				for (int opcode : opcodes)
					targetContext.compileOpcode(opcode);				
			}
		});
	}

	/**
	 * @return the opcode
	 */
	public int[] getOpcodes() {
		return opcodes;
	}

}
