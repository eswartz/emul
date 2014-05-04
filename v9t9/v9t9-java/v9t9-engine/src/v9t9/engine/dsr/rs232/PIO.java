/**
 * 
 */
package v9t9.engine.dsr.rs232;

import java.util.Timer;
import java.util.TimerTask;

import v9t9.common.dsr.IOBuffer;
import v9t9.common.dsr.IPIOHandler;
import v9t9.engine.Dumper;
/**
 * This manages high-level emulation of the RS232.
 * @author ejs
 *
 */
public class PIO {
	private Dumper dumper;
	private IPIOHandler handler;
	private Timer timer;

	private IOBuffer xmitBuffer = new IOBuffer(256), recvBuffer = new IOBuffer(256);
	
	public PIO(Dumper dumper) {
		this.dumper = dumper;
		timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				pioMonitor();
			}
			
		}, 0, 1000 / 50);
	}
	
	protected void pioMonitor() {
		if (handler != null) {
			// transmit buffered chars.
			handler.transmitChars(xmitBuffer);
		} else {
			xmitBuffer.clear();
		}
	}
	/**
	 * @param handler the handler to set
	 */
	public void setHandler(IPIOHandler handler) {
		this.handler = handler;
	}
	/**
	 * @return the handler
	 */
	public IPIOHandler getHandler() {
		return handler;
	}

	/**
	 * @return the dumper
	 */
	public Dumper getDumper() {
		return dumper;
	}

	public void transmitChar(byte ch) {
		dumper.info(String.format("PIO: Buffering char %02X (%c)", ch, (char) ch));

		xmitBuffer.add(ch);
	}

	public void dump()
	{
		StringBuilder sb = new StringBuilder(); 
	
		sb.append(String.format("PIO: Write buffer: %s  Read buffer: %s",
			 xmitBuffer, recvBuffer));
	
		dumper.info(sb.toString());
	}

	/**
	 * 
	 */
	public void clear() {
		xmitBuffer.clear();
		recvBuffer.clear();
		dump();
	}

	public IOBuffer getRecvBuffer() {
		return recvBuffer;
	}
	public IOBuffer getXmitBuffer() {
		return xmitBuffer;
	}

}
