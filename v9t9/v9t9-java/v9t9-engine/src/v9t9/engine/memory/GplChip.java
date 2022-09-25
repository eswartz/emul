/**
 * 
 */
package v9t9.engine.memory;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import v9t9.common.cpu.ICpu;
import v9t9.common.hardware.IGplChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class GplChip implements IGplChip {
	private static final String ID_ADDR = "ADDR";
	private static final String ID_BUF = "BUF";
	private static final String ID_RADDR = "RADDR";
	private static final String ID_WADDR = "WADDR";

	private static final int REG_ADDR = 0;
	private static final int REG_BUF= 1;
	private static final int REG_RADDR = 2;
	private static final int REG_WADDR = 3;
	
	private ListenerList<IRegisterWriteListener> listeners = new ListenerList<IRegisterAccess.IRegisterWriteListener>();
	private IMachine machine;
	private IMemoryDomain domain;
	
	private IProperty dumpGplAccess;
	private IProperty dumpFullInstructions;

    private short gromaddr;
    private boolean gromwaddrflag, gromraddrflag;
    private byte buf;
    
	private RegisterInfo gromAddrReg;
	private RegisterInfo gromBufReg;
	private RegisterInfo gromRaddrFlagReg;
	private RegisterInfo gromWaddrFlagReg;
	
	public GplChip(IMachine machine, IMemoryDomain domain) {
		this.machine = machine;
		this.domain = domain;
		
        dumpFullInstructions = Settings.get(machine, ICpu.settingDumpFullInstructions);
        dumpGplAccess = Settings.get(machine, settingDumpGplAccess);
        
		gromAddrReg = new RegisterInfo(ID_ADDR, FLAG_ROLE_GENERAL, 2, "GROM Address");
		gromBufReg = new RegisterInfo(ID_BUF, FLAG_ROLE_GENERAL, 1, "Buffered Read");
		gromRaddrFlagReg = new RegisterInfo(ID_RADDR, FLAG_ROLE_GENERAL, 1, "Read Address Flag");
		gromWaddrFlagReg = new RegisterInfo(ID_WADDR, FLAG_ROLE_GENERAL, 1, "Write Address Flag");
	}
	
	@Override
	public String getGroupName() {
		return "GPL Registers";
	}

	@Override
	public int getFirstRegister() {
		return 0;
	}

	@Override
	public int getRegisterCount() {
		return 4;
	}

	@Override
	public RegisterInfo getRegisterInfo(int reg) {
		switch (reg) { 
		case REG_ADDR: return gromAddrReg;
		case REG_BUF: return gromBufReg;
		case REG_RADDR: return gromRaddrFlagReg;
		case REG_WADDR: return gromWaddrFlagReg;
		default: return null;
		}
	}

	@Override
	public int getRegister(int reg) {
		switch (reg) { 
		case REG_ADDR: return gromaddr;
		case REG_BUF: return buf;
		case REG_RADDR: return gromraddrflag ? 1 : 0;
		case REG_WADDR: return gromwaddrflag ? 1 : 0;
		default: return 0xff;
		}
	}

	@Override
	public int setRegister(int reg, int newValue) {
		int old;
		switch (reg) {
		case REG_ADDR:
			old = gromaddr;
			newValue &= 0xffff;
			gromaddr = (short) newValue;
			break;
		case REG_BUF:
			old = buf;
			newValue = newValue & 0xff;
			buf = (byte) newValue;
			break;
		case REG_RADDR:
			old = gromraddrflag ? 1 : 0;
			newValue = newValue != 0 ? 1 : 0;
			gromraddrflag = newValue != 0;
			break;
		case REG_WADDR:
			old = gromwaddrflag ? 1 : 0;
			newValue = newValue != 0 ? 1 : 0;
			gromwaddrflag = newValue != 0;
			break;
		default:
			old = 0xff;
		}
		if (old != newValue) {
			fireRegisterChanged(reg, newValue);
		}
		return old;
	}

	@Override
	public String getRegisterTooltip(int reg) {
		return null;
	}

	@Override
	public int getRegisterNumber(String id) {
		if (ID_ADDR.equals(id)) {
			return REG_ADDR;
		}
		if (ID_BUF.equals(id)) {
			return REG_BUF;
		}
		if (ID_RADDR.equals(id)) {
			return REG_RADDR;
		}
		if (ID_WADDR.equals(id)) {
			return REG_WADDR;
		}
		if (ID_BUF.equals(id)) {
			return REG_BUF;
		}
		return -1;
	}

	@Override
	public void addWriteListener(IRegisterWriteListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeWriteListener(IRegisterWriteListener listener) {
		listeners.remove(listener);
	}
	
	protected final void fireRegisterChanged(int reg, int value) {
		if (!listeners.isEmpty()) {
			for (IRegisterWriteListener listener : listeners) {
				try {
					listener.registerChanged(reg, value);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public IMemoryDomain getGplMemory() {
		return domain;
	}

	@Override
	public IMachine getMachine() {
		return machine;
	}

	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		setAddr((short) section.getInt("Addr"));
		setRaddrFlag(section.getBoolean("ReadAddrFlag"));
		setWaddrFlag(section.getBoolean("WriteAddrFlag"));
		byte buf;
		if (section.getObject("Buf") != null) {
			buf = (byte) section.getInt("Buf");
		} else {
			buf = domain.readByte(((gromaddr - 1) & 0x1fff) | (gromaddr & 0xe000));
		}
		setBuf(buf);
	}

	@Override
	public void saveState(ISettingSection section) {
		section.put("Addr", gromaddr);
		section.put("Buf", buf);
		section.put("ReadAddrFlag", gromraddrflag);
		section.put("WriteAddrFlag", gromwaddrflag);
	}

	@Override
	public void reset() {
		setRegister(REG_ADDR, 0);
		setRegister(REG_BUF, 0);
		setRegister(REG_WADDR, 0);
		setRegister(REG_RADDR, 0);
	}

	@Override
	public short getAddr() {
		return gromaddr;
	}

	@Override
	public void setAddr(short addr) {
		// directly set here; we use this a lot
		if (gromaddr != addr) {
			gromaddr = addr;
			fireRegisterChanged(REG_ADDR, addr & 0xffff);
		}
	}

	@Override
	public boolean getWaddrFlag() {
		return gromwaddrflag;
	}

	@Override
	public void setWaddrFlag(boolean flag) {
		// directly set here; we use this a lot
		if (gromwaddrflag != flag) {
			gromwaddrflag = flag;
			fireRegisterChanged(REG_WADDR, flag ? 1 : 0);
		}
	}

	@Override
	public boolean getRaddrFlag() {
		return gromraddrflag;
	}

	@Override
	public void setRaddrFlag(boolean flag) {
		// directly set here; we use this a lot
		if (gromraddrflag != flag) {
			gromraddrflag = flag;
			fireRegisterChanged(REG_RADDR, flag ? 1 : 0);
		}
	}

	@Override
	public byte getBuf() {
		return buf;
	}

	@Override
	public void setBuf(byte buf) {
		if (this.buf != buf) {
			this.buf = buf;
			fireRegisterChanged(REG_BUF, buf);
		}
	}

    /*	GROM has a strange banking scheme where the upper portion
	of the address does not change when incremented;
	this acts like an 8K bank. */
    private short getNextAddr(short addr) {
        return (short) (addr+1 & 0x1fff | gromaddr & 0xe000);
    }
    
    public byte getAddrByte() {
        return (byte) (getNextAddr(gromaddr) >> 8);        
    }
    
    public boolean addrIsComplete() {
        return !gromwaddrflag;
    }
    
    @Override
	public byte readGrom() {
		byte ret = buf;
		setBuf(domain.readByte(gromaddr));
	    setAddr(getNextAddr(gromaddr));
		return ret;
	}

    @Override
	public void writeAddressByte(byte val) {
    	setRaddrFlag(false);
		if (gromwaddrflag) {
			gromaddr = (short) (gromaddr & 0xff00 | val & 0xff);
			// writing the grom address advances the address...
			readGrom();
		}
		else {
			gromaddr = (short) (((val & 0xff) << 8) | gromaddr & 0xff);
			fireRegisterChanged(REG_ADDR, gromaddr);
		}
    	setWaddrFlag(!gromwaddrflag);
	}
	
    @Override
	public void writeDataByte(byte val) {
    	setWaddrFlag(false);
    	setRaddrFlag(false);

		if (dumpGplAccess.getBoolean() && dumpFullInstructions.getBoolean()) {
			PrintWriter pw = Logging.getLog(dumpFullInstructions);
			if (pw != null)
				pw.println(
					"Write GPL >" + HexUtils.toHex4(gromaddr - 1) + " = >" + HexUtils.toHex2(val));
		}
		
		domain.writeByte(gromaddr - 1, val);
		gromaddr = getNextAddr(gromaddr);
	}   

	@Override
	public byte readAddressByte() {
		byte ret;
		setWaddrFlag(false);
		if (gromraddrflag)
			ret = (byte) (gromaddr & 0xff);
		else
			ret = (byte) (gromaddr >> 8);
		setRaddrFlag(!gromraddrflag);
	    return ret;
	}

	@Override
	public byte readDataByte() {
		if (dumpGplAccess.getBoolean() && dumpFullInstructions.getBoolean()) {
			PrintWriter pw = Logging.getLog(dumpFullInstructions);
			if (pw != null)
				pw.println(
					"Read GPL >" + HexUtils.toHex4(gromaddr - 1) + " = >" + HexUtils.toHex2(buf));
		}

	    return readGrom();
	}
}
