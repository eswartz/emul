/*
 * (c) Ed Swartz, 2010
 */
package v9t9.engine.cpu;

import org.ejs.coffee.core.utils.Check;

import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.Symbol;

/**
 * A machine operand, as parsed from an instruction or converted from an
 * assembler.
 * 
 * This is either an inst code or a placeholder for an immediate.
 * 
 * @author ejs
 */
public class MachineOperandF99 extends BaseMachineOperand {
    // Operand Type
	

	/** signed immediate */
	public static final int OP_IMM = 1;
	public static final int OP_ADDR = 2;
	public static final int OP_PCREL = 3;

	public int encoding;
	public int cycles;
	
	public static final int OP_ENC_UNSET = 0;
	public static final int OP_ENC_IMM6 = 1;
	public static final int OP_ENC_IMM3 = 2;
	
	public static final int OP_ENC_IMM15S1 = 3;
	public static final int OP_ENC_IMM16 = 4;
	

	/** 
     * Create an empty operand
     * @param type
     */
    public MachineOperandF99(int type, int enc) {
        this.type = type;
        this.encoding = enc;
    }
    public MachineOperandF99(int type) {
    	this.type = type;
    	this.encoding = OP_ENC_UNSET;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isMemory()
	 */
    public boolean isMemory() {
        return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegisterReference()
	 */
    public boolean isRegisterReference() {
        return false;        
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
        return false;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isRegister(int)
	 */
    public boolean isRegister(int reg) {
        return false;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isConstant()
	 */
    public boolean isConstant() {
        return true;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#hasImmediate()
	 */
	public boolean hasImmediate() {
	    return true
	    ;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#isLabel()
	 */
    public boolean isLabel() {
        return type == OP_ADDR || type == OP_PCREL;
    }
    
    /**
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
    	case OP_IMM: {
    		return "#>" + Integer.toHexString(val).toUpperCase() + " (" + val + ")";
    	}
    	case OP_ADDR:
    	case OP_PCREL:
    	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();

    	case OP_NONE:
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
	    case OP_ADDR:
	    case OP_PCREL:
	    	addr += 2;
	    	break;
	    }
	
	    return addr;
	}
	
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.MachineOperand#valueString(short, short)
	 */
    public String valueString(short ea, short theValue) {
       if (type == OP_NONE) {
			return null;
		}
		//if (byteop) {
		//	theValue &= 0xff;
		//}
		return Integer.toHexString(theValue & 0xffff).toUpperCase() + "(@"
				+ Integer.toHexString(ea & 0xffff).toUpperCase() + ")";
	}

    public short getEA(InstructionWorkBlock block) {
        short ea = 0;
    	switch (type) {
    	case OP_ADDR:
    	case OP_PCREL:
    		ea = immed;
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
        case OP_IMM:
            value = immed;
            break;
        case OP_ADDR:
            value = (short) val;
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
		if (type == OP_IMM)	// hack
			return;
		Check.checkState(type == OP_PCREL);
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
		MachineOperandF99 other = (MachineOperandF99) obj;
		int rtype = type;
		int rotype = other.type;
		if (rtype != rotype) {
			return false;
		}
		if ((type == OP_ADDR || type == OP_IMM || type == OP_PCREL) && immed != other.immed) {
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

	public static MachineOperandF99 createImmediateOperand(int i, int enc) {
		MachineOperandF99 op = new MachineOperandF99(OP_IMM);
		op.encoding = enc;
		op.immed = (short) (op.val = i);
		return op;
	}
	public static MachineOperandF99 createImmediate(int i) {
		return createImmediateOperand(i, OP_IMM);
	}

	public static MachineOperandF99 createSymbolImmediate(Symbol symbol) {
		MachineOperandF99 op = new MachineOperandF99(OP_IMM);
		if (symbol.isDefined()) {
			op.immed = (short) (op.val = symbol.getAddr());
			op.symbolResolved = true;
		}
		op.symbol = symbol;
		return op;
	}

	public static MachineOperandF99 createEmptyOperand() {
		return new MachineOperandF99(OP_NONE);
	}

	public static MachineOperandF99 createPCRelativeOperand(int immed) {
		return createImmediateOperand(OP_PCREL, immed);
	}

	public static MachineOperandF99 createAddrOperand(int immed) {
		return createImmediateOperand(OP_ADDR, immed);
	}
	
}
