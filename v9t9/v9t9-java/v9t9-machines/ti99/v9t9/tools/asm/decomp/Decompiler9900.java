/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.decomp;



import v9t9.emulator.runtime.cpu.CpuState9900;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.StockMemoryModel;
import v9t9.tools.asm.assembler.inst9900.InstructionFactory9900;

public class Decompiler9900 extends Decompiler {
	static StockMemoryModel model = new StockMemoryModel();

    public Decompiler9900() {
		super(model, InstructionFactory9900.INSTANCE, 
				new CpuState9900(model.getMemory().getDomain(MemoryDomain.NAME_CPU)));
    }
}
