/**
 * 
 */
package org.ejs.eulang.llvm.types;

/**
 * @author ejs
 *
 */
public interface LLType {
	String toString();
	boolean equals(Object obj);
	int hashCode();
	
	/** Get the subtype, if any */
	LLType getType();
}
