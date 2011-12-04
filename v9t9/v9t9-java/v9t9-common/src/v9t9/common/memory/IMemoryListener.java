/**
 * 
 */
package v9t9.common.memory;

public interface IMemoryListener {
	/** The mapping of addresses changed, e.g., due to banking */
    void logicalMemoryMapChanged(IMemoryEntry entry);
    /** The mapping of entries changed */
    void physicalMemoryMapChanged(IMemoryEntry entry);
}