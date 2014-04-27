/**
 * 
 */
package v9t9.machine.ti99.tests.dsr;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.IRS232Handler.DataSize;
import v9t9.common.dsr.IRS232Handler.Parity;
import v9t9.common.dsr.IRS232Handler.Stop;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.dsr.rs232.RS232;
import v9t9.engine.hardware.CruManager;
import v9t9.machine.ti99.dsr.rs232.TIRS232Dsr;
import v9t9.machine.ti99.machine.StandardTI994AMachineModel;
import v9t9.machine.ti99.machine.TI99Machine;
/**
 * @author ejs
 *
 */
public class TestRS232Regs {
	protected ISettingsHandler settings = new BasicSettingsHandler();
	protected TI99Machine machine = (TI99Machine) new StandardTI994AMachineModel().createMachine(settings);
	protected TIRS232Dsr dsr;
	private CruManager cru;
	private RS232 rs232;
	
	@Before
	public void setupDSR() throws Exception {
		cru = machine.getCruManager();
		dsr = new TIRS232Dsr(machine, (short) 0x1300);
//		dsr.activate(machine.getConsole(), machine.getMemory().getMemoryEntryFactory());
		dsr.init();
		rs232 = dsr.getDevice(1).getRS232();
		assertNotNull(rs232);
	}
	
	@Test
	public void testReset() throws Exception {
		cru.writeBits(0x1340 + 31 * 2, 1, 1);
		assertEquals(Parity.NONE, rs232.getParity());
		assertEquals(Stop.STOP_1_5, rs232.getStopBits());
		assertEquals(DataSize.FIVE, rs232.getDataSize());
		assertEquals(0, rs232.getIntervalRate());
		assertEquals(0, rs232.getReceiveRate());
		assertEquals(0, rs232.getTransmitRate());
		assertTrue(rs232.getRecvBuffer().isEmpty());
		assertTrue(rs232.getXmitBuffer().isEmpty());
	}

	@Test
	public void testSetupInvl() throws Exception {
		cru.writeBits(0x1340 + 31 * 2, 1, 1);
		cru.writeBits(0x1340 + 13*2, 1, 1);	// invl
		cru.writeBits(0x1340, 0, 11);	
		
		assertEquals(0, rs232.getIntervalRate());
		
		cru.writeBits(0x1340 + 14*2, 1, 1);	// ctrl
		cru.writeBits(0x1340, 0x0, 8);	// !CLK4M

		cru.writeBits(0x1340 + 13*2, 1, 1);	// invl
		cru.writeBits(0x1340, 1, 11);	
		assertEquals(15625, rs232.getIntervalRate());
		
		cru.writeBits(0x1340 + 13*2, 1, 1);	// invl
		cru.writeBits(0x1340, 0xff, 8);	
		assertEquals(61, rs232.getIntervalRate());
		
		

		cru.writeBits(0x1340 + 14*2, 1, 1);	// ctrl
		cru.writeBits(0x1340, 0x8, 8);	// CLK4M
		

		cru.writeBits(0x1340 + 13*2, 1, 1);	// invl
		cru.writeBits(0x1340, 1, 11);	
		assertEquals(11718, rs232.getIntervalRate());
		
		cru.writeBits(0x1340 + 13*2, 1, 1);	// invl
		cru.writeBits(0x1340, 0xff, 8);	
		assertEquals(45, rs232.getIntervalRate());

	}
	
	@Test
	public void testSetupThierry() throws Exception {
		cru.writeBits(0x1340 + 14*2, 1, 1);	// ctrl
		
		cru.writeBits(0x1340, 0xbb, 8);	// 1200 odd 8 st
		assertEquals(Parity.ODD, rs232.getParity());
		assertEquals(Stop.STOP_1, rs232.getStopBits());
		assertEquals(DataSize.EIGHT, rs232.getDataSize());
		
		assertEquals(0, rs232.getIntervalRate());
		
		cru.writeBits(0x1340 + 12*2, 1, 1);	// rate ctr;
		
		cru.writeBits(0x1340, 0x9c, 11);	// recv
		assertEquals(2403, rs232.getReceiveRate());
		assertEquals(0, rs232.getTransmitRate());
		
		cru.writeBits(0x1340, 0x9c, 11);	// xmit
		assertEquals(2403, rs232.getReceiveRate());
		assertEquals(2403, rs232.getTransmitRate());
	}
	
	@Test
	public void testSetupDSR() throws Exception {
		cru.writeBits(0x1340 + 31*2, 1, 1);	// reset
		
		cru.writeBits(0x1340, 0xba, 8);	// odd 7 st
		assertEquals(Parity.ODD, rs232.getParity());
		assertEquals(Stop.STOP_1, rs232.getStopBits());
		assertEquals(DataSize.SEVEN, rs232.getDataSize());
		
		assertEquals(0, rs232.getIntervalRate());
		
		cru.writeBits(0x1340 + 13*2, 0, 1);	// invl ctr = 0
		assertEquals(0, rs232.getIntervalRate());
		
		
		cru.writeBits(0x1340, 0x271, 12);	// recv + xmit
		assertEquals(600, rs232.getReceiveRate());
		assertEquals(600, rs232.getTransmitRate());
	}
}
