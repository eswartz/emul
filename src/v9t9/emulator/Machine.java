/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.DialogSettings;

import v9t9.emulator.clients.builtin.SoundTMS9919;
import v9t9.emulator.clients.builtin.video.tms9918a.VdpTMS9918A;
import v9t9.emulator.hardware.CruManager;
import v9t9.emulator.hardware.InternalCru9901;
import v9t9.emulator.hardware.MachineModel;
import v9t9.emulator.hardware.dsrs.DSRManager;
import v9t9.emulator.runtime.AbortedException;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;
import v9t9.engine.settings.SettingsCollection;
import v9t9.engine.timer.FastTimer;
import v9t9.engine.timer.FastTimerTask;
import v9t9.keyboard.KeyboardState;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
abstract public class Machine {
    SettingsCollection settings;
    protected Memory memory;
    protected MemoryDomain console;
    Cpu cpu;
    Executor executor;
    Client client;
    boolean bRunning;
    Timer timer;
    FastTimer cpuTimer;
    //Timer cpuTimer;
    Timer videoTimer;
 
    long lastInterrupt = System.currentTimeMillis();
    long lastInfo = lastInterrupt;
    long upTime = 0;
    
    boolean allowInterrupts;
    final int clientTick = 1000 / 100;
    final int videoUpdateTick = 1000 / 30;
    final int cpuTicksPerSec = 100;
    final int vdpInterruptsPerSec = 60;
    private long now;
    //private TimerTask vdpInterruptTask;
    private TimerTask clientTask;
    private FastTimerTask cpuTimingTask;
	protected MemoryModel memoryModel;
	private VdpHandler vdp;
	private CruManager cruManager;
	private DSRManager dsrManager;
	private TimerTask videoUpdateTask;
	private Thread machineRunner;
	private Thread videoRunner;
	protected int throttleCount;
	private KeyboardState keyboardState;
	protected Object executionLock = new Object();
	protected boolean bExecuting;
	private SoundTMS9919 sound;
	static public final String sPauseMachine = "PauseMachine";
	static public final Setting settingPauseMachine = new Setting(sPauseMachine, new Boolean(false));
	static public final String sThrottleInterrupts = "ThrottleVDPInterrupts";
	static public final Setting settingThrottleInterrupts = new Setting(sThrottleInterrupts, new Boolean(false));
	
