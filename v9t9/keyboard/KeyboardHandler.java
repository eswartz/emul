/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.keyboard;

/**
 * This handler manages translating keyboard input from the outside
 * world into CRU change commands.
 * @author ejs
 */
public interface KeyboardHandler {
    /** 
     * Scan keyboard and update keyboard state
     *
     */
    public void scan(KeyboardState state);
}
