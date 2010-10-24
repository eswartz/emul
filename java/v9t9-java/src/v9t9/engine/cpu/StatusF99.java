/*
 * (c) Ed Swartz, 2010
 *
 */
package v9t9.engine.cpu;


/**
 * The CPU status word is a flattened array of bits, many of which are hard to
 * calculate. We implement it instead as a class which contains the values
 * for which the flags are set. Then, we can save time by not forming the
 * word until it's needed.
 * 
 * @author ejs
 */
public class StatusF99 implements Status {
    private static final short ST_INT = 1;
	short bits; 

    public StatusF99() {
    }

    public StatusF99(short val) {
        expand(val);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#toString()
	 */
    @Override
    public String toString() {
    	return  ((bits & StatusF99.ST_INT) != 0 ? "I" : " ");
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#copyTo(v9t9.engine.cpu.Status9900)
	 */
    public void copyTo(Status copy_) {
    	StatusF99 copy = (StatusF99) copy_;
        copy.bits = bits;
    }

    

    @Override
	protected Object clone()  {
        return new StatusF99(bits);
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

    /*
     * for LIMI
     */
    public void setIntMask(int mask) {
        bits = (short) (bits & ~0x1 | mask & 0x1);
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isLT()
	 */
    public boolean isLT() {
        return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isLE()
	 */
    public boolean isLE() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isL()
	 */
    public boolean isL() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isEQ()
	 */
    public boolean isEQ() {
    	return false;       
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isNE()
	 */
    public boolean isNE() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isHE()
	 */
    public boolean isHE() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isGT()
	 */
    public boolean isGT() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isH()
	 */
    public boolean isH() {
    	return false;
    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#isC()
	 */
    public boolean isC() {
    	return false;
    }
    
    /**
     * @return
     */
    public int getIntMask() {
        return (bits & 1);
    }


    
}