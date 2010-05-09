/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class BaseLocal implements ILocal {

	private LLType type;
	private int size;
	private ISymbol name;

	
	public BaseLocal(ISymbol name, LLType type, int size) {
		super();
		this.name = name;
		this.type = type;
		this.size = size;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "local " + name.getName() + " [" + type + "] size = " + size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + size;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		BaseLocal other = (BaseLocal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (size != other.size)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getName()
	 */
	@Override
	public ISymbol getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

}
