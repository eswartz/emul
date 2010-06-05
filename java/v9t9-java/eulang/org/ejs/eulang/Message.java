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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + ((ref == null) ? 0 : ref.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		if (ref == null) {
			if (other.ref != null)
				return false;
		} else if (!ref.equals(other.ref))
			return false;
		return true;
	}

	
}