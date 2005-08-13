/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9;

import java.util.Iterator;

/**
 * @author ejs
 */
public class MemoryDomain {
    /*
     * This must remain 64K, even if mega-memory expansion is emulated. All the
     * public routines expect to be passed 16-bit addresses.
     */
    public static final int PHYSMEMORYSIZE = 65536;

    static final int NUMAREAS = (PHYSMEMORYSIZE >> MemoryArea.AREASHIFT);

    private MemoryArea areahandlers[] = new MemoryArea[NUMAREAS];
    
    public MemoryDomain() {
        MemoryArea area = new ZeroMemoryArea();
        setArea(0, PHYSMEMORYSIZE, area);        
    }
    
    /** For testing, create a RAM-accessible memory domain which spans
     * the size of data.
     * @param data populating data, length on AREASIZE boundary 
     * @return
     */
    public static MemoryDomain newFromArray(byte[] data, boolean bWordAccess) {
        MemoryDomain domain = new MemoryDomain();
        MemoryArea area = MemoryArea.newDefaultArea();
        area.bWordAccess = bWordAccess;
        area.memory = data;
        area.read = data;
        area.write = data;
        domain.setArea(0, data.length, area);
        return domain;
    }    
    
    public final MemoryArea getArea(int addr) {
        return areahandlers[(addr & (PHYSMEMORYSIZE - 1)) >> MemoryArea.AREASHIFT];
    }

    void setArea(int addr, int size, MemoryArea handler) {
        MemoryArea tmp = (MemoryArea) handler.clone();

        if ((size < MemoryArea.AREASIZE)
                || (addr & (MemoryArea.AREASIZE - 1)) != 0)
            throw new AssertionError(
                    "attempt made to set a memory handler on an illegal boundary\n"
                            + "(" + Integer.toHexString(addr) + "..."
                            + Integer.toHexString(addr + size - 1)
                            + "), the minimum granularity is "
                            + Integer.toHexString(MemoryArea.AREASIZE)
                            + " bytes");

        if (handler.read == null && handler.areaReadByte == null
                && handler.areaReadWord != null)
            throw new AssertionError(
                    "cannot have a handler define read_word without read_byte");
        if (handler.write == null && handler.areaWriteByte == null
                && handler.areaWriteWord != null)
            throw new AssertionError(
                    "cannot have a handler define write_word without write_byte");

        if (size > PHYSMEMORYSIZE || addr >= PHYSMEMORYSIZE
                || addr + size > PHYSMEMORYSIZE)
            throw new AssertionError("illegal address or size (64k limit)");

        if (handler.memory != null && handler.offset + size > handler.memory.length)
            throw new AssertionError(
                    "memory is not big enough for area handlers from "
                            + Integer.toHexString(handler.offset) + " ("
                            + Integer.toHexString(handler.memory.length)
                            + ") for " + Integer.toHexString(size) + " bytes");

        //System.out.println("setting addr="+addr+",size="+size);
        size = (size + MemoryArea.AREASIZE - 1) >> MemoryArea.AREASHIFT;
        addr >>= MemoryArea.AREASHIFT;
        //System.out.println("====== addr="+addr+",size="+size+" of "+areahandlers.length);
        while (size != 0) {
            areahandlers[addr++] = tmp;

            /* advance memory pointer(s) */
            if (size-- != 0) {
                tmp = (MemoryArea) tmp.clone();
                tmp.offset += MemoryArea.AREASIZE;
            }
        }
        //System.out.println("area "+areahandlers+":");
        //for (size=0; size<areahandlers.length; size++)
        //    System.out.print(areahandlers[size]+",");
        //System.out.println();
    }

    public final short flatReadWord(int addr) {
        MemoryArea area = getArea(addr);
        //if (area != null)
            return area.flatReadWord(addr);
        //else
        //    return 0;
    }

    public final short flatReadByte(int addr) {
        MemoryArea area = getArea(addr);
        //if (area != null)
            return area.flatReadByte(addr);
        //else
        //    return 0;
    }

    public final void flatWriteByte(int addr, byte val) {
        MemoryArea area = getArea(addr);
        //if (area != null)
            area.flatWriteByte(addr, val);
    }

    public final short readWord(int addr) {
        MemoryArea area = getArea(addr);
        //if (area == null)
        //    return 0;
        //else
            return area.readWord(addr);
    }

    public final byte readByte(int addr) {
        MemoryArea area = getArea(addr);
        //if (area == null) {
        //    System.out.println("null area for "+Globals.toHex4(addr));
        //    return 0;
       // }
        //else
            return area.readByte(addr);
    }

    public final void writeWord(int addr, short val) {
        MemoryArea area = getArea(addr);
        //if (area != null)
            area.writeWord(addr, val);
    }

    public final void writeByte(int addr, byte val) {
        MemoryArea area = getArea(addr);
        //if (area != null)
            area.writeByte(addr, val);
    }

    public final boolean hasRamAccess(int addr) {
        MemoryArea area = getArea(addr);
        return area != null && area.hasRamAccess();
    }

    public final boolean hasRomAccess(int addr) {
        MemoryArea area = getArea(addr);
        return area != null && area.hasRomAccess();
    }

    /** Iterate all the areas in the domain. */
    public class AreaIterator implements Iterator {

        MemoryArea area;
        
        int areaIdx;

        int lastArea;

        private boolean bFresh;

        /** Iterate over a specified memory range */
        AreaIterator(int startaddr, int size) {
            if (startaddr < 0)
                throw new IndexOutOfBoundsException();
            area = null;
            areaIdx = startaddr >> MemoryArea.AREASHIFT;
            lastArea = (startaddr + size + MemoryArea.AREASIZE - 1) >> MemoryArea.AREASHIFT;
            if (lastArea < 0 || lastArea > NUMAREAS)
                throw new IndexOutOfBoundsException();
        }

        /** Iterate all the memory in the domain */
        AreaIterator() {
            this(0, PHYSMEMORYSIZE);
        }

        private void getNext() {
            while (areaIdx < lastArea) {
                area = areahandlers[areaIdx++];
                if (area != null)
                    break;
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if (area == null)
                getNext();
            return area != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if (area == null)
                getNext();
            if (area == null)
                throw new java.util.NoSuchElementException();
            MemoryArea ret = area;
            area = null;
            return ret;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** Clear out the memory areas, making them inaccessible.
     *	 
     */
    /*public void clear() {
        for (int i = 0; i < areahandlers.length; i++) {
            areahandlers[i] = null;
        }
    }*/

    /** Zero out the memory areas, setting them to zeroed-out ROM.
     *	 
     */
    public void zero() {
        for (int i = 0; i < areahandlers.length; i++) {
            areahandlers[i] = new ZeroMemoryArea();
        }
    }
}