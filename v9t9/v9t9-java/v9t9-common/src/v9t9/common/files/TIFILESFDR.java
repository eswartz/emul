/*
  TIFILESFDR.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class TIFILESFDR extends FDR {

    private byte[] sig = new byte[8];

    private byte[] unused = new byte[100];

	private String name;

	private byte mxt;

	private byte res1b;

	private int exthdr;
	private int crtime;
	private int updtime;

	public TIFILESFDR() {
		super(128);
	}
	
    public static final byte[] SIGNATURE = { 7, 'T', 'I', 'F', 'I', 'L', 'E', 'S' };
    public static final byte[] MULTI_SIGNATURE = { 8, 'T', 'I', 'F', 'I', 'L', 'E', 'S' };
    
    public static FDR readFDR(File file) throws IOException, InvalidFDRException {
        TIFILESFDR fdr = new TIFILESFDR();
        
        FileInputStream stream = new FileInputStream(file);
        try {
        	fdr.name = DiskDirectoryUtils.hostToDSR(file.getName().toUpperCase());
        	
	        stream.read(fdr.sig, 0, 8);
	        
	        if (!Arrays.equals(fdr.sig, SIGNATURE)
	        		&& !Arrays.equals(fdr.sig, MULTI_SIGNATURE)) {
				throw new InvalidFDRException("No TIFILES signature found");
			}
	        
	        fdr.secsused = (stream.read() << 8 | stream.read());
	        fdr.flags = stream.read();
	        fdr.recspersec = stream.read();
	        fdr.byteoffs = stream.read();
	        fdr.reclen = stream.read();
	        fdr.numrecs = (stream.read() | stream.read() << 8);
	        
	        fdr.mxt = (byte) stream.read();
	        fdr.res1b = (byte) stream.read();
	        fdr.exthdr = (stream.read() << 8 | stream.read());
	        fdr.crtime = (stream.read() << 24 | stream.read() << 16 | stream.read() << 8 | stream.read());
	        
	        fdr.unused = new byte[104];
	        int cnt = stream.read(fdr.unused, 0, fdr.unused.length);
	        
	        if (cnt < fdr.unused.length)
	        	throw new InvalidFDRException("File header is too short; expected 128 bytes");
        } finally {
        	stream.close();
        }
        return fdr;
    }
    
    public byte[] toBytes() {

    	ByteArrayOutputStream os = new ByteArrayOutputStream(128);
    	try {
        	
        	os.write(SIGNATURE);
        	os.write(secsused >> 8);
        	os.write(secsused & 0xff);
        	os.write(flags);
        	os.write(recspersec);
        	os.write(byteoffs);
        	os.write(reclen);
        	os.write(numrecs & 0xff);
        	os.write(numrecs >> 8);
        	
        	os.write(mxt);
        	os.write(res1b);
        	os.write(exthdr >> 8);
        	os.write(exthdr & 0xff);
        	
        	os.write(crtime >> 24);
        	os.write(crtime >> 16);
        	os.write(crtime >> 8);
        	os.write(crtime);
        	os.write(updtime >> 24);
        	os.write(updtime >> 16);
        	os.write(updtime >> 8);
        	os.write(updtime);
        	
            os.write(unused);
            
            os.close();

    	} catch (IOException e) {
    		throw new IllegalStateException(e);
    	}
    	return os.toByteArray(); 
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.IFDRInfo#getFileName()
     */
    @Override
    public String getFileName() {
    	return name;
    }
    
    @Override
    public void setFileName(String name) throws IOException {
    	this.name = name;
    }
    
    @Override
    public String getComment() {
    	String possibleComment = new String(unused).trim();
    	if (possibleComment.length() > 0)
			return possibleComment;
    	return null;
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.FDR#fetchContentSectors()
     */
    @Override
    protected int[] fetchContentSectors() {
    	int[] secs = new int[getSectorsUsed()];
    	for (int i = 0; i < secs.length; i++) {
    		secs[i] = 32 + i;
    	}
    	return secs;
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.FDR#setContentSectors(int[])
     */
    @Override
    public void setContentSectors(int[] secs) throws IOException {
    	throw new IOException("cannot allocate sectors for TIFILES files");
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.FDR#copyFrom(v9t9.common.files.FDR)
     */
    @Override
    public void copyFrom(FDR srcFdr) throws IOException {
    	super.copyFrom(srcFdr);
    	if (srcFdr instanceof TIFILESFDR) {
    		TIFILESFDR o = (TIFILESFDR) srcFdr;
    		System.arraycopy(o.sig, 0, sig, 0, sig.length);
    		System.arraycopy(o.unused, 0, unused, 0, unused.length);
    		mxt = o.mxt;
    		res1b = o.res1b;
    		exthdr = o.exthdr;
    		crtime = o.crtime;
    		updtime = o.updtime;
    	}
    }

	/* (non-Javadoc)
	 * @see v9t9.common.files.FDR#copyAllocation(v9t9.common.files.FDR)
	 */
	@Override
	public void copyAllocation(FDR srcFdr) throws IOException {
		throw new IOException("unsupported");
	}
}
