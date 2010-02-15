/**
 * 
 */
package v9t9.emulator.hardware.dsrs.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.ejs.coffee.core.utils.HexUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import v9t9.emulator.hardware.dsrs.DiskDirectoryMapper;
import v9t9.emulator.hardware.dsrs.DsrException;
import v9t9.emulator.hardware.dsrs.EmuDiskDsr;
import v9t9.emulator.hardware.dsrs.MemoryTransfer;
import v9t9.emulator.hardware.dsrs.PabConstants;
import v9t9.emulator.hardware.dsrs.PabStruct;
import v9t9.emulator.hardware.dsrs.EmuDiskDsr.EmuDiskPabHandler;
import v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper;
import v9t9.emulator.hardware.dsrs.EmuDiskDsr.EmuDiskPabHandler.PabInfoBlock;
import v9t9.engine.files.FDR;
import v9t9.engine.files.FDRFactory;
import v9t9.engine.files.NativeFDRFile;
import v9t9.engine.files.NativeFile;
import v9t9.engine.files.NativeFileFactory;
import v9t9.engine.files.NativeTextFile;
import v9t9.engine.files.V9t9FDR;
import v9t9.engine.memory.ByteMemoryAccess;


/**
 * @author ejs
 *
 */
public class TestEmuDiskDSR {

	protected static DiskDirectoryMapper mymapper = new DiskDirectoryMapper();
	private static File dsk1Path;
	
	@BeforeClass
	public static void setupSearch() {
		String path = TestEmuDiskDSR.class.getName().replaceAll("\\.", "/");
		String cwd = System.getProperty("user.dir");
		File dir = new File(cwd + "/src/" + path);
		dir = new File(dir.getParentFile(), "data");
		assertTrue(dir+"", dir.exists());
		dsk1Path = dir;
		mymapper.setDiskPath("DSK1", dir);
	}
	
	static class FakeMemory implements MemoryTransfer {
		private byte[] vdp = new byte[0x4000];
		private boolean[] vdpTouched = new boolean[0x4000];
		
		private byte[] param = new byte[0x100];
		
		public byte readParamByte(int offset) {
			return param[offset];
		}

