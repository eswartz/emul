/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.emulator.runtime.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.TABLESWITCH;
import org.apache.bcel.generic.Type;
import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.Machine;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;
import v9t9.emulator.runtime.Cpu;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * This class compiles 9900 code into Java bytecode. 
 * 
 * @author ejs
 */
public class Compiler {
	public interface InstructionRangeCompiler {
		void compileInstructionRange(Compiler compiler, Instruction[] insts,
				HighLevelCodeInfo highLevel,  
				InstructionList ilist, CompileInfo info);
	}
    Cpu cpu;

    Machine machine;

    MemoryDomain memory;

    static public final String sOptimize = "CompilerOptimize";

    static public final Setting settingOptimize = new Setting(sOptimize,
            new Boolean(false));

    static public final String sOptimizeRegAccess = "CompilerOptimizeRegAccess";

    static public final Setting settingOptimizeRegAccess = new Setting(
            sOptimizeRegAccess, new Boolean(false));

    static public final String sOptimizeStatus = "CompilerOptimizeStatus";

    static public final Setting settingOptimizeStatus = new Setting(
            sOptimizeStatus, new Boolean(false));

    static public final String sCompileOptimizeCallsWithData = "CompilerOptmizeCallsWithData";

    static public final Setting settingCompileOptimizeCallsWithData = new Setting(
            sCompileOptimizeCallsWithData, new Boolean(false));

    static public final String sDebugInstructions = "DebugInstructions";

    static public final Setting settingDebugInstructions = new Setting(
            sDebugInstructions, new Boolean(false));

    static public final String sDumpModuleRomInstructions = "CompilerDumpModuleRomInstructions";

    static public final Setting settingDumpModuleRomInstructions = new Setting(
    		sDumpModuleRomInstructions, new Boolean(false));

    static public final String sCompileFunctions = "CompilerCompileFunctions";

    static public final Setting settingCompileFunctions = new Setting(
    		sCompileFunctions, new Boolean(false));

    public Compiler(Cpu cpu) {
        this.cpu = cpu;
        
        this.machine = cpu.getMachine();
        this.memory = cpu.getConsole();

        machine.getSettings().register(settingOptimize);
        machine.getSettings().register(settingOptimizeRegAccess);
        machine.getSettings().register(settingOptimizeStatus);
        machine.getSettings().register(settingDebugInstructions);
        machine.getSettings().register(settingCompileOptimizeCallsWithData);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     */
    private void updateStatus(int handler, CompileInfo info) {
        InstructionList ilist = info.ilist;
        InstructionList labellist = null, skiplist = null;
        switch (handler) {
        case Instruction.st_NONE:
            return;
        case Instruction.st_ALL:
            // just a note that Status should be up to date, for future work
            return;
        case Instruction.st_INT:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "setIntMask", Type.VOID,
                    new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_BYTE_LAECOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(InstructionConstants.I2B);
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(InstructionConstants.I2B);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_ADD_BYTE_LAECOP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_LAECO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_ADD_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_LAECO_REV:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_ADD_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_SUB_BYTE_LAECOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(InstructionConstants.I2B);
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(InstructionConstants.I2B);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_SUB_BYTE_LAECOP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_SUB_LAECO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_SUB_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_BYTE_CMP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(InstructionConstants.I2B);
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(InstructionConstants.I2B);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_BYTE_CMP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_CMP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_CMP", Type.VOID, new Type[] { Type.SHORT,
                    Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_DIV_O:
            skiplist = new InstructionList();
            skiplist.append(InstructionConstants.NOP);
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(InstructionConstants.IAND);
            // ilist.append(InstructionConstants.I2S);
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(InstructionConstants.IAND);
            // ilist.append(InstructionConstants.I2S);
            ilist.append(new IF_ICMPLE(labellist.getStart()));
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_O", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_E:
            skiplist = new InstructionList();
            skiplist.append(InstructionConstants.NOP);
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IF_ICMPEQ(labellist.getStart()));
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_E", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_LAE:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_LAE", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_LAE_1:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_LAE", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_BYTE_LAEP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(InstructionConstants.I2B);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_BYTE_LAEP", Type.VOID,
                    new Type[] { Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_BYTE_LAEP_1:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(InstructionConstants.I2B);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_BYTE_LAEP", Type.VOID,
                    new Type[] { Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_LAEO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_LAEO", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_O:
            skiplist = new InstructionList();
            skiplist.append(InstructionConstants.NOP);
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0x8000));
            ilist.append(new IF_ICMPEQ(labellist.getStart()));
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_O", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_SHIFT_LEFT_CO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_SHIFT_LEFT_CO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));

            break;
        case Instruction.st_SHIFT_RIGHT_C:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_SHIFT_RIGHT_C", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_XOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.engine.cpu.Status.class
                    .getName(), "set_X", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            break;

        default:
            throw new AssertionError("unhandled status handler " + handler);
        }
    }

