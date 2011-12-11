/**
 * 
 */
package v9t9.server.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemory;

/**
 * This service extends the standard TCF IMemory service
 * with support for guaranteed memory change tracking.
 * 
 * @author ejs
 *
 */
public interface IMemoryV2 extends IMemory {

	class MemoryChange {
		
		public MemoryChange(Number addr, long size, byte[] data) {
			this.addr = addr;
			this.size = size;
			this.data = data;
		}
		/**
		 * @param map
		 */
		public MemoryChange(Map<String, Object> map) {
			addr = (Number) map.get(PROP_ADDR);
			size = ((Number) map.get(PROP_SIZE)).longValue();
			data = JSON.toByteArray(map.get(PROP_DATA));
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "MemoryChange: " + addr.intValue() + "+" + size + " = " + new String(data);
		}
		public final Number addr;
		public final long size;
		public final byte[] data;
	}
	
	interface MemoryContentChangeListener {
		void contentChanged(String contextId, MemoryChange[] memChanges);
	}
	
	String NAME = "MemoryV2";
	
	String COMMAND_START_CHANGE_NOTIFY = "startChangeNotify";
	String COMMAND_STOP_CHANGE_NOTIFY = "stopChangeNotify";

	String EVENT_CONTENT_CHANGED = "contentChanged";
	
	String PROP_ADDR = "addr";
	String PROP_SIZE = "size";
	String PROP_DATA = "data";
	
	/**
	 * 	As a mode to {@link MemoryContext#get(Number, int, byte[], int, int, int, org.eclipse.tm.tcf.services.IMemory.DoneMemory)}
	 * 	or {@link MemoryContext#set(Number, int, byte[], int, int, int, org.eclipse.tm.tcf.services.IMemory.DoneMemory)},
	 * 	specifies that memory be written without side effects (e.g., memory-mapped I/O or event notification).
	 * 
	 *  The default behavior is to read/write memory as a target program would.
	 */
	int MODE_FLAT = 0x4;
	
	interface DoneCommand {
		void done(Exception error);
	}
	
	/**
	 * Start notifying full memory change information for the given 
	 * context ID.  Fires memoryContentChanged events with extra 'data':<Byte>
	 * attributes.
	 * 
	 * This may be called multiple times for the same context ID 
	 * to update the delay or gap.
	 * 
	 * @param contextId
	 * @param msDelay delay in ms between events
	 * @param granularity minimum gap in bytes reported changed, to minimize
	 * size of delta information. E.g., if this 2, and bytes 0 and 2 change,
	 * then the memoryChanged event will report that bytes 0-3 changed.  
	 * @param done
	 * @return
	 */
	IToken startChangeNotify(String contextId, int msDelay, int granularity, DoneCommand done);
	/**
	 * Stop notifying full memory change information for the given
	 * context ID, so no more memoryContentChanged events will be generated.
	 * @param contextId
	 * @param done
	 * @return
	 */
	IToken stopChangeNotify(String contextId, DoneCommand done);
	
	void addListener(MemoryContentChangeListener listener);
	void removeListener(MemoryContentChangeListener listener);
}
