/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.memory.MemoryDomain;
import v9t9.utils.Check;

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
    public static final int OP_IMMED = 4; // immediate >xxxx

    public static final int OP_CNT = 5; // shift count x (4 bits)

    public static final int OP_JUMP = 6; // jump target >xxxx

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
    public int size = 0;	// size of operand (outside instruction) in bytes

    public int cycles = 0;	// memory cycles needed to read

    /** 
     * Create an empty operand (to be filled in piecewise)
     * @param type
     */
    public MachineOperand(int type) {
        this.type = type;
    }

    public final static String OPT_R = "(?:R|r)?";
    public final static String REG_NUM = "([0-9]|(?:1[0-5]))";
    public final static String IMMED = "((?:>?)(?:(?:[+|-])?)[0-9A-Fa-f]+)";
    final static Pattern OPERAND_PATTERN = Pattern.compile(
            //       1                2        3
            "(?:(\\*?)" + OPT_R + REG_NUM + "(\\+?))|" +
            //         4          5             6 
            "(?:@" + IMMED + "(\\(" + OPT_R + REG_NUM + "\\))?)|" +
            //         7
            "(?:" + IMMED + ")"
            );
    
    final static Pattern JUMP_PATTERN = Pattern.compile(
            //       1       2
            "\\$([+-])" + IMMED
            );
    /** 
     * Parse a string to construct an operand
     * @param string
     */
    public MachineOperand(String string) {
        if (string == null || string.length() == 0) {
            type = OP_NONE;
            return;
        }
        Matcher matcher = OPERAND_PATTERN.matcher(string);
        if (matcher.matches()) {
            if (matcher.group(2) != null) {
                val = Integer.parseInt(matcher.group(2));
                if (matcher.group(1).length() > 0) {
                    if (matcher.group(3) != null && matcher.group(3).length() > 0) {
                        // *R0+
                        type = OP_INC;
                    } else {
                        // *R0
                        type = OP_IND;
                    }
                } else {
                    // R9
                    type = OP_REG;
                    Check.checkArg(matcher.group(3) == null || matcher.group(3).length() == 0);
                }
            } else if (matcher.group(4) != null) {
                immed = parseImmed(matcher.group(4));
                type = OP_ADDR;
                if (matcher.group(5) != null && matcher.group(5).length() > 0) {
                    // @>4(r5)
                    val = Integer.parseInt(matcher.group(6));
                    Check.checkArg(val != 0);
                } else {
                    // @>5
                    val = 0;
                }
            } else {
                // immed
                type = OP_IMMED;
                val = immed = parseImmed(matcher.group(7));
            }
        } else {
        	matcher = JUMP_PATTERN.matcher(string);
        	if (matcher.matches()) {
        		int op = parseImmed(matcher.group(2));
        		if (matcher.group(1) != null && matcher.group(1).equals("-"))
        			op = -op;
        		type = OP_JUMP;
        		val = op;
        	} else {
        		Check.checkArg(false);
        	}
        }
    }

    private short parseImmed(String string) {
        int radix = 10;
        if (string.charAt(0) == '>') {
            radix = 16;
            string = string.substring(1);
        }
        return (short) Integer.parseInt(string, radix);
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
    
        	case OP_OFFS_R12:
        	    return ">" + ((val & 0x8000) != 0 ? "-" : "") + ((val & 0x8000) != 0 ? -val : val); 
    
        	case OP_REG0_SHIFT_COUNT:
        	    return ">" + Integer.toHexString(val & 0xffff).toUpperCase();
        	    
        	case OP_JUMP:
        	    return "$+>" + Integer.toHexString(val & 0xffff).toUpperCase();
    
        	case OP_STATUS:		// not real operands
        	case OP_INST:		
        	default:
        		return null;
        	}
    }

    /**
     * Read any extra immediates for an operand from the instruction stream.
     * Fills in Operand.size and Operand.immed, sets Operand.cycles for reads.
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
        	switch (type)
        	{
        	case MachineOperand.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
        		immed = domain.readWord(addr); 
        		this.cycles += 8 + Instruction.getMemoryCycles(addr);
        		addr += 2;
        		size = 2;
        		break;
        	case MachineOperand.OP_IMMED:	// immediate
        		immed = domain.readWord(addr);
        		//ea = addr;
        		this.cycles += Instruction.getMemoryCycles(addr);
        		addr += 2;
        		size = 2;
        		break;
        	}
       
        return addr;
    }

    public boolean hasImmediate() {
        return type == OP_ADDR || type == OP_IMMED; 
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
            this.cycles += 8 + Instruction.getMemoryCycles(addr);
            addr += 2;
            size = 2;
            break;
        case MachineOperand.OP_IMMED:   // immediate
            this.cycles += Instruction.getMemoryCycles(addr);
            addr += 2;
            size = 2;
            break;
        }
       
        return addr;
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
     * @return
     */
    public short getEA(MemoryDomain domain, int pc, short wp) {
        short ea = 0;
        	switch (type)
        	{
        	case MachineOperand.OP_NONE:
        		break;
        	case MachineOperand.OP_REG:	// Rx
        		ea = (short) ((val<<1) + wp);
        	this.cycles += 0 * 4;
        		break;
        	case MachineOperand.OP_INC:	// *Rx+
        	case MachineOperand.OP_IND: {	// *Rx
        		short ad = (short)((val<<1) + wp);
        		ea = domain.readWord(ad);
    
        		/* update register if necessary */
        		if (type == MachineOperand.OP_INC) {
        		    this.cycles += byteop ? 2 : 4;
        		    domain.writeWord(ad, (short)(ea + (byteop ? 1 : 2)));
        		}
        		break;
        	}
        	case MachineOperand.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
        	    short ad;
        		ea = immed; 
        		if (val != 0) {
        			ad = (short)((val<<1) + wp);
        			ea += domain.readWord(ad);
        			this.cycles += Instruction.getMemoryCycles(ad);
        		}
        		break;
        	}
        	case MachineOperand.OP_IMMED:	// immediate
        		break;
        	case MachineOperand.OP_CNT:	// shift count
        		break;
        	case MachineOperand.OP_OFFS_R12:	// offset from R12
        		ea = (short) ((12<<1) + wp);
        		break;
        	case MachineOperand.OP_REG0_SHIFT_COUNT: // shift count from R0
        	    ea = wp;
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

        switch (type)
        {
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
		if (type == OP_IMMED)
			return;
		Check.checkState(type == MachineOperand.OP_REG);
		type = OP_IMMED;
		immed = (short) val;
	}


}
