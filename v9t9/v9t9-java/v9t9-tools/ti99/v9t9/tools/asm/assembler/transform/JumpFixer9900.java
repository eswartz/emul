/*
  JumpFixer9900.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler.transform;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.directive.LabelDirective;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.JumpOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;

/**
 * @author Ed
 *
 */
public class JumpFixer9900 {

	private final Assembler assembler;
	private List<IInstruction> insts;

	public JumpFixer9900(Assembler assembler, List<IInstruction> insts) {
		this.assembler = assembler;
		this.insts = insts;
	}

	public List<IInstruction> run() throws ResolveException {
		while (fixOutOfRangeJumps()) {
			insts = assembler.resolve(insts);
		}
		return insts;
	}

	private boolean fixOutOfRangeJumps() throws ResolveException {
		boolean changed = false;
		for (ListIterator<IInstruction> iterator = insts.listIterator(); iterator.hasNext();) {
			IInstruction inst = iterator.next();
			if (!(inst instanceof LLInstruction)) {
				continue;
			}
			LLInstruction llInstruction = (LLInstruction) inst;
			if (assembler.getInstructionFactory().isJumpInst(llInstruction.getInst())) {
				LLOperand op1 = llInstruction.getOp1();
				if (op1 instanceof LLPCRelativeOperand) {
					LLPCRelativeOperand jump = (LLPCRelativeOperand) op1;
					int offset = jump.getOffset();
					if (offset < -256 || offset >= 256) {
						if (convertJump(llInstruction, 
								jump.getOriginal(), 
								llInstruction.getPc() + offset,  
								iterator)) {
							changed = true;
						}
						else
							throw new ResolveException(inst, op1, "Cannot convert out-of-range jump");
					}
				}
			}
		}
		return changed;
	}

	static Map<Integer, Integer> jumpInvertMap = new HashMap<Integer, Integer>();
	static void invert(int a, int b) {
		jumpInvertMap.put(a, b);
		jumpInvertMap.put(b, a);
	}
	static {
		invert(Inst9900.Ijeq, Inst9900.Ijne);
		invert(Inst9900.Ijh, Inst9900.Ijle);
		invert(Inst9900.Ijl, Inst9900.Ijhe);
		invert(Inst9900.Ijoc, Inst9900.Ijnc);
	}
	private boolean convertJump(LLInstruction jump, AssemblerOperand assemblerOperand, int targetAddr,
			ListIterator<IInstruction> iterator) {
		
		// easy
		if (jump.getInst() == Inst9900.Ijmp) {
			jump.setInst(Inst9900.Ib);
			jump.setOp1(new LLForwardOperand(new AddrOperand(assemblerOperand), 2));
			return true;
		}
			
		// other jumps require short inverted jump to next instruction
		// and a branch to original target

		Symbol nextInstSymbol = assembler.getSymbolTable().createSymbol("fixup$" + jump.getPc());
		SymbolOperand nextInstTarget = new SymbolOperand(nextInstSymbol);
		
		LabelDirective pastBranchInstLabel = new LabelDirective(nextInstSymbol);
		
		HLInstruction branchInst = new HLInstruction(assembler.getInstructionFactory());
		branchInst.setInst(Inst9900.Ib);
		branchInst.setOp1(new AddrOperand(assemblerOperand));
		branchInst.setOp2(null);
		
		jump.setOp1(new LLForwardOperand(new JumpOperand(nextInstTarget), 0));
		
		Integer invJump = jumpInvertMap.get(jump.getInst());
		if (invJump != null) {
			jump.setInst(invJump);
			iterator.add(branchInst);
			iterator.add(pastBranchInstLabel);
			return true;
		}

		HLInstruction equInst = new HLInstruction(assembler.getInstructionFactory());
		equInst.setInst(Inst9900.Ijeq);
		equInst.setOp1(new JumpOperand(nextInstTarget));
		equInst.setOp2(null);
		
		if (jump.getInst() == Inst9900.Ijlt || jump.getInst() == Inst9900.Ijgt) {
			// jgt, jeq
			jump.setInst(jump.getInst() == Inst9900.Ijlt ? Inst9900.Ijgt : Inst9900.Ijlt);
			iterator.add(equInst);
			iterator.add(branchInst);
			iterator.add(pastBranchInstLabel);
			return true;
		}
		
		return false;
	}
	
}
