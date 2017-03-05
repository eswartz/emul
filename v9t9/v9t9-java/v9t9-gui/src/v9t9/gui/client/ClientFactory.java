/*
  ClientFactory.java

  (c) 2010-2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.IClient;
import v9t9.server.client.EmulatorServerBase;

/**
 * @author ejs
 *
 */
public class ClientFactory {

	public static final ClientFactory INSTANCE = new ClientFactory();
	private ClientFactory() { }
	
	private Map<String, Class<? extends IClient>> classMap = new HashMap<String, Class<? extends IClient>>();
	private String defaultClient;

	public Collection<String> getRegisteredClients() {
		return classMap.keySet();
	}
	public void register(String id, Class<? extends IClient> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
		if (defaultClient == null)
			defaultClient = id;
	}
	
	public IClient createClient(String id, EmulatorServerBase server) {
		Class<? extends IClient> klass = classMap.get(id);
		if (klass == null)
			return null;
		try {
			return klass.getConstructor(EmulatorServerBase.class).
					newInstance(server);
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @return
	 */
	public String getDefaultClient() {
		return defaultClient;
	}
	/**
	 * @param id
	 */
	public void setDefault(String id) {
		this.defaultClient = id;
	}
	
}
