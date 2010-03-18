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
public class MachineOperand implements Operand {
    // Operand Type
    
    public static final int OP_NONE = -1;

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
    
    public int type = OP_NONE;	// type of operand
    /** value in opcode, usually register or count */
    public int val = 0;	
    /** immediate word */
    public short immed = 0;	
    public boolean byteop = false; // for OP_REG...OP_INC, byte access
    public int dest = OP_DEST_FALSE;	// operand changes (OP_DEST_xxx)
    public boolean bIsCodeDest = false; // operand is an address?

    public int cycles = 0;	// clock cycles needed to read/write
    public Symbol symbol;	// associated symbol
    public boolean symbolResolved; // false initially; true once a defined symbol is converted to val/immed 

    /** 
     * Create an empty operand (to be filled in piecewise)
     * @param type
     */
    public MachineOperand(int type) {
        this.type = type;
    }

    public boolean isMemory() {
        return type == OP_IND || type == OP_ADDR || type == OP_INC || !bIsCodeDest;
    }
    
    public boolean isRegisterReference() {
        return type == OP_REG || type == OP_IND || type == OP_INC 
        	|| type == OP_ADDR && val != 0 || type == OP_REG0_SHIFT_COUNT;        
    }
    
    public boolean isRegisterReference(int reg) {
        return val == reg && isRegisterReference();        
    }
   
    public boolean isRegister() {
        return type == OP_REG;
    }

    public boolean isRegister(int reg) {
        return type == OP_REG && val == reg;
    }

    public boolean isConstant() {
        return type == OP_IMMED || type == OP_CNT || type == OP_ADDR && val == 0; 
    }
    
