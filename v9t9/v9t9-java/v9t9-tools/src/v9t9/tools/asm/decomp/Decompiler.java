/**
 * 
 */
package v9t9.tools.asm.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import v9t9.base.utils.HexUtils;
import v9t9.common.asm.ICodeProvider;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntry;
import v9t9.common.memory.MemoryModel;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.memory.NativeFileMemoryEntry;
import v9t9.machine.ti99.asm.HighLevelCodeInfo;
import v9t9.machine.ti99.asm.Phase;
import v9t9.machine.ti99.asm.TopDownPhase;

/**
 * @author ejs
 *
 */
public class Decompiler implements ICodeProvider {

	protected MemoryModel model;
	protected IMemory memory;
	protected IMemoryDomain consoleMemory;
	protected DecompileOptions options;
	protected HighLevelCodeInfo highLevel;
	protected ICpuState state;

	/**
	 * 
	 */
	public Decompiler(MemoryModel model, IInstructionFactory instructionFactory, ICpuState state) {
		super();
		this.model = model;
		this.state = state;
		
		memory = model.getMemory();
		consoleMemory = model.getConsole();
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
	    NativeFile file = NativeFileFactory.createNativeFile(new File(filename));
	    MemoryEntry entry = NativeFileMemoryEntry.newWordMemoryFromFile(baseAddr, filename, 
	            consoleMemory, file, 0x0);
	    memory.addAndMap(entry);
	    highLevel.getMemoryRanges().addRange(baseAddr, entry.getSize(), true);
	}

	public Phase decompile() {
		//FullSweepPhase llp = new FullSweepPhase(state, highLevel);
	    TopDownPhase llp = new TopDownPhase(state, highLevel);
	    llp.addRefDefTables(getOptions().refDefTables);
	    llp.disassemble();
	    llp.addStandardROMRoutines();
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