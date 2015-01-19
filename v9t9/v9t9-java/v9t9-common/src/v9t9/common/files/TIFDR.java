/*
  TIFDR.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public abstract class TIFDR extends FDR {

    /**
	 * @param fdrsize
	 */
	public TIFDR(int fdrsize) {
		super(fdrsize);
		dcpb = new byte[Math.min(192, fdrsize - 28)];
		if (fdrsize > 0xdc)
			comment = new byte[36];
		else
			comment = new byte[0];
	}
	
	/** 10 bytes, padded with spaces */
	protected final byte[] filename = new byte[10];
    
	protected final byte[] res10 = new byte[2];
	protected final byte[] rec20 = new byte[8];
	protected final byte[] dcpb;
	protected final byte[] comment;

	/* (non-Javadoc)
	 * @see v9t9.common.files.FDR#copyFrom(v9t9.common.files.FDR)
	 */
	@Override
	public void copyFrom(FDR srcFdr) throws IOException {
		super.copyFrom(srcFdr);
		if (srcFdr instanceof TIFDR) {
			TIFDR o = (TIFDR) srcFdr;
			System.arraycopy(o.res10, 0, res10, 0, res10.length);
			System.arraycopy(o.rec20, 0, rec20, 0, rec20.length);
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.common.files.FDR#copyAllocation(v9t9.common.files.FDR)
	 */
	@Override
	public void copyAllocation(FDR srcFdr) throws IOException {
		if (srcFdr instanceof TIFDR) {
			TIFDR o = (TIFDR) srcFdr;
			System.arraycopy(o.dcpb, 0, dcpb, 0, Math.min(dcpb.length, o.dcpb.length));
			System.arraycopy(o.comment, 0, comment, 0, Math.min(comment.length, o.comment.length));
		}
	}
}
