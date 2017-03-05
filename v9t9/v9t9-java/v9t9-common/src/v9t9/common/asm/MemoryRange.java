/*
  MemoryRange.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;



public class MemoryRange {
    static public final int EMPTY = 0;
    static public final int CODE = 1;
    static public final int DATA = 2;
    
    /** start address */
    public int from;
    /** type (EMPTY, CODE, DATA) */
    int type;
    /** First instruction, if code */
    private IHighLevelInstruction code;
    
    public MemoryRange(int baseAddr, int type) {
        this.from = baseAddr;
        this.type = type;
        this.code = null;
    }

    public IHighLevelInstruction getCode() {
    	return code;
    }
    public void setCode(IHighLevelInstruction first) {
    	this.code = first;
    }
    
    public int getType() {
        return type;
    }
    public boolean isCode() {
        return type == CODE;
    }
}
