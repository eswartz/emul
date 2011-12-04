/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.machine;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import v9t9.base.properties.IProperty;
import v9t9.base.properties.IPropertyListener;
import v9t9.base.settings.ISettingSection;
import v9t9.base.timer.FastTimer;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.CpuMetrics;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuMetrics;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.memory.Memory;
import v9t9.common.memory.MemoryDomain;
import v9t9.common.memory.MemoryEntry;
import v9t9.common.memory.MemoryModel;
import v9t9.engine.client.IClient;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.DsrManager;
import v9t9.engine.dsr.IDsrManager;
import v9t9.engine.events.RecordingEventNotifier;
import v9t9.engine.files.DataFiles;
import v9t9.engine.hardware.ICruAccess;
import v9t9.engine.hardware.ISoundChip;
import v9t9.engine.hardware.ISpeechChip;
import v9t9.engine.hardware.IVdpChip;
import v9t9.engine.keyboard.KeyboardState;
import v9t9.engine.settings.WorkspaceSettings;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
abstract public class Machine implements IMachine {
    protected Memory memory;
    protected MemoryDomain console;
    protected  ICpu cpu;
    protected  Executor executor;
    protected IClient client;
    protected  volatile boolean bAlive;
    protected Timer timer;
    protected FastTimer fastTimer;
    //Timer cpuTimer;
    //protected Timer videoTimer;
	private IVdpChip vdp;
	protected DsrManager dsrManager;

    protected long lastInterrupt = System.currentTimeMillis();
    protected long lastInfo = lastInterrupt;
    protected long upTime = 0;
    
    protected boolean allowInterrupts;
    protected final int clientTick = 1000 / 100;
    protected final int videoUpdateTick = 1000 / 30;
    protected final int cpuTicksPerSec = 100;
    //protected long now;
    //private TimerTask vdpInterruptTask;
    protected  TimerTask clientTask;
    protected  Runnable cpuTimingTask;
	protected MemoryModel memoryModel;
	protected  TimerTask videoUpdateTask;
	protected  Thread machineRunner;
	protected  Thread videoRunner;
	protected int throttleCount;
	protected  KeyboardState keyboardState;
	private Object executionLock = new Object();
	volatile protected boolean bExecuting;
	protected  ISoundChip sound;
	protected  ISpeechChip speech;
	private List<Runnable> runnableList;
	private ICpuMetrics cpuMetrics;
	
	protected  TimerTask memorySaverTask;
	protected  ModuleManager moduleManager;
	
	private ICruAccess cruAccess;
	
	protected  RecordingEventNotifier recordingNotifier = new RecordingEventNotifier();
	private IRawInstructionFactory instructionFactory;
	private final MachineModel machineModel;
	private IPropertyListener pauseListener;
	private Runnable speechTimerTask;
	
    public Machine(MachineModel machineModel) {
    	pauseListener = new IPropertyListener() {
    		
    		public void propertyChanged(IProperty setting) {
    			synchronized (executionLock) {
    				executor.interruptExecution = true;
    				cpu.resetCycleCounts();
    				bExecuting = !setting.getBoolean();
    				executionLock.notifyAll();
    			}
    		}
    		
    	};
		settingPauseMachine.addListener(pauseListener);
    	
    	this.machineModel = machineModel;
    	
    	runnableList = Collections.synchronizedList(new LinkedList<Runnable>());
    	this.memoryModel = machineModel.getMemoryModel();
    	this.memory = memoryModel.getMemory();
    	this.console = memoryModel.getConsole();
    	
    	timer = new Timer();
    	fastTimer = new FastTimer();

    	
    	init(machineModel);

    	machineModel.defineDevices(this);
    	
    	cpuMetrics = new CpuMetrics();
    	executor = machineModel.createExecutor(cpu, cpuMetrics);
    	
    	//executor.addInstructionListener(new DebugConditionListener(cpu));
    	//executor.addInstructionListener(new DebugConditionListenerF99b(cpu));

	}


