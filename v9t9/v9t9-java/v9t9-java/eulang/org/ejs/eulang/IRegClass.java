/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface IRegClass {

	/**
	 * @return
	 */
	BasicType getBasicType();

	/**
	 * @return
	 */
	int getRegisterCount();

	/**
	 * @return size in bits
	 */
	int getRegisterSize();

	/**
	 * Get the size in bytes for a register of the given type
	 * @param type
	 * @return
	 */
	int getByteSize(LLType type);

	/**
	 * Tell if the class can store the type (one or more registers may be used)
	 * @param type
	 * @return
	 */
	boolean supportsType(LLType type);

}
