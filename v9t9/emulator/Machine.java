/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.emulator;

import java.util.Timer;
import java.util.TimerTask;

import v9t9.emulator.handlers.CruHandler;
import v9t9.emulator.runtime.Cpu;
import v9t9.emulator.runtime.Executor;
import v9t9.emulator.runtime.TerminatedException;
import v9t9.engine.Client;
import v9t9.engine.DummyClient;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.settings.SettingsCollection;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
abstract public class Machine {
    SettingsCollection settings;
    protected Memory memory;
    Cpu cpu;
    Executor executor;
    Client client;
    CruHandler cru;
    boolean bRunning;
    Timer timer;
 
    /* CPU RAM */
    public MemoryDomain CPU = new MemoryDomain();
    

    long lastInterrupt = System.currentTimeMillis();
    long lastInfo = lastInterrupt;
    long upTime = 0;
    
    boolean allowInterrupts;
    final int interruptTick = 1000 / 30;    // TODO: non-reduced rate
    private long now;
    private TimerTask interruptTask;

    public Machine() {
        timer = new Timer();
        settings = new SettingsCollection();
        client = new DummyClient();
        createMemory();
        cpu = new Cpu(this);
        executor = new Executor(cpu);
        
        setupDefaults();
        loadMemory();
        allowInterrupts = true;
        interruptTask = new TimerTask() {

            @Override
			public void run() {
                handleTimerInterrupt();
            }
        };
        timer.scheduleAtFixedRate(interruptTask, 0, interruptTick);

        bRunning = true;
    }

    /**
     * Create the memory profile for the emulated computer.
     * Set 'this.memory'
     */
    abstract protected void createMemory();

    /**
     * Load ROMs, etc. into memory 
     */
    abstract protected void loadMemory();

    /* Memory areas */

    public interface ConsoleMmioReader {
        byte read(int addrMask);
    }

    /* Memory areas */

    public interface ConsoleMmioWriter {
        void write(int addrMask, byte val);
    }

    /** Set default settings */
    abstract protected void setupDefaults();

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
    
    protected void run() throws Throwable {
    	executor.execute();
    }

    protected void handleTimerInterrupt() {
        if (allowInterrupts) { 
            cpu.holdpin(Cpu.INTPIN_INTREQ);
        }
        now = System.currentTimeMillis();
        if (now >= lastInfo + 1000) {
            upTime += now - lastInfo;
            executor.dumpStats();
            lastInfo = now;
        }
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
}


