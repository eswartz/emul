/**
 * 
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetWord;

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
				getEntry().use();
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
