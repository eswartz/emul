/**
 * 
 */
package v9t9.engine.compiler;

import java.util.Map;
import java.util.TreeMap;


import v9t9.base.utils.Pair;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.MemoryArea;
import v9t9.common.memory.MemoryEntry;
import v9t9.engine.cpu.Executor;

/**
 * @author ejs
 *
 */
public class CodeBlockCompilerStrategy implements ICompilerStrategy {
	// map of address blocks to CodeBlocks
    //
    // this size seems best for JVM HotSpot compiler:  too few reduces ability to optimize
    // and too many makes classes that are too large to optimize
    static final short BLOCKSIZE = 0x100;   

    Map<Pair<MemoryArea, Integer>, CodeBlock> codeblocks;
    DirectLoader loader;

	private Executor executor;

	private CompilerBase compiler;

	public CodeBlockCompilerStrategy() {
		
        codeblocks = new TreeMap<Pair<MemoryArea, Integer>, CodeBlock>();
        loader = new DirectLoader();

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#canCompile()
	 */
	@Override
	public boolean canCompile() {
		return true;
	}
	
	public void setup(Executor executor, CompilerBase compiler) {
		this.executor = executor;
		this.compiler = compiler;
	}
	public ICompiledCode getCompiledCode() {
		ICpu cpu = executor.getCpu();
		if (cpu.shouldDebugCompiledCode(cpu.getPC())) {
			ICpu.settingDumpInstructions.setBoolean(true);
			ICpu.settingDumpFullInstructions.setBoolean(true);
		}
        CodeBlock cb = getCodeBlock(cpu.getPC());
		return cb;
	}
	
   /**
     * @param ent
     * @return
     */
    public boolean isCompilable(MemoryEntry ent) {
        return ent != null && ent.getDomain().isEntryFullyMapped(ent) && ent.getArea().hasReadAccess()
         //&& !ent.area.hasWriteAccess()
        ; // for now
    }


	/**
	 * Get a code block to represent the eventual compiled code
	 * for this block.
	 * @param pc entry point PC
	 * @return code block or null if not compilable
	 */
	private CodeBlock getCodeBlock(int pc) {
	    MemoryEntry ent = executor.getCpu().getConsole().getEntryAt(pc);
	    if (!isCompilable(ent)) {
			return null;
		}
	    
	    Integer blockaddr = new Integer(pc & ~(BLOCKSIZE - 1));
	    Pair<MemoryArea, Integer> key = new Pair<MemoryArea, Integer>(ent.getArea(), blockaddr);
	    CodeBlock cb;
	    if ((cb = codeblocks.get(key)) == null
	            || !cb.matches(ent)) {
	        cb = new CodeBlock(compiler, executor,  
	        		loader, ent, blockaddr.shortValue(), BLOCKSIZE);
	        cb.build();
	        executor.nCompiles++;
	        codeblocks.put(key, cb);
	    }
	    return cb;
	}

}
