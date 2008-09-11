/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 24, 2004
 *
 */
package v9t9.emulator.clients.builtin;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import v9t9.emulator.Machine;
import v9t9.emulator.handlers.CruHandler;
import v9t9.utils.Utils;

/**
 * @author ejs
 */
public class InternalCru implements CruHandler {
    Machine machine;
    
    public interface CruReader {
        public int read(int addr, int data, int num);
    }

    public interface CruWriter {
        public int write(int addr, int data, int num);
    }

    public InternalCru(Machine machine) {
        this.machine = machine;
        readers = new TreeMap<Integer, CruReader>();
        writers = new TreeMap<Integer, CruWriter>();
    }

    private Map<Integer, CruReader> readers;
	private Map<Integer, CruWriter> writers;
    
    /*
     *  base is a base ADDRESS
     * 	range is in BITS, not address units:  base ... base + range*2
     */
    public void add(int rw, int base, int range, CruReader access) {
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

    public void add(int rw, int base, int range, CruWriter access) {
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

    public void remove(int rw, int base, int range, CruReader access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        readers.remove(new Integer(base));
    }

    public void remove(int rw, int base, int range, CruWriter access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        if (range != 1) {
            throw new AssertionError("only single-bit ranges allowed, got " + Integer.toHexString(range));
        }
        writers.remove(new Integer(base));
    }

    /**
     * @param addr
     * @param value
     * @param bits
     */
    public void writeBits(int addr, int val, int num) {
        addr &= 0x1fff;

    	//logger(_L | L_2, _("CRU write: >%04X[%d], %04X\n"), addr, num, val & 0xffff);
        System.out.println("CRU write: >" + Utils.toHex4(addr) + "[" + num + "], " + Utils.toHex4(val));
    	/* on 99/4A console, 0x0000 through 0x0040 map to 0x40*k through 0x1000 */

    	if (addr < 0x1000) {
    		addr &= 0x3f;
    	}

    	if (addr >= 0x30) {
    	    // TODO
    	//	setclockmode9901(0);
    	}

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
                ((CruWriter)writers.get(base)).write(addr, val & mask, used);

                num -= used;
                addr += used * 2;
                val >>= used;
            }
        }
    }

    /**
     * @param s
     * @param value
     * @return
     */
    public int readBits(int addr, int num) {
        int orgaddr;
        int val = 0;
        
        addr &= 0x1fff;
        orgaddr = addr;
        
        //logger(_L | L_2, _("CRU read: >%04X[%d] = \n"), addr, num);
        System.out.print("CRU read: >" + Utils.toHex4(addr) + "[" + num + "] = ");
        
    	if (addr >= 0x30) {
    	    // TODO
    		//setclockmode9901(0);
    	}
        
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
                bits = ((CruReader)readers.get(base)).read(addr, val, used) & mask;
                val = val & ~(mask << shift) | bits << shift;
                num -= used;
                addr += used * 2;
            }
        }

        System.out.println(Utils.toHex4(val));

        return val;
    }
    
    
}
