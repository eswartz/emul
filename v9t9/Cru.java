/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9;

/**
 * @author ejs
 */
public interface Cru {
    public void writeBits(int addr, int val, int num);
    public int readBits(int addr, int num);
}
