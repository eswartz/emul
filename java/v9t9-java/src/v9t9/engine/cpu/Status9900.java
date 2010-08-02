/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine.cpu;

import org.ejs.coffee.core.utils.HexUtils;


/**
 * The CPU status word is a flattened array of bits, many of which are hard to
 * calculate. We implement it instead as a class which contains the values
 * for which the flags are set. Then, we can save time by not forming the
 * word until it's needed.
 * 
 * @author ejs
 */
public class Status9900 implements Status {
    short lastval; /* last value of calculation */

    short lastcmp; /* last compared-to value, usually 0 */

    byte lastparity;/* last parity candidate */
    
    short bits; /* other bits: ST_O|ST_X|ST_C|ST_INTLEVEL */

    /* status bits */
    public static final short ST_L = (short)0x8000;

    public static final short ST_A = (short)0x4000;

    public static final short ST_E = (short)0x2000;

    public static final short ST_C = (short)0x1000;

    public static final short ST_O = (short)0x800;

    public static final short ST_P = (short)0x400;

    public static final short ST_X = (short)0x200;

    public static final short ST_INTLEVEL = (short)0xf;

    public Status9900() {
    }

    Status9900(short lastval, short lastcmp, byte lastparity, short bits) {
        this.lastval = lastval;
        this.lastcmp = lastcmp;
        this.lastparity = lastparity;
        this.bits = bits;
    }
    
