/*
  BaseKeyboardMapping.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardMode;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class BaseKeyboardMapping implements IKeyboardMapping {

	private List<PhysKey> physKeys = new ArrayList<PhysKey>();
	private Map<String, IKeyboardMode> modes = new HashMap<String, IKeyboardMode>();
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardMapping#getPhysicalLayout()
	 */
	@Override
	public PhysKey[] getPhysicalLayout() {
		return physKeys.toArray(new PhysKey[physKeys.size()]);
	}
	public void add(IKeyboardMode mode) {
		modes.put(mode.getId(), mode);
		
		Map<PhysKey, Pair<Integer, String>> map = mode.getShiftLockMaskMap((byte) 0);
		if (map != null) {
			for (PhysKey key : map.keySet()) {
				if (!physKeys.contains(key)) {
					physKeys.add(key);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardMapping#getMode(v9t9.common.machine.IMachine)
	 */
	@Override
	public IKeyboardMode getMode(String id) {
		return modes.get(id);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardMapping#getModes()
	 */
	@Override
	public IKeyboardMode[] getModes() {
		return modes.values().toArray(new IKeyboardMode[modes.values().size()]);
	}


}
