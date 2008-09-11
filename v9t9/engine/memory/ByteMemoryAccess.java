package v9t9.engine.memory;

/**
 * @author ejs
 *
 */
public class ByteMemoryAccess {
	public byte[] memory;
	public int offset;

	public ByteMemoryAccess(byte[] memory, int offset) {
		this.memory = memory;
		this.offset = offset;
	}
	
}
