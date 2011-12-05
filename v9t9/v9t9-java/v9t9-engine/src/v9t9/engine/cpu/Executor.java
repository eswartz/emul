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
import v9t9.base.settings.Logging;
import v9t9.base.settings.SettingProperty;
import v9t9.common.compiler.ICompiledCode;
import v9t9.common.compiler.ICompiler;
import v9t9.common.compiler.ICompilerStrategy;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.MetricEntry;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.engine.interpreter.IInterpreter;


/**
 * Handle executing instructions, either in interpret mode or compile mode.
 * 
 * @author ejs
 */
public class Executor implements IExecutor {

    private ICpu cpu;

    public IInterpreter interp;
    ICompilerStrategy compilerStrategy;

    public long nInstructions;
    public long nCompiledInstructions;
    public long nSwitches;
    public long nCompiles;

	//private ICpuController cpuController;

	public int nVdpInterrupts;

	/** counter for DBG/DBGF instructions */
    public int debugCount;

	public volatile Boolean interruptExecution;
    
	private IInstructionListener[] instructionListeners;

	private long lastCycleCount;

	private final ICpuMetrics cpuMetrics;

	private SettingProperty compile;

	private SettingProperty singleStep;

    public Executor(ICpu cpu, ICpuMetrics cpuMetrics, 
    		IInterpreter interpreter, ICompiler compiler, 
    		ICompilerStrategy compilerStrategy,
    		final IInstructionListener dumpFullReporter, final IInstructionListener dumpReporter) {
    	
    	compile = cpu.getMachine().getClient().getSettingsHandler().get(settingCompile);
    	singleStep = cpu.getMachine().getClient().getSettingsHandler().get(settingSingleStep);
    	
        this.cpu = cpu;
		this.cpuMetrics = cpuMetrics;
        this.interp = interpreter;
        this.compilerStrategy = compilerStrategy;
        
        compilerStrategy.setup(this, compiler);
        
        final Object lock = Executor.this.cpu.getMachine().getExecutionLock();
        cpu.settingDumpFullInstructions().addListener(new IPropertyListener() {

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
        cpu.settingDumpInstructions().addListener(new IPropertyListener() {
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
        cpu.settingRealTime().addListener(new IPropertyListener() {

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
        
        Logging.registerLog(cpu.settingDumpInstructions(), "instrs.txt");
        Logging.registerLog(cpu.settingDumpFullInstructions(), "instrs_full.txt");
    }

	public SettingProperty settingCompile() {
		return compile;
	}
	public SettingProperty settingSingleStep() {
		return singleStep;
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#interpretOneInstruction()
	 */
    @Override
	public synchronized void interpretOneInstruction() {
        interp.executeChunk(1, this);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#execute()
	 */
    @Override
	public void execute() {
    	if (cpu.isIdle() && cpu.settingRealTime().getBoolean()) {
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
    		while (!cpu.isThrottled() && nVdpInterrupts < IVdpChip.settingVdpInterruptRate.getInt()) {
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
				if (cpu.settingRealTime().getBoolean()) {
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
 
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#recordMetrics()
	 */
	@Override
	public final void recordMetrics() {
		
		long totalCycleCount = cpu.getTotalCycleCount();
		if (totalCycleCount == lastCycleCount)
			return;
		
		MetricEntry entry = new MetricEntry(
				(int) nInstructions,
				(int) (totalCycleCount - lastCycleCount), 
				(int) cpu.settingCyclesPerSecond().getInt(),
				nVdpInterrupts, cpu.getAndResetInterruptCount(), 
				nCompiledInstructions, (int) nSwitches, (int) nCompiles);

		cpuMetrics.log(entry);
		
        nInstructions = 0;
        nCompiledInstructions = 0;
        nSwitches = 0;
        nCompiles = 0;
        
        lastCycleCount = totalCycleCount;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#getInstructionListeners()
	 */
	@Override
	public final IInstructionListener[] getInstructionListeners() {
		return instructionListeners;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#addInstructionListener(v9t9.engine.cpu.InstructionListener)
	 */
	@Override
	public void addInstructionListener(IInstructionListener listener) {
		List<IInstructionListener> newListeners;
		if (instructionListeners == null) {
			newListeners = new ArrayList<IInstructionListener>();
		} else {
			newListeners = new ArrayList<IInstructionListener>(Arrays.asList(instructionListeners));
		}
		if (!newListeners.contains(listener))
			newListeners.add(listener);
		instructionListeners = (IInstructionListener[]) newListeners
				.toArray(new IInstructionListener[newListeners.size()]);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#removeInstructionListener(v9t9.engine.cpu.InstructionListener)
	 */
	@Override
	public void removeInstructionListener(IInstructionListener listener) {
		if (instructionListeners == null)
			return;
		List<IInstructionListener> newListeners = new ArrayList<IInstructionListener>(Arrays.asList(instructionListeners));
		newListeners.remove(listener);
		if (newListeners.size() == 0)
			instructionListeners = null;
		else
			instructionListeners = (IInstructionListener[]) newListeners
				.toArray(new IInstructionListener[newListeners.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#getCompilerStrategy()
	 */
	@Override
	public final ICompilerStrategy getCompilerStrategy() {
		return compilerStrategy;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#setCpu(v9t9.common.cpu.ICpu)
	 */
	@Override
	public void setCpu(ICpu cpu) {
		this.cpu = cpu;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#getCpu()
	 */
	@Override
	public final ICpu getCpu() {
		return cpu;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#recordSwitch()
	 */
	@Override
	public final void recordSwitch() {
		nSwitches++;		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#recordCompileRun(int, int)
	 */
	@Override
	public final void recordCompileRun(int nInstructions, int nCycles) {
        this.nInstructions += nInstructions;
        nCompiledInstructions += nInstructions;
        getCpu().addCycles(nCycles);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#recordCompilation()
	 */
	@Override
	public final void recordCompilation() {
		 nCompiles++;		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#breakAfterExecution()
	 */
	@Override
	public final boolean breakAfterExecution(int count) {
		nInstructions += count;
		cpu.checkAndHandleInterrupts();
		return interruptExecution;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#debugCount(int)
	 */
	@Override
	public final void debugCount(int i) {
    	int oldCount = debugCount; 
    	debugCount += i;
    	if ((oldCount == 0) != (debugCount == 0))
    		cpu.settingDumpFullInstructions().setBoolean(i > 0);
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#vdpInterrupt()
	 */
	@Override
	public final void vdpInterrupt() {
		nVdpInterrupts++;		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#resetVdpInterrupts()
	 */
	@Override
	public final void resetVdpInterrupts() {
		nVdpInterrupts = 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#interruptExecution()
	 */
	@Override
	public final void interruptExecution() {
		interruptExecution = Boolean.TRUE;		
	}

}