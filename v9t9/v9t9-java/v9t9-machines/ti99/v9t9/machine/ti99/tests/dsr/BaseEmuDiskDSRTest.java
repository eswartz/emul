/*
  BaseEmuDiskDSRTest.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.tests.dsr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import ejs.base.properties.IProperty;
import ejs.base.utils.HexUtils;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.files.FDR;
import v9t9.common.files.FDRFactory;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.PabStruct;
import v9t9.engine.files.directory.DiskDirectoryMapper;
import v9t9.engine.files.directory.EmuDiskConsts;
import v9t9.engine.files.directory.EmuDiskSettings;
import v9t9.engine.files.directory.EmuDiskPabHandler;
import v9t9.engine.files.image.Dumper;
import v9t9.engine.files.image.RealDiskDsrSettings;
import v9t9.machine.ti99.dsr.emudisk.EmuDiskDsr;

/**
 * @author ejs
 *
 */
public class BaseEmuDiskDSRTest {

	protected ISettingsHandler settings = new BasicSettingsHandler();
	
	protected DiskDirectoryMapper mymapper = new DiskDirectoryMapper(settings.get(EmuDiskSettings.emuDiskDsrEnabled));
	protected File dsk1Path;
	
	protected IProperty diskImageDsrEnabled;
	

	private IProperty emuDiskDsrEnabled;
	
	static class FakeMemory implements IMemoryTransfer {
		byte[] vdp = new byte[0x4000];
		private boolean[] vdpTouched = new boolean[0x4000];
		
		private byte[] param = new byte[0x100];
		
		public byte readParamByte(int offset) {
			return param[offset];
		}

		public short readParamWord(int offset) {
			return (short) (((param[offset] & 0xff) << 8) | (param[offset+1] & 0xff));
		}
		public void writeParamByte(int offset, byte val) {
			param[offset] = val;
		}
		
		public void writeParamWord(int offset, short val) {
			param[offset] = (byte) (val >> 8);
			param[offset + 1] = (byte) (val & 0xff);
		}

		public byte readVdpByte(int vaddr) {
			return vdp[vaddr];
		}

		public short readVdpShort(int vaddr) {
			return (short) ((vdp[vaddr] & 0xff << 8) | (vdp[vaddr + 1] & 0xff));
		}

		public void writeVdpByte(int vaddr, byte byt) {
			vdp[vaddr] = byt;
			vdpTouched[vaddr] = true;
		}

		public void dirtyVdpMemory(int vaddr, int read) {
			Arrays.fill(vdpTouched, vaddr, vaddr + read, true);
		}

		public ByteMemoryAccess getVdpMemory(int vaddr) {
			return new ByteMemoryAccess(vdp, vaddr);
		}

		/**
		 * 
		 */
		public void reset() {
			Arrays.fill(vdpTouched, false);
		}

		public void assertTouched(int addr, int count, int notbeyond) {
			int start = -1;
			int end = -1;
			int fin = addr + count;
			int finboundary = addr + notbeyond;
			while (addr < fin) {
				if (!vdpTouched[addr]) {
					if (start < 0)
						start = addr;
					end = addr;
				} else {
					if (start  >= 0)
						fail("VDP not touched: " + HexUtils.toHex4(start) + "-" + HexUtils.toHex4(end));
				}
				addr++;
			}
			
			start = end = -1;
			while (addr < finboundary) {
				if (vdpTouched[addr]) {
					if (start < 0)
						start = addr;
					end = addr;
				} else {
					if (start  >= 0)
						fail("VDP touched: " + HexUtils.toHex4(start) + "-" + HexUtils.toHex4(end));
				}
				addr++;
			}
		}
		
	}

	protected FakeMemory xfer = new FakeMemory();
	protected EmuDiskDsr dsr;
	private Dumper dumper;
	
	@Before
	public void setupDSR() throws Exception {
		settings = new BasicSettingsHandler();
		

		diskImageDsrEnabled = settings.get(RealDiskDsrSettings.diskImageDsrEnabled);
		diskImageDsrEnabled.setBoolean(false);
		
		emuDiskDsrEnabled = settings.get(EmuDiskSettings.emuDiskDsrEnabled);
		emuDiskDsrEnabled.setBoolean(true);
		
		
		xfer.writeParamWord(0x70, (short) 0x3fff);
		
		dsr = new EmuDiskDsr(settings, mymapper);
		

		URL url = BaseEmuDiskDSRTest.class.getResource("/data/df80");
		assertNotNull(url);
		File dir = new File(url.toURI()).getParentFile();
		assertTrue(dir+"", dir.isDirectory());
		
		
		dsk1Path = dir;
		mymapper.registerDiskSetting("DSK1", new SettingSchemaProperty("DSK1", dir.getAbsolutePath()));
		
		dir = new File(dir.getParentFile(), mymapper.getLocalFileName("EXTRA/LALA"));
		mymapper.registerDiskSetting("DSK2", new SettingSchemaProperty("DSK2", dir.getAbsolutePath()));
		
		mymapper.unregisterDiskSetting("DSK3");
		
		dumper = new Dumper(settings, RealDiskDsrSettings.diskImageDebug, ICpu.settingDumpFullInstructions); 
		dsr.handleDSR(xfer, (short) EmuDiskConsts.D_INIT);
	}

	protected EmuDiskPabHandler runCase(PabStruct pab) throws DsrException {
		EmuDiskPabHandler handler = new EmuDiskPabHandler(
				dumper,
				(short)0x1000, xfer, mymapper, pab, (short) 0x3ff5);
		xfer.reset();
		handler.run();
		return handler;
	}
	
	protected PabStruct createBinaryPab(int opcode, int addr, int count, String path) {
		PabStruct pab = new PabStruct();
		pab.opcode = opcode;
		pab.bufaddr = addr;
		pab.recnum = count;
		pab.path = path;
		return pab;
	}
	
	protected void copyFile(String devNameTo, String devNameFrom) throws Exception {
		File dst = mymapper.getLocalDottedFile(devNameTo);
		File src = mymapper.getLocalDottedFile(devNameFrom);
		
		// put something there
		dst.setWritable(true);
		FileOutputStream os = new FileOutputStream(dst);
		FileInputStream is = new FileInputStream(src);
		int ch;
		while ((ch = is.read()) != -1)
			os.write(ch);
		os.close();
		is.close();
		
		// rewrite FDR
		FDR fdr = FDRFactory.createFDR(src);
		assertNotNull(fdr);
		
		fdr.setFileName(mymapper.getDsrFileName(dst.getName()));
		fdr.writeFDR(dst);
	}
	

}
