/*
  BaseEnhancedClockedVoice.java

  (c) 2011 Edward Swartz

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
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public abstract class BaseEnhancedClockedVoice extends BaseClockedVoice implements IEnhancedVoice {
	protected byte[] effectValues;
	protected final int numEffects;
	protected int baseEffectReg;

	/**
	 * @param id
	 * @param name
	 * @param listeners
	 */
	public BaseEnhancedClockedVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners,
			int numEffects) {
		super(id, name, listeners);
		this.numEffects = numEffects;
		this.effectValues = new byte[numEffects];
	}

	protected int doInitEffectRegisters(int baseReg) {
		baseEffectReg = baseReg;
		for (int i = 0; i < numEffects; i++) {
			register(baseEffectReg + i,
					getId() + ":Eff" + i,
					getName() + " Effect #" + i);
		}
		return numEffects;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IEnhancedVoice#setEffect(int, int)
	 */
	@Override
	public void setEffect(int effect, byte value) {
		effectValues[effect] = value;
		fireRegisterChanged(baseEffectReg + effect, value);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IEnhancedVoice#getEffectValue(int)
	 */
	@Override
	public byte getEffectValue(int effect) {
		return effectValues[effect];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseClockedVoice#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		super.loadState(settings);
		
		for (int i = 0; i < numEffects; i++) {
			setEffect(i, (byte) settings.getInt("Effect" + i));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.BaseClockedVoice#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		super.saveState(settings);
		
		for (int i = 0; i < numEffects; i++) {
			settings.put("Effect" + i, effectValues[i]);
		}

	}
}
