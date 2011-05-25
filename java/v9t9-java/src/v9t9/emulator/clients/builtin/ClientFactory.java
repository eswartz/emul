/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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
	
	public static Client createClient(String id, Machine machine) {
		Class<? extends Client> klass = classMap.get(id);
		if (klass == null)
			return null;
		try {
			return klass.getConstructor(Machine.class).newInstance(machine);
		} catch (SecurityException e) {
			assert false : e.getCause().getMessage();
		} catch (InstantiationException e) {
			assert false : e.getCause().getMessage();
		} catch (IllegalAccessException e) {
			assert false : e.getCause().getMessage();
		} catch (InvocationTargetException e) {
			assert false : e.getCause().getMessage();
		} catch (NoSuchMethodException e) {
			assert false : e.getCause().getMessage();
		}
		return null;
	}
	
}
