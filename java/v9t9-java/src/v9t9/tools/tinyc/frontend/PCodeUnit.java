/**
 * 
 */
package v9t9.tools.tinyc.frontend;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ejs
 *
 */
public class PCodeUnit {

	List<PCodeFunction> functions = new ArrayList<PCodeFunction>();
	/**
	 * @param string
	 * @return
	 */
	public PCodeFunction addFunction(String name) {
		PCodeFunction function = new PCodeFunction(name);
		functions.add(function);
		return function;
	}

}
