/*
  BaseClockedVoice.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;


import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.TMS9919Consts;
import v9t9.engine.machine.BaseRegisterBank;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public abstract class BaseClockedVoice extends BaseRegisterBank implements IClockedVoice {

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
