/**
 * 
 */
package v9t9.machine.ti99.tests.dsr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.files.IFileMapper;
import v9t9.common.files.NativeFDRFile;
import v9t9.common.files.NativeFile;
import v9t9.common.files.NativeFileFactory;
import v9t9.common.files.NativeTextFile;
import v9t9.engine.dsr.DsrException;
import v9t9.engine.dsr.PabConstants;
import v9t9.engine.dsr.PabStruct;
import v9t9.engine.files.directory.EmuDiskConsts;
import v9t9.engine.files.directory.EmuDiskPabHandler;
import v9t9.engine.files.directory.OpenFile;
import v9t9.engine.files.directory.PabInfoBlock;


/**
 * @author ejs
 *
 */
public class TestEmuDiskDSR extends BaseEmuDiskDSRTest {

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
		assertEquals(new File(dsk1Path, "monkey"), mapper.getLocalFile("DSK", "DATA.MONKEY"));
		assertEquals(dsk1Path, mapper.getLocalFile("DSK1", ""));
		assertEquals(dsk1Path, mapper.getLocalFile("DSK1", null));
		assertNull(mapper.getLocalFile("DSK3", "MONKEY"));

	}
	
	@Test
	public void testDeviceMapping() throws Exception {
		IFileMapper mapper = mymapper;
		assertEquals("DSK1", mapper.getDeviceNamed("DATA"));
		assertEquals("DSK2", mapper.getDeviceNamed("EXTRA/LALA"));
		assertNull(mapper.getDeviceNamed("MONKEY"));

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
		
		assertNull("Should not register open file", getOpenFile(pab));
	}
	
	@Test
	public void testOpenBinarySmallBuffer() throws Exception {
		// read portion of the thing
		PabStruct pab = createBinaryPab(PabConstants.op_load, 0x1000, 0x20, "DSK.DATA.XBPRG");
		runCase(pab);
		assertEquals(0x00, pab.pflags);
		assertEquals(0x20, pab.recnum);
		assertFDRFile(pab);
		xfer.assertTouched(0x1000, 0x20, getNativeFile(pab.path).getFileSize());
	}
	
	@Test
	public void testOpenBinaryFail() throws Exception {
		// DIS/VAR 254 
		PabStruct pab = createBinaryPab(PabConstants.op_load, 0x1000, 0x2000, "DSK1.XBPRGBIG");
		try {
			runCase(pab);
			fail("Should have failed");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badfiletype, e.getErrorCode());
		}
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
			assertEquals(PabConstants.e_readonly, e.getErrorCode());
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
	
	protected PabStruct createPab(int opcode, int pflags, int addr, int reclen, String path) {
		PabStruct pab = new PabStruct();
		pab.opcode = opcode;
		pab.bufaddr = addr;
		pab.preclen = reclen;
		pab.pflags = pflags;
		pab.path = path;
		return pab;
	}

	protected PabStruct createOpenPab(int mode, int access, int addr, int reclen, String path) {
		PabStruct pab = new PabStruct();
		pab.pabaddr = (short)(addr - 0x20);
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
		return NativeFileFactory.INSTANCE.createNativeFile(file);
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
		PabInfoBlock diskInfoBlock = EmuDiskPabHandler.getPabInfoBlock(dsr.getCruBase());
		diskInfoBlock.reset();
		
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
		assertNotNull("Should register open file", getOpenFile(pab));
		
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
	
	//@Test
	public void testCreateIntFixPreAlloced() throws Exception {
		// ensure we use default record length
		PabStruct pab = createOpenPab(PabConstants.m_output, PabConstants.fp_internal, 0x1000, 128, "DSK1.TMP2");
		pab.recnum = 0x8000;
		try {
			runCase(pab);
		} catch (DsrException e) {
			assertEquals(PabConstants.e_badopenmode, e.getErrorCode());
		}
		
		pab.recnum = 0x7fff;
		runCase(pab);
		assertFDRFile(pab);
		
		assertEquals(0, pab.recnum);
		
		int status = readStatus(pab);
		assertEquals(PabConstants.st_internal, status);
		
		
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
		xfer.assertTouched(pab.bufaddr, pab.charcount, pab.preclen);
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
		
		assertNull("Should close file", getOpenFile(pab));
		
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
					assertEquals(80, pab.charcount); 
					assertEquals(80, pab.preclen); 
					assertEquals(80, str1.length());
				} catch (DsrException e) {
					fail("#"+i+" -> " + e.toString());
				}
			}
			assertEquals(5, pab.recnum);
		try {
			pab.opcode = PabConstants.op_read;
			runCase(pab);
			fail("Should have hit EOF");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_endoffile, e.getErrorCode());
		}
				
	}
	
	@Test
	public void testRestoreRewind() throws Exception {
		PabStruct pab;
		
		// relative file
		pab = createOpenPab(PabConstants.m_input, PabConstants.fp_relative, 0x1000, 80, "DSK1.DF80");
		runCase(pab);
		assertEquals(0, pab.recnum);
		assertFDRFile(pab);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(1, pab.recnum);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(2, pab.recnum);
		
		pab.opcode = PabConstants.op_restore;
		pab.recnum = 3;
		runCase(pab);
		assertEquals(3, pab.recnum);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(4, pab.recnum);
	
		pab.opcode = PabConstants.op_close;
		runCase(pab);
		
		
		// relative file
		pab = createOpenPab(PabConstants.m_input, 0, 0x1000, 80, "DSK1.DF80");
		runCase(pab);
		assertEquals(0, pab.recnum);
		assertFDRFile(pab);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(1, pab.recnum);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(2, pab.recnum);
		
		// ignored here
		pab.opcode = PabConstants.op_restore;
		pab.recnum = 3;
		runCase(pab);
		assertEquals(0, pab.recnum);
		
		pab.opcode = PabConstants.op_read;
		runCase(pab);
		assertEquals(80, pab.charcount);
		assertEquals(1, pab.recnum);
	
		pab.opcode = PabConstants.op_close;
		runCase(pab);
	}
	
	@Test
	public void testStatus() throws Exception {
		PabStruct pab;
		
		int pflags = PabConstants.m_output | PabConstants.fp_relative; // meaningless
		
		// closed file
		pab = createPab(PabConstants.op_status, pflags, 0x1000, 80, "DSK1.DF80");
		runCase(pab);
		// better not overwrite
		assertFDRFile(pab);	
		
		assertEquals(0, pab.scrnoffs);
		
		pab = createPab(PabConstants.op_status, pflags, 0x1000, 80, "DSK1.DV80");
		assertEquals(PabConstants.st_variable, readStatus(pab));
		
		pab = createPab(PabConstants.op_status, pflags, 0x1000, 80, "DSK1.ROFILE");
		assertEquals(PabConstants.st_protected | PabConstants.st_program, readStatus(pab));
		
		pab = createPab(PabConstants.op_status, pflags, 0x1000, 80, "DSK1.XBPRG");
		assertEquals(PabConstants.st_program, readStatus(pab));
		
		pab = createPab(PabConstants.op_status, pflags, 0x1000, 80, "DSK1.XBPRGBIG");
		assertEquals(PabConstants.st_variable | PabConstants.st_internal, readStatus(pab));

		///////
		
		pab = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 80, "DSK1.DV80");
		runCase(pab);
		assertFDRFile(pab);
		
		assertEquals(PabConstants.st_variable, readStatus(pab));
		
		// read up to end
		for (int i = 0; i < test1_lines.length; i++) {
			assertEquals(PabConstants.st_variable, readStatus(pab));
			
			readString(pab);
		}
		
		assertEquals(PabConstants.st_endoffile | PabConstants.st_variable, readStatus(pab));
	}
	
	protected int readStatus(PabStruct pab) throws DsrException {
		pab.opcode = PabConstants.op_status;
		runCase(pab);
		return pab.scrnoffs;
	}

	@Test
	public void testDelete() throws Exception {
		String devName = "DSK1.TMP0";
		copyFile(devName, "DSK1.ROFILE");
		
		PabStruct pab;
		pab = createPab(PabConstants.op_delete, 0, 0x1000, 80, devName);
		try {
			runCase(pab);
			fail("Should not have deleted protected file");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_readonly, e.getErrorCode());
			assertFDRFile(pab);
		}

		copyFile(devName, "DSK1.XBPRG");
		
		pab = createPab(PabConstants.op_delete, 0, 0x1000, 80, devName);
		runCase(pab);
		try {
			getNativeFile(devName);
			fail("Should have deleted");
		} catch (IOException e) {
			
		}
		
		// delete open file

		pab = createOpenPab(PabConstants.m_output, PabConstants.fp_variable, 0x1000, 0, "DSK1.TMP1");
		runCase(pab);
		assertFDRFile(pab);
		
		for (int i =0; i < test1_lines.length; i++) {
			writeString(pab, test1_lines[i]);
		}
		
		pab.opcode = PabConstants.op_delete;
		runCase(pab);
		
		assertNull("Should close deleted file", getOpenFile(pab));
	}

	/**
	 * @param pab
	 * @return
	 */
	private OpenFile getOpenFile(PabStruct pab) {
		return EmuDiskPabHandler.getPabInfoBlock(dsr.getCruBase()).findOpenFile(pab.pabaddr);
	}
	
	@Test
	public void testFileCount() throws Exception {
		xfer.writeParamByte(0x4c, (byte) 1);
		xfer.writeParamByte(0x50, (byte) 0xff);
		dsr.handleDSR(xfer, (short) EmuDiskConsts.D_FILES);
		assertEquals(0, xfer.readParamByte(0x50));	// if not 0, we thought the real disk DSR would handle it
		
		PabStruct pab1 = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.DV80");
		runCase(pab1);
		assertFDRFile(pab1);

		// fine to make a new file on top of an old one
		PabStruct pab2 = createOpenPab(PabConstants.m_output, 0, 0x1000, 0, "DSK1.TMP1");
		runCase(pab2);
		assertFDRFile(pab2);

		pab2.opcode = PabConstants.op_close;
		runCase(pab2);
		
		////////
		
		pab1 = createOpenPab(PabConstants.m_input, PabConstants.fp_variable, 0x1000, 0, "DSK1.DV80");
		runCase(pab1);
		assertFDRFile(pab1);

		// second file, at different address
		pab2 = createOpenPab(PabConstants.m_output, 0, 0x1100, 0, "DSK1.TMP1");
		try {
			runCase(pab2);
			fail("Should have failed to open extra file");
		} catch (DsrException e) {
			assertEquals(PabConstants.e_outofspace, e.getErrorCode());
		}

	}
}
