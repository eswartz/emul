/**
 * 
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