    public Status9900(short val) {
        expand(val);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#toString()
	 */
    @Override
    public String toString() {
    	flatten();
    	return ((bits & Status9900.ST_L) != 0 ? "L" : " ")
    		+ ((bits & Status9900.ST_A) != 0 ? "A" : " ")
    		+ ((bits & Status9900.ST_E) != 0 ? "E" : " ")
    		+ ((bits & Status9900.ST_C) != 0 ? "C" : " ")
    		+ ((bits & Status9900.ST_O) != 0 ? "O" : " ")
    		+ ((bits & Status9900.ST_P) != 0 ? "P" : " ")
    		+ ((bits & Status9900.ST_X) != 0 ? "X" : " ")
    		+ (HexUtils.toHex2((bits & 0xf)));
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#copyTo(v9t9.engine.cpu.Status9900)
	 */
    public void copyTo(Status copy_) {
    	Status9900 copy = (Status9900) copy_;
        copy.lastval = lastval;
        copy.lastcmp = lastcmp;
        copy.lastparity = lastparity;
        copy.bits = bits;
    }

    

    @Override
	protected Object clone()  {
        return new Status9900(lastval, lastcmp, lastparity, bits);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#flatten()
	 */
    public short flatten() {
        bits = (short) (bits & ~(Status9900.ST_L + Status9900.ST_E + Status9900.ST_A + Status9900.ST_P)
                | ((lastval & 0xffff) > (lastcmp & 0xffff) ? Status9900.ST_L : 0)
                | (lastval > lastcmp ? Status9900.ST_A : 0)
                | (lastval == lastcmp ? Status9900.ST_E : 0) 
                | (isOddParity(lastparity) ? Status9900.ST_P : 0)
            	);
        return bits;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#expand(short)
	 */
    public void expand(short stat) {
        lastval = lastcmp = 0;
        if ((stat & Status9900.ST_E) == 0) {
            if ((stat & Status9900.ST_L + Status9900.ST_A) == 0) {
				lastcmp = 2; /* less than arith+logical: 0, 2 */
			} else {
                lastval++;
                if ((stat & Status9900.ST_L) == 0) {
                    /* less than logical only: 0, 0xfffe (-2) */
                    lastcmp = (short) -2;
                } else if ((stat & Status9900.ST_A) == 0) {
                    /* less than arithmetic only: 0xfffe (-2), 0 */
                    lastval = (short) 0xfffe;
                }
            }
        }
        lastparity = (byte) ((stat & Status9900.ST_P) != 0 ? 1 : 0);
        bits = (short) (bits & ~ST_INTLEVEL | stat & ST_INTLEVEL);
    }

    /*
     * Set lae, preserve C and O
     */
    public void set_LAE(short val) {
        lastcmp = 0;
        lastval = val;
    }

    /*
     * Set lae, preserve C and O (BYTE)
     */
    public void set_BYTE_LAEP(byte val) {
        lastval = val;
        lastcmp = 0;
        lastparity = val;
    }

    /*
     * Set lae, and O if val == 0x8000
     */
    public void set_LAEO(short val) {
        set_LAE(val);
        if (val == (short)0x8000) {
			bits |= Status9900.ST_O;
		} else {
			bits &= ~Status9900.ST_O;
		}
    }

    /*
     * For COC, CZC, and TB
     */
    public void set_E(boolean equal) {
        // TODO: hmm, this actually clears L> and A>, while TB doesn't strictly do that.
        if (equal) {
            lastval = lastcmp;
        } else {
            lastcmp = (short) (lastval+1);
        }
    }
    
    /*
	Set laeco for add, preserve none
	*/
    public void set_ADD_LAECO(short dst,short src) {
        short res = (short) (dst + src);
        bits &= ~(Status9900.ST_C|Status9900.ST_O);
        if ((dst & src & 0x8000) != 0
                || (dst & 0x8000 ^ src & 0x8000) != 0 && (res & 0x8000) == 0) {
			bits |= Status9900.ST_C;
		}
        if ( ((~dst & ~src & res | dst & src & ~res) & 0x8000) != 0) {
			bits |= Status9900.ST_O;
		}
        lastval = res; lastcmp = 0; 
    }

    /*
	Set laeco for subtract, preserve none
	*/
    public void set_SUB_LAECO(short dst, short src) {
        set_ADD_LAECO(dst, (short) (1+~src));
        if (src==0 || src==(short)0x8000) {
			bits |= Status9900.ST_C;
		}
    }

    /*
	Set laeco for add, preserve none (BYTE)
	*/
    public void set_ADD_BYTE_LAECOP(byte dst, byte src) {
        byte res = (byte)(dst+src);
        bits &= ~(Status9900.ST_C|Status9900.ST_O|Status9900.ST_P);
        if ( (dst & src & 0x80) != 0 ||  
		(dst & 0x80 ^ src & 0x80) != 0 && (res & 0x80) == 0 ) {
			bits |= Status9900.ST_C;
		}
		
        if ( ((~dst & ~src & res | dst & src & ~res) & 0x80) != 0) {
			bits |= Status9900.ST_O;
		}
        
        lastparity = res;
        lastval = res; lastcmp = 0; 
    }
    
    /*
	Set laeco for subtract, preserve none (BYTE)
	*/
    public void set_SUB_BYTE_LAECOP(byte dst, byte src) {
        set_ADD_BYTE_LAECOP(dst, (byte)(1+~src));
        if (src==0 || (src & 0xff) == 0x80) {
			bits |= Status9900.ST_C;
		}
    }

    /*
	For ABS and DIV
	*/
    public void set_O(boolean b) {
        if (b) {
			bits |= Status9900.ST_O;
		} else {
			bits &= ~Status9900.ST_O;
		}
    }
    
    /*	
	For CMP
	*/
    public void set_CMP(short a, short b) {
        lastval = a;
        lastcmp = b;
    }

    /*	
	For CMP
	*/
    public void set_BYTE_CMP(byte a, byte b) {
        lastval = a;
        lastcmp = b;
    }

    /*
	Right shift carries
	*/
    public void set_SHIFT_RIGHT_C(short a, short c) {
        short mask= (short) (c != 0 ? 1 << c-1 : 0);
        if ((a & mask) != 0) {
			bits |= Status9900.ST_C;
		} else {
			bits &= ~Status9900.ST_C;
		}
    }

    /*
     Left shift overflow & status
     */
    public void set_SHIFT_LEFT_CO(short a, short c) {
        short mask = (short)(0x10000 >> c);
        
        if ((a & mask) != 0) {
			bits |= Status9900.ST_C;
		} else {
			bits &= ~Status9900.ST_C;
		}
        
        if (((a ^ a<<c) & 0x8000) != 0) {
			bits |= Status9900.ST_O;
		} else {
			bits &= ~Status9900.ST_O;
		}
	}

    /*
     For XOP
     */
    public void set_X() {
        bits |= Status9900.ST_X;
    }
	
    /*
     * for LIMI
     */
    public void setIntMask(int mask) {
        bits = (short) (bits & ~0xf | mask & 0xf);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isLT()
	 */
    public boolean isLT() {
        return lastval < lastcmp;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isLE()
	 */
    public boolean isLE() {
        return (lastval & 0xffff) <= (lastcmp & 0xffff);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isL()
	 */
    public boolean isL() {
        return (lastval & 0xffff) < (lastcmp & 0xffff);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isEQ()
	 */
    public boolean isEQ() {
        return lastval == lastcmp;        
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isNE()
	 */
    public boolean isNE() {
        return lastval != lastcmp;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isHE()
	 */
    public boolean isHE() {
        return (lastval & 0xffff) >= (lastcmp & 0xffff);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isGT()
	 */
    public boolean isGT() {
        return lastval > lastcmp;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isH()
	 */
    public boolean isH() {
        return (lastval & 0xffff) > (lastcmp & 0xffff);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isC()
	 */
    public boolean isC() {
        return (bits & Status9900.ST_C) != 0;
    }
    
    public boolean isO() {
        return (bits & Status9900.ST_O) != 0;
    }
    
    public boolean isP() {
        return isOddParity(lastparity);
    }

    static boolean isOddParity(byte val) {
        final byte parity[] = { 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1,
                1, 0 };

        // determine parity of an 8-bit byte, 0=even, 1=odd
        return ((parity[(val & 0xf)] ^ parity[val >> 4 & 0xf]) & 1) != 0;
    }

    /**
     * @return
     */
    public int getIntMask() {
        return bits & ST_INTLEVEL;
    }

    
}