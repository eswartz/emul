/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 30, 2004
 *
 */
package v9t9.cpu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.*;

import v9t9.Machine;
import v9t9.MemoryDomain;
import v9t9.MemoryEntry;

/**
 * This class compiles 9900 code into Java bytecode.
 * 
 * @author ejs
 */
public class Compiler {
    DirectLoader loader;

    Executor exec;

    Cpu cpu;

    Machine machine;

    MemoryDomain memory;

    PrintWriter dump;

    // map of address blocks to CodeBlocks
    static final short BLOCKSIZE = 0x100;   // this size seems best for JVM HotSpot compiler

    Map codeblocks;

    static final boolean doDump = false;
    static final boolean doDumpFull = false;
    
    public Compiler(Executor exec) {
        loader = new DirectLoader();
        this.exec = exec;
        this.cpu = exec.cpu;
        this.machine = cpu.getMachine();
        this.memory = machine.getMemory().CPU;
        codeblocks = new TreeMap();

        if (false)
        try {
            File file = new File("/tmp/compiler.txt");
            dump = new PrintWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            System.exit(1);
        }
    }

    /** Simple-minded loader for constructed classes. */
    protected static class DirectLoader extends SecureClassLoader {
        protected DirectLoader() {
            super();
        }

        protected Class load(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
        if (dump != null)
            dump.close();
    }

    /**
     * Try to build or execute code at the current CPU address.
     * 
     * @return true if all instructions were executed, false if the current
     *         instruction must be emulated.
     */
    public boolean execute() {
        CodeBlock cb = null;
        short pc = cpu.getPC();
        MemoryEntry ent = cpu.memory.map.lookupEntry(memory, pc);
        if (!isCompilable(ent))
            return false;

        /* get a CodeBlock for this memory... */
        Integer blockaddr = new Integer(pc & ~(BLOCKSIZE - 1));
        if ((cb = (CodeBlock) codeblocks.get(blockaddr)) == null
                || !cb.matches(ent)) {
            cb = new CodeBlock(ent, blockaddr.shortValue(), BLOCKSIZE);
            codeblocks.put(blockaddr, cb);
        }

        return cb.run(exec);
    }

    /**
     * @param ent
     * @return
     */
    private boolean isCompilable(MemoryEntry ent) {
        return ent != null && ent.isMapped() && ent.area.read != null
                && ent.area.write == null /* for now */;
    }

