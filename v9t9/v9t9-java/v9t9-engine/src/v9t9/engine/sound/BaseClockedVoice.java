/*
  BaseClockedVoice.java

  (c) 2011-2012 Edward Swartz

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
import v9t9.common.sound.TMS9919Consts;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public abstract class BaseClockedVoice extends BaseVoice implements IClockedVoice {

	private int period;
	private int att;

	
	
	public BaseClockedVoice(String id, String name,
			ListenerList<IRegisterWriteListener> listeners) {
		super(id, name, listeners);
	}

	@Override
	public void setPeriod(int hz) {
		this.period = hz;
		fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_FREQUENCY_PERIOD, period); 
	}

	@Override
	public int getPeriod() {
		return period;
	}

	@Override
	public void setAttenuation(int att) {
		this.att = att;
		fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_ATTENUATION, att); 
	}

	@Override
	public int getAttenuation() {
		return att;
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#saveState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection settings) {
		settings.put("Period", period);
		settings.put("Attenuation", att);
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPersistable#loadState(ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		setPeriod(settings.getInt("Period"));
		setAttenuation(settings.getInt("Attenuation"));
	}
}
