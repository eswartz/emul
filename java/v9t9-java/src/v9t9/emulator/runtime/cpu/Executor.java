/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.Logging;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.emulator.runtime.compiler.ICompiledCode;
import v9t9.emulator.runtime.compiler.ICompilerStrategy;
import v9t9.emulator.runtime.cpu.CpuMetrics.MetricEntry;
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
    static public final SettingProperty settingCompile = new SettingProperty(sCompile, new Boolean(false));
    static public final String sDumpInstructions = "DumpInstructions";
    static public final SettingProperty settingDumpInstructions = new SettingProperty(sDumpInstructions, new Boolean(false));
    static public final String sDumpFullInstructions = "DumpFullInstructions";
    static public final SettingProperty settingDumpFullInstructions = new SettingProperty(sDumpFullInstructions, new Boolean(false));
    static public final SettingProperty settingSingleStep = new SettingProperty("SingleStep", new Boolean(false));

    /** counter for DBG/DBGF instructions */
    public int debugCount;

	public volatile Boolean interruptExecution;
    
	private InstructionListener[] instructionListeners;

	private final CpuMetrics cpuMetrics;
	
    public Executor(Cpu cpu, CpuMetrics cpuMetrics, 
    		Interpreter interpreter,
    		ICompilerStrategy compilerStrategy,
    		final InstructionListener dumpFullReporter,
    		final InstructionListener dumpReporter) {
        this.cpu = cpu;
		this.cpuMetrics = cpuMetrics;
        this.interp = interpreter;
        this.compilerStrategy = compilerStrategy;
        compilerStrategy.setExecutor(this);
        this.highLevelCodeInfoMap = new HashMap<MemoryArea, HighLevelCodeInfo>();
        
        final Object lock = Executor.this.cpu.getMachine().getExecutionLock();
        settingDumpFullInstructions.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				synchronized (lock) {
					Machine.settingThrottleInterrupts.setBoolean(setting.getBoolean());
					
					if (setting.getBoolean()) {
						Executor.this.addInstructionListener(dumpFullReporter);
					} else {
						Executor.this.removeInstructionListener(dumpFullReporter);
					}
					interruptExecution = Boolean.TRUE;
					lock.notifyAll();
				}
			}
        	
        });
        settingDumpInstructions.addListener(new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				synchronized (lock) {
					if (setting.getBoolean()) {
						Executor.this.addInstructionListener(dumpReporter);
					} else {
						Executor.this.removeInstructionListener(dumpReporter);
					}
					interruptExecution = Boolean.TRUE;
					lock.notifyAll();
				}
			}
        	
        });
        /*
        Machine.settingPauseMachine.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				interruptExecution = Boolean.TRUE;
			}
        	
        });*/
        settingSingleStep.addListener(new IPropertyListener() {
        	
        	public void propertyChanged(IProperty setting) {
        		synchronized (lock) {
        			interruptExecution = Boolean.TRUE;
        			lock.notifyAll();
        		}
        	}
        	
        });
        Cpu.settingRealTime.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				interruptExecution = Boolean.TRUE;
				//synchronized (lock) {
				//	lock.notifyAll();
				// }
			}
        	
        });
        Logging.registerLog(settingDumpInstructions, "instrs.txt");
        Logging.registerLog(settingDumpFullInstructions, "instrs_full.txt");
        
        Machine.settingPauseMachine.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				Executor.this.cpuMetrics.resetLastCycleCount();				
			}
        	
        });

    }

    public synchronized void interpretOneInstruction() {
        interp.executeChunk(1, this);
    }

    /** 
     * Run an unbounded amount of code.  Some external factor
     * tells the execution unit when to stop.  The interpret/compile
     * setting is sticky until execution is interrupted.
     * @throws AbortedException when interrupt or other machine event stops execution
     */
    public void execute() {
    	if (cpu.isIdle() && Cpu.settingRealTime.getBoolean()) {
    		if (cpu.isThrottled())
    			return;
    		long start = System.currentTimeMillis();
    		try {
    			// short sleep
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			long end = System.currentTimeMillis();
			cpu.checkAndHandleInterrupts();
			cpu.addCycles(cpu.getBaseCyclesPerSec() * (int)(end - start) / 1000);
    	} else {
			if (settingCompile.getBoolean()) {
				executeCompilableCode();
			} else if (settingSingleStep.getBoolean()){
				interpretOneInstruction();
			} else {
				interruptExecution = Boolean.FALSE;
				if (Cpu.settingRealTime.getBoolean()) {
					while (!cpu.isThrottled() && !interruptExecution) {
						interp.executeChunk(10, this);
					}
				} else {
					interp.executeChunk(100, this);
				}
			}
    	}
    }
    

    private void executeCompilableCode() {
    	try {
	    	boolean interpreting = false;
			if (settingCompile.getBoolean()) {
				/* try to make or run native code, which may fail */
				ICompiledCode code = compilerStrategy.getCompiledCode(cpu);
			    if (code == null || !code.run()) {
			    	// Returns false if an instruction couldn't be executed
			    	// because it did not look like real code (or was not expected to be directly invoked).
			    	// Returns true if fell out of the code block.
			    	System.out.println("Switch  branching to >" + HexUtils.toHex4(cpu.getPC()));
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
    		highLevel = new HighLevelCodeInfo(cpu);
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

	/**
	 * @return
	 */
	public ICompilerStrategy getCompilerStrategy() {
		return compilerStrategy;
	}

}