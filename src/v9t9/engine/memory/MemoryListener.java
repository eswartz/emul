/**
 * 
 */
package v9t9.engine.memory;

public interface MemoryListener {
	/** The mapping of addresses changed, e.g., due to banking */
    void logicalMemoryMapChanged(MemoryEntry entry);
    /** The mapping of entries changed */
    void physicalMemoryMapChanged(MemoryEntry entry);
}