		public short readParamWord(int offset) {
			return (short) ((param[offset] & 0xff << 8) | (param[offset+1] & 0xff));
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
	
	protected EmuDiskDsr dsr = new EmuDiskDsr(mymapper);
	protected FakeMemory xfer = new FakeMemory();
	
	protected EmuDiskPabHandler runCase(PabStruct pab) throws DsrException {
		return runCase(pab, pab.bufaddr - 0x100);
	}
	
	protected EmuDiskPabHandler runCase(PabStruct pab, int pabaddr) throws DsrException {
		EmuDiskPabHandler handler = new EmuDiskPabHandler((short)0x1000, xfer, mymapper, pab);
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
		
		if (fdr instanceof V9t9FDR) {
			((V9t9FDR) fdr).setFileName(mymapper.getDsrFileName(dst.getName()));
			fdr.writeFDR(dst);
		}
	}
	
	@Test
	public void testFileMappingDos() throws Exception {
		assertEquals("FILENAME", mymapper.dsrToDOS("filename"));
		assertEquals("FILENAME", mymapper.dsrToDOS("FileName"));
		assertEquals("FOO" + (char)('/' + 0x80) + "S", mymapper.dsrToDOS("FOO/S"));
		assertEquals("LONGNAME.11", mymapper.dsrToDOS("LONGNAME11"));
		assertEquals("LONGNAME.11", mymapper.dsrToDOS("LONGNAME11GARBAGE"));
		assertEquals("SPACE", mymapper.dsrToDOS("SPACE NAME"));
		//assertEquals("SPACE" + (char)(' ' + 0x80) + "N.AME", mymapper.dsrToDOS("SPACE NAME"));
	}

	@Test
	public void testFileMappingHost() throws Exception {
		assertEquals("filename", mymapper.dsrToHost("filename"));
		assertEquals("filename", mymapper.dsrToHost("FileName"));
		assertEquals("foo&#2F;s", mymapper.dsrToHost("FOO/S"));
		assertEquals("longname11", mymapper.dsrToHost("LONGNAME11"));
		assertEquals("longname11", mymapper.dsrToHost("LONGNAME11GARBAGE"));
		assertEquals("space name", mymapper.dsrToHost("SPACE NAME"));

	}
	

	@Test
	public void testFileMappingFromHost() throws Exception {
		assertEquals("FILENAME", mymapper.hostToDSR("filename"));
		assertEquals("FILENAME", mymapper.hostToDSR("FileName"));
		assertEquals("FOO/S", mymapper.hostToDSR("foo&#2F;s"));
		assertEquals("FOO/S", mymapper.hostToDSR("FOO" + (char)('/' + 0x80) + "S"));
		assertEquals("LONGNAME11", mymapper.hostToDSR("longname11"));
		assertEquals("LONGNAME11", mymapper.hostToDSR("longname11garbage"));
		assertEquals("SPACE NAME", mymapper.hostToDSR("space name"));

	}
	

	@Test
	public void testFileMapping() throws Exception {
		IFileMapper mapper = mymapper;
		assertEquals("DSK1", mapper.getDsrDeviceName(dsk1Path));
		assertNull(mapper.getDsrDeviceName(new File(dsk1Path, "foo")));
		assertEquals(dsk1Path, mapper.getLocalRoot(new File(dsk1Path, "foo")));
		assertEquals("FOO/S", mapper.getDsrFileName("foo&#2f;s"));
		assertEquals(new File(dsk1Path, "monkey"), mapper.getLocalDottedFile("DSK1.Monkey"));
		assertEquals(new File(dsk1Path, "monkey"), mapper.getLocalFile("DSK1", "MONKEY"));
		assertEquals(dsk1Path, mapper.getLocalFile("DSK1", ""));
		assertEquals(dsk1Path, mapper.getLocalFile("DSK1", null));
		assertNull(mapper.getLocalFile("DSK2", "MONKEY"));

	}
	@Test
	public void testOpenBinary() throws Exception {
		// read whole thing
		PabStruct pab = createBinaryPab(PabConstants.op_load, 0x1000, 0x2000, "DSK1.XBPRG");
		runCase(pab);
		assertEquals(0x00, pab.pflags);
		assertEquals(0x2000, pab.recnum);
		assertFDRFile(pab);
		xfer.assertTouched(0x1000, getNativeFile(pab.path).getFileSize(), 0x2000);
	}
	
	@Test
	public void testOpenBinarySmallBuffer() throws Exception {
		// read portion of the thing
		PabStruct pab = createBinaryPab(PabConstants.op_load, 0x1000, 0x20, "DSK1.XBPRG");
		runCase(pab);
		assertEquals(0x00, pab.pflags);
		assertEquals(0x20, pab.recnum);
		assertFDRFile(pab);
		xfer.assertTouched(0x1000, 0x20, getNativeFile(pab.path).getFileSize());
	}
	

	@Test
	public void testSaveBinaryNotExisting() throws Exception {
		String devName = "DSK1.TMP0";
		File file = mymapper.getLocalDottedFile(devName);
		file.delete();
		doTestSaveBinary(devName);
	}
	

	@Test
	public void testSaveBinaryExisting() throws Exception {
		String devName = "DSK1.TMP0";
		copyFile(devName, "DSK1.XBPRG");
		doTestSaveBinary(devName);
	}


	@Test
	public void testSaveBinaryReadOnly() throws Exception {
		String devName = "DSK1.TMP0";
		copyFile(devName, "DSK1.ROFILE");
		try {
			doTestSaveBinary(devName);
			fail("Should not have overwritten protected file");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_illegal, e.getErrorCode());
		}
	}

