package v9t9.engine;
/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */


/**
 * Handle the behavior of the CRU.
 * 
 * @author ejs
 */
public interface CruHandler {
    public void writeBits(int addr, int val, int num);
    public int readBits(int addr, int num);
}
