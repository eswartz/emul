/*
  VDR.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Volume information block
 * @author ejs
 *
 */
public class VIB {
	public static final int OFFS_NAME = 0;
	public static final int OFFS_NUM_SECS = 0xA;
	public static final int OFFS_SECS_PER_TRACK = 0xc;
	public static final int OFFS_DSR_MARK = 0xd;
	public static final int OFFS_PROTECTION = 0x10;
	public static final int OFFS_TRACKS_PER_SIDE = 0x11;
	public static final int OFFS_SIDES = 0x12;
	public static final int OFFS_DENSITY = 0x13;
	public static final int OFFS_RESERVED_14 = 0x14;
	public static final int OFFS_SECTOR_BITMAP = 0x38;
	public static final int OFFS_RESERVED_EC = 0xEC;

	public static final int VDRSIZE = 256;

    public static VIB createVIB(byte[] data, int offset) {
        VIB vib = new VIB();
        
    	System.arraycopy(data, offset + OFFS_NAME, vib.volname, 0, vib.volname.length);
    	System.arraycopy(data, offset + OFFS_NUM_SECS, vib.numSecs, 0, vib.numSecs.length);
    	vib.secsPerTrack = data[offset + OFFS_SECS_PER_TRACK] & 0xff;
    	System.arraycopy(data, offset + OFFS_DSR_MARK, vib.dsrMark, 0, vib.dsrMark.length);
        vib.protection = (char) data[offset + OFFS_PROTECTION];
        vib.tracksPerSide = data[offset + OFFS_TRACKS_PER_SIDE] & 0xff;
        vib.sides = data[offset + OFFS_SIDES] & 0xff;
        vib.density = data[offset + OFFS_DENSITY] & 0xff;
        System.arraycopy(data, offset + OFFS_RESERVED_14, vib.reserved14, 0, vib.reserved14.length);
        System.arraycopy(data, offset + OFFS_SECTOR_BITMAP, vib.secBitMap, 0, vib.secBitMap.length);
        System.arraycopy(data, offset + OFFS_RESERVED_EC, vib.reservedEC, 0, vib.reservedEC.length);
        
        return vib;
    }
    
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
    
    public VIB() {
    	
    }
   
    
	@Override
	public String toString() {
		return "VDR [volname=" + new String(volname) + ", numSecs="
				+ getTotalSecs() + ", secsPerTrack=" + secsPerTrack
				+ ", tracksPerSide=" + tracksPerSide + ", sides=" + sides
				+ ", density=" + density + "]";
	}


	public String getVolumeName() {
		StringBuilder builder = new StringBuilder();
		int len = 0;
    	for (int i = 0; i < volname.length; i++) {
    		char ch = (char) volname[i];
    		if (ch != ' ')
    			len = i;
    		builder.append(ch);
    	}
    	builder.setLength(len + 1);
		return builder.toString();
	}


	public int getTotalSecs() {
		return ((numSecs[0] & 0xff) << 8) | (numSecs[1] & 0xff);
	}
	
	void setTotalSecs(short total) {
		numSecs[0] = (byte) ((total >> 8) & 0xff);
		numSecs[1] = (byte) (total & 0xff);
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

	public boolean isFormatted() {
		return "DSK".equals(new String(dsrMark)) && getTotalSecs() > 0;
	}
	
	public int allocateSector(int start) throws IOException {
		int bytes = getTotalSecs() / 8;
		int num = start;
		int i = start / 8;
		for (; i < bytes; i++) {
			if (i >= secBitMap.length)
				return -1;
			for (int j = 0x1; j < 0x100; j <<= 1) {
				if ((secBitMap[i] & j) == 0) {
					secBitMap[i] |= j;
					return num;
				}
				num++;
			}
			if (start != 0 && i + 1 == bytes)
				i = -1;
		}
		throw new IOException("no free sectors available");
	}
	
	public void deallocateSector(int num) throws IOException {
		int bytes = getTotalSecs() / 8;
		int i = num / 8;
		if (i < 0 || i >= Math.min(bytes, secBitMap.length))
			throw new IOException("illegal sector number: " + num);
		int j = num % 8;
		secBitMap[i] &= ~(0x1 << (7 - j));
	}
	
    public byte[] toBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream(256); 
        
        try {
	        os.write(volname);
	    	os.write(numSecs);
	    	os.write(secsPerTrack);
	    	os.write(dsrMark);
	    	os.write(protection);
	        os.write(tracksPerSide);
	        os.write(sides);
	        os.write(density);
	        os.write(reserved14);
	        os.write(secBitMap);
	        os.write(reservedEC);
        } catch (IOException e) {
        	throw new IllegalStateException(e);
        }
        return os.toByteArray();
    }


	/**
	 * @return
	 */
	public int getSecsPerTrack() {
		return secsPerTrack;
	}

}
