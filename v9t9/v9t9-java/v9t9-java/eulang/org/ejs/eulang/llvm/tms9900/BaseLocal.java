/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.BitSet;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class BaseLocal implements ILocal {

	private LLType type;
	private ISymbol name;
	private ILocal incoming;
	private BitSet uses;
	private BitSet defs;
	private boolean exprTemp;
	private boolean singleBlock;
	private int init;
	private boolean outgoing;
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param size in bits
	 */
	public BaseLocal(ISymbol name, LLType type) {
		this.name = name;
		this.type = type;
		this.uses = new BitSet();
		this.defs = new BitSet();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "local " + name.getName() + " [" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getIncoming()
	 */
	@Override
	public ILocal getIncoming() {
		return incoming;
	}
	
	/**
	 * @param incoming the incoming to set
	 */
	public void setIncoming(ILocal incoming) {
		this.incoming = incoming;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getUses()
	 */
	@Override
	public BitSet getUses() {
		return uses;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getDefs()
	 */
	@Override
	public BitSet getDefs() {
		return defs;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#isExprTemp()
	 */
	@Override
	public boolean isExprTemp() {
		return exprTemp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#isSingleBlock()
	 */
	@Override
	public boolean isSingleBlock() {
		return singleBlock;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#setExprTemp(boolean)
	 */
	@Override
	public void setExprTemp(boolean temp) {
		this.exprTemp = temp;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#setSingleBlock(boolean)
	 */
	@Override
	public void setSingleBlock(boolean single) {
		this.singleBlock = single;
	}

	public int getInit() {
		return init;
	}

	public void setInit(int init) {
		this.init = init;
	}

	public boolean isOutgoing() {
		return outgoing;
	}

	public void setOutgoing(boolean outgoing) {
		this.outgoing = outgoing;
	}
	

	
	
}
