/*
  DiskDirectoryMapper.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;


public class DiskDirectoryUtils {
	/*	In V9t9 6.0, we used 8.3 filenames, and these chars
		were converted by adding 0x80 to the name on disk. 
		In this version, we still have illegal chars, but
		they are replaced with the escape sequence '&#xx;' as
		in HTML. */
	private final static String DOS_illegalchars       = "<>=,;:*?[]/\\";
	private final static String DOS_illegalchars_linux = "<>=,;:*?[]\u00BB\\";

	private static char FIAD_esc = '&';
	private static final String FIAD_illegalchars = "<>,:*?/\\";

	/**	Convert a TI filename to a DOS 8.3 filename.
	 *
	 * We replace illegal chars with high-ASCII characters
	 */
	public static String dsrToDOS(String tiname) {
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


	/**	Convert a TI filename to a DOS 8.3 filename.
	 *
	 * We replace illegal chars with high-ASCII characters,
	 * and then replace those with what Linux sees (UTF-8
	 * charmapped variants)
	 */
	public static String dsrToDOSLinux(String tiname) {
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
			int illidx = DOS_illegalchars.indexOf(cur); 
			if (illidx >= 0) {
				cur = DOS_illegalchars_linux.charAt(illidx);
			}

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

	/** Convert a TI filename to the host OS, assuming a modern
	 * filesystem.  

	   We convert illegal chars in FIAD_illegalchars into HTML-like
	   encodings (&#xx;) so all possible filenames can be stored.
	*/
	public static String dsrToHost(String tiname) {
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
	
	public static File getLocalFile(File dir, String filename) {
		if (filename == null || filename.length() == 0)
			return dir;
		
		String[] cands = new String[] {
				dsrToHost(filename),
				dsrToDOS(filename),
				dsrToDOSLinux(filename)
		};
		
		File preferred = new File(dir, cands[0]);
		
		String[] names = dir.list();
		if (names != null) {
			for (String candName : cands) {
				// do case-insensitive check
				for (String name : names) {
					if (name.equalsIgnoreCase(candName)
							|| (name.toLowerCase().startsWith(candName.toLowerCase()) && name.toLowerCase().endsWith(".bin"))) {
						File cand = new File(dir, name);
						if (cand.exists() && !preferred.exists())
							return cand;
					}
				}
			}
		}
		return preferred;
	}
	
	protected static boolean isxdigit(char ch) {
		return (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9');
	}
	protected static int hexval(char ch) {
		if (ch >= 'A' && ch <= 'F')
			return 10 + (ch - 'A');
		if (ch >= 'a' && ch <= 'f')
			return 10 + (ch - 'a');
		return ch - '0';
	}

	/** Convert a filename to TI format */
	public static String hostToDSR(String hostname) {
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

}