/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.cpu;

import org.apache.bcel.generic.*;

import v9t9.MemoryDomain;

/**
 * @author ejs
 */
public class Operand {
    // Operand Type
    
    public static final int OP_NONE = -1;

    //  from ts/td field of opcode, don't change order
    public static final int OP_REG = 0; // register Rx

    public static final int OP_IND = 1; // indirect *Rx

    public static final int OP_ADDR = 2; // address @>xxxx

    public static final int OP_INC = 3; // register increment *Rx+

    public static final int OP_IMMED = 4; // immediate >xxxx

    public static final int OP_CNT = 5; // shift count x (4 bits)

    public static final int OP_JUMP = 6; // jump target >xxxx

    public static final int OP_OFFS_R12 = 7; // offset >xxxx or .xxxx

    public static final int OP_STATUS = 8; // status word >xxxx

    public static final int OP_INST = 9; // instruction for X
    
    public static final int OP_REG0_SHIFT_COUNT = 10;	// shift count from R0
    
    // Operand changes
    public static final int OP_DEST_FALSE = 0;
    public static final int OP_DEST_TRUE = 1;
    public static final int OP_DEST_KILLED = 2;
    
    public int type = OP_NONE;	// type of operand
    public int val = 0;	// value in opcode
    public short immed = 0;	// immediate word
    //public short ea;	// effective address of operand
    public boolean byteop = false; // for OP_REG...OP_INC, byte access
    public int dest = OP_DEST_FALSE;	// operand changes (OP_DEST_xxx)
    public boolean bIsAddr = false; // operand is an address?
    public int size = 0;	// size of operand (outside instruction) in bytes

    public int cycles = 0;	// memory cycles needed to read
//    public short value;	// actual value of operand

    public boolean isMemory() {
        return type == OP_IND || type == OP_ADDR || type == OP_INC || !bIsAddr;
    }
    
    public boolean isRegister() {
        return type == OP_REG || type == OP_IND || type == OP_INC 
        	|| (type == OP_ADDR && val != 0) || (type == OP_REG0_SHIFT_COUNT);        
    }
    
    public boolean isConstant() {
        return type == OP_IMMED || type == OP_CNT || (type == OP_ADDR && val == 0); 
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
    	case Operand.OP_ADDR:	// @>xxxx or @>xxxx(Rx)
    		immed = domain.readWord(addr); 
    		this.cycles += 8 + Instruction.getMemoryCycles(addr);
    		addr += 2;
    		size = 2;
    		break;
    	case Operand.OP_IMMED:	// immediate
    		immed = domain.readWord(addr);
    		//ea = addr;
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
       if (type == OP_NONE)
            return null;
       if (byteop)
           theValue &= 0xff;
        return Integer.toHexString(theValue & 0xffff).toUpperCase()
        	+"(@"+Integer.toHexString(ea & 0xffff).toUpperCase()+")"; 
    }

