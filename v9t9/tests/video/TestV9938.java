/**
 * 
 */
package v9t9.tests.video;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import v9t9.emulator.clients.builtin.video.ImageDataCanvas24Bit;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.emulator.hardware.memory.EnhancedConsoleMemoryModel;
import v9t9.emulator.hardware.memory.mmio.Vdp9938Mmio;
import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.Memory;


/**
 * @author ejs
 *
 */
public class TestV9938 {

	private Vdp9938Mmio mmio;
	private VdpV9938 v9938;

	@Before
	public void setUp() throws Exception {
		EnhancedConsoleMemoryModel model = new EnhancedConsoleMemoryModel();
		Memory memory = model.createMemory();
		memory.addDomain(model.VIDEO);
		this.mmio = new Vdp9938Mmio(memory, model.VIDEO, 0x20000);
		v9938 = new VdpV9938(model.VIDEO, mmio, new ImageDataCanvas24Bit());
	}

	protected void vwaddr(int addr) {
		mmio.write(0x8c02, (byte) (addr & 0xff));
		mmio.write(0x8c02, (byte) ((addr >> 8) |0x40));
	}
	protected void vraddr(int addr) {
		mmio.write(0x8c02, (byte) (addr & 0xff));
		mmio.write(0x8c02, (byte) (addr >> 8));
	}
	protected byte vr() {
		return mmio.read(0x8800);
	}
	
	protected void vw(int i) {
		mmio.write(0x8c00, (byte) i);
	}
	
	protected void vwreg(int reg, int val) {
		mmio.write(0x8c02, (byte) val);
		mmio.write(0x8c02, (byte) (reg | 0x80));
	}
	
	protected byte frob(int i) {
		return (byte) ((byte) (i >> 12) ^ (i));
	}
	@Test
	public void testBasic() throws Exception {
		vwaddr(0x0);
		int sum = 0;
		for (int i = 0; i < 16383; i++) {
			sum = frob(i);
			vw(sum);
		}
		
		vraddr(0x0);
		for (int i = 0; i < 16383; i++) {
			sum = frob(i);
			byte ret = vr();
			assertEquals(""+i, (byte)sum, ret);
		}
	}
	
	@Test
	public void testBanks() throws Exception {
		// write low bank
		vwreg(14, 0);
		vwaddr(0x0);
		vw(0xff);
		
		// write bank 1
		vwreg(14, 0x4000 >> 14);
		vwaddr(0x0);
		vw(0xaa);
		
		// read bank 1
		vraddr(0x0);
		assertEquals("hi", (byte) 0xaa, vr());
		
		// read bank 0
		vwreg(14, 0);
		vraddr(0x0);
		assertEquals("lo", (byte) 0xff, vr());
		
		// test all banks
		for (int bank = 0; bank < 0x20000 / 0x4000; bank++) {
			vwreg(14, bank);
			vwaddr(0x0);
			vw((byte)bank);
		}
		for (int bank = 0; bank < 0x20000 / 0x4000; bank++) {
			vwreg(14, bank);
			vraddr(0x0);
			assertEquals(""+bank, (byte)bank, vr());
		}
	}
	
	@Test
	public void testAutoBank() throws Exception {
		vwreg(14, 0);
		vwaddr(0x3fff);
		vw(0xaa);
		vw(0x55);
		
		assertEquals(1, mmio.getMemoryBank().getCurrentBank());
		
		vwreg(14, 0);
		vraddr(0x3fff);
		assertEquals((byte) 0xaa, vr());
		assertEquals((byte) 0x55, vr());
	}
	
	@Test
	public void testFillMem() throws Exception {
		// write low bank
		vwreg(14, 0);
		vwaddr(0x0);
		
		int sum; 
		for (int i = 0; i < 0x20000; i++) {
			sum = frob(i);
			vw((byte) sum);
		}
		
		vwreg(14, 0);
		vraddr(0x0);
		sum = 0; 
		for (int i = 0; i < 0x20000; i++) {
			_testByte(i, frob(i));
		}
		
	}

	private void _testByte(int i, byte b) {
		// this read should bump banks
		assertEquals(""+i, b, vr());
		
		// these should be independent
		if (b != v9938.readAbsoluteVdpMemory(i))
			assertEquals(""+i, 
					b, v9938.readAbsoluteVdpMemory(i));
		ByteMemoryAccess access = v9938.getByteReadMemoryAccess(i);
		if (b != access.memory[access.offset])
			assertEquals(""+i,
					b, access.memory[access.offset]);
	}
	
	
}
