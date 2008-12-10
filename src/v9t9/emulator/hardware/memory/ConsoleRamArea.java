/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.MemoryArea;
import v9t9.engine.settings.Setting;

/** Builtin console RAM: 256 bytes */
public class ConsoleRamArea extends ConsoleMemoryArea {
    static public final String sEnhRam = "ExtraConsoleRAM";
	static public final Setting settingEnhRam = new Setting(sEnhRam, new Boolean(false));

	public ConsoleRamArea() {
    	super(0);
    	
        memory = new short[0x400/2];
        read = memory;
        write = memory;

        /*
         * standard console RAM masks the addresses to 0x100 bytes; this is
         * conventionally accessed at 0x8300
         */
        class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
            public byte readByte(MemoryArea area, int addr) {
                return area.flatReadByte(ConsoleRamArea.settingEnhRam.getBoolean() ? addr
                        & AREASIZE - 1 : (addr & 0xff) + 0x0300);
            }
            public short readWord(MemoryArea area, int addr) {
                return area.flatReadWord(ConsoleRamArea.settingEnhRam.getBoolean() ? addr
                        & AREASIZE - 1 : (addr & 0xff) + 0x0300);
            }
            public void writeByte(MemoryArea area, int addr, byte val) {
                area.flatWriteByte(ConsoleRamArea.settingEnhRam.getBoolean() ? addr & AREASIZE - 1
                        : (addr & 0xff) + 0x0300, val);
            }
            public void writeWord(MemoryArea area, int addr, short val) {
                area.flatWriteWord(ConsoleRamArea.settingEnhRam.getBoolean() ? addr & AREASIZE - 1
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