	private void doTestSaveBinary(String dsrname) throws Exception {
		int bufaddr = 0x1000;
		for (int x = 0; x < 0x1234; x++)
			xfer.writeVdpByte(bufaddr + x, (byte) (x*3));
		
		// write content
		PabStruct pab = createBinaryPab(PabConstants.op_save, bufaddr, 0x1234, dsrname);
		runCase(pab);
		assertEquals(0x00, pab.pflags);
		assertEquals(0x1234, pab.recnum);
		assertFDRFile(pab);
		
		
		// read whole thing back
		int bufrdaddr = 0x0;
		pab = createBinaryPab(PabConstants.op_load, bufrdaddr, 0x2000, dsrname);
		runCase(pab);
		assertFDRFile(pab);
		assertEquals(0x00, pab.pflags);
		assertEquals(0x2000, pab.recnum);
		
		xfer.assertTouched(0x0, 0x1234, 0x2000);
		
		for (int i = 0; i < 0x1234; i++) {
			assertEquals(xfer.readVdpByte(bufaddr + i), xfer.readVdpByte(bufrdaddr + i));
		}
	}
	

	protected PabStruct createOpenPab(int mode, int access, int addr, int reclen, String path) {
		PabStruct pab = new PabStruct();
		pab.opcode = PabConstants.op_open;
		pab.bufaddr = addr;
		pab.preclen = reclen;
		pab.pflags = (mode  | access);
		pab.path = path;
		return pab;
	}

	protected NativeFile getNativeFile(String path) throws IOException {
		int idx = path.indexOf('.');
		File file = mymapper.getLocalFile(path.substring(0, idx), path.substring(idx+1));
		return NativeFileFactory.createNativeFile(file);
	}
	/**
	 * @throws IOException 
	 */
	private void assertFDRFile(PabStruct pab) throws IOException {
		NativeFile file = getNativeFile(pab.path);
		assertTrue(file.getClass()+"", file instanceof NativeFDRFile);
		assertTrue(file.getFile().exists());		
	}
	private void assertTextFile(PabStruct pab) throws IOException {
		NativeFile file = getNativeFile(pab.path);
		assertTrue(file.getClass()+"", file instanceof NativeTextFile);
		assertTrue(file.getFile().exists());		
	}
	
	@Before
	public void clearFiles() {
		PabInfoBlock pabInfoBlock = EmuDiskDsr.EmuDiskPabHandler.getPabInfoBlock(dsr.getCruBase());
		pabInfoBlock.reset();
		
		deleteFile("DSK1.TMP1");
		deleteFile("DSK1.TMP2");
		
		Arrays.fill(xfer.vdp, (byte) 0);
	}
	
	private void deleteFile(String string) {
		try {
			NativeFile file = getNativeFile(string);
			file.getFile().delete();
		} catch (IOException e) {
			
		}
	}

	@Test
	public void testOpenDisVar80Text() throws Exception {
		
		PabStruct pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 80, "DSK1.TEXTFILE");
		runCase(pab);
		assertEquals(0x14, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(80, pab.preclen);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertTextFile(pab);
		
		pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.TEXTFILE");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertEquals(0x14, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertTextFile(pab);
	}
	

	@Test
	public void testOpenDisVar80() throws Exception {
		PabStruct pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 80, "DSK1.DV80");
		runCase(pab);
		assertEquals(0x14, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(80, pab.preclen);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
		
		pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.DV80");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertEquals(0x14, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
	}
	
	@Test
	public void testOpenDisVar80NoModify() throws Exception {
		NativeFile nativeFile = getNativeFile("DSK1.DV80");
		int origSize = nativeFile.getFileSize();
		
		PabStruct pab = createOpenPab(PabConstants.m_update, PabConstants.fp_variable, 0x1000, 80, "DSK1.DV80");
		runCase(pab);
		assertEquals(0x10, pab.pflags);
		assertFDRFile(pab);
		
		assertEquals(origSize, nativeFile.getFileSize());
		
		pab = createOpenPab(PabConstants.m_append, PabConstants.fp_variable, 0x1000, 0, "DSK1.DV80");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertFDRFile(pab);
		
		assertEquals(origSize, nativeFile.getFileSize());
	}


