/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import v9t9.common.files.IMD5SumFilter;
import v9t9.common.files.IMD5SumFilter.FilterSegment;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.utils.Pair;
import ejs.base.utils.TextUtils;

/**
 * This class analyzes has utilities for detecting ROMs
 * @author ejs
 *
 */
public class TI99RomUtils {

	private static final String[] ACRONYMS_AND_ROMAN_NUMERALS = new String[] { "Ti", 
		"Ii", "Iii", "Iv", "Vi", "Vii", "Viii", "Ix", 
		"Xi", "Xii", "Xiii", "Xiv", "Xv", "Xvi", "Xvii", "Xviii", "Xix" };
	private static final String[] PREPOSITIONS = new String[] { "And", "Or", "Of", "In", "For", "The" };

	private static final Logger log = Logger.getLogger(TI99RomUtils.class);
	

	public static boolean hasId(byte[] content) {
		for (int addr = 0 ; addr < content.length; addr += 0x2000) {
			if (content[addr] == (byte) 0xaa) {
				return true;
			}
		}
		return false;
	}

	public static boolean looksLikeBankedOr9900Code(byte[] content) {
		int insts = 0;
		
		// assume that real content is in the first half (e.g. for E/A where
		// content for console is in latter half)
		for (int addr = 0; addr < content.length / 2; addr += 2) {
			int word = readAddr(content, addr);
			if (word == 0x45b /* RT */
				|| word == 0xD820  /* */
				|| word == 0x380  /* RTWP */
				|| word == 0x8300  /* CPU RAM base */
				|| word == 0x83e0  /* GPLWS */
				|| word == 0x8c00  /* VDPWA */
				|| word == 0x8c02  /* VDPWD */
				|| word == 0x8400  /* SOUND */
				)
			{
				insts++;
			}
		}
		
		boolean allIded = true;
		for (int addr = 0 ; addr < content.length; addr += 0x2000) {
			if (content[addr] != (byte) 0xaa) {
				allIded = false;
			}
		}
		if (content.length > 0x2000 && allIded)
			insts *= 2;
		
		boolean isROMCode = insts > content.length / 256;
		
		log.debug("# insts = " + insts +"; all banks have IDs: " + allIded);
		
		return isROMCode;
	}

	public static void fetchMD5(IPathFileLocator fileLocator, MemoryEntryInfo info, boolean bank2) {
		String md5;
		try {
			md5 = (String) info.getProperties().get(bank2 ? MemoryEntryInfo.FILE2_MD5 : MemoryEntryInfo.FILE_MD5);
			if (!TextUtils.isEmpty(md5))
				return;
				
			String fileName = bank2 ? info.getFilename2() : info.getFilename();
			URI uri = fileLocator.findFile(fileName);
			if (uri == null)
				return;
			
			int contentLength = fileLocator.getContentLength(uri);
			
			Pair<IMD5SumFilter, Integer> minfo = getEffectiveMD5AndSize(info, contentLength);
			IMD5SumFilter filter = minfo.first;
			contentLength = minfo.second;
			md5 = fileLocator.getContentMD5(uri, filter, true);
			
			if (bank2) {
				info.getProperties().put(MemoryEntryInfo.FILE2_MD5, md5);
				if (!info.isBanked())
					info.getProperties().put(MemoryEntryInfo.SIZE2, contentLength);
			} else {
				info.getProperties().put(MemoryEntryInfo.FILE_MD5, md5);
				if (!info.isBanked())
					info.getProperties().put(MemoryEntryInfo.SIZE, contentLength);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the canonical algorithm for summing this entry and the
	 * effective size relevant for such summing.
	 * @param info
	 * @param contentLength 
	 * @param bank2
	 * @return MD5 algorithm id and size
	 */
	public static Pair<IMD5SumFilter, Integer> getEffectiveMD5AndSize(MemoryEntryInfo info, int contentLength) {
		IMD5SumFilter filter;
		if (IMemoryDomain.NAME_GRAPHICS.equals(info.getDomainName())) {
			filter = MD5FilterAlgorithms.GromFilter.INSTANCE;
		} else {
			filter = MD5FilterAlgorithms.FullContentFilter.INSTANCE;
		}

		// tweak the filter if the segments are not what the filter
		// would have naturally selected
		List<FilterSegment> segments = new ArrayList<IMD5SumFilter.FilterSegment>();
		filter.fillSegments(contentLength, segments);
		
		if (!segments.isEmpty()) {
			FilterSegment last = segments.get(segments.size() - 1);
			int filteredContentLength = last.offset + last.length;
			
			if (filteredContentLength != contentLength) {
				contentLength = filteredContentLength;
			}
		}
		
		return new Pair<IMD5SumFilter, Integer>(filter, contentLength);
	}


	/**
	 * @param name
	 * @return
	 */
	public static boolean isASCII(String name) {
		for (char ch : name.toCharArray()) {
			if (ch < 0x20 || ch >= 127)
				return false;
		}
		return true;
	}

	/**
	 * @param addr
	 * @return
	 */
	public static String readString(byte[] content, int addr) {
		if (addr < 0 || addr >= content.length)
			return "";
		int len = content[addr++] & 0xff;
		StringBuilder sb = new StringBuilder();
		while (len != 0 && addr >= 0 && addr < content.length) {
			sb.append((char) content[addr++]);
			len--;
		}
		return sb.toString();
	}

	/**
	 * @param content
	 * @param addr
	 * @return
	 */
	public static int readAddr(byte[] content, int addr) {
		if (addr >= 0 && addr < content.length - 1)
			return ((content[addr] << 8) & 0xff00) | (content[addr+1] & 0xff);
		else
			return 0;
	}
	
	public static String cleanupTitle(String allCaps) {
		allCaps = allCaps.trim();
		
		// remove spurious quotes
		allCaps = TextUtils.unquote(allCaps, '"');
		
		// capitalize each word
		StringBuilder sb = new StringBuilder();
		boolean newWord = true;
		for (char ch : allCaps.toCharArray()) {
			
			if (Character.isLetter(ch)) {
				if (newWord) {
					ch = Character.toUpperCase(ch);
					newWord = false;
				} else {
					ch = Character.toLowerCase(ch);
				}
			} else {
				newWord = true;
			}
				
			sb.append(ch);
		}
		
		String titledName = sb.toString();
		
		// lowercase prepositions
		for (String prep : PREPOSITIONS) {
			titledName = replaceWord(titledName, prep, prep.toLowerCase(), false);
		}

		// uppercase common acronyms
		for (String acr : ACRONYMS_AND_ROMAN_NUMERALS) {
			titledName = replaceWord(titledName, acr, acr.toUpperCase(), true);
		}
				
		return titledName;		
	}

	private static boolean isSpaceOrSep(char ch) {
		return Character.isWhitespace(ch) || ch == '-' || ch == '/';
	}
	private static String replaceWord(String str, String word, String repl, boolean allowAtStart) {
		int idx = str.indexOf(word);
		if (idx > (allowAtStart ? -1 : 0)) {
			if ((idx == 0 || isSpaceOrSep(str.charAt(idx-1)))
					&& (idx + word.length() >= str.length() || isSpaceOrSep(str.charAt(idx+word.length())))) {
				str = str.substring(0, idx) + repl + str.substring(idx + word.length());
			}
		}

		return str;
	}
}
