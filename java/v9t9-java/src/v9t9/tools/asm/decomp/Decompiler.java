/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.emulator.runtime.cpu.CpuState9900;
import v9t9.emulator.runtime.cpu.CpuStateMFP201;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.memory.NativeFileMemoryEntry;
import v9t9.engine.memory.StockMemoryModel;
import v9t9.tools.asm.assembler.Assembler;

public class Decompiler implements ICodeProvider {
    static final String DEFAULT_EXT = ".dump";

    String ext = DEFAULT_EXT;
    String outfilename = null;
    boolean forceNonBinary = false;
    boolean showOpcodeAddr = false;
    boolean showComments = false;
    int verbose = 0;
    boolean nativeFile = false;
    
    MemoryModel model;
    Memory memory;
    MemoryDomain consoleMemory;

    private DecompileOptions options;
    HighLevelCodeInfo highLevel;

	CpuState state;

    public Decompiler(String proc) {
    	if (proc == null || proc.equals(Assembler.PROC_9900)) {
			model = new StockMemoryModel();
			memory = model.createMemory();
			consoleMemory = model.getConsole();
			state = new CpuState9900(consoleMemory);
			highLevel = new HighLevelCodeInfo(state);
		} else if (proc.equals(Assembler.PROC_MFP201)) {
			model = new StockMemoryModel();
			memory = model.createMemory();
			consoleMemory = model.getConsole();
			state = new CpuState9900(consoleMemory);
			highLevel = new HighLevelCodeInfo(state);
			state = new CpuStateMFP201(consoleMemory);
		} else {
			throw new IllegalStateException("unknown processor: " + proc);
		}
    	
        options = new DecompileOptions();

    }
    
    public void addRangeFromArgv(String string, boolean isCode) throws IOException {
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
