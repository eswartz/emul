/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Set;

/**
 * @author ejs
 *
 */
public interface IAstAttributes extends IAttrs {
	Set<String> getAttrs();
	boolean hasAttr(String attr);

}
