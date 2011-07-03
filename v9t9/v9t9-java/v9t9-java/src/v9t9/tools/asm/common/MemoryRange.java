/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.common;

import v9t9.tools.asm.decomp.HighLevelInstruction;


public class MemoryRange {
    static public final int EMPTY = 0;
    static public final int CODE = 1;
    static public final int DATA = 2;
    
    /** start address */
    public int from;
    /** type (EMPTY, CODE, DATA) */
    int type;
    /** First instruction, if code */
    private HighLevelInstruction code;
    
    public MemoryRange(int baseAddr, int type) {
        this.from = baseAddr;
        this.type = type;
        this.code = null;
    }

    public HighLevelInstruction getCode() {
    	return code;
    }
    public void setCode(HighLevelInstruction first) {
    	this.code = first;
    }
    
    public int getType() {
        return type;
    }
    public boolean isCode() {
        return type == CODE;
    }
}
