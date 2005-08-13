/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;

/** Global settings for the emulator as a whole.
 * @author ejs
 */
public class Globals  {
    public static SettingsCollection settings = new SettingsCollection();

    /**
     * @param string
     * @return
     */
    public static String padAddress(String string) {
        final byte[] zeroes = { '0', '0', '0', '0' };
        int len = 4 - string.length();
        if (len > 0)
            return new String(zeroes, 0, len) + string;
        else
            return string;
    } 
    
    public static String toHex4(int value) {
        return padAddress(Integer.toHexString(value & 0xffff).toUpperCase());
    }
}
