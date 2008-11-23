/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.memory.MemoryArea;

/** 99/4A expansion RAM, accessed over the peripheral bus */
public class ExpRamArea extends ConsoleMemoryArea {
    @Override
	public boolean hasWriteAccess() {
        return StandardConsoleMemoryModel.settingExpRam.getBoolean();
    }

    public ExpRamArea(int size) {
    	super(4);
    	
        if (!(size == 0x2000 || size == 0x6000)) {
			throw new IllegalArgumentException("unexpected expanded RAM size");
		}

        memory = new short[size/2];
        read = memory;
        write = memory;

        /* only allow access if expansion memory is on */
        class AreaHandlers implements AreaReadByte, AreaReadWord, AreaWriteByte, AreaWriteWord {
            public byte readByte(MemoryArea area, int addr) {
                return StandardConsoleMemoryModel.settingExpRam.getBoolean() ? area.flatReadByte(addr) : 0;
            }
            public short readWord(MemoryArea area, int addr) {
                return StandardConsoleMemoryModel.settingExpRam.getBoolean() ? area.flatReadWord(addr) : 0;
            }
            public void writeByte(MemoryArea area, int addr, byte val) {
                if (StandardConsoleMemoryModel.settingExpRam.getBoolean()) {
					area.flatWriteByte(addr, val);
				}
            }
            public void writeWord(MemoryArea area, int addr, short val) {
                if (StandardConsoleMemoryModel.settingExpRam.getBoolean()) {
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