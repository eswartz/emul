/*
  BaseTopDownPhaseTest9900.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.inst9900;

import java.util.Collection;
import java.util.Map;

import v9t9.common.asm.ICodeProvider;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.MemoryRanges;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockRamArea;
import v9t9.machine.ti99.asm.HighLevelCodeInfo9900;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.asm.Phase9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.tools.asm.ParseException;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;

public abstract class BaseTopDownPhaseTest9900 extends BaseTest9900 implements ICodeProvider
{
	protected HighLevelCodeInfo9900 highLevel;
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
        highLevel = new HighLevelCodeInfo9900(state, AsmInstructionFactory9900.INSTANCE);
        highLevel.getMemoryRanges().addRange(0, 0x2000, true);
        highLevel.getMemoryRanges().addRange(0x8300, 0x100, true);
        
        decompileInfo = highLevel.getDecompileInfo();
    }
    
    protected void parse(IMemoryDomain cpu, int pc, int wp, String[] insts) throws ParseException {
	    IHighLevelInstruction first = null;
//	    IHighLevelInstruction prev = null;
	    int startPc = pc;
	    for (String element : insts) {
	    	IHighLevelInstruction hinst = createHLInstruction(pc, wp, element);
	    	RawInstruction inst = hinst.getInst();
	    	
	    	byte[] bytes = InstructionFactory9900.INSTANCE.encodeInstruction(inst);
	    	for (int  l = 0; l < bytes.length; l++)
	    		cpu.flatWriteByte(pc++, bytes[l]);
	    	
	    	/*
	    	highLevel.getLLInstructions().put(Integer.valueOf(inst.pc), hinst);
	    	highLevel.addInstruction(hinst);
	        cpu.flatWriteWord(pc, (short) inst.opcode);
	        if (inst.getInst() != InstTableCommon.Idata) {
				pc += 2;
			}
	        if (((IMachineOperand) inst.getOp1()).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((BaseMachineOperand) inst.getOp1()).immed);
	            pc += 2;
	        }
	        if (((IMachineOperand) inst.getOp2()).hasImmediate()) {
	            cpu.flatWriteWord(pc, ((BaseMachineOperand) inst.getOp2()).immed);
	            pc += 2;
	        }
	        if (prev != null) {
				prev.setPhysicalNext(hinst);
			}
	        prev = hinst;
	        if (first == null) {
				first = hinst;
			}
			*/
	    }
	    
	    decompileInfo.getMemoryRanges().clear();
	    decompileInfo.getMemoryRanges().addRange(startPc, pc - startPc, true);
	    new Phase9900(state, decompileInfo) {
	    	/* (non-Javadoc)
	    	 * @see v9t9.common.asm.IDecompilePhase#run()
	    	 */
	    	@Override
	    	public void run() {
	    		
	    	}
	    }.disassemble();
	    
	    first = decompileInfo.getLLInstructions().get(startPc);
	    
	    while (first != null) {
	    	first.setWp((short) wp);
	        System.out.println("inst: "+ first);
	        first = first.getLogicalNext();
	    }
	}
    
 	public IMemoryDomain getCPUMemory() {
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