    public boolean isLabel() {
        return type == OP_IMMED || type == OP_ADDR && val == 0 || type == OP_JUMP;
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
    	case OP_REG:
    		return "R"+val;

    	case OP_IND:
    		return "*R"+val;

    	case OP_ADDR:
    		if (val == 0) {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase();
    		} else {
    			return "@>" + Integer.toHexString(immed & 0xffff).toUpperCase() + "(R" + val + ")";
    		}

    	case OP_INC:
    		return "*R" + val + "+";

    	case OP_IMMED:
    	    return ">" + Integer.toHexString(immed & 0xffff).toUpperCase();

    	case OP_CNT:
    	    return Integer.toString(val);

    	case OP_OFFS_R12: {
    		//byte offs = (byte) ((val >> 1) & 0xff);
    		byte offs = (byte) (val & 0xff);
    	    return ">" + (offs < 0 ? "-" : "") +Integer.toHexString(offs < 0 ? -offs : offs);
    	}

    	case OP_REG0_SHIFT_COUNT:
    	    return ">" + Integer.toHexString(val & 0xffff).toUpperCase();
    	    
    	case OP_JUMP:
    	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();

    	case OP_NONE:
    	case OP_STATUS:		// not real operands
    	case OP_INST:		
    	default:
    		return null;
    	}
	}

    /**
	 * Advance PC and get cycle count for the size of the operand.
	 * 
	 * @param addr
	 *            is current address
	 * @return new address
	 */
	public short advancePc(short addr) {
	    switch (type)
	    {
	    case MachineOperand.OP_ADDR:    // @>xxxx or @>xxxx(Rx)
	        //this.cycles += 8 + Instruction.getMemoryCycles(addr);
	        addr += 2;
	        break;
	    case MachineOperand.OP_IMMED:   // immediate
	        //this.cycles += Instruction.getMemoryCycles(addr);
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
    	case MachineOperand.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
    		immed = domain.readWord(addr); 
    		//this.cycles += 8 + Instruction.getMemoryCycles(addr);
    		addr += 2;
    		break;
    	case MachineOperand.OP_IMMED:	// immediate
    		immed = domain.readWord(addr);
    		//this.cycles += Instruction.getMemoryCycles(addr);
    		addr += 2;
    		break;
    	}
       
        return addr;
    }

    public boolean hasImmediate() {
        return type == OP_ADDR || type == OP_IMMED; 
    }


    /**
     * @return
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

    /**
     * Get the effective address of the operand and fill in its clock cycles.
     * (Memory cycles are accounted through the memory handler.)
     * @return
     */
    public short getEA(MemoryDomain domain, int pc, short wp) {
        short ea = 0;
    	switch (type) {
    	case MachineOperand.OP_NONE:
    		break;
    	case MachineOperand.OP_REG:	// Rx
    		ea = (short) ((val<<1) + wp);
    		this.cycles += 0;
    		break;
    	case MachineOperand.OP_INC:	// *Rx+
    	case MachineOperand.OP_IND: {	// *Rx
    		short ad = (short)((val<<1) + wp);
    		ea = domain.readWord(ad);

    		/* update register if necessary */
    		this.cycles += 4;
    		if (type == MachineOperand.OP_INC) {
    		    this.cycles += byteop ? 2 : 4;
    		    domain.writeWord(ad, (short)(ea + (byteop ? 1 : 2)));
    		}
    		break;
    	}
    	case MachineOperand.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    	    short ad;
    		ea = immed; 
    		this.cycles += 8; //Instruction.getMemoryCycles(ad);
    		if (val != 0) {
    			ad = (short)((val<<1) + wp);
    			ea += domain.readWord(ad);
    		}
    		break;
    	}
    	case MachineOperand.OP_IMMED:	// immediate
    		this.cycles += 0;
    		break;
    	case MachineOperand.OP_CNT:	// shift count
    		this.cycles += 0;
    		break;
    	case MachineOperand.OP_OFFS_R12:	// offset from R12
    		this.cycles += 0;
    		ea = (short) ((12<<1) + wp);
    		break;
    	case MachineOperand.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ea = wp;
    	    this.cycles += 8;
		    break;
    	
    	case MachineOperand.OP_JUMP:	// jump target
    		ea = (short)(val + pc);
    		break;
    	case MachineOperand.OP_STATUS:	// status word
    		break;
    	case MachineOperand.OP_INST:
    		break;		
    	}
    	return ea;
    }

    /**
     * @param memory
     * @return
     */
    public short getValue(MemoryDomain domain, short ea) {
        short value = 0;

        switch (type) {
        case MachineOperand.OP_NONE:
            break;
        case MachineOperand.OP_REG:    // Rx
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = domain.readByte(ea);
				} else {
					value = domain.readWord(ea);
				}
            break;
        case MachineOperand.OP_INC:    // *Rx+
        case MachineOperand.OP_IND: {  // *Rx
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
        case MachineOperand.OP_ADDR: { // @>xxxx or @>xxxx(Rx)
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
        case MachineOperand.OP_IMMED:  // immediate
            value = immed;
            break;
        case MachineOperand.OP_CNT:    // shift count
            value = (short) val;
            break;
        case MachineOperand.OP_OFFS_R12:   // offset from R12
            value = (short) (domain.readWord(ea) + val);
            break;
        case MachineOperand.OP_REG0_SHIFT_COUNT: // shift count from R0
            value = (short) (domain.readWord(ea) & 0xf);
            if (value == 0) {
				value = 16;
			}
            break;
        
        case MachineOperand.OP_JUMP:   // jump target
            value = ea;
            break;
        case MachineOperand.OP_STATUS: // status word
            //TODO: NOTHING -- make sure we don't depend on this   
            break;
        case MachineOperand.OP_INST:
            value = domain.readWord(ea);
            break;      
        }

        return value;
    }

	public void convertToImmedate() {
		if (type == OP_IMMED || type == OP_ADDR)	// hack
			return;
		org.ejs.coffee.core.utils.Check.checkState((type == MachineOperand.OP_REG));
		type = OP_IMMED;
		immed = (short) val;
	}

	/** Generate the bits for the operand, or throw IllegalArgumentException
	 * for a non-machine operand */
	public int getBits() {
		switch (type)
        {
        case MachineOperand.OP_NONE:
        	return 0;
        case MachineOperand.OP_REG:    	// Rx
        case MachineOperand.OP_INC:    	// *Rx+
        case MachineOperand.OP_IND:   	// *Rx
        case MachineOperand.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
        case MachineOperand.OP_REG0_SHIFT_COUNT: // shift count from R0
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal register number: " + val);
        	return val | (type << 4);
        case MachineOperand.OP_IMMED:  // immediate
            return 0;
        case MachineOperand.OP_CNT:    // shift count
        	if (val < 0 || val > 15)
        		throw new IllegalArgumentException("Illegal shift count: " + val);
            return val;
        case MachineOperand.OP_OFFS_R12:   // offset from R12 or jump offset
        	if (val < -255 || val > 255)
        		throw new IllegalArgumentException("Illegal offset: " + val);
        	return val & 0xff;
        case MachineOperand.OP_JUMP:   // jump target offset from PC
        	int byt = (val - 2) / 2;
        	if (byt < -0x80 || byt >= 0x80)
        		throw new IllegalArgumentException("Illegal jump offset: " + val);
        	return byt & 0xff;
        case MachineOperand.OP_STATUS: // status word
        case MachineOperand.OP_INST:
        	// not real operand
        	return 0;
        }
		throw new IllegalArgumentException("Non-compilable operand: " + this);
	}
	
	public Operand resolve(RawInstruction inst) {
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + immed;
		result = prime * result + type;
		result = prime * result + val;
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
		MachineOperand other = (MachineOperand) obj;
		int rtype = reduceType(type);
		int rotype = reduceType(other.type);
		if (rtype != rotype) {
			return false;
		}
		if ((type == OP_ADDR || type == OP_IMMED) && immed != other.immed) {
			return false;
		}
		if (type != OP_IMMED && val != other.val) {
			return false;
		}

		return true;
	}

	private int reduceType(int type) {
		if (type == OP_STATUS || type == OP_INST)
			return OP_NONE;
		return type;
	}

	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		if (symbol != null && !symbolResolved) {
			if (!symbol.isDefined())
				throw new ResolveException(this, "Undefined symbol " + symbol);
			
			short theVal;
			if (type == OP_JUMP || type == OP_OFFS_R12) {
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

	public static MachineOperand createImmediate(int i) {
		MachineOperand op = new MachineOperand(MachineOperand.OP_IMMED);
		op.immed = (short) (op.val = i);
		return op;
	}

	public static MachineOperand createGeneralOperand(int type, short val) {
		MachineOperand op = new MachineOperand(type);
		op.val = val;
		return op;
	}

	public static MachineOperand createGeneralOperand(int type, short val, short immed) {
		MachineOperand op = new MachineOperand(type);
		op.val = val;
		op.immed = immed;
		return op;
	}

	public static MachineOperand createSymbolImmediate(Symbol symbol) {
		MachineOperand op = new MachineOperand(MachineOperand.OP_IMMED);
		if (symbol.isDefined()) {
			op.immed = (short) (op.val = symbol.getAddr());
			op.symbolResolved = true;
		}
		op.symbol = symbol;
		return op;
	}

	public static MachineOperand createEmptyOperand() {
		return new MachineOperand(OP_NONE);
	}

	public static MachineOperand createJumpNextOperand() {
		MachineOperand op = new MachineOperand(MachineOperand.OP_JUMP);
		op.val = 2;
		return op;
	}

	
}
