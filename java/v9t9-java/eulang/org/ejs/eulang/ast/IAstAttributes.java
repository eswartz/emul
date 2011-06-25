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
	/** get read-only attrs */
	Set<String> getAttrs();
	/** get modifiable variant */
	Set<String> attrs();
	boolean hasAttr(String attr);

}
