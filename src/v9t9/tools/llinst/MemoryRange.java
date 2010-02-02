/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.llinst;

import v9t9.engine.cpu.IInstruction;


public class MemoryRange {
    static public final int EMPTY = 0;
    static public final int CODE = 1;
    static public final int DATA = 2;
    
    /** start address */
    int from;
    /** type (EMPTY, CODE, DATA) */
    int type;
    /** First instruction, if code */
    private IInstruction code;
    
    public MemoryRange(int baseAddr, int type) {
        this.from = baseAddr;
        this.type = type;
        this.code = null;
    }

    public IInstruction getCode() {
    	return code;
    }
    public void setCode(IInstruction code) {
    	this.code = code;
    }
    
    public int getType() {
        return type;
    }
    public boolean isCode() {
        return type == CODE;
    }
}
