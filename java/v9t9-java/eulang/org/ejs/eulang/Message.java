/**
 * 
 */
package org.ejs.eulang;

/**
 * @author ejs
 *
 */
public class Message {

	protected ISourceRef ref;
	protected String msg;
	public Message(ISourceRef ref, String msg) {
		this.ref = ref;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return (ref != null ? ref.toString() : "") + ": " + msg;
	}

}