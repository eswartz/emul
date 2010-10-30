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
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.clients.builtin.NotifyException;
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
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.modules.IModule;
import v9t9.engine.modules.ModuleLoader;
import v9t9.engine.timer.FastTimer;
import v9t9.engine.timer.FastTimerTask;
import v9t9.keyboard.KeyboardState;
import v9t9.tools.asm.assembler.IInstructionFactory;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
abstract public class Machine {
    protected Memory memory;
    protected MemoryDomain console;
    protected  Cpu cpu;
    protected  Executor executor;
    protected Client client;
    protected  volatile boolean bAlive;
    protected Timer timer;
    protected FastTimer cpuTimer;
    //Timer cpuTimer;
    protected Timer videoTimer;
	private VdpHandler vdp;
	protected DsrManager dsrManager;

    protected long lastInterrupt = System.currentTimeMillis();
    protected long lastInfo = lastInterrupt;
    protected long upTime = 0;
    
    protected boolean allowInterrupts;
    protected final int clientTick = 1000 / 100;
    protected final int videoUpdateTick = 1000 / 30;
    protected final int cpuTicksPerSec = 100;
    protected long now;
    //private TimerTask vdpInterruptTask;
    protected  TimerTask clientTask;
    protected  FastTimerTask cpuTimingTask;
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
	
	static public final SettingProperty settingPauseMachine = new SettingProperty("PauseMachine", new Boolean(false));
	static public final SettingProperty settingThrottleInterrupts = new SettingProperty("ThrottleVDPInterrupts", new Boolean(false));
	
	static public final SettingProperty settingModuleList = new SettingProperty("ModuleListFile", new String("modules.xml"));
	
	protected  TimerTask memorySaverTask;
	protected  ModuleManager moduleManager;
	protected  List<IModule> modules;
	
	protected  RecordingEventNotifier recordingNotifier = new RecordingEventNotifier();
	private IInstructionFactory instructionFactory;
	private final MachineModel machineModel;
	private IPropertyListener pauseListener;

	
    public Machine(MachineModel machineModel) {
    	WorkspaceSettings.CURRENT.register(settingModuleList);
    	
    	pauseListener = new IPropertyListener() {
    		
    		public void propertyChanged(IProperty setting) {
    			executor.interruptExecution = true;
    			synchronized (executionLock) {
    				cpu.resetCycleCounts();
    				bExecuting = !setting.getBoolean();
    				executionLock.notifyAll();
    			}
    		}
    		
    	};
		settingPauseMachine.addListener(pauseListener);
    	
    	this.machineModel = machineModel;
    	
    	runnableList = new LinkedList<Runnable>();
    	this.memoryModel = machineModel.getMemoryModel();
    	this.memory = memoryModel.createMemory();
    	this.console = memoryModel.getConsole();
    	
    	init(machineModel);

    	machineModel.defineDevices(this);
    	
    	cpuMetrics = new CpuMetrics();
    	executor = machineModel.createExecutor(cpu, cpuMetrics);
    	
    	//executor.addInstructionListener(new DebugConditionListener(cpu));

	}


	protected void init(MachineModel machineModel) {
		sound = machineModel.createSoundProvider(this);
    	this.vdp = machineModel.createVdp(this);
    	memoryModel.initMemory(this);
    	
    	moduleManager = new ModuleManager(this, getModules());
    	
    	cpu = machineModel.createCPU(this); 
		keyboardState = new KeyboardState(this);

    	this.instructionFactory = machineModel.getInstructionFactory();
	}
    
    
    public List<IModule> getModules() {
    	if (modules == null) {
    		String dbName = settingModuleList.getString();
    		try {
				modules = ModuleLoader.loadModuleList(dbName);
    		} catch (NotifyException e) {
    			notifyEvent(e.getEvent());
    			notifyEvent(IEventNotifier.Level.ERROR,
    					"Be sure your " + DataFiles.settingBootRomsPath.getName() + " setting is established in "
						+ WorkspaceSettings.CURRENT.getConfigFilePath());
    			modules = Collections.emptyList();
			}
    	}
    	return modules;
    }

