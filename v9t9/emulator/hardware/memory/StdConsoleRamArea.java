/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.MemoryArea;

/** Builtin console RAM */
public class StdConsoleRamArea extends ConsoleMemoryArea {
    public StdConsoleRamArea() {
    	super(0);
    	
        memory = new short[0x400/2];
        read = memory;
        write = memory;

        /*
         * standard console RAM masks the addresses to 0x100 bytes; this is
         * conventionally at 0x8300
         */
        class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
            public byte readByte(MemoryArea area, int addr) {
                return area.flatReadByte(StandardConsoleMemoryModel.settingEnhRam.getBoolean() ? addr
                        & AREASIZE - 1 : (addr & 0xff) + 0x0300);
            }
            public short readWord(MemoryArea area, int addr) {
                return area.flatReadWord(StandardConsoleMemoryModel.settingEnhRam.getBoolean() ? addr
                        & AREASIZE - 1 : (addr & 0xff) + 0x0300);
            }
            public void writeByte(MemoryArea area, int addr, byte val) {
                area.flatWriteByte(StandardConsoleMemoryModel.settingEnhRam.getBoolean() ? addr & AREASIZE - 1
                        : (addr & 0xff) + 0x0300, val);
            }
            public void writeWord(MemoryArea area, int addr, short val) {
                area.flatWriteWord(StandardConsoleMemoryModel.settingEnhRam.getBoolean() ? addr & AREASIZE - 1
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