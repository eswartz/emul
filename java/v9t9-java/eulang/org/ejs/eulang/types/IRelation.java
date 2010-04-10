/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public interface IRelation {
	/** Get the controlling type */
	ITyped getHead();
	/** Get the types derived from or equal to the controlling type */
	ITyped[] getTails();

	/** Tell if the head and tails have been resolved */ 
	boolean isComplete();
	
	/** Tell if the head and tails match according to the relation */
	//boolean isValid(TypeEngine typeEngine);
	
	/** Propagate the head type to the tails, or throw if invalid.
	 * This may not change the structure of the AST. 
	 * @param typeEngine */
	boolean inferDown(TypeEngine typeEngine) throws TypeException;
	/** Propagate the tail type(s) up to the head, or throw if invalid.
	 * This may not change the structure of the AST. 
	 * @param typeEngine*/
	boolean inferUp(TypeEngine typeEngine) throws TypeException;
	
	/** Finalize types, from top down.  This may change the tree. */
	void finalize(TypeEngine typeEngine) throws TypeException;
}
