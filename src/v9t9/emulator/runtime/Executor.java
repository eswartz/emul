/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.Machine;
import v9t9.emulator.runtime.compiler.CodeBlockCompilerStrategy;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.emulator.runtime.compiler.ICompiledCode;
import v9t9.emulator.runtime.compiler.ICompilerStrategy;
import v9t9.emulator.runtime.interpreter.Interpreter;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;


/**
 * Handle executing instructions, either in interpret mode or compile mode.
 * 
 * @author ejs
 */
public class Executor {

    public Cpu cpu;

    public Map<MemoryArea, HighLevelCodeInfo> highLevelCodeInfoMap;
    public Interpreter interp;
    ICompilerStrategy compilerStrategy;
    


    public long nInstructions;
    public long nCompiledInstructions;
    public long nSwitches;
    public long nCompiles;

	private long nLastCycleCount;

	//private ICpuController cpuController;

	public int nVdpInterrupts;

	static public final String sCompile = "Compile";
    static public final Setting settingCompile = new Setting(sCompile, new Boolean(false));
    static public final String sDumpInstructions = "DumpInstructions";
    static public final Setting settingDumpInstructions = new Setting(sDumpInstructions, new Boolean(false));
    static public final String sDumpFullInstructions = "DumpFullInstructions";
    static public final Setting settingDumpFullInstructions = new Setting(sDumpFullInstructions, new Boolean(false));

    /** counter for DBG/DBGF instructions */
    public int debugCount;
    
    public Executor(Cpu cpu) {
        this.cpu = cpu;
        this.interp = new Interpreter(cpu.getMachine());
        this.compilerStrategy = new CodeBlockCompilerStrategy(this);
        this.highLevelCodeInfoMap = new HashMap<MemoryArea, HighLevelCodeInfo>();
        
        settingDumpFullInstructions.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				if (setting.getBoolean())
					debugCount++;
				else
					debugCount--;
				Machine.settingThrottleInterrupts.setBoolean(setting.getBoolean());
			}
        	
        });
        cpu.getMachine().getSettings().register(settingDumpInstructions);
        cpu.getMachine().getSettings().register(settingDumpFullInstructions);
        Logging.registerLog(settingDumpInstructions, "instrs.txt");
        Logging.registerLog(settingDumpFullInstructions, "instrs_full.txt");
        
        Machine.settingPauseMachine.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				nLastCycleCount = 0;				
			}
        	
        });

    }

    public interface ICpuController {
    	void act(Cpu cpu);
    }
    
    /** Allow for an event to inject behavior before the next instruction
     * executes.
     * @param controller
     */
   // public synchronized void controlCpu(ICpuController controller) {
    //	cpuController = controller;
    //}
    public synchronized void interpretOneInstruction() {
    	//if (cpuController != null) {
    	//	ICpuController controller = cpuController;
    	//	cpuController = null;
    	//	controller.act(cpu);
    	//}
        interp.execute(cpu, null);
        nInstructions++;
    }

    /** 
     * Run an unbounded amount of code.  Some external factor
     * tells the execution unit when to stop.  The interpret/compile
     * setting is sticky until execution is interrupted.
     * @throws AbortedException when interrupt or other machine event stops execution
     */
    public void execute() {
		if (settingCompile.getBoolean()) {
			executeCompilableCode();
		} else {
			if (Cpu.settingRealTime.getBoolean()) {
				while (!cpu.isThrottled()) {
					interpretOneInstruction();
					cpu.checkAndHandleInterrupts();
				}
			} else {
				// pretend the realtime setting doesn't change often
				for (int i = 0; i < 1000; i++) {
					interpretOneInstruction();
					cpu.checkAndHandleInterrupts();
				}	
			}
			
		}
    }

    private void executeCompilableCode() {
    	try {
	    	boolean interpreting = false;
			if (settingCompile.getBoolean()) {
				/* try to make or run native code, which may fail */
				short pc = cpu.getPC();
				if ((pc >= 0x6000 && pc < 0x8000) 
						&& Compiler.settingDumpModuleRomInstructions.getBoolean()) {
			    	settingDumpInstructions.setBoolean(true);
			        settingDumpFullInstructions.setBoolean(true);
			    }
	
				ICompiledCode code = compilerStrategy.getCompiledCode(cpu.getPC() & 0xffff, cpu.getWP());
			    if (code == null || !code.run()) {
			    	// Returns false if an instruction couldn't be executed
			    	// because it did not look like real code (or was not expected to be directly invoked).
			    	// Returns true if fell out of the code block.
			    	//System.out.println("Switch  branching to >" + Utils.toHex4(cpu.getPC()));
			    	interpreting = true;
			    	nSwitches++;
				}
			} else {
				interpreting = true;
			}
			
			if (interpreting) {
			    interpretOneInstruction();
			    cpu.checkInterrupts();
			}
    	} catch (AbortedException e) {
            cpu.handleInterrupts();
		}
	}
	public PrintWriter getDump() {
        return Logging.getLog(settingDumpInstructions);
    }

    public PrintWriter getDumpfull() {
    	return Logging.getLog(settingDumpFullInstructions);
    }
 
    /** Currently, only gather high-level info for one memory entry at a time */
    public HighLevelCodeInfo getHighLevelCode(MemoryEntry entry) {
    	MemoryArea area = entry.getArea();
    	HighLevelCodeInfo highLevel = highLevelCodeInfoMap.get(area);
    	if (highLevel == null) {
    		System.out.println("Initializing high level info for " + entry + " / " + area);
    		highLevel = new HighLevelCodeInfo(cpu.getConsole());
    		highLevel.disassemble(entry.addr, entry.size);
    		highLevelCodeInfoMap.put(area, highLevel);
    	}
    	return highLevel;
    }

	public void dumpStats() {
        int compileAvg = 0;
        if (nInstructions == 0) {
        	return;
        }
        
        double compiled = (double)nCompiledInstructions / (double)nInstructions;
        compileAvg = ((int) (compiled * 10000));
        
        System.out.println("# instructions / second: " + nInstructions
        		+ " (cycles = " + (cpu.getTotalCycleCount() - nLastCycleCount) 
        		+ (settingCompile.getBoolean() ? 
        		"; " + compileAvg / 100 + "." + compileAvg % 100 + "% compiled, " 
        		+ nSwitches + " context switches, " + nCompiles + " compiles)" : ")")
        		+ "; VDP Interrupts = " +nVdpInterrupts + " (honored = " + cpu.getAndResetInterruptCount() + ")");
        nInstructions = 0;
        nCompiledInstructions = 0;
        nSwitches = 0;
        nCompiles = 0;
        nLastCycleCount = cpu.getTotalCycleCount();
	}

}