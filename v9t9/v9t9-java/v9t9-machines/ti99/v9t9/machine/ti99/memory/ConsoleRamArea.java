/**
 * 
 */
package v9t9.machine.ti99.memory;


import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;

/** Builtin console RAM: 256 bytes */
public class ConsoleRamArea extends ConsoleMemoryArea {
    static public final SettingSchema settingEnhRam = new SettingSchema(
    		ISettingsHandler.WORKSPACE,
    		"ExtraConsoleRAM", new Boolean(false));
	private final SettingProperty enhRam;

	public ConsoleRamArea(ISettingsHandler settings) {
    	super(0);
		this.enhRam = settings.get(ConsoleRamArea.settingEnhRam);
    	
        memory = new short[0x400/2];
        read = memory;
        write = memory;
    }
	
	private int maskAddress(int addr) {
		if (!enhRam.getBoolean())
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