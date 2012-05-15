/**
 * 
 */
package v9t9.engine.demos.format.old;

import java.io.IOException;
import java.io.OutputStream;

import v9t9.common.demo.IDemoOutputEventBuffer;

public abstract class OldDemoPacketBuffer extends OldDemoOutputBuffer implements IDemoOutputEventBuffer {
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
