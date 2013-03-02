/*
  IGLDataCanvas.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import java.nio.Buffer;

/**
 * @author ejs
 *
 */
public interface IGLDataCanvas {
	int GL_RGB = 0x1907;
	int GL_RGBA = 0x1908;
	int GL_BGR = 0x80e0;
	int GL_BGRA = 0x80e1;
	int GL_LUMINANCE = 0x1909;
	
	int GL_LUMINANCE16 = 0x8042;
	int GL_LUMINANCE12_ALPHA4 = 0x8046;
	int GL_LUMINANCE16_ALPHA16 = 0x8048;
	
	int GL_UNSIGNED_BYTE = 0x1401;
	
	int GL_UNSIGNED_BYTE_3_3_2 = 0x8032;
	
	int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
	int GL_UNSIGNED_SHORT_5_6_5_REV = 0x8364;
	int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033;
	int GL_UNSIGNED_SHORT_4_4_4_4_REV = 0x8365;
	
	int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
	int GL_UNSIGNED_SHORT_1_5_5_5_REV = 0x8366;
	
	int GL_UNSIGNED_INT_8_8_8_8 = 0x8035;
	int GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367;
	
	int GL_RGB4 = 0x804f;
	int GL_RGB5 = 0x8050;
	int GL_RGB8 = 0x8051;
	int GL_RGBA8 = 0x8058;
	
	Buffer getBuffer();

	/**
	 * @return GL_UNSIGNED_BYTE, etc.
	 */
	int getImageType();

	/**
	 * @return GL_RGB, GL_RGBA, GL_LUMINANCE, etc.
	 */
	int getImageFormat();

	/**
	 * @return
	 */
	int getInternalFormat();
}
