/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.types.LLType;

/**
 * An instruction with a (primary) type.  This type defines the result of the instruction, if any.
 * @author ejs
 *
 */
public abstract class LLTypedInstr extends LLBaseInstr {
	
	private final LLType type;

	public LLTypedInstr(String name, LLType type, LLOperand... ops) {
		super(name, ops);
		this.type = type;
		
	}
	public LLType getType() {
		return type;
	}

}
