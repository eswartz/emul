/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package sound;

/**
 * @author ejs
 */
public interface Handler {
    /** Write a byte to the sound port. 
     */
     public abstract void writeSound(byte val);
}

