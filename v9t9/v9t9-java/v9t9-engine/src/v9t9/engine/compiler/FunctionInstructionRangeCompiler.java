/**
 * 
 */
package v9t9.engine.compiler;

import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import v9t9.common.asm.Block;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.RawInstruction;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.compiler.ICompiler;

/**
 * Compile lists of instructions per discovered functions, assuming only a few
 * are entry points. This must finalize info.sw.
 * 
 * @author ejs
 * 
 */
public class FunctionInstructionRangeCompiler implements
		InstructionRangeCompiler {

	private ISettingsHandler settings;

	public FunctionInstructionRangeCompiler(ISettingsHandler settings) {
		this.settings = settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.Compiler.InstructionRangeCompiler#compileInstructionRange(v9t9.emulator.runtime.Compiler, int, int, v9t9.emulator.runtime.HighLevelCodeInfo, org.apache.bcel.generic.InstructionList, v9t9.emulator.runtime.CompileInfo)
	 */
	public void compileInstructionRange(final ICompiler compiler, RawInstruction[] insts,
			final IDecompileInfo highLevel, InstructionList ilist, CompileInfo info) {

		int numinsts = insts.length;
		int addr = insts[0].pc;
	    //int size = insts[numinsts-1].pc + insts[numinsts-1].size - addr;
	    
	    IDecompileInfo decompileInfo = highLevel;
		
	    // generate all the code for each block
	    CompiledInstInfo[] chunks = new CompiledInstInfo[numinsts];
	    for (Block block : decompileInfo.getBlocks()) {
	    	IHighLevelInstruction inst = block.getFirst();
	    	int i = (inst.getInst().pc - addr) / 2;
	    	if (i >= 0 && i < numinsts) {
		    	chunks[i] = new CompiledInstInfo();
		    	info.ilist = new InstructionList();
		    	
		    	int j = i;
		    	while (inst != null) {
		    		// note: need low-level instr here (insts[j])
		    		((CompilerBase) compiler).generateInstruction(inst.getInst().pc, insts[j], info, chunks[i]);
		            
		    		// not compiled?
		            if (chunks[i].chunk == null) {
		            	chunks[i] = null;
		            	break;
		            }
		            	

		            if (inst == block.getLast())
		            	break;
		            
		            j += inst.getInst().getSize() / 2;
		            if (j >= numinsts)
		            	break;		// if out of the code range but not the block
		            
		            inst = inst.getNext();
		            
		        }
		    	
		    	if (settings.get(ICompiler.settingOptimize).getBoolean() 
		    			&& chunks[i] != null) {
		    		// TODO: buggy
		    		//BytecodeOptimizer.peephole(info, chunks[i]);
		    	}
	    	}
	    }
	
	    // have each chunk branch to appropriate instruction in list
	    for (int i = 0; i < numinsts; i ++) {
	        CompiledInstInfo ii = chunks[i];
	        if (ii != null) {
	        	// this will be the last entry for the block
	        	if (false && ii.ins.getInfo().jump == v9t9.common.asm.InstInfo.INST_JUMP_FALSE) {
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
