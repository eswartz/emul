/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.common;

import v9t9.tools.asm.decomp.IHighLevelInstruction;


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
