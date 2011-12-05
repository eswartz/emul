/**
 * 
 */
package v9t9.machine.ti99.memory;


import v9t9.base.settings.SettingProperty;
import v9t9.common.memory.IMemoryEntry;

/** Builtin console RAM: 256 bytes */
public class ConsoleRamArea extends ConsoleMemoryArea {
    static public final String sEnhRam = "ExtraConsoleRAM";
	static public final SettingProperty settingEnhRam = new SettingProperty(sEnhRam, new Boolean(false));

	public ConsoleRamArea() {
    	super(0);
    	
        memory = new short[0x400/2];
        read = memory;
        write = memory;
    }
	
	private int maskAddress(int addr) {
		if (!ConsoleRamArea.settingEnhRam.getBoolean())
			 addr = (addr & 0xff) + 0x8300;
		return addr;
	}

	public byte readByte(IMemoryEntry entry, int addr) {
		 return super.readByte(entry, maskAddress(addr));
     }

	public short readWord(IMemoryEntry entry, int addr) {
		 return super.readWord(entry, maskAddress(addr));
     }
     public void writeByte(IMemoryEntry entry, int addr, byte val) {
		 super.writeByte(entry, maskAddress(addr), val);
     }
     public void writeWord(IMemoryEntry entry, int addr, short val) {
		 super.writeWord(entry, maskAddress(addr), val);
     }
     
}