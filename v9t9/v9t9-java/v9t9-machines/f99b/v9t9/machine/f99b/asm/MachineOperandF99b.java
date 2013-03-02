/*
  MachineOperandF99b.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.f99b.asm;

import static v9t9.machine.f99b.asm.InstF99b.ctxStrings;
import static v9t9.machine.f99b.asm.InstF99b.syscallStrings;
import ejs.base.utils.Check;
import ejs.base.utils.HexUtils;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.InstructionWorkBlock;

/**
 * A machine operand, as parsed from an instruction or converted from an
 * assembler.
 * 
 * This is either an inst code or a placeholder for an immediate.
 * 
 * @author ejs
 */
public class MachineOperandF99b extends BaseMachineOperand {
    // Operand Type
	

	/** signed immediate */
	public static final int OP_IMM = 1;
	public static final int OP_ADDR = 2;
	public static final int OP_PCREL = 3;

	public int encoding;
	public int cycles;
	
	public static final int OP_ENC_UNSET = 0;
	public static final int OP_ENC_IMM4 = 1;
	public static final int OP_ENC_IMM8 = 2;
	public static final int OP_ENC_IMM15S1 = 3;
	public static final int OP_ENC_IMM16 = 4;
	public static final int OP_ENC_IMM32 = 5;
	public static final int OP_ENC_CTX = 6;
	public static final int OP_ENC_SYSCALL = 7;


	/** 
     * Create an empty operand
     * @param type
     */
    public MachineOperandF99b(int type, int enc) {
        this.type = type;
        this.encoding = enc;
    }
    public MachineOperandF99b(int type) {
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
    	return basic;
    }

	private String basicString() {
		switch (type) 
    	{
    	case OP_IMM: {
    		if (encoding == OP_ENC_IMM32)
    			return "#>" + Integer.toHexString(val) + " (" + val + ")";
    		else if (encoding == OP_ENC_IMM15S1)
    			return "#>" + HexUtils.toHex4(val);
    		else if (encoding == OP_ENC_IMM8)
    			return "#>" + HexUtils.toHex4((byte)val) + " (" + val + ")";
    		else if (encoding == OP_ENC_CTX)
    			return (val >= 0 && val < ctxStrings.length ? ctxStrings[val] : "") + " (" + val + ")";
    		else if (encoding == OP_ENC_SYSCALL)
    			return (val >= 0 && val < syscallStrings.length ? syscallStrings[val] : "") + " (" + val + ")";
    		else
    			return "#>" + HexUtils.toHex4(val) + " (" + val + ")";
    	}
    	case OP_ADDR:
    	case OP_PCREL:
    	    return "$+>" + HexUtils.toHex4(val);

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
	    case OP_IMM:
	    	if (encoding == OP_ENC_IMM15S1 || encoding == OP_ENC_IMM16)
	    		addr += 2;
	    	else if (encoding == OP_ENC_IMM8)
	    		addr++;
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
        case IMachineOperand.OP_NONE:
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
	public IOperand resolve(RawInstruction inst) {
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
		MachineOperandF99b other = (MachineOperandF99b) obj;
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

	public static MachineOperandF99b createImmediateOperand(int i, int enc) {
		MachineOperandF99b op = new MachineOperandF99b(OP_IMM);
		op.encoding = enc;
		op.immed = (short) (op.val = i);
		return op;
	}
	public static MachineOperandF99b createImmediate(int i) {
		return createImmediateOperand(i, OP_IMM);
	}

	public static MachineOperandF99b createEmptyOperand() {
		return new MachineOperandF99b(OP_NONE);
	}

	public static MachineOperandF99b createPCRelativeOperand(int immed) {
		return createImmediateOperand(OP_PCREL, immed);
	}

	public static MachineOperandF99b createAddrOperand(int immed) {
		return createImmediateOperand(OP_ADDR, immed);
	}
	
}
