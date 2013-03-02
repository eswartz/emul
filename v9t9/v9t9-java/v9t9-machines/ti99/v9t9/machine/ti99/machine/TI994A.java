/*
  TI994A.java

  (c) 2008-2012 Edward Swartz

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
package v9t9.machine.ti99.machine;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.keyboard.IKeyboardModeListener;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryWriteListener;



public class TI994A extends TI99Machine {
	private static String[] keyboardModeArray = {
			KEYBOARD_MODE_TI994A,
			KEYBOARD_MODE_LEFT,
			KEYBOARD_MODE_RIGHT,
			KEYBOARD_MODE_PASCAL,
			KEYBOARD_MODE_TI994,
			KEYBOARD_MODE_TI994A,
	};
	protected byte currentMode;
	private IMemoryWriteListener keyboardModeListener;
	public TI994A(ISettingsHandler settings) {
		this(settings, new StandardMachineModel());
	}
	
    public TI994A(ISettingsHandler settings, IMachineModel machineModel) {
        super(settings, machineModel);
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.machine.MachineBase#attachKeyboardModeListener()
     */
    @Override
    protected void attachKeyboardModeListener() {
    	keyboardModeListener = new IMemoryWriteListener() {

			@Override
			public void changed(IMemoryEntry entry, int addr, Number value) {
				if ((addr & 0xffff) == 0x8374) {
					if (currentMode != value.byteValue()) {
						currentMode = value.byteValue();
						fireKeyboardModeChanged(keyboardModeArray[currentMode]);
					}
				}
			}
    		
    	};
    	getMemory().getDomain(IMemoryDomain.NAME_CPU).addWriteListener(keyboardModeListener);
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.machine.MachineBase#removeKeyboardModeListener(v9t9.common.keyboard.IKeyboardModeListener)
     */
    @Override
    public synchronized void removeKeyboardModeListener(
    		IKeyboardModeListener listener) {
    	getMemory().getDomain(IMemoryDomain.NAME_CPU).removeWriteListener(keyboardModeListener);
    }
    
    /* (non-Javadoc)
     * @see v9t9.engine.machine.MachineBase#getKeyboardMode()
     */
    @Override
    public String getKeyboardMode() {
    	return currentMode >= 0 && currentMode < keyboardModeArray.length ? keyboardModeArray[currentMode] : null;
    }
    
}

