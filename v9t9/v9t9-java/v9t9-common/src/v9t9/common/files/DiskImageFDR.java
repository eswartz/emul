/*
  DiskImageFDR.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DiskImageFDR extends FDR {

    public static final int FDRSIZE = 256;
    
    /** 10 bytes, padded with spaces */
    private final byte[] filename = new byte[10];
    
    private final byte[] res10 = new byte[2];
    private final byte[] rec20 = new byte[8];
    private final byte[] dcpb = new byte[228];

    /**
    char        filenam[10];// filename, padded with spaces
    u8          res10[2];   // reserved 
    u8          flags;      // filetype flags 
    u8          recspersec; // # records per sector, 
                               256/reclen for FIXED,
                               255/(reclen+1) for VAR,
                               0 for program 
    u16         secsused;   // [big-endian]:  # sectors in file 
    u8          byteoffs;   // last byte used in file 
                                   (0 = no last empty sector) 
    u8          reclen;     // record length, 0 for program 
    u16         numrecs;    // [little-endian]:  # records for FIXED file,
                               # sectors for VARIABLE file,
                               0 for program 
    u8          rec20[8];   // reserved 
    u8          dcpb[228];  // sector layout of file, in >UM >SN >OF == >NUM >OFS format
     */

    public DiskImageFDR() {
    	super(FDRSIZE);
    }
    public static DiskImageFDR readFDR(File file) throws IOException, InvalidFDRException {
        DiskImageFDR fdr = new DiskImageFDR();
        FileInputStream stream = new FileInputStream(file);
        try {
	        stream.read(fdr.filename);
	        stream.read(fdr.res10);
	        fdr.flags = stream.read();
	        fdr.recspersec = stream.read();
	        fdr.secsused = (stream.read() << 8) | stream.read();
	        fdr.byteoffs = stream.read();
	        fdr.reclen = stream.read();
	        fdr.numrecs = stream.read() | (stream.read() << 8);
	        stream.read(fdr.rec20);
	        stream.read(fdr.dcpb);
        } finally { 
        	stream.close();
        }
        fdr.validate(file);
        
        return fdr;
    }
    
    public static DiskImageFDR createFDR(byte[] data, int offset) {
        DiskImageFDR fdr = new DiskImageFDR();
        
    	System.arraycopy(data, offset, fdr.filename, 0, fdr.filename.length);
    	System.arraycopy(data, offset + 0xA, fdr.res10, 0, fdr.res10.length);
    	fdr.flags = data[offset + 0xc] & 0xff;
        fdr.recspersec = data[offset + 0xd] & 0xff;
        fdr.secsused = ((data[offset + 0xe] & 0xff) << 8) | (data[offset + 0xf] & 0xff);
        fdr.byteoffs = data[offset + 0x10] & 0xff;
        fdr.reclen = data[offset + 0x11] & 0xff;
        fdr.numrecs = (data[offset + 0x12] & 0xff) | ((data[offset + 0x13] & 0xff) << 8);
        System.arraycopy(data, offset + 0x14, fdr.rec20, 0, fdr.rec20.length);
        System.arraycopy(data, offset + 0x1C, fdr.dcpb, 0, fdr.dcpb.length);
        
        return fdr;
    }

    /**
     * Set the filename
     */
    public void setFileName(String name) throws IOException {
    	if (name.length() > 10)
    		throw new IOException("Name too long: " + name);
    	for (int i = 0; i < filename.length; i++) {
    		char ch = ' ';
    		if (i < name.length())
    			ch = name.charAt(i);
    		filename[i] = (byte) ch;
    	}
    }

    public void writeFDR(File file) throws IOException {
    	file.setWritable(true);
    	
    	RandomAccessFile raf = new RandomAccessFile(file, "rw");
    	raf.seek(0);
    	
    	raf.write(filename);
    	raf.write(res10);
    	raf.write(flags);
    	raf.write(recspersec);
    	raf.write(secsused >> 8);
    	raf.write(secsused & 0xff);
    	raf.write(byteoffs);
    	raf.write(reclen);
    	raf.write(numrecs & 0xff);
    	raf.write(numrecs >> 8);
    	raf.write(rec20);
        raf.write(dcpb);
        
        raf.close();
        
        file.setWritable(!isReadOnly());
    }
    
	public String getFileName() {
		StringBuilder builder = new StringBuilder();
		int len = 0;
    	for (int i = 0; i < filename.length; i++) {
    		char ch = (char) filename[i];
    		if (ch != ' ')
    			len = i;
    		builder.append(ch);
    	}
    	builder.setLength(len + 1);
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.FDR#fetchContentSectors()
	 */
	@Override
	protected int[] fetchContentSectors() {
    	int[] secs = new int[getSectorsUsed()];
    	
		// decode cluster list:
    	//
    	// >1C-FF 	Cluster list 	>UM >SN >OF == >NUM >OFS

    	int clusOffs = 0;
    	int secIdx = 0;
    	while (secIdx < secs.length) {
    		int um = dcpb[clusOffs++] & 0xff;
    		int sn = dcpb[clusOffs++] & 0xff;
    		int of = dcpb[clusOffs++] & 0xff;
    		if ((um | sn | of) == 0) 
    			break;
    		int num = um | ((sn & 0xf) << 8);
    		int ofs = (of << 4) | ((sn >> 4) & 0xf);
    		while (secIdx <= ofs) {
    			secs[secIdx++] = num++;
    		}
    	}
    	
    	while (secIdx < secs.length) {
    		secs[secIdx++] = -1;
    	}
    	
    	return secs;
	}
	
}
