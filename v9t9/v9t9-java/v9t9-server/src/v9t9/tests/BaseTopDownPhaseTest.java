/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import java.util.Collection;
import java.util.Map;

import v9t9.emulator.runtime.cpu.CpuState9900;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockRamArea;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.common.MemoryRanges;
import v9t9.tools.asm.decomp.IHighLevelInstruction;
import v9t9.tools.asm.decomp.ICodeProvider;
import v9t9.tools.asm.decomp.IDecompileInfo;
import v9t9.tools.asm.decomp.Routine;

public abstract class BaseTopDownPhaseTest extends BaseTest implements ICodeProvider
{
	protected HighLevelCodeInfo highLevel;
	protected IDecompileInfo decompileInfo;
	protected CpuState9900 state;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        //model = new StandardConsoleMemoryModel(new DummyClient(), memory);
        MemoryEntry romEntry = new MemoryEntry("CPU ROM", CPU, 0, 0x2000,
                new StockRamArea(0x2000)
                );
		memory.addAndMap(romEntry);
        memory.addAndMap(new MemoryEntry("CPU RAM", CPU, 0x8000, 0x400,
                new StockRamArea(0x400)
                ));
        state = new CpuState9900(CPU);
        highLevel = new HighLevelCodeInfo(state);
        highLevel.getMemoryRanges().addRange(0, 0x2000, true);
        highLevel.getMemoryRanges().addRange(0x8300, 0x100, true);
        
        decompileInfo = highLevel.getDecompileInfo();
    }
    
    protected void parse(MemoryDomain cpu, int pc, int wp, String[] insts) throws ParseException {
	    IHighLevelInstruction first = null;
	    IHighLevelInstruction prev = null;
	    for (String element : insts) {
	    	IHighLevelInstruction hinst = createHLInstruction(pc, wp, element);
	    	RawInstruction inst = hinst.getInst();
	        highLevel.getLLInstructions().put(new Integer(inst.pc), hinst);
	        highLevel.addInstruction(hinst);
	        cpu.flatWriteWord(pc, (short) inst.opcode);
	        if (inst.getInst() != InstTableCommon.Idata) {
				pc += 2;
			}
	        if (((MachineOperand) inst.getOp1()).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((BaseMachineOperand) inst.getOp1()).immed);
	            pc += 2;
	        }
	        if (((MachineOperand) inst.getOp2()).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((BaseMachineOperand) inst.getOp2()).immed);
	            pc += 2;
	        }
	        if (prev != null) {
				prev.setNext(hinst);
			}
	        prev = hinst;
	        if (first == null) {
				first = hinst;
			}
	    }
	    while (first != null) {
	        System.out.println("inst: "+ first);
	        first = first.getNext();
	    }
	}
    
 	public MemoryDomain getCPUMemory() {
        return CPU;
    }

    public MemoryRanges getRanges() {
        return decompileInfo.getMemoryRanges();
    }
    
    public Collection<Routine> getRoutines() {
        return decompileInfo.getRoutines();
    }
    
    public Map<Integer, IHighLevelInstruction> getLLInstructions() {
    	return decompileInfo.getLLInstructions();
    }

	public RawInstruction getInstruction(int addr) {
		return decompileInfo.getInstruction(addr);
	}
}
