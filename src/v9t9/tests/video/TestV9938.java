/**
 * 
 */
package v9t9.tests.video;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import v9t9.emulator.clients.builtin.video.VdpCanvas;
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
		v9938 = new VdpV9938(memory.getDomain("VIDEO"));
		this.mmio = new Vdp9938Mmio(memory, v9938, 0x20000);
		
		// ensure 64k mode, color
		vwreg(8, v9938.readVdpReg(8) | VdpV9938.R8_VR & ~VdpV9938.R8_BW);
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
		
		////////
		
		// test 16k mode
		
		vwreg(0, 0x0);
		vwreg(8, v9938.readVdpReg(8) & ~VdpV9938.R8_VR);
		
		// write low bank
		vwreg(14, 0);
		vwaddr(0x0);
		vw(0xff);
		
		// write bank 1
		vwreg(14, 1);
		vwaddr(0x0);
		vw(0xaa);
		
		// read bank 1 ( = 0 )
		assertEquals(1, v9938.readVdpReg(14));
		vraddr(0x0);
		assertEquals("hi", (byte) 0xaa, vr());
		
		// read bank 0 ( = 0 0
		vwreg(14, 0);
		vraddr(0x0);
		assertEquals("lo", (byte) 0xaa, vr());
		
	}
	
	@Test
	public void testAutoBank() throws Exception {
		// enable enhanced mode so autobank happens
		vwreg(0, 0x1f);

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
		// enable enhanced mode so autobank happens
		vwreg(0, 0x1f);
		
		// only 128k accessed at once

		// write low bank
		vwreg(14, 0);
		vwaddr(0x0);
		
		for (int i = 0; i < 0x10000; i++) {
			vw((byte) frob(i));
		}
		for (int i = 0x10000; i < 0x20000; i++) {
			vw((byte) frob(i));
		}
		
		vwreg(14, 0);
		vraddr(0x0);
		for (int i = 0; i < 0x10000; i++) {
			_testByte(i, frob(i));
		}
		for (int i = 0x10000; i < 0x20000; i++) {
			_testByte(i, frob(i));
		}

		for (int i = 0x20000; i < 0x30000; i++) {
			_testByte(i & 0x1ffff, frob(i & 0x1ffff));
		}

	}

	private void _testByte(int i, byte b) {
		// this read should bump banks, but not over a 128k boundary
		assertEquals(""+i, b, vr());
		
		// these should be independent
		if (b != v9938.readAbsoluteVdpMemory(i & 0x1ffff))
			assertEquals(""+i, 
					b, v9938.readAbsoluteVdpMemory(i & 0x1ffff));
		ByteMemoryAccess access = v9938.getByteReadMemoryAccess(i & 0x1ffff);
		if (b != access.memory[access.offset])
			assertEquals(""+i,
					b, access.memory[access.offset]);
	}

	@Test
	public void testPalette() {
		byte[] stock;
		byte[] rgb;
		VdpCanvas canvas = v9938.getCanvas();
		
		vwreg(16, 8);
		mmio.write(0x8c04, (byte) 0x04);
		mmio.write(0x8c04, (byte) 0x7);
		mmio.write(0x8c04, (byte) 0x5a);	// A -> 2
		mmio.write(0x8c04, (byte) 0xf);
		
		rgb = canvas.getRGB(1);
		stock = canvas.getStockRGB(1);
		assertEquals(stock[0], rgb[0]);
		assertEquals(stock[1], rgb[1]);
		assertEquals(stock[2], rgb[2]);
		
		rgb = canvas.getRGB(8);
		assertEquals((byte) 0x80, rgb[0]);
		assertEquals((byte) 0x00, rgb[1]);
		assertEquals((byte) 0xff, rgb[2]);
		
		rgb = v9938.getCanvas().getRGB(9);
		assertEquals((byte) 0x40, rgb[0]);
		assertEquals((byte) 0xbf, rgb[1]);
		assertEquals((byte) 0xff, rgb[2]);
		
		///
		
		// B&W
		vwreg(8, v9938.readVdpReg(8) | VdpV9938.R8_BW);
				
		rgb = v9938.getCanvas().getRGB(1);
		assertEquals(rgb[0], rgb[1]);
		assertEquals(rgb[1], rgb[2]);
		
		rgb = v9938.getCanvas().getRGB(8);
		assertEquals(rgb[0], rgb[1]);
		assertEquals(rgb[1], rgb[2]);
		
		rgb = v9938.getCanvas().getRGB(9);
		assertEquals(rgb[0], rgb[1]);
		assertEquals(rgb[1], rgb[2]);
		
		// turn on 
		vwreg(8, v9938.readVdpReg(8) & ~VdpV9938.R8_BW);
		
		rgb = v9938.getCanvas().getRGB(1);
		stock = v9938.getCanvas().getStockRGB(1);
		assertEquals(stock[0], rgb[0]);
		assertEquals(stock[1], rgb[1]);
		assertEquals(stock[2], rgb[2]);
		
		rgb = v9938.getCanvas().getRGB(8);
		assertEquals((byte) 0xff, rgb[2]);
		assertEquals((byte) 0x00, rgb[1]);
		assertEquals((byte) 0x80, rgb[0]);
		
		rgb = v9938.getCanvas().getRGB(9);
		assertEquals((byte) 0x40, rgb[0]);
		assertEquals((byte) 0xbf, rgb[1]);
		assertEquals((byte) 0xff, rgb[2]);
		
	}

	@Test
	public void testIndirect() {
		// autoinc
		vwreg(17, 0x00);
		for (int i = 0; i < 32; i++) 
			mmio.write(0x8c06, (byte) ((i << 5) + i));
		
		for (int i = 0; i < 32; i++)
			// 17 never affected
			if (i != 17)
				assertEquals(""+i, (byte) ((i << 5) + i), v9938.readVdpReg(i));
		
		// no autoinc
		vwreg(17, 0x80);
		for (int i = 0; i < 32; i++) { 
			mmio.write(0x8c06, (byte) ((i << 5) + i));
			assertEquals(""+i, (byte) ((i << 5) + i), v9938.readVdpReg(0));
		}
	}
	
	@Test
	public void testExpansionRAM() {
		// enable enhanced mode so autobank happens
		vwreg(0, 0x1f);

		// without nudging, only the first 128k is modified
		vwaddr(0x0);
		int sum = 0;
		for (int i = 0; i < 131072; i++) {
			sum = frob(i);
			vw(sum);
		}
		
		vraddr(0x0);
		for (int i = 0; i < 131072; i++) {
			sum = frob(i);
			byte ret = vr();
			assertEquals(""+i, (byte)sum, ret);
		}
		
		// enable expansion RAM
		vwreg(45, v9938.readVdpReg(45) | VdpV9938.R45_MXC);

		vwreg(14, 0);
		vraddr(0x0);
		for (int i = 0; i < 65536; i++) {
			assertEquals(""+i, 0, vr());
		}
		
		vwreg(14, 0);
		vwaddr(0x0);
		for (int i = 0; i < 65536; i++) {
			vw(-frob(i));
		}

		// verify
		vwreg(14, 0);
		vraddr(0x0);
		for (int i = 0; i < 65536; i++) {
			assertEquals(""+i, (byte)-frob(i), vr());
		}

		// make sure low mem is ok
		vwreg(14, 0);
		vwreg(45, v9938.readVdpReg(45) & ~VdpV9938.R45_MXC);
		vraddr(0x0);
		for (int i = 0; i < 131072; i++) {
			sum = frob(i);
			byte ret = vr();
			assertEquals(""+i, (byte)sum, ret);
		}
		
	}
}
