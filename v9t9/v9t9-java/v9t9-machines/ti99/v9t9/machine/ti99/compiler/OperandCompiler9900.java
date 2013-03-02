/*
  OperandCompiler9900.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.compiler;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.I2S;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.IAND;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.engine.compiler.CompileInfo;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.Instruction9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

public class OperandCompiler9900 {

    static boolean hasConstAddr(BaseMachineOperand op, CompileInfo info) {
        return op.type == MachineOperand9900.OP_ADDR && op.val == 0 
                && !op.bIsReference 
                && info.memory.hasRomAccess(op.immed)
                && !info.memory.hasRamAccess(op.immed)
                && info.memory.isStatic(op.immed);
    }
    
    /**
     * @return true: has an EA 
     */
    public static boolean compileGetEA(MachineOperand9900 op, int eaIndex, CompileInfo info, Instruction9900 ins, int pc) {
        InstructionList ilist = info.ilist;
        switch (op.type)
        {
        case MachineOperand9900.OP_REG:    // Rx
            op.cycles += 0 * 4;
            // (short) ((val<<1) + wp);
            if (!(ins.getInst() == Inst9900.Impy /*&& ins.op2 == this*/)
                    && !(ins.getInst() == Inst9900.Idiv /*&& ins.op2 == this*/)
                    && info.optimizeRegAccess
                    && false) {
				return false;
			}
            // slow mode: treat this as normal memory access
            OperandCompiler9900.compileGetRegEA(info, op.val);
            break;
        case MachineOperand9900.OP_IND: {  // *Rx
            //short ad = (short)((val<<1) + wp);
            if (info.optimizeRegAccess) {
                OperandCompiler9900.compileReadRegWord(info, ilist, op.val);
            } else {
                OperandCompiler9900.compileGetRegEA(info, op.val);
                OperandCompiler9900.compileReadWord(info, ilist); // regval
            }
            break;
        }
        case MachineOperand9900.OP_INC: {   // *Rx+
            if (info.optimizeRegAccess) {

            	OperandCompiler9900.compileReadRegWordAndInc(info, ilist, op.val, op.byteop ? 1 : 2, false);
                
                // &Rxx
                /* postincrement register */
                op.cycles += op.byteop ? 2 : 4;

            } else {
                //short ad = (short)((val<<1) + wp);
                OperandCompiler9900.compileGetRegEA(info, op.val);
                
                // &Rxx
                /* postincrement register */
                op.cycles += op.byteop ? 2 : 4;
                
                ilist.append(new DUP());    // &Rxx, &Rxx
                OperandCompiler9900.compileReadWord(info, ilist); // &Rxx, regval 
                ilist.append(new DUP_X1()); // regval, &Rxx, regval
                ilist.append(new PUSH(info.pgen, op.byteop ? 1 : 2));
                ilist.append(new IADD());
                ilist.append(new I2S());    // regval, &Rxx, regval+K
                OperandCompiler9900.compileWriteWord(info, ilist); // regval
            }
            break;
        }
        case MachineOperand9900.OP_ADDR: { // @>xxxx or @>xxxx(Rx)
            if (hasConstAddr(op, info)) {
				return false;
			}
            ilist.append(new PUSH(info.pgen, op.immed));
            if (op.val != 0) {
                //ad = (short)((val<<1) + wp);
//              OperandCompiler.compileGetRegEA(info, val);
  //            Compiler.compileReadWord(info, ilist);  // &Rxx, immed, regval
                if (info.optimizeRegAccess) {
                	OperandCompiler9900.compileReadRegWord(info, ilist, op.val);
                } else {
                    OperandCompiler9900.compileGetRegEA(info, op.val);
                    OperandCompiler9900.compileReadWord(info, ilist); // regval
                }
                
                ilist.append(new IADD());
                ilist.append(new I2S());    // &Rxx, regval+immed
                ////this.cycles += Instruction.getMemoryCycles(ad);
            }
            break;
        }
        case MachineOperand9900.OP_OFFS_R12:   // offset from R12
            if (info.optimizeRegAccess) {
                return false;
            }
            OperandCompiler9900.compileGetRegEA(info, 12);
            break;
        case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
            if (info.optimizeRegAccess) {
                return false;
            }
            OperandCompiler9900.compileGetRegEA(info, 0);
            break;
        
        case MachineOperand9900.OP_JUMP:   // jump target
            ilist.append(new PUSH(info.pgen, (short)(op.val + pc)));
            break;
        case IMachineOperand.OP_NONE:
        case MachineOperand9900.OP_IMMED:  // immediate
        case MachineOperand9900.OP_CNT:    // shift count
        case MachineOperand9900.OP_STATUS: // status word
        case MachineOperand9900.OP_INST:
        default:
            return false;
            //ilist.append(new PUSH(info.pgen, 0));
        }
        ilist.append(new ISTORE(eaIndex));
        return true;
    }

    /**
     * @param memory
     * @return true: has value
     */
    public static boolean compileGetValue(MachineOperand9900 op, int valIndex, int eaIndex, CompileInfo info) {
        InstructionList ilist = info.ilist;
        switch (op.type)
        {
        case MachineOperand9900.OP_REG:    // Rx
            if (info.optimizeRegAccess) {
                // when optimizing, read directly from WP memory
                if (op.byteop) {
					OperandCompiler9900.compileReadRegByte(info, ilist, op.val, op.dest == IOperand.OP_DEST_TRUE);
				} else {
					OperandCompiler9900.compileReadRegWord(info, ilist, op.val, op.dest == IOperand.OP_DEST_TRUE);
				}
                break;
            }
            // fall through
            
        case MachineOperand9900.OP_INC:    // *Rx+
        case MachineOperand9900.OP_IND:    // *Rx
        case MachineOperand9900.OP_ADDR:   // @>xxxx or @>xxxx(Rx)
            if (hasConstAddr(op, info)) {
                if (op.byteop) {
                	OperandCompiler9900.compileReadAbsByte(info, ilist, op.immed);
				} else {
					OperandCompiler9900.compileReadAbsWord(info, ilist, op.immed);
				}
            } else {
                ilist.append(new ILOAD(eaIndex));
                if (!op.bIsReference) {
                    if (op.byteop) {
                    	OperandCompiler9900.compileReadByte(info, ilist);
					} else {
						OperandCompiler9900.compileReadWord(info, ilist);
					}
                }
            }
            break;
        case MachineOperand9900.OP_IMMED:  // immediate
            ilist.append(new PUSH(info.pgen, op.immed));
            break;
        case MachineOperand9900.OP_CNT:    // shift count
            ilist.append(new PUSH(info.pgen, op.val));
            break;
        case MachineOperand9900.OP_OFFS_R12:   // offset from R12
            if (info.optimizeRegAccess) {
                OperandCompiler9900.compileReadRegWord(info, ilist, 12);
            } else {
                ilist.append(new ILOAD(eaIndex));
                OperandCompiler9900.compileReadWord(info, ilist);
            }
            if (op.val != 0) {
                ilist.append(new PUSH(info.pgen, op.val));
                ilist.append(new IADD());
                ilist.append(new I2S());
            }
            break;
        case MachineOperand9900.OP_REG0_SHIFT_COUNT: // shift count from R0
            if (info.optimizeRegAccess) {
                OperandCompiler9900.compileReadRegWord(info, ilist, 0);
            } else {
                ilist.append(new ILOAD(eaIndex));
                OperandCompiler9900.compileReadWord(info, ilist);
            }
            ilist.append(new PUSH(info.pgen, 0xf));
            ilist.append(new IAND());
            ilist.append(new I2S());
            
            // if value==0 value=16
            InstructionList skip = new InstructionList();
            InstructionHandle skipInst = skip.append(new NOP());
            ilist.append(new DUP());    // value
            ilist.append(new IFNE(skipInst));
            ilist.append(new POP());
            ilist.append(new PUSH(info.pgen, 16));
            
            ilist.append(skip);
            break;
        
        case MachineOperand9900.OP_JUMP:   // jump target
            ilist.append(new ILOAD(eaIndex));
            break;
        case MachineOperand9900.OP_INST:
            ilist.append(new ILOAD(eaIndex));
            OperandCompiler9900.compileReadWord(info, ilist);
            break;      
        case IMachineOperand.OP_NONE:
        case MachineOperand9900.OP_STATUS: // status word
            //TODO: NOTHING -- make sure we don't depend on this
        default:
            //ilist.append(new PUSH(info.pgen, 0));
            return false;
        }
        ilist.append(new ISTORE(valIndex));
        return true;
    }

    public static void compilePutValue(MachineOperand9900 op, int valIndex, int eaIndex, CompileInfo info) {
        switch (op.type) {
        case MachineOperand9900.OP_REG:
            if (info.optimizeRegAccess) {
                // write directly to WP memory 
                info.ilist.append(new ILOAD(valIndex));
                if (op.byteop) {
					OperandCompiler9900.compileWriteRegByte(info, info.ilist, op.val, op.dest == IOperand.OP_DEST_TRUE);
				} else {
					OperandCompiler9900.compileWriteRegWord(info, info.ilist, op.val, op.dest == IOperand.OP_DEST_TRUE);
				}
                break;
            }
            // fall through
            
        default:
            info.ilist.append(new ILOAD(eaIndex));
            info.ilist.append(new ILOAD(valIndex));
            if (op.byteop) {
                info.ilist.append(InstructionConstants.I2B);
                OperandCompiler9900.compileWriteByte(info, info.ilist);
            } else {
                OperandCompiler9900.compileWriteWord(info, info.ilist);
            }
            break;
        }
    }

	/** Read a register */
	public static void compileGetRegEA(CompileInfo info, int reg) {
	    InstructionList ilist = info.ilist;
	    if (reg < 0 || reg > 15) {
			throw new AssertionError("bad register " + reg);
		}
	    ilist.append(new ILOAD(info.localWp));
	    if (reg != 0) {
	        ilist.append(new PUSH(info.pgen, reg * 2));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(InstructionConstants.I2S);
	    }
	}

	/**
	 * Read a word from the given constant address
	 */
	public static void compileReadAbsWord(CompileInfo info,
	        InstructionList ilist, short addr) {
	    ilist.append(new PUSH(info.pgen, info.memory.flatReadWord(addr)));
	}

	/**
	 * Read a byte from the given constant address
	 */
	public static void compileReadAbsByte(CompileInfo info,
	        InstructionList ilist, short addr) {
	    ilist.append(new PUSH(info.pgen, info.memory.flatReadByte(addr)));
	}

	private static void compileGetRegAddress(CompileInfo info,
	        InstructionList ilist, int val) {
	    ilist.append(new ILOAD(info.localWpOffset));
	    if (val != 0) {
	        ilist.append(new PUSH(info.pgen, val)); // short[] index
	        ilist.append(InstructionConstants.IADD);
	    }
	}

	public static void compileReadRegByte(CompileInfo info,
	        InstructionList ilist, int val) {
	    compileReadRegByte(info, ilist, val, false);
	}

	/**
	 * Read a byte from the given register
	 */
	public static void compileReadRegByte(CompileInfo info,
	        InstructionList ilist, int val, boolean saveAddr) {
	    ilist.append(new ALOAD(info.localWpWordMemory));
	    compileGetRegAddress(info, ilist, val);
	    if (saveAddr) {
			ilist.append(InstructionConstants.DUP2);
		}
	    ilist.append(InstructionFactory.createArrayLoad(Type.SHORT));
	    ilist.append(new PUSH(info.pgen, 8));
	    ilist.append(InstructionConstants.ISHR);
	    ilist.append(InstructionConstants.I2S);
	}

	public static void compileReadRegWord(CompileInfo info,
	        InstructionList ilist, int val) {
	    compileReadRegWord(info, ilist, val, false);
	}

	/**
	 * Read a word from the given register
	 * 
	 * if saveAddr is true, stack is: wp[] woffs wreg
	 */
	public static void compileReadRegWord(CompileInfo info,
	        InstructionList ilist, int val, boolean saveAddr) {
	    ilist.append(new ALOAD(info.localWpWordMemory));
	    compileGetRegAddress(info, ilist, val);
	    if (saveAddr) {
			ilist.append(InstructionConstants.DUP2);
		}
	    ilist.append(InstructionFactory.createArrayLoad(Type.SHORT));
	}

	public static void compileReadRegWordAndInc(CompileInfo info,
	        InstructionList ilist, int val, int incBy, boolean saveAddr) {
	    ilist.append(new ALOAD(info.localWpWordMemory));
	    // stack: wp[]
	    compileGetRegAddress(info, ilist, val);
	    // stack: wp[] woffs
	    if (saveAddr) {
			ilist.append(InstructionConstants.DUP2);
		}
	
	    ilist.append(InstructionConstants.DUP2);
	    // stack: wp[] woffs wp[] woffs
	    ilist.append(InstructionFactory.createArrayLoad(Type.SHORT));
	    // stack: wp[] woffs wreg
	    ilist.append(InstructionConstants.DUP_X2);
	    // stack: wreg wp[] woffs wreg
	    ilist.append(new PUSH(info.pgen, incBy));
	    ilist.append(InstructionConstants.IADD);
	    // ilist.append(InstructionConstants.I2S);
	    // stack: wreg wp[] woffs wreg+2
	    ilist.append(InstructionFactory.createArrayStore(Type.SHORT));
	    // stack: wreg
	}

	/**
	 * Write a byte to the given register
	 */
	public static void compileWriteRegByte(CompileInfo info,
	        InstructionList ilist, int val, boolean savedAddr) {
	    // move value to high byte
	    ilist.append(new PUSH(info.pgen, 8));
	    ilist.append(InstructionConstants.ISHL);
	
	    if (savedAddr) {
	        // stack: wp[], woffs, val<<8
	
	        // shove val off
	        ilist.append(new ISTORE(info.localTemp));
	
	        ilist.append(InstructionConstants.DUP2);
	        // stack: wp[], woffs, wp[], woffs
	
	        // read whole reg
	        ilist.append(InstructionFactory.createArrayLoad(Type.SHORT));
	        // stack: wp[], woffs, wreg
	
	        // mask and replace
	        ilist.append(new PUSH(info.pgen, 0xff));
	        ilist.append(InstructionConstants.IAND);
	
	        ilist.append(new ILOAD(info.localTemp));
	        // stack: wp[], woffs, wreg, val<<8
	
	        ilist.append(InstructionConstants.IOR);
	        // stack: wp[], woffs, wreg'
	
	        // store back
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(InstructionFactory.createArrayStore(Type.SHORT));
	
	    } else {
	        // stack: val<<8
	
	        ilist.append(new ALOAD(info.localWpWordMemory));
	        // stack: val<<8, wp[]
	        ilist.append(InstructionConstants.DUP_X1);
	        // stack: wp[], val<<8, wp[]
	
	        // get reg offset
	        compileGetRegAddress(info, ilist, val);
	        // stack: wp[], val<<8, wp[], woffs
	        ilist.append(InstructionConstants.DUP_X2);
	        // stack: wp[], woffs, val<<8, wp[], woffs
	
	        // read whole reg
	        ilist.append(InstructionFactory.createArrayLoad(Type.SHORT));
	        // stack: wp[], woffs, val<<8, wreg
	
	        // mask and replace
	        ilist.append(new PUSH(info.pgen, 0xff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.IOR);
	        // stack: wp[], woffs, wreg'
	
	        // store back
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(InstructionFactory.createArrayStore(Type.SHORT));
	    }
	
	}

	/**
	 * Write a word to the given register
	 */
	public static void compileWriteRegWord(CompileInfo info,
	        InstructionList ilist, int val) {
	    compileWriteRegWord(info, ilist, val, false);
	}

	public static void compileWriteRegWord(CompileInfo info,
	        InstructionList ilist, int val, boolean savedAddr) {
	
	    if (savedAddr) {
	        // stack: wp[], woffs, val
	        ilist.append(InstructionFactory.createArrayStore(Type.SHORT));
	    } else {
	
	        // stack: val
	        // get reg offset
	        ilist.append(new ALOAD(info.localWpWordMemory));
	        // stack: val, wp[]
	        ilist.append(InstructionConstants.SWAP);
	        // stack: wp[], val
	        compileGetRegAddress(info, ilist, val);
	        // stack: wp[], val, woffs
	        ilist.append(InstructionConstants.SWAP);
	        // stack: wp[], woffs, val
	        ilist.append(InstructionFactory.createArrayStore(Type.SHORT));
	    }
	}

	/**
	 * Read a word from the address on the stack.
	 */
	public static void compileReadWord(CompileInfo info, InstructionList ilist) {
	    ilist.append(new ALOAD(info.localMemory));
	    ilist.append(InstructionConstants.SWAP);
	    ilist.append(info.ifact.createInvoke(v9t9.common.memory.IMemoryDomain.class
	            .getName(), "readWord", Type.SHORT, new Type[] { Type.INT },
	            Constants.INVOKEINTERFACE));
	}

	/**
	 * Read a byte from the address on the stack.
	 */
	public static void compileReadByte(CompileInfo info, InstructionList ilist) {
	    ilist.append(new ALOAD(info.localMemory));
	    ilist.append(InstructionConstants.SWAP);
	    ilist.append(info.ifact.createInvoke(v9t9.common.memory.IMemoryDomain.class
	            .getName(), "readByte", Type.BYTE, new Type[] { Type.INT },
	            Constants.INVOKEINTERFACE));
	
	}

	/**
	 * Read a word from the variable in addrIndex
	 */
	public static void compileReadByte(CompileInfo info, int addrIndex,
	        InstructionList ilist) {
	    ilist.append(new ILOAD(addrIndex));
	    compileReadByte(info, ilist);
	}

	/**
	 * Write a word from the value,address on the stack.
	 */
	public static void compileWriteWord(CompileInfo info, InstructionList ilist) {
	    ilist.append(new ALOAD(info.localMemory));
	
	    ilist.append(InstructionConstants.DUP_X2);
	    ilist.append(InstructionConstants.POP);
	    ilist.append(info.ifact.createInvoke(v9t9.common.memory.IMemoryDomain.class
	            .getName(), "writeWord", Type.VOID, new Type[] { Type.INT,
	            Type.SHORT }, Constants.INVOKEINTERFACE));
	
	}

	/**
	 * Write a byte from the value,address on the stack.
	 */
	public static void compileWriteByte(CompileInfo info, InstructionList ilist) {
	    ilist.append(new ALOAD(info.localMemory));
	    ilist.append(InstructionConstants.DUP_X2);
	    ilist.append(InstructionConstants.POP);
	    ilist.append(info.ifact.createInvoke(v9t9.common.memory.IMemoryDomain.class
	            .getName(), "writeByte", Type.VOID, new Type[] { Type.INT,
	            Type.BYTE }, Constants.INVOKEINTERFACE));
	}

}
