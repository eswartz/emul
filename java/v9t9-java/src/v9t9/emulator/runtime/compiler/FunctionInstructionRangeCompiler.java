/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import v9t9.emulator.runtime.compiler.Compiler9900.InstInfo;
import v9t9.emulator.runtime.compiler.Compiler9900.InstructionRangeCompiler;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.cpu.Instruction9900;
import v9t9.tools.asm.decomp.Block;
import v9t9.tools.asm.decomp.HighLevelInstruction;
import v9t9.tools.asm.decomp.IDecompileInfo;

/**
 * Compile lists of instructions per discovered functions, assuming only a few
 * are entry points. This must finalize info.sw.
 * 
 * @author ejs
 * 
 */
public class FunctionInstructionRangeCompiler implements
		InstructionRangeCompiler {

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Compiler.InstructionRangeCompiler#compileInstructionRange(v9t9.emulator.runtime.Compiler, int, int, v9t9.emulator.runtime.HighLevelCodeInfo, org.apache.bcel.generic.InstructionList, v9t9.emulator.runtime.CompileInfo)
	 */
	public void compileInstructionRange(final Compiler9900 compiler, Instruction9900[] insts,
			final HighLevelCodeInfo highLevel, InstructionList ilist, CompileInfo info) {

		int numinsts = insts.length;
		int addr = insts[0].pc;
	    //int size = insts[numinsts-1].pc + insts[numinsts-1].size - addr;
	    
	    IDecompileInfo decompileInfo = highLevel.getDecompileInfo();
		
	    // generate all the code for each block
	    InstInfo[] chunks = new InstInfo[numinsts];
	    for (Block block : decompileInfo.getBlocks()) {
	    	HighLevelInstruction inst = block.getFirst();
	    	int i = (inst.pc - addr) / 2;
	    	if (i >= 0 && i < numinsts) {
		    	chunks[i] = new InstInfo();
		    	info.ilist = new InstructionList();
		    	
		    	int j = i;
		    	while (inst != null) {
		    		// note: need low-level instr here (insts[j])
		    		compiler.generateInstruction(inst.pc, insts[j], info, chunks[i]);
		            
		    		// not compiled?
		            if (chunks[i].chunk == null) {
		            	chunks[i] = null;
		            	break;
		            }
		            	

		            if (inst == block.getLast())
		            	break;
		            
		            j += inst.size / 2;
		            if (j >= numinsts)
		            	break;		// if out of the code range but not the block
		            
		            inst = inst.getNext();
		            
		        }
		    	
		    	if (Compiler.settingOptimize.getBoolean() && chunks[i] != null) {
		    		// TODO: buggy
		    		//BytecodeOptimizer.peephole(info, chunks[i]);
		    	}
	    	}
	    }
	
	    // have each chunk branch to appropriate instruction in list
	    for (int i = 0; i < numinsts; i ++) {
	        InstInfo ii = chunks[i];
	        if (ii != null) {
	        	// this will be the last entry for the block
	        	if (false && ii.ins.info.jump == v9t9.engine.cpu.InstInfo.INST_JUMP_FALSE) {
	            	// not a jump, goto the next code block 
	        		ii.chunk.append(new GOTO(info.doneInst));
	            } else {
	            	// the jump has updated the PC, so re-switch 
	                ii.chunk.append(new GOTO(info.switchInst));
	            }
	        }
	    }
	
	    // Complete switch table.  It must handle every value from
	    // [addr, addr+size), but we can point to a "null handler" and
	    // return to force interpretation if an errant instruction is reached.
	    InstructionHandle firstNullInstructionHandler = null;
	    for (int i = 0; i < numinsts; i++) {
            InstructionHandle ih = null;
            if (chunks[i] != null) {
				ih = chunks[i].chunk.getStart();
			}
            if (ih != null) {
                ilist.append(chunks[i].chunk);
                info.sw.setTarget(i, ih);
            } else {
                if (firstNullInstructionHandler == null) {
                    ih = ilist.append(InstructionConstants.ICONST_0);
                    ilist.append(new GOTO(info.breakInst));
                    firstNullInstructionHandler = ih;
                } else {
                    ih = firstNullInstructionHandler;
                }
                info.sw.setTarget(i, ih);

            }
	    }

		
	}

}
