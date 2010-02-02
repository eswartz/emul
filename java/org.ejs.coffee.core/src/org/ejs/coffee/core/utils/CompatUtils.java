/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package org.ejs.coffee.core.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;



/** Global settings for the emulator as a whole.
 * @author ejs
 */
public class CompatUtils  {

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
