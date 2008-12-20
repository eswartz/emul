/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.engine;

import v9t9.keyboard.KeyboardState;

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
    
    /**
     * Inject text into the keyboard at a steady pace
     */
    public void pasteText(String contents);
}
