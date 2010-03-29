/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;


/**
 * @author ejs
 *
 */
public interface IAstNodeList <T extends IAstNode> extends IAstNode {
	IAstNodeList<T> copy(IAstNode copyParent);
	List<T> list();
}
