/**
 * 
 */
package v9t9.engine.video;

import v9t9.common.memory.ByteMemoryAccess;

/**
 * An abstraction for the video memory access
 * @author ejs
 *
 */
public interface VideoMemory {
	ByteMemoryAccess getByteReadMemoryAccess(int offset);
	
	short flatReadByte(int addr);
}
