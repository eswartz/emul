/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator;

import java.util.Timer;
import java.util.TimerTask;

import v9t9.emulator.hardware.MachineModel;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.CruHandler;
import v9t9.engine.VdpHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryModel;
import v9t9.engine.settings.SettingsCollection;

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
    CruHandler cru;
    boolean bRunning;
    Timer timer;
 
    long lastInterrupt = System.currentTimeMillis();
    long lastInfo = lastInterrupt;
    long upTime = 0;
    
    boolean allowInterrupts;
    final int interruptTick = 1000 / 60;
    final int clientTick = 1000 / 60;
    final int cpuTick = 1000 / 100;
    private long now;
    private TimerTask vdpInterruptTask;
    private TimerTask clientTask;
    private TimerTask cpuTask;
	protected MemoryModel memoryModel;
	private VdpHandler vdp;
	
    public Machine(MachineModel machineModel) {
    	this.memoryModel = machineModel.getMemoryModel();
    	this.memory = memoryModel.createMemory();
    	this.console = memoryModel.getConsole();
    	this.vdp = machineModel.createVdp(this);
    	memoryModel.initMemory(this);
    	
    	settings = new SettingsCollection();
    	cpu = new Cpu(this, cpuTick);
    	executor = new Executor(cpu);
    	timer = new Timer();
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
        vdpInterruptTask = new TimerTask() {

            @Override
			public void run() {
            	vdp.tick();
            	if (allowInterrupts) { 
            		cpu.holdpin(Cpu.INTPIN_INTREQ);
            	}
            }
        };
        timer.scheduleAtFixedRate(vdpInterruptTask, 0, interruptTick);
        
        clientTask = new TimerTask() {
        	
        	@Override
        	public void run() {
        		if (client != null) client.timerInterrupt();
        	}
        };
        timer.scheduleAtFixedRate(clientTask, 0, clientTick);
        
        cpuTask = new TimerTask() {
        	
        	@Override
        	public void run() {
        		now = System.currentTimeMillis();

                if (now >= lastInfo + 1000) {
                    upTime += now - lastInfo;
                    //executor.dumpStats();
                    lastInfo = now;
                }
                
                cpu.tick();
        	}
        };
        timer.scheduleAtFixedRate(cpuTask, 0, cpuTick);

        bRunning = true;
    }
    
    /**
     * Forcibly stop the machine and throw TerminatedException
     */
    public void stop() {
    	setNotRunning();
        throw new TerminatedException();
    }

	public void setNotRunning() {
		bRunning = false;
        timer.cancel();
	}
    
    public void run() throws Throwable {
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
}


