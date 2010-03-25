/**
 * 
 */
package v9t9.tools.tinyc.frontend;

import java.util.ArrayList;
import java.util.List;

import v9t9.tools.asm.common.Block;

/**
 * @author ejs
 *
 */
public class PCodeFunction {

	private final String name;
	List<Block> blocks = new ArrayList<Block>();
	/**
	 * @param name
	 */
	public PCodeFunction(String name) {
		this.name = name;
	}

}
