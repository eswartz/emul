/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;
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
	private Pair<Block, AsmInstruction> init;
	private Map<Block, List<AsmInstruction>> instUses;
	private BitSet uses;

	/**
	 * 
	 * @param name
	 * @param type
	 * @param size in bits
	 */
	public BaseLocal(ISymbol name, LLType type) {
		this.name = name;
		this.type = type;
		this.init = null;
		this.instUses = new HashMap<Block, List<AsmInstruction>>();
		this.uses = new BitSet();
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

	public Pair<Block, AsmInstruction> getInit() {
		return init;
	}

	public void setInit(Pair<Block, AsmInstruction> init) {
		this.init = init;
	}

	public Map<Block, List<AsmInstruction>> getInstUses() {
		return instUses;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ILocal#getUses()
	 */
	@Override
	public BitSet getUses() {
		return uses;
	}
	
}
