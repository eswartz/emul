/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ejs.eulang.llvm.tms9900.asm.LocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.AsmOperand;
import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.symbols.ISymbol;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.Instruction.Effects;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;


/**
 * @author Ed
 *
 */
public class AsmInstruction extends HLInstruction {

	private int number;
	
	private ISymbol[] targets;
	private ISymbol[] sources;
	private ISymbol[] implTargets;
	private ISymbol[] implSources;

	private Effects fx;

	public AsmInstruction() {
		targets = implTargets = null;
		sources = implSources = null;
	}

	/**
	 * @return the fx
	 */
	public Effects getEffects() {
		if (fx == null) {
			fx = Instruction.getInstructionEffects(getInst());
			if (fx == null) {
				fx = new Instruction.Effects();
				switch (getInst()) {
				case InstrSelection.Pcopy:
				case InstrSelection.Piset:
					fx.mop1_dest = Operand.OP_DEST_FALSE;
					fx.mop2_dest = Operand.OP_DEST_KILLED;
					break;
				case InstrSelection.Pepilog:
				case InstrSelection.Pprolog:
					break;
				case InstrSelection.Pjcc:
					fx.jump = Instruction.INST_JUMP_COND;
					fx.mop1_dest = Operand.OP_DEST_FALSE;
					fx.mop2_dest = Operand.OP_DEST_FALSE;
					fx.mop3_dest = Operand.OP_DEST_FALSE;
					fx.stReads = ~0;	// TODO
					break;
				case InstrSelection.Plea:
					fx.mop1_dest = Operand.OP_DEST_FALSE;
					fx.mop2_dest = Operand.OP_DEST_KILLED;
					break;
				}
			}
			if (getInst() == InstructionTable.Impy || getInst() == InstructionTable.Idiv) {
				fx.mop3_dest = Operand.OP_DEST_KILLED;
			}
		}
		return fx;
	}
	
	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		StringBuilder sb = new StringBuilder();
		String num = number + "";
		while (sb.length() < 5 - num.length())
			sb.append(' ');
		sb.append(num).append(":  ");
		sb.append(str);
		ISymbol[] srcs = getSources();
		ISymbol[] targs = getTargets();
		boolean anySrcs = (srcs != null && srcs.length > 0);
		boolean anyTargs = (targs != null && targs.length > 0);
		if (anySrcs || anyTargs) {
			while (sb.length() < 40)
				sb.append(' ');
			if (anySrcs) {
				sb.append(" ; reads = ");
				boolean first = true;
				for (ISymbol op : srcs) {
					if (first)
						first = false;
					else
						sb.append(",");
					sb.append(op);
				}
			}
			if (anyTargs) {
				sb.append(" ; writes = ");
				boolean first = true;
				for (ISymbol op : targs) {
					if (first)
						first = false;
					else
						sb.append(",");
					sb.append(op);
				}
			}
		}
		return sb.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#setInst(int)
	 */
	@Override
	public void setInst(int inst) {
		super.setInst(inst);
		fx = null;
	}
	
	/** Get the operands (either explicitly specified as operands or implicit
	 * in the calling convention, etc.) that the instruction modifies
	 * @return
	 */
	public ISymbol[] getTargets() {
		if (targets == null) {
			Set<ISymbol> targets = new LinkedHashSet<ISymbol>();
			if (implTargets != null) {
				targets.addAll(Arrays.asList(implTargets));
			} else {
				if (getEffects() != null) {
					if (getOp1() != null) {
						if (fx.mop1_dest != Operand.OP_DEST_FALSE) {
							getTargetSymbolRefs(targets, getOp1());
						}
						if (getOp2() != null) {
							if (fx.mop2_dest != Operand.OP_DEST_FALSE) {
								getTargetSymbolRefs(targets, getOp2());
							}
						}
					}
				}
				if (getOp3() != null) {
					getTargetSymbolRefs(targets, getOp3());
				}
			}
			this.targets = (ISymbol[]) targets.toArray(new ISymbol[targets.size()]);
		}
		return targets;
	}
	
	/** Get the symbols whose contents are modified if the operand 
	 * is written*/
	private static void getTargetSymbolRefs(Set<ISymbol> list, AssemblerOperand op) {
		if (op.isRegister()) {
			getOperandSymbol(list, op);
			return;
		}
		
		if (op instanceof AsmOperand && ((AsmOperand) op).isConst())
			return;
		
		// else, look for the address 
		if (op instanceof RegTempOffsOperand) {
			// the register itself is not the target 
		}
		else if (op instanceof AddrOperand) {
			// op itself is an indirection
			op = ((AddrOperand) op).getAddr();
			getOperandSymbol(list, op);
		}
		else {
			getOperandSymbol(list, op);
		}
	}

	
	/** Get the operands (either explicitly specified as operands or implicit
	 * in the calling convention, etc.) that the instruction reads
	 * @return
	 */
	public ISymbol[] getSources() {
		if (sources == null) {
			Set<ISymbol> sources = new LinkedHashSet<ISymbol>();
			if (implSources != null) {
				sources.addAll(Arrays.asList(implSources));
			} else {
				if (getEffects() != null) {
					if (getOp1() != null) {
						// be sure to get refs to the indirect registers
						getSourceSymbolRefs(sources, getOp1(), fx.mop1_dest != Operand.OP_DEST_KILLED);
						if (getOp2() != null) {
							getSourceSymbolRefs(sources, getOp2(), fx.mop2_dest != Operand.OP_DEST_KILLED);
							if (getOp3() != null) {
								getSourceSymbolRefs(sources, getOp3(), fx.mop3_dest != Operand.OP_DEST_KILLED);
							}
						}
					}
				}
			}
			this.sources = (ISymbol[]) sources.toArray(new ISymbol[sources.size()]);
		}
		return sources;
	}
	
