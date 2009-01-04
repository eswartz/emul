/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9.emulator.hardware;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import v9t9.engine.CruHandler;
import v9t9.utils.Utils;

/**
 * This class handles a set of CRU handlers
 * @author ejs
 */
public class CruManager implements CruHandler {
    
    public CruManager() {
        readers = new TreeMap<Integer, CruReader>();
        writers = new TreeMap<Integer, CruWriter>();
    }

    private Map<Integer, CruReader> readers;
	private Map<Integer, CruWriter> writers;
    
    /*
     *  base is a base ADDRESS
     * 	range is in BITS, not address units:  base ... base + range*2
     */
    public void add(int base, int range, CruReader access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        Integer baseObj = new Integer(base);
        if (readers.get(baseObj) != null) {
            throw new AssertionError("overlapping I/O at "+Integer.toHexString(base));
        }
        readers.put(baseObj, access);
    }

    public void add(int base, int range, CruWriter access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        Integer baseObj = new Integer(base);
        if (writers.get(baseObj) != null) {
            throw new AssertionError("overlapping I/O at "+Integer.toHexString(base));
        }
        writers.put(baseObj, access);        
    }

    public void remove(int base, int range, CruReader access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        readers.remove(new Integer(base));
    }

    public void remove(int base, int range, CruWriter access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        writers.remove(new Integer(base));
    }

    /**
     * @param addr CRU address line (multiplied by 2)
     * @param value
     * @param bits
     */
    public void writeBits(int addr, int val, int num) {
    	//System.out.println(Utils.toHex4(addr) + " @" + num + " = " + val);
        Iterator<Integer> iter = writers.keySet().iterator();
        while (iter.hasNext() && num > 0) {
            Integer base = iter.next();
            if (base.intValue() > addr) {
    			/* if we've already passed a handler for addr,
    			 shift out the lost bits */
                int lost = (base.intValue() - addr) / 2;

                if (lost > num) {
					lost = num;
				}

                val >>= lost;
                num -= lost;
                addr = base.intValue();
                //logger(_L | L_2, _("cruwrite:  skipping bits, range is now %04X[%d]\n"), addr,
                //       num);
            }

            if (addr == base.intValue() && num != 0) {
                int used;
                int mask;

                //	logger(_L | L_2, _("cruwrite:  handling %04X[%d] with %04X\n"),
                //	 addr, num, ptr->addr);

                used = 1;
                mask = ~(~0 << used);
                writers.get(base).write(addr, val & mask, used);

                num -= used;
                addr += used * 2;
                val >>= used;
            }
        }
    }

    /**
     * @param address  CRU address line (multiplied by 2)
     * @param value
     * @return
     */
    public int readBits(int addr, int num) {
        int orgaddr;
        int val = 0;
        
        orgaddr = addr;
        
        Iterator<Integer> iter = readers.keySet().iterator();
        while (iter.hasNext() && num > 0) {
            Integer base = iter.next();
            if (base.intValue() > addr) {
    			/* if we've already passed a handler for addr,
    			 shift out the lost bits */
                int lost = (base.intValue() - addr) / 2;

                if (lost > num) {
					lost = num;
				}

                val >>= lost;
                num -= lost;
                addr = base.intValue();
                //logger(_L | L_2, _("cruread:  skipping bits, range is now %04X[%d]\n"), addr,
                //       num);
            }

            if (addr == base.intValue() && num != 0) {
                int used;
                int mask, bits, shift;

    			//logger(_L | L_2, _("cruread:  handling %04X[%d] with %04X\n"),
    			//		 addr, num, ptr->addr);

                used = 1;
                mask = ~(~0 << used);
                shift = (addr - orgaddr) / 2;
                bits = readers.get(base).read(addr, val, used) & mask;
                val = (val & ~(mask << shift)) | (bits << shift);
                num -= used;
                addr += used * 2;
            }
        }

        return val;
    }
    
    
}
