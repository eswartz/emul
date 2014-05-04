/**
 * 
 */
package v9t9.machine.ti99.dsr.rs232;

import v9t9.common.machine.IMachine;
import v9t9.engine.Dumper;
import v9t9.engine.dsr.rs232.PIO;

/**
 * @author ejs
 *
 */
public class PIORegs {
	/** was CRU BIT 1 set? */
	public boolean reading;			
	/** CRU BIT 2 */
	public boolean handshakein;
	/** CRU BIT 2 */
	public boolean handshakeout;
	/** CRU BIT 3 */
	public boolean sparein;
	/** CRU BIT 3 */
	public boolean spareout;
	/** CRU BIT 4 */
	public boolean reflect;

	/** last transmitted byte */
	public byte data;
	
	private PIO pio;

	/**
	 * 
	 */
	public PIORegs(IMachine machine, PIO pio, Dumper dumper) {
		this.pio = pio;
	}

	/**
	 * @return the pio
	 */
	public PIO getPIO() {
		return pio;
	}
}
