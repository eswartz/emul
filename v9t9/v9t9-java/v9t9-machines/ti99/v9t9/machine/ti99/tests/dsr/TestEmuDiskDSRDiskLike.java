/*
  TestEmuDiskDSRDiskLike.java

  (c) 2010-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.tests.dsr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import v9t9.common.files.V9t9FDR;
import v9t9.engine.files.directory.EmuDiskConsts;


/**
 * @author ejs
 *
 */
public class TestEmuDiskDSRDiskLike extends BaseEmuDiskDSRTest {

	private boolean wasRealDisk;

	@Before
	public void setupEmuDisk() {
		wasRealDisk = diskImageDsrEnabled.getBoolean();
		diskImageDsrEnabled.setBoolean(false);
	}
	
	@After
	public void resetRealDisk() {
		diskImageDsrEnabled.setBoolean(wasRealDisk);
	}
	
	private String getString(int addr, int cnt) {
		String string = "";
		for (int i = 0; i <cnt; i++)
			string += (char)xfer.readVdpByte(addr + i);
		return string;
	}
	
	
	@Test
	public void testDiskDirectory() throws Exception {
		// volume
		xfer.writeParamWord(0x4c, (short) 0x01ff);
		xfer.writeParamWord(0x4e, (short) 0x1000);
		xfer.writeParamWord(0x50, (short) 0x0);
		dsr.handleDSR(xfer, (short) EmuDiskConsts.D_SECRW);
		assertEquals(0, xfer.readParamByte(0x50));
	
		String dskName = mymapper.getDsrFileName(dsk1Path.getName());
		assertEquals(dskName, getString(0x1000, dskName.length()));
		
		int total = xfer.readVdpShort(0x1000 + 0xA) & 0xffff;
		assertTrue(total+"", total >= 360);
		
		// index
		xfer.writeParamWord(0x4c, (short) 0x01ff);
		xfer.writeParamWord(0x4e, (short) 0x1000);
		xfer.writeParamWord(0x50, (short) 0x1);
		dsr.handleDSR(xfer, (short) EmuDiskConsts.D_SECRW);
		assertEquals(0, xfer.readParamByte(0x50));

		boolean atEnd = false;
		for (int i = 0; i < 128; i++) {
			int sec = xfer.readVdpShort(0x1000 + i * 2);
			if (sec == 0) {
				atEnd = true;
				continue;
			} else if (!atEnd) {
				assertTrue(sec+"", sec > 1);
			} else {
				assertTrue(sec+"", sec == 0);
				continue;
			}
			
			// FDR
			xfer.writeParamWord(0x4c, (short) 0x01ff);
			xfer.writeParamWord(0x4e, (short) 0x1100);
			xfer.writeParamWord(0x50, (short) sec);
			dsr.handleDSR(xfer, (short) EmuDiskConsts.D_SECRW);
			assertEquals(0, xfer.readParamByte(0x50));

			V9t9FDR fdr = V9t9FDR.createFDR(xfer.vdp, 0x1100);
			File file = mymapper.getLocalFile("DSK1", fdr.getFileName());
			assertTrue(fdr.getFileName(),  file.exists());
		}
	}
}
