/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.decomp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.memory.NativeFileMemoryEntry;
import v9t9.engine.memory.StockMemoryModel;
import v9t9.utils.Utils;

public class Decompiler implements ICodeProvider {
    static final String DEFAULT_EXT = ".dump";

    String ext = DEFAULT_EXT;
    String outfilename = null;
    boolean forceNonBinary = false;
    boolean showOpcodeAddr = false;
    boolean showComments = false;
    int verbose = 0;
    boolean nativeFile = false;
    
    MemoryModel model = new StockMemoryModel();
    Memory memory = model.getMemory();
    MemoryDomain CPU = model.getConsole();

    private DecompileOptions options;
    HighLevelCodeInfo highLevel = new HighLevelCodeInfo(CPU);

    public Decompiler() {
        options = new DecompileOptions();

    }
    
    public void addRangeFromArgv(String string, boolean isCode) throws IOException {
        String hex = "((?=0x)?(?=\\d|[a-fA-F])+)";
        Pattern pattern = Pattern.compile(hex + "(:" + hex + ")?");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.matches()) {
			throw new IllegalArgumentException("invalid range: " + string);
		}
        
        int baseAddr = Utils.parseInt(matcher.group(1));
        int size = 0;
        if (matcher.group(2) != null) {
			size = Utils.parseInt(matcher.group(3));
		}
        highLevel.getMemoryRanges().addRange(baseAddr, size, isCode);
    }

    public void addFile(String filename, int baseAddr) throws IOException {
        NativeFile file = NativeFileFactory.createNativeFile(new File(filename));
        MemoryEntry entry = NativeFileMemoryEntry.newWordMemoryFromFile(baseAddr, 0, 
                filename, CPU, file, 0x0);
        memory.addAndMap(entry);
        highLevel.getMemoryRanges().addRange(baseAddr, entry.size, true);
    }

    public void decompile() {
        //FullSweepPhase llp = new FullSweepPhase(this);
        TopDownPhase llp = new TopDownPhase(CPU, highLevel);
        llp.addRefDefTables(getOptions().refDefTables);
        llp.disassemble();
        llp.addStandardROMRoutines();
        llp.run();
        
    }

    public DecompileOptions getOptions() {
        return options;
    }

	public MemoryDomain getCPUMemory() {
	    return CPU;
	}
}
