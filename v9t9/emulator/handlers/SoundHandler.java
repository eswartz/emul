/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.emulator.handlers;

/**
 * @author ejs
 */
public interface SoundHandler {
    /** Write a byte to the sound port. 
     */
     public abstract void writeSound(byte val);
}

