/**
 * 
 */
package v9t9.emulator.hardware.dsrs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper;


public class DiskDirectoryMapper implements IFileMapper {
	private Map<String, File> diskMap = new HashMap<String, File>();
	
	public static final DiskDirectoryMapper INSTANCE = new DiskDirectoryMapper();
	
	public DiskDirectoryMapper() {
	}
	
	public void setDiskPath(String device, File dir) {
		diskMap.put(device, dir);
	}
	

	public File getLocalRoot(File file) {
		while (file != null) {
			for (Map.Entry<String, File> entry : diskMap.entrySet()) {
				if (entry.getValue().equals(file)) {
					return file;
				}
			}

			file = file.getParentFile();
		}
		return null;
	}
	

	/*	In V9t9 6.0, we used 8.3 filenames, and these chars
		were converted by adding 0x80 to the name on disk. 
		In this version, we still have illegal chars, but
		they are replaced with the escape sequence '&#xx;' as
		in HTML. */
	private final String DOS_illegalchars = "<>=,;:*?[]/\\";

	private char FIAD_esc = '&';
	private final String FIAD_illegalchars = "<>,:*?/\\";

	/**	Convert a TI filename to a DOS 8.3 filename.
	 *
	 * We replace illegal chars with high-ASCII characters
	 */
	public String dsrToDOS(String tiname) {
		StringBuilder dosname = new StringBuilder();

		int max = 10;
		int ptr = 0;

		while (ptr < tiname.length() && max-- > 0) {
			char cur;

			cur = tiname.charAt(ptr);

			/* forced end-of-filename? */
			if (cur == ' ' || cur == 0)
				break;

			if (ptr == 8)
				dosname.append('.');

			/* offset illegal chars */
			if (DOS_illegalchars.indexOf(cur) >= 0)
				cur |= 0x80;

			/* force uppercase */
			if (cur >= 'a' && cur <= 'z')
				cur -= 0x20;

			dosname.append(cur);
			ptr++;
		}

		// fiad_logger(_L | L_2,
		// _("fiad_filename_ti2dos:  incoming = '%.*s', outgoing = '%s'\n"),
		// 10 - max, tiname,dosname);
		return dosname.toString();
	}

	/** Convert a TI filename to the host OS.  

	   We convert illegal chars in FIAD_illegalchars into HTML-like
	   encodings (&#xx;) so all possible filenames can be stored.
	*/
	public String dsrToHost(String tiname) {
		StringBuilder hostname = new StringBuilder();
		int max = 10;
		int tptr = 0;

		while (tptr < tiname.length() && max-- > 0) {
			char cur = tiname.charAt(tptr++);

			/* force lowercase */
			if (cur >= 'A' && cur <= 'Z')
				cur += 0x20;
			else
			// illegal chars
			if (cur == FIAD_esc || FIAD_illegalchars.indexOf(cur) >= 0) {
				char hex;

				hostname.append('&');
				hostname.append('#');
				hex = (char) ((cur & 0xf0) >> 4);
				if (hex > 9)
					hex += 'A' - 10;
				else
					hex += '0';
				hostname.append(hex);
				hex = (char) (cur & 0xf);
				if (hex > 9)
					hex += 'A' - 10;
				else
					hex += '0';
				hostname.append(hex);
				cur = ';';
			}

			hostname.append(cur);
		}
		// fiad_logger(_L | L_2,
		// _("fiad_filename_ti2host:  incoming = '%.*s', outgoing = '%s'\n"),
		// 10 - max, tiname, hostname);
		return hostname.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getLocalFilePath(java.lang.String)
	 */
	public String getLocalFileName(String dsrPath) {
		return dsrToHost(dsrPath);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.EmuDiskDsr.IFileMapper#getLocalDottedFile(java.lang.String)
	 */
	public File getLocalDottedFile(String deviceFilename) {
		int idx = deviceFilename.indexOf('.');
		if (idx < 0)
			return getLocalFile(deviceFilename, null);
		else
			return getLocalFile(deviceFilename.substring(0, idx), deviceFilename.substring(idx + 1));
	}
	
	public File getLocalFile(String device, String filename) {
		File dir = diskMap.get(device);
		if (dir == null)
			return null;
		if (filename == null || filename.length() == 0)
			return dir;
		return new File(dir, getLocalFileName(filename));
	}
	
	protected boolean isxdigit(char ch) {
		return (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9');
	}
	protected int hexval(char ch) {
		if (ch >= 'A' && ch <= 'F')
			return 10 + (ch - 'A');
		if (ch >= 'a' && ch <= 'f')
			return 10 + (ch - 'a');
		return ch - '0';
	}

	/** Convert a filename to TI format */
	public String hostToDSR(String hostname) {
		StringBuilder tiname = new StringBuilder();

		int hptr = 0, max = 10;

		while (hptr < hostname.length() && max > 0) {
			char cur = hostname.charAt(hptr);

			if (cur != '.') {
				/* force uppercase */
				if (Character.isLowerCase(cur))
					cur = Character.toUpperCase(cur);
				else if (cur == '&' && hptr + 4 < hostname.length()
						&& hostname.charAt(hptr + 1) == '#'
						&& isxdigit(hostname.charAt(hptr + 2))
						&& isxdigit(hostname.charAt(hptr + 3))
						&& hostname.charAt(hptr + 4) == ';') {
					int val;

					val = hexval(hostname.charAt(hptr + 2));
					cur = (char) (hexval(hostname.charAt(hptr + 3)) | (val << 4));
					hptr += 4;
				} else if ((cur & 0x80) != 0
						&& DOS_illegalchars.indexOf(cur & 0x7f) >= 0) {
					cur ^= 0x80;
				}

				tiname.append(cur);
				max--;
			}
			hptr++;
		}
		// fiad_logger(_L | L_2,
		// _("fiad_filename_host2ti:  incoming: '%s', outgoing: '%.10s'\n"),
		// hostname, tiname);

		return tiname.toString();
	}

	
	public String getDsrFileName(String filename) {
		return hostToDSR(filename);
	}
	
	public String getDsrDeviceName(File dir) {
		for (Map.Entry<String, File> entry : diskMap.entrySet()) {
			if (entry.getValue().equals(dir)) {
				return entry.getKey();
			}
		}
		return null;
	}
}