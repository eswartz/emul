/**
 * 
 */
package v9t9.engine.memory;

/**
 * This is RAM intended for testing purposes.  Its latency is 0 and it allows
 * full-speed uninterpreted reads and writes everywhere.
 * @author ejs
 *
 */
public class StockRamArea extends WordMemoryArea {
    public StockRamArea(int size) {
    	super(0);
        memory = new short[size];
        read = memory;
        write = memory;
    }
}