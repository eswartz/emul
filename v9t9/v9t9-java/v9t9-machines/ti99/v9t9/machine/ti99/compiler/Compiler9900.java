/*
  Compiler9900.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.compiler;

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
import org.apache.bcel.generic.IAND;
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
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.TABLESWITCH;
import org.apache.bcel.generic.Type;

import ejs.base.launch.CommandLauncher;
import ejs.base.launch.ProcessClosure;
import ejs.base.properties.IProperty;
import ejs.base.utils.HexUtils;
import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.RawInstruction;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.compiler.ICompiler;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IStatus;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.compiler.CompileInfo;
import v9t9.engine.compiler.CompiledCode;
import v9t9.engine.compiler.CompiledInstInfo;
import v9t9.engine.compiler.CompilerBase;
import v9t9.engine.compiler.FunctionInstructionRangeCompiler;
import v9t9.engine.compiler.InstructionRangeCompiler;
import v9t9.engine.compiler.SerialInstructionRangeCompiler;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.VdpMmio;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.Instruction9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;
import v9t9.machine.ti99.cpu.Status9900;

/**
 * This class compiles 9900 code into Java bytecode. 
 * 
 * @author ejs
 */
public class Compiler9900 extends CompilerBase {
	private Cpu9900 cpu;

    private IMemoryDomain memory;

    private Set<Integer> instSet;

	private ISettingsHandler settings;

	private IProperty compileFunctions;

	private IProperty debugInstructions;

	private IProperty optimize;

	private IProperty optimizeRegAccess;

    static public final SettingSchema settingDumpModuleRomInstructions = new SettingSchema(
    		ISettingsHandler.TRANSIENT,
    		"CompilerDumpModuleRomInstructions", Boolean.FALSE);

    public Compiler9900(Cpu9900 cpu) {
    	super(cpu.getState(), InstructionFactory9900.INSTANCE);
    	this.cpu = cpu;
    	this.settings = Settings.getSettings(cpu);
        
    	compileFunctions = settings.get(ICompiler.settingCompileFunctions);
    	debugInstructions = settings.get(ICompiler.settingDebugInstructions);
    	optimize = settings.get(ICompiler.settingOptimize);
    	optimizeRegAccess = settings.get(ICompiler.settingOptimizeRegAccess);
    	
        this.memory = cpu.getConsole();
    }

