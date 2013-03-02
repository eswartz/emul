/*
  ClientFactory.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
