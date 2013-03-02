/*
  Assembler9900.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.inst9900;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;


import v9t9.common.asm.IInstruction;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.StockRamArea;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.AssemblerError;
import v9t9.tools.asm.assembler.Equate;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.SymbolTable;
import v9t9.tools.asm.assembler.directive.LabelDirective;
import v9t9.tools.asm.assembler.transform.JumpFixer9900;
import v9t9.tools.asm.assembler.transform.Simplifier9900;

/**
 * @author ejs
 *
 */
public class Assembler9900 extends Assembler implements IAssembler {
	/**
	 * @param proc
	 */
	@Override
	public void setProcessor(String proc) {
		if (instructionFactory != null) {
			throw new IllegalStateException("already set the processor");
		}
		if (PROC_9900.equals(proc)) {
		    MemoryEntry StdCPURAM = new MemoryEntry("Std CPU RAM",
		    		StdCPU, 0x8000, 0x400, new StockRamArea(0x400));
		    MemoryEntry StdCPUExpLoRAM = new MemoryEntry("Std CPU Low Exp RAM",
		    		StdCPU, 0x2000, 0x2000, new StockRamArea(0x2000));
		    MemoryEntry StdCPUExpHiRAM = new MemoryEntry("Std CPU Hi Exp RAM",
		    		StdCPU, 0xA000, 0x6000, new StockRamArea(0x6000));
		    
			StdCPU.mapEntry(StdCPURAM);
			StdCPU.mapEntry(StdCPUExpHiRAM);
			StdCPU.mapEntry(StdCPUExpLoRAM);
			
			instructionFactory = new AsmInstructionFactory9900();
			
	    	OperandParser operandParser = new OperandParser();
			operandParser.appendStage(new AssemblerOperandParserStage9900(this));
			
			configureParser(
					operandParser,
					new StandardInstructionParserStage9900(operandParser));

			for (int i = 0; i < 16; i++) {
				symbolTable.addSymbol(new Equate(symbolTable, "R" + i, i));
			}
			
			basicSize = 2;
		}
		else {
			throw new IllegalArgumentException("unknown processor: "+ proc);
		}
	}
    public Assembler9900() {
		CPUFullRAM.mapEntry(CPUFullRAMEntry);

    	//instDescrMap = new HashMap<IInstruction, String>();
    	
    	symbolTable = new SymbolTable();
    	
    	labelTable = new IdentityHashMap<Symbol, LabelDirective>();
    	errorList = new ArrayList<AssemblerError>();
	}

	/**
	 * Optimize instructions
	 * @param insts
	 */
	@Override
	public List<IInstruction> optimize(List<IInstruction> insts) {
		new Simplifier9900(insts).run();
		return resolve(insts);
	}
	
	/**
	 * Fix up any jumps that go too far
	 * @param insts
	 * @return
	 */
	@Override
	public List<IInstruction> fixupJumps(List<IInstruction> insts) {
		try {
			return new JumpFixer9900(this, insts).run();
		} catch (ResolveException e) {
			reportError(e);
			return insts;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.inst9900.Assembler#injectInstruction(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	protected void injectInstruction(LLInstruction inst) {
		try {
			RawInstruction rawInst = getInstructionFactory().createRawInstruction(inst);
			int pc = rawInst.getPc();
			
			// Obviously, RAM can change.  Also, jump instructions may be moved
			// (there is a bug here too)
			if (getConsole().hasRamAccess(pc) || 
					getInstructionFactory().isJumpInst(rawInst.getInst()))
				return;
			
			short[] words = InstTable9900.encode(rawInst);
			
			for (int i = 0; i < words.length; i++) {
				short word = words[i];
				if (i == 0 || (i == 1 && inst.getOp1().isConstant())
						|| (((i == 1 && inst.getOp1().getSize() == 0) || i == 2) && inst.getOp2()!=null && inst.getOp2().isConstant())) {
					Integer ipc = getConstPool().getInstWordMap().get(word & 0xffff);
					if (ipc == null) {
						getConstPool().getInstWordMap().put(word & 0xffff, pc + i * 2);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// some unresolved jump insts
		} catch (ResolveException e) {
			// unresolved inst
		}
	}

	
}