    /**
     * @param iblock
     * @param ins
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
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "setIntMask", Type.VOID,
                    new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_BYTE_LAECOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new I2B());
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new I2B());
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_ADD_BYTE_LAECOP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_LAECO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_ADD_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_ADD_LAECO_REV:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_ADD_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_SUB_BYTE_LAECOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new I2B());
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new I2B());
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_SUB_BYTE_LAECOP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_SUB_LAECO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_SUB_LAECO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_BYTE_CMP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new I2B());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new I2B());
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_BYTE_CMP", Type.VOID, new Type[] {
                    Type.BYTE, Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_CMP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_CMP", Type.VOID, new Type[] { Type.SHORT,
                    Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_DIV_O:
            skiplist = new InstructionList();
            skiplist.append(new NOP());
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new IF_ICMPLE(labellist.getStart()));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_O", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_E:
            skiplist = new InstructionList();
            skiplist.append(new NOP());
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IF_ICMPEQ(labellist.getStart()));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_E", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_LAE:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_LAE", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_LAE_1:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_LAE", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_BYTE_LAEP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new I2B());
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_BYTE_LAEP", Type.VOID,
                    new Type[] { Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.st_BYTE_LAEP_1:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new I2B());
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_BYTE_LAEP", Type.VOID,
                    new Type[] { Type.BYTE }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_LAEO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_LAEO", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_O:
            skiplist = new InstructionList();
            skiplist.append(new NOP());
            labellist = new InstructionList();
            labellist.append(new PUSH(info.pgen, 1));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0x8000));
            ilist.append(new IF_ICMPEQ(labellist.getStart()));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new GOTO(skiplist.getStart()));
            ilist.append(labellist);
            ilist.append(skiplist);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_O", Type.VOID,
                    new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_SHIFT_LEFT_CO:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_SHIFT_LEFT_CO", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));

            break;
        case Instruction.st_SHIFT_RIGHT_C:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "set_SHIFT_RIGHT_C", Type.VOID, new Type[] {
                    Type.SHORT, Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;

        case Instruction.st_XOP:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "" + "set_X", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            break;

        default:
            throw new AssertionError("unhandled status handler " + handler);
        }
    }

    /**
     * Get compile-time behavior
     */
    public InstructionList getCompileAction(Instruction ins, CompileInfo info) {
        InstructionList ilist = new InstructionList();
        InstructionList labellist, skiplist;

        switch (ins.inst) {
        case Instruction.Idata:
            return null;
        case Instruction.Ili:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iai:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new IADD());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iandi:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new IAND());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iori:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new IOR());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Ici:
            break;
        case Instruction.Istwp:
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Istst:
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "flatten", Type.SHORT, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Ilwpi:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISTORE(info.localWp));
            break;
        case Instruction.Ilimi:
            return null;
            /*
             ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "ping", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(info.cpuIndex));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getPC", Type.SHORT,
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ISTORE(info.localPc));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getWP", Type.SHORT,
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ISTORE(info.localWp));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getStatus", new ObjectType(v9t9.cpu.Status.class.getName()),
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ASTORE(info.localStatus));

            ilist.append(InstructionConstants.POP); // cpu
            break;
            */
            
            
            
        case Instruction.Iidle:
            //cpu.idle(); // TODO
            break;
        case Instruction.Irset:
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal1));
            //cpu.rset(); // TODO
            break;
        case Instruction.Irtwp:
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new PUSH(info.pgen, 30));
            ilist.append(new IADD());
            ilist.append(new I2S());
            ilist.append(new DUP()); // save WP+30
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new SWAP());
            Compiler.compileReadWord(info, ilist);
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class
                    .getName(), "expand", Type.VOID, new Type[] { Type.SHORT },
                    Constants.INVOKEVIRTUAL));

            ilist.append(new PUSH(info.pgen, -2));
            ilist.append(new IADD());
            ilist.append(new I2S());
            ilist.append(new DUP()); // save WP+28
            Compiler.compileReadWord(info, ilist);
            ilist.append(new ISTORE(info.localPc));

            ilist.append(new PUSH(info.pgen, -2));
            ilist.append(new IADD());
            ilist.append(new I2S()); // use WP+26
            Compiler.compileReadWord(info, ilist);
            ilist.append(new ISTORE(info.localWp));
            break;
        case Instruction.Ickon:
            // TODO
            break;
        case Instruction.Ickof:
            // TODO
            break;
        case Instruction.Ilrex:
            // TODO
            break;
        case Instruction.Iblwp:
            if (false && ins.op1.isConstant()) {
                Compiler.compileReadAbsWord(info, ilist, (short)ins.op1.val);
            } else {
                ilist.append(new ILOAD(info.localVal1));
                ilist.append(new DUP());
                Compiler.compileReadWord(info, ilist);
            }
        	if (false && ins.op1.isConstant()) {
        	    Compiler.compileReadAbsWord(info, ilist, (short)(ins.op1.val+2));
        	} else {
        	    ilist.append(new ISTORE(info.localWp));
        	    ilist.append(new PUSH(info.pgen, 2));
        	    ilist.append(new IADD());
        	    ilist.append(new I2S());
        	    Compiler.compileReadWord(info, ilist);
        	}
            ilist.append(new ISTORE(info.localPc));
            break;

        case Instruction.Ib:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISTORE(info.localPc));
            break;
        case Instruction.Ix:
            ilist.append(new ALOAD(0));
            int execIndex = info.pgen.addFieldref(v9t9.cpu.CompiledCode.class
                    .getName(), "exec", Utility
                    .getSignature(v9t9.cpu.Executor.class.getName()));
            ilist.append(new GETFIELD(execIndex));
            int interpIndex = info.pgen.addFieldref(v9t9.cpu.Executor.class
                    .getName(), "interp", Utility
                    .getSignature(v9t9.cpu.Interpreter.class.getName()));
            ilist.append(new GETFIELD(interpIndex));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Interpreter.class
                    .getName(), "execute", Type.VOID,
                    new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
            break;
        case Instruction.Iclr:
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Ineg:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new INEG());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iinv:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, -1));
            ilist.append(new IXOR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iinc:
            //ilist.append(new ILOAD(info.localVal1));
            //ilist.append(new PUSH(info.pgen, 1));
            //ilist.append(new IADD());
            //list.append(new I2S());
            //ilist.append(new ISTORE(info.localVal1));
            ilist.append(new IINC(info.localVal1, 1));
            break;
        case Instruction.Iinct:
//            ilist.append(new ILOAD(info.localVal1));
//            ilist.append(new PUSH(info.pgen, 2));
//            ilist.append(new IADD());
//            ilist.append(new I2S());
//            ilist.append(new ISTORE(info.localVal1));
            ilist.append(new IINC(info.localVal1, 2));
            break;
        case Instruction.Idec:
//            ilist.append(new ILOAD(info.localVal1));
//            ilist.append(new PUSH(info.pgen, -1));
//            ilist.append(new IADD());
//            ilist.append(new I2S());
//            ilist.append(new ISTORE(info.localVal1));
            ilist.append(new IINC(info.localVal1, -1));
            break;
        case Instruction.Idect:
//            ilist.append(new ILOAD(info.localVal1));
//            ilist.append(new PUSH(info.pgen, -2));
//            ilist.append(new IADD());
//            ilist.append(new I2S());
//            ilist.append(new ISTORE(info.localVal1));
            ilist.append(new IINC(info.localVal1, -2));
            break;
        case Instruction.Ibl:
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new PUSH(info.pgen, 11 * 2));
            ilist.append(new IADD());
            ilist.append(new ILOAD(info.localPc));
            Compiler.compileWriteWord(info, ilist);
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISTORE(info.localPc));
            break;
        case Instruction.Iswpb:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new DUP());
            ilist.append(new PUSH(info.pgen, 8));
            ilist.append(new ISHL());
            ilist.append(new PUSH(info.pgen, 0xff00));
            ilist.append(new IAND());
            ilist.append(new SWAP());
            ilist.append(new PUSH(info.pgen, 8));
            ilist.append(new ISHR());
            ilist.append(new PUSH(info.pgen, 0x00ff));
            ilist.append(new IAND());
            ilist.append(new IOR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iseto:
            ilist.append(new PUSH(info.pgen, -1));
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Iabs:
            skiplist = new InstructionList();
            skiplist.append(new NOP());
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0x8000));
            ilist.append(new IAND());
            ilist.append(new IFEQ(skiplist.getStart()));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new INEG());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            ilist.append(skiplist);
            break;
        case Instruction.Isra:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ISHR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;
        case Instruction.Isrl:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IUSHR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;

        case Instruction.Isla:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ISHL());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;

        case Instruction.Isrc:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IUSHR());

            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 16));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ISUB());
            ilist.append(new ISHL());

            ilist.append(new IOR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));

            break;

        case Instruction.Ijmp:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISTORE(info.localPc));
            break;
        case Instruction.Ijlt:
            compileJump(info, ilist, "isLT");
            break;
        case Instruction.Ijle:
            compileJump(info, ilist, "isLE");
            break;

        case Instruction.Ijeq:
            compileJump(info, ilist, "isEQ");
            break;
        case Instruction.Ijhe:
            compileJump(info, ilist, "isHE");
            break;
        case Instruction.Ijgt:
            compileJump(info, ilist, "isGT");
            break;
        case Instruction.Ijne:
            compileJump(info, ilist, "isNE");
            break;
        case Instruction.Ijnc:
            compileJump(info, ilist, "isC", true);
            break;
        case Instruction.Ijoc:
            compileJump(info, ilist, "isC");
            break;
        case Instruction.Ijno:
            compileJump(info, ilist, "isO", true);
            break;
        case Instruction.Ijl:
            compileJump(info, ilist, "isL");
            break;
        case Instruction.Ijh:
            compileJump(info, ilist, "isH");
            break;

        case Instruction.Ijop:
            compileJump(info, ilist, "isP");
            break;

        case Instruction.Isbo:
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cruIndex));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 1));
            ilist.append(new PUSH(info.pgen, 1));
            ilist.append(info.ifact.createInvoke(v9t9.Cru.class.getName(),
                    "writeBits", Type.VOID, new Type[] { Type.INT, Type.INT,
                            Type.INT }, Constants.INVOKEINTERFACE));
            break;

        case Instruction.Isbz:
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cruIndex));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new PUSH(info.pgen, 1));
            ilist.append(info.ifact.createInvoke(v9t9.Cru.class.getName(),
                    "writeBits", Type.VOID, new Type[] { Type.INT, Type.INT,
                            Type.INT }, Constants.INVOKEINTERFACE));
            break;

        case Instruction.Itb:
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cruIndex));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 1));
            ilist.append(info.ifact.createInvoke(v9t9.Cru.class.getName(),
                    "readBits", Type.INT, new Type[] { Type.INT, Type.INT },
                    Constants.INVOKEINTERFACE));
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Icoc:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IAND());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Iczc:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new PUSH(info.pgen, -1));
            ilist.append(new IXOR());
            ilist.append(new IAND());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Ixor:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IXOR());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Ixop:
            if (false && ins.op1.isConstant()) {
                Compiler.compileReadAbsWord(info, ilist, (short)(ins.op1.val*2+0x40));
            } else {
                ilist.append(new ILOAD(info.localVal1));
                ilist.append(new PUSH(info.pgen, 2));
                ilist.append(new ISHL());
                ilist.append(new PUSH(info.pgen, 0x40));
                ilist.append(new IADD());
                ilist.append(new DUP());
                Compiler.compileReadWord(info, ilist);
            }
        	if (false && ins.op1.isConstant()) {
        	    Compiler.compileReadAbsWord(info, ilist, (short)(ins.op1.val*2+0x42));
        	} else {
        	    ilist.append(new ISTORE(info.localWp));
        	    ilist.append(new PUSH(info.pgen, 2));
        	    ilist.append(new IADD());
        	    ilist.append(new I2S());
        	    Compiler.compileReadWord(info, ilist);
        	}
            ilist.append(new ISTORE(info.localPc));
            break;

        case Instruction.Impy:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new IMUL());
            ilist.append(new DUP());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal3));
            ilist.append(new PUSH(info.pgen, 16));
            ilist.append(new ISHR());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Idiv:
            skiplist = new InstructionList();
            skiplist.append(new NOP());

            ilist.append(new ILOAD(info.localEa2));
            ilist.append(new PUSH(info.pgen, 2));
            ilist.append(new IADD());
            Compiler.compileReadWord(info, ilist);
            ilist.append(new ISTORE(info.localVal3));

            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new IF_ICMPLE(skiplist.getStart()));

            //short low = block.val3;
            //int val = ((block.val2 & 0xffff) << 16) | (low & 0xffff);
            ilist.append(new ILOAD(info.localVal3));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new PUSH(info.pgen, 16));
            ilist.append(new ISHL());
            ilist.append(new IOR());

            ilist.append(new DUP());
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new IDIV());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal2));

            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0xffff));
            ilist.append(new IAND());
            ilist.append(new IREM());
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal3));

            ilist.append(skiplist);
            break;

        case Instruction.Ildcr:
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cruIndex));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new PUSH(info.pgen, 12 * 2));
            ilist.append(new IADD());
            Compiler.compileReadWord(info, ilist);
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.Cru.class.getName(),
                    "writeBits", Type.VOID, new Type[] { Type.INT, Type.INT,
                            Type.INT }, Constants.INVOKEINTERFACE));
            break;

        case Instruction.Istcr:
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cruIndex));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new PUSH(info.pgen, 12 * 2));
            ilist.append(new IADD());
            Compiler.compileReadWord(info, ilist);
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(info.ifact.createInvoke(v9t9.Cru.class.getName(),
                    "readBits", Type.INT, new Type[] { Type.INT, Type.INT },
                    Constants.INVOKEINTERFACE));
            ilist.append(new I2S());
            ilist.append(new ISTORE(info.localVal1));
            break;

        case Instruction.Iszc:
        case Instruction.Iszcb:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new PUSH(info.pgen, -1));
            ilist.append(new IXOR());
            ilist.append(new IAND());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Is:
        case Instruction.Isb:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISUB());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Ic:
        case Instruction.Icb:
            break;

        case Instruction.Ia:
        case Instruction.Iab:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new IADD());
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Imov:
        case Instruction.Imovb:
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new ISTORE(info.localVal2));
            break;

        case Instruction.Isoc:
        case Instruction.Isocb:
            ilist.append(new ILOAD(info.localVal2));
            ilist.append(new ILOAD(info.localVal1));
            ilist.append(new IOR());
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
    private void compileJump(CompileInfo info, InstructionList ilist,
            String test, boolean invert) {
        InstructionList skiplist;
        skiplist = new InstructionList();
        skiplist.append(new NOP());
        ilist.append(new ALOAD(info.localStatus));
        ilist.append(info.ifact.createInvoke(v9t9.cpu.Status.class.getName(),
                test, Type.BOOLEAN, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        if (invert)
            ilist.append(new IFNE(skiplist.getStart()));
        else
            ilist.append(new IFEQ(skiplist.getStart()));
        ilist.append(new ILOAD(info.localVal1));
        ilist.append(new ISTORE(info.localPc));
        ilist.append(skiplist);
    }

    private void compileJump(CompileInfo info, InstructionList ilist,
            String test) {
        compileJump(info, ilist, test, false);
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
    Set instSet;
    
    public InstInfo generateInstruction(short pc, Instruction ins,
            CompileInfo info, InstInfo ii) {
        InstructionHandle ih;
        InstructionList ilist = info.ilist;

        if (instSet == null)
            instSet = new java.util.HashSet();
        Integer instInt = new Integer(ins.inst);
        if (!instSet.contains(instInt)) {
            System.out.println("first use of " + ins.name + " at " + v9t9.Globals.toHex4(ins.pc));
            instSet.add(instInt);
        }
        
        // for LIMI >2, always return to get a chance to ping the CPU
        //if (ins.inst == Instruction.Ilimi)
        //    return null;
        
        if (ins.op1.type == Operand.OP_STATUS
                || ins.op1.type == Operand.OP_INST) {
            ins.op1.type = Operand.OP_NONE;
            ins.op1.dest = Operand.OP_DEST_FALSE;
        }
        if (ins.op2.type == Operand.OP_STATUS
                || ins.op2.type == Operand.OP_INST) {
            ins.op2.type = Operand.OP_NONE;
            ins.op2.dest = Operand.OP_DEST_FALSE;
        }

        /* generate code for the specific opcode */
        InstructionList actlist = getCompileAction(ins, info);
        if (actlist == null)
            return null;

        // TODO debug
        //ilist.append(new PUSH(info.pgen, v9t9.Globals.toHex4(ins.pc) + " " + ins.toString()));
        //ilist.append(new POP());

        if (doDump) {
            ilist.append(new ALOAD(0));
            ilist.append(new PUSH(info.pgen, pc));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.vdpIndex));
            ilist.append(info.ifact.createInvoke(v9t9.vdp.Vdp.class.getName(),
                    "getAddr", Type.SHORT, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.gplIndex));
            ilist.append(info.ifact.createInvoke(v9t9.Gpl.class.getName(),
                    "getAddr", Type.SHORT, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.CompiledCode.class
                    .getName(), "dump", Type.VOID,
                    new Type[] { 
                    		Type.SHORT, Type.SHORT,
                            new ObjectType(v9t9.cpu.Status.class.getName()),
                            Type.SHORT, Type.SHORT },
                    Constants.INVOKEVIRTUAL));
        }

        // update # instructions executed
        /*
        ilist.append(new ALOAD(0));
        ilist.append(new DUP());
        ilist.append(new GETFIELD(info.nInstructionsIndex));
        ilist.append(InstructionConstants.ICONST_1);
        ilist.append(new IADD());
        ilist.append(new PUTFIELD(info.nInstructionsIndex));
        */
        ilist.append(new IINC(info.localInsts, 1));
        
        // on a jump, be sure to do a CPU ping to handle interrupts, etc.
        if (ins.jump != Instruction.INST_JUMP_FALSE) {
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "ping", Type.VOID, Type.NO_ARGS,
                    Constants.INVOKEVIRTUAL));
        }
        
        /* compose operand values and instruction timings */
        fetchOperands(ins, pc, info);

        if (doDumpFull) {
	        dumpFull(info, ilist, ins, "dumpBefore", ins.toString());
        }
        
        /* do pre-instruction status word updates */
        if (ins.stsetBefore != Instruction.st_NONE) {
            updateStatus(ins.stsetBefore, info);
        }

        /* execute */
        ilist.append(actlist);

        /* do post-instruction status word updates */
        if (ins.stsetAfter != Instruction.st_NONE) {
            updateStatus(ins.stsetAfter, info);
        }

        /* save any operands */
        flushOperands(ins, info);

        if (doDumpFull) {
            dumpFull(info, ilist, ins, "dumpAfter", null);
        }
        
        ilist.append(new NOP());
        
        ii.chunk = ilist;
        ii.ins = ins;
        return ii;
    }

    private void dumpFull(CompileInfo info, InstructionList ilist, Instruction ins, 
            String routine, String insString) {
        Type types[];
        ilist.append(new ALOAD(0));
        if (insString != null) {
            ilist.append(new PUSH(info.pgen, insString));
            types = new Type[] { new ObjectType(String.class.getName()),
                    Type.SHORT, Type.SHORT, 
                    new ObjectType(v9t9.cpu.Status.class.getName()),
                    Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, 
                    Type.INT, Type.INT, Type.INT, Type.INT }; 

        } else
            types = new Type[] { 
                Type.SHORT, Type.SHORT, 
                new ObjectType(v9t9.cpu.Status.class.getName()),
                Type.SHORT, Type.SHORT, Type.SHORT, Type.SHORT, 
                Type.INT, Type.INT, Type.INT, Type.INT }; 
            
        ilist.append(new PUSH(info.pgen, ins.pc));
        ilist.append(new ILOAD(info.localWp));
        ilist.append(new ALOAD(info.localStatus));
        int ignore = insString!=null ? Operand.OP_DEST_KILLED : Operand.OP_DEST_FALSE;
        if (ins.op1.type != Operand.OP_NONE && ins.op1.dest != ignore) {
            ilist.append(new ILOAD(info.localEa1));
            ilist.append(new ILOAD(info.localVal1));
        } else {
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new PUSH(info.pgen, 0));
        }
        if (ins.op2.type != Operand.OP_NONE && ins.op2.dest != ignore) {
            ilist.append(new ILOAD(info.localEa2));
            ilist.append(new ILOAD(info.localVal2));
        } else {
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new PUSH(info.pgen, 0));
        }
        ilist.append(new PUSH(info.pgen, ins.op1.type));
        ilist.append(new PUSH(info.pgen, ins.op1.dest));
        ilist.append(new PUSH(info.pgen, ins.op2.type));
        ilist.append(new PUSH(info.pgen, ins.op2.dest));
        ilist.append(info.ifact.createInvoke(v9t9.cpu.CompiledCode.class
                .getName(), routine, Type.VOID,
                types,
                Constants.INVOKEVIRTUAL));
    }

    /**
     * Fetch operands for instruction (compile time)
     * 
     * @param ins
     * @param pc
     * @param info
     */
    private void fetchOperands(Instruction ins, short pc, CompileInfo info) {
        InstructionList ilist = info.ilist;

        /* update PC to current position */
        /*
         * // pc += ins.size; ilist.append(new ILOAD(info.localPc));
         * ilist.append(new PUSH(info.pgen, ins.size)); ilist.append(new
         * IADD()); ilist.append(new I2S()); ilist.append(new
         * ISTORE(info.localPc));
         */
        ilist.append(new PUSH(info.pgen, ins.pc + ins.size));
        ilist.append(new ISTORE(info.localPc));

        if (ins.op1.type != Operand.OP_NONE) {
            ins.op1.compileGetEA(info.localEa1, info, ins.pc);
        }
        if (ins.op2.type != Operand.OP_NONE) {
            ins.op2.compileGetEA(info.localEa2, info, ins.pc);
        }
        if (ins.op1.type != Operand.OP_NONE
                && ins.op1.dest != Operand.OP_DEST_KILLED) {
            ins.op1.compileGetValue(info.localVal1, info.localEa1, info);
        }
        if (ins.op2.type != Operand.OP_NONE
                && ins.op2.dest != Operand.OP_DEST_KILLED) {
            ins.op2.compileGetValue(info.localVal2, info.localEa2, info);
        }
        if (ins.inst == Instruction.Idiv) {
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

        if (ins.op1.dest != Operand.OP_DEST_FALSE) {
            ilist.append(new ILOAD(info.localEa1));
            ilist.append(new ILOAD(info.localVal1));
            if (ins.op1.byteop) {
                ilist.append(new I2B());
                Compiler.compileWriteByte(info, ilist);
            } else
                Compiler.compileWriteWord(info, ilist);
        }
        if (ins.op2.dest != Operand.OP_DEST_FALSE) {
            ilist.append(new ILOAD(info.localEa2));
            ilist.append(new ILOAD(info.localVal2));
            if (ins.op2.byteop) {
                ilist.append(new I2B());
                Compiler.compileWriteByte(info, ilist);
            } else {
                Compiler.compileWriteWord(info, ilist);

                if (ins.inst == Instruction.Impy
                        || ins.inst == Instruction.Idiv) {
                    ilist.append(new ILOAD(info.localEa2));
                    ilist.append(new PUSH(info.pgen, 2));
                    ilist.append(new IADD());
                    ilist.append(new ILOAD(info.localVal3));
                    Compiler.compileWriteWord(info, ilist);
                }
            }
        }

        if ((ins.writes & Instruction.INST_RSRC_CTX) != 0) {
            /* commit changes to cpu before callback */

            /* save status */
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "setStatus", Type.VOID, new Type[] { new ObjectType(
                            v9t9.cpu.Status.class.getName()) },
                    Constants.INVOKEVIRTUAL));

            /* update PC first */
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new PUSH(info.pgen, ins.pc + ins.size)); // absolute
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "setPC", Type.VOID, new Type[] { Type.SHORT },
                    Constants.INVOKEVIRTUAL));

            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(info.cpuIndex));
            ilist.append(new ILOAD(info.localPc));
            ilist.append(new ILOAD(info.localWp));
            ilist.append(info.ifact.createInvoke(v9t9.cpu.Cpu.class.getName(),
                    "contextSwitch", Type.VOID, new Type[] { Type.SHORT,
                            Type.SHORT }, Constants.INVOKEVIRTUAL));

        }
    }

    /** Read a register */
    public static void compileGetRegEA(CompileInfo info, int reg) {
        InstructionList ilist = info.ilist;
        if (reg < 0 || reg > 15)
            throw new AssertionError("bad register " + reg);
        ilist.append(new ILOAD(info.localWp));
        if (reg != 0) {
            ilist.append(new PUSH(info.pgen, reg * 2));
            ilist.append(new IADD());
            ilist.append(new I2S());
        }
    }


    /**
     * Read a word from the given constant address
     */
    public static void compileReadAbsWord(CompileInfo info, InstructionList ilist, short addr) {
        ilist.append(new PUSH(info.pgen, info.memory.CPU.flatReadWord(addr)));
    }

    /**
     * Read a byte from the given constant address
     */
    public static void compileReadAbsByte(CompileInfo info, InstructionList ilist, short addr) {
        ilist.append(new PUSH(info.pgen, info.memory.CPU.flatReadByte(addr)));
    }

    /**
     * Read a word from the address on the stack.
     */
    public static void compileReadWord(CompileInfo info, InstructionList ilist) {
        ilist.append(new ALOAD(info.localMemory));
        ilist.append(new SWAP());
        ilist.append(info.ifact.createInvoke(v9t9.MemoryDomain.class.getName(),
                "readWord", Type.SHORT, new Type[] { Type.INT },
                Constants.INVOKEVIRTUAL));
    }

    /**
     * Read a byte from the address on the stack.
     */
    public static void compileReadByte(CompileInfo info, InstructionList ilist) {
        ilist.append(new ALOAD(info.localMemory));
        ilist.append(new SWAP());
        ilist.append(info.ifact.createInvoke(v9t9.MemoryDomain.class.getName(),
                "readByte", Type.BYTE, new Type[] { Type.INT },
                Constants.INVOKEVIRTUAL));

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

        ilist.append(new DUP_X2());
        ilist.append(new POP());
        ilist.append(info.ifact.createInvoke(v9t9.MemoryDomain.class.getName(),
                "writeWord", Type.VOID, new Type[] { Type.INT, Type.SHORT },
                Constants.INVOKEVIRTUAL));

    }

    /**
     * Write a byte from the value,address on the stack.
     */
    public static void compileWriteByte(CompileInfo info, InstructionList ilist) {
        ilist.append(new ALOAD(info.localMemory));
        ilist.append(new DUP_X2());
        ilist.append(new POP());
        ilist.append(info.ifact.createInvoke(v9t9.MemoryDomain.class.getName(),
                "writeByte", Type.VOID, new Type[] { Type.INT, Type.BYTE },
                Constants.INVOKEVIRTUAL));
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
    
   /** Generate bytecode for the 9900 instructions in our
     * segment of the memory.
     * @return true for success, false for failure
     */
    public static byte[] compile(Executor exec, CodeBlock block) {
        String className = block.className;
        MemoryEntry ent = block.ent;
        int addr = block.addr;
        int size = block.size;
        
        block.clear();
        System.out.println("compiling code block at >" + v9t9.Globals.toHex4(block.addr) + ":" + 
                v9t9.Globals.toHex4(block.size) + "/" + ent);
    
        try {
            // build generators for the new class
            ClassGen cgen = new ClassGen(className /*class*/, 
                    CompiledCode.class.getName() /*superclass*/, 
                    className + ".java" /*filename*/,
                    Constants.ACC_PUBLIC, 
                    null /*interfaces*/);
            InstructionFactory ifact = new InstructionFactory(cgen);
            ConstantPoolGen pgen = cgen.getConstantPool();
            
            InstructionList ilist;
            MethodGen mgen;
            
            // create instruction list for default constructor
            ilist = new InstructionList();
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(ifact.createInvoke(CompiledCode.class.getName(), "<init>", 
                    Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
            ilist.append(InstructionFactory.createReturn(Type.VOID));
        
            // add public default constructor method to class
            mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
    	            Type.NO_ARGS, new String[] { }, "<init>", className, ilist, pgen);
            addMethod(mgen, cgen);
    
            // create instruction list
            ilist = new InstructionList();
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(InstructionConstants.ALOAD_1);
            ilist.append(ifact.createInvoke(CompiledCode.class.getName(), "<init>", 
                    Type.VOID,
                    new Type[] { new ObjectType(v9t9.cpu.Executor.class.getName()) },
                    Constants.INVOKESPECIAL));
            ilist.append(InstructionFactory.createReturn(Type.VOID));
        
            // add constructor method to class
            mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
    	            new Type[] { new ObjectType(v9t9.cpu.Executor.class.getName()) },
    	            new String[] { "exec" }, "<init>", className, ilist, pgen);
            addMethod(mgen, cgen);
    
            // create public boolean run() method
            ilist = new InstructionList();
            CompileInfo info = new CompileInfo(pgen, ifact);
            info.ilist = null;
            info.memory = exec.cpu.memory;
            
            int cpuIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "cpu",
                    Utility.getSignature(v9t9.cpu.Cpu.class.getName()));
            int memoryIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "memory",
                    Utility.getSignature(v9t9.MemoryDomain.class.getName()));
            int cruIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "cru",
                    Utility.getSignature(v9t9.Cru.class.getName()));
            int nInstsIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "nInstructions", 
                    Utility.getSignature("int"));
            int vdpIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "vdp",
                    Utility.getSignature(v9t9.vdp.Vdp.class.getName()));
            int gplIndex = pgen.addFieldref(v9t9.cpu.CompiledCode.class.getName(), //className, 
                    "gpl",
                    Utility.getSignature(v9t9.Gpl.class.getName()));
            
            info.cpuIndex = cpuIndex;
            info.memoryIndex = memoryIndex;
            info.cruIndex = cruIndex;
            info.nInstructionsIndex = nInstsIndex;
            info.vdpIndex = vdpIndex;
            info.gplIndex = gplIndex;
            
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
            lg = mgen.addLocalVariable("status", new ObjectType(v9t9.cpu.Status.class.getName()), null, null);
            info.localStatus = lg.getIndex();
            lg = mgen.addLocalVariable("memory", new ObjectType(v9t9.MemoryDomain.class.getName()), null, null);
            info.localMemory = lg.getIndex();
            lg = mgen.addLocalVariable("nInsts", Type.INT, null, null);
            info.localInsts = lg.getIndex();
            
            // init code: read current info into locals
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(cpuIndex));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getPC", Type.SHORT,
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ISTORE(info.localPc));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getWP", Type.SHORT,
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ISTORE(info.localWp));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "getStatus", new ObjectType(v9t9.cpu.Status.class.getName()),
                    Type.NO_ARGS, 
                    Constants.INVOKEVIRTUAL));
            ilist.append(new ASTORE(info.localStatus));

            ilist.append(InstructionConstants.POP);	// cpu
            
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.memoryIndex));
            ilist.append(new ASTORE(info.localMemory));	        
            ilist.append(new ALOAD(0));
            ilist.append(new GETFIELD(info.nInstructionsIndex));
            ilist.append(new ISTORE(info.localInsts));
    
            // clear locals to avoid warnings
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal1));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal2));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localVal3));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localEa1));
            ilist.append(new PUSH(info.pgen, 0));
            ilist.append(new ISTORE(info.localEa2));
    
            // while (1) loop
            InstructionList loopEnd = new InstructionList();
            loopEnd.append(new NOP());
            
            // get PC entry points for every possible address
            // (even ODD ones; the switch statement requires contiguous entries)
            int numinsts = size;
            int[] pcs = new int[numinsts];
            for (int i = 0; i < numinsts; i++) {
                pcs[i] = addr + i;
            }     

            // discover the instructions for the block
            Instruction insts[] = new Instruction[numinsts/2];
            for (int i = 0; i < numinsts/2; i++) {
                short pc = (short)(addr + i*2);
                short op = exec.cpu.console.readWord(pc);
                Instruction ins = new Instruction(op, pc, exec.cpu.console);
                insts[i] = ins;
            }

            // remove spurious status setters
            peephole_status(insts, numinsts/2);
            
            InstructionList breakList = new InstructionList();
            info.doneInst = breakList.append(new ICONST(1));
            info.breakInst = breakList.append(new NOP());
            
            // switch(...) { ... }
            Select sw = new TABLESWITCH(pcs, new InstructionHandle[numinsts], null);
            info.switchInst = ilist.append(new ILOAD(info.localPc));
            ilist.append(sw);
            
            // generate all the code for each addr 
            InstInfo[] chunks = new InstInfo[numinsts];
            for (int i = 0; i < numinsts; i+=2) {
                chunks[i] = new InstInfo();
                info.ilist = new InstructionList();
                chunks[i] = exec.compiler.generateInstruction((short) (addr + i), insts[i/2], info, chunks[i]);

                // lifetime calculations
                if (chunks[i] != null)
                    peephole_lifetimes(info, chunks[i]);
            }
            
            // have each chunk branch to appropriate instruction in list
            for (int i = 0; i < numinsts; i+=2) {
                InstInfo ii = chunks[i];
                if (ii != null) {
                    if (ii.ins.jump == Instruction.INST_JUMP_FALSE) {
    	                short target = (short)(ii.ins.pc + ii.ins.size);
                        int index = (target - addr);
    	                if (target < addr + size && index >= 0 && index < numinsts && chunks[index] != null) {
                            ii.chunk.append(new GOTO(chunks[index].chunk.getStart()));
    	                }
    	                else {
    	                    ii.chunk.append(new PUSH(pgen, 1));
    	                    ii.chunk.append(new GOTO(info.breakInst));
    	                }
                    }
                    else {
                        ii.chunk.append(new GOTO(info.switchInst));
                    }
                }
            }
    
            // complete switch table
            for (int i = 0; i < numinsts; i++) {
                if (i % 2 == 0) {
                    InstructionHandle ih = null;
                    if (chunks[i] != null)
                        ih = chunks[i].chunk.getStart();
                    if (ih != null) {
                        ilist.append(chunks[i].chunk);
                        sw.setTarget(i, ih);
                    }
                    else {
                        ih = ilist.append(new PUSH(pgen, 0));
                        ilist.append(new GOTO(info.breakInst));
                        sw.setTarget(i, ih);
                    }
                }
                else {
                    sw.setTarget(i, sw.getTargets()[i-1]);
                }
            }
    
            // default for switch
            InstructionList cleanupList = new InstructionList();
            InstructionHandle cleanupInst = cleanupList.append(new NOP());
            
            sw.setTarget(ilist.append(new ICONST(0)));
            ilist.append(new GOTO(cleanupInst));
            
            // end of switch
            ilist.append(breakList);
            
            // ALL PATHS leave return code on stack
            ilist.append(cleanupList);
            
            // finish code: write locals into cpu
            ilist.append(InstructionConstants.ALOAD_0);
            ilist.append(new GETFIELD(cpuIndex));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(new ILOAD(info.localPc));
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "setPC", Type.VOID,
                    new Type[] { Type.SHORT }, 
                    Constants.INVOKEVIRTUAL));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(new ILOAD(info.localWp));
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "setWP", Type.VOID,
                    new Type[] { Type.SHORT }, 
                    Constants.INVOKEVIRTUAL));
    
            ilist.append(InstructionConstants.DUP);
            ilist.append(new ALOAD(info.localStatus));
            ilist.append(ifact.createInvoke(v9t9.cpu.Cpu.class.getName(), 
                    "setStatus", Type.VOID,
                    new Type[] { new ObjectType(v9t9.cpu.Status.class.getName()) },
                    Constants.INVOKEVIRTUAL));

            ilist.append(InstructionConstants.POP);	// cpu

            ilist.append(new ALOAD(0));
            ilist.append(new ILOAD(info.localInsts));
            ilist.append(new PUTFIELD(info.nInstructionsIndex));

            // return, with return code previously placed on stack
            ilist.append(InstructionConstants.IRETURN);
    
            // add method
            mgen = new MethodGen(Constants.ACC_PUBLIC + Constants.ACC_FINAL, Type.BOOLEAN,
    	            Type.NO_ARGS, 
    	            null, "run", className, ilist, pgen);
            addMethod(mgen, cgen);
    
            // return bytecode of completed class
            byte[] bytecode = cgen.getJavaClass().getBytes();
            
            if (true) {
                File test = new File(block.baseName + ".class");
                FileOutputStream out = new FileOutputStream(test);
                out.write(bytecode);
                out.close();
            }
            return bytecode;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
            return null;
        }    
    }

   
