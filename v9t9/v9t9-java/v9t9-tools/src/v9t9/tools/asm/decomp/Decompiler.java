/**
 * 
 */
package v9t9.tools.asm.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.asm.ICodeProvider;
import v9t9.engine.asm.IInstructionFactory;
import v9t9.engine.asm.Phase;
import v9t9.engine.asm.TopDownPhase;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.memory.NativeFileMemoryEntry;

/**
 * @author ejs
 *
 */
public class Decompiler implements ICodeProvider {

	protected MemoryModel model;
	protected Memory memory;
	protected MemoryDomain consoleMemory;
	protected DecompileOptions options;
	protected HighLevelCodeInfo highLevel;
	protected CpuState state;

	/**
	 * 
	 */
	public Decompiler(MemoryModel model, IInstructionFactory instructionFactory, CpuState state) {
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
	    highLevel.getMemoryRanges().addRange(baseAddr, entry.size, true);
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

	public MemoryDomain getCPUMemory() {
	    return consoleMemory;
	}

}