package v9t9.common.memory;

/** Listener for noticing memory accesses. */
public interface IMemoryAccessListener {
	/** Indicate that a memory entry was accessed.
	 * @param entry
	 */
	void access(IMemoryEntry entry);
}