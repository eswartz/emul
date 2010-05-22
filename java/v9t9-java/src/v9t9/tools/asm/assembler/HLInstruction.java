/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.symbols.ISymbol;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;


/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	private ISymbol[] targets;
	private ISymbol[] sources;
	private ISymbol[] implTargets;
	private ISymbol[] implSources;

	public HLInstruction() {
		targets = implTargets = null;
		sources = implSources = null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.AssemblerInstruction#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		StringBuilder sb = new StringBuilder(str);
		ISymbol[] targs = getTargets();
		if (targs != null && targs.length > 0) {
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
		ISymbol[] srcs = getSources();
		if (srcs != null && srcs.length > 0) {
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
		return sb.toString();
	}
	
	
	/** Get the operands (either explicitly specified as operands or implicit
	 * in the calling convention, etc.) that the instruction modifies
	 * @return
	 */
	public ISymbol[] getTargets() {
		if (targets == null) {
			Instruction.Effects fx = Instruction.getInstructionEffects(getInst());
			//assert fx != null;	// this instr should explicitly define targets
			
			List<ISymbol> targets = new ArrayList<ISymbol>();
			if (implTargets != null) {
				targets.addAll(Arrays.asList(implTargets));
			} else {
				if (fx != null) {
					if (getOp1() != null) {
						if (fx.mop1_dest != Operand.OP_DEST_FALSE) {
							addSymbol(targets, getOp1());
						}
						if (getOp2() != null) {
							if (fx.mop2_dest != Operand.OP_DEST_FALSE) {
								addSymbol(targets, getOp2());
							}
						}
					}
				}
				if (getOp3() != null) {
					addSymbol(targets, getOp3());
				}
			}
			this.targets = (ISymbol[]) targets.toArray(new ISymbol[targets
					.size()]);
		}
		return targets;
	}
	
	/** Get the operands (either explicitly specified as operands or implicit
	 * in the calling convention, etc.) that the instruction reads
	 * @return
	 */
	public ISymbol[] getSources() {
		if (sources == null) {
			Instruction.Effects fx = Instruction.getInstructionEffects(getInst());
			//assert fx != null;	// this instr should explicitly define targets
			
			List<ISymbol> sources = new ArrayList<ISymbol>();
			if (implSources != null) {
				sources.addAll(Arrays.asList(implSources));
			} else {
				if (fx != null) {
					if (getOp1() != null) {
						if (fx.mop1_dest != Operand.OP_DEST_KILLED) {
							addSymbol(sources, getOp1());
						}
						if (getOp2() != null) {
							if (fx.mop2_dest != Operand.OP_DEST_KILLED) {
								addSymbol(sources, getOp2());
							}
						}
					}
				}
				if (getOp3() != null) {
					addSymbol(sources, getOp3());
				}
			}
			this.sources = (ISymbol[]) sources.toArray(new ISymbol[sources
			                                                                         .size()]);
		}
		return sources;
	}
	
	/**
	 * @param sources2
	 * @param op3
	 */
	private void addSymbol(List<ISymbol> list, AssemblerOperand op) {
		if (op instanceof ISymbolOperand) {
			ISymbol symbol = ((ISymbolOperand) op).getSymbol();
			if (symbol != null)
				list.add(symbol);
		}
	}

	@Override
	public byte[] getBytes() throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
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

	/**
	 * @param implTargets the implicit targets to set
	 */
	public void setImplicitTargets(ISymbol[] implTargets) {
		this.implTargets = implTargets;
	}
	
	public void setImplicitSources(ISymbol[] implSources) {
		this.implSources = implSources;
	}
	
	public static HLInstruction create(int inst) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		return instr;
	}
	public static HLInstruction create(int inst, AssemblerOperand op1) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		return instr;
	}
	public static HLInstruction create(int inst, AssemblerOperand op1, AssemblerOperand op2) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		return instr;
	}

	public static HLInstruction create(int inst, AssemblerOperand op1,
			AssemblerOperand op2, AssemblerOperand op3) {
		HLInstruction instr = new HLInstruction();
		instr.setInst(inst);
		instr.setOp1(op1);
		instr.setOp2(op2);
		instr.setOp3(op3);
		return instr;
	}

	/**
	 * @return
	 */

}
