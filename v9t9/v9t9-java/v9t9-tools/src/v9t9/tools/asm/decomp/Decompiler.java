/*
  Decompiler.java

  (c) 2008-2012 Edward Swartz

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
package v9t9.tools.asm.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ejs.base.utils.HexUtils;


import v9t9.common.asm.ICodeProvider;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.cpu.ICpuState;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.NativeFileMemoryEntry;
import v9t9.machine.ti99.asm.HighLevelCodeInfo;
import v9t9.machine.ti99.asm.TopDownPhase;

/**
 * @author ejs
 *
 */
public class Decompiler implements ICodeProvider {

	protected IMemory memory;
	protected IMemoryDomain consoleMemory;
	protected DecompileOptions options;
	protected HighLevelCodeInfo highLevel;
	protected ICpuState state;

	/**
	 * 
	 */
	public Decompiler(IMemory memory, IInstructionFactory instructionFactory, ICpuState state) {
		super();
		this.state = state;
		
		options = new DecompileOptions();
		this.memory = memory; 
		consoleMemory = memory.getDomain(IMemoryDomain.NAME_CPU);
		highLevel = new HighLevelCodeInfo(state, instructionFactory);
	}

	public void addRangeFromArgv(String string, boolean isCode)
			throws IOException {
			    String hex = "((?:0x)?(?:\\d|[a-fA-F])+)";
			    Pattern pattern = Pattern.compile(hex + "(:" + hex + ")?");
			    Matcher matcher = pattern.matcher(string);
			    if (!matcher.matches()) {
					throw new IllegalArgumentException("invalid range: " + string);
				}
			    
			    int baseAddr = HexUtils.parseInt(matcher.group(1));
			    int size = 0;
			    if (matcher.group(2) != null) {
					size = HexUtils.parseInt(matcher.group(3));
				}
			    highLevel.getMemoryRanges().addRange(baseAddr, size, isCode);
			}

	public void addFile(String filename, int baseAddr) throws IOException {
	    NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(filename));
	    MemoryEntry entry = NativeFileMemoryEntry.newWordMemoryFromFile(baseAddr, filename, 
	            consoleMemory, file, 0x0);
	    memory.addAndMap(entry);
	    highLevel.getMemoryRanges().addRange(baseAddr, entry.getSize(), true);
	}

	public IDecompilePhase decompile() {
		//FullSweepPhase llp = new FullSweepPhase(state, highLevel);
	    TopDownPhase llp = new TopDownPhase(state, highLevel);
	    llp.addRefDefTables(getOptions().refDefTables);
	    llp.disassemble();
	    llp.run();
	    return llp;
	    
	}

	public DecompileOptions getOptions() {
	    return options;
	}

	public IMemoryDomain getCPUMemory() {
	    return consoleMemory;
	}

}