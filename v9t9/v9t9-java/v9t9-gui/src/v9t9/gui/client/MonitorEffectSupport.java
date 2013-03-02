/*
  MonitorEffectSupport.java

  (c) 2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.common.client.IMonitorEffect;
import v9t9.common.client.IMonitorEffectSupport;

/**
 * @author ejs
 *
 */
public class MonitorEffectSupport implements IMonitorEffectSupport {

	public static MonitorEffectSupport INSTANCE = new MonitorEffectSupport();

	private Map<String, IMonitorEffect> monitorEffects = new LinkedHashMap<String, IMonitorEffect>();
	
	public void registerEffect(String id, IMonitorEffect effect) {
		monitorEffects.put(id, effect);
	}
	
	public String[] getIds() {
		return monitorEffects.keySet().toArray(new String[monitorEffects.keySet().size()]);
	}
	
	public IMonitorEffect getEffect(String id) {
		return monitorEffects.get(id);
	}
}
