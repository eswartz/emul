/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import java.util.Collection;
import java.util.Map;

import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.RamArea;
import v9t9.tools.decomp.ICodeProvider;
import v9t9.tools.decomp.IDecompileInfo;
import v9t9.tools.llinst.HighLevelInstruction;
import v9t9.tools.llinst.MemoryRanges;
import v9t9.tools.llinst.ParseException;
import v9t9.tools.llinst.Routine;

public abstract class BaseTopDownPhaseTest extends BaseTest implements ICodeProvider
{
	protected HighLevelCodeInfo highLevel;
	protected IDecompileInfo decompileInfo;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        //model = new StandardConsoleMemoryModel(new DummyClient(), memory);
        MemoryEntry romEntry = new MemoryEntry("CPU ROM", CPU, 0, 0x2000,
                new RamArea(0x2000)
                );
		memory.addAndMap(romEntry);
        memory.addAndMap(new MemoryEntry("CPU RAM", CPU, 0x8000, 0x400,
                new RamArea(0x400)
                ));
        highLevel = new HighLevelCodeInfo(CPU);
        highLevel.getMemoryRanges().addRange(0, 0x2000, true);
        highLevel.getMemoryRanges().addRange(0x8300, 0x100, true);
        
        decompileInfo = highLevel.getDecompileInfo();
    }
    
    protected void parse(MemoryDomain cpu, int pc, int wp, String[] insts) throws ParseException {
	    HighLevelInstruction first = null;
	    HighLevelInstruction prev = null;
	    for (String element : insts) {
	    	HighLevelInstruction inst = createHLInstruction(pc, wp, element);
	        highLevel.getLLInstructions().put(new Integer(inst.pc), inst);
	        highLevel.addInstruction(inst);
	        cpu.flatWriteWord(pc, inst.opcode);
	        if (inst.inst != InstructionTable.Idata) {
				pc += 2;
			}
	        if (((MachineOperand) inst.op1).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((MachineOperand) inst.op1).immed);
	            pc += 2;
	        }
	        if (((MachineOperand) inst.op2).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((MachineOperand) inst.op2).immed);
	            pc += 2;
	        }
	        if (prev != null) {
				prev.setNext(inst);
			}
	        prev = inst;
	        if (first == null) {
				first = inst;
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
    
    public Map<Integer, HighLevelInstruction> getLLInstructions() {
    	return decompileInfo.getLLInstructions();
    }

	public RawInstruction getInstruction(int addr) {
		return decompileInfo.getInstruction(addr);
	}
}
