/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

public abstract class PabHandler {
	protected Short pabaddr;
	
	protected String devname;
	protected String fname;
	protected PabStruct pab;
	protected final MemoryTransfer xfer;

	public PabHandler(MemoryTransfer xfer) {
		/*  rambase+0x56 holds a pointer into VDP RAM to the end
		   of the device name (RS232|, DSK|., DSK1|.ed) */
	
		//pf.fnptr = fn;
		//fiad_tifile_clear(&pf->tf);
	
		this.xfer = xfer;

		byte len;
		short fpaddr;
		short devlen;

		fpaddr = xfer.readParamWord(0x56);
		devlen = xfer.readParamWord(0x54);
		pabaddr = (short) (fpaddr - 9 - devlen - 1);

		len = xfer.readVdpByte(fpaddr - devlen - 1);					/* length of device+filename */
		short fnaddr = (short) (fpaddr + 1);	/* addr of filename (skip period) */
		len -= devlen + 1;	/* minus length of device + period */
		fname = readFilename(fnaddr, len);
		
		devname = readFilename((short) (fnaddr - devlen - 1), devlen);
	
		pab = new PabStruct();
		
		pab.fetch(xfer, pabaddr);
	}
	
	/**
	 * Construct handler from pre-created pab information.
	 * @param pab2
	 */
	public PabHandler(MemoryTransfer xfer, PabStruct pab) {
		this.xfer = xfer;
		this.pab = pab;
		int idx = pab.path.indexOf('.');
		this.devname = pab.path.substring(0, idx);
		this.fname = pab.path.substring(idx + 1);
	}

	protected String readFilename(short addr, int len) {
		StringBuilder builder = new StringBuilder();
		int endAddr = addr;
		while (len-- > 0) {
			byte ch = xfer.readVdpByte(endAddr);
			builder.append((char) ch);
			endAddr++;
		}
		return builder.toString();
	}

	abstract public void run() throws DsrException;
	
	public void error(DsrException e) {
		
		int current = pab.pflags;
		current = (current & ~0xe0) | (e.getErrorCode());
		pab.pflags = current;
		e.printStackTrace();
		
	}
	
	public void store() {
		if (pabaddr != null) {
			pab.store(xfer);
		}
	}

}