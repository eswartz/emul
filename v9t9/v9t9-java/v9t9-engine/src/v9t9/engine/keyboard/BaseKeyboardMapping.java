/*
  BaseKeyboardMapping.java

  (c) 2012 Edward Swartz

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
