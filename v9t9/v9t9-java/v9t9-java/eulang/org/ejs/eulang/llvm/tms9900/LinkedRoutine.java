/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.llvm.directives.LLDefineDirective;

import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.InstructionFactory9900;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;

public class LinkedRoutine extends Routine {

    public int returnReg;

    public LinkedRoutine(LLDefineDirective def) {
        super(def);
        returnReg = 11;
    }
    
    @Override
    public boolean isReturn(AsmInstruction inst) {
        if (inst.getInst() != Inst9900.Ib)
        	return false;
        Operand op1 = inst.getOp1();
        if (op1 instanceof RegIndOperand) {
        	return ((RegIndOperand) op1).isReg(returnReg);
        }
        if (op1 instanceof LLRegIndOperand) {
        	return ((LLRegIndOperand) op1).getRegister() == returnReg;
        }
        return false;
    }
    
    public AsmInstruction[] generateReturn() {
    	return new AsmInstruction[] { 
    			AsmInstruction.create(InstructionFactory9900.INSTANCE, 
    					Inst9900.Ib, new RegIndOperand(new NumberOperand(returnReg)))
    	};
    }
}
