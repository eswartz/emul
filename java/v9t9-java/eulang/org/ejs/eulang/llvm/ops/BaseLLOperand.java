/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.llvm.ILLCodeVisitor;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public abstract class BaseLLOperand implements LLOperand {

	protected LLType type;

	public BaseLLOperand(LLType type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#accept(org.ejs.eulang.llvm.ILLCodeVisitor)
	 */
	@Override
	public void accept(LLInstr instr, int num, ILLCodeVisitor visitor) {
		if (visitor.enterOperand(instr, num, this)) {
			
			visitor.exitOperand(instr, num, this);
		}
	}
}
