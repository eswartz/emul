/**
 * 
 */
package v9t9.emulator.hardware.memory;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;

/** 99/4A expansion RAM, accessed over the peripheral bus */
public class ExpRamArea extends ConsoleMemoryArea {
    static public final SettingProperty settingExpRam = new SettingProperty("MemoryExpansion32K", new Boolean(false));

	@Override
	public boolean hasWriteAccess() {
        return ExpRamArea.settingExpRam.getBoolean();
    }

    public ExpRamArea(int size) {
    	this(4, size);
    	
    }
    public ExpRamArea(int latency, int size) {
    	super(latency);
    	
        if (!(size == 0x2000 || size == 0x6000)) {
			throw new IllegalArgumentException("unexpected expanded RAM size");
		}

        memory = new short[size/2];
        read = settingExpRam.getBoolean() ? memory : null;
        write = settingExpRam.getBoolean() ? memory : null;

        ExpRamArea.settingExpRam.addListener(new IPropertyListener() {

			public void propertyChanged(IProperty setting) {
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