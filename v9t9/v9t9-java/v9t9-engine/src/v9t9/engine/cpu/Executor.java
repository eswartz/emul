/*
  Executor.java

  (c) 2008-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.cpu;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import v9t9.common.compiler.ICompiledCode;
import v9t9.common.compiler.ICompiler;
import v9t9.common.compiler.ICompilerStrategy;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.BreakpointManager;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.IInterpreter;
import v9t9.common.cpu.MetricEntry;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import ejs.base.settings.Logging;
import ejs.base.utils.ListenerList;


/**
 * Handle executing instructions, either in interpret mode or compile mode.
 * 
 * @author ejs
 */
public class Executor implements IExecutor {
	private Object executionLock = new Object();
    private ICpu cpu;

    private IInterpreter interp;
    ICompilerStrategy compilerStrategy;

    public long nInstructions;
    public long nCompiledInstructions;
    public long nSwitches;
    public long nCompiles;

	protected long lastInfo = 0;
	protected long upTime = 0;

	private List<Runnable> runnableList;


	//private ICpuController cpuController;

	public int nVdpInterrupts;

	/** counter for DBG/DBGF instructions */
    public int debugCount;

	protected volatile Boolean interruptExecution;
    
	private ListenerList<IInstructionListener> instructionListeners = new ListenerList<IInstructionListener>();

	private long lastCycleCount;

	private ICpuMetrics cpuMetrics;

	private IProperty compile;

	private IProperty singleStep;

	private IProperty vdpInterruptRate;

	private IProperty pauseMachine;

	private BreakpointManager breakpointManager;

	private boolean executing;

	private IMachine machine;

	private TimerTask memorySaverTask;

	private Thread videoRunner;

	private Thread cpuRunner;
	private volatile boolean needsTick;

    public Executor(IMachine machine, ICpu cpu, 
    		IInterpreter interpreter, ICompiler compiler, 
    		ICompilerStrategy compilerStrategy,
    		final IInstructionListener dumpFullReporter, final IInstructionListener dumpReporter) {
    	
    	runnableList = Collections.synchronizedList(new LinkedList<Runnable>());

    	this.machine = machine;
		compile = Settings.get(cpu, settingCompile);
    	singleStep = Settings.get(cpu, settingSingleStep);
    	pauseMachine = Settings.get(cpu, IMachine.settingPauseMachine);
    	vdpInterruptRate = Settings.get(cpu, IVdpChip.settingVdpInterruptRate);
    	
        this.cpu = cpu;
        this.interp = interpreter;
        this.compilerStrategy = compilerStrategy;
        
        compilerStrategy.setup(this, compiler);
        
        breakpointManager = new BreakpointManager((IMachine) cpu.getMachine());
        
        cpu.settingDumpFullInstructions().addListenerAndFire(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				synchronized (executionLock) {
					Settings.get(Executor.this.cpu, 
							IMachine.settingThrottleInterrupts).setBoolean(setting.getBoolean());
					
					if (setting.getBoolean()) {
						Executor.this.addInstructionListener(dumpFullReporter);
					} else {
						Executor.this.removeInstructionListener(dumpFullReporter);
					}
					interruptExecution = Boolean.TRUE;
					executionLock.notifyAll();
				}
			}
        	
        });
        cpu.settingDumpInstructions().addListenerAndFire(new IPropertyListener() {
			public void propertyChanged(IProperty setting) {
				synchronized (executionLock) {
					if (setting.getBoolean()) {
						Executor.this.addInstructionListener(dumpReporter);
					} else {
						Executor.this.removeInstructionListener(dumpReporter);
					}
					interruptExecution = Boolean.TRUE;
					executionLock.notifyAll();
				}
			}
        	
        });
        
        pauseMachine.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				interruptExecution = Boolean.TRUE;
			}
        	
        });
        
        singleStep.addListener(new IPropertyListener() {
        	
        	public void propertyChanged(IProperty setting) {
        		synchronized (executionLock) {
        			interruptExecution = Boolean.TRUE;
        			executionLock.notifyAll();
        		}
        	}
        	
        });
        cpu.settingRealTime().addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				interruptExecution = Boolean.TRUE;
			}
        	
        });

        


        pauseMachine.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
				lastCycleCount = 0;				
			}
        	
        });
        
        Logging.registerLog(cpu.settingDumpInstructions(), "instrs.txt");
        Logging.registerLog(cpu.settingDumpFullInstructions(), "instrs_full.txt");
    }

    /* (non-Javadoc)
     * @see v9t9.common.cpu.IExecutor#setMetrics(v9t9.common.cpu.ICpuMetrics)
     */
    @Override
    public void setMetrics(ICpuMetrics cpuMetrics) {
    	this.cpuMetrics = cpuMetrics;
    }
	public IProperty settingCompile() {
		return compile;
	}
	public IProperty settingSingleStep() {
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
     * @see v9t9.common.cpu.IExecutor#execute()
     */
    @Override
    public int execute() {
    	if (cpu.isIdle() && cpu.settingRealTime().getBoolean()) {
    		if (nVdpInterrupts < vdpInterruptRate.getInt()) {
    			try {
    				cpu.getCycleCounts().addOverhead(64);
    				cpu.checkInterrupts();
    			} catch (AbortedException e) {
    				cpu.handleInterrupts();
				}
    		}
    		cpu.applyCycles();
    		return 0;
    	} else {
    		int curCycles = cpu.getCurrentCycleCount();
			if (compile.getBoolean()) {
				executeCompilableCode();
			} else if (singleStep.getBoolean()) {
				interpretOneInstruction();
			} else {
				interruptExecution = Boolean.FALSE;
				if (cpu.settingRealTime().getBoolean()) {
					interp.executeChunk(24, this);
				} else {
					interp.executeChunk(100, this);
				}
			}
			cpu.applyCycles();
			int usedCycles = cpu.getCurrentCycleCount() - curCycles;
			return usedCycles;
    	}
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#execute()
	 */
//    @Override
//	public void execute() {
//    	if (cpu.isIdle() && cpu.settingRealTime().getBoolean()) {
//    		if (cpu.isThrottled())
//    			return;
//    		
//    		while (!cpu.isThrottled() && nVdpInterrupts < vdpInterruptRate.getInt()) {
//    			try {
//    				//long start = System.currentTimeMillis();
//    				Thread.yield();
//    				
//    				//long end = System.currentTimeMillis();
//    				cpu.addCycles(1);
//    				cpu.checkInterrupts();
//    			} catch (AbortedException e) {
//    				cpu.handleInterrupts();
//    				break;
//    			}
//    		}
//    	} else {
//			if (compile.getBoolean()) {
//				executeCompilableCode();
//			} else if (singleStep.getBoolean()) {
//				interpretOneInstruction();
//			} else {
//				interruptExecution = Boolean.FALSE;
//				if (cpu.settingRealTime().getBoolean()) {
//					while (!cpu.isThrottled() && !interruptExecution) {
//						interp.executeChunk(10, this);
//					}
//				} else {
//					interp.executeChunk(100, this);
//				}
//			}
//    	}
//    }
    

    private void executeCompilableCode() {
    	try {
	    	boolean interpreting = false;
			if (compile.getBoolean()) {
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
		if (cpuMetrics == null)
			return;
		
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
	public final ListenerList<IInstructionListener> getInstructionListeners() {
		return instructionListeners;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#addInstructionListener(v9t9.engine.cpu.InstructionListener)
	 */
	@Override
	public void addInstructionListener(IInstructionListener listener) {
		instructionListeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IExecutor#removeInstructionListener(v9t9.engine.cpu.InstructionListener)
	 */
	@Override
	public void removeInstructionListener(IInstructionListener listener) {
		instructionListeners.remove(listener);
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
        getCpu().getCycleCounts().addOverhead(nCycles);
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#getBreakpoints()
	 */
	@Override
	public BreakpointManager getBreakpoints() {
		return breakpointManager;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#setExecuting(boolean)
	 */
	@Override
	public boolean setExecuting(boolean b) {
		synchronized (executionLock) {
			interruptExecution();
			boolean wasExecuting = executing;
			executing = b;
			cpu.resetCycleCounts();
			executionLock.notifyAll();
			return wasExecuting;
		}

	}
	
	/**
	 * @return the executionLock
	 */
	public Object getExecutionLock() {
		return executionLock;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#isExecuting()
	 */
	@Override
	public boolean isExecuting() {
		synchronized (executionLock) {
			return executing;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#tick()
	 */
	@Override
	public void tick() {
		synchronized (executionLock) {
			if (!executing)
				return;
			
			long now = System.currentTimeMillis();
			
			if (now >= lastInfo + 1000) {
				upTime += now - lastInfo;
				recordMetrics();
				resetVdpInterrupts();
				lastInfo = now;
			}
			
//			if (!cpu.isIdle() && !machine.isPaused()) {
//				// make up the difference if we did not fulfill all the instructions 
//				int diff = cpu.getCurrentTargetCycleCount() - cpu.getCurrentCycleCount();
//				if (diff < cpu.getTargetCycleCount()) {
//					while (diff > 0) {
//						interpretOneInstruction();
//						diff = cpu.getCurrentTargetCycleCount() - cpu.getCurrentCycleCount();
//					}
//				}
//			}
			
			cpu.tick();
			
			machine.getVdp().tick();
		}    					
	}
	
	public void queueTick() {
		needsTick = true;
	}
	@Override
	public void start() {
		
        videoRunner = new Thread("Video Runner") {
        	@Override
        	public void run() {
        		IVdpChip vdp = machine.getVdp();
        				
        		while (machine.isAlive()) {
	        		// delay if going too fast
	    			while (vdp.isThrottled() && machine.isAlive()) {
	    				// Just sleep.  Another timer thread will reset the throttle.
	    				try {
	    					Thread.sleep(10);
	    				} catch (InterruptedException e) {
	    					break;
	    				}
	    			}
	    			if (!vdp.isThrottled()) {
	    				vdp.work();
	    			}
        		}
        	}
        };
        
        
        memorySaverTask = new TimerTask() {
        	@Override
        	public void run() {
        		synchronized (executionLock) {
        			machine.getMemory().save();
        		}
        	}
        };
        machine.getMachineTimer().scheduleAtFixedRate(memorySaverTask, 0, 5000);
        
        
		cpuRunner = new Thread("CPU Runner") {
        	@Override
        	public void run() {
    	        while (machine.isAlive()) {
            		Runnable runnable;
            		synchronized (executionLock) {
	            		while (runnableList.size() > 0) {
	            			runnable = runnableList.remove(0);
	            			try {
	            				runnable.run();
	            			} catch (Throwable t) {
	            				t.printStackTrace();
	            			}
	            		}

	    	            if (needsTick) {
	    	            	needsTick = false;
	    	            	tick();
	    	            }
            		}
            		

    	            try {
    	            	// synchronize on events like debugging, loading/saving, etc
    	            	int usedCycles = 0;
    	            	boolean realTime = false;
	            		synchronized (executionLock) {
	            			realTime = cpu.settingRealTime().getBoolean();
	            					
	            			if (!executing && machine.isAlive()) {
	            				executionLock.wait(10);	// need short delay here for 9900 ints ??
	            			}
	            			if (executing) {
	            				usedCycles = execute();
	            			}
	            		}
	            		
            			if (usedCycles >= 0 && realTime) {
            				int count = Math.min(usedCycles, cpu.getCurrentTargetCycleCount());
            				while (false == cpu.getAllocatedCycles().tryAcquire(count)) {
        	    	            if (needsTick) {
        	    	            	needsTick = false;
        	    	            	tick();
        	    	            }
        	    	            if (pauseMachine.getBoolean()) {
        	    	            	Thread.sleep(50);
        	    	            } else {
        	    	            	Thread.sleep(0, 1);
        	    	            }
            				}
            			}
	            		
    	            } catch (AbortedException e) {
    	            } catch (InterruptedException e) {
      	              	break;
    	            } catch (Throwable t) {
    	            	t.printStackTrace();
    	            	machine.stop();
    	            	break;
    	            }

    	        }
        	}
        };
        
        
        cpuRunner.start();

        videoRunner.start();
        
        setExecuting(!pauseMachine.getBoolean());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#stop()
	 */
	@Override
	public void stop() {
		setExecuting(false);
		
		cpuRunner.interrupt();
		videoRunner.interrupt();
		try {
			videoRunner.join();
		} catch (InterruptedException e) {
		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#asyncExec(java.lang.Runnable)
	 */
	@Override
	public void asyncExec(Runnable runnable) {
		runnableList.add(runnable);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IExecutor#reset()
	 */
	@Override
	public void reset() {
		runnableList.clear();
		if (interp != null)
			interp.reset();
		if (compilerStrategy != null)
			compilerStrategy.reset();
			
	}

	/**
	 * @return the interp
	 */
	public IInterpreter getInterpreter() {
		return interp;
	}

	/**
	 * @param interp the interp to set
	 */
	public void setInterpreter(IInterpreter interp) {
		this.interp = interp;
	}
}