/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.Symbol;

/**
 * A machine operand, as parsed from the instruction.
 * @author ejs
 */
public class MachineOperand9900 extends BaseMachineOperand {
    // Operand Type
    
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
        return type == InstTable9900.OP_IND || type == InstTable9900.OP_ADDR || type == InstTable9900.OP_INC || !bIsCodeDest;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegisterReference()
	 */
    public boolean isRegisterReference() {
        return type == InstTable9900.OP_REG || type == InstTable9900.OP_IND || type == InstTable9900.OP_INC 
        	|| type == InstTable9900.OP_ADDR && val != 0 || type == InstTable9900.OP_REG0_SHIFT_COUNT;        
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
        return type == InstTable9900.OP_REG;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegister(int)
	 */
    public boolean isRegister(int reg) {
        return type == InstTable9900.OP_REG && val == reg;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isConstant()
	 */
    public boolean isConstant() {
        return type == InstTable9900.OP_IMMED || type == InstTable9900.OP_CNT || type == InstTable9900.OP_ADDR && val == 0; 
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isLabel()
	 */
    public boolean isLabel() {
        return type == InstTable9900.OP_IMMED || type == InstTable9900.OP_ADDR && val == 0 || type == InstTable9900.OP_JUMP;
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
    	if (symbol != null && !symbolResolved) {
    		if (basic == null)
    			basic = "";
    		basic += "{" + symbol +"}";
    	}
    	return basic;
    }

	private String basicString() {
		switch (type) 
    	{
    	case InstTable9900.OP_REG:
    		return "R"+val;

    	case InstTable9900.OP_IND:
    		return "*R"+val;

    	case InstTable9900.OP_ADDR:
    		if (val == 0) {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase();
    		} else {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase() + "(R" + val + ")";
    		}

    	case InstTable9900.OP_INC:
    		return "*R" + val + "+";

    	case InstTable9900.OP_IMMED:
    	    return ">" + Integer.toHexString(immed & 0xffff).toUpperCase();

    	case InstTable9900.OP_CNT:
    	    return Integer.toString(val);

    	case InstTable9900.OP_OFFS_R12: {
    		//byte offs = (byte) ((val >> 1) & 0xff);
    		byte offs = (byte) (val & 0xff);
    	    return ">" + (offs < 0 ? "-" : "") +Integer.toHexString(offs < 0 ? -offs : offs);
    	}

    	case InstTable9900.OP_REG0_SHIFT_COUNT:
    	    return ">" + Integer.toHexString(val & 0xffff).toUpperCase();
    	    
    	case InstTable9900.OP_JUMP:
    	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();

    	case OP_NONE:
    	case InstTable9900.OP_STATUS:		// not real operands
    	case InstTable9900.OP_INST:		
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
	    case InstTable9900.OP_ADDR:    // @>xxxx or @>xxxx(Rx)
	        addr += 2;
	        break;
	    case InstTable9900.OP_IMMED:   // immediate
	        addr += 2;
	        break;
	    }
	
	    return addr;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#fetchOperandImmediates(v9t9.engine.memory.MemoryDomain, short)
	 */
    public short fetchOperandImmediates(MemoryDomain domain, short addr) {
    	switch (type) {
    	case InstTable9900.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
    		immed = domain.readWord(addr); 
    		addr += 2;
    		break;
    	case InstTable9900.OP_IMMED:	// immediate
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
        return type == InstTable9900.OP_ADDR || type == InstTable9900.OP_IMMED; 
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
    public short getEA(MemoryDomain domain, int pc, short wp) {
        short ea = 0;
    	switch (type) {
    	case MachineOperand.OP_NONE:
    		break;
    	case InstTable9900.OP_REG:	// Rx
    		ea = (short) ((val<<1) + wp);
    		this.cycles += 0;
    		break;
    	case InstTable9900.OP_INC:	// *Rx+
    	case InstTable9900.OP_IND: {	// *Rx
    		short ad = (short)((val<<1) + wp);
    		ea = domain.readWord(ad);

    		/* update register if necessary */
    		this.cycles += 4;
    		if (type == InstTable9900.OP_INC) {
    		    this.cycles += byteop ? 2 : 4;
    		    domain.writeWord(ad, (short)(ea + (byteop ? 1 : 2)));
    		}
    		break;
    	}
    	case InstTable9900.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    	    short ad;
    		ea = immed; 
    		this.cycles += 8; //Instruction.getMemoryCycles(ad);
    		if (val != 0) {
    			ad = (short)((val<<1) + wp);
    			ea += domain.readWord(ad);
    		}
    		break;
    	}
    	case InstTable9900.OP_IMMED:	// immediate
    		this.cycles += 0;
    		break;
    	case InstTable9900.OP_CNT:	// shift count
    		this.cycles += 0;
    		break;
    	case InstTable9900.OP_OFFS_R12:	// offset from R12
    		this.cycles += 0;
    		ea = (short) ((12<<1) + wp);
    		break;
    	case InstTable9900.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ea = wp;
    	    this.cycles += 8;
		    break;
    	
    	case InstTable9900.OP_JUMP:	// jump target
    		ea = (short)(val + pc);
    		break;
    	case InstTable9900.OP_STATUS:	// status word
    		break;
    	case InstTable9900.OP_INST:
    		break;		
    	}
    	return ea;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#getValue(v9t9.engine.memory.MemoryDomain, short)
	 */
    public short getValue(MemoryDomain domain, short ea) {
        short value = 0;

        switch (type) {
        case MachineOperand.OP_NONE:
            break;
        case InstTable9900.OP_REG:    // Rx
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = domain.readByte(ea);
				} else {
					value = domain.readWord(ea);
				}
            break;
        case InstTable9900.OP_INC:    // *Rx+
        case InstTable9900.OP_IND: {  // *Rx
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = domain.readByte(ea);
				} else {
					value = domain.readWord(ea);
				}
            break;
        }
        case InstTable9900.OP_ADDR: { // @>xxxx or @>xxxx(Rx)
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = domain.readByte(ea);
				} else {
					value = domain.readWord(ea);
				}
            break;
        }
        case InstTable9900.OP_IMMED:  // immediate
            value = immed;
            break;
        case InstTable9900.OP_CNT:    // shift count
            value = (short) val;
            break;
        case InstTable9900.OP_OFFS_R12:   // offset from R12
            value = (short) (domain.readWord(ea) + val);
            break;
        case InstTable9900.OP_REG0_SHIFT_COUNT: // shift count from R0
            value = (short) (domain.readWord(ea) & 0xf);
            if (value == 0) {
				value = 16;
			}
            break;
        
        case InstTable9900.OP_JUMP:   // jump target
            value = ea;
            break;
        case InstTable9900.OP_STATUS: // status word
            //TODO: NOTHING -- make sure we don't depend on this   
            break;
        case InstTable9900.OP_INST:
            value = domain.readWord(ea);
            break;      
        }

        return value;
    }

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#convertToImmedate()
	 */
	public void convertToImmedate() {
		if (type == InstTable9900.OP_IMMED || type == InstTable9900.OP_ADDR)	// hack
			return;
		org.ejs.coffee.core.utils.Check.checkState((type == InstTable9900.OP_REG));
		type = InstTable9900.OP_IMMED;
		immed = (short) val;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#getBits()
	 */
	public int getBits() {
		switch (type)
        {
        case MachineOperand.OP_NONE:
        	return 0;
        case InstTable9900.OP_REG:    	// Rx
        case InstTable9900.OP_INC:    	// *Rx+
        case InstTable9900.OP_IND:   	// *Rx
        case InstTable9900.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
        case InstTable9900.OP_REG0_SHIFT_COUNT: // shift count from R0
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal register number: " + val);
        	return val | (type << 4);
        case InstTable9900.OP_IMMED:  // immediate
            return 0;
        case InstTable9900.OP_CNT:    // shift count
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal shift count: " + val);
            return val;
        case InstTable9900.OP_OFFS_R12:   // offset from R12 or jump offset
        	if (val < -255 || val > 255)
        		throw new IllegalArgumentException("Illegal offset: " + val);
        	return val & 0xff;
        case InstTable9900.OP_JUMP:   // jump target offset from PC
        	int byt = (val - 2) / 2;
        	if (byt < -0x80 || byt >= 0x80)
        		throw new IllegalArgumentException("Illegal jump offset: " + val);
        	return byt & 0xff;
        case InstTable9900.OP_STATUS: // status word
        case InstTable9900.OP_INST:
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
		if ((type == InstTable9900.OP_ADDR || type == InstTable9900.OP_IMMED) && immed != other.immed) {
			return false;
		}
		if (type != InstTable9900.OP_IMMED && val != other.val) {
			return false;
		}

		return true;
	}

	private int reduceType(int type) {
		if (type == InstTable9900.OP_STATUS || type == InstTable9900.OP_INST)
			return OP_NONE;
		return type;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#resolve(v9t9.tools.asm.assembler.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		if (symbol != null && !symbolResolved) {
			if (!symbol.isDefined())
				throw new ResolveException(this, "Undefined symbol " + symbol);
			
			short theVal;
			if (type == InstTable9900.OP_JUMP || type == InstTable9900.OP_OFFS_R12) {
				theVal = (short) (symbol.getAddr() - inst.getPc());
				/*
				 * check this later
				if (theVal < -256 || theVal >= 256)
					throw new OutOfRangeException(inst, this, symbol, theVal);
					*/
				val = theVal + immed;
				immed = 0;
			} else {
				theVal = (short) symbol.getAddr();
				immed += theVal;
			}
			symbolResolved = true;
		}
		return this;
	}

	public static MachineOperand9900 createImmediate(int i) {
		MachineOperand9900 op = new MachineOperand9900(InstTable9900.OP_IMMED);
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

	public static MachineOperand createSymbolImmediate(Symbol symbol) {
		MachineOperand9900 op = new MachineOperand9900(InstTable9900.OP_IMMED);
		if (symbol.isDefined()) {
			op.immed = (short) (op.val = symbol.getAddr());
			op.symbolResolved = true;
		}
		op.symbol = symbol;
		return op;
	}

	public static MachineOperand9900 createEmptyOperand() {
		return new MachineOperand9900(OP_NONE);
	}

	public static MachineOperand createJumpNextOperand() {
		MachineOperand9900 op = new MachineOperand9900(InstTable9900.OP_JUMP);
		op.val = 2;
		return op;
	}

	
}
