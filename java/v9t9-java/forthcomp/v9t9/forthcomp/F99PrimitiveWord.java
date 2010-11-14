/**
 * 
 */
package v9t9.forthcomp;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;

import v9t9.forthcomp.words.TargetContext;
import v9t9.forthcomp.words.TargetWord;

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
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				int opcode = getOpcode();
				targetContext.compileOpcode(opcode);
			}
		});
	}

	/**
	 * @return the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

}
