/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.memory;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.Client;
import v9t9.engine.settings.Setting;

/**
 * The standard TI-99/4[A] console memory map.
 * @author ejs
 */
public class StandardConsoleMemoryModel {
    /* CPU ROM/RAM */
    public MemoryDomain CPU;

    /* GPL ROM/RAM */
    public MemoryDomain GRAPHICS;

    /* VDP RAM */
    public MemoryDomain VIDEO;

    /* Speech ROM */
    public MemoryDomain SPEECH;

    
    public boolean bHasExpRam; /* is 32k expansion RAM enabled? */

    public boolean bHasEnhRam; /* is enhanced this RAM enabled? */

    static public final String sExpRam = "MemoryExpansion32K";
    static public final String sEnhRam = "ExtraConsoleRAM";
    static public final Setting settingExpRam = new Setting(sExpRam, new Boolean(false));
    static public final Setting settingEnhRam = new Setting(sEnhRam, new Boolean(false));

    public Vdp vdpMmio;
    
    public Sound soundMmio;
    
    public Speech speechMmio;
    public Gpl gplMmio;

    private Client client;

	private Memory memory;
    
    public StandardConsoleMemoryModel(Memory memory) {
    	this.memory = memory;
        CPU = new MemoryDomain();
        GRAPHICS = new MemoryDomain();
        VIDEO = new MemoryDomain();
        SPEECH = new MemoryDomain();

        memory.addDomain(CPU);
        memory.addDomain(VIDEO);
        memory.addDomain(GRAPHICS);
        memory.addDomain(SPEECH);
        
        memory.addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
                0x2000, new ExpRamArea(0x2000)));
        memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
                new StdConsoleRamArea()));
        memory.addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
                0x6000, new ExpRamArea(0x6000)));
     
        memory.addAndMap(new MemoryEntry("VDP RAM", VIDEO, 0x0000, 0x4000, 
                new VdpRamArea()));

    }

    public void connectClient(Client client) {
        this.client = client;

        vdpMmio = new v9t9.engine.memory.Vdp(VIDEO, this.client);
        gplMmio = new Gpl(GRAPHICS);
        soundMmio = new v9t9.engine.memory.Sound(this.client);
        speechMmio = new Speech(this.client);

        vdpMmio.setClient(client);
        soundMmio.setClient(client);
        speechMmio.setClient(client);

        this.memory.addAndMap(new MemoryEntry("Sound MMIO", CPU, 0x8400, 0x0400,
                new ConsoleSoundArea(soundMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Read MMIO", CPU, 0x8800, 0x0400,
                new ConsoleVdpReadArea(vdpMmio)));
        this.memory.addAndMap(new MemoryEntry("VDP Write MMIO", CPU, 0x8C00, 0x0400,
                new ConsoleVdpWriteArea(vdpMmio)));
        this.memory.addAndMap(new MemoryEntry("Speech Read MMIO", CPU, 0x9000, 0x0400,
                new ConsoleSpeechReadArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("Speech Write MMIO", CPU, 0x9400, 0x0400,
                new ConsoleSpeechWriteArea(speechMmio)));
        this.memory.addAndMap(new MemoryEntry("GROM Read MMIO", CPU, 0x9800, 0x0400,
                new ConsoleGromReadArea(gplMmio)));
        this.memory.addAndMap(new MemoryEntry("GRAM Write MMIO", CPU, 0x9C00, 0x0400,
                new ConsoleGramWriteArea(gplMmio)));
        
        this.memory.addAndMap(new MemoryEntry("Low 8K expansion RAM", CPU, 0x2000,
                0x2000, new ExpRamArea(0x2000)));
        this.memory.addAndMap(new MemoryEntry("Console RAM", CPU, 0x8000, 0x0400,
                new StdConsoleRamArea()));
        this.memory.addAndMap(new MemoryEntry("High 24K expansion RAM", CPU, 0xA000,
                0x6000, new ExpRamArea(0x6000)));
     
        this.memory.addAndMap(new MemoryEntry("VDP RAM", VIDEO, 0x0000, 0x4000, 
                new VdpRamArea()));

    }
    
    /* Memory areas */
    class ConsoleMemoryArea extends WordMemoryArea {
        ConsoleMemoryArea() {
            bWordAccess = true;
        }
    }

    class StdConsoleRamArea extends ConsoleMemoryArea {
        StdConsoleRamArea() {
            memory = new short[0x400/2];
            read = memory;
            write = memory;

            /*
             * standard console RAM masks the addresses to 0x100 bytes; this is
             * conventionally at 0x8300
             */
            class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
                public byte readByte(MemoryArea area, int addr) {
                    return area.flatReadByte(settingEnhRam.getBoolean() ? addr
                            & AREASIZE - 1 : (addr & 0xff) + 0x0300);
                }
                public short readWord(MemoryArea area, int addr) {
                    return area.flatReadWord(settingEnhRam.getBoolean() ? addr
                            & AREASIZE - 1 : (addr & 0xff) + 0x0300);
                }
                public void writeByte(MemoryArea area, int addr, byte val) {
                    area.flatWriteByte(settingEnhRam.getBoolean() ? addr & AREASIZE - 1
                            : (addr & 0xff) + 0x0300, val);
                }
                public void writeWord(MemoryArea area, int addr, short val) {
                    area.flatWriteWord(settingEnhRam.getBoolean() ? addr & AREASIZE - 1
                            : (addr & 0xff) + 0x0300, val);
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
        @Override
		public boolean hasWriteAccess() {
            return settingExpRam.getBoolean();
        }

        ExpRamArea(int size) {
            if (!(size == 0x2000 || size == 0x6000)) {
				throw new IllegalArgumentException("unexpected expanded RAM size");
			}

            memory = new short[size/2];
            read = memory;
            write = memory;

            /* only allow access if expansion memory is on */
            class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
                public byte readByte(MemoryArea area, int addr) {
                    return settingExpRam.getBoolean() ? area.flatReadByte(addr) : 0;
                }
                public short readWord(MemoryArea area, int addr) {
                    return settingExpRam.getBoolean() ? area.flatReadWord(addr) : 0;
                }
                public void writeByte(MemoryArea area, int addr, byte val) {
                    if (settingExpRam.getBoolean()) {
						area.flatWriteByte(addr, val);
					}
                }
                public void writeWord(MemoryArea area, int addr, short val) {
                    if (settingExpRam.getBoolean()) {
						area.flatWriteWord(addr, val);
					}
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
        public ConsoleMmioReadArea(final ConsoleMmioReader reader) {
            if (reader == null) {
				throw new NullPointerException();
			}

            memory = ZeroWordMemoryArea.zeroes;

            areaReadByte = new AreaReadByte() {
                public byte readByte(MemoryArea area, int addr) {
                    //System.out.println("read byte from "
                    //      + Integer.toHexString(addr));
                    if (0 == (addr & 1)) {
						return reader.read(addr & 2);
					} else {
						return 0;
					}
                }
            };
        }
    }

    class ConsoleMmioWriteArea extends ConsoleMemoryArea {
        ConsoleMmioWriteArea(final ConsoleMmioWriter writer) {
            if (writer == null) {
				throw new NullPointerException();
			}

            memory = ZeroWordMemoryArea.zeroes;

            areaWriteByte = new AreaWriteByte() {
                public void writeByte(MemoryArea area, int addr, byte val) {
                    //System.out.println("wrote addr " + Integer.toHexString(addr)
                    // + "="
                    //  + Integer.toHexString(val));
                    if (0 == (addr & 1)) {
                        writer.write((addr & 2), val);
                    }
                }
            };
        };
    }

    class DummyConsoleMmioHandler implements ConsoleMmioReader,
            ConsoleMmioWriter {

        public byte read(int addrMask) {
            return 0;
        }

        public void write(int addrMask, byte val) {

        }
    }

    class ConsoleSoundArea extends ConsoleMmioWriteArea {
        public ConsoleSoundArea(v9t9.engine.memory.Sound mmio) {
            super(mmio);
        }
    }

    class ConsoleVdpReadArea extends ConsoleMmioReadArea {
        public ConsoleVdpReadArea(v9t9.engine.memory.Vdp mmio) {
            super(mmio);
        }
    }

    class ConsoleVdpWriteArea extends ConsoleMmioWriteArea {
        public ConsoleVdpWriteArea(v9t9.engine.memory.Vdp mmio) {
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
        public ConsoleSpeechReadArea(Speech mmio) {
            super(mmio);
        }
    }

    class ConsoleSpeechWriteArea extends ConsoleMmioWriteArea {
        public ConsoleSpeechWriteArea(Speech mmio) {
            super(mmio);
        }
    }

    class VdpRamArea extends ByteMemoryArea {
        VdpRamArea() {
            memory = new byte[0x4000];
            read = memory;
            write = memory;
        }
    }

    public static class RamArea extends WordMemoryArea {
        public RamArea(int size) {
            memory = new short[size];
            read = memory;
            write = memory;
        }
    }
 

}
