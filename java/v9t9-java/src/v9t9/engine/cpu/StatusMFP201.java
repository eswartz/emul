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
    short bits; 

    /* status bits */
    
    /** negative bit: set when an operation is negative (high bit of result) */
    public static final short ST_N = (short)4;

    /** zero bit: set when an operation is zero */
    public static final short ST_Z = (short)2;

    /** carry bit: set when a carry produced out of high bit */
    public static final short ST_C = (short)1;

    /** overflow bit: set when an operation overflows signed range
     * 
     *  add:  when pos+pos = neg, or neg+neg = pos
     *  sub:  when pos-neg = neg, or neg-pos = pos
     */
    public static final short ST_V = (short)8;

    /** general interrupt enable bit */
    public static final short ST_GIE = (short)8;
    
    public static final short ST_RSV = (short)0xfef0;

    public StatusMFP201() {
    }

    public StatusMFP201(short val) {
        expand(val);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#toString()
	 */
    @Override
    public String toString() {
    	return ((bits & StatusMFP201.ST_N) != 0 ? "N" : " ")
    		+ ((bits & StatusMFP201.ST_Z) != 0 ? "Z" : " ")
    		+ ((bits & StatusMFP201.ST_C) != 0 ? "C" : " ")
    		+ ((bits & StatusMFP201.ST_V) != 0 ? "V" : " ")
    		+ ((bits & StatusMFP201.ST_GIE) != 0 ? "I" : " ")
    		+ (HexUtils.toHex2((bits & ST_RSV)));
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#copyTo(v9t9.engine.cpu.Status9900)
	 */
    public void copyTo(Status copy_) {
    	StatusMFP201 copy = (StatusMFP201) copy_;
        copy.bits = bits;
    }

    

    @Override
	protected Object clone()  {
        return new StatusMFP201(bits);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#flatten()
	 */
    public short flatten() {
        return bits;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#expand(short)
	 */
    public void expand(short stat) {
    	this.bits = stat;
    }

    /**
     * Set bits comparing arithmetically against zero
     */
    public void set_NZ(short val) {
    	bits &= ~(ST_Z + ST_N);
    	if (val == 0)
    		bits |= ST_Z;
    	if (val < 0)
    		bits |= ST_N;
    }

    /**
     * Set bits comparing logically against zero
     */
    public void set_NZV(short val) {
    	set_NZ(val);
    	bits &= ~ST_C;
    	if ((bits & ST_Z) == 0)
    		bits |= ST_C;
    }
    
    /**
     * Set bits for add
	*/
    public void set_ADD_LNZCV(short dst,short src) {
        short res = (short) (dst + src);
        bits &= ~(StatusMFP201.ST_C|StatusMFP201.ST_V);
        if ((dst & src & 0x8000) != 0
                || (dst & 0x8000 ^ src & 0x8000) != 0 && (res & 0x8000) == 0) {
			bits |= StatusMFP201.ST_C;
		}
        if ( ((~dst & ~src & res | dst & src & ~res) & 0x8000) != 0) {
			bits |= StatusMFP201.ST_V;
		}
        set_NZ(res);
    }

    /**
     * Set bits for subtract
	*/
    public void set_SUB_LNZCV(short dst, short src) {
        set_ADD_LNZCV(dst, (short) (1+~src));
        if (src==0 || src==(short)0x8000) {
			bits |= StatusMFP201.ST_C;
		}
    }

    /**
     * Set overflow bit
     */
    public void set_V(boolean b) {
        if (b) {
			bits |= StatusMFP201.ST_V;
		} else {
			bits &= ~StatusMFP201.ST_V;
		}
    }
    
    /**
     * Set carry for right shift
	*/
    public void set_SHIFT_RIGHT_NZC(short a, short c) {
    	set_NZ((short) (a << c));
    	
    	bits &= ~StatusMFP201.ST_V;

        short mask= (short) (c != 0 ? 1 << c-1 : 0);
        if ((a & mask) != 0) {
			bits |= StatusMFP201.ST_C;
		} else {
			bits &= ~StatusMFP201.ST_C;
		}
    }

    /**
     * Set C and V for left shift 
     */
    public void set_SHIFT_LEFT_NZCV(short a, short c) {
    	short res = (short) (a << c);
		set_NZ(res);
    	
        short mask = (short)(0x10000 >> c);
        
        if ((a & mask) != 0) {
			bits |= StatusMFP201.ST_C;
		} else {
			bits &= ~StatusMFP201.ST_C;
		}
        
        if (((a ^ res) & 0x8000) != 0) {
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
        return ((bits & ST_V) != 0) != ((bits & ST_N) != 0);
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isLE()
	 */
    public boolean isLE() {
        return (bits & ST_V + ST_C + ST_Z) != 0;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isL()
	 */
    public boolean isL() {
        return (bits & ST_C) != 0;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isEQ()
	 */
    public boolean isEQ() {
        return (bits & ST_Z) != 0;        
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isNE()
	 */
    public boolean isNE() {
        return (bits & ST_Z) == 0;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isHE()
	 */
    public boolean isHE() {
        return (bits & ST_C) == 0 || (bits & ST_Z) != 0;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isGT()
	 */
    public boolean isGT() {
        return isGE() && isNE();
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isH()
	 */
    public boolean isH() {
        return  (bits & ST_V + ST_C + ST_Z) == 0;
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
        return (bits & ST_GIE) >> 3;
    }

	/**
	 * @return
	 */
	public boolean isN() {
		return (bits & ST_N) != 0;
	}

	/**
	 * @return
	 */
	public boolean isGE() {
		return ((bits & ST_N) != 0) != ((bits & ST_V) != 0);
	}

    
}