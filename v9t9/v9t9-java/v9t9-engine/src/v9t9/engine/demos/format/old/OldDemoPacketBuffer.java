/**
 * 
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.OutputStream;

public class OldDemoPacketBuffer extends OldDemoOutputBuffer {
	private final int type;

	public OldDemoPacketBuffer(OutputStream stream, int type, int size) {
		super(stream, size);
		this.type = type;
	}
	protected void writeHeader() throws IOException {
		stream.write(type);
		super.writeHeader();
	}
}
