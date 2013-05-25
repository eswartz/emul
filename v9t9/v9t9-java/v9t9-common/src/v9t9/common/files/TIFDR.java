/**
 * 
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
		dcpb = new byte[fdrsize - 28];
	}
	
	/** 10 bytes, padded with spaces */
	protected final byte[] filename = new byte[10];
    
	protected final byte[] res10 = new byte[2];
	protected final byte[] dcpb;
	protected final byte[] rec20 = new byte[8];

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
		}
	}
}
