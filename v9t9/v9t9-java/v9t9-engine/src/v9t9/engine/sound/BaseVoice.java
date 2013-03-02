/*
  BaseVoice.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import java.util.Map;

import v9t9.common.machine.IRegisterAccess.IRegisterWriteListener;
import v9t9.common.sound.IVoice;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public abstract class BaseVoice implements IVoice {

	protected final String id;

	public abstract void loadState(ISettingSection settings);

	public abstract void saveState(ISettingSection settings);

	protected final String name;
	protected final ListenerList<IRegisterWriteListener> listeners;
	protected int baseReg;
	private Map<Integer, String> regNames;
	private Map<Integer, String> regDescs;
	private Map<String, Integer> regIds;
	private int registerCount;

	/**
	 * @param name 
	 * 
	 */
	public BaseVoice(String id, String name, ListenerList<IRegisterWriteListener> listeners) {
		this.id = id;
		this.name = name;
		this.listeners = listeners;
	}

	/** Initialize register info 
	 * @param baseReg first register # to use
	 * 
	 * @return register count
	 */
	public int initRegisters(Map<Integer, String> regNames, Map<Integer, String> regDescs,
			Map<String, Integer> regIds, int baseReg) {
		this.regNames = regNames;
		this.regDescs = regDescs;
		this.regIds = regIds;
		this.baseReg = baseReg;
		this.registerCount = doInitRegisters();
		return registerCount;
	}

	/** 
	 * Initialize register info (based on {@link #baseReg})
	 * @return number of registers
	 */
	public abstract int doInitRegisters();

	protected void register(int reg, String id, String desc)
	{
		if (regNames.containsKey(reg))
			throw new IllegalStateException();
		regNames.put(reg, id);
		regDescs.put(reg, desc);
		regIds.put(id, reg);
	}

	protected void fireRegisterChanged(int reg, int newValue) {
		if (!listeners.isEmpty()) {
			for (Object listenerObj : listeners.toArray()) {
				((IRegisterWriteListener) listenerObj).registerChanged(reg, newValue);
			}
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getBaseRegister()
	 */
	@Override
	public int getBaseRegister() {
		return baseReg;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.sound.IVoice#getRegisterCount()
	 */
	@Override
	public int getRegisterCount() {
		return registerCount;
	}

}