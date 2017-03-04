/**
 * 
 */
package v9t9.common.keyboard;

/** The identity of a controller and component */
public class ControllerIdentifier implements Comparable<ControllerIdentifier> {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((controllerName == null) ? 0 : controllerName.hashCode());
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ControllerIdentifier other = (ControllerIdentifier) obj;
		if (controllerName == null) {
			if (other.controllerName != null)
				return false;
		} else if (!controllerName.equals(other.controllerName))
			return false;
		if (index != other.index)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/** Owning controller */
	public String controllerName;
	/** Index of component in controller (or -1 if not applicable) */
	public int index;
	/** Name of component in controller */
	public String name;
	
	public ControllerIdentifier(String controllerName, int index, String name) {
		this.controllerName = controllerName;
		this.index = index;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ControllerIdentifier o) {
		int diff = controllerName.compareTo(o.controllerName);
		if (diff != 0) return diff;
		
		diff = name.compareTo(o.name);
		if (diff != 0) return diff;
		
		return index - o.index;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{ " + controllerName + " : " + name + " # " + index + " }";
	}
}