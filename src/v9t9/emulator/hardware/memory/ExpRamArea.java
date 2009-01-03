/**
 * 
 */
package v9t9.emulator.hardware.memory;

import v9t9.engine.settings.ISettingListener;
import v9t9.engine.settings.Setting;

/** 99/4A expansion RAM, accessed over the peripheral bus */
public class ExpRamArea extends ConsoleMemoryArea {
    static public final String sExpRam = "MemoryExpansion32K";
	static public final Setting settingExpRam = new Setting(sExpRam, new Boolean(false));

	@Override
	public boolean hasWriteAccess() {
        return ExpRamArea.settingExpRam.getBoolean();
    }

    public ExpRamArea(int size) {
    	super(4);
    	
        if (!(size == 0x2000 || size == 0x6000 || size == 0xC000)) {
			throw new IllegalArgumentException("unexpected expanded RAM size");
		}

        memory = new short[size/2];
        read = settingExpRam.getBoolean() ? memory : null;
        write = settingExpRam.getBoolean() ? memory : null;

        ExpRamArea.settingExpRam.addListener(new ISettingListener() {

			public void changed(Setting setting, Object oldValue) {
				if (setting.getBoolean()) {
					read = memory;
					write = memory;
				} else {
					read = null;
					write = null;
				}
			}
        	
        });
    }
}