/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.llinst;

import v9t9.utils.Check;
import v9t9.utils.Utils;

public class Label implements Comparable<Label> {
    private Block block;   // block owning label
    int id;     // unique label
    private String name; // real or unique name
    boolean named; // actually a real name
    
    public Label(Block block, String name) {
    	Check.checkArg(block);
        this.block = block;
        setName(name);
    }

    public void setName(String name) {
        this.name = name != null ? name : uniqueName(block);
        this.named = this.name != null;
    }

    static private String uniqueName(Block block) {
        return "L" + Utils.toHex4(block.getFirst().pc);
    }
    
    @Override
    public String toString() {
        return getName() + (!named ? " @>" + Utils.toHex4(block.getFirst().pc) : "");
    }

	public int compareTo(Label o) {
		return block.compareTo(o.getBlock());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + block.hashCode();
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
		final Label other = (Label) obj;
		if (!block.equals(other.block))
			return false;
		return true;
	}

	public Block getBlock() {
		return block;
	}
	
	public int getAddr() {
		return block.getFirst().pc;
	}

	public String getName() {
		return name;
	}
}