	private static void getSourceSymbolRefs(Set<ISymbol> list, AssemblerOperand op, boolean includeTop) {
		if (op instanceof LocalOffsOperand) {
			// fine
		} else if (op instanceof AddrOperand) {
			// the @ is just an indirection to actual content
			if (((AddrOperand) op).getAddr().isMemory())
				op = ((AddrOperand) op).getAddr();
		}
		
		if (includeTop)
			getOperandSymbol(list, op);
		
		for (AssemblerOperand kid : op.getChildren()) {
			getSymbolRefs(list, kid);
		}
	}
	
	public static void getSymbolRefs(Set<ISymbol> list, AssemblerOperand op) {
		getOperandSymbol(list, op);
		for (AssemblerOperand kid : op.getChildren()) {
			getSymbolRefs(list, kid);
		}
	}

	private static void getOperandSymbol(Set<ISymbol> list, AssemblerOperand op) {
		if (op instanceof ISymbolOperand) {
			ISymbol symbol = ((ISymbolOperand) op).getSymbol();
			if (symbol != null)
				list.add(symbol);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#setOp1(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void setOp1(AssemblerOperand op1) {
		super.setOp1(op1);
		targets = null;
		sources = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#setOp2(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void setOp2(AssemblerOperand op2) {
		super.setOp2(op2);
		targets = null;
		sources = null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#setOp3(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void setOp3(AssemblerOperand op3) {
		super.setOp3(op3);
		targets = null;
		sources = null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#setOp(int, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void setOp(int i, AssemblerOperand op) {
		super.setOp(i, op);
		targets = null;
		sources = null;
	}
	
	/**
	 * @param implTargets the implicit targets to set
	 */
	public void setImplicitTargets(ISymbol[] implTargets) {
		this.implTargets = implTargets;
		this.targets = null;
	}
	
	public void setImplicitSources(ISymbol[] implSources) {
		this.implSources = implSources;
		this.sources = null;
	}
	
	public static AsmInstruction create(int inst) {
		AsmInstruction instr = new AsmInstruction();
		instr.setInst(inst);
		return instr;
	}
	public static AsmInstruction create(int inst, AssemblerOperand op1) {
		AsmInstruction instr = new AsmInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		return instr;
	}
	public static AsmInstruction create(int inst, AssemblerOperand op1, AssemblerOperand op2) {
		AsmInstruction instr = new AsmInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		return instr;
	}

	public static AsmInstruction create(int inst, AssemblerOperand op1,
			AssemblerOperand op2, AssemblerOperand op3) {
		AsmInstruction instr = new AsmInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		instr.setOp3(op3);
		return instr;
	}


	/**
	 * @param visitor
	 */
	public void accept(Block block, ICodeVisitor visitor) {
		if (visitor.enterInstr(block, this)) {
			AssemblerOperand[] ops = getOps();
			for (int i = 0; i < ops.length; i++) {
				if (visitor.enterOperand(this, i, ops[i])) {
					visitor.exitOperand(this, i, ops[i]);
				}
			}
			for (ISymbol source : getSources()) {
				visitor.handleSource(this, source);
			}
			for (ISymbol target : getTargets()) {
				visitor.handleTarget(this, target);
			}
			visitor.exitInstr(block, this);
		}
	}


	public AssemblerOperand getDestOp() {
		if (getEffects() != null) {
			if (getOp1() != null && fx.mop1_dest != Operand.OP_DEST_FALSE)
				return getOp1();
			if (getOp2() != null && fx.mop2_dest != Operand.OP_DEST_FALSE)
				return getOp2();
			if (getOp3() != null && fx.mop2_dest != Operand.OP_DEST_FALSE)
				return getOp3();
		}
		return null;
	}
	public AssemblerOperand getSrcOp() {
		if (getEffects() != null) {
			if (getOp1() != null && fx.mop1_dest != Operand.OP_DEST_KILLED)
				return getOp1();
			if (getOp2() != null && fx.mop2_dest != Operand.OP_DEST_KILLED)
				return getOp2();
			if (getOp3() != null && fx.mop2_dest != Operand.OP_DEST_KILLED)
				return getOp3();
		}
		return null;
	}


	/**
	 * @param dst
	 */
	public void setDestOp(AssemblerOperand dst) {
		if (getEffects() != null) {
			if (getOp1() != null && fx.mop1_dest != Operand.OP_DEST_FALSE) {
				setOp1(dst);
				return;
			} else if (getOp2() != null && fx.mop2_dest != Operand.OP_DEST_FALSE) {
				setOp2(dst);
				return;
			} else if (getOp3() != null && fx.mop3_dest != Operand.OP_DEST_FALSE) {
				setOp3(dst);
				return;
			}
		}
		assert false;
	}


}
