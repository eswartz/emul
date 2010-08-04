/*
 * (c) Ed Swartz, 2010
 */
package v9t9.engine.cpu;

import org.ejs.coffee.core.utils.Check;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.Symbol;

/**
 * A machine operand, as parsed from an instruction or converted from an
 * assembler.
 * <p>
 * In this operand, the 'type' is the semantic type. This may or may not
 * directly map to bits in an instruction, but should be uniquely parseable in
 * the context of a given instruction. For example, a jump instruction will have
 * a {@link #OP_PCREL} operand, while <code>@10(PC)</code> would be
 * {@link #OP_OFFS}.
 * <p>
 * There may be multiple ways -- less and more efficient -- to represent the
 * same operand (e.g. an explicit immediate vs. some addressing magic on SR).
 * For this, 'encoding' and the OP_ENC_xxx macros tell the difference. Usually,
 * this are set to something other than {@link #OP_ENC_UNSET} only by a
 * disassembler, for use in round-trip converting from binary.  The assembler
 * should not usually set this, because it means clients have to be more
 * careful when modifying the operand. 
 * 
 * @author ejs
 */
public class MachineOperandMFP201 extends BaseMachineOperand {
	public static final int SP = 13;
	public static final int PC = 14;
	public static final int SR = 15;
	
    // Operand Type
    
    //  Raw types from As/Ad field of opcode, don't change order
	/** 4-bit register Rx */
	public static final int OP_REG = 0;
	/** 16-bit register offset @>xxxx(Rx) */ 
	public static final int OP_OFFS = 1;
	/** 4-bit indirect @Rx */
	public static final int OP_IND = 2;
	/** 4-bit register increment @Rx+ */
	public static final int OP_INC = 3;
	
	// These are semantic operand types, whose meanings are 
	// dictated on the actual instruction.
	/** immediate >xxxx */
	public static final int OP_IMM = 4;
	/** shift count x (4 bits) */
	public static final int OP_CNT = 6; 
	/** signed PC-relative offset in bytes */
	public static final int OP_PCREL = 7; 
	
	/** @>offs(Rx+Ry*scale) -- uses scaleReg, scale */
	public static final int OP_SRO = 8;
	
	/** shift count from R0 */
	public static final int OP_REG0_SHIFT_COUNT = 11;	

	/** 4-bit register decrement @Rx-, when under control of LOOP */
	public static final int OP_DEC = 13;

	/** push/pop count */
	public static final int OP_PUSH_POP_COUNT = 14;
	/** scale factor for LEA (power of 2, 0-7) */
	public static final int OP_SCALE = 15;

	// Operand Encoding Types
	
	/** Unknown or unspecified encoding */
	public static final int OP_ENC_UNSET = 0;
	/** Implicit immediate, e.g. 2 as Rx=SR and As=2 (for {@link #OP_IMM}) */
	public static final int OP_ENC_IMM_IMPLICIT = 1;
	/** Explicit 8-bit signed immediate (for {@link #OP_IMM} or {@link #OP_PCREL}) */
	public static final int OP_ENC_IMM8 = 2;
	/** Explicit 8-bit signed immediate (for {@link #OP_IMM} or {@link #OP_PCREL}) */
	public static final int OP_ENC_PCREL8 = 2;
	/** Explicit 16-bit signed immediate  (for {@link #OP_IMM}) */
	public static final int OP_ENC_IMM16 = 3;
	/** Explicit 12-bit signed immediate (for {@link #OP_PCREL} with mem/size)*/
	public static final int OP_ENC_PCREL12 = 4;
	/** Explicit 16-bit signed immediate (for {@link #OP_PCREL} with mem/size)*/
	public static final int OP_ENC_PCREL16 = 5;
	
	public int encoding;
	
	public int scaleBits;
	public int scaleReg;
	
