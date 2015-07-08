/*
  CassetteChip.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import static v9t9.common.sound.TMS9919Consts.GROUP_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.cassette.ICassetteChip;
import v9t9.common.cassette.ICassetteDeck;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.IRegisterBank;
import v9t9.engine.machine.BaseRegisterBank;
import ejs.base.properties.IProperty;
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
	
	protected final Map<Integer, IRegisterBank> regIdToVoice;

	protected final IMachine machine;

	protected final ListenerList<IRegisterWriteListener> listeners;
	
	protected int regBase;
	protected IClockedVoice[] voices = new IClockedVoice[4];
	protected AudioGateVoice audioGateVoice;
	private CassetteDeck cassette1;
	private CassetteDeck cassette2;

	public CassetteChip(IMachine machine, String id, String name, int regBase) {
		this.machine = machine;
		listeners = new ListenerList<IRegisterWriteListener>();
		
		this.regBase = regBase;
		
		regNames = new HashMap<Integer, String>();
		regDescs = new HashMap<Integer, String>();
		regIds = new HashMap<String, Integer>();
		
		regIdToVoice = new HashMap<Integer, IRegisterBank>();
		
		initRegisters(id, name, regBase);
		
		reset();
	}
	
	/** Initialize registers and return new regBase */
	public int initRegisters(String id, String name, int regBase) {
		int count;
		
		cassette1 = new CassetteDeck(id + "C1", "CS1", listeners, machine);
		count = ((BaseRegisterBank) cassette1).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, cassette1);
		regBase += count;

		cassette2 = new CassetteDeck(id + "C2", "CS2", listeners, machine);
		count = ((BaseRegisterBank) cassette2).initRegisters(regNames, regDescs, regIds, regBase);
		mapRegisters(regBase, count, cassette2);
		regBase += count;

		return regBase;
	}

	protected void mapRegisters(int regBase, int count, IRegisterBank voice) {
		while (count-- > 0)
			regIdToVoice.put(regBase++, voice);
	}

	@Override
	public ICassetteDeck getCassette1() {
		return cassette1;
	}
	@Override
	public ICassetteDeck getCassette2() {
		return cassette2;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ICassetteChip#reset()
	 */
	@Override
	public void reset() {
		cassette1.writeBit(false);
		cassette2.writeBit(false);
//		cassetteVoice.setMotor1(false);
//		cassetteVoice.setMotor2(false);		
	}
	

	public void saveState(ISettingSection settings) {
		cassette1.saveState(settings.addSection(cassette1.getName()));
		cassette2.saveState(settings.addSection(cassette2.getName()));

	}
	public void loadState(ISettingSection settings) {
		if (settings == null) return;
		cassette1.loadState(settings.getSection(cassette1.getName()));
		cassette2.loadState(settings.getSection(cassette2.getName()));
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
		IRegisterBank voice = regIdToVoice.get(reg);
		RegisterInfo info = new RegisterInfo(regNames.get(reg), 
				IRegisterAccess.FLAG_ROLE_GENERAL,
				voice instanceof CassetteDeck ? 4 : 1,
				regDescs.get(reg));
		return info;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#getRegister(int)
	 */
	@Override
	public int getRegister(int reg) {
		IRegisterBank voice = regIdToVoice.get(reg);
		if (voice == null)
			return 0;
		return voice.getRegister(reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IRegisterAccess#setRegister(int, int)
	 */
	@Override
	public int setRegister(int reg, int newValue) {
		IRegisterBank voice = regIdToVoice.get(reg);
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.hardware.ICassetteChip#getDeviceSettings()
	 */
	@Override
	public IDeviceSettings getDeviceSettings() {
		final ISettingsHandler msettings = machine.getSettings();
		
		IDeviceSettings cassette = new IDeviceSettings() {
			private Map<String, Collection<IProperty>> map;
			@Override
			public Map<String, Collection<IProperty>> getEditableSettingGroups() {
				if (map == null) {
					map = new HashMap<String, Collection<IProperty>>();
					Collection<IProperty> list = new ArrayList<IProperty>();
					list.add(msettings.get(ICassetteChip.settingCassetteInput));
					list.add(msettings.get(ICassetteChip.settingCassette1OutputFile));
					list.add(msettings.get(ICassetteChip.settingCassette2OutputFile));
					list.add(msettings.get(ICassetteChip.settingCassetteDebug));
					map.put(ICassetteChip.GROUP_CASSETTE, list);
					
					list = new ArrayList<IProperty>();
					list.add(msettings.get(ICassetteChip.settingCassetteEnabled));
					map.put(IDsrHandler.GROUP_DSR_SELECTION, list);
				}
				return map;
			}
		};
		return cassette;
	}
	
	/**
	 * @param audioGateVoice the audioGateVoice to set
	 */
	public void setAudioGateVoice(AudioGateVoice audioGateVoice) {
		this.audioGateVoice = audioGateVoice;
	}
}
