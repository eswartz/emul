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

	public Equate(SymbolTable table, String name, int value) {
		super(table, name);
		setAddr(value);
	}

	public int getValue() {
		return getAddr();
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}
