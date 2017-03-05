/*
  Label.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import ejs.base.utils.Check;
import ejs.base.utils.HexUtils;



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
        return "L" + HexUtils.toHex4(block.getFirst().getInst().pc);
    }
    
    @Override
    public String toString() {
        return getName() + (!named ? " @>" + HexUtils.toHex4(block.getFirst().getInst().pc) : "");
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
		return block.getFirst().getInst().pc;
	}

	public String getName() {
		return name;
	}
}
