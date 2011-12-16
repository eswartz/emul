package ejs.base.utils;

/**
 * A tuple of zero or more items.
 * @author eswartz
 *
 */
public class Tuple {
	private Object[] args;

	public Tuple(Object... args) {
		this.args = args;
	}

	public Object get(int index) {
		return args[index];
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Tuple))
			return false;
		Object[] otherArgs = ((Tuple) obj).args;
		if (args.length != otherArgs.length)
			return false;
		for (int i = 0; i < otherArgs.length; i++) {
			if (!(args[i] == null && otherArgs[i] == null)
				&& !(args[i] != null && otherArgs[i] != null && args[i].equals(otherArgs[i]))	)
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0x12345678;
		for (int i = 0; i < args.length; i++) {
			hashCode ^= (args[i] != null ? args[i].hashCode() : 0);
		}
		return hashCode;
	}
}
