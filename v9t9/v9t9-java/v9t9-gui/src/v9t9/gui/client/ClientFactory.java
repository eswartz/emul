/**
 * 
 */
package v9t9.gui.client;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import v9t9.engine.client.IClient;
import v9t9.engine.machine.IMachine;
import v9t9.engine.machine.Machine;

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
			return klass.getConstructor(Machine.class).newInstance(machine);
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