    Set<Integer> instSet;

    /**
     * Generate code for one instruction. Postcondition: either the code jumps
     * to info.doneInst (which indicates successful execution) or the code
     * pushes its own ICONST(0) and jumps to info.breakInst (to indicate
     * failure).
     * 
     * @param ilist
     * @param i
     * @param info
     * @return
     */
    public void generateInstruction(int pc, Instruction ins,
            CompileInfo info, InstInfo ii) {
        InstructionList ilist = info.ilist;

        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

        if (instSet == null) {
			instSet = new java.util.HashSet<Integer>();
		}
        Integer instInt = new Integer(ins.inst);
        if (!instSet.contains(instInt)) {
            System.out.println("first use of " + ins.getName() + " at "
                    + HexUtils.toHex4(ins.pc));
            instSet.add(instInt);
        }

        // for LIMI >2, always return to get a chance to ping the CPU
        // if (ins.inst == Instruction.Ilimi)
        // return null;

        if (mop1.type == MachineOperand.OP_STATUS
                || mop1.type == MachineOperand.OP_INST) {
            mop1.type = MachineOperand.OP_NONE;
            mop1.dest = MachineOperand.OP_DEST_FALSE;
        }
        if (mop2.type == MachineOperand.OP_STATUS
                || mop2.type == MachineOperand.OP_INST) {
            mop2.type = MachineOperand.OP_NONE;
            mop2.dest = MachineOperand.OP_DEST_FALSE;
        }

        /* generate code for the specific opcode */
        InstructionList actlist = Convert9900ToByteCode.getCompileAction(ins, info);
        
        /*
        // If jumping, there is the potential for an infinite loop,
        // so this is a good time to see if we need to stop to handle
        // interrupts, etc.
        if (ins.jump != Instruction.INST_JUMP_FALSE) {
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "abortIfInterrupted", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
        }
        */
        
        // TODO debug
        ilist.append(new PUSH(info.pgen, HexUtils.toHex4(ins.pc) + " "
                + ins.toString()));
        ilist.append(InstructionConstants.POP);

        if (settingDebugInstructions.getBoolean()) {
            ilist.append(InstructionConstants.THIS);
            ilist.append(new PUSH(info.pgen, pc));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.vdpIndex));
            ilist.append(info.ifact.createInvoke(VdpMmio.class
                    .getName(), "getAddr", Type.INT, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.gplIndex));
            ilist.append(info.ifact.createInvoke(GplMmio.class
                    .getName(), "getAddr", Type.INT, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            ilist.append(info.ifact.createInvoke(CompiledCode.class
                    .getName(), "dump", Type.VOID, new Type[] { Type.SHORT,
                    Type.SHORT,
                    new ObjectType(v9t9.engine.cpu.Status.class.getName()),
                    Type.INT, Type.INT}, Constants.INVOKEVIRTUAL));
        }

        // no compilation?
        if (actlist == null) {
        	//System.out.println("Interpreting >" + Utils.toHex4(ins.pc) + " " + ins);
        	ii.ins = ins;
        	return;
        }

        // update # instructions executed
        /*
         * ilist.append(InstructionConstants.THIS);
         * ilist.append(InstructionConstants.DUP); ilist.append(new
         * GETFIELD(info.nInstructionsIndex));
         * ilist.append(InstructionConstants.ICONST_1);
         * ilist.append(InstructionConstants.IADD); ilist.append(new
         * PUTFIELD(info.nInstructionsIndex));
         */
        ilist.append(new IINC(info.localInsts, 1));

        /* compose operand values and instruction timings */
        fetchOperands(ins, pc, info);

        ilist.append(new IINC(info.localCycles, ins.cycles + ((MachineOperand) ins.op1).cycles + ((MachineOperand) ins.op2).cycles));

        if (settingDebugInstructions.getBoolean()) {
            dumpFull(info, ilist, ins, "dumpBefore", ins.toString());
        }

        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore, info);
        }

        /* execute */
        if (actlist != null)
        	ilist.append(actlist);
        else
        	ilist.append(InstructionConstants.NOP);

        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter, info);
        }

        /* save any operands */
        flushOperands(ins, info);

        if (settingDebugInstructions.getBoolean()) {
            dumpFull(info, ilist, ins, "dumpAfter", null);
        }

        /*
        // access an object which we change outside the executor
        // to indicate that execution should stop
        //
        // If jumping, there is the potential for an infinite loop,
        // so this is a good time to see if we need to stop to handle
        // interrupts, etc.
        if (ins.jump != Instruction.INST_JUMP_FALSE || ins.inst == Instruction.Ilimi) {
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                    "abortIfInterrupted", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
        }
        */
        
        ilist.append(InstructionConstants.NOP);

        ii.chunk = ilist;
        ii.ins = ins;
    }

    private void dumpFull(CompileInfo info, InstructionList ilist,
            Instruction ins, String routine, String insString) {
        Type types[];
        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;
        ilist.append(InstructionConstants.THIS);
        if (insString != null) {
            ilist.append(new PUSH(info.pgen, insString));
            types = new Type[] { new ObjectType(String.class.getName()),
                    Type.SHORT, Type.SHORT,
                    new ObjectType(v9t9.engine.cpu.Status.class.getName()),
                    Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, Type.INT,
                    Type.INT, Type.INT, Type.INT };

        } else {
			types = new Type[] { Type.SHORT, Type.SHORT,
                    new ObjectType(v9t9.engine.cpu.Status.class.getName()),
                    Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, Type.INT,
                    Type.INT, Type.INT, Type.INT };
		}

        ilist.append(new PUSH(info.pgen, ins.pc));
        ilist.append(new ILOAD(info.localWp));
        ilist.append(new ALOAD(info.localStatus));
        int ignore = insString != null ? MachineOperand.OP_DEST_KILLED
                : MachineOperand.OP_DEST_FALSE;
        if (mop1.type != MachineOperand.OP_NONE && mop1.dest != ignore) {
            ilist.append(new ILOAD(info.localEa1));
            ilist.append(new ILOAD(info.localVal1));
        } else {
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(InstructionConstants.ICONST_0);
        }
        if (mop2.type != MachineOperand.OP_NONE && mop2.dest != ignore) {
            ilist.append(new ILOAD(info.localEa2));
            ilist.append(new ILOAD(info.localVal2));
        } else {
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(InstructionConstants.ICONST_0);
        }
        ilist.append(new PUSH(info.pgen, mop1.type));
        ilist.append(new PUSH(info.pgen, mop1.dest));
        ilist.append(new PUSH(info.pgen, mop2.type));
        ilist.append(new PUSH(info.pgen, mop2.dest));
        ilist
                .append(info.ifact.createInvoke(v9t9.emulator.runtime.compiler.CompiledCode.class
                        .getName(), routine, Type.VOID, types,
                        Constants.INVOKEVIRTUAL));
    }

    /**
     * Fetch operands for instruction (compile time)
     * 
     * @param ins
     * @param pc
     * @param info
     */
    private void fetchOperands(Instruction ins, int pc, CompileInfo info) {
        InstructionList ilist = info.ilist;

        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

        /* update PC to current position */
        ilist.append(new PUSH(info.pgen, ins.pc + ins.size));
        ilist.append(new ISTORE(info.localPc));

        if (mop1.type != MachineOperand.OP_NONE) {
            OperandCompiler.compileGetEA(mop1, info.localEa1, info, ins, ins.pc);
        }
        if (mop2.type != MachineOperand.OP_NONE) {
            OperandCompiler.compileGetEA(mop2, info.localEa2, info, ins, ins.pc);
        }
        if (mop1.type != MachineOperand.OP_NONE
                && mop1.dest != MachineOperand.OP_DEST_KILLED) {
            OperandCompiler.compileGetValue(mop1, info.localVal1, info.localEa1, info);
        }
        if (mop2.type != MachineOperand.OP_NONE
                && mop2.dest != MachineOperand.OP_DEST_KILLED) {
            OperandCompiler.compileGetValue(mop2, info.localVal2, info.localEa2, info);
        }
        if (ins.inst == InstructionTable.Idiv) {
            // TODO: read this value in instruction code
            /*
             * compileLoadAddress(info, info.localEa2, 2);
             * compileReadWord(info); ilist.append(new ISTORE(info.localEa3));
             */
        }

    }

    /**
     * 
     */
    private void flushOperands(Instruction ins, CompileInfo info) {
        InstructionList ilist = info.ilist;

        MachineOperand mop1 = (MachineOperand) ins.op1;
        MachineOperand mop2 = (MachineOperand) ins.op2;

        if (mop1.dest != MachineOperand.OP_DEST_FALSE) {
            OperandCompiler.compilePutValue(mop1, info.localVal1, info.localEa1, info);
        }
        if (mop2.dest != MachineOperand.OP_DEST_FALSE) {
            OperandCompiler.compilePutValue(mop2, info.localVal2, info.localEa2, info);

            if (ins.inst == InstructionTable.Impy || ins.inst == InstructionTable.Idiv) {
                ilist.append(new ILOAD(info.localEa2));
                ilist.append(new PUSH(info.pgen, 2));
                ilist.append(InstructionConstants.IADD);
                ilist.append(new ILOAD(info.localVal3));
                OperandCompiler.compileWriteWord(info, ilist);
            }
        }

        if ((ins.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* commit changes to cpu before callback */

            /* save status */
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                    "setStatus", Type.VOID, new Type[] { new ObjectType(
                            v9t9.engine.cpu.Status.class.getName()) },
                    Constants.INVOKEVIRTUAL));

            /* update PC first */
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new PUSH(info.pgen, ins.pc + ins.size)); // old value
            ilist.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                    "setPC", Type.VOID, new Type[] { Type.SHORT },
                    Constants.INVOKEVIRTUAL));

            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new ILOAD(info.localPc));
            ilist.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                    "contextSwitch", Type.VOID, new Type[] { Type.SHORT,
                            Type.SHORT }, Constants.INVOKEVIRTUAL));

            Convert9900ToByteCode.updateWorkspaceVariables(info.ifact, ilist, info);

        }
    }

    /** Utility method for adding constructed method to class. */
    private static void addMethod(MethodGen mgen, ClassGen cgen) {
        mgen.setMaxStack();
        mgen.setMaxLocals();
        InstructionList ilist = mgen.getInstructionList();
        Method method = mgen.getMethod();
        cgen.addMethod(method);
        ilist.dispose();
    }

    static public class InstInfo {
        InstructionList chunk;

        Instruction ins;
    }

    /**
     * Generate bytecode for the 9900 instructions in our segment of the memory.
     * @param block
     * @param entries 
     */
    public byte[] compile(String className, String baseName, HighLevelCodeInfo highLevel, Instruction[] insts, short[] entries) {

        // build generators for the new class
        ClassGen cgen = new ClassGen(className /* class */,
                CompiledCode.class.getName() /* superclass */, className
                        + ".java" /* filename */, Constants.ACC_PUBLIC, null /* interfaces */);
        InstructionFactory ifact = new InstructionFactory(cgen);

        // set up class
        createConstructors(className, cgen, ifact);

        // define the code
        createRunMethod(className, insts, highLevel, 
        		cgen, ifact);

        // return bytecode of completed class
        byte[] bytecode = cgen.getJavaClass().getBytes();

        if (true) {
            File dir = new File("tmp");
            dir.mkdirs();
            File test = new File(dir, baseName + ".class");
            try {
	            FileOutputStream out = new FileOutputStream(test);
	            out.write(bytecode);
	            out.close();
            } catch (IOException e) {
            	System.err.println("Could not write class file to " + test + ": " + e);
            }
        }
        return bytecode;
    }

	private void createConstructors(String className, ClassGen cgen,
			InstructionFactory ifact) {
		ConstantPoolGen pgen = cgen.getConstantPool();
		InstructionList ilist;
		MethodGen mgen;
		// create instruction list for default constructor
        ilist = new InstructionList();
        ilist.append(InstructionConstants.THIS);
        ilist.append(ifact.createInvoke(CompiledCode.class.getName(),
                        "<init>", Type.VOID, Type.NO_ARGS,
                        Constants.INVOKESPECIAL));
        ilist.append(InstructionFactory.createReturn(Type.VOID));

        // add public default constructor method to class
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
                new String[] {}, "<init>", className, ilist, pgen);
        addMethod(mgen, cgen);

        // create instruction list
        ilist = new InstructionList();
        ilist.append(InstructionConstants.THIS);
        ilist.append(InstructionConstants.ALOAD_1);
        ilist.append(ifact.createInvoke(CompiledCode.class.getName(),
                "<init>", Type.VOID, new Type[] { new ObjectType(
                        v9t9.emulator.runtime.Executor.class.getName()) },
                Constants.INVOKESPECIAL));
        ilist.append(InstructionFactory.createReturn(Type.VOID));

        // add constructor method to class
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
                new Type[] { new ObjectType(v9t9.emulator.runtime.Executor.class
                        .getName()) }, new String[] { "exec" }, "<init>",
                className, ilist, pgen);
        addMethod(mgen, cgen);
	}

    private void createRunMethod(String className, Instruction[] insts,
				HighLevelCodeInfo highLevel, 
				ClassGen cgen,
				InstructionFactory ifact) {
			
        ConstantPoolGen pgen = cgen.getConstantPool();

		MethodGen mgen;
		// create public boolean run() method
        mgen = new MethodGen(Constants.ACC_PUBLIC + Constants.ACC_FINAL,
                Type.BOOLEAN, Type.NO_ARGS, null, "run", className, new InstructionList(),
                pgen);

        InstructionList ilist;
        ilist = mgen.getInstructionList();
        
        // setup the locals and the state used during compilation
        CompileInfo info = setupCompileInfo(ifact, pgen, mgen);

        // load CPU state info locals
        compileLoadCpuState(ifact, ilist, info);

        // this is the start of the try {
        InstructionHandle tryStart = ilist.append(InstructionConstants.NOP);

        // switch(...) { ... }
        
		// get PC entry points for every possible even address
        int numInsts = insts.length;
		int[] pcs = new int[numInsts];
        for (int i = 0; i < numInsts; i ++) {
            pcs[i] = insts[i].pc / 2;
        }

        info.sw = new TABLESWITCH(pcs, new InstructionHandle[numInsts], null);
        info.switchInst = ilist.append(InstructionConstants.NOP);

        // Add cycles from "nCycles" and reset, for timing that depends on CPU
        //
        ilist.append(InstructionConstants.THIS);
        ilist.append(new GETFIELD(info.cpuIndex));
        ilist.append(new ILOAD(info.localCycles));
        ilist.append(info.ifact.createInvoke(Cpu.class.getName(),
        		"addCycles", Type.VOID, new Type[] { Type.INT },
                Constants.INVOKEVIRTUAL));
        ilist.append(InstructionConstants.ICONST_0);
        ilist.append(new ISTORE(info.localCycles));

        ilist.append(new ILOAD(info.localPc));
        ilist.append(InstructionConstants.ICONST_1);
        ilist.append(InstructionConstants.ISHR);
        ilist.append(info.sw);

        InstructionRangeCompiler instructionRangeCompiler = null;
        
    	// compile every instruction as a target of a switch(pc) { ...},
    	// where any jump instr comes back to the switch, serial instructions
    	// jump to their next logical instruction, and the last instruction returns true;
    	// any switch() not handled returns false so the interpreter can have a look
        if (settingCompileFunctions.getBoolean()) {
        	instructionRangeCompiler = new FunctionInstructionRangeCompiler();
        } else {
        	instructionRangeCompiler = new SerialInstructionRangeCompiler();
        }
        instructionRangeCompiler.compileInstructionRange(this, insts, highLevel, ilist, info);

        // falling through for default just means to try again in another code block,
        // so that is successful
        info.sw.setTarget(info.doneInst);

        // end of switch (return 1)
        ilist.append(info.breakList);
        
        // ALL PATHS leave return code on stack: 
        // true to keep trying to compile, false when hitting instruction in range but not compiled
        InstructionList cleanupList = new InstructionList();

        InstructionHandle cleanupInst = cleanupList.append(InstructionConstants.NOP);
        ilist.append(cleanupList);

        // flush local variables back to CPU
        compileStoreCpuState(ifact, ilist, info);

        // return, with return code previously placed on stack
        ilist.append(InstructionConstants.IRETURN);

        // //////////////

        // handle AbortedException (anything)

        // the exception is on the stack
        InstructionHandle here = compileStoreCpuState(ifact, ilist, info);

        ilist.append(InstructionConstants.ATHROW);

        mgen.addExceptionHandler(tryStart, cleanupInst, here, null); 

        addMethod(mgen, cgen);
	}

	private CompileInfo setupCompileInfo(InstructionFactory ifact,
			ConstantPoolGen pgen, MethodGen mgen) {
		CompileInfo info = new CompileInfo(pgen, ifact);
        info.ilist = null;
        info.memory = cpu.getConsole();

        info.cpuIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "cpu", Utility.getSignature(v9t9.emulator.runtime.Cpu.class.getName()));
        info.memoryIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "memory", Utility.getSignature(v9t9.engine.memory.MemoryDomain.class.getName()));
        info.cruIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "cru", Utility.getSignature(v9t9.engine.CruHandler.class.getName()));
        info.nInstructionsIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "nInstructions", Utility.getSignature("int"));
        info.nCyclesIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "nCycles", Utility.getSignature("int"));
        info.vdpIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "vdpMmio", Utility.getSignature(v9t9.emulator.hardware.memory.mmio.VdpMmio.class.getName()));
        info.gplIndex = pgen.addFieldref(v9t9.emulator.runtime.compiler.CompiledCode.class.getName(), // className,
                "gplMmio", Utility.getSignature(v9t9.emulator.hardware.memory.mmio.GplMmio.class.getName()));

        // create locals
        LocalVariableGen lg;
        lg = mgen.addLocalVariable("pc", Type.SHORT, null, null);
        info.localPc = lg.getIndex();
        lg = mgen.addLocalVariable("wp", Type.SHORT, null, null);
        info.localWp = lg.getIndex();
        lg = mgen.addLocalVariable("ea1", Type.SHORT, null, null);
        info.localEa1 = lg.getIndex();
        lg = mgen.addLocalVariable("ea2", Type.SHORT, null, null);
        info.localEa2 = lg.getIndex();
        lg = mgen.addLocalVariable("val1", Type.SHORT, null, null);
        info.localVal1 = lg.getIndex();
        lg = mgen.addLocalVariable("val2", Type.SHORT, null, null);
        info.localVal2 = lg.getIndex();
        lg = mgen.addLocalVariable("val3", Type.SHORT, null, null);
        info.localVal3 = lg.getIndex();
        lg = mgen.addLocalVariable("status", new ObjectType(v9t9.engine.cpu.Status.class.getName()), null, null);
        info.localStatus = lg.getIndex();
        lg = mgen.addLocalVariable("memory", new ObjectType(v9t9.engine.memory.MemoryDomain.class.getName()), null, null);
        info.localMemory = lg.getIndex();
        lg = mgen.addLocalVariable("nInsts", Type.INT, null, null);
        info.localInsts = lg.getIndex();
        lg = mgen.addLocalVariable("nCycles", Type.INT, null, null);
        info.localCycles = lg.getIndex();

        if (settingOptimize.getBoolean()
                && settingOptimizeRegAccess.getBoolean()) {
            lg = mgen.addLocalVariable("wpWordMemory", new ArrayType(
                    Type.SHORT, 1), null, null);
            info.localWpWordMemory = lg.getIndex();
            lg = mgen.addLocalVariable("wpOffset", Type.INT, null, null);
            info.localWpOffset = lg.getIndex();
            lg = mgen.addLocalVariable("temp", Type.SHORT, null, null);
            info.localTemp = lg.getIndex();
        }
        
        info.breakList = new InstructionList();
        info.doneInst = info.breakList.append(new ICONST(1));
        info.breakInst = info.breakList.append(InstructionConstants.NOP);

        // Check interrupt mask, etc. to throw AbortedException if execution should stop
        //
        // If jumping, there is the potential for an infinite loop,
        // so this is a good time to see if we need to stop to handle
        // interrupts, etc.
        info.breakList.append(InstructionConstants.THIS);
        info.breakList.append(new GETFIELD(info.cpuIndex));
        info.breakList.append(info.ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
                "checkInterrupts", Type.VOID, Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));

		return info;
	}

	private void compileLoadCpuState(InstructionFactory ifact,
			InstructionList ilist, CompileInfo info) {
		// init code: read current info into locals
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.cpuIndex));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
	                    "getPC", Type.SHORT, Type.NO_ARGS,
	                    Constants.INVOKEVIRTUAL));
	    ilist.append(new ISTORE(info.localPc));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
	                    "getWP", Type.SHORT, Type.NO_ARGS,
	                    Constants.INVOKEVIRTUAL));
	    ilist.append(new ISTORE(info.localWp));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
	            "getStatus",
	            new ObjectType(v9t9.engine.cpu.Status.class.getName()),
	            Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	    ilist.append(new ASTORE(info.localStatus));
	
	    ilist.append(InstructionConstants.POP); // cpu
	
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.memoryIndex));
	    ilist.append(new ASTORE(info.localMemory));
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.nInstructionsIndex));
	    ilist.append(new ISTORE(info.localInsts));
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.nCyclesIndex));
	    ilist.append(new ISTORE(info.localCycles));
	
	    // clear locals to avoid warnings
	    ilist.append(InstructionConstants.ICONST_0);
	    ilist.append(new ISTORE(info.localVal1));
	    ilist.append(InstructionConstants.ICONST_0);
	    ilist.append(new ISTORE(info.localVal2));
	    ilist.append(InstructionConstants.ICONST_0);
	    ilist.append(new ISTORE(info.localVal3));
	    ilist.append(InstructionConstants.ICONST_0);
	    ilist.append(new ISTORE(info.localEa1));
	    ilist.append(InstructionConstants.ICONST_0);
	    ilist.append(new ISTORE(info.localEa2));
	    
	    if (settingOptimize.getBoolean() && settingOptimizeRegAccess.getBoolean()) {
	    	// localWpOffset and localWpMemory will be established
	    	// by Convert9900ToByteCode.updateWorkspaceVariables
		    ilist.append(InstructionConstants.ICONST_0);
		    ilist.append(new ISTORE(info.localTemp));
	    }

	    Convert9900ToByteCode.updateWorkspaceVariables(ifact, ilist, info);
	}

	private InstructionHandle compileStoreCpuState(
	        InstructionFactory ifact, InstructionList ilist, CompileInfo info) {
	    InstructionHandle first;
	
	    // finish code: write locals into cpu
	    first = ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.cpuIndex));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ILOAD(info.localPc));
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(), "setPC",
	            Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ILOAD(info.localWp));
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(), "setWP",
	            Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ALOAD(info.localStatus));
	    ilist.append(ifact.createInvoke(v9t9.emulator.runtime.Cpu.class.getName(),
	            "setStatus", Type.VOID, new Type[] { new ObjectType(
	                    v9t9.engine.cpu.Status.class.getName()) },
	            Constants.INVOKEVIRTUAL));
	
	    ilist.append(InstructionConstants.POP); // cpu
	
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new ILOAD(info.localInsts));
	    ilist.append(new PUTFIELD(info.nInstructionsIndex));
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new ILOAD(info.localCycles));
	    ilist.append(new PUTFIELD(info.nCyclesIndex));
	
	    return first;
	}

    /**
     * Tell whether the CPU is in a valid state to run compiled code.
     * 
     * @return true compiled code can run
     */
    public boolean validCpuState() {
    	if ((cpu.getWP() & 1) == 1) {
    		return false;
    	}
    	MemoryEntry wpEntry = memory.getEntryAt(cpu.getWP());
    	MemoryEntry wpEndEntry = memory.getEntryAt(cpu.getWP() + 31);
        if (wpEntry != wpEndEntry) {
			return false;
		}
        return wpEntry.hasWriteAccess();
    }


}
