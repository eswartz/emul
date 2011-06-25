/**
 * 
 */
package v9t9.emulator.hardware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ejs
 *
 */
public class MachineModelFactory {

	private static Map<String, Class<? extends MachineModel>> classMap = new HashMap<String, Class<? extends MachineModel>>();

	public static void register(String id, Class<? extends MachineModel> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
	}
	
	public static MachineModel createModel(String id) {
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
	
}