/**
 * @param insts
 * @param i
 */
  
private static void peephole_status(Instruction[] insts, int numinsts) {
    Instruction prev = null;
    int prevBits = 0;
    int i;
    i = 0;
    while (i < numinsts) {
        Instruction ins = insts[i];
        int bits = Instruction.getStatusBits(ins.stsetBefore) 
            | Instruction.getStatusBits(ins.stsetAfter); 
        if (prev != null) {
            if (//prev.stsetBefore != Instruction.st_INT && 
                 //   prev.stsetAfter != Instruction.st_INT &&
                    (prevBits & ~bits) == 0) {
                // prev's bits are totally ignored
                //System.out.println("ignoring bits set by '"+prev+"' due to '"+ins+"'");
                prev.stsetBefore = Instruction.st_NONE;
                prev.stsetAfter = Instruction.st_NONE;
            }
        }
        prev = ins;
        prevBits = bits;
        i += ins.size/2;
    }
    
}

/**
 * @param insts
 * @param i
 */
private static void peephole_lifetimes(CompileInfo info, InstInfo ii) {
    final class Lifetime
    {
        private int start, end;
        private InstructionHandle endHandle;
        private InstructionHandle startHandle;
        
        public Lifetime() {
            start = end = -1;
        }
        public void setUse(int index, InstructionHandle handle) {
            if (start == -1) {
                start = index;
                startHandle = handle;
            }
            end = index;
            endHandle = handle;
        }
    }
    
    Map lifetimes = new HashMap();
    lifetimes.put(new Integer(info.localEa1), new Lifetime());
    lifetimes.put(new Integer(info.localEa2), new Lifetime());
    lifetimes.put(new Integer(info.localVal1), new Lifetime());
    lifetimes.put(new Integer(info.localVal2), new Lifetime());
    
    InstructionHandle inst;
    for (inst = ii.chunk.getStart(); inst != null; inst = inst.getNext()) {
        
        if (inst == ii.chunk.getEnd())
            break;
    }
}


}

