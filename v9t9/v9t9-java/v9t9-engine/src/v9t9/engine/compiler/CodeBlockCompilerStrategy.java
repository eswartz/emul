/*
  CodeBlockCompilerStrategy.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.compiler;

import java.util.Map;
import java.util.TreeMap;

import ejs.base.utils.Pair;


import v9t9.common.compiler.ICompiledCode;
import v9t9.common.compiler.ICompiler;
import v9t9.common.compiler.ICompilerStrategy;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;

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

    Map<Pair<IMemoryArea, Integer>, CodeBlock> codeblocks;
    DirectLoader loader;

	private IExecutor executor;

	private ICompiler compiler;

	public CodeBlockCompilerStrategy() {
		
        codeblocks = new TreeMap<Pair<IMemoryArea, Integer>, CodeBlock>();
        loader = new DirectLoader();

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.ICompilerStrategy#canCompile()
	 */
	@Override
	public boolean canCompile() {
		return true;
	}
	
	public void setup(IExecutor executor, ICompiler compiler) {
		this.executor = executor;
		this.compiler = compiler;
	}
	public synchronized ICompiledCode getCompiledCode() {
		ICpu cpu = executor.getCpu();
		short pc = cpu.getState().getPC();
		if (cpu.shouldDebugCompiledCode(pc)) {
			Settings.get(cpu, ICpu.settingDumpInstructions).setBoolean(true);
			Settings.get(cpu, ICpu.settingDumpFullInstructions).setBoolean(true);
		}
        CodeBlock cb = getCodeBlock(pc);
		return cb;
	}
	
   /**
     * @param ent
     * @return
     */
    public boolean isCompilable(IMemoryEntry ent) {
        return ent != null 
        //&& ent.getDomain().isEntryFullyMapped(ent) 
        && ent.getArea().hasReadAccess()
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
	    IMemoryEntry ent = executor.getCpu().getConsole().getEntryAt(pc);
	    
	    Integer blockaddr = new Integer(pc & ~(BLOCKSIZE - 1));
	    Pair<IMemoryArea, Integer> key = new Pair<IMemoryArea, Integer>(ent.getArea(), blockaddr);
	    CodeBlock cb;
	    if ((cb = codeblocks.get(key)) == null
	            || !cb.matches(ent)) {
		    if (!isCompilable(ent)) {
				return null;
			}
	        cb = new CodeBlock(compiler, executor,  
	        		loader, ent, blockaddr.shortValue(), BLOCKSIZE);
	        cb.build();
	        executor.recordCompilation();
	        codeblocks.put(key, cb);
	    }
	    return cb;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompilerStrategy#reset()
	 */
	@Override
	public synchronized void reset() {
		codeblocks.clear();
	}
}
