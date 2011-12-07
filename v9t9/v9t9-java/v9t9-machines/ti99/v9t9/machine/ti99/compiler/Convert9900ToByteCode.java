/**
 * 
 */
package v9t9.machine.ti99.compiler;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.compiler.CompileInfo;
import v9t9.engine.compiler.CompiledCode;
import v9t9.engine.memory.WordMemoryArea;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.Instruction9900;
import v9t9.machine.ti99.cpu.Status9900;

/**
 * @author ejs
 *
 */
public class Convert9900ToByteCode {

	/**
	 * Get compile-time behavior
	 */
	public static InstructionList getCompileAction(Instruction9900 ins, CompileInfo info) {
	    InstructionList ilist = new InstructionList();
	    InstructionList skiplist;
	
	    IMachineOperand mop1 = (IMachineOperand) ins.getOp1();
	    BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();
	    switch (ins.getInst()) {
	    case InstTableCommon.Idata:
	        return null;
	    case Inst9900.Ili:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iai:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iandi:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iori:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Ici:
	        break;
	    case Inst9900.Istwp:
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Istst:
	        ilist.append(new ALOAD(info.localStatus));
	        ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.Status9900.class
	                .getName(), "flatten", Type.SHORT, Type.NO_ARGS,
	                Constants.INVOKEVIRTUAL));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Ilwpi:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localWp));
	        updateWorkspaceVariables(info.ifact, ilist, info);
	        break;
	    case Inst9900.Ilimi:
	        ilist.append(new ALOAD(info.localStatus));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(info.ifact.createInvoke(Status9900.class.getName(), "setIntMask",
	                Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(info.ifact.createGetField(CompiledCode.class.getName(), "cpu",
	                new ObjectType(ICpu.class.getName())));
	        ilist.append(info.ifact.createInvoke(ICpu.class.getName(), "checkInterrupts",
	                Type.VOID, Type.NO_ARGS, Constants.INVOKEINTERFACE));
	        break;
	        //return null;
	    
	
	    case Inst9900.Iidle:
	    	return null;
	    case Inst9900.Irset:
	    	return null;
	    case Inst9900.Irtwp:
	        ilist.append(new ILOAD(info.localWp)); // WP
	        ilist.append(new PUSH(info.pgen, 30)); // WP, 30
	        ilist.append(InstructionConstants.IADD); // WP+30
	        ilist.append(InstructionConstants.I2S); // WP+30
	        ilist.append(InstructionConstants.DUP); // WP+30, WP+30
	        ilist.append(new ALOAD(info.localStatus)); // WP+30, WP+30, status
	        ilist.append(InstructionConstants.SWAP); // WP+30, status, WP+30
	        OperandCompiler9900.compileReadWord(info, ilist); // WP+30, status,
	                                                // <oldStatus>
	        ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.Status9900.class
	                .getName(), "expand", Type.VOID, new Type[] { Type.SHORT },
	                Constants.INVOKEVIRTUAL));
	
	        ilist.append(new PUSH(info.pgen, -2)); // WP+30, -2
	        ilist.append(InstructionConstants.IADD); // WP+28
	        ilist.append(InstructionConstants.I2S); // WP+28
	        ilist.append(InstructionConstants.DUP); // WP+28, WP+28
	        OperandCompiler9900.compileReadWord(info, ilist); // WP+28, <oldPC>
	        ilist.append(new ISTORE(info.localPc)); // WP+28
	
	        ilist.append(new PUSH(info.pgen, -2)); // WP+28, -2
	        ilist.append(InstructionConstants.IADD); // WP+26
	        ilist.append(InstructionConstants.I2S); // WP+26
	        OperandCompiler9900.compileReadWord(info, ilist); // <oldWP>
	        ilist.append(new ISTORE(info.localWp)); //
	
	        updateWorkspaceVariables(info.ifact, ilist, info);
	
	        break;
	    case Inst9900.Ickon:
	        // TODO
	    	return null;
	    case Inst9900.Ickof:
	        // TODO
	    	return null;
	    case Inst9900.Ilrex:
	        // TODO
	    	return null;
	    case Inst9900.Iblwp:
	        if (false && mop1.isConstant()) {
	            /*
	             * short targWp, targPc; targWp = mop1.getEA(memory, (short)
	             * mop1.ea, ... what wp? ...); targPc = mop1.getEA(memory,
	             * (short) (mop1.ea + 2), ... what wp? ...);
	             * OperandCompiler.compileReadAbsWord(info, ilist, targWp);
	             * ilist.append(new ISTORE(info.localWp));
	             * OperandCompiler.compileReadAbsWord(info, ilist, targPc);
	             * ilist.append(new ISTORE(info.localPc));
	             */
	        } else {
	            // just get the arguments: the INST_RSRC_CTX flag will invoke a
	            // contextSwitch
	            ilist.append(new ILOAD(info.localVal1));
	            ilist.append(InstructionConstants.DUP);
	            OperandCompiler9900.compileReadWord(info, ilist);
	            ilist.append(new ISTORE(info.localWp));
	            ilist.append(new PUSH(info.pgen, 2));
	            ilist.append(InstructionConstants.IADD);
	            ilist.append(InstructionConstants.I2S);
	            OperandCompiler9900.compileReadWord(info, ilist);
	            ilist.append(new ISTORE(info.localPc));
	        }
	        break;
	
	    case Inst9900.Ib:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case Inst9900.Ix:
	        return null;
	    /*
	     * ilist.append(InstructionConstants.THIS); int execIndex =
	     * info.pgen.addFieldref(v9t9.cpu.CompiledCode.class .getName(), "exec",
	     * Utility .getSignature(v9t9.cpu.Executor.class.getName()));
	     * ilist.append(new GETFIELD(execIndex)); int interpIndex =
	     * info.pgen.addFieldref(v9t9.cpu.Executor.class .getName(), "interp",
	     * Utility .getSignature(v9t9.cpu.Interpreter.class.getName()));
	     * ilist.append(new GETFIELD(interpIndex)); ilist.append(new
	     * ILOAD(info.localVal1));
	     * ilist.append(info.ifact.createInvoke(v9t9.cpu.Interpreter.class
	     * .getName(), "execute", Type.VOID, new Type[] { Type.SHORT },
	     * Constants.INVOKEVIRTUAL));
	     * 
	     * break;
	     */
	    case Inst9900.Iclr:
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Ineg:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.INEG);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iinv:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iinc:
	        ilist.append(new IINC(info.localVal1, 1));
	        break;
	    case Inst9900.Iinct:
	        ilist.append(new IINC(info.localVal1, 2));
	        break;
	    case Inst9900.Idec:
	        ilist.append(new IINC(info.localVal1, -1));
	        break;
	    case Inst9900.Idect:
	        ilist.append(new IINC(info.localVal1, -2));
	        break;
	    case Inst9900.Ibl:
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 11 * 2));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(new ILOAD(info.localPc));
	        OperandCompiler9900.compileWriteWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case Inst9900.Iswpb:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.DUP);
	        ilist.append(new PUSH(info.pgen, 8));
	        ilist.append(InstructionConstants.ISHL);
	        ilist.append(new PUSH(info.pgen, 0xff00));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.SWAP);
	        ilist.append(new PUSH(info.pgen, 8));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(new PUSH(info.pgen, 0x00ff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iseto:
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Iabs:
	        skiplist = new InstructionList();
	        skiplist.append(InstructionConstants.NOP);
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0x8000));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new IFEQ(skiplist.getStart()));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.INEG);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        ilist.append(skiplist);
	        break;
	    case Inst9900.Isra:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case Inst9900.Isrl:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0xffff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IUSHR); // why bother? shorts
	                                                    // are always sign
	                                                    // extended
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	
	    case Inst9900.Isla:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.ISHL);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	
	    case Inst9900.Isrc:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0xffff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IUSHR);
	
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 16));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.ISUB);
	        ilist.append(InstructionConstants.ISHL);
	
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	
	        break;
	
	    case Inst9900.Ijmp:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case Inst9900.Ijlt:
	        compileJump(info, ilist, "isLT");
	        break;
	    case Inst9900.Ijle:
	        compileJump(info, ilist, "isLE");
	        break;
	
	    case Inst9900.Ijeq:
	        compileJump(info, ilist, "isEQ");
	        break;
	    case Inst9900.Ijhe:
	        compileJump(info, ilist, "isHE");
	        break;
	    case Inst9900.Ijgt:
	        compileJump(info, ilist, "isGT");
	        break;
	    case Inst9900.Ijne:
	        compileJump(info, ilist, "isNE");
	        break;
	    case Inst9900.Ijnc:
	        compileJump(info, ilist, "isC", true);
	        break;
	    case Inst9900.Ijoc:
	        compileJump(info, ilist, "isC");
	        break;
	    case Inst9900.Ijno:
	        compileJump(info, ilist, "isO", true);
	        break;
	    case Inst9900.Ijl:
	        compileJump(info, ilist, "isL");
	        break;
	    case Inst9900.Ijh:
	        compileJump(info, ilist, "isH");
	        break;
	
	    case Inst9900.Ijop:
	        compileJump(info, ilist, "isP");
	        break;
	
	    case Inst9900.Isbo:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.hardware.ICruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case Inst9900.Isbz:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.hardware.ICruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case Inst9900.Itb:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.hardware.ICruHandler.class
	                .getName(), "readBits", Type.INT, new Type[] { Type.INT,
	                Type.INT }, Constants.INVOKEINTERFACE));
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Icoc:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Iczc:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Ixor:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Ixop:
	    	OperandCompiler9900.compileReadAbsWord(info, ilist,
	    			(short) (mop2.val * 4 + 0x40));
	    	ilist.append(InstructionConstants.DUP);
	    	ilist.append(new ISTORE(info.localWp));
	    	
	    	OperandCompiler9900.compileReadAbsWord(info, ilist,
	    			(short) (mop2.val * 4 + 0x42));
	    	ilist.append(new ISTORE(info.localPc));
	    	
	    	//memory.writeWord(iblock.wp + 11 * 2, iblock.ea1);
            ilist.append(new PUSH(info.pgen, 11 * 2));
            ilist.append(InstructionConstants.IADD);
            ilist.append(InstructionConstants.I2S);
            ilist.append(new ILOAD(info.localEa1));
            //ilist.append(InstructionConstants.SWAP);
            OperandCompiler9900.compileWriteWord(info, ilist);
	        break;
	
	    case Inst9900.Impy:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0xffff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new PUSH(info.pgen, 0xffff));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.IMUL);
	        ilist.append(InstructionConstants.DUP);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal3));
	        ilist.append(new PUSH(info.pgen, 16));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Idiv:
	        skiplist = new InstructionList();
	        skiplist.append(InstructionConstants.NOP);
	
	        ilist.append(new ILOAD(info.localEa2));
	        ilist.append(new PUSH(info.pgen, 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler9900.compileReadWord(info, ilist);
	        ilist.append(new ISTORE(info.localVal3));
	
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new IF_ICMPLE(skiplist.getStart()));
	
	        // short low = block.val3;
	        // int val = ((block.val2 & 0xffff) << 16) | (low & 0xffff);
	        ilist.append(new ILOAD(info.localVal3));
	        // ilist.append(new PUSH(info.pgen, 0xffff));
	        // ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ILOAD(info.localVal2));
	        // ilist.append(new PUSH(info.pgen, 0xffff));
	        // ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new PUSH(info.pgen, 16));
	        ilist.append(InstructionConstants.ISHL);
	        ilist.append(InstructionConstants.IOR);
	
	        ilist.append(InstructionConstants.DUP);
	        ilist.append(new ILOAD(info.localVal1));
	        // ilist.append(new PUSH(info.pgen, 0xffff));
	        // ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(InstructionConstants.IDIV);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal2));
	
	        ilist.append(new ILOAD(info.localVal1));
	        // ilist.append(new PUSH(info.pgen, 0xffff));
	        // ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(InstructionConstants.IREM);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal3));
	
	        ilist.append(skiplist);
	        break;
	
	    case Inst9900.Ildcr:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 12 * 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler9900.compileReadWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.hardware.ICruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case Inst9900.Istcr:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 12 * 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler9900.compileReadWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.hardware.ICruHandler.class
	                .getName(), "readBits", Type.INT, new Type[] { Type.INT,
	                Type.INT }, Constants.INVOKEINTERFACE));
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	
	    case Inst9900.Iszc:
	    case Inst9900.Iszcb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Is:
	    case Inst9900.Isb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.ISUB);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Ic:
	    case Inst9900.Icb:
	        break;
	
	    case Inst9900.Ia:
	    case Inst9900.Iab:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Imov:
	    case Inst9900.Imovb:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case Inst9900.Isoc:
	    case Inst9900.Isocb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    default:
	        /* not handled: interpret it */
	        return null;
	    }
	
	    return ilist;
	}

	/**
	 * @param info
	 * @param ilist
	 * @param
	 * @param t
	 */
	private static void compileJump(CompileInfo info, InstructionList ilist,
	        String test, boolean invert) {
	    InstructionList skiplist;
	    skiplist = new InstructionList();
	    skiplist.append(InstructionConstants.NOP);
	    ilist.append(new ALOAD(info.localStatus));
	    ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.Status9900.class.getName(),
	            test, Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	    if (invert) {
			ilist.append(new IFNE(skiplist.getStart()));
		} else {
			ilist.append(new IFEQ(skiplist.getStart()));
		}
	    ilist.append(new ILOAD(info.localVal1));
	    ilist.append(new ISTORE(info.localPc));
	    ilist.append(skiplist);
	}

	private static void compileJump(CompileInfo info, InstructionList ilist,
	        String test) {
	    compileJump(info, ilist, test, false);
	}

	static public void updateWorkspaceVariables(InstructionFactory ifact,
	        InstructionList ilist, CompileInfo info) {
        /* update WP */
        ilist.append(InstructionConstants.THIS);
        ilist.append(new GETFIELD(info.cpuIndex));
	    ilist.append(info.ifact.createCheckCast(new ObjectType(Cpu9900.class.getName())));

        ilist.append(new ILOAD(info.localWp));
        ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.Cpu9900.class.getName(),
                "setWP", Type.VOID, new Type[] { Type.SHORT },
                Constants.INVOKEVIRTUAL));

	    if (info.optimizeRegAccess) {
	        // get the wp memory...
	        ilist.append(new ALOAD(info.localMemory));
	        // ... entry
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(ifact.createInvoke(IMemoryDomain.class.getName(),
	                "getEntryAt", new ObjectType(IMemoryEntry.class.getName()),
	                new Type[] { Type.INT }, Constants.INVOKEINTERFACE));
	        ilist.append(ifact.createInvoke(IMemoryEntry.class.getName(),
	                "getArea", new ObjectType(IMemoryArea.class.getName()),
	                new Type[] { }, Constants.INVOKEINTERFACE));
	        // ... as a WordMemoryArea
	        ilist.append(ifact
	                .createCast(new ObjectType(IMemoryArea.class.getName()),
	                        new ObjectType(WordMemoryArea.class.getName())));
	
	        // ... and get the memory
	        //ilist.append(InstructionConstants.DUP);
	        ilist.append(ifact.createGetField(WordMemoryArea.class.getName(),
	                "memory", new ArrayType(Type.SHORT, 1)));
	        ilist.append(new ASTORE(info.localWpWordMemory));
	
	        // ... and get the offset of the WP into this
	        // WP >> 1
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(new ISTORE(info.localWpOffset));
	    }
	}

}
