/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.utils;



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
}