    public Machine(MachineModel machineModel) {
    	this.memoryModel = machineModel.getMemoryModel();
    	this.memory = memoryModel.createMemory();
    	this.console = memoryModel.getConsole();
    	cruManager = new CruManager();
    	dsrManager = new DSRManager(this);
    	
    	sound = new SoundTMS9919(this);
    	this.vdp = machineModel.createVdp(this);
    	memoryModel.initMemory(this);
    	
    	settings = new SettingsCollection();
    	cpu = new Cpu(this, 1000 / cpuTicksPerSec, vdp);
    	keyboardState = new KeyboardState(cpu);
    	machineModel.defineDevices(this);
    	
    	executor = new Executor(cpu);
    	
    	settingPauseMachine.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				synchronized (executionLock) {
					bExecuting = !setting.getBoolean();
					executionLock.notifyAll();
				}
			}
        	
        });
	}

	public interface ConsoleMmioReader {
        byte read(int addrMask);
    }

    /* Memory areas */

    public interface ConsoleMmioWriter {
        void write(int addrMask, byte val);
    }


    public void close() {
        bRunning = false;
        if (client != null) {
			client.close();
		}
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
	protected void finalize() throws Throwable {
        super.finalize();
        client = null;
        memory = null;
        executor = null;
        settings = null;
    }
    
    public Memory getMemory() {
        return memory;
    }
    public SettingsCollection getSettings() {
        return settings;
    }
    
    public boolean isRunning() {
        return bRunning;
    }
    
    public void start() {
    	allowInterrupts = true;
    	
    	timer = new Timer();
    	cpuTimer = new FastTimer();
    	videoTimer = new Timer();

        // the CPU emulation task (a fast timer because 100 Hz is a little too much for Windows) 
        cpuTimingTask = new FastTimerTask() {

			private int vdpInterruptDelta;

			@Override
        	public void run() {
    			now = System.currentTimeMillis();
    			//System.out.print(now);
    			
    	
    			//System.out.print(now);
    			
    			if (now >= lastInfo + 1000) {
    				upTime += now - lastInfo;
    				executor.dumpStats();
    				lastInfo = now;
    				vdpInterruptDelta = 0;
    			}
    			
    			cpu.tick();

    			// In Win32, the timer is not nearly as accurate as 1/100 second,
    			// so we get a lot of interrupts at the same time.
    			
    			// Synchronize VDP interrupts along with the CPU in the same task
    			// so we don't succumb to misscheduling between different timers
    			// OR timer tasks.
    			if (bExecuting && !VdpTMS9918A.settingCpuSynchedVdpInterrupt.getBoolean()) {
	    			vdpInterruptDelta += vdpInterruptsPerSec * 65536 / cpuTicksPerSec;
	    			//System.out.print("[VDP delt:" + vdpInterruptDelta + "]");
	    			if (vdpInterruptDelta >= 65536) {
				
						vdpInterruptDelta -= 65536;
	    				vdp.tick();
	            		if (settingThrottleInterrupts.getBoolean()) {
	            			if (throttleCount-- < 0) {
	            				throttleCount = 60;
	            			} else {
	            				return;
	            			}
	            		}
	            		
	            		cpu.getCruAccess().triggerInterrupt(InternalCru9901.INT_VDP);
	            		executor.nVdpInterrupts++;
	            		//System.out.print('!');
	    			}
    			}
    			
    			sound.getSoundHandler().flushAudio();
    			
        	}
        };
        cpuTimer.scheduleTask(cpuTimingTask, cpuTicksPerSec);
        
        
        videoRunner = new Thread("Video Runner") {
        	@Override
        	public void run() {
        		while (Machine.this.isRunning()) {
	        		// delay if going too fast
	    			while (vdp.isThrottled() && bRunning) {
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
        
        bRunning = true;
        
      	// the machine (well, actually, 9900) runner
		machineRunner = new Thread("Machine Runner") {
        	@Override
        	public void run() {
    	        while (Machine.this.isRunning()) {
    	            try {
    	            	// synchronize on events like debugging, loading/saving, etc
	            		synchronized (executionLock) {
	            			while (!bExecuting) {
	            				executionLock.wait();
	            			}
	    	            	// delay if going too fast
	    	        		if (Cpu.settingRealTime.getBoolean()) {
	    	        			while (cpu.isThrottled() && bRunning) {
	    	        				// Just sleep.  Another timer thread will reset the throttle.
	    	        				try {
	    	        					Thread.sleep(10);
	    	        				} catch (InterruptedException e) {
	    	        					break;
	    	        				}
	    	        			}
	    	        		}
	    	        		executor.execute();
	            		}
    	            } catch (AbortedException e) {
    	                
    	            } catch (Throwable t) {
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
		synchronized (executionLock) {
			bExecuting = false;
			executionLock.notifyAll();
		}
		bRunning = false;
		machineRunner.interrupt();
		videoRunner.interrupt();
        timer.cancel();
        cpuTimer.cancel();
        videoTimer.cancel();
        getSound().getSoundHandler().dispose();
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

    public VdpHandler getVdp() {
		return vdp;
	}

	public CruManager getCruManager() {
		return cruManager;
	}

	public DSRManager getDSRManager() {
		return dsrManager;
	}

	public CruHandler getCru() {
		return cruManager;
	}

	public KeyboardState getKeyboardState() {
		return keyboardState;
	}

	public synchronized void saveState(String filename) throws IOException {
		synchronized (executionLock) {
			bExecuting = false;
			executionLock.notifyAll();
		}
		DialogSettings settings = new DialogSettings("state");
		cpu.saveState(settings.addNewSection("CPU"));
		memory.saveState(settings.addNewSection("Memory"));
		vdp.saveState(settings.addNewSection("VDP"));
		sound.saveState(settings.addNewSection("Sound"));
		settings.save(filename);
		synchronized (executionLock) {
			bExecuting = true;
			executionLock.notifyAll();
		}
	}

	public synchronized void restoreState(String filename) throws IOException {
		/*
		machineRunner.interrupt();
		videoRunner.interrupt();
		timer.cancel();
		cpuTimer.cancel();
		videoTimer.cancel();
		*/
		synchronized (executionLock) {
			bExecuting = false;
			executionLock.notifyAll();
		}
		
		DialogSettings settings = new DialogSettings("state");
		settings.load(filename);
		
		memory.loadState(settings.getSection("Memory"));
		cpu.loadState(settings.getSection("CPU"));
		vdp.loadState(settings.getSection("VDP"));
		sound.loadState(settings.getSection("Sound"));
		keyboardState.resetKeyboard();
		
		//Executor.settingDumpFullInstructions.setBoolean(true);
		
		//start();
		
		synchronized (executionLock) {
			bExecuting = true;
			executionLock.notifyAll();
		}
	}

	public SoundTMS9919 getSound() {
		return sound;
	}

	public int getCpuTicksPerSec() {
		return cpuTicksPerSec;
	}
}


