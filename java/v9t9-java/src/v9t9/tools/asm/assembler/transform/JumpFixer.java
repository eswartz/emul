/**
 * 
 */
package v9t9.tools.asm.assembler.transform;

import static v9t9.engine.cpu.InstructionTable.Ib;
import static v9t9.engine.cpu.InstructionTable.Ijeq;
import static v9t9.engine.cpu.InstructionTable.Ijgt;
import static v9t9.engine.cpu.InstructionTable.Ijh;
import static v9t9.engine.cpu.InstructionTable.Ijhe;
import static v9t9.engine.cpu.InstructionTable.Ijl;
import static v9t9.engine.cpu.InstructionTable.Ijle;
import static v9t9.engine.cpu.InstructionTable.Ijlt;
import static v9t9.engine.cpu.InstructionTable.Ijmp;
import static v9t9.engine.cpu.InstructionTable.Ijnc;
import static v9t9.engine.cpu.InstructionTable.Ijne;
import static v9t9.engine.cpu.InstructionTable.Ijoc;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.directive.LabelDirective;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.JumpOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLJumpOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class JumpFixer {

	private final Assembler assembler;
	private List<IInstruction> insts;

	public JumpFixer(Assembler assembler, List<IInstruction> insts) {
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
			if (llInstruction.isJumpInst()) {
				LLOperand op1 = llInstruction.getOp1();
				if (op1 instanceof LLJumpOperand) {
					LLJumpOperand jump = (LLJumpOperand) op1;
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
		invert(Ijeq, Ijne);
		invert(Ijh, Ijle);
		invert(Ijl, Ijhe);
		invert(Ijoc, Ijnc);
	}
	private boolean convertJump(LLInstruction jump, AssemblerOperand assemblerOperand, @SuppressWarnings("unused") int targetAddr,
			ListIterator<IInstruction> iterator) {
		
		// easy
		if (jump.getInst() == Ijmp) {
			jump.setInst(Ib);
			jump.setOp1(new LLForwardOperand(new AddrOperand(assemblerOperand), 2));
			return true;
		}
			
		// other jumps require short inverted jump to next instruction
		// and a branch to original target

		Symbol nextInstSymbol = assembler.getSymbolTable().createSymbol("fixup$" + jump.getPc());
		SymbolOperand nextInstTarget = new SymbolOperand(nextInstSymbol);
		
		LabelDirective pastBranchInstLabel = new LabelDirective(nextInstSymbol);
		
		HLInstruction branchInst = new HLInstruction();
		branchInst.setInst(Ib);
		//int offset = (short)(targetAddr - assemblerOperand.getAddr());
		//branchInst.op1 = new AddrOperand(new BinaryOperand('+', new SymbolOperand(target), new NumberOperand(offset)));
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

		HLInstruction equInst = new HLInstruction();
		equInst.setInst(Ijeq);
		equInst.setOp1(new JumpOperand(nextInstTarget));
		equInst.setOp2(null);
		
		if (jump.getInst() == Ijlt || jump.getInst() == Ijgt) {
			// jgt, jeq
			jump.setInst(jump.getInst() == Ijlt ? Ijgt : Ijlt);
			iterator.add(equInst);
			iterator.add(branchInst);
			iterator.add(pastBranchInstLabel);
			return true;
		}
		
		return false;
	}
	
}
