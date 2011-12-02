/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Sep 5, 2005
 *
 */
package v9t9.engine.cpu;

public class FunctionInfo {

    public static final int FUNCTION_BL = 1;
    public static final int FUNCTION_BLWP = 2;
    
    /** Function type FUNCTION_xxx */
    public int type;
    
    /** Number of words consumed after caller */
    public int paramWords;
    
    public FunctionInfo(int type, int paramWords) {
        this.type = type;
        this.paramWords = paramWords;
    }

}
