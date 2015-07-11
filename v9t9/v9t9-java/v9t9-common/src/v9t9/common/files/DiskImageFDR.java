/*
  DiskImageFDR.java

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
import java.util.ArrayList;
import java.util.List;

public class DiskImageFDR extends TIFDR {

    public static final int FDRSIZE = 256;
    
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
    u8          dcpb[192];  // sector layout of file, in >UM >SN >OF == >NUM >OFS format
    u8          comment[36];  // used for comments
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
	        stream.read(fdr.comment);
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
        System.arraycopy(data, offset + 0xDC, fdr.comment, 0, fdr.comment.length);
        
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

    public byte[] toBytes() {
    	ByteArrayOutputStream os = new ByteArrayOutputStream(256);
    	
    	try {
	    	os.write(filename);
	    	os.write(res10);
	    	os.write(flags);
	    	os.write(recspersec);
	    	os.write(secsused >> 8);
	    	os.write(secsused & 0xff);
	    	os.write(byteoffs);
	    	os.write(reclen);
	    	os.write(numrecs & 0xff);
	    	os.write(numrecs >> 8);
	    	os.write(rec20);
	        os.write(dcpb);
	        os.write(comment);
	        
	        os.close();
    	} catch (IOException e) {
    		throw new IllegalStateException(e);
    	}
    	
        return os.toByteArray();
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
    	while (secIdx < secs.length && clusOffs < dcpb.length) {
    		int um = dcpb[clusOffs++] & 0xff;
    		int sn = dcpb[clusOffs++] & 0xff;
    		int of = dcpb[clusOffs++] & 0xff;
    		if ((um | sn | of) == 0) 
    			break;
    		int num = um | ((sn & 0xf) << 8);
    		int ofs = (of << 4) | ((sn >> 4) & 0xf);
    		while (secIdx <= ofs && secIdx < secs.length) {
    			secs[secIdx++] = num++;
    		}
    	}
    	
    	while (secIdx < secs.length) {
    		secs[secIdx++] = -1;
    	}
    	
    	return secs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.FDR#setContentSectors(int[])
	 */
	@Override
	public void setContentSectors(int[] secs) throws IOException {

    	int clusOffs = 0;
    	
    	int startSec = -1;
    	int prevSec = -1;
    	int ofs = 0;
		for (int i = 0; i < secs.length; i++) {
			if (startSec < 0) {
				startSec = secs[i];
			} else if (prevSec + 1 == secs[i]) {
				// ok
			} else {
				clusOffs = addContentRange(clusOffs, startSec, ofs);
				startSec = secs[i];
			}
			prevSec = secs[i];
			ofs++;
		}
		if (prevSec >= 0) {
			clusOffs = addContentRange(clusOffs, startSec, ofs);
		}
	}
	
	/**
	 * @param clusOffs
	 * @param startSec
	 * @param i
	 * @return
	 */
	protected int addContentRange(int clusOffs, int num, int ofs) throws IOException {
		// encode cluster list:
    	//
    	// >1C-FF 	Cluster list 	>UM >SN >OF == >NUM >OFS

		if (clusOffs + 3 > dcpb.length) 
			throw new IOException("cannot encode sectors for file; disk is too fragmented: " + this);
		
		int um = num & 0xff;
		dcpb[clusOffs++] = (byte) um;
		
		int sn = ((ofs << 4) & 0xf0) | ((num >> 8) & 0xf);
		dcpb[clusOffs++] = (byte) sn;
		
		int of = (ofs >> 4);
		dcpb[clusOffs++] = (byte) of;
		
		return clusOffs;
	}
	/**
	 * @param cnt 
	 * @throws IOException 
	 * 
	 */
	public void allocateSectors(IDiskImage image, int cnt) throws IOException {
		for (int s = 0; s < cnt; s++) {
			int sec = image.allocateSector(32);
			addContentSector(sec);
			setSectorsUsed(getSectorsUsed() + 1);
		}
	}
	/**
	 * @param sec
	 * @throws IOException 
	 */
	public void addContentSector(int sec) throws IOException {
		int[] secs = getContentSectors();
		List<Integer> secList = new ArrayList<Integer>(secs.length);
		for (int s : secs) {
			secList.add(s);
		}
		
		if (secList.contains(sec)) {
			throw new IOException("sector " + sec + " already allocated to file: " + this);
		}
		
		int idx = secList.indexOf(-1);
		if (idx >= 0)
			secList.set(idx, sec);
		else
			secList.add(sec);
		
		int[] newSecs = new int[secList.size()];
		for (int i = 0; i < newSecs.length; i++)
			newSecs[i] = secList.get(i);
		
		setContentSectors(newSecs);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFDRInfo#getComment()
	 */
	@Override
	public String getComment() {
		String str = new String(comment).trim();
		if (str.length() > 0)
			return str;
		return null;
	}
}
