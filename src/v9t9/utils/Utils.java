/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;



/** Global settings for the emulator as a whole.
 * @author ejs
 */
public class Utils  {
    /**
     * @param string
     * @return
     */
    public static String padAddress(String string) {
        final byte[] zeroes = { '0', '0', '0', '0' };
        int len = 4 - string.length();
        if (len > 0) {
			return new String(zeroes, 0, len) + string;
		} else {
			return string;
		}
    } 
    
    public static String padByte(String string) {
    	final byte[] zeroes = { '0', '0' };
    	int len = 2 - string.length();
    	if (len > 0) {
    		return new String(zeroes, 0, len) + string;
    	} else {
    		return string;
    	}
    } 
    
    public static String toHex4(int value) {
        return padAddress(Integer.toHexString(value & 0xffff).toUpperCase());
    }
    
    /**
     * Parse an integer which may be in C-style hex notation (0xF00).
     * Otherwise it's interpreted as decimal.
     * @param string
     * @return
     */
    public static int parseInt(String string) {
        string = string.toUpperCase();
        if (string.length() > 2 && string.charAt(0) == '0'
            && string.charAt(1) == 'X') {
            return Integer.parseInt(string.substring(2), 16);
        }
        return Integer.parseInt(string);
    }
    
    public static String padString(String string, int size) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(string);
    	while (builder.length() < size)
    		builder.append(' ');
    	return builder.toString();
    }

	public static String toHex2(int value) {
		return padByte(Integer.toHexString(value & 0xff).toUpperCase());
	}

	/**
	 * Read an integer without a superfluous NumberFormatException from
	 * a missing key.
	 * @param section
	 * @param key
	 * @return
	 */
	public static int readSavedInt(IDialogSettings section, String key) {
		if (section == null)
			return 0;
		String value = section.get(key);
		if (value == null) {
			return 0;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return 0;
			}
		}
	}
	
	/**
	 * Read a boolean without a superfluous NumberFormatException from
	 * a missing key.

	 * @param section
	 * @param key
	 * @return
	 */
	public static boolean readSavedBoolean(IDialogSettings section, String key) {
		if (section == null)
			return false;
		String value = section.get(key);
		if (value == null) {
			return false;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	static byte   swapped_nybbles[] = 
	{ 
		0x0, 0x8, 0x4, 0xc,
		0x2, 0xa, 0x6, 0xe,
		0x1, 0x9, 0x5, 0xd,
		0x3, 0xb, 0x7, 0xf
	};

	public static      byte
	swapbits(byte in)
	{
		return (byte) ((swapped_nybbles[in & 0xf] << 4) |
			(swapped_nybbles[(in & 0xf0) >> 4]));
	}

	/**
	 * Overcome egregious buggy surprising behavior in {@link InputStream#skip(long)}
	 * @param in
	 * @param nBytes
	 * @throws IOException
	 */
	public static void skipFully(InputStream in, long nBytes) throws IOException {
    	long remaining = nBytes;
    	while (remaining > 0) {
    		long skipped = in.skip(remaining);
    		if (skipped == 0)
    			throw new EOFException();
    		remaining -= skipped;
    	}
    }
	

	public static FontDescriptor getFontDescriptor(Font font) {
		// hmmm... FontRegister.createFont() is busted
		FontData[] fontData = font.getFontData();
		int len = 0;
		while (len < fontData.length && fontData[len] != null) 
			len++;
		FontData[] fontData2 = new FontData[len];
		System.arraycopy(fontData, 0, fontData2, 0, len);
		///
		
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(fontData2);
		return fontDescriptor;
	}
}
