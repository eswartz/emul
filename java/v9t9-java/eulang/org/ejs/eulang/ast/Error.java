/**
 * 
 */
package org.ejs.eulang.ast;

public class Error extends Message {
	
	public Error(ISourceRef ref, String msg) {
		super(ref, msg != null ? msg : "");
	}
}