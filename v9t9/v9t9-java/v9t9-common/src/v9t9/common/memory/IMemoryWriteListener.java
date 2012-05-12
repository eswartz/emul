package v9t9.common.memory;

/** Listener for noticing memory writes. */
public interface IMemoryWriteListener {
	void changed(IMemoryEntry entry, int addr, Number value);
}