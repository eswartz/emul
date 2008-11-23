/**
 * 
 */
package v9t9.engine.memory;

public class RamArea extends WordMemoryArea {
    public RamArea(int size) {
        memory = new short[size];
        read = memory;
        write = memory;
    }
}