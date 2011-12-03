/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.Logging;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.cpu.MetricEntry;
import v9t9.engine.compiler.CompilerBase;
import v9t9.engine.compiler.ICompiledCode;
import v9t9.engine.compiler.ICompilerStrategy;
import v9t9.engine.interpreter.Interpreter;
import v9t9.engine.machine.IMachine;
import v9t9.engine.video.tms9918a.VdpTMS9918A;


/**
 * Handle executing instructions, either in interpret mode or compile mode.
 * 
 * @author ejs
 */
public class Executor {

    private ICpu cpu;

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
    static public final SettingProperty settingSingleStep = new SettingProperty("SingleStep", new Boolean(false));

    /** counter for DBG/DBGF instructions */
    public int debugCount;

	public volatile Boolean interruptExecution;
    
	private InstructionListener[] instructionListeners;

	private long lastCycleCount;

	private final ICpuMetrics cpuMetrics;

    public Executor(ICpu cpu, ICpuMetrics cpuMetrics, 
    		Interpreter interpreter, CompilerBase compiler, 
    		ICompilerStrategy compilerStrategy,
    		final InstructionListener dumpFullReporter, final InstructionListener dumpReporter) {
        this.cpu = cpu;
		this.cpuMetrics = cpuMetrics;
        this.interp = interpreter;
        this.compilerStrategy = compilerStrategy;
        compilerStrategy.setup(this, compiler);
        
        final Object lock = Executor.this.cpu.getMachine().getExecutionLock();
        ICpu.settingDumpFullInstructions.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				synchronized (lock) {
					IMachine.settingThrottleInterrupts.setBoolean(setting.getBoolean());
					
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
        ICpu.settingDumpInstructions.addListener(new IPropertyListener() {
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
        ICpu.settingRealTime.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				interruptExecution = Boolean.TRUE;
				//synchronized (lock) {
				//	lock.notifyAll();
				// }
			}
        	
        });
        


        IMachine.settingPauseMachine.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				lastCycleCount = 0;				
			}
        	
        });
        
        Logging.registerLog(ICpu.settingDumpInstructions, "instrs.txt");
        Logging.registerLog(ICpu.settingDumpFullInstructions, "instrs_full.txt");
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
    	if (cpu.isIdle() && ICpu.settingRealTime.getBoolean()) {
    		if (cpu.isThrottled())
    			return;
    		/*
    		long start = System.currentTimeMillis();
    		try {
    			// short sleep
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			long end = System.currentTimeMillis();
			//System.out.print((end - start) + " ");
			cpu.addCycles(cpu.getBaseCyclesPerSec() * (int)(end - start + 500) / 1000);
    		cpu.checkAndHandleInterrupts();
			*/
    		while (!cpu.isThrottled() && nVdpInterrupts < VdpTMS9918A.settingVdpInterruptRate.getInt()) {
    			try {
    				//long start = System.currentTimeMillis();
    				Thread.yield();
    				
    				//long end = System.currentTimeMillis();
    				cpu.addCycles(1);
    				cpu.checkInterrupts();
    			} catch (AbortedException e) {
    				cpu.handleInterrupts();
    				break;
    			}
    		}
    	} else {
			if (settingCompile.getBoolean()) {
				executeCompilableCode();
			} else if (settingSingleStep.getBoolean()){
				interpretOneInstruction();
			} else {
				interruptExecution = Boolean.FALSE;
				if (ICpu.settingRealTime.getBoolean()) {
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
				ICompiledCode code = compilerStrategy.getCompiledCode();
			    if (code == null || !code.run()) {
			    	// Returns false if an instruction couldn't be executed
			    	// because it did not look like real code (or was not expected to be directly invoked).
			    	// Returns true if fell out of the code block.
			    	//System.out.println("Switch  branching to >" + HexUtils.toHex4(cpu.getPC()));
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
        return Logging.getLog(ICpu.settingDumpInstructions);
    }

    public static PrintWriter getDumpfull() {
    	return Logging.getLog(ICpu.settingDumpFullInstructions);
    }
 
	public void recordMetrics() {
		
		long totalCycleCount = cpu.getTotalCycleCount();
		if (totalCycleCount == lastCycleCount)
			return;
		
		MetricEntry entry = new MetricEntry(
				(int) nInstructions,
				(int) (totalCycleCount - lastCycleCount), 
				(int) ICpu.settingCyclesPerSecond.getInt(),
				nVdpInterrupts, cpu.getAndResetInterruptCount(), 
				nCompiledInstructions, (int) nSwitches, (int) nCompiles);

		cpuMetrics.log(entry);
		
        nInstructions = 0;
        nCompiledInstructions = 0;
        nSwitches = 0;
        nCompiles = 0;
        
        lastCycleCount = totalCycleCount;
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

	/**
	 * @param cpu the cpu to set
	 */
	public void setCpu(ICpu cpu) {
		this.cpu = cpu;
	}

	/**
	 * @return the cpu
	 */
	public ICpu getCpu() {
		return cpu;
	}

}