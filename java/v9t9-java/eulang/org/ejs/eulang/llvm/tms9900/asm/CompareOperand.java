/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import java.util.HashMap;
import java.util.Map;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Inst9900;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class CompareOperand extends NumberOperand {
	public final static Map<String, Integer> compareToInt = new HashMap<String, Integer>();
	public final static Map<Integer, Integer> compareToJump  = new HashMap<Integer, Integer>();
	public final static int CMP_EQ = 0, CMP_NE = 1, CMP_SGT = 2, CMP_SLT = 3, CMP_SGE = 4, 
	CMP_SLE = 5, CMP_UGT = 6, CMP_ULT = 7, CMP_UGE = 8, CMP_ULE = 9;
	
	static {
		compareToInt.put("eq", CMP_EQ);
		compareToInt.put("ne", CMP_NE);
		compareToInt.put("sgt", CMP_SGT);
		compareToInt.put("slt", CMP_SLT);
		compareToInt.put("sge", CMP_SGE);
		compareToInt.put("sle", CMP_SLE);
		compareToInt.put("ugt", CMP_UGT);
		compareToInt.put("ult", CMP_ULT);
		compareToInt.put("uge", CMP_UGE);
		compareToInt.put("ule", CMP_ULE);
		
		compareToJump.put(CMP_EQ, Inst9900.Ijeq);
		compareToJump.put(CMP_NE, Inst9900.Ijne);
		compareToJump.put(CMP_SGT, Inst9900.Ijgt);
		compareToJump.put(CMP_SLT, Inst9900.Ijlt);
		compareToJump.put(CMP_SGE, 0);
		compareToJump.put(CMP_SLE, 0);
		compareToJump.put(CMP_UGT, Inst9900.Ijh);
		compareToJump.put(CMP_ULT, Inst9900.Ijl);
		compareToJump.put(CMP_UGE, Inst9900.Ijhe);
		compareToJump.put(CMP_ULE, Inst9900.Ijle);
	}

	private String cmp;
	
	public CompareOperand(String cmp) {
		super(compareToInt.get(cmp));
		this.cmp = cmp;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return cmp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#resolve(v9t9.tools.asm.assembler.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(inst, null, "Should not have this operand in assembler code!");
	}

	/**
	 * @return
	 */
	public int getJumpInstr() {
		return compareToJump.get(getValue());
	}

}
