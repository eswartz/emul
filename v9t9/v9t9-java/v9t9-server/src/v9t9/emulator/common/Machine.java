/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator.common;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.timer.FastTimer;

import v9t9.emulator.clients.builtin.SoundProvider;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.hardware.MachineModel;
import v9t9.emulator.hardware.dsrs.DsrManager;
import v9t9.emulator.hardware.dsrs.IDsrManager;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.emulator.runtime.cpu.AbortedException;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.emulator.runtime.cpu.CpuMetrics;
import v9t9.emulator.runtime.cpu.Executor;
import v9t9.engine.Client;
import v9t9.engine.VdpHandler;
import v9t9.engine.cpu.IRawInstructionFactory;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;
import v9t9.keyboard.KeyboardState;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
abstract public class Machine implements IMachine {
    protected Memory memory;
    protected MemoryDomain console;
    protected  Cpu cpu;
    protected  Executor executor;
    protected Client client;
    protected  volatile boolean bAlive;
    protected Timer timer;
    protected FastTimer fastTimer;
    //Timer cpuTimer;
    //protected Timer videoTimer;
	private VdpHandler vdp;
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
	protected  SoundProvider sound;
	private List<Runnable> runnableList;
	private CpuMetrics cpuMetrics;
	
	protected  TimerTask memorySaverTask;
	protected  ModuleManager moduleManager;
	
	protected  RecordingEventNotifier recordingNotifier = new RecordingEventNotifier();
	private IRawInstructionFactory instructionFactory;
	private final MachineModel machineModel;
	private IPropertyListener pauseListener;
	private Runnable soundTimingTask;

	
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
    	this.memory = memoryModel.createMemory();
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
		sound = machineModel.createSoundProvider(this);
    	this.vdp = machineModel.createVdp(this);
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

	public interface ConsoleMmioReader {
        byte read(int addrMask);
    }

    /* Memory areas */

    public interface ConsoleMmioWriter {
        void write(int addrMask, byte val);
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
	    			
	    			//sound.tick();
	    			cpu.tick();
	
	    			vdp.tick();
				}    			
        	}
        };
        fastTimer.scheduleTask(cpuTimingTask, cpuTicksPerSec);
        
        soundTimingTask = new Runnable() {

			@Override
        	public void run() {
				if (!bExecuting)
					return;
			
    			sound.tick();
        	}
        };
        fastTimer.scheduleTask(soundTimingTask, cpuTicksPerSec);

        
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
    				if (Cpu.settingRealTime.getBoolean()) {
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
	 * @see v9t9.emulator.common.IMachine#getCpu()
	 */
	@Override
	public Cpu getCpu() {
        return cpu;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setCpu(v9t9.emulator.runtime.cpu.Cpu)
	 */
    @Override
	public void setCpu(Cpu cpu) {
        this.cpu = cpu;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getClient()
	 */
    @Override
	public Client getClient() {
        return client;
    }
    /* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#setClient(v9t9.engine.Client)
	 */
    @Override
	public void setClient(Client client) {
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
	 * @see v9t9.emulator.common.IMachine#saveState(org.ejs.coffee.core.settings.ISettingSection)
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
		sound.saveState(settings.addSection("Sound"));
		if (moduleManager != null)
			moduleManager.saveState(settings.addSection("Modules"));
		if (dsrManager != null)
			dsrManager.saveState(settings.addSection("DSRs"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#loadState(org.ejs.coffee.core.settings.ISettingSection)
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
		sound.loadState(section.getSection("Sound"));
		keyboardState.resetKeyboard();
		keyboardState.resetJoystick();
		if (dsrManager != null)
			dsrManager.loadState(section.getSection("DSRs"));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IMachine#getSound()
	 */
	@Override
	public SoundProvider getSound() {
		return sound;
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
	public CpuMetrics getCpuMetrics() {
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
	public VdpHandler getVdp() {
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
	
}


