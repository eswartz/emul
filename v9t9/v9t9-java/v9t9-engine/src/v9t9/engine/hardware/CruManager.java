/*
  CruManager.java

  (c) 2005-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.hardware;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * This class handles a set of CRU handlers
 * @author ejs
 */
public class CruManager implements ICruHandler {
    
    public CruManager() {
        readers = new TreeMap<Integer, ICruReader>();
        writers = new TreeMap<Integer, ICruWriter>();
    }

    private Map<Integer, ICruReader> readers;
	private Map<Integer, ICruWriter> writers;
    
	private ICruWriter[] writerArray;
	private ICruReader[] readerArray;
	
    /*
     *  base is a base ADDRESS
     * 	range is in BITS, not address units:  base ... base + range*2
     */
    public void add(int base, int range, ICruReader access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        for (int x = 0; x < range; x++) {
	        Integer baseObj = base + x * 2;
	        if (readers.get(baseObj) != null) {
	            throw new AssertionError("overlapping I/O at "+Integer.toHexString(base));
	        }
	        readers.put(baseObj, access);
        }
        ensureReaderArray();
    }

    public void add(int base, int range, ICruWriter access) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        for (int x = 0; x < range; x++) {
	        Integer baseObj = base + x * 2;
	        
	        ICruWriter exist = writers.get(baseObj);
	        if (exist != null) {
	        	if (exist instanceof MultiCruWriter) {
	        		((MultiCruWriter) exist).addWriter(access);
	        	} else {
	        		MultiCruWriter mw = new MultiCruWriter();
	        		mw.addWriter(exist);
	        		mw.addWriter(access);
	        		writers.put(baseObj, mw);
	        	}
	        } else {
	        	writers.put(baseObj, access);
	        }
        }
        ensureWriterArray();
    }

    public void removeReader(int base, int range) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        for (int x = 0; x < range; x++) {
        	readers.remove(base + x * 2);
        }
        ensureReaderArray();
    }

    public void removeWriter(int base, int range) {
        if (base >= 0x2000) {
            throw new AssertionError("invalid CRU address "+Integer.toHexString(base));
        }
        for (int x = 0; x < range; x++) {
        	ICruWriter access = writers.remove(base + x * 2);
        	if (access instanceof MultiCruWriter) {
        		if (((MultiCruWriter) access).removeLast()) {
        			writers.put(base + x * 2, access);
        		}
        	}
        }
        ensureWriterArray();
    }

    /**
     * @param addr CRU address line (multiplied by 2)
     * @param value
     * @param bits
     */
    public final void writeBits(int addr, int val, int num) {
    	addr &= 0x1fff;
        while (num > 0) {
        	if (addr >= 0x2000)
        		return;
        	ICruWriter writer = writerArray[addr / 2];
        	if (writer != null) {
        		try {
        			writer.write(addr, val & 1, 1);
        		} catch (Throwable t) {
        			t.printStackTrace();
        		}
        	}
        	num -= 1;
        	addr += 2;
        	val >>= 1;
        }
    }

	private void ensureWriterArray() {
		if (writerArray == null)
			writerArray = new ICruWriter[0x1000];
		else
			Arrays.fill(writerArray, null);
		for (Map.Entry<Integer, ICruWriter> entry : writers.entrySet()) {
			writerArray[entry.getKey() / 2] = entry.getValue();
		}
	}
    
    /**
     * @param address  CRU address line (multiplied by 2)
     * @param value
     * @return
     */
    public final int readBits(int addr, int num) {
    	addr &= 0x1fff;
    	int val = 0;
    	int shift = 0;
    	while (num > 0) {
    		if (addr >= 0x2000)
    			break;
         	ICruReader reader = readerArray[addr / 2];
            int bit = 0;
            if (reader != null) {
            	try {
            		bit = reader.read(addr, val, 1) & 1;
            	} catch (Throwable t) {
        			t.printStackTrace();
        		}
            }
            val |= bit << shift;
            addr += 2;
            shift++;
            num --;
    	}
        return val;
    }

	private void ensureReaderArray() {
		if (readerArray == null)
			readerArray = new ICruReader[0x1000];
		else
			Arrays.fill(readerArray, null);
		for (Map.Entry<Integer, ICruReader> entry : readers.entrySet()) {
			readerArray[entry.getKey() / 2] = entry.getValue();
		}
	}
    /**
     * @param addr CRU address line (multiplied by 2)
     * @param value
     * @param bits
     */
    public void writeBits_(int addr, int val, int num) {
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
    public int readBits_(int addr, int num) {
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

    /**
	 * @return the readers
	 */
	public Map<Integer, ICruReader> getReaders() {
		return readers;
	}
	/**
	 * @return the writers
	 */
	public Map<Integer, ICruWriter> getWriters() {
		return writers;
	}
    
}
