/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.emulator.runtime;

import v9t9.emulator.Machine.ConsoleMmioReader;
import v9t9.emulator.Machine.ConsoleMmioWriter;
import v9t9.engine.Client;
import v9t9.engine.memory.MemoryDomain;


/** VDP chip entry
 * @author ejs
 */
public class Vdp implements ConsoleMmioReader, ConsoleMmioWriter {

    private MemoryDomain memory;
    
    short vdpaddr;
    boolean vdpaddrflag;
    byte vdpreadahead;
    byte vdpstatus;
    // TODO: read vdpstatus regularly
    
    byte vdpregs[];

    private Client client;

    /**
     * @param machine
     */
    public Vdp(MemoryDomain memory) {
        if (memory == null) {
			throw new IllegalArgumentException();
		}
        this.memory = memory;
        vdpregs = new byte[16];
    }

    /**
     * @see v9t9.engine.memory.Memory.ConsoleMmioReader#read
     */
    public byte read(int addr) {
        	byte ret;
    
        	vdpaddrflag = false;
        	if ((addr & 2) != 0) {
        	    /* >8802, status read */
        		ret = vdpstatus;
        		vdpstatus &= ~0xe0;		// thierry:  reset bits when read
        		
        		// TODO machine.getCpu().reset9901int(v9t9.cpu.Cpu.M_INT_VDP);
        	} else {					/* >8800, memory read */
        		ret = vdpreadahead;
        		vdpreadahead = getMemory().readByte(vdpaddr);
        		vdpaddr = (short) (vdpaddr + 1 & 0x3fff);
        	}
        	return ret;
    }

    /**
     * @see v9t9.engine.memory.Memory.ConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
        	if (addr != 0) {				
        	    /* >8C02, address write */
        	    
        		vdpaddr = (short) (vdpaddr >> 8 & 0xff | val << 8);
        		if ((vdpaddrflag = !vdpaddrflag) == false) {
        			if ((vdpaddr & 0x8000) != 0) {
        				writeRegAddr(vdpaddr);
        				vdpaddr &= 0x3fff;
        			} else if ((vdpaddr & 0x4000) != 0) {
        				vdpaddr &= 0x3fff;
        			} else {
        				// read ahead one byte
        				vdpreadahead = getMemory().readByte(vdpaddr);
        				vdpaddr = (short) (vdpaddr+1 & 0x3fff);
        			}
        		}
        	} else {					
        	    /* >8C00, data write */
        	    
        		/* this flag is used to verify that the VDP
        		   address was written as >4000 + vdpaddr.
        		   If not, then writing to it functions as
        		   a read-before-write. */
        		vdpaddrflag = false;
    
    		    //System.out.println("vdp @" + Integer.toHexString(vdpaddr) + " = " + Integer.toHexString(val&0xff));
    
        		byte oldVal = getMemory().readByte(vdpaddr);
        		getMemory().writeByte(vdpaddr, val);
        		if (oldVal != val) {
        			if (client != null)
        				client.getVideoHandler().writeVdpMemory(vdpaddr, val);
        		}
    
        		vdpaddr = (short) (vdpaddr + 1 & 0x3fff);
        		vdpreadahead = val;
        	}    
        
    }
    
    public void setAddr(short addr) {
        if ((addr & 0x8000) != 0) {
			writeRegAddr(addr);
		}
        vdpaddr = (short) (addr & 0x3fff);
        vdpaddrflag = false;
    }
    
    public short getAddr() {
        return vdpaddr;
    }
    
    /** Update VDP register 
     * 
     *	@param addr in the form 0x8<reg.4><val.8>
     */
    public void writeRegAddr(short addr) {
        byte reg = (byte) (addr >> 8 & 0xf);
        byte val = (byte) (addr & 0xff);
        byte old = vdpregs[reg];
        vdpregs[reg] = val;
        if (old != val)
        	if (client != null)
        		client.getVideoHandler().writeVdpReg(reg, val);
    }

	public MemoryDomain getMemory() {
		return memory;
	}

	public void setClient(Client client) {
	    this.client = client;
	}

}
