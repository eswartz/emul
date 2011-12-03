/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;

import org.ejs.coffee.core.utils.Check;

import v9t9.engine.memory.MemoryDomain;

/**
 * A machine operand, as parsed from the instruction.
 * @author ejs
 */
public class MachineOperand9900 extends BaseMachineOperand {
	public int cycles = 0;
	public boolean byteop = false;

    // Operand Type
    
    //  from ts/td field of opcode, don't change order
	/** register Rx */
	public static final int OP_REG = 0;
	/** indirect *Rx */
	public static final int OP_IND = 1;
	/** address @>xxxx or @>xxxx(Rx) */ 
	public static final int OP_ADDR = 2;
	/**  register increment *Rx+ */
	public static final int OP_INC = 3;
	// these depend on the actual instruction
	public static final int OP_IMMED = 4; // immediate >xxxx (for jump, the target addr)
	public static final int OP_CNT = 5; // shift count x (4 bits)
	public static final int OP_JUMP = 6; // jump target >xxxx (offset in bytes from PC)
	public static final int OP_OFFS_R12 = 7; // offset >xxxx or .xxxx
	public static final int OP_STATUS = 8; // status word >xxxx
	public static final int OP_INST = 9; // instruction for X
	public static final int OP_REG0_SHIFT_COUNT = 10;	// shift count from R0
	
	// special val/reg base for unresolved PC-relative operand
	public static final short PCREL = 16;

