/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.common.IMachine;
import v9t9.emulator.common.Machine;
import v9t9.engine.Client;

/**
 * @author ejs
 *
 */
public class ClientFactory {

	private static Map<String, Class<? extends Client>> classMap = new HashMap<String, Class<? extends Client>>();

	public static void register(String id, Class<? extends Client> klass) {
		assert !classMap.containsKey(id);
		classMap.put(id, klass);
	}
	
	public static Client createClient(String id, IMachine machine) {
		Class<? extends Client> klass = classMap.get(id);
		if (klass == null)
			return null;
		try {
			return klass.getConstructor(Machine.class).newInstance(machine);
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
