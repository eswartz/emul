/*
  PabHandler.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.dsr;

import v9t9.common.dsr.IMemoryTransfer;

public abstract class PabHandler {
	protected Short pabaddr;
	
	public String devname;
	public String fname;
	protected PabStruct pab;
	protected final IMemoryTransfer xfer;

	public PabHandler(IMemoryTransfer xfer) {
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
		
		devname = readFilename((short) (fnaddr - devlen - 1), devlen).toUpperCase();
	
		pab = new PabStruct();
		
		pab.path = devname + "." + fname;
		
		pab.fetch(xfer, pabaddr);
	}
	
	/**
	 * Construct handler from pre-created pab information.
	 * @param pab2
	 */
	public PabHandler(IMemoryTransfer xfer, PabStruct pab) {
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
		
		if (e.getErrorCode() != PabConstants.e_endoffile)
			System.out.println(e.getMessage());
	}
	
	public void store() {
		if (pabaddr != null) {
			pab.store(xfer);
		}
	}

}