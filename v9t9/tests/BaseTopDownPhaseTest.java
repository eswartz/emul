/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import java.util.Collection;
import java.util.Map;

import v9t9.emulator.runtime.HighLevelCodeInfo;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StandardConsoleMemoryModel;
import v9t9.tools.asm.MachineOperandParserStage;
import v9t9.tools.asm.OperandParser;
import v9t9.tools.asm.StandardInstructionParserStage;
import v9t9.tools.decomp.ICodeProvider;
import v9t9.tools.decomp.IDecompileInfo;
import v9t9.tools.llinst.LLInstruction;
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
                new StandardConsoleMemoryModel.RamArea(0x2000)
                );
		memory.addAndMap(romEntry);
        memory.addAndMap(new MemoryEntry("CPU RAM", CPU, 0x8000, 0x400,
                new StandardConsoleMemoryModel.RamArea(0x400)
                ));
        highLevel = new HighLevelCodeInfo(CPU);
        highLevel.getMemoryRanges().addRange(0, 0x2000, true);
        highLevel.getMemoryRanges().addRange(0x8300, 0x100, true);
        
        decompileInfo = highLevel.getDecompileInfo();
    }
    
    protected void parse(MemoryDomain cpu, int pc, int wp, String[] insts) throws ParseException {
	    LLInstruction first = null;
	    LLInstruction prev = null;
	    for (String element : insts) {
	    	LLInstruction inst = createLLInstruction(pc, wp, element);
	        highLevel.getLLInstructions().put(new Integer(inst.pc), inst);
	        highLevel.addInstruction(inst);
	        cpu.flatWriteWord(pc, inst.opcode);
	        if (inst.inst != Instruction.Idata) {
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
    
    public Map<Integer, LLInstruction> getLLInstructions() {
    	return decompileInfo.getLLInstructions();
    }

	public Instruction getInstruction(int addr) {
		return decompileInfo.getInstruction(addr);
	}
}
