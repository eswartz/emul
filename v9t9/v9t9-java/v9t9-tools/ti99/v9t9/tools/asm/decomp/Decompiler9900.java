/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.decomp;



import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.tools.asm.assembler.inst9900.AsmInstructionFactory9900;

public class Decompiler9900 extends Decompiler {
	static IMemory memory = new Memory();
	static {
		memory.addDomain(IMemoryDomain.NAME_CPU, new MemoryDomain(IMemoryDomain.NAME_CPU));
	}
    public Decompiler9900() {
		super(memory, AsmInstructionFactory9900.INSTANCE, 
				new CpuState9900(memory.getDomain(IMemoryDomain.NAME_CPU)));
    }
}
