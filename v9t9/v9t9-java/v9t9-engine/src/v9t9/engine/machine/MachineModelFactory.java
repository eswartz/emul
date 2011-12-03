/**
 * 
 */
package v9t9.engine.machine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ejs
 *
 */
public class MachineModelFactory {

	public static final MachineModelFactory INSTANCE = new MachineModelFactory();
	
	private Map<String, Class<? extends MachineModel>> classMap = new HashMap<String, Class<? extends MachineModel>>();

	private String defaultModel;

	public void register(String id, Class<? extends MachineModel> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
		if (defaultModel == null)
			defaultModel = id;
	}
	
	public MachineModel createModel(String id) {
		Class<? extends MachineModel> klass = classMap.get(id);
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
