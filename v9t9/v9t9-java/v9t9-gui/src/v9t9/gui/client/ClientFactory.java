/*
  ClientFactory.java

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
package v9t9.gui.client;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.IClient;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class ClientFactory {

	private static Map<String, Class<? extends IClient>> classMap = new HashMap<String, Class<? extends IClient>>();

	public static void register(String id, Class<? extends IClient> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
	}
	
	public static IClient createClient(String id, IMachine machine) {
		Class<? extends IClient> klass = classMap.get(id);
		if (klass == null)
			return null;
		try {
			return klass.getConstructor(IMachine.class).
					newInstance(machine);
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
