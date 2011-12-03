/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.engine.client;


import v9t9.engine.keyboard.KeyboardState;

/**
 * This handler manages translating keyboard input from the outside
 * world into CRU change commands.
 * @author ejs
 */
public interface IKeyboardHandler {
    /** 
     * Scan keyboard and update keyboard state
     *
     */
    public void scan(KeyboardState state);
}