	/** 
     * Create an empty operand (to be filled in piecewise)
     * @param type
     */
    public MachineOperandMFP201(int type) {
        this.type = type;
        this.encoding = OP_ENC_UNSET;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isMemory()
	 */
    public boolean isMemory() {
        return type == OP_IND || type == OP_OFFS || type == OP_INC || !bIsCodeDest;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegisterReference()
	 */
    public boolean isRegisterReference() {
        return type == OP_REG || type == OP_IND || type == OP_INC 
        	|| type == OP_OFFS && val != SR || type == OP_REG0_SHIFT_COUNT;        
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
        return type == OP_REG;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegister(int)
	 */
    public boolean isRegister(int reg) {
        return type == OP_REG && val == reg;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isConstant()
	 */
    public boolean isConstant() {
        return type == OP_IMM || type == OP_CNT; 
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isLabel()
	 */
    public boolean isLabel() {
        return type == OP_IMM || type == OP_OFFS && (val == SR || val == PC) 
        	|| type == OP_PCREL;
    }
    
    /*
     * Print out an operand into a disassembler operand, returns NULL if no
     * printable information
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
    		return regName(val);

    	case OP_IND:
    		return "@"+regName(val);

    	case OP_OFFS: {
    		String addr = HexUtils.toHex4(immed);
    		if (val == SR) {
    			return "&>" + addr;
    		} else {
    			return "@>" + addr + "(" + regName(val) + ")";
    		}
    	}
    	case OP_SRO: {
    		String addr = "@>" + Integer.toHexString(immed & 0xffff).toUpperCase();
    		String scaled = regName(val) + "+" + regName(scaleReg)
    			+ (scaleBits > 1 ? "*" + scaleBits : "");
    		return addr + "(" + scaled + ")";
    	}
    	case OP_INC:
    		return "@" + regName(val) + "+";
    	case OP_DEC:
    		return "@" + regName(val) + "-";
    	
    	case OP_IMM:
    		if (encoding == OP_ENC_IMM8)
    			return "#>" + Integer.toHexString(immed & 0xff).toUpperCase();
    		else if (encoding == OP_ENC_PCREL12)
    			return "#>" + Integer.toHexString(immed & 0xfff).toUpperCase();
    		else
    			return "#>" + Integer.toHexString(immed & 0xffff).toUpperCase();

    	case OP_PUSH_POP_COUNT:
    	case OP_CNT:
    	case OP_REG0_SHIFT_COUNT:
    	    return "#" + Integer.toString(val);
    	case OP_SCALE:
    		return Integer.toString(1 << val);
    	    
    	case OP_PCREL:
    	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();

    	case OP_NONE:
    	default:
    		return null;
    	}
	}

    /**
	 * @param val
	 * @return
	 */
	public String regName(int val) {
		switch (val) {
		case SP: return "SP";
		case SR: return "SR";
		case PC: return "PC";
		}
		return "R" + val;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#advancePc(short)
	 */
	public short advancePc(short addr) {
	    switch (type)
	    {
	    case OP_OFFS:
	    case OP_IMM:  
	    case OP_SRO:
	    	if (encoding == OP_ENC_IMM8 || encoding == OP_ENC_PCREL12)
	    		addr++;
	    	else 
	    		addr += 2;
	        break;
	    }
	
	    return addr;
	}
	
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#hasImmediate()
	 */
    public boolean hasImmediate() {
        return type == OP_OFFS || type == OP_PCREL ||
        	(type == OP_IMM && encoding != OP_ENC_IMM_IMPLICIT)
        	|| type == OP_SRO;
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
		return Integer.toHexString(theValue & 0xffff).toUpperCase() + "(@"
				+ Integer.toHexString(ea & 0xffff).toUpperCase() + ")";
	}

    public short getEA(InstructionWorkBlock block) {
        short ea = 0;
    	switch (type) {
    	case MachineOperand.OP_NONE:
    		break;
    	case OP_REG:	
    		// no address!
    		ea = (short) val;
    		break;
    	case OP_INC:	// @Rx+
    	case OP_DEC:	// @Rx-
    	case OP_IND: {	// @Rx
    		ea = (short) block.cpu.getRegister(val);

    		/* update register if necessary */
    		this.cycles += 4;
    		if (type == OP_INC) {
    		    this.cycles += byteop ? 2 : 4;
    		    block.cpu.setRegister(val, (ea + (byteop ? 1 : 2)));
    		}
    		else if (type == OP_DEC) {
    		    this.cycles += byteop ? 2 : 4;
    		    block.cpu.setRegister(val, (ea - (byteop ? 1 : 2)));
    		}
    		break;
    	}
    	case OP_OFFS: {	// @>xxxx or @>xxxx(Rx)
    		ea = immed; 
    		this.cycles += 8;
    		if (val != SR) {
    			ea += (short) block.cpu.getRegister(val);
    		}
    		break;
    	}
    	case OP_SRO: {
    		ea = immed; 
    		this.cycles += 12;
    		if (val != SR) {
    			ea += (short) block.cpu.getRegister(val);
    		}
    		ea += (short) block.cpu.getRegister(scaleReg) * scaleBits;
    		break;
    	}
    	case OP_IMM:	// immediate
    		this.cycles += 0;
    		break;
    	case OP_CNT:	// shift count
    		this.cycles += 0;
    		break;
    	case OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ea = 0;
    	    this.cycles += 8;
		    break;
    	
    	case OP_PCREL:
    		ea = (short)(val + block.inst.pc);
    		break;
    	}
    	return ea;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#getValue(v9t9.engine.memory.MemoryDomain, short)
	 */
    public short getValue(InstructionWorkBlock block, short ea) {
        short value = 0;

        switch (type) {
        case MachineOperand.OP_NONE:
            break;
        case OP_REG:    // Rx
            if (bIsCodeDest) {
				value = ea;
			} else {
				// byteop only applies to memory
				value = (short) block.cpu.getRegister(ea);
			}
            break;
        case OP_INC:    // @Rx+
        case OP_DEC:	// @Rx-
        case OP_IND: {  // @Rx
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = block.domain.readByte(ea);
				} else {
					value = block.domain.readWord(ea);
				}
            break;
        }
        case OP_OFFS: { // @>xxxx or @>xxxx(Rx)
            if (bIsCodeDest) {
				value = ea;
			} else
                if (byteop) {
					value = block.domain.readByte(ea);
				} else {
					value = block.domain.readWord(ea);
				}
            break;
        }
        case OP_SRO: // scaled reg offset, only used in LEA, thus EA is value
        	value = ea;
        	break;
        	
        case OP_IMM:  // immediate
            value = immed;
            break;
        case OP_CNT:    // shift count
            value = (short) val;
            break;
        case OP_REG0_SHIFT_COUNT: // shift count from R0
            value = (short) block.cpu.getRegister(0);
            value &= 31;
            break;
        
        case OP_PCREL:   // jump target
            value = ea;
            break;
        }

        return value;
    }

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#convertToImmedate()
	 */
	public void convertToImmedate() {
		if (type == OP_IMM || type == OP_OFFS || type == OP_PCREL)	// hack
			return;
		Check.checkState((type == OP_REG));
		type = OP_IMM;
		encoding = OP_ENC_UNSET;
		immed = (short) val;
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
		result = prime * result + encoding;
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
		MachineOperandMFP201 other = (MachineOperandMFP201) obj;
		int rtype = type;
		int rotype = other.type;
		if (rtype != rotype) {
			return false;
		}
		if ((type == OP_OFFS || type == OP_IMM || type == OP_PCREL) && immed != other.immed) {
			return false;
		}
		if (type != OP_IMM && type != OP_PCREL && val != other.val) {
			return false;
		}

		return true;
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
			if (type == OP_PCREL) {
				theVal = (short) (symbol.getAddr() - inst.getPc());
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

	public static MachineOperandMFP201 createImmediate(int i) {
		MachineOperandMFP201 op = new MachineOperandMFP201(OP_IMM);
		op.immed = (short) (op.val = i);
		return op;
	}

	public static MachineOperandMFP201 createGeneralOperand(int type, int val) {
		MachineOperandMFP201 op = new MachineOperandMFP201(type);
		op.val = val;
		return op;
	}

	public static MachineOperandMFP201 createGeneralOperand(int type, int val, int immed) {
		MachineOperandMFP201 op = new MachineOperandMFP201(type);
		op.val = val;
		op.immed = (short) immed;
		return op;
	}

	public static MachineOperand createSymbolImmediate(Symbol symbol) {
		MachineOperandMFP201 op = new MachineOperandMFP201(OP_IMM);
		if (symbol.isDefined()) {
			op.immed = (short) (op.val = symbol.getAddr());
			op.symbolResolved = true;
		}
		op.symbol = symbol;
		return op;
	}

	public static MachineOperandMFP201 createEmptyOperand() {
		return new MachineOperandMFP201(OP_NONE);
	}

	public static MachineOperand createScaledRegOffsOperand(int offset,
			int addReg, int register, int scale) {
		MachineOperandMFP201 op = new MachineOperandMFP201(OP_SRO);
		op.val = addReg;
		op.immed = (short) offset;
		op.scaleReg = register;
		int scaleBits = 0;
		while ((1 << scaleBits) != scale && scaleBits < 8)
			scaleBits++;
		if (scaleBits == 8)
			throw new IllegalArgumentException();
		op.scaleBits = scale;
		return op;
	}

}