    /**
     */
	private void updateStatus(int handler, CompileInfo info) {
		InstructionList ilist = info.ilist;
		InstructionList labellist = null, skiplist = null;
		switch (handler) {
		case IStatus.stset_NONE:
			return;
		case IStatus.stset_ALL:
			// just a note that Status should be up to date, for future work
			return;
		case IStatus.stset_INT:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"setIntMask", Type.VOID, new Type[] { Type.INT },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_BYTE_LAECOP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(InstructionConstants.I2B);
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(InstructionConstants.I2B);
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_BYTE_LAECOP", Type.VOID, new Type[] { Type.BYTE,
							Type.BYTE }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
							Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO_REV:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
							Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO_REV_1:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ICONST(1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
				Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO_REV_2:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ICONST(2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
				Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO_REV_N1:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ICONST(-1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
				Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_ADD_LAECO_REV_N2:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createConstant(-2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_ADD_LAECO", Type.VOID, new Type[] { Type.SHORT,
				Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_SUB_BYTE_LAECOP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(InstructionConstants.I2B);
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(InstructionConstants.I2B);
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_SUB_BYTE_LAECOP", Type.VOID, new Type[] { Type.BYTE,
							Type.BYTE }, Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_SUB_LAECO:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_SUB_LAECO", Type.VOID, new Type[] { Type.SHORT,
							Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_BYTE_CMP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(InstructionConstants.I2B);
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(InstructionConstants.I2B);
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_BYTE_CMP", Type.VOID, new Type[] { Type.BYTE,
							Type.BYTE }, Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_CMP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_CMP", Type.VOID,
					new Type[] { Type.SHORT, Type.SHORT },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_DIV_O:
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
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_O", Type.VOID, new Type[] { Type.BOOLEAN },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_E:
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
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_E", Type.VOID, new Type[] { Type.BOOLEAN },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_LAE:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_LAE", Type.VOID, new Type[] { Type.SHORT },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_LAE_1:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_LAE", Type.VOID, new Type[] { Type.SHORT },
					Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_BYTE_LAEP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(InstructionConstants.I2B);
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_BYTE_LAEP", Type.VOID, new Type[] { Type.BYTE },
					Constants.INVOKEVIRTUAL));
			break;
		case Status9900.stset_BYTE_LAEP_1:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(InstructionConstants.I2B);
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_BYTE_LAEP", Type.VOID, new Type[] { Type.BYTE },
					Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_LAEO:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_LAEO", Type.VOID, new Type[] { Type.SHORT },
					Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_O:
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
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_O", Type.VOID, new Type[] { Type.BOOLEAN },
					Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_SHIFT_LEFT_CO:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_SHIFT_LEFT_CO", Type.VOID, new Type[] { Type.SHORT,
							Type.SHORT }, Constants.INVOKEVIRTUAL));

			break;
		case Status9900.stset_SHIFT_RIGHT_C:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(new ILOAD(info.localVal1));
			ilist.append(new ILOAD(info.localVal2));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_SHIFT_RIGHT_C", Type.VOID, new Type[] { Type.SHORT,
							Type.SHORT }, Constants.INVOKEVIRTUAL));
			break;

		case Status9900.stset_XOP:
			ilist.append(new ALOAD(info.localStatus));
			ilist.append(info.ifact.createInvoke(Status9900.class.getName(),
					"set_X", Type.VOID, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
			break;

		default:
			throw new AssertionError("unhandled status handler " + handler);
		}
	}

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
    public void generateInstruction(int pc, RawInstruction rawins,
            CompileInfo info, CompiledInstInfo ii) {
    	Instruction9900 ins = rawins instanceof Instruction9900 ? (Instruction9900) rawins : new Instruction9900(rawins, cpu.getConsole());
        InstructionList ilist = info.ilist;

        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();

        if (instSet == null) {
			instSet = new java.util.HashSet<Integer>();
		}
        Integer instInt = new Integer(ins.getInst());
        if (!instSet.contains(instInt)) {
            System.out.println("first use of " + 
            		InstTable9900.getInstName(ins.getInst()) +
            		" at "
                    + HexUtils.toHex4(ins.pc));
            instSet.add(instInt);
        }

        // for LIMI >2, always return to get a chance to ping the CPU
        // if (ins.inst == Instruction.Ilimi)
        // return null;

        if (mop1.type == MachineOperand9900.OP_STATUS
                || mop1.type == MachineOperand9900.OP_INST) {
            mop1.type = IMachineOperand.OP_NONE;
            mop1.dest = MachineOperand9900.OP_DEST_FALSE;
        }
        if (mop2.type == MachineOperand9900.OP_STATUS
                || mop2.type == MachineOperand9900.OP_INST) {
            mop2.type = IMachineOperand.OP_NONE;
            mop2.dest = MachineOperand9900.OP_DEST_FALSE;
        }

        /* generate code for the specific opcode */
        InstructionList actlist = Convert9900ToByteCode.getCompileAction(ins, info);
        
        // TODO debug
        ilist.append(new PUSH(info.pgen, HexUtils.toHex4(ins.pc) + " "
                + ins.toString()));
        ilist.append(InstructionConstants.POP);

        if (debugInstructions.getBoolean()) {
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
                    Type.getType(IStatus.class),
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
        info.cycles = 0;
        fetchOperands(ins, pc, info);

        
        // FIXME
        int cycles = 12 + info.cycles;
        	//ins.fetchCycles + ((MachineOperand9900) ins.getOp1()).cycles + ((MachineOperand9900) ins.getOp2()).cycles;
        
        ilist.append(new IINC(info.localCycles, cycles));

        if (debugInstructions.getBoolean()) {
            dumpFull(info, ilist, ins, "dumpBefore", ins.toString());
        }

        /* do pre-instruction status word updates */
        if (ins.getInfo().stsetBefore != IStatus.stset_NONE) {
            updateStatus(ins.getInfo().stsetBefore, info);
        }

        /* execute */
        if (actlist != null)
        	ilist.append(actlist);
        else
        	ilist.append(InstructionConstants.NOP);

        /* do post-instruction status word updates */
        if (ins.getInfo().stsetAfter != IStatus.stset_NONE) {
            updateStatus(ins.getInfo().stsetAfter, info);
        }

        /* save any operands */
        flushOperands(ins, info);

        if (debugInstructions.getBoolean()) {
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
            Instruction9900 ins, String routine, String insString) {
        Type types[];
        BaseMachineOperand mop1 = (BaseMachineOperand) ins.getOp1();
        BaseMachineOperand mop2 = (BaseMachineOperand) ins.getOp2();
        ilist.append(InstructionConstants.THIS);
        if (insString != null) {
            ilist.append(new PUSH(info.pgen, insString));
            types = new Type[] { Type.STRING,
                    Type.SHORT, Type.SHORT,
                    Type.getType(IStatus.class),
                    Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, Type.INT,
                    Type.INT, Type.INT, Type.INT };

        } else {
			types = new Type[] { Type.SHORT, Type.SHORT,
                    Type.getType(IStatus.class),
                    Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, Type.INT,
                    Type.INT, Type.INT, Type.INT };
		}

        ilist.append(new PUSH(info.pgen, ins.pc));
        ilist.append(new ILOAD(info.localWp));
        ilist.append(new ALOAD(info.localStatus));
        int ignore = insString != null ? MachineOperand9900.OP_DEST_KILLED
                : MachineOperand9900.OP_DEST_FALSE;
        if (mop1.type != IMachineOperand.OP_NONE && mop1.dest != ignore) {
            ilist.append(new ILOAD(info.localEa1));
            ilist.append(new ILOAD(info.localVal1));
        } else {
            ilist.append(InstructionConstants.ICONST_0);
            ilist.append(InstructionConstants.ICONST_0);
        }
        if (mop2.type != IMachineOperand.OP_NONE && mop2.dest != ignore) {
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
                .append(info.ifact.createInvoke(v9t9.engine.compiler.CompiledCode.class
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
    private void fetchOperands(Instruction9900 ins, int pc, CompileInfo info) {
        InstructionList ilist = info.ilist;

        MachineOperand9900 mop1 = (MachineOperand9900) ins.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) ins.getOp2();

        /* update PC to current position */
        ilist.append(new PUSH(info.pgen, ins.pc + ins.getSize()));
        ilist.append(new ISTORE(info.localPc));

        if (mop1.type != IMachineOperand.OP_NONE) {
            OperandCompiler9900.compileGetEA(mop1, info.localEa1, info, ins, ins.pc);
        }
        if (mop2.type != IMachineOperand.OP_NONE) {
            OperandCompiler9900.compileGetEA(mop2, info.localEa2, info, ins, ins.pc);
        }
        if (mop1.type != IMachineOperand.OP_NONE
                && mop1.dest != MachineOperand9900.OP_DEST_KILLED) {
            OperandCompiler9900.compileGetValue(mop1, info.localVal1, info.localEa1, info);
        }
        if (mop2.type != IMachineOperand.OP_NONE
                && mop2.dest != MachineOperand9900.OP_DEST_KILLED) {
            OperandCompiler9900.compileGetValue(mop2, info.localVal2, info.localEa2, info);
        }
        if (ins.getInst() == Inst9900.Idiv) {
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
    private void flushOperands(Instruction9900 ins, CompileInfo info) {
        InstructionList ilist = info.ilist;

        MachineOperand9900 mop1 = (MachineOperand9900) ins.getOp1();
        MachineOperand9900 mop2 = (MachineOperand9900) ins.getOp2();

        if (mop1.dest != MachineOperand9900.OP_DEST_FALSE) {
            OperandCompiler9900.compilePutValue(mop1, info.localVal1, info.localEa1, info);
        }
        if (mop2.dest != MachineOperand9900.OP_DEST_FALSE) {
            OperandCompiler9900.compilePutValue(mop2, info.localVal2, info.localEa2, info);

            if (ins.getInst() == Inst9900.Impy || ins.getInst() == Inst9900.Idiv) {
                ilist.append(new ILOAD(info.localEa2));
                ilist.append(new PUSH(info.pgen, 2));
                ilist.append(InstructionConstants.IADD);
                ilist.append(new ILOAD(info.localVal3));
                OperandCompiler9900.compileWriteWord(info, ilist);
            }
        }

        if ((ins.getInfo().writes & v9t9.common.asm.InstInfo.INST_RSRC_CTX) != 0) {
            /* commit changes to cpu before callback */

            /* save status */
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuStateIndex));
    	    ilist.append(info.ifact.createCheckCast(new ObjectType(CpuState9900.class.getName())));
    	    
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.common.cpu.ICpuState.class.getName(),
                    "setStatus", Type.VOID, new Type[] { new ObjectType(
                            IStatus.class.getName()) },
                    Constants.INVOKEINTERFACE));

            /* update PC first */
            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuStateIndex));
    	    ilist.append(info.ifact.createCheckCast(new ObjectType(CpuState9900.class.getName())));

            ilist.append(new PUSH(info.pgen, ins.pc + ins.getSize())); // old value
            ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.CpuState9900.class.getName(),
                    "setPC", Type.VOID, new Type[] { Type.SHORT },
                    Constants.INVOKEVIRTUAL));

            ilist.append(InstructionConstants.THIS);
            ilist.append(new GETFIELD(info.cpuIndex));
    	    ilist.append(info.ifact.createCheckCast(new ObjectType(Cpu9900.class.getName())));

            ilist.append(new ILOAD(info.localWp));
            ilist.append(new ILOAD(info.localPc));
            ilist.append(info.ifact.createInvoke(v9t9.machine.ti99.cpu.Cpu9900.class.getName(),
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

    /**
     * Generate bytecode for the 9900 instructions in our segment of the memory.
     * @param block
     * @param entries 
     */
    public byte[] compile(String className, String baseName, IDecompileInfo highLevel, RawInstruction[] insts, short[] entries) {

        // build generators for the new class
        ClassGen cgen = new ClassGen(className /* class */,
                CompiledCode9900.class.getName() /* superclass */, className
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
            File dir = new File(new File(Settings.getSettings(cpu).getUserSettings().getConfigDirectory()), "compilertmp"); 
            dir.mkdirs();
            File test = new File(dir, baseName + ".class");
            try {
	            FileOutputStream out = new FileOutputStream(test);
	            out.write(bytecode);
	            out.close();
            } catch (IOException e) {
            	System.err.println("Could not write class file to " + test + ": " + e);
            }
            
            if (DEBUG) {
	        	try {
	        		CommandLauncher launch = new CommandLauncher();
	        		launch.showCommand(true);
	        		String classPath = System.getProperty("java.class.path")
						+ ":" + dir.getAbsolutePath();
	        		System.out.println(classPath+"\n"+cgen.getClassName());
	        		Process execute = launch.execute("java", new String[] {
	        				"-cp",
	        				classPath,
	        				"org.apache.bcel.verifier.Verifier",
									cgen.getClassName() + ".class" },
									null,
	        					dir.getAbsolutePath());
	        		ProcessClosure closure = new ProcessClosure(execute, System.out, System.err);
	        		closure.runBlocking();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        return bytecode;
    }

	private void createConstructors(String className, ClassGen cgen,
			InstructionFactory ifact) {
		ConstantPoolGen pgen = cgen.getConstantPool();
		InstructionList ilist;
		MethodGen mgen;
//		// create instruction list for default constructor
//        ilist = new InstructionList();
//        ilist.append(InstructionConstants.THIS);
//        ilist.append(ifact.createInvoke(CompiledCode9900.class.getName(),
//                        "<init>", Type.VOID, Type.NO_ARGS,
//                        Constants.INVOKESPECIAL));
//        ilist.append(InstructionFactory.createReturn(Type.VOID));
//
//        // add public default constructor method to class
//        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, Type.NO_ARGS,
//                new String[] {}, "<init>", className, ilist, pgen);
//        addMethod(mgen, cgen);

        // create instruction list
        ilist = new InstructionList();
        ilist.append(InstructionConstants.THIS);
        ilist.append(InstructionConstants.ALOAD_1);
        ilist.append(ifact.createInvoke(CompiledCode9900.class.getName(),
                "<init>", Type.VOID, new Type[] { Type.getType(IExecutor.class) },
                Constants.INVOKESPECIAL));
        ilist.append(InstructionFactory.createReturn(Type.VOID));

        // add constructor method to class
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
                new Type[] { Type.getType(IExecutor.class) }, new String[] { "exec" }, "<init>",
                className, ilist, pgen);
        addMethod(mgen, cgen);
	}

    private void createRunMethod(String className, RawInstruction[] insts,
    		IDecompileInfo highLevel, 
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
	    ilist.append(info.ifact.createCheckCast(new ObjectType(Cpu9900.class.getName())));

	    ilist.append(info.ifact.createInvoke(ICpu.class.getName(),
        		"getCycleCounts", Type.getType(CycleCounts.class), 
        		Type.NO_ARGS,
                Constants.INVOKEINTERFACE));
	    
        ilist.append(new ILOAD(info.localCycles));
        ilist.append(info.ifact.createInvoke(CycleCounts.class.getName(),
        		"addExecute", Type.VOID, new Type[] { Type.INT },
                Constants.INVOKEVIRTUAL));
        ilist.append(InstructionConstants.ICONST_0);
        ilist.append(new ISTORE(info.localCycles));

        ilist.append(new ILOAD(info.localPc));
        ilist.append(new PUSH(info.pgen, 0xffff));
        ilist.append(new IAND());
        ilist.append(InstructionConstants.ICONST_1);
        ilist.append(InstructionConstants.IUSHR);
        ilist.append(info.sw);

        InstructionRangeCompiler instructionRangeCompiler = null;
        
    	// compile every instruction as a target of a switch(pc) { ...},
    	// where any jump instr comes back to the switch, serial instructions
    	// jump to their next logical instruction, and the last instruction returns true;
    	// any switch() not handled returns false so the interpreter can have a look
        if (compileFunctions.getBoolean()) {
        	instructionRangeCompiler = new FunctionInstructionRangeCompiler(settings);
        } else {
        	instructionRangeCompiler = new SerialInstructionRangeCompiler(settings);
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
		CompileInfo info = new CompileInfo(Settings.getSettings(cpu), pgen, ifact);
        info.ilist = null;
        info.memory = cpu.getConsole();

        info.cpuIndex = pgen.addFieldref(v9t9.engine.compiler.CompiledCode.class.getName(), // className,
                "cpu", Utility.getSignature(v9t9.common.cpu.ICpu.class.getName()));
        info.cpuStateIndex = pgen.addFieldref(v9t9.engine.compiler.CompiledCode.class.getName(), // className,
        		"cpuState", Utility.getSignature(v9t9.common.cpu.ICpuState.class.getName()));
        info.memoryIndex = pgen.addFieldref(v9t9.engine.compiler.CompiledCode.class.getName(), // className,
                "memory", Utility.getSignature(v9t9.common.memory.IMemoryDomain.class.getName()));
        info.cruIndex = pgen.addFieldref(CompiledCode9900.class.getName(), // className,
                "cru", Utility.getSignature(v9t9.engine.hardware.ICruHandler.class.getName()));
        info.nInstructionsIndex = pgen.addFieldref(v9t9.engine.compiler.CompiledCode.class.getName(), // className,
                "nInstructions", Utility.getSignature("int"));
        info.nCyclesIndex = pgen.addFieldref(v9t9.engine.compiler.CompiledCode.class.getName(), // className,
                "nCycles", Utility.getSignature("int"));
        info.vdpIndex = pgen.addFieldref(v9t9.machine.ti99.compiler.CompiledCode9900.class.getName(), // className,
                "vdpMmio", Utility.getSignature(v9t9.engine.memory.VdpMmio.class.getName()));
        info.gplIndex = pgen.addFieldref(v9t9.machine.ti99.compiler.CompiledCode9900.class.getName(), // className,
                "gplMmio", Utility.getSignature(v9t9.engine.memory.GplMmio.class.getName()));

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
        lg = mgen.addLocalVariable("status", Type.getType(Status9900.class), null, null);
        info.localStatus = lg.getIndex();
        lg = mgen.addLocalVariable("memory", Type.getType(IMemoryDomain.class), null, null);
        info.localMemory = lg.getIndex();
        lg = mgen.addLocalVariable("nInsts", Type.INT, null, null);
        info.localInsts = lg.getIndex();
        lg = mgen.addLocalVariable("nCycles", Type.INT, null, null);
        info.localCycles = lg.getIndex();

        if (optimize.getBoolean()
                && optimizeRegAccess.getBoolean()) {
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
        info.breakList.append(info.ifact.createInvoke(v9t9.common.cpu.ICpu.class.getName(),
                "checkInterrupts", Type.VOID, Type.NO_ARGS,
                Constants.INVOKEINTERFACE));

		return info;
	}

	private void compileLoadCpuState(InstructionFactory ifact,
			InstructionList ilist, CompileInfo info) {
		// init code: read current info into locals
	    ilist.append(InstructionConstants.THIS);
	    ilist.append(new GETFIELD(info.cpuStateIndex));
	    ilist.append(info.ifact.createCheckCast(new ObjectType(CpuState9900.class.getName())));

	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.common.cpu.ICpuState.class.getName(),
	                    "getPC", Type.SHORT, Type.NO_ARGS,
	                    Constants.INVOKEINTERFACE));
	    ilist.append(new ISTORE(info.localPc));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.machine.ti99.cpu.CpuState9900.class.getName(),
	                    "getWP", Type.SHORT, Type.NO_ARGS,
	                    Constants.INVOKEVIRTUAL));
	    ilist.append(new ISTORE(info.localWp));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(ifact.createInvoke(v9t9.common.cpu.ICpuState.class.getName(),
	            "getStatus",
	            Type.getType(IStatus.class),
	            Type.NO_ARGS, Constants.INVOKEINTERFACE));
	    ilist.append(ifact.createCheckCast((ReferenceType) Type.getType(Status9900.class))); 
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
	    
	    if (settings.get(ICompiler.settingOptimize).getBoolean() 
	    		&& settings.get(ICompiler.settingOptimizeRegAccess).getBoolean()) {
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
	    
	    ilist.append(new GETFIELD(info.cpuStateIndex));
	    ilist.append(info.ifact.createCheckCast(new ObjectType(CpuState9900.class.getName())));

	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ILOAD(info.localPc));
	    ilist.append(ifact.createInvoke(v9t9.common.cpu.ICpuState.class.getName(), "setPC",
	            Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKEINTERFACE));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ILOAD(info.localWp));
	    ilist.append(ifact.createInvoke(v9t9.machine.ti99.cpu.CpuState9900.class.getName(), "setWP",
	            Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
	
	    ilist.append(InstructionConstants.DUP);
	    ilist.append(new ALOAD(info.localStatus));
	    ilist.append(ifact.createInvoke(v9t9.common.cpu.ICpuState.class.getName(),
	            "setStatus", Type.VOID, new Type[] { new ObjectType(
	                    IStatus.class.getName()) },
	            Constants.INVOKEINTERFACE));
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
    	IMemoryEntry wpEntry = memory.getEntryAt(cpu.getWP());
    	IMemoryEntry wpEndEntry = memory.getEntryAt(cpu.getWP() + 31);
        if (wpEntry != wpEndEntry) {
			return false;
		}
        return wpEntry.hasWriteAccess();
    }


}
