/*
  StatusF99b.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import v9t9.common.cpu.IStatus;


/**
 * The CPU status word is a flattened array of bits, many of which are hard to
 * calculate. We implement it instead as a class which contains the values
 * for which the flags are set. Then, we can save time by not forming the
 * word until it's needed.
 * 
 * @author ejs
 */
public class StatusF99b implements IStatus {
    private static final short ST_INT = 0xF;
	private static final short ST_CUR = 0xF0;
	short bits; 

    public StatusF99b() {
    }

    public StatusF99b(short val) {
        expand(val);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#toString()
	 */
    @Override
    public String toString() {
    	return "I:" + (bits & StatusF99b.ST_INT) +" C:" + ((bits & StatusF99b.ST_CUR) >> 4);
    }
    /* (non-Javadoc)
	 * @see v9t9.engine.cpu.Status#copyTo(v9t9.engine.cpu.Status9900)
	 */
    public void copyTo(IStatus copy_) {
    	StatusF99b copy = (StatusF99b) copy_;
        copy.bits = bits;
    }

    

    @Override
	protected Object clone()  {
        return new StatusF99b(bits);
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

    public void setIntMask(int mask) {
        bits = (short) (bits & ~ST_INT | mask & ST_INT);
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
        return (bits & ST_INT);
    }

	/**
	 * @param intr
	 */
	public void setCurrentInt(int intr) {
		bits = (short) ((bits & ~ST_CUR) | ((intr & 0xF) << 4));
	}
	
	public int getCurrentInt() {
		return (bits & ST_CUR) >> 4;
	}


    
}