/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9;

/** GPL chip entry
 * @author ejs
 */
public class Gpl implements v9t9.Memory.ConsoleMmioReader, v9t9.Memory.ConsoleMmioWriter {

    private Machine machine;
    private MemoryDomain memory;
    
    short gromaddr;
    boolean gromaddrflag;

    /**
     * @param machine
     */
    public Gpl(Machine machine) {
        this.machine = machine;
    }

    /*	GROM has a strange banking scheme where the upper portion
	of the address does not change when incremented;
	this acts like an 8K bank. */
    private short getNextAddr(short addr) {
        return (short) (((addr+1) & 0x1fff) | (gromaddr & 0xe000));
    }
    
    public short getAddr() {
        return gromaddr;
    }
    
    public byte getAddrByte() {
        return (byte) (getNextAddr(gromaddr) >> 8);        
    }
    
    public void setAddr(short addr) {
        gromaddr = addr;
    }

    public boolean addrIsComplete() {
        return !gromaddrflag;
    }
    
    /**
     * @see v9t9.Memory.ConsoleMmioReader#read
     */
    public byte read(int addr) {
    	byte ret;
    	short temp;
    	
    	if (addr != 0) {
    	    /* >9802, address read */
    	    temp = getNextAddr(gromaddr);
    	    ret = getAddrByte();
    	    gromaddr = (short) (temp << 8);
    	    gromaddrflag = !gromaddrflag;
    	} else {
    	    /* >9800, memory read */
    	    gromaddrflag = false;
        	if (memory == null) 
                memory = machine.getMemory().GRAPHICS;
    	    ret = memory.readByte(gromaddr);
    	    gromaddr = getNextAddr(gromaddr);
    	}
    	return ret;
    }

    /**
     * @see v9t9.Memory.ConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	if (addr != 0) {				
    	    /* >9C02, address write */
    	    
    		gromaddr = (short) ((gromaddr << 8) | (val & 0xff));
    		gromaddrflag = !gromaddrflag;
    	} else {					
    	    /* >9C00, data write */
    		gromaddrflag = false;
    		
        	if (memory == null) 
                memory = machine.getMemory().GRAPHICS;

    		memory.writeByte(gromaddr, val);
    		gromaddr = getNextAddr(gromaddr);
    	}    
    }
}
