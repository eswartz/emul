/**
 * 
 */
package v9t9.emulator.runtime.compiler;

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

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Status;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.WordMemoryArea;

/**
 * @author ejs
 *
 */
public class Convert9900ToByteCode {

	/**
	 * Get compile-time behavior
	 */
	public static InstructionList getCompileAction(Instruction ins, CompileInfo info) {
	    InstructionList ilist = new InstructionList();
	    InstructionList skiplist;
	
	    MachineOperand mop1 = (MachineOperand) ins.op1;
	    //MachineOperand mop2 = (MachineOperand) ins.op2;
	    switch (ins.inst) {
	    case InstructionTable.Idata:
	        return null;
	    case InstructionTable.Ili:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iai:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iandi:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iori:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Ici:
	        break;
	    case InstructionTable.Istwp:
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Istst:
	        ilist.append(new ALOAD(info.localStatus));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
	                .getName(), "flatten", Type.SHORT, Type.NO_ARGS,
	                Constants.INVOKEVIRTUAL));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Ilwpi:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localWp));
	        updateWorkspaceVariables(info.ifact, ilist, info);
	        break;
	    case InstructionTable.Ilimi:
	        ilist.append(new ALOAD(info.localStatus));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(info.ifact.createInvoke(Status.class.getName(), "setIntMask",
	                Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
	        /*
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(info.ifact.createGetField(CompiledCode.class.getName(), "cpu",
	                new ObjectType(Cpu.class.getName())));
	        ilist.append(info.ifact.createInvoke(Cpu.class.getName(), "abortIfInterrupted",
	                Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	    	 */
	        break;
	        //return null;
	    /*
	     * ilist.append(InstructionConstants.THIS); ilist.append(new
	     * GETFIELD(info.cpuIndex));
	     * ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
	     * "ping", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	     * 
	     * ilist.append(InstructionConstants.THIS); ilist.append(new
	     * GETFIELD(info.cpuIndex));
	     * 
	     * ilist.append(InstructionConstants.DUP);
	     * ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
	     * "getPC", Type.SHORT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	     * ilist.append(new ISTORE(info.localPc));
	     * 
	     * ilist.append(InstructionConstants.DUP);
	     * ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
	     * "getWP", Type.SHORT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	     * ilist.append(new ISTORE(info.localWp));
	     * 
	     * ilist.append(InstructionConstants.DUP);
	     * ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
	     * "getStatus", new ObjectType(v9t9.cpu.Status.class.getName()),
	     * Type.NO_ARGS, Constants.INVOKEVIRTUAL)); ilist.append(new
	     * ASTORE(info.localStatus));
	     * 
	     * ilist.append(InstructionConstants.POP); // cpu break;
	     */
	
	    case InstructionTable.Iidle:
	        // cpu.idle(); // TODO
	        break;
	    case InstructionTable.Irset:
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new ISTORE(info.localVal1));
	        // cpu.rset(); // TODO
	        break;
	    case InstructionTable.Irtwp:
	        ilist.append(new ILOAD(info.localWp)); // WP
	        ilist.append(new PUSH(info.pgen, 30)); // WP, 30
	        ilist.append(InstructionConstants.IADD); // WP+30
	        ilist.append(InstructionConstants.I2S); // WP+30
	        ilist.append(InstructionConstants.DUP); // WP+30, WP+30
	        ilist.append(new ALOAD(info.localStatus)); // WP+30, WP+30, status
	        ilist.append(InstructionConstants.SWAP); // WP+30, status, WP+30
	        OperandCompiler.compileReadWord(info, ilist); // WP+30, status,
	                                                // <oldStatus>
	        ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
	                .getName(), "expand", Type.VOID, new Type[] { Type.SHORT },
	                Constants.INVOKEVIRTUAL));
	
	        ilist.append(new PUSH(info.pgen, -2)); // WP+30, -2
	        ilist.append(InstructionConstants.IADD); // WP+28
	        ilist.append(InstructionConstants.I2S); // WP+28
	        ilist.append(InstructionConstants.DUP); // WP+28, WP+28
	        OperandCompiler.compileReadWord(info, ilist); // WP+28, <oldPC>
	        ilist.append(new ISTORE(info.localPc)); // WP+28
	
	        ilist.append(new PUSH(info.pgen, -2)); // WP+28, -2
	        ilist.append(InstructionConstants.IADD); // WP+26
	        ilist.append(InstructionConstants.I2S); // WP+26
	        OperandCompiler.compileReadWord(info, ilist); // <oldWP>
	        ilist.append(new ISTORE(info.localWp)); //
	
	        updateWorkspaceVariables(info.ifact, ilist, info);
	
	        break;
	    case InstructionTable.Ickon:
	        // TODO
	        break;
	    case InstructionTable.Ickof:
	        // TODO
	        break;
	    case InstructionTable.Ilrex:
	        // TODO
	        break;
	    case InstructionTable.Iblwp:
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
	            OperandCompiler.compileReadWord(info, ilist);
	            ilist.append(new ISTORE(info.localWp));
	            ilist.append(new PUSH(info.pgen, 2));
	            ilist.append(InstructionConstants.IADD);
	            ilist.append(InstructionConstants.I2S);
	            OperandCompiler.compileReadWord(info, ilist);
	            ilist.append(new ISTORE(info.localPc));
	        }
	        break;
	
	    case InstructionTable.Ib:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case InstructionTable.Ix:
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
	    case InstructionTable.Iclr:
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Ineg:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.INEG);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iinv:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iinc:
	        ilist.append(new IINC(info.localVal1, 1));
	        break;
	    case InstructionTable.Iinct:
	        ilist.append(new IINC(info.localVal1, 2));
	        break;
	    case InstructionTable.Idec:
	        ilist.append(new IINC(info.localVal1, -1));
	        break;
	    case InstructionTable.Idect:
	        ilist.append(new IINC(info.localVal1, -2));
	        break;
	    case InstructionTable.Ibl:
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 11 * 2));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(new ILOAD(info.localPc));
	        OperandCompiler.compileWriteWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case InstructionTable.Iswpb:
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
	    case InstructionTable.Iseto:
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Iabs:
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
	    case InstructionTable.Isra:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	    case InstructionTable.Isrl:
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
	
	    case InstructionTable.Isla:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.ISHL);
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	
	    case InstructionTable.Isrc:
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
	
	    case InstructionTable.Ijmp:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localPc));
	        break;
	    case InstructionTable.Ijlt:
	        compileJump(info, ilist, "isLT");
	        break;
	    case InstructionTable.Ijle:
	        compileJump(info, ilist, "isLE");
	        break;
	
	    case InstructionTable.Ijeq:
	        compileJump(info, ilist, "isEQ");
	        break;
	    case InstructionTable.Ijhe:
	        compileJump(info, ilist, "isHE");
	        break;
	    case InstructionTable.Ijgt:
	        compileJump(info, ilist, "isGT");
	        break;
	    case InstructionTable.Ijne:
	        compileJump(info, ilist, "isNE");
	        break;
	    case InstructionTable.Ijnc:
	        compileJump(info, ilist, "isC", true);
	        break;
	    case InstructionTable.Ijoc:
	        compileJump(info, ilist, "isC");
	        break;
	    case InstructionTable.Ijno:
	        compileJump(info, ilist, "isO", true);
	        break;
	    case InstructionTable.Ijl:
	        compileJump(info, ilist, "isL");
	        break;
	    case InstructionTable.Ijh:
	        compileJump(info, ilist, "isH");
	        break;
	
	    case InstructionTable.Ijop:
	        compileJump(info, ilist, "isP");
	        break;
	
	    case InstructionTable.Isbo:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.CruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case InstructionTable.Isbz:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.CruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case InstructionTable.Itb:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.CruHandler.class
	                .getName(), "readBits", Type.INT, new Type[] { Type.INT,
	                Type.INT }, Constants.INVOKEINTERFACE));
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        ilist.append(new PUSH(info.pgen, 0));
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Icoc:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Iczc:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Ixor:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Ixop:
	        if (false && mop1.isConstant()) {
	            OperandCompiler.compileReadAbsWord(info, ilist,
	                    (short) (mop1.val * 2 + 0x40));
	        } else {
	            ilist.append(new ILOAD(info.localVal1));
	            ilist.append(new PUSH(info.pgen, 2));
	            ilist.append(InstructionConstants.ISHL);
	            ilist.append(new PUSH(info.pgen, 0x40));
	            ilist.append(InstructionConstants.IADD);
	            ilist.append(InstructionConstants.DUP);
	            OperandCompiler.compileReadWord(info, ilist);
	        }
	        if (false && mop1.isConstant()) {
	            OperandCompiler.compileReadAbsWord(info, ilist,
	                    (short) (mop1.val * 2 + 0x42));
	        } else {
	            ilist.append(new ISTORE(info.localWp));
	            ilist.append(new PUSH(info.pgen, 2));
	            ilist.append(InstructionConstants.IADD);
	            ilist.append(InstructionConstants.I2S);
	            OperandCompiler.compileReadWord(info, ilist);
	        }
	        ilist.append(new ISTORE(info.localPc));
	        break;
	
	    case InstructionTable.Impy:
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
	
	    case InstructionTable.Idiv:
	        skiplist = new InstructionList();
	        skiplist.append(InstructionConstants.NOP);
	
	        ilist.append(new ILOAD(info.localEa2));
	        ilist.append(new PUSH(info.pgen, 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler.compileReadWord(info, ilist);
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
	
	    case InstructionTable.Ildcr:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 12 * 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler.compileReadWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.CruHandler.class
	                .getName(), "writeBits", Type.VOID, new Type[] { Type.INT,
	                Type.INT, Type.INT }, Constants.INVOKEINTERFACE));
	        break;
	
	    case InstructionTable.Istcr:
	        ilist.append(InstructionConstants.THIS);
	        ilist.append(new GETFIELD(info.cruIndex));
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, 12 * 2));
	        ilist.append(InstructionConstants.IADD);
	        OperandCompiler.compileReadWord(info, ilist);
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(info.ifact.createInvoke(v9t9.engine.CruHandler.class
	                .getName(), "readBits", Type.INT, new Type[] { Type.INT,
	                Type.INT }, Constants.INVOKEINTERFACE));
	        ilist.append(InstructionConstants.I2S);
	        ilist.append(new ISTORE(info.localVal1));
	        break;
	
	    case InstructionTable.Iszc:
	    case InstructionTable.Iszcb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new PUSH(info.pgen, -1));
	        ilist.append(InstructionConstants.IXOR);
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Is:
	    case InstructionTable.Isb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.ISUB);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Ic:
	    case InstructionTable.Icb:
	        break;
	
	    case InstructionTable.Ia:
	    case InstructionTable.Iab:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Imov:
	    case InstructionTable.Imovb:
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    case InstructionTable.Isoc:
	    case InstructionTable.Isocb:
	        ilist.append(new ILOAD(info.localVal2));
	        ilist.append(new ILOAD(info.localVal1));
	        ilist.append(InstructionConstants.IOR);
	        ilist.append(new ISTORE(info.localVal2));
	        break;
	
	    default:
	        /* not handled */
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
	    ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class.getName(),
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
        ilist.append(new ILOAD(info.localWp));
        ilist.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                "setWP", Type.VOID, new Type[] { Type.SHORT },
                Constants.INVOKEVIRTUAL));

	    if (Compiler.settingOptimize.getBoolean()
	            && Compiler.settingOptimizeRegAccess.getBoolean()) {
	        // get the wp memory...
	        ilist.append(new ALOAD(info.localMemory));
	        // ... area
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(ifact.createInvoke(MemoryDomain.class.getName(),
	                "getArea", new ObjectType(MemoryArea.class.getName()),
	                new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
	        // ... as a WordMemoryArea
	        ilist.append(ifact
	                .createCast(new ObjectType(MemoryArea.class.getName()),
	                        new ObjectType(WordMemoryArea.class.getName())));
	
	        // ... and get the memory
	        ilist.append(InstructionConstants.DUP);
	        ilist.append(ifact.createGetField(WordMemoryArea.class.getName(),
	                "memory", new ArrayType(Type.SHORT, 1)));
	        ilist.append(new ASTORE(info.localWpWordMemory));
	
	        // ... and get the offset of the WP into this
	        // (area.offset + (WP & 0xfffe)) >> 1
	        ilist.append(ifact.createGetField(MemoryArea.class.getName(),
	                "offset", Type.INT));
	        ilist.append(new ILOAD(info.localWp));
	        ilist.append(new PUSH(info.pgen, MemoryArea.AREASIZE - 2));
	        ilist.append(InstructionConstants.IAND);
	        ilist.append(InstructionConstants.IADD);
	        ilist.append(new PUSH(info.pgen, 1));
	        ilist.append(InstructionConstants.ISHR);
	        ilist.append(new ISTORE(info.localWpOffset));
	    }
	}

}
