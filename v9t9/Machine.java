/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;

import v9t9.cpu.Cpu;
import v9t9.cpu.Executor;
import v9t9.vdp.Handler;

/** Encapsulate all the information about a running emulated machine.
 * @author ejs
 */
public class Machine {
    SettingsCollection settings;
    Memory memory;
    Cpu cpu;
    Executor executor;
    Client client;
    Cru cru;
    boolean bRunning;
    
    public Machine() {
        settings = new SettingsCollection(Globals.settings);
        client = new DummyClient();
        memory = new Memory(this);
        cpu = new Cpu(this);
        executor = new Executor(cpu, false /*interpret*/);
        
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x0, 0x2000, "CPU ROM", memory.CPU,
                "/usr/local/src/v9t9-data/roms/994arom.bin", 0x0, false, false));
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x0, 0x6000, "CPU GROM", memory.GRAPHICS,
                "/usr/local/src/v9t9-data/roms/994agrom.bin", 0x0, false, false));
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x6000, 0, "Parsec", memory.GRAPHICS,
                "/usr/local/src/v9t9-data/modules/meteorg.bin", 0x0, false, false));
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x6000, 0, "Parsec", memory.CPU,
                "/usr/local/src/v9t9-data/modules/meteorc.bin", 0x0, false, false));
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x6000, 0, "Parsec", memory.GRAPHICS,
                "/usr/local/src/v9t9-data/modules/parsecg.bin", 0x0, false, false));
        memory.addAndMap(DiskMemoryEntry.newFromFile(0x6000, 0, "Parsec", memory.CPU,
                "/usr/local/src/v9t9-data/modules/parsecc.bin", 0x0, false, false));

        memory.addAndMap(DiskMemoryEntry.newFromFile(0x6000, 0, "Diags", memory.GRAPHICS,
               "/usr/local/src/v9t9-data/modules/diagsg.bin", 0x0, false, false));

        bRunning = true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
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
    
    public void stop() {
        bRunning = false;
    }
    
    public static void main(String args[]) {
        Machine machine = new Machine();
        machine.setClient(new DemoClient(machine));
        machine.setCru((Cru)machine.getClient());
        
        machine.cpu.contextSwitch(0);
        long lastInterrupt = System.currentTimeMillis();
        long lastInfo = lastInterrupt;
        long upTime = 0;
        
        boolean allowInterrupts = false;
        allowInterrupts = true;
        
        final int interruptTick = 1000 / 30;	// TODO: non-reduced rate
        while (machine.isRunning()) {
            long now = System.currentTimeMillis(); 
            if (allowInterrupts && now >= lastInterrupt + interruptTick) { 
                machine.cpu.holdpin(Cpu.INTPIN_INTREQ);
                lastInterrupt += interruptTick;
            }
            if (now >= lastInfo + 1000) {
                upTime += now - lastInfo;
                System.out.println("# instructions / second: " + machine.executor.nInstructions);
                machine.executor.nInstructions = 0;
                lastInfo = now;
            }
            machine.executor.execute();
        }
        //machine = null;
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
    public Cru getCru() {
        return cru;
    }
    public void setCru(Cru cru) {
        this.cru = cru;
    }
    public Executor getExecutor() {
        return executor;
    }
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}

class DummyClient extends Client {
    v9t9.vdp.Handler video;
    sound.Handler sound;
    
    public DummyClient() {
        video = new v9t9.vdp.Handler() {

            public void writeVdpReg(byte reg, byte val, byte old) {
            }

            public byte readVdpStatus() {
                return 0;
            }

            public void writeVdpMemory(short vdpaddr, byte val) {
            }
        };
        sound = new sound.Handler() {
            public void writeSound(byte val) {
            }
        };
    }

    /* (non-Javadoc)
     * @see v9t9.Client#getVideo()
     */
    public Handler getVideo() {
        // TODO Auto-generated method stub
        return video;
    }

    /* (non-Javadoc)
     * @see v9t9.Client#setVideo(vdp.Handler)
     */
    public void setVideo(Handler video) {
        // TODO Auto-generated method stub
        this.video = video;
    }
    
    /* (non-Javadoc)
     * @see v9t9.Client#close()
     */
    void close() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see v9t9.Client#timerTick()
     */
    public void timerTick() {
        // TODO Auto-generated method stub
        
    }

    public sound.Handler getSound() {
        return sound;
    }
    public void setSound(sound.Handler sound) {
        this.sound = sound;
    }
}