/**
 * 
 */
package v9t9.engine.memory;

/**
 * This is RAM intended for testing purposes.  Its latency is 1 and it allows
 * full-speed uninterpreted reads and writes everywhere.
 * @author ejs
 *
 */
public class StockRamArea extends WordMemoryArea {
    public StockRamArea(int size) {
    	super(1);
        memory = new short[size];
        read = memory;
        write = memory;
    }
}