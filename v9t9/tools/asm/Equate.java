/**
 * 
 */
package v9t9.tools.asm;

/**
 * This is a symbol equated to a constant
 * @author ejs
 *
 */
public class Equate extends Symbol {

	private final int value;

	public Equate(String name, int value) {
		super(name);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Equate other = (Equate) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}
	
	
}