	@Test
	public void testCreateDisVar80() throws Exception {
		// ensure we use default record length
		PabStruct pab = createOpenPab(PabConstants.m_output, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertEquals(0x12, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(80, pab.preclen);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
		
		pab = createOpenPab(PabConstants.m_update, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertEquals(0x10, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
		
		try {
			pab = createOpenPab(PabConstants.m_update, 0, 0x1000, 0, "DSK1.TMP1");
			runCase(pab);
			fail("Cannot update with different attributes");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}
		try {
			pab = createOpenPab(PabConstants.m_update, PabConstants.fp_internal, 0x1000, 0, "DSK1.TMP1");
			runCase(pab);
			fail("Cannot update with different attributes");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}
		try {
			pab = createOpenPab(PabConstants.m_update, PabConstants.fp_variable, 0x1000, 128, "DSK1.TMP1");
			runCase(pab);
			fail("Cannot update with different attributes");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}

		// can overwrite with different flags
		pab = createOpenPab(PabConstants.m_output, 0, 0x1000, 128, "DSK1.TMP1");
		runCase(pab);
	}
	

	@Test
	public void testCreateDisFix() throws Exception {
		// ensure we use default record length
		PabStruct pab = createOpenPab(PabConstants.m_output, 0, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertEquals(0x2, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(80, pab.preclen);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
		
		pab = createOpenPab(PabConstants.m_update, 0, 0x1000, 80, "DSK1.TMP1");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertEquals(0x0, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
		
		try {
			pab = createOpenPab(PabConstants.m_update, PabConstants.fp_variable, 0x1000, 128, "DSK1.TMP1");
			runCase(pab);
			fail("Cannot update with different attributes");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}
		
		// allow display vs. internal 
		pab = createOpenPab(PabConstants.m_update, PabConstants.fp_internal, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		
		// can overwrite
		pab = createOpenPab(PabConstants.m_output, 0, 0x1000, 128, "DSK1.TMP1");
		runCase(pab);
		
		assertEquals(128, pab.preclen);
		assertEquals(0x2, pab.pflags);
		assertEquals(0x0, pab.recnum);
		assertEquals(0x1000, pab.bufaddr);
		assertEquals(0, pab.charcount);
		assertFDRFile(pab);
	}
	

	String test1_exp = " DEF SECRET\n" + 
	" REF VMBW,KSCAN\n" + 
	"SECRET LI R0,300\n" + 
	"KEYB EQU >8375\n" + 
	"STAT EQU >837C\n" + 
	"SET DATA >2000\n" + 
	"TE CLR @KEYB\n" + 
	" BLWP @KSCAN\n" + 
	" MOV @STAT,R3\n" + 
	" COC @SET,R3\n" + 
	" JEQ PR\n" + 
	" JMP TE\n" + 
	"PR LI R1,ME\n" + 
	" LI R2,15\n" + 
	" CLR R4\n" + 
	" BLWP @VMBW\n" + 
	"Q INC R4\n" + 
	" CI R4,>100\n" + 
	" JNE Q\n" + 
	" JMP T\n" + 
	"T LI R1,NO\n" + 
	" LI R2,15\n" + 
	" BLWP @VMBW\n" + 
	" JMP TE\n" + 
	"ME TEXT 'TOUCH YOUR FACE'\n" + 
	"NO TEXT '               '\n" + 
	" END\n" + 
	" \n" + 
	" \n"; 
	
	String[] test1_lines = test1_exp.split("\n");
	@Test
	public void readDisVar80_1() throws Exception {
		
		doTest1("DSK1.DV80");

	}

	/**
	 * @param string
	 */
	private void doTest1(String filename) throws Exception {
		PabStruct pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, filename);
		runCase(pab);
		assertFDRFile(pab);
		
		for (int i =0; i < test1_lines.length; i++) {
			String str1 = readString(pab);
			assertEquals(test1_lines[i], str1);
		}
		
		try {
			pab.opcode = PabConstants.op_read;
			runCase(pab);
			fail("Should have hit EOF");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_endoffile, e.getErrorCode());
		}
				
	}

	/**
	 * @param str1
	 */
	private void assertAscii(String str1) {
		for (int i = 0; i < str1.length(); i++) {
			char ch = str1.charAt(i);
			if (ch < 32 || ch > 127)
				fail("not ASCII: " + str1);
		}
	}

	@Test
	public void writeDisVar80_1() throws Exception {
		PabStruct pab = createOpenPab(PabConstants.m_output, PabConstants.fp_variable, 0x1000, 80, "DSK1.TMP1");
		runCase(pab);
		assertFDRFile(pab);
		
		String str1 = "Hello there.";
		writeString(pab, str1);

		pab.opcode = PabConstants.op_close;
		runCase(pab);
		
		NativeFile nativeFile = getNativeFile("DSK1.TMP1");
		int size = nativeFile.getFileSize();
		assertEquals(str1.length() + 1, size);
		
		///
		
		pab = createOpenPab(PabConstants.m_append, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertFDRFile(pab);
		
		String str2 = "And how, and how, and how!";
		writeString(pab, str2);
		
		pab.opcode = PabConstants.op_close;
		runCase(pab);
		
		nativeFile = getNativeFile("DSK1.TMP1");
		size = nativeFile.getFileSize();
		assertEquals(str1.length() + 1 + str2.length() + 1, size);
		
		////////
		
		// note: not closing
		pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertFDRFile(pab);
		
		String str = readString(pab);
		assertEquals(str1, str);
		str = readString(pab);
		assertEquals(str2, str);
		
		try {
			pab.opcode = PabConstants.op_read;
			runCase(pab);
			fail("Expected EOF");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_endoffile, e.getErrorCode());
		}
	}

	private String readString(PabStruct pab) throws DsrException {
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		String str = getPabString(pab);
		return str;
	}

	private void writeString(PabStruct pab, String str1) throws DsrException {
		setPabString(pab, str1);
		pab.opcode = PabConstants.op_write;
		runCase(pab);
	}

	private void setPabString(PabStruct pab, String string) {
		for (int i = 0; i <string.length(); i++)
			xfer.writeVdpByte(pab.bufaddr + i, (byte) string.charAt(i));
		pab.charcount = string.length();
	}

	private String getPabString(PabStruct pab) {
		String string = "";
		for (int i = 0; i <pab.charcount; i++)
			string += (char)xfer.readVdpByte(pab.bufaddr + i);
		return string;
	}
	
	@Test
	public void testWriteDisVar80_2() throws Exception {
		PabStruct pab = createOpenPab(PabConstants.m_output, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertFDRFile(pab);
		
		for (int i =0; i < test1_lines.length; i++) {
			writeString(pab, test1_lines[i]);
		}

		pab.opcode = PabConstants.op_close;
		runCase(pab);
		
		doTest1("DSK1.TMP1");
		
	}
	
	@Test
	public void testReadDisFix80() throws Exception {
		PabStruct pab;
		try {
			pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.DF80");
			runCase(pab);
			fail("Should have failed");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}
		
		pab = createOpenPab(PabConstants.m_input, 0, 0x1000, 0, "DSK1.DF80");
		runCase(pab);
		assertEquals(80, pab.preclen); // default
		assertFDRFile(pab);

			for (int i = 0; i < 5; i++) {
				try {
					String str1 = readString(pab);
					assertEquals(80, str1.length());
				} catch (DsrException e) {
					fail("#"+i+" -> " + e.toString());
				}
			}
		try {
			pab.opcode = PabConstants.op_read;
			runCase(pab);
			fail("Should have hit EOF");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_endoffile, e.getErrorCode());
		}
				
	}
}
