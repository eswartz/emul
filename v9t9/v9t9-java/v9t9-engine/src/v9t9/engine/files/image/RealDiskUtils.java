/*
  RealDiskUtils.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class RealDiskUtils {
	static void dumpBuffer(Dumper dumper, byte[] buffer, int offs, int len)
	{
		if (!dumper.isEnabled())
			return;
		
		StringBuilder builder = new StringBuilder();
		int rowLength = 32;
		int x;
		if (len > 0)
			builder.append("Buffer contents:\n");
		for (x = offs; len-- > 0; x+=rowLength, len-=rowLength) {
			int         y;
	
			builder.append(HexUtils.toHex4(x));
			builder.append(' ');
			for (y = 0; y < rowLength && x + y < buffer.length; y++)
				builder.append(HexUtils.toHex2(buffer[x + y]) + " ");
			builder.append(' ');
			for (y = 0; y < rowLength && x + y < buffer.length; y++) {
				byte b = buffer[x+y];
				if (b >= 32 && b < 127)
					builder.append((char) b);
				else
					builder.append('.');
			}
			builder.append('\n');
		}
		
		dumper.info(builder.toString());
	
	}
}
