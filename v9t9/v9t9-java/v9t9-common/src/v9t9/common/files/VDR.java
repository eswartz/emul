/*
  VDR.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;


/**
 * Volume description record
 * @author ejs
 *
 */
public class VDR {
    public static final int VDRSIZE = 256;
    
    /** 10 bytes, padded with spaces */
    protected final byte[] volname = new byte[10];
    
    protected final byte[] numSecs = new byte[2];
    protected int secsPerTrack;
    /** 'DSR' */
    protected final byte[] dsrMark = new byte[3];
    
    /** ' ' or 'P' */
    protected char protection;
    protected int tracksPerSide;
    protected int sides;
    /** single: 1, double: 2 */
    protected int density;
    protected final byte[] reserved14 = new byte[0x24];
    protected final byte[] secBitMap = new byte[0xB4];
    protected final byte[] reservedEC = new byte[0x14];
    
    public VDR() {
    	
    }
    public static VDR createVDR(byte[] data, int offset) {
        VDR vdr = new VDR();
        
    	System.arraycopy(data, offset, vdr.volname, 0, vdr.volname.length);
    	System.arraycopy(data, offset + 0xA, vdr.numSecs, 0, vdr.numSecs.length);
    	vdr.secsPerTrack = data[offset + 0xc] & 0xff;
    	System.arraycopy(data, offset + 0xd, vdr.dsrMark, 0, vdr.dsrMark.length);
        vdr.protection = (char) data[offset + 0x10];
        vdr.tracksPerSide = data[offset + 0x11] & 0xff;
        vdr.sides = data[offset + 0x12] & 0xff;
        vdr.density = data[offset + 0x13] & 0xff;
        System.arraycopy(data, offset + 0x14, vdr.reserved14, 0, vdr.reserved14.length);
        System.arraycopy(data, offset + 0x38, vdr.secBitMap, 0, vdr.secBitMap.length);
        System.arraycopy(data, offset + 0xEC, vdr.reservedEC, 0, vdr.reservedEC.length);
        
        return vdr;
    }
    
	public String getVolumeName() {
		StringBuilder builder = new StringBuilder();
		int len = 0;
    	for (int i = 0; i < volname.length; i++) {
    		char ch = (char) volname[i];
    		if (ch != ' ')
    			len = i;
    		builder.append(Character.toUpperCase(ch));
    	}
    	builder.setLength(len + 1);
		return builder.toString();
	}


	public int getTotalSecs() {
		return ((numSecs[0] & 0xff) << 8) | (numSecs[1] & 0xff);
	}
	
	public int getSecsUsed() {
		int bytes = getTotalSecs() / 8;
		int count = 0;
		for (int i = 0; i < bytes; i++) {
			if (i >= secBitMap.length)
				return -1;
			for (int j = 0x80; j > 0; j >>= 1) {
				if ((secBitMap[i] & j) != 0) {
					count++;
				}
			}
		}
		return count;
	}

}
