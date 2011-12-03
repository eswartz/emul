/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Jan 20, 2005
 *
 */
package v9t9.common.cpu;

/**
 * This exception is thrown from within an interpreter
 * or compiled code to indicate the current stream of
 * execution must be restarted somehow -- either due to
 * interrupts changing, due to changes in the memory map,
 * etc. 
 * @author ejs
 *
 */
public class AbortedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AbortedException() {}
}