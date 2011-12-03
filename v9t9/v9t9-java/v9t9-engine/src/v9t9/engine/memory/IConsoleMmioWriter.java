/**
 * 
 */
package v9t9.engine.memory;

public interface IConsoleMmioWriter {
    void write(int addrMask, byte val);
}