    /**
     * @return
     */
    public short getEA(MemoryDomain domain, short pc, short wp) {
        short ea = 0;
    	switch (type)
    	{
    	case Operand.OP_NONE:
    		break;
    	case Operand.OP_REG:	// Rx
    		ea = (short) ((val<<1) + wp);
    	this.cycles += 0 * 4;
    		break;
    	case Operand.OP_INC:	// *Rx+
    	case Operand.OP_IND: {	// *Rx
    		short ad = (short)((val<<1) + wp);
    		ea = domain.readWord(ad);

    		/* update register if necessary */
    		if (type == Operand.OP_INC) {
    		    this.cycles += byteop ? 2 : 4;
    		    domain.writeWord(ad, (short)(ea + (byteop ? 1 : 2)));
    		}
    		break;
    	}
    	case Operand.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    	    short ad;
    		ea = immed; 
    		if (val != 0) {
    			ad = (short)((val<<1) + wp);
    			ea += domain.readWord(ad);
    			this.cycles += Instruction.getMemoryCycles(ad);
    		}
    		break;
    	}
    	case Operand.OP_IMMED:	// immediate
    		break;
    	case Operand.OP_CNT:	// shift count
    		break;
    	case Operand.OP_OFFS_R12:	// offset from R12
    		ea = (short) ((12<<1) + wp);
    		break;
    	case Operand.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ea = wp;
		    break;
    	
    	case Operand.OP_JUMP:	// jump target
    		ea = (short)(val + pc);
    		break;
    	case Operand.OP_STATUS:	// status word
    		break;
    	case Operand.OP_INST:
    		break;		
    	}
    	return ea;
    }

    boolean hasConstAddr(v9t9.cpu.CompileInfo info) {
	    return (type == Operand.OP_ADDR && val == 0 
	            && !bIsAddr 
	            && info.memory.CPU.hasRomAccess(immed)
	            && !info.memory.CPU.hasRamAccess(immed));
    }
    
    /**
     * @return true: has an EA 
     */
    public boolean compileGetEA(int eaIndex, v9t9.cpu.CompileInfo info, short pc) {
        InstructionList ilist = info.ilist;
    	switch (type)
    	{
    	case Operand.OP_REG:	// Rx
    		// (short) ((val<<1) + wp);
    	    Compiler.compileGetRegEA(info, val);
    		this.cycles += 0 * 4;
    		break;
    	case Operand.OP_INC:	// *Rx+
    	case Operand.OP_IND: {	// *Rx
    		//short ad = (short)((val<<1) + wp);
    	    Compiler.compileGetRegEA(info, val);
    	    
    		// &Rxx
    		if (type == Operand.OP_INC) {
        		/* postincrement register */
       		    this.cycles += byteop ? 2 : 4;
       		    
    		    ilist.append(new DUP());	// &Rxx, &Rxx
        		Compiler.compileReadWord(info, ilist); // &Rxx, regval 
    		    ilist.append(new DUP_X1());	// regval, &Rxx, regval
    		    ilist.append(new PUSH(info.pgen, byteop ? 1 : 2));
        		ilist.append(new IADD());
        		ilist.append(new I2S());	// regval, &Rxx, regval+K
       		    Compiler.compileWriteWord(info, ilist);	// regval
    		}
    		else {
    		    Compiler.compileReadWord(info, ilist); // regval
    		}
    		break;
    	}
    	case Operand.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    	    if (hasConstAddr(info))
    	        return false;
    	    ilist.append(new PUSH(info.pgen, immed));
    	    if (val != 0) {
    	        //ad = (short)((val<<1) + wp);
        	    Compiler.compileGetRegEA(info, val);
        	    ////ilist.append(new DUP_X1());	// &Rxx, immed, &Rxx
        	    Compiler.compileReadWord(info, ilist);	// &Rxx, immed, regval
        	    ilist.append(new IADD());
        	    ilist.append(new I2S());	// &Rxx, regval+immed
    			////this.cycles += Instruction.getMemoryCycles(ad);
    		}
    		break;
    	}
    	case Operand.OP_OFFS_R12:	// offset from R12
    	    Compiler.compileGetRegEA(info, 12);
    		break;
    	case Operand.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    Compiler.compileGetRegEA(info, 0);
		    break;
    	
    	case Operand.OP_JUMP:	// jump target
    	    ilist.append(new PUSH(info.pgen, (short)(val + pc)));
    		break;
    	case Operand.OP_NONE:
    	case Operand.OP_IMMED:	// immediate
    	case Operand.OP_CNT:	// shift count
    	case Operand.OP_STATUS:	// status word
    	case Operand.OP_INST:
    	default:
    	    return false;
    		//ilist.append(new PUSH(info.pgen, 0));
    	}
    	ilist.append(new ISTORE(eaIndex));
    	return true;
    }

    /**
     * @param memory
     * @return
     */
    public short getValue(MemoryDomain domain, short ea) {
        short value = 0;

    	switch (type)
    	{
    	case Operand.OP_NONE:
    		break;
    	case Operand.OP_REG:	// Rx
    		if (bIsAddr)
    		    value = ea;
    		else
    		    if (byteop)
    		        value = (short) domain.readByte(ea);
    		    else
    		        value = (short) domain.readWord(ea);
    		break;
    	case Operand.OP_INC:	// *Rx+
    	case Operand.OP_IND: {	// *Rx
    		if (bIsAddr)
    		    value = ea;
    		else
    		    if (byteop)
    		        value = (short) domain.readByte(ea);
    		    else
    		        value = (short) domain.readWord(ea);
    		break;
    	}
    	case Operand.OP_ADDR: {	// @>xxxx or @>xxxx(Rx)
    		if (bIsAddr)
    		    value = ea;
    		else
    		    if (byteop)
    		        value = (short) domain.readByte(ea);
    		    else
    		        value = (short) domain.readWord(ea);
    		break;
    	}
    	case Operand.OP_IMMED:	// immediate
    		value = immed;
    		break;
    	case Operand.OP_CNT:	// shift count
   	        value = (short) val;
    		break;
    	case Operand.OP_OFFS_R12:	// offset from R12
   		    value = (short) (domain.readWord(ea) + val);
    		break;
    	case Operand.OP_REG0_SHIFT_COUNT: // shift count from R0
		    value = (short) (domain.readWord(ea) & 0xf);
		    if (value == 0)
		        value = 16;
		    break;
    	
    	case Operand.OP_JUMP:	// jump target
    		value = ea;
    		break;
    	case Operand.OP_STATUS:	// status word
    	    //TODO: NOTHING -- make sure we don't depend on this   
    		break;
    	case Operand.OP_INST:
    	    value = domain.readWord(ea);
    		break;		
    	}

        return value;
    }

    /**
     * @param memory
     * @return true: has value
     */
    public boolean compileGetValue(int valIndex, int eaIndex, CompileInfo info) {
        InstructionList ilist = info.ilist;
    	switch (type)
    	{
    	case Operand.OP_REG:	// Rx
    	case Operand.OP_INC:	// *Rx+
    	case Operand.OP_IND: 	// *Rx
    	case Operand.OP_ADDR: 	// @>xxxx or @>xxxx(Rx)
    	    if (hasConstAddr(info)) {
    	        if (byteop)
    	            Compiler.compileReadAbsByte(info, ilist, (short)immed);
    	        else
    	            Compiler.compileReadAbsWord(info, ilist, (short)immed);
    		} else {
    		    ilist.append(new ILOAD(eaIndex));
    		    if (!bIsAddr) {
    		        if (byteop)
    		            Compiler.compileReadByte(info, ilist);
    		        else
    		            Compiler.compileReadWord(info, ilist);
    		    }
    		}
    		break;
    	case Operand.OP_IMMED:	// immediate
    	    ilist.append(new PUSH(info.pgen, immed));
    		break;
    	case Operand.OP_CNT:	// shift count
    	    ilist.append(new PUSH(info.pgen, val));
    		break;
    	case Operand.OP_OFFS_R12:	// offset from R12
    	    ilist.append(new ILOAD(eaIndex));
    		Compiler.compileReadWord(info, ilist);
    		if (val != 0) {
    		    ilist.append(new PUSH(info.pgen, val));
    		    ilist.append(new IADD());
    		    ilist.append(new I2S());
    		}
    		break;
    	case Operand.OP_REG0_SHIFT_COUNT: // shift count from R0
    	    ilist.append(new ILOAD(eaIndex));
    		Compiler.compileReadWord(info, ilist);
    		ilist.append(new PUSH(info.pgen, 0xf));
    		ilist.append(new IAND());
    		ilist.append(new I2S());
    		
    		// if value==0 value=16
    		InstructionList skip = new InstructionList();
    		InstructionHandle skipInst = skip.append(new NOP());
    		ilist.append(new DUP());	// value
    		ilist.append(new IFNE(skipInst));
    		ilist.append(new POP());
    		ilist.append(new PUSH(info.pgen, 16));
    		
    		ilist.append(skip);
    	    break;
    	
    	case Operand.OP_JUMP:	// jump target
    	    ilist.append(new ILOAD(eaIndex));
    		break;
    	case Operand.OP_INST:
    	    ilist.append(new ILOAD(eaIndex));
    		Compiler.compileReadWord(info, ilist);
    		break;		
    	case Operand.OP_NONE:
    	case Operand.OP_STATUS:	// status word
    	    //TODO: NOTHING -- make sure we don't depend on this
    	default:
    	    //ilist.append(new PUSH(info.pgen, 0));
    	    return false;
    	}
    	ilist.append(new ISTORE(valIndex));
    	return true;
    }

}