	public void notifyEvent(IEventNotifier.Level level, String string) {
		if (client != null)
			client.getEventNotifier().notifyEvent(this, level, string);
		else
			recordingNotifier.notifyEvent(this, level, string);
	}
	
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
    
    public Memory getMemory() {
        return memory;
    }
    
    public boolean isAlive() {
        return bAlive;
    }
    
    public void start() {
    	allowInterrupts = true;
    	
    	timer = new Timer();
    	cpuTimer = new FastTimer();
    	videoTimer = new Timer();

        // the CPU emulation task (a fast timer because 100 Hz is a little too much for Windows) 
        cpuTimingTask = new FastTimerTask() {

			@Override
        	public void run() {
				synchronized (executionLock) {
					if (!bExecuting)
						return;
				
	    			now = System.currentTimeMillis();
	    			//System.out.print(now);
	    			
	    	
	    			//System.out.print(now);
	    			
	    			if (now >= lastInfo + 1000) {
	    				upTime += now - lastInfo;
	    				executor.recordMetrics();
	    				executor.nVdpInterrupts = 0;
	    				lastInfo = now;
	    				//vdpInterruptDelta = 0;
	    			}
	    			
	    			sound.tick();
	    			cpu.tick();
	
	    			vdp.tick();
				}    			
        	}
        };
        cpuTimer.scheduleTask(cpuTimingTask, cpuTicksPerSec);
        
        
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
        videoTimer.schedule(videoUpdateTask, 0, videoUpdateTick);
        
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
    	        	synchronized (runnableList) {
	            		Runnable runnable;
	            		while (runnableList.size() > 0) {
	            			runnable = runnableList.remove(0);
	            			runnable.run();
	            		}
					}
	            	
    	        	
    	        	// delay if going too fast
    				if (Cpu.settingRealTime.getBoolean()) {
    					if (cpu.isThrottled() && cpu.getMachine().bAlive) {
    						// Just sleep.  Another timer thread will reset the throttle.
    						try {
    							Thread.sleep(100);
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
	            			if (bExecuting) {
	            				executor.execute();
	            			}
	            		}
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
    
	/**
     * Forcibly stop the machine and throw TerminatedException
     */
    public void stop() {
    	setNotRunning();
        throw new TerminatedException();
    }

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
        cpuTimer.cancel();
        videoTimer.cancel();
		try {
			videoRunner.join();
		} catch (InterruptedException e) {
		}
		
		memory.save();        
        getSound().getSoundHandler().dispose();
        if (dsrManager != null)
			dsrManager.dispose();
	}
    
	public Cpu getCpu() {
        return cpu;
    }
    public void setCpu(Cpu cpu) {
        this.cpu = cpu;
    }
    public Client getClient() {
        return client;
    }
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
    public Executor getExecutor() {
        return executor;
    }
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
    
    /** Get the primary memory */
    public MemoryDomain getConsole() {
		return console;
	}
    
    public MemoryModel getMemoryModel() {
        return memoryModel;
    }

    public KeyboardState getKeyboardState() {
		return keyboardState;
	}

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
		moduleManager.saveState(settings.addSection("Modules"));
		if (dsrManager != null)
			dsrManager.saveState(settings.addSection("DSRs"));
	}

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

	public SoundProvider getSound() {
		return sound;
	}

	public int getCpuTicksPerSec() {
		return cpuTicksPerSec;
	}

	public Object getExecutionLock() {
		return executionLock;
	}

	/**
	 * @return
	 */
	public boolean isExecuting() {
		synchronized (executionLock) {
			return bExecuting;
		}
	}

	public void asyncExec(Runnable runnable) {
		synchronized (runnableList) {
			runnableList.add(runnable);
		}
		//synchronized (executionLock) {
		//	executionLock.notifyAll();
		//}
	}

	public CpuMetrics getCpuMetrics() {
		return cpuMetrics;
	}

	/**
	 * @return the moduleManager
	 */
	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public VdpHandler getVdp() {
		return vdp;
	}

	public IDsrManager getDsrManager() {
		return dsrManager;
	}


	/**
	 * @return
	 */
	public IInstructionFactory getInstructionFactory() {
		return instructionFactory;
	}


	/**
	 * @return
	 */
	public MachineModel getModel() {
		return machineModel;
	}
}


