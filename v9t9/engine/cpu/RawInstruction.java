package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstructionTable.Iab;
import static v9t9.engine.cpu.InstructionTable.Icb;
import static v9t9.engine.cpu.InstructionTable.Imovb;
import static v9t9.engine.cpu.InstructionTable.Isb;
import static v9t9.engine.cpu.InstructionTable.Isocb;
import static v9t9.engine.cpu.InstructionTable.Iszcb;
import v9t9.tools.asm.BaseInstruction;
import v9t9.utils.Utils;
public class RawInstruction extends BaseInstruction implements Comparable<RawInstruction> {

	public String name;
	public int pc;
	/** size in bytes */
	public int size;
	public short opcode;
	/** InstTable.I... */
	public int inst;
	public Operand op1;
	public Operand op2;

	public RawInstruction() {
	}
	
	public RawInstruction(RawInstruction other) {
		this.name = other.name;
		this.pc = other.pc;
		this.size = other.size;
		this.opcode = other.opcode;
		this.inst = other.inst;
		this.op1 = other.op1;
		this.op2 = other.op2;
	}

	public String toString() {
	    StringBuilder buffer = new StringBuilder();
	    if (name != null)
	    	buffer.append(name);
	    else
	    	buffer.append(InstructionTable.getInstName(inst));
	    String opstring;
	    opstring = op1.toString();
	    if (opstring != null) {
	        buffer.append(' ');
	        buffer.append(opstring);
	        opstring = op2.toString();
	        if (opstring != null) {
	            buffer.append(',');
	            buffer.append(opstring);
	        }
	    }
	    return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + inst;
		result = prime * result + ((op1 == null) ? 0 : op1.hashCode());
		result = prime * result + ((op2 == null) ? 0 : op2.hashCode());
		result = prime * result + pc;
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RawInstruction other = (RawInstruction) obj;
		if (inst != other.inst) {
			return false;
		}
		if (op1 == null) {
			if (other.op1 != null) {
				return false;
			}
		} else if (!op1.equals(other.op1)) {
			return false;
		}
		if (op2 == null) {
			if (other.op2 != null) {
				return false;
			}
		} else if (!op2.equals(other.op2)) {
			return false;
		}
		if (pc != other.pc) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}

	public boolean isJumpInst() {
		return inst >= InstructionTable.Ijmp && inst <= InstructionTable.Ijop;
	}

	
	//public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass)
		//throws ResolveException {
		/*
		int pc = assembler.getPc();
		
		// instructions and associated labels are bumped when following uneven data
		if ((pc & 1) != 0 && inst != InstructionTable.Ibyte) {
			pc = (pc + 1) & 0xfffe;
			assembler.setPc(pc);
		
			if (previous instanceof LabelDirective) {
				((LabelDirective) previous).setPc(assembler.getPc());
				
			}
		}
		
		this.pc = pc;
		
		MachineOperand mop1 = op1.resolve(assembler, this);
		MachineOperand mop2 = op2.resolve(assembler, this);
		
		RawInstruction target = this;
		if (!finalPass) {
			target = new RawInstruction(this);
		}
		target.op1 = mop1;
		target.op2 = mop2;
		//target.completeInstruction(pc);

		InstructionTable.calculateInstructionSize(target);
		
		assembler.setPc((short) (target.pc + target.size));
		return new RawInstruction[] { target };
		*/
	//	return new RawInstruction[] { this };
	//}
	
	public int compareTo(RawInstruction o) {
	    	return pc - o.pc;
	}
	
	public int getPc() {
		return pc;
	}

	public String toInfoString() {
		return ">" + Utils.toHex4(pc) + " " + toString() + " @" + size;
	}
	
	public boolean isByteOp() {
		return inst == Isocb || inst == Icb || inst == Iab 
		|| inst == Isb || inst == Iszcb || inst == Imovb;
	}
	
}