/**
 * 
 */
package org.ejs.eulang.llvm.ops;

/**
 * @author ejs
 *
 */
public interface LLOperand {
	boolean equals(Object obj);
	int hashCode();
	
	String toString();
}
