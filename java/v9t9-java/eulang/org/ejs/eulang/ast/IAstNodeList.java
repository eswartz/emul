/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;


/**
 * @author ejs
 *
 */
public interface IAstNodeList extends IAstNode {
	List<IAstNode> list();
}
