/*
  TI994A.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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

