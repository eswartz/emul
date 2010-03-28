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
	List<T> list();
}
