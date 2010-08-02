/*
 * (c) Ed Swartz, 2010
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
public class StatusMFP201 implements Status {
    short lastval; /* last value of calculation */

    short lastcmp; /* last compared-to value, usually 0 */

    short bits; /* other bits: ST_V|ST_C|ST_INTLEVEL */

    /* status bits */
    public static final short ST_L = (short)0x8000;

    public static final short ST_N = (short)4;

    public static final short ST_E = (short)2;

    public static final short ST_C = (short)1;

    public static final short ST_V = (short)8;

    public static final short ST_INTLEVEL = (short)0xf;

    public StatusMFP201() {
    }

    StatusMFP201(short lastval, short lastcmp, short bits) {
        this.lastval = lastval;
        this.lastcmp = lastcmp;
        this.bits = bits;
    }
    
    public StatusMFP201(short val) {
        expand(val);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#toString()
	 */
    @Override
    public String toString() {
    	flatten();
    	return ((bits & StatusMFP201.ST_L) != 0 ? "L" : " ")
    		+ ((bits & StatusMFP201.ST_N) != 0 ? "N" : " ")
    		+ ((bits & StatusMFP201.ST_E) != 0 ? "E" : " ")
    		+ ((bits & StatusMFP201.ST_C) != 0 ? "C" : " ")
    		+ ((bits & StatusMFP201.ST_V) != 0 ? "V" : " ")
    		+ (HexUtils.toHex2((bits & 0xf)));
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#copyTo(v9t9.engine.cpu.Status9900)
	 */
    public void copyTo(Status copy_) {
    	StatusMFP201 copy = (StatusMFP201) copy_;
        copy.lastval = lastval;
        copy.lastcmp = lastcmp;
        copy.bits = bits;
    }

    

    @Override
	protected Object clone()  {
        return new StatusMFP201(lastval, lastcmp, bits);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#flatten()
	 */
    public short flatten() {
        bits = (short) (bits & ~(StatusMFP201.ST_L + StatusMFP201.ST_E + StatusMFP201.ST_N)
                | ((lastval & 0xffff) > (lastcmp & 0xffff) ? StatusMFP201.ST_L : 0)
                | (lastval > lastcmp ? StatusMFP201.ST_N : 0)
                | (lastval == lastcmp ? StatusMFP201.ST_E : 0) 
            	);
        return bits;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#expand(short)
	 */
    public void expand(short stat) {
        lastval = lastcmp = 0;
        if ((stat & StatusMFP201.ST_E) == 0) {
            if ((stat & StatusMFP201.ST_L + StatusMFP201.ST_N) == 0) {
				lastcmp = 2; /* less than arith+logical: 0, 2 */
			} else {
                lastval++;
                if ((stat & StatusMFP201.ST_L) == 0) {
                    /* less than logical only: 0, 0xfffe (-2) */
                    lastcmp = (short) -2;
                } else if ((stat & StatusMFP201.ST_N) == 0) {
                    /* less than arithmetic only: 0xfffe (-2), 0 */
                    lastval = (short) 0xfffe;
                }
            }
        }
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
     * Set lae, and O if val == 0x8000
     */
    public void set_LAEV(short val) {
        set_LAE(val);
        if (val == (short)0x8000) {
			bits |= StatusMFP201.ST_V;
		} else {
			bits &= ~StatusMFP201.ST_V;
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
    public void set_ADD_LNECV(short dst,short src) {
        short res = (short) (dst + src);
        bits &= ~(StatusMFP201.ST_C|StatusMFP201.ST_V);
        if ((dst & src & 0x8000) != 0
                || (dst & 0x8000 ^ src & 0x8000) != 0 && (res & 0x8000) == 0) {
			bits |= StatusMFP201.ST_C;
		}
        if ( ((~dst & ~src & res | dst & src & ~res) & 0x8000) != 0) {
			bits |= StatusMFP201.ST_V;
		}
        lastval = res; lastcmp = 0; 
    }

    /*
	Set laeco for subtract, preserve none
	*/
    public void set_SUB_LNECV(short dst, short src) {
        set_ADD_LNECV(dst, (short) (1+~src));
        if (src==0 || src==(short)0x8000) {
			bits |= StatusMFP201.ST_C;
		}
    }

    /*
	For ABS and DIV
	*/
    public void set_V(boolean b) {
        if (b) {
			bits |= StatusMFP201.ST_V;
		} else {
			bits &= ~StatusMFP201.ST_V;
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
			bits |= StatusMFP201.ST_C;
		} else {
			bits &= ~StatusMFP201.ST_C;
		}
    }

    /*
     Left shift overflow & status
     */
    public void set_SHIFT_LEFT_CO(short a, short c) {
        short mask = (short)(0x10000 >> c);
        
        if ((a & mask) != 0) {
			bits |= StatusMFP201.ST_C;
		} else {
			bits &= ~StatusMFP201.ST_C;
		}
        
        if (((a ^ a<<c) & 0x8000) != 0) {
			bits |= StatusMFP201.ST_V;
		} else {
			bits &= ~StatusMFP201.ST_V;
		}
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
        return (bits & StatusMFP201.ST_C) != 0;
    }
    
    public boolean isV() {
        return (bits & StatusMFP201.ST_V) != 0;
    }
    
    /**
     * @return
     */
    public int getIntMask() {
        return bits & ST_INTLEVEL;
    }

    
}