	/** 
     * Create an empty operand (to be filled in piecewise)
     * @param type
     */
    public MachineOperand9900(int type) {
        this.type = type;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isMemory()
	 */
    public boolean isMemory() {
        return type == MachineOperand9900.OP_IND || type == MachineOperand9900.OP_ADDR || type == MachineOperand9900.OP_INC || !bIsReference;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegisterReference()
	 */
    public boolean isRegisterReference() {
        return type == MachineOperand9900.OP_REG || type == MachineOperand9900.OP_IND || type == MachineOperand9900.OP_INC 
        	|| type == MachineOperand9900.OP_ADDR && val != 0 || type == MachineOperand9900.OP_REG0_SHIFT_COUNT;        
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegisterReference(int)
	 */
    public boolean isRegisterReference(int reg) {
        return val == reg && isRegisterReference();        
    }
   
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegister()
	 */
    public boolean isRegister() {
        return type == MachineOperand9900.OP_REG;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegister(int)
	 */
    public boolean isRegister(int reg) {
        return type == MachineOperand9900.OP_REG && val == reg;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isConstant()
	 */
    public boolean isConstant() {
        return type == MachineOperand9900.OP_IMMED || type == MachineOperand9900.OP_CNT || type == MachineOperand9900.OP_ADDR && val == 0; 
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isLabel()
	 */
    public boolean isLabel() {
        return type == MachineOperand9900.OP_IMMED || type == MachineOperand9900.OP_ADDR && val == 0 || type == MachineOperand9900.OP_JUMP;
    }
    
    /*
     * Print out an operand into a disassembler operand, returns NULL if no
     * printable information
     */
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#toString()
	 */
    @Override
	public String toString() {
    	String basic = basicString();
    	return basic;
    }

	private String basicString() {
		switch (type) 
    	{
    	case MachineOperand9900.OP_REG:
    		return "R"+val;

    	case MachineOperand9900.OP_IND:
    		return "*R"+val;

    	case MachineOperand9900.OP_ADDR:
    		if (val == 0) {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase();
    		} else {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase() + "(R" + val + ")";
    		}

    	case MachineOperand9900.OP_INC:
    		return "*R" + val + "+";

    	case MachineOperand9900.OP_IMMED:
    	    return ">" + Integer.toHexString(immed & 0xffff).toUpperCase();

    	case MachineOperand9900.OP_CNT:
    	    return Integer.toString(val);

    	case MachineOperand9900.OP_OFFS_R12: {
    		//byte offs = (byte) ((val >> 1) & 0xff);
    		byte offs = (byte) (val & 0xff);
    	    return ">" + (offs < 0 ? "-" : "") +Integer.toHexString(offs < 0 ? -offs : offs);
    	}

    	case MachineOperand9900.OP_REG0_SHIFT_COUNT:
    	    return ">" + Integer.toHexString(val & 0xffff).toUpperCase();
    	    
    	case MachineOperand9900.OP_JUMP:
    	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();

    	case OP_NONE:
    	case MachineOperand9900.OP_STATUS:		// not real operands
    	case MachineOperand9900.OP_INST:		
    	default:
    		return null;
    	}
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#advancePc(short)
	 */
	public short advancePc(short addr) {
	    switch (type)
	    {
	    case MachineOperand9900.OP_ADDR:    // @>xxxx or @>xxxx(Rx)
	        addr += 2;
	        break;
	    case MachineOperand9900.OP_IMMED:   // immediate
	        addr += 2;
	        break;
	    }
	
	    return addr;
	}

	/**
	 * Read any extra immediates for an operand from the instruction stream.
	 * Fills in Operand.size and Operand.immed.
	 * 
	 * @param w
	 * @param addr
	 *            is current address
	 * @param pc
	 *            address of instruction
	 * @param wp
	 *            workspace pointer
	 * @return new address
	 */
    public short fetchOperandImmediates(MemoryDomain domain, short addr) {
    	switch (type) {
    	case MachineOperand9900.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
    		immed = domain.readWord(addr); 
    		addr += 2;
    		break;
    	case MachineOperand9900.OP_IMMED:	// immediate
    		immed = domain.readWord(addr);
    		addr += 2;
    		break;
    	}
       
        return addr;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#hasImmediate()
	 */
    public boolean hasImmediate() {
        return type == MachineOperand9900.OP_ADDR || type == MachineOperand9900.OP_IMMED; 
    }


    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#valueString(short, short)
	 */
    public String valueString(short ea, short theValue) {
       if (type == OP_NONE) {
		return null;
	}
       if (byteop) {
		theValue &= 0xff;
	}
        return Integer.toHexString(theValue & 0xffff).toUpperCase()
        	+"(@"+Integer.toHexString(ea & 0xffff).toUpperCase()+")"; 
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#getEA(v9t9.engine.memory.MemoryDomain, int, short)
	 */
    public short getEA(BaseInstructionWorkBlock block) {
    	short wp = ((InstructionWorkBlock9900) block).wp;
        short ea = 0;
    	switch (type) {
    	case MachineOperand.OP_NONE:
    		break;
    	case MachineOperand9900.OP_REG:	// Rx
    		ea = (short) ((val<<1) + wp);
    		this.cycles += 0;
    		break;
    	case MachineOperand9900.OP_INC:	// *Rx+
    	case MachineOperand9900.OP_IND: {	// *Rx
    		short ad = (short)((val<<1) + wp);
    		ea = block.domain.readWord(ad);

    		/* update register if necessary */
    		this.cycles += 4;
    		if (type == MachineOperand9900.OP_INC) {
    		    this.cycles += byteop ? 2 : 4;
    		    block.domain.writeWord(ad, (short)(ea + (byteop ? 1 : 2)));
    		}
    		break;
    	}
    	case MachineOperand9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    	    short ad;
    		ea = immed; 
    		this.cycles += 8; //Instruction.getMemoryCycles(ad);
    		if (val != 0) {
    			ad = (short)((val<<1) + wp);
    			ea += block.domain.readWord(ad);
    		}
    		break;
    	}
    	case MachineOperand9900.OP_IMMED:	// immediate
    		this.cycles += 0;
    		break;
    	case MachineOperand9900.OP_CNT:	// shift count
    		this.cycles += 0;
    		break;
    	case MachineOperand9900.OP_OFFS_R12:	// offset from R12
    		this.cycles += 0;
    		ea = (short) ((12<<1) + wp);
    		break;
    	case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ea = wp;
    	    this.cycles += 8;
		    break;
    	
    	case MachineOperand9900.OP_JUMP:	// jump target
    		ea = (short)(val + block.inst.pc);
    		break;
    	case MachineOperand9900.OP_STATUS:	// status word
    		break;
    	case MachineOperand9900.OP_INST:
    		break;		
    	}
    	return ea;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#getValue(v9t9.engine.memory.MemoryDomain, short)
	 */
    public short getValue(BaseInstructionWorkBlock block, short ea) {
        short value = 0;

        switch (type) {
        case MachineOperand.OP_NONE:
            break;
        case MachineOperand9900.OP_REG:    // Rx
            if (bIsReference) {
				value = ea;
			} else
                if (byteop) {
					value = block.domain.readByte(ea);
				} else {
					value = block.domain.readWord(ea);
				}
            break;
        case MachineOperand9900.OP_INC:    // *Rx+
        case MachineOperand9900.OP_IND: {  // *Rx
            if (bIsReference) {
				value = ea;
			} else
                if (byteop) {
					value = block.domain.readByte(ea);
				} else {
					value = block.domain.readWord(ea);
				}
            break;
        }
        case MachineOperand9900.OP_ADDR: { // @>xxxx or @>xxxx(Rx)
            if (bIsReference) {
				value = ea;
			} else
                if (byteop) {
					value = block.domain.readByte(ea);
				} else {
					value = block.domain.readWord(ea);
				}
            break;
        }
        case MachineOperand9900.OP_IMMED:  // immediate
            value = immed;
            break;
        case MachineOperand9900.OP_CNT:    // shift count
            value = (short) val;
            break;
        case MachineOperand9900.OP_OFFS_R12:   // offset from R12
            value = (short) (block.domain.readWord(ea) + val);
            break;
        case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
            value = (short) (block.domain.readWord(ea) & 0xf);
            if (value == 0) {
				value = 16;
			}
            break;
        
        case MachineOperand9900.OP_JUMP:   // jump target
            value = ea;
            break;
        case MachineOperand9900.OP_STATUS: // status word
            //TODO: NOTHING -- make sure we don't depend on this   
            break;
        case MachineOperand9900.OP_INST:
            value = block.domain.readWord(ea);
            break;      
        }

        return value;
    }

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#convertToImmedate()
	 */
	public void convertToImmedate() {
		if (type == MachineOperand9900.OP_IMMED || type == MachineOperand9900.OP_ADDR)	// hack
			return;
		Check.checkState((type == MachineOperand9900.OP_REG));
		type = MachineOperand9900.OP_IMMED;
		immed = (short) val;
	}

	/** Generate the bits for the operand, or throw IllegalArgumentException
	 * for a non-machine operand */
	public int getBits() {
		switch (type)
        {
        case MachineOperand.OP_NONE:
        	return 0;
        case MachineOperand9900.OP_REG:    	// Rx
        case MachineOperand9900.OP_INC:    	// *Rx+
        case MachineOperand9900.OP_IND:   	// *Rx
        case MachineOperand9900.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
        case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal register number: " + val);
        	return val | (type << 4);
        case MachineOperand9900.OP_IMMED:  // immediate
            return 0;
        case MachineOperand9900.OP_CNT:    // shift count
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal shift count: " + val);
            return val;
        case MachineOperand9900.OP_OFFS_R12:   // offset from R12 or jump offset
        	if (val < -255 || val > 255)
        		throw new IllegalArgumentException("Illegal offset: " + val);
        	return val & 0xff;
        case MachineOperand9900.OP_JUMP:   // jump target offset from PC
        	int byt = (val - 2) / 2;
        	if (byt < -0x80 || byt >= 0x80)
        		throw new IllegalArgumentException("Illegal jump offset: " + val);
        	return byt & 0xff;
        case MachineOperand9900.OP_STATUS: // status word
        case MachineOperand9900.OP_INST:
        	// not real operand
        	return 0;
        }
		throw new IllegalArgumentException("Non-compilable operand: " + this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#resolve(v9t9.engine.cpu.RawInstruction)
	 */
	public Operand resolve(RawInstruction inst) {
		return this;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + immed;
		result = prime * result + type;
		result = prime * result + val;
		return result;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#equals(java.lang.Object)
	 */
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
		MachineOperand9900 other = (MachineOperand9900) obj;
		int rtype = reduceType(type);
		int rotype = reduceType(other.type);
		if (rtype != rotype) {
			return false;
		}
		if ((type == MachineOperand9900.OP_ADDR || type == MachineOperand9900.OP_IMMED) && immed != other.immed) {
			return false;
		}
		if (type != MachineOperand9900.OP_IMMED && val != other.val) {
			return false;
		}

		return true;
	}

	private int reduceType(int type) {
		if (type == MachineOperand9900.OP_STATUS || type == MachineOperand9900.OP_INST)
			return OP_NONE;
		return type;
	}

	public static MachineOperand9900 createImmediate(int i) {
		MachineOperand9900 op = new MachineOperand9900(MachineOperand9900.OP_IMMED);
		op.immed = (short) (op.val = i);
		return op;
	}

	public static MachineOperand9900 createGeneralOperand(int type, short val) {
		MachineOperand9900 op = new MachineOperand9900(type);
		op.val = val;
		return op;
	}

	public static MachineOperand9900 createGeneralOperand(int type, short val, short immed) {
		MachineOperand9900 op = new MachineOperand9900(type);
		op.val = val;
		op.immed = immed;
		return op;
	}

	public static MachineOperand9900 createEmptyOperand() {
		return new MachineOperand9900(OP_NONE);
	}

}
