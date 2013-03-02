/*
  MachineModelFactory.java

  (c) 2010-2011 Edward Swartz

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
package v9t9.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.machine.IMachineModel;

/**
 * @author ejs
 *
 */
public class MachineModelFactory {

	public static final MachineModelFactory INSTANCE = new MachineModelFactory();
	
	private Map<String, Class<? extends IMachineModel>> classMap = new HashMap<String, Class<? extends IMachineModel>>();

	private String defaultModel;

	public void register(String id, Class<? extends IMachineModel> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
		if (defaultModel == null)
			defaultModel = id;
	}
	
	public IMachineModel createModel(String id) {
		Class<? extends IMachineModel> klass = classMap.get(id);
		if (klass == null)
			return null;
		try {
			return klass.newInstance();
		} catch (InstantiationException e) {
			assert false : e.getMessage();
		} catch (IllegalAccessException e) {
			assert false : e.getMessage();
		}
		return null;
	}

	public Collection<String> getRegisteredModels() {
		return Collections.unmodifiableCollection(classMap.keySet());
	}

	/**
	 * @return
	 */
	public String getDefaultModel() {
		return defaultModel;
	}
	
}
