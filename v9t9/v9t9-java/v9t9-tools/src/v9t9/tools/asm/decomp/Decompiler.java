/*
  Decompiler.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.common.asm.ICodeProvider;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.cpu.ICpuState;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.asm.HighLevelCodeInfo;
import v9t9.machine.ti99.asm.TopDownPhase;
import v9t9.memory.MemoryEntry;
import v9t9.memory.NativeFileMemoryEntry;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class Decompiler implements ICodeProvider {

	protected DecompileOptions options;
	protected HighLevelCodeInfo highLevel;
	protected ICpuState state;
	private IMemory memory;
	private IMemoryDomain consoleMemory;

	/**
	 * 
	 */
	public Decompiler(IMachine machine, IInstructionFactory instructionFactory) {
		super();
		this.state = machine.getCpu().getState();
		
		options = new DecompileOptions();
		this.memory = machine.getMemory(); 
		consoleMemory = memory.getDomain(IMemoryDomain.NAME_CPU);
		highLevel = new HighLevelCodeInfo(state, instructionFactory);
	}

	public void addRangeFromArgv(String string, boolean isCode)
			throws IOException {
	    String hex = "((?:0x)?(?:[0-9a-fA-F])+)";
	    Pattern pattern = Pattern.compile(hex + "(:" + hex + ")?");
	    Matcher matcher = pattern.matcher(string);
	    if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid range: " + string);
		}
	    int baseAddr = HexUtils.parseHexInt(matcher.group(1));
	    int size = 0;
	    if (matcher.group(2) != null) {
			size = HexUtils.parseHexInt(matcher.group(3));
		}
	    highLevel.getMemoryRanges().addRange(baseAddr, size, isCode);
	}

	public void addRange(int addr, int size, boolean isCode) {
		highLevel.getMemoryRanges().addRange(addr, size, isCode);
	}
	public void addFile(String filename, int baseAddr) throws IOException {
	    NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(filename));
	    MemoryEntry entry = NativeFileMemoryEntry.newMemoryFromFile(baseAddr, filename, 
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
	
	/**
	 * @return the highLevel
	 */
	public HighLevelCodeInfo getHighLevel() {
		return highLevel;
	}

}