/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

import v9t9.emulator.Machine;
import v9t9.emulator.runtime.CpuMetrics.MetricEntry;
import v9t9.emulator.runtime.compiler.CodeBlockCompilerStrategy;
import v9t9.emulator.runtime.compiler.Compiler;
import v9t9.emulator.runtime.compiler.ICompiledCode;
import v9t9.emulator.runtime.compiler.ICompilerStrategy;
import v9t9.emulator.runtime.interpreter.Interpreter;
import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryEntry;


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

	//private ICpuController cpuController;

	public int nVdpInterrupts;

	static public final String sCompile = "Compile";
    static public final Setting settingCompile = new Setting(sCompile, new Boolean(false));
    static public final String sDumpInstructions = "DumpInstructions";
    static public final Setting settingDumpInstructions = new Setting(sDumpInstructions, new Boolean(false));
    static public final String sDumpFullInstructions = "DumpFullInstructions";
    static public final Setting settingDumpFullInstructions = new Setting(sDumpFullInstructions, new Boolean(false));
    static public final Setting settingSingleStep = new Setting("SingleStep", new Boolean(false));

    /** counter for DBG/DBGF instructions */
    public int debugCount;

	public volatile Boolean interruptExecution;
    
	private InstructionListener[] instructionListeners;

	private final CpuMetrics cpuMetrics;
	
    public Executor(Cpu cpu, CpuMetrics cpuMetrics) {
        this.cpu = cpu;
		this.cpuMetrics = cpuMetrics;
        this.interp = new Interpreter(cpu.getMachine());
        this.compilerStrategy = new CodeBlockCompilerStrategy(this);
        this.highLevelCodeInfoMap = new HashMap<MemoryArea, HighLevelCodeInfo>();
        
        settingDumpFullInstructions.addListener(new ISettingListener() {

        	DumpFullReporter reporter = new DumpFullReporter(Executor.this.cpu);
			public void changed(Setting setting, Object oldValue) {
				Machine.settingThrottleInterrupts.setBoolean(setting.getBoolean());
				
				if (setting.getBoolean()) {
					Executor.this.addInstructionListener(reporter);
				} else {
					Executor.this.removeInstructionListener(reporter);
				}
				interruptExecution = Boolean.TRUE;
			}
        	
        });
        settingDumpInstructions.addListener(new ISettingListener() {
        	DumpReporter reporter = new DumpReporter(Executor.this.cpu);
			public void changed(Setting setting, Object oldValue) {
				if (setting.getBoolean()) {
					Executor.this.addInstructionListener(reporter);
				} else {
					Executor.this.removeInstructionListener(reporter);
				}
				interruptExecution = Boolean.TRUE;
			}
        	
        });
        Machine.settingPauseMachine.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				interruptExecution = Boolean.TRUE;
			}
        	
        });
        settingSingleStep.addListener(new ISettingListener() {
        	
        	public void changed(Setting setting, Object oldValue) {
        		interruptExecution = Boolean.TRUE;
        	}
        	
        });
        Cpu.settingRealTime.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				interruptExecution = Boolean.TRUE;
			}
        	
        });
        cpu.getMachine().getSettings().register(settingDumpInstructions);
        cpu.getMachine().getSettings().register(settingDumpFullInstructions);
        Logging.registerLog(settingDumpInstructions, "instrs.txt");
        Logging.registerLog(settingDumpFullInstructions, "instrs_full.txt");
        
        Machine.settingPauseMachine.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				Executor.this.cpuMetrics.resetLastCycleCount();				
			}
        	
        });

    }

    public synchronized void interpretOneInstruction() {
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
		} else if (settingSingleStep.getBoolean()){
			interpretOneInstruction();
			cpu.checkAndHandleInterrupts();
		} else {
			interruptExecution = Boolean.FALSE;
			if (Cpu.settingRealTime.getBoolean()) {
				while (!cpu.isThrottled() && !interruptExecution) {
					interpretOneInstruction();
					cpu.checkAndHandleInterrupts();
				}
			} else {
				// pretend the realtime and instructionListeners settings don't change often
				if (instructionListeners == null) {
					for (int i = 0; i < 1000 && !interruptExecution; i++) {
						interp.executeFast(cpu, null);
				        nInstructions++;
						cpu.checkAndHandleInterrupts();
					}
				} else {
					for (int i = 0; i < 1000 && !interruptExecution; i++) {
						interp.execute(cpu, null);
				        nInstructions++;
						cpu.checkAndHandleInterrupts();
					}
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
	public static PrintWriter getDump() {
        return Logging.getLog(settingDumpInstructions);
    }

    public static PrintWriter getDumpfull() {
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

	public void recordMetrics() {
		MetricEntry entry = cpuMetrics.log(nInstructions,
				cpu.getTotalCycleCount(), Cpu.settingCyclesPerSecond.getInt(),
				nVdpInterrupts, cpu.getAndResetInterruptCount(), 
				nCompiledInstructions, nSwitches, nCompiles);
		
		if (entry != null) {
			//entry.dump();
		}
        nInstructions = 0;
        nCompiledInstructions = 0;
        nSwitches = 0;
        nCompiles = 0;
	}

	/**
	 * @return
	 */
	public InstructionListener[] getInstructionListeners() {
		return instructionListeners;
	}
	
	public void addInstructionListener(InstructionListener listener) {
		List<InstructionListener> newListeners;
		if (instructionListeners == null) {
			newListeners = new ArrayList<InstructionListener>();
		} else {
			newListeners = new ArrayList<InstructionListener>(Arrays.asList(instructionListeners));
		}
		if (!newListeners.contains(listener))
			newListeners.add(listener);
		instructionListeners = (InstructionListener[]) newListeners
				.toArray(new InstructionListener[newListeners.size()]);
	}
	public void removeInstructionListener(InstructionListener listener) {
		if (instructionListeners == null)
			return;
		List<InstructionListener> newListeners = new ArrayList<InstructionListener>(Arrays.asList(instructionListeners));
		newListeners.remove(listener);
		if (newListeners.size() == 0)
			instructionListeners = null;
		else
			instructionListeners = (InstructionListener[]) newListeners
				.toArray(new InstructionListener[newListeners.size()]);
	}

}