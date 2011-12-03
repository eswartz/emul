/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 18, 2004
 *
 */
package v9t9.engine.memory;


import v9t9.base.properties.IPersistable;
import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.ISettingSection;
import v9t9.base.settings.Logging;
import v9t9.base.utils.HexUtils;
import v9t9.common.cpu.ICpu;
import v9t9.common.memory.MemoryDomain;
import v9t9.engine.cpu.Executor;

/** GPL chip entry
 * @author ejs
 */
public class GplMmio implements IConsoleMmioReader, IConsoleMmioWriter, IPersistable {
	static public final SettingProperty settingDumpGplAccess = new SettingProperty("DumpGplAccess", new Boolean(false));
    
    private MemoryDomain domain;
    
    short gromaddr;
    boolean gromwaddrflag, gromraddrflag;
    byte buf;

    /**
     * @param machine
     */
    public GplMmio(MemoryDomain domain) {
        if (domain == null) {
			throw new IllegalArgumentException();
		}
        this.domain = domain;
        
		// interleave with CPU log
		Logging.registerLog(settingDumpGplAccess, "instrs_full.txt");

     }

    /*	GROM has a strange banking scheme where the upper portion
	of the address does not change when incremented;
	this acts like an 8K bank. */
    private short getNextAddr(short addr) {
        return (short) (addr+1 & 0x1fff | gromaddr & 0xe000);
    }
    
    public int getAddr() {
        return gromaddr;
    }
    
    public byte getAddrByte() {
        return (byte) (getNextAddr(gromaddr) >> 8);        
    }
    
    public void setAddr(short addr) {
        gromaddr = addr;
    }

    public boolean addrIsComplete() {
        return !gromwaddrflag;
    }
    
    /**
     * @see v9t9.common.memory.Memory.IConsoleMmioReader#read
     */
    public byte read(int addr) {
    	byte ret;
    	
    	if ((addr & 2) != 0) {
    	    /* >9802, address read */
    	    //temp = getNextAddr(gromaddr);
    	    //ret = getAddrByte();
    	    //gromaddr = (short) (temp << 8);
    		gromwaddrflag = false;
    		if (gromraddrflag)
    			ret = (byte) (gromaddr & 0xff);
    		else
    			ret = (byte) (gromaddr >> 8);
    	    gromraddrflag = !gromraddrflag;
    	} else {
    	    /* >9800, memory read */
    	    //gromaddrflag = false;
    		if (ICpu.settingDumpFullInstructions.getBoolean())
    			Executor.getDumpfull().println("Read GPL >" + HexUtils.toHex4(gromaddr - 1) + " = >" + HexUtils.toHex2(buf));

    	    ret = readGrom();
    	}
    	return ret;
    }

    /**
	 * @return
	 */
	private byte readGrom() {
		byte ret = buf;
	    buf = domain.readByte(gromaddr);
	    gromaddr = getNextAddr(gromaddr);
		return ret;
	}

	/**
     * @see v9t9.common.memory.Memory.IConsoleMmioWriter#write 
     */
    public void write(int addr, byte val) {
    	if ((addr & 2) != 0) {				
    	    /* >9C02, address write */
    	    
    		gromraddrflag = false;
    		if (gromwaddrflag) {
    			gromaddr = (short) (gromaddr & 0xff00 | val & 0xff);
    			readGrom();
    		}
    		else
    			gromaddr = (short) (((val & 0xff) << 8) | gromaddr & 0xff);
    		gromwaddrflag = !gromwaddrflag;
    	} else {					
    	    /* >9C00, data write */
    		gromraddrflag = gromwaddrflag = false;

    		domain.writeByte(gromaddr - 1, val);
    		gromaddr = getNextAddr(gromaddr);
    	}    
    }

	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		gromaddr = (short) section.getInt("Addr");
		gromraddrflag = section.getBoolean("ReadAddrFlag");
		gromwaddrflag = section.getBoolean("WriteAddrFlag");
	}

	public void saveState(ISettingSection section) {
		section.put("Addr", gromaddr);
		section.put("ReadAddrFlag", gromraddrflag);
		section.put("WriteAddrFlag", gromwaddrflag);
	}
}
