/*
  ExpRamArea.java

  (c) 2008-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.memory;

import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

/** 99/4A expansion RAM, accessed over the peripheral bus */
public class ExpRamArea extends ConsoleMemoryArea {
    static public final SettingSchema settingExpRam = new SettingSchema(
    		ISettingsHandler.MACHINE,
    		"MemoryExpansion32K", Boolean.FALSE);
	private IProperty expRam;


    public ExpRamArea(ISettingsHandler settings, int size) {
    	this(settings, 4, size);
    	
    }
    public ExpRamArea(ISettingsHandler settings, int latency, int size) {
    	super(latency);
    	
        if (!(size == 0x2000 || size == 0x6000)) {
			throw new IllegalArgumentException("unexpected expanded RAM size");
		}
        
        this.expRam = settings.get(ExpRamArea.settingExpRam); 

        memory = new short[size/2];

        expRam.addListenerAndFire(new IPropertyListener() {

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
    
	@Override
	public boolean hasWriteAccess() {
        return expRam.getBoolean();
    }

}