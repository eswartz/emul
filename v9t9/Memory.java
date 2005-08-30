/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *  
 */
package v9t9;

import java.util.Iterator;

/*
 * @author ejs
 */
public class Memory {

    /* CPU RAM */
    public MemoryDomain CPU = new MemoryDomain();

    /* GPL ROM/RAM */
    public MemoryDomain GRAPHICS = new MemoryDomain();

    /* VDP RAM */
    public MemoryDomain VIDEO = new MemoryDomain();

    /* Speech ROM */
    public MemoryDomain SPEECH = new MemoryDomain();

    public boolean bHasExpRam; /* is 32k expansion RAM enabled? */

    public boolean bHasEnhRam; /* is enhanced CPU RAM enabled? */

    public MemoryMap map;

    public static ZeroMemoryArea zeroMemoryArea = new ZeroMemoryArea();

    private Machine machine;
    
    private v9t9.vdp.Vdp vdpMmio;
    
    private v9t9.sound.Sound soundMmio;
    
    private java.util.List listeners;
    
    public interface Listener {
        void notifyMemoryMapChanged(MemoryEntry entry);
    }
    
    void addListener(Listener listener) {
        listeners.add(listener);
    }

    void removeListener(Listener listener) {
        listeners.remove(listener);
    }

   void notifyListeners(MemoryEntry entry) {
       if (listeners != null)
        for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            Listener element = (Listener) iter.next();
            element.notifyMemoryMapChanged(entry);
        }
    }
    
    public void addAndMap(MemoryEntry entry) {
        map.add(entry);
        entry.map();
        notifyListeners(entry);
    }
    
    static public final String sExpRam = "MemoryExpansion32K";
    static public final String sEnhRam = "ExtraConsoleRAM";

    private Gpl gplMmio;
    
    Memory(Machine machine) {
        listeners = new java.util.ArrayList();
        this.machine = machine;
        machine.settings.register(sExpRam, new Setting(new Boolean(false)));
        machine.settings.register(sEnhRam, new Setting(new Boolean(false)));
        
        map = new MemoryMap();

        map.add(CPU);
        map.add(VIDEO);
        map.add(GRAPHICS);
        map.add(SPEECH);

        initializeRam();
        initializeMmio();
        
    }
    
     /**
     *  
     */
    private void initializeMmio() {
        vdpMmio = new v9t9.vdp.Vdp(machine);
        gplMmio = new Gpl(machine);
        soundMmio = new v9t9.sound.Sound(machine);
        
        addAndMap(new MemoryEntry("Sound MMIO", CPU, 0x8400, 0x0400,
                new ConsoleSoundArea(soundMmio)));
        addAndMap(new MemoryEntry("VDP Read MMIO", CPU, 0x8800, 0x0400,
                new ConsoleVdpReadArea(vdpMmio)));
        addAndMap(new MemoryEntry("VDP Write MMIO", CPU, 0x8C00, 0x0400,
                new ConsoleVdpWriteArea(vdpMmio)));
        addAndMap(new MemoryEntry("Speech Read MMIO", CPU, 0x9000, 0x0400,
                new ConsoleSpeechReadArea()));
        addAndMap(new MemoryEntry("Speech Write MMIO", CPU, 0x9400, 0x0400,
                new ConsoleSpeechWriteArea()));
        addAndMap(new MemoryEntry("GROM Read MMIO", CPU, 0x9800, 0x0400,
                new ConsoleGromReadArea(gplMmio)));
        addAndMap(new MemoryEntry("GRAM Write MMIO", CPU, 0x9C00, 0x0400,
                new ConsoleGramWriteArea(gplMmio)));

    }

    /**
     *  
     */
    private void initializeRam() {
        addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
                0x2000, new ExpRamArea(machine, 0x2000)));
        addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
                new StdConsoleRamArea(machine)));
        addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
                0x6000, new ExpRamArea(machine, 0x6000)));
        
        addAndMap(new MemoryEntry("VDP RAM", VIDEO, 0x0000, 0x4000, new VdpRamArea(machine)));

    }
 
    /* Memory areas */

    public interface ConsoleMmioReader {
        byte read(int addrMask);
    }

    /* Memory areas */

    public interface ConsoleMmioWriter {
        void write(int addrMask, byte val);
    }
    public v9t9.sound.Sound getSoundMmio() {
        return soundMmio;
    }
    public void setSoundMmio(v9t9.sound.Sound soundMmio) {
        this.soundMmio = soundMmio;
    }
    public v9t9.vdp.Vdp getVdpMmio() {
        return vdpMmio;
    }
    public void setVdpMmio(v9t9.vdp.Vdp vdpMmio) {
        this.vdpMmio = vdpMmio;
    }
    public Gpl getGplMmio() {
        return gplMmio;
    }
    public void setGplMmio(Gpl gplMmio) {
        this.gplMmio = gplMmio;
    }
  }

/* Memory areas */

class ConsoleMemoryArea extends MemoryArea {
    ConsoleMemoryArea() {
        bWordAccess = true;
    }
}

class StdConsoleRamArea extends ConsoleMemoryArea {
    StdConsoleRamArea(Machine machine) {
        memory = new byte[0x400];
        read = memory;
        write = memory;

        final Setting sEnhRam = machine.settings.find(Memory.sEnhRam);

        /*
         * standard console RAM masks the addresses to 0x100 bytes; this is
         * conventionally at 0x8300
         */
        class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
            public byte readByte(MemoryArea area, int addr) {
                return area.flatReadByte(sEnhRam.getBool() ? addr
                        & (AREASIZE - 1) : ((addr & 0xff) + 0x0300));
            }
            public short readWord(MemoryArea area, int addr) {
                return area.flatReadWord(sEnhRam.getBool() ? addr
                        & (AREASIZE - 1) : ((addr & 0xff) + 0x0300));
            }
            public void writeByte(MemoryArea area, int addr, byte val) {
                area.flatWriteByte(sEnhRam.getBool() ? addr & (AREASIZE - 1)
                        : ((addr & 0xff) + 0x0300), val);
            }
            public void writeWord(MemoryArea area, int addr, short val) {
                area.flatWriteWord(sEnhRam.getBool() ? addr & (AREASIZE - 1)
                        : ((addr & 0xff) + 0x0300), val);
            }
            
        }
        AreaHandlers handlers = new AreaHandlers();
        areaReadByte = handlers;
        areaReadWord = handlers;
        areaWriteByte = handlers;
        areaWriteWord = handlers;
    }
}

class ExpRamArea extends ConsoleMemoryArea {
    final Setting sExpRam;
	public boolean hasRamAccess() {
		return sExpRam.getBool();
	}

    ExpRamArea(Machine machine, int size) {
        if (!(size == 0x2000 || size == 0x6000))
            throw new IllegalArgumentException("unexpected expanded RAM size");

        sExpRam = machine.settings.find(Memory.sExpRam);
        memory = new byte[size];
        read = memory;
        write = memory;

        /* only allow access if expansion memory is on */
        class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
            public byte readByte(MemoryArea area, int addr) {
                return sExpRam.getBool() ? area.flatReadByte(addr) : 0;
            }
            public short readWord(MemoryArea area, int addr) {
                return sExpRam.getBool() ? area.flatReadWord(addr) : 0;
            }
            public void writeByte(MemoryArea area, int addr, byte val) {
                if (sExpRam.getBool())
                    area.flatWriteByte(addr, val);
            }
            public void writeWord(MemoryArea area, int addr, short val) {
                if (sExpRam.getBool())
                    area.flatWriteWord(addr, val);
            }
        }
        
        AreaHandlers handlers = new AreaHandlers();
        areaReadByte = handlers;
        areaReadWord = handlers;
        areaWriteByte = handlers;
        areaWriteWord = handlers;
    }
}

class ConsoleMmioReadArea extends ConsoleMemoryArea {
    public ConsoleMmioReadArea(final Memory.ConsoleMmioReader reader) {
        if (reader == null)
            throw new NullPointerException();

        memory = ZeroMemoryArea.zeroes;

        areaReadByte = new AreaReadByte() {
            public byte readByte(MemoryArea area, int addr) {
                //System.out.println("read byte from "
                //		+ Integer.toHexString(addr));
                if (0 == (addr & 1))
                    return reader.read(addr & 2);
                else
                    return 0;
            }
        };
    }
}

class ConsoleMmioWriteArea extends ConsoleMemoryArea {
    ConsoleMmioWriteArea(final Memory.ConsoleMmioWriter writer) {
        if (writer == null)
            throw new NullPointerException();

        memory = ZeroMemoryArea.zeroes;

        areaWriteByte = new AreaWriteByte() {
            public void writeByte(MemoryArea area, int addr, byte val) {
                //System.out.println("wrote addr " + Integer.toHexString(addr)
                // + "="
                //	+ Integer.toHexString(val));
                if (0 == (addr & 1)) {
                    writer.write((addr & 2), val);
                }
            }
        };
    };
}

class DummyConsoleMmioHandler implements Memory.ConsoleMmioReader,
        Memory.ConsoleMmioWriter {

    public byte read(int addrMask) {
        return 0;
    }

    public void write(int addrMask, byte val) {

    }
}

class ConsoleSoundArea extends ConsoleMmioWriteArea {
    public ConsoleSoundArea(v9t9.sound.Sound mmio) {
        super(mmio);
    }
}

class ConsoleVdpReadArea extends ConsoleMmioReadArea {
    public ConsoleVdpReadArea(v9t9.vdp.Vdp mmio) {
        super(mmio);
    }
}

class ConsoleVdpWriteArea extends ConsoleMmioWriteArea {
    public ConsoleVdpWriteArea(v9t9.vdp.Vdp mmio) {
        super(mmio);
    }
}

class ConsoleGromReadArea extends ConsoleMmioReadArea {
    public ConsoleGromReadArea(Gpl mmio) {
        super(mmio);
    }
}

class ConsoleGramWriteArea extends ConsoleMmioWriteArea {
    public ConsoleGramWriteArea(Gpl mmio) {
        super(mmio);
    }
}

class ConsoleSpeechReadArea extends ConsoleMmioReadArea {
    public ConsoleSpeechReadArea() {
        super(new DummyConsoleMmioHandler()); // TODO
    }
}

class ConsoleSpeechWriteArea extends ConsoleMmioWriteArea {
    public ConsoleSpeechWriteArea() {
        super(new DummyConsoleMmioHandler()); // TODO
    }
}

class VdpRamArea extends MemoryArea {
    VdpRamArea(Machine machine) {
        memory = new byte[0x4000];
        read = memory;
        write = memory;
    }
}