	protected void init(MachineModel machineModel) {
    	this.vdp = machineModel.createVdp(this);
    	sound = machineModel.createSoundChip(this);
    	speech = machineModel.createSpeechChip(this);
    	memoryModel.initMemory(this);
    	
    	if (!settingModuleList.getString().isEmpty()) {
    		moduleManager = new ModuleManager(this);
    	}
    	
    	cpu = machineModel.createCPU(this); 
		keyboardState = new KeyboardState(this);

    	this.instructionFactory = machineModel.getInstructionFactory();
	}
    
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#notifyEvent(v9t9.emulator.common.IEventNotifier.Level, java.lang.String)
	 */
	@Override
	public void notifyEvent(IEventNotifier.Level level, String string) {
		if (client != null)
			client.getEventNotifier().notifyEvent(this, level, string);
		else
			recordingNotifier.notifyEvent(this, level, string);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#notifyEvent(v9t9.emulator.common.NotifyEvent)
	 */
	@Override
	public void notifyEvent(NotifyEvent event) {
		if (client != null)
			client.getEventNotifier().notifyEvent(event);
		else
			recordingNotifier.notifyEvent(event);
	}

	/* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable {
    	settingPauseMachine.removeListener(pauseListener);
    	
    	if (recordingNotifier != null) {
    		NotifyEvent event;
    		while ((event = recordingNotifier.getNextEvent()) != null) {
    			event.print(System.err);
    		}
    	}
        super.finalize();
        client = null;
        memory = null;
        executor = null;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getMemory()
	 */
    @Override
	public Memory getMemory() {
        return memory;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#isAlive()
	 */
    @Override
	public boolean isAlive() {
        return bAlive;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#start()
	 */
    @Override
	public void start() {
    	allowInterrupts = true;
    	
    	//videoTimer = new Timer();

        // the CPU emulation task (a fast timer because 100 Hz is a little too much for Windows) 
        cpuTimingTask = new Runnable() {

			@Override
        	public void run() {
				synchronized (executionLock) {
					if (!bExecuting)
						return;
				
	    			long now = System.currentTimeMillis();
	    			//System.out.println(now);
	    			
	    	
	    			//System.out.print(now);
	    			
	    			if (now >= lastInfo + 1000) {
	    				upTime += now - lastInfo;
	    				executor.recordMetrics();
	    				executor.nVdpInterrupts = 0;
	    				lastInfo = now;
	    				//vdpInterruptDelta = 0;
	    			}
	    			
	    			if (sound != null)
	    				sound.tick();
	    			
	    			cpu.tick();
	
	    			vdp.tick();
				}    			
        	}
        };
        fastTimer.scheduleTask(cpuTimingTask, cpuTicksPerSec);
        
        if (speech != null) {
    		int hz;

			hz = speech.getGenerateRate();
			speechTimerTask = new Runnable() {
	
				@Override
				public void run() {
					if (IMachine.settingPauseMachine.getBoolean())
						return;
					speech.generateSpeech();
				}
				
			};
			fastTimer.scheduleTask(speechTimerTask, hz);
        }
        
        videoRunner = new Thread("Video Runner") {
        	@Override
        	public void run() {
        		while (Machine.this.isAlive()) {
	        		// delay if going too fast
	    			while (vdp.isThrottled() && bAlive) {
	    				// Just sleep.  Another timer thread will reset the throttle.
	    				try {
	    					Thread.sleep(10);
	    				} catch (InterruptedException e) {
	    					break;
	    				}
	    			}
	    			vdp.work();
        		}
        	}
        };
        
        
        // the potentially expensive task of blitting the screen to the
        // physical screen -- not scheduled at a fixed rate to avoid
        // overloading the CPU with pending redraw requests
        videoUpdateTask = new TimerTask() {

            @Override
			public void run() {
            	if (client != null) client.updateVideo();
            }
        };
        //videoTimer.schedule(videoUpdateTask, 0, videoUpdateTick);
        timer.schedule(videoUpdateTask, 0, videoUpdateTick);
        
        // the client's interrupt task, which lets it monitor
        // other less expensive devices like the keyboard, sound,
        // etc.
        clientTask = new TimerTask() {
        	
        	@Override
        	public void run() {
        		if (client != null) client.timerInterrupt();
        	}
        };
        timer.scheduleAtFixedRate(clientTask, 0, clientTick);
        
        memorySaverTask = new TimerTask() {
        	@Override
        	public void run() {
        		synchronized (executionLock) {
        			memory.save();
        		}
        	}
        };
        timer.scheduleAtFixedRate(memorySaverTask, 0, 5000);
        
        bAlive = true;
        
      	// the machine (well, actually, CPU) runner
		machineRunner = new Thread("Machine Runner") {
        	@Override
        	public void run() {
    	        while (Machine.this.isAlive()) {
            		Runnable runnable;
            		while (runnableList.size() > 0) {
            			runnable = runnableList.remove(0);
            			runnable.run();
            		}
	            	
    	        	
    	        	// delay if going too fast
    				if (ICpu.settingRealTime.getBoolean()) {
    					if (cpu.isThrottled() && cpu.getMachine().isAlive()) {
    						// Just sleep.  Another timer thread will reset the throttle.
    						try {
    							Thread.sleep(1000 / 200);		// expected clock: 100Hz
    							continue;
    						} catch (InterruptedException e) {
    							return;
    						}
    					}
    				}
    				
    	            try {
    	            	// synchronize on events like debugging, loading/saving, etc
	            		synchronized (executionLock) {
	            			if (!bExecuting && bAlive) {
	            				executionLock.wait(100);
	            			}
	            			if (bExecuting && !cpu.isIdle()) {
	            				executor.execute();
	            			}
	            		}
	            		if (bExecuting && cpu.isIdle())
	            			executor.execute();
    	            } catch (AbortedException e) {
    	            } catch (InterruptedException e) {
      	              	break;
    	            } catch (Throwable t) {
    	            	t.printStackTrace();
    	            	Machine.this.setNotRunning();
    	            	break;
    	            }
    	        }
        	}
        };
        
        machineRunner.start();
        videoRunner.start();
        
        synchronized (executionLock) {
			bExecuting = true;
			executionLock.notifyAll();
		}
    }
    
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#stop()
	 */
    @Override
	public void stop() {
    	setNotRunning();
        throw new TerminatedException();
    }

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setNotRunning()
	 */
	@Override
	public void setNotRunning() {
		bAlive = false;
		executor.interruptExecution = true;
		synchronized (executionLock) {
			bExecuting = false;
			executionLock.notifyAll();
		}
		machineRunner.interrupt();
		videoRunner.interrupt();
		timer.cancel();
        fastTimer.cancel();
        //videoTimer.cancel();
		try {
			videoRunner.join();
		} catch (InterruptedException e) {
		}
		
		memory.save();        
        getSound().getSoundHandler().dispose();
        if (dsrManager != null)
			dsrManager.dispose();
	}
    
	/* (non-Javadoc)
	 * @see v9t9.common.settings.IBaseMachine#reset()
	 */
	@Override
	public void reset() {

		MemoryDomain domain = getMemory().getDomain(MemoryDomain.NAME_CPU);
		for (MemoryEntry entry : domain.getFlattenedMemoryEntries()) {
			if (entry.isVolatile()) {
				int addr = entry.mapAddress(entry.addr);
				//System.out.println("Wiping " + entry +  "@" + HexUtils.toHex4(addr));
				for (int i = 0; i < entry.size; i+= 2)
					domain.writeWord(i + addr, (short) 0);
			}
		}
		
		if (cruAccess != null)
			cruAccess.reset();
				
		cpu.reset();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getCpu()
	 */
	@Override
	public ICpu getCpu() {
        return cpu;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setCpu(v9t9.emulator.runtime.cpu.Cpu)
	 */
    @Override
	public void setCpu(ICpu cpu) {
        this.cpu = cpu;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getClient()
	 */
    @Override
	public IClient getClient() {
        return client;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setClient(v9t9.engine.Client)
	 */
    @Override
	public void setClient(IClient client) {
        this.client = client;
        if (client != null) {
        	if (recordingNotifier != null) {
        		NotifyEvent event;
        		while ((event = recordingNotifier.getNextEvent()) != null) {
        			client.getEventNotifier().notifyEvent(event);
        		}
        		recordingNotifier = null;
        	}
        } else {
        	recordingNotifier = new RecordingEventNotifier();
        }
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getExecutor()
	 */
    @Override
	public Executor getExecutor() {
        return executor;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setExecutor(v9t9.emulator.runtime.cpu.Executor)
	 */
    @Override
	public void setExecutor(Executor executor) {
        this.executor = executor;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getConsole()
	 */
    @Override
	public MemoryDomain getConsole() {
		return console;
	}
    
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getMemoryModel()
	 */
    @Override
	public MemoryModel getMemoryModel() {
        return memoryModel;
    }

    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getKeyboardState()
	 */
    @Override
	public KeyboardState getKeyboardState() {
		return keyboardState;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#saveState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public synchronized void saveState(ISettingSection settings) {
		synchronized (executionLock) {
			bExecuting = false;
			executionLock.notifyAll();
		}
		
		settings.put("Class", getClass());
		
		doSaveState(settings);
		
		DataFiles.saveState(settings);
		
		ISettingSection workspace = settings.addSection("Workspace");
		WorkspaceSettings.CURRENT.save(workspace);
		//WorkspaceSettings.CURRENT.saveState(settings);
		
		synchronized (executionLock) {
			bExecuting = true;
			executionLock.notifyAll();
		}
	}


	protected void doSaveState(ISettingSection settings) {
		settings.put("MachineModel", machineModel.getIdentifier());
		
		cpu.saveState(settings.addSection("CPU"));
		memory.saveState(settings.addSection("Memory"));
		vdp.saveState(settings.addSection("VDP"));
		if (sound != null)
			sound.saveState(settings.addSection("Sound"));
		if (speech != null)
			speech.saveState(settings.addSection("Speech"));
		if (moduleManager != null)
			moduleManager.saveState(settings.addSection("Modules"));
		if (dsrManager != null)
			dsrManager.saveState(settings.addSection("DSRs"));

		if (cruAccess != null)
			cruAccess.saveState(settings.addSection("CRU"));

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#loadState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public synchronized void loadState(ISettingSection section) throws IOException {
		/*
		machineRunner.interrupt();
		videoRunner.interrupt();
		timer.cancel();
		cpuTimer.cancel();
		videoTimer.cancel();
		*/
		bExecuting = false;
		synchronized (executionLock) {
			executionLock.notifyAll();
		}

		String origWorkspace = section.get(WorkspaceSettings.currentWorkspace.getName());
		if (origWorkspace != null) {
			try {
				WorkspaceSettings.loadFrom(origWorkspace);
			} catch (IOException e) {
				notifyEvent(Level.WARNING, 
						MessageFormat.format(
								"Could not find the workspace ''{0}'' referenced in the saved state",
								origWorkspace));
			}
		}
		
		ISettingSection workspace = section.getSection("Workspace");
		if (workspace != null) {
			WorkspaceSettings.CURRENT.load(workspace);
		}
		
		DataFiles.loadState(section);
		
		doLoadState(section);
		
		//Executor.settingDumpFullInstructions.setBoolean(true);
		
		//start();
		
		synchronized (executionLock) {
			bExecuting = true;
			executionLock.notifyAll();
		}
	}


	protected void doLoadState(ISettingSection section) {
		memory.getModel().resetMemory();
		//machineModel.getMemoryModel().initMemory(this);
		if (moduleManager != null)
			moduleManager.loadState(section.getSection("Modules"));
		memory.loadState(section.getSection("Memory"));
		cpu.loadState(section.getSection("CPU"));
		vdp.loadState(section.getSection("VDP"));
		if (sound != null)
			sound.loadState(section.getSection("Sound"));
		if (speech != null)
			speech.loadState(section.getSection("Speech"));
		keyboardState.resetKeyboard();
		keyboardState.resetJoystick();
		if (dsrManager != null)
			dsrManager.loadState(section.getSection("DSRs"));
		if (cruAccess != null)
			cruAccess.loadState(section.getSection("CRU"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getSound()
	 */
	@Override
	public ISoundChip getSound() {
		return sound;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.machine.IMachine#getSpeech()
	 */
	@Override
	public ISpeechChip getSpeech() {
		return speech;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getCpuTicksPerSec()
	 */
	@Override
	public int getCpuTicksPerSec() {
		return cpuTicksPerSec;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getExecutionLock()
	 */
	@Override
	public Object getExecutionLock() {
		return executionLock;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#isExecuting()
	 */
	@Override
	public boolean isExecuting() {
		synchronized (executionLock) {
			return bExecuting;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#asyncExec(java.lang.Runnable)
	 */
	@Override
	public void asyncExec(Runnable runnable) {
		runnableList.add(runnable);
		//synchronized (executionLock) {
		//	executionLock.notifyAll();
		//}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getCpuMetrics()
	 */
	@Override
	public ICpuMetrics getCpuMetrics() {
		return cpuMetrics;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getModuleManager()
	 */
	@Override
	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getVdp()
	 */
	@Override
	public IVdpChip getVdp() {
		return vdp;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getDsrManager()
	 */
	@Override
	public IDsrManager getDsrManager() {
		return dsrManager;
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getInstructionFactory()
	 */
	@Override
	public IRawInstructionFactory getInstructionFactory() {
		return instructionFactory;
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getModel()
	 */
	@Override
	public MachineModel getModel() {
		return machineModel;
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#keyStateChanged()
	 */
	@Override
	public void keyStateChanged() {
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getMachineTimer()
	 */
	@Override
	public Timer getMachineTimer() {
		return timer;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IBaseMachine#interrupt()
	 */
	@Override
	public void interrupt() {
		executor.interruptExecution = Boolean.TRUE;
	}

	/**
	 * @return the cruAccess
	 */
	public ICruAccess getCruAccess() {
		return cruAccess;
	}
	/**
	 * @param cruAccess the cruAccess to set
	 */
	public void setCruAccess(ICruAccess cruAccess) {
		this.cruAccess = cruAccess;
	}
}

