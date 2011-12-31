/**
 * 
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
		fireRegisterChanged(baseReg + TMS9919Consts.REG_OFFS_ATTENTUATION, att); 
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
