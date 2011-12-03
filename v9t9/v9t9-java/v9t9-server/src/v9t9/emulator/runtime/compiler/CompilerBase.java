/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.emulator.runtime.cpu.CpuState;
import v9t9.engine.asm.IDecompileInfo;
import v9t9.engine.asm.IInstructionFactory;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author Ed
 *
 */
public abstract class CompilerBase {

	static public final SettingProperty settingOptimize = new SettingProperty("CompilerOptimize",
	new Boolean(false));
	static public final SettingProperty settingOptimizeRegAccess = new SettingProperty(
	"CompilerOptimizeRegAccess", new Boolean(false));
	static public final SettingProperty settingOptimizeStatus = new SettingProperty(
	"CompilerOptimizeStatus", new Boolean(false));
	static public final SettingProperty settingCompileOptimizeCallsWithData = new SettingProperty(
	"CompilerOptmizeCallsWithData", new Boolean(false));
	static public final SettingProperty settingDebugInstructions = new SettingProperty(
	"DebugInstructions", new Boolean(false));
	static public final SettingProperty settingCompileFunctions = new SettingProperty(
	"CompilerCompileFunctions", new Boolean(false));
	

    public Map<MemoryArea, IDecompileInfo> highLevelCodeInfoMap = new HashMap<MemoryArea, IDecompileInfo>();
	private IInstructionFactory instructionFactory;
	private CpuState cpuState;

	/**
	 * 
	 */
	public CompilerBase(CpuState cpuState, IInstructionFactory instructionFactory) {
		this.cpuState = cpuState;
		this.instructionFactory = instructionFactory;
	}
    /**
	 * Compile the instructions into bytecode.
	 * @param uniqueClassName
	 * @param baseName
	 * @param highLevel
	 * @param insts
	 * @param entries
	 * @return
	 */
	abstract public byte[] compile(String uniqueClassName, String baseName,
			IDecompileInfo highLevel, RawInstruction[] insts, short[] entries);
	/**
	 * Tell if the CPU is coherent and compilation makes sense
	 * @return
	 */
	abstract public boolean validCpuState();

	abstract public void generateInstruction(int pc, RawInstruction rawins,
            CompileInfo info, CompiledInstInfo ii);
	
    /** Currently, only gather high-level info for one memory entry at a time */
    public IDecompileInfo getHighLevelCode(MemoryEntry entry) {
    	MemoryArea area = entry.getArea();
    	IDecompileInfo highLevel = highLevelCodeInfoMap.get(area);
    	if (highLevel == null) {
    		System.out.println("Initializing high level info for " + entry + " / " + area);
    		highLevel = instructionFactory.createDecompileInfo(cpuState);
    		highLevel.disassemble(entry.addr, entry.size);
    		highLevelCodeInfoMap.put(area, highLevel);
    	}
    	return highLevel;
    }
}
