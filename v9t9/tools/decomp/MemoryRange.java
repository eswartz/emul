/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.decomp;


public class MemoryRange {
    static public final int EMPTY = 0;
    static public final int CODE = 1;
    static public final int DATA = 2;
    
    /** start address */
    int from;
    /** type (EMPTY, CODE, DATA) */
    int type;
    /** First instruction, if code */
    private LLInstruction code;
    
    public MemoryRange(int baseAddr, int type) {
        this.from = baseAddr;
        this.type = type;
        this.code = null;
    }

    public LLInstruction getCode() {
    	return code;
    }
    public void setCode(LLInstruction code) {
    	this.code = code;
    }
    
    public int getType() {
        return type;
    }
    public boolean isCode() {
        return type == CODE;
    }
}
