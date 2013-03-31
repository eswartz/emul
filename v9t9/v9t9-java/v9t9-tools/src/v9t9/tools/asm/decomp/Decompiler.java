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

import ejs.base.utils.HexUtils;


import v9t9.common.asm.ICodeProvider;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.cpu.ICpuState;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.ByteMemoryArea;
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

	public void addFile(String filename, int baseAddr) throws IOException {
	    NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(filename));
	    MemoryEntry entry = NativeFileMemoryEntry.newWordMemoryFromFile(baseAddr, filename, 
	            consoleMemory, file, 0x0);
	    memory.addAndMap(entry);
	    highLevel.getMemoryRanges().addRange(baseAddr, entry.getSize(), true);
	}
	public void addMemoryImage(String name) throws IOException {
    	int loadNext = 0;
    	do {
    		int size = 0;
    		int addr = 0;
        	try {
        		System.err.println("loading " + name);
        		NativeFile file = NativeFileFactory.INSTANCE.createNativeFile(new File(name));
        		byte[] contents = new byte[file.getFileSize()];
        		file.readContents(contents, 0, 0, contents.length);
        		if (contents.length < 6) {
        			throw new IOException("not enough data for memory image header");
        		}
        		loadNext = ((contents[0] & 0xff) << 8) | (contents[1] & 0xff);
        		size = ((contents[2] & 0xff) << 8) | (contents[3] & 0xff);
        		addr = ((contents[4] & 0xff) << 8) | (contents[5] & 0xff);
        		if (!((addr >= 0x2000 && addr < 0x4000) || (addr >= 0xA000 || addr <= 0xffff))) {
        			throw new IOException("malformed memory image header: content not targeting RAM");
        		}
        		if (addr + size > 0x10000) {
        			throw new IOException("malformed memory image header: addr + size > 64k");
        		}

        		int baseAddr = (addr  + IMemoryDomain.AREASIZE - 1) & ~(IMemoryDomain.AREASIZE - 1);
        		int endAreaAddr = (addr + size + IMemoryDomain.AREASIZE - 1) & ~(IMemoryDomain.AREASIZE - 1);
        				
        		int areaSize = endAreaAddr - baseAddr;
        	    MemoryEntry entry = new MemoryEntry(name, consoleMemory, baseAddr, areaSize,
        	    		new ByteMemoryArea(0, new byte[areaSize]));
        	    memory.addAndMap(entry);

        		for (int o = 0; o < size; o++) {
        			entry.flatWriteByte(addr + o, contents[o + 6]);
        		}
        		
        		highLevel.getMemoryRanges().addRange(addr, size, true);
			} catch (IOException e) {
				System.err.println("failed to load file: " + e.getMessage());
        		System.exit(1);
			}
        	
        	name = name.substring(0, name.length() - 1) +(char)  ( name.charAt(name.length() - 1) + 1);
    	} while (loadNext != 0);
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