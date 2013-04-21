/**
 * 
 */
package v9t9.engine.sound;

import static v9t9.common.sound.TMS9919Consts.GROUP_NAME;

import java.util.HashMap;
import java.util.Map;

import v9t9.common.hardware.ICassetteChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.ICassetteVoice;
import v9t9.common.sound.IVoice;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class CassetteChip implements ICassetteChip {


	protected final Map<Integer, String> regNames;
	protected final Map<Integer, String> regDescs;
	protected final Map<String, Integer> regIds;
	
	protected final Map<Integer, IVoice> regIdToVoice;

	protected final IMachine machine;

	protected final ListenerList<IRegisterWriteListener> listeners;
	
	protected int regBase;
	protected IClockedVoice[] voices = new IClockedVoice[4];
	protected AudioGateVoice audioGateVoice;
	private CassetteVoice cassetteVoice;

	public CassetteChip(IMachine machine, String id, String name, int regBase) {
		this.machine = machine;
		listeners = new ListenerList<IRegisterWriteListener>();
		
		this.regBase = regBase;
		
		regNames = new HashMap<Integer, String>();
		regDescs = new HashMap<Integer, String>();
		regIds = new HashMap<String, Integer>();
		
		regIdToVoice = new HashMap<Integer, IVoice>();
		
		initRegisters(id, name, regBase);
		
		reset();
	}
	
	/** Initialize registers and return new regBase */
	public int initRegisters(String id, String name, int regBase) {
		int count;
		
		cassetteVoice = new CassetteVoice(id + "C", "Cassette", listeners, machine);
		count = ((BaseVoice) cassetteVoice).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, cassetteVoice);
		regBase += count;
		
		return regBase;
	}

	protected void mapRegisters(int regBase, int count, IVoice voice) {
		while (count-- > 0)
			regIdToVoice.put(regBase++, voice);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ICassetteChip#getCassetteVoice()
	 */
	@Override
	public ICassetteVoice getCassetteVoice() {
		return cassetteVoice;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ICassetteChip#reset()
	 */
	@Override
	public void reset() {
		cassetteVoice.setState(false);
//		cassetteVoice.setMotor1(false);
//		cassetteVoice.setMotor2(false);		
	}
	

	public void saveState(ISettingSection settings) {
		cassetteVoice.saveState(settings.addSection(cassetteVoice.getName()));

	}
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		cassetteVoice.loadState(settings.getSection(cassetteVoice.getName()));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return GROUP_NAME;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getFirstRegister()
	 */
	@Override
	public int getFirstRegister() {
		return regBase;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return regIds.size();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterNumber(java.lang.String)
	 */
	@Override
	public int getRegisterNumber(String id) {
		Integer val = regIds.get(id);
		return val != null ? val : Integer.MIN_VALUE;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterInfo(int)
	 */
	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		IVoice voice = regIdToVoice.get(reg);
		RegisterInfo info = new RegisterInfo(regNames.get(reg), 
				IRegisterAccess.FLAG_ROLE_GENERAL,
				voice instanceof CassetteVoice ? 4 : 1,
				regDescs.get(reg));
		return info;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		IVoice voice = regIdToVoice.get(reg);
		if (voice == null)
			return 0;
		return voice.getRegister(reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#setRegister(int, int)
	 */
	@Override
	public int setRegister(int reg, int newValue) {
		IVoice voice = regIdToVoice.get(reg);
		if (voice == null)
			return 0;
		int old = voice.getRegister(reg);
		voice.setRegister(reg, newValue);
		return old;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegisterTooltip(int)
	 */
	@Override
	public String getRegisterTooltip(int reg) {
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#addWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#removeWriteListener(v9t9.common.machine.IRegisterAccess.IRegisterWriteListener)
	 */
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		listeners.remove(listener);
	}
	
}
