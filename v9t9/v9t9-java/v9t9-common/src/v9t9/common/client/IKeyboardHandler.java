/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.common.client;

import v9t9.common.keyboard.IKeyboardState;

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
    public void scan(IKeyboardState state);
}
