/**
 * 
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
