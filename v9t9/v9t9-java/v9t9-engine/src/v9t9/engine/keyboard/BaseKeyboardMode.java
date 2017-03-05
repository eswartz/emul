/*
  BaseKeyboardMode.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ejs.base.utils.Pair;

import v9t9.common.keyboard.IKeyboardMapping.PhysKey;
import v9t9.common.keyboard.IKeyboardMode;
import v9t9.common.keyboard.KeyboardConstants;

/**
 * @author ejs
 *
 */
public class BaseKeyboardMode implements IKeyboardMode {
	private String id;
	private String label;
	
	/** map of interesting shift masks to map */
	private Map<Byte, Map<PhysKey, Pair<Integer, String>>> shiftLockMaskMap = 
			new HashMap<Byte, Map<PhysKey,Pair<Integer,String>>>();
	
	/**
	 * 
	 */
	public BaseKeyboardMode(String id, String label) {
		this.id = id;
		this.label = label;
	}
	
	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	static public class KeyInfoBuilder {
		final private PhysKey key;
		private List<Pair<Byte, Pair<Integer, String>>> shiftStates = new ArrayList<Pair<Byte,Pair<Integer,String>>>();
		
		public static KeyInfoBuilder forKey(PhysKey key) {
			return new KeyInfoBuilder(key);
		}
		public KeyInfoBuilder(PhysKey key) {
			this.key = key;
		}
		public KeyInfoBuilder normal(int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) 0, 
					new Pair<Integer,String>(keycode, label)));
			return this;
		}
		public KeyInfoBuilder shift(int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) KeyboardConstants.MASK_SHIFT, 
					new Pair<Integer,String>(keycode, label)));
			return this;
		}
		public KeyInfoBuilder alpha(int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) 0, 
					new Pair<Integer,String>(keycode, label.toLowerCase())));
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) KeyboardConstants.MASK_SHIFT, 
					new Pair<Integer,String>(keycode, label.toUpperCase())));
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) KeyboardConstants.MASK_CAPS_LOCK, 
					new Pair<Integer,String>(keycode, label.toUpperCase())));
			return this;
		}
		public KeyInfoBuilder ctrl(int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) KeyboardConstants.MASK_CONTROL, 
					new Pair<Integer,String>(keycode, label)));
			return this;
		}
		public KeyInfoBuilder fctn(int keycode, String label) {
			return alt(keycode, label);
		}
		public KeyInfoBuilder alt(int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					(byte) KeyboardConstants.MASK_ALT, 
					new Pair<Integer,String>(keycode, label)));
			return this;
		}
		public KeyInfoBuilder mask(byte mask, int keycode, String label) {
			shiftStates.add(new Pair<Byte, Pair<Integer,String>>(
					mask, 
					new Pair<Integer,String>(keycode, label)));
			return this;
		}
		
		public void apply(BaseKeyboardMode... modes) {
			for (BaseKeyboardMode mode : modes)
				mode.add(this);
		}
	}
	
	void add(KeyInfoBuilder builder) {
		for (Pair<Byte, Pair<Integer, String>> ent : builder.shiftStates) {
			if (ent.first == 0) {
				Map<PhysKey, Pair<Integer, String>> shiftMap = shiftLockMaskMap.get((byte) 0);
				if (shiftMap == null) {
					shiftMap = new HashMap<PhysKey, Pair<Integer,String>>();
					shiftLockMaskMap.put((byte) 0, shiftMap);
				}
				shiftMap.put(builder.key, ent.second);
			} else {
				for (byte mask = 1; mask <= ent.first; mask <<= 1) {
					if ((ent.first & mask) != 0) {
						Map<PhysKey, Pair<Integer, String>> shiftMap = shiftLockMaskMap.get(mask);
						if (shiftMap == null) {
							shiftMap = new HashMap<PhysKey, Pair<Integer,String>>();
							shiftLockMaskMap.put(mask, shiftMap);
						}
						shiftMap.put(builder.key, ent.second);
						
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardMapping#getShiftLockMaskMap(byte)
	 */
	@Override
	public Map<PhysKey, Pair<Integer, String>> getShiftLockMaskMap(
			byte shiftLockMask) {
		Map<PhysKey, Pair<Integer, String>> map = shiftLockMaskMap.get(shiftLockMask);
		if (map == null) 
			map = Collections.emptyMap();
		return map;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardMode#getKeycode(byte, v9t9.common.keyboard.IKeyboardMapping.PhysKey)
	 */
	@Override
	public int getKeycode(byte shiftLockMask, PhysKey key) {
		Pair<Integer, String> info = getShiftLockMaskMap(shiftLockMask).get(key);
		if (info == null)
			info = getShiftLockMaskMap((byte) 0).get(key);
		if (info == null)
			return KeyboardConstants.KEY_UNKNOWN;
		return info.first;
	}

}
