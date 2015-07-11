/*
  RawInstruction.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import ejs.base.utils.HexUtils;

public class RawInstruction extends BaseInstruction implements Comparable<RawInstruction>, ICpuInstruction {

	private String name;
	public int pc;
	/** size in bytes */
	private int size;
	public long opcode;
	/** InstTable.I... */
	private int inst;
	private IOperand op1;
	private IOperand op2;
	private IOperand op3;

    /** is the instruction a byte operation */
    public boolean byteop;
    
    private InstInfo info;
    
//    public int fetchCycles;
    
	public RawInstruction() {
	}
	
	public RawInstruction(RawInstruction other) {
		this.name = other.name;
		this.pc = other.pc;
		this.size = other.size;
		this.opcode = other.opcode;
		this.inst = other.getInst();
		this.byteop = other.byteop;
		this.op1 = other.op1;
		this.op2 = other.op2;
		this.op3 = other.op3;
//		this.fetchCycles = other.fetchCycles;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
	    StringBuilder buffer = new StringBuilder();
	    buffer.append(getName());
	    String opstring;
	    opstring = op1 != null ? op1.toString() : null;
	    if (opstring != null) {
	        buffer.append(' ').append(opstring);
	        opstring = op2 != null ? op2.toString() : null;
	        if (opstring != null) {
	            buffer.append(',').append(opstring);
	            opstring = op3 != null ? op3.toString() : null;
		        if (opstring != null) {
		            buffer.append(',').append(opstring);
		        }
	        }
	    }
	    return buffer.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getInst();
		result = prime * result + ((op1 == null) ? 0 : op1.hashCode());
		result = prime * result + ((op2 == null) ? 0 : op2.hashCode());
		result = prime * result + ((op3 == null) ? 0 : op3.hashCode());
		result = prime * result + pc;
		//result = prime * result + size;
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
		if (getInst() != other.getInst()) {
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
		if (op3 == null) {
			if (other.op3 != null) {
				return false;
			}
		} else if (!op3.equals(other.op3)) {
			return false;
		}
		if (pc != other.pc) {
			return false;
		}
		//if (size != other.size) {
		//	return false;
		//}
		return true;
	}

	public int compareTo(RawInstruction o) {
	    return pc - o.pc;
	}
	
	public int getPc() {
		return pc;
	}

	public String toInfoString() {
		return ">" + HexUtils.toHex4(pc) + " " + toString() + " @" + size;
	}
	
	public void setOp1(IOperand op1) {
		this.op1 = op1;
	}

	public IOperand getOp1() {
		return op1;
	}

	public void setOp2(IOperand op2) {
		this.op2 = op2;
	}

	public IOperand getOp2() {
		return op2;
	}
	public void setOp3(IOperand op3) {
		this.op3 = op3;
	}

	public IOperand getOp3() {
		return op3;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.ICPUInstruction#getInst()
	 */
	@Override
	public int getInst() {
		return inst;
	}

	public void setInst(int inst) {
		this.inst = inst;
		this.name = null;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public IMachineOperand getOp(int op) {
		return (IMachineOperand) (op == 1 ? op1 : op == 2 ? op2 : op == 3 ? op3 : null);
	}
	
	public void setOp(int op, IOperand oper) {
		if (op == 1) op1 = oper;
		else if (op == 2) op2 = oper;
		else if (op == 3) op3 = oper;
		else throw new IllegalArgumentException();
	}

	public void setInfo(InstInfo info) {
		this.info = info;
	}

	public InstInfo getInfo() {
		if (info == null) {
			info = new InstInfo();
		}
		return info;
	}
	
}