/*
  RealDiskUtils.java

  (c) 2011-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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

	/* calculate CRC for data address marks or sector data */
	/* borrowed from xmess-0.56.2.  seems like this only works for MFM */
	static short calc_crc(int crc, int value) {
		int l, h;
	
		l = value ^ ((crc >> 8) & 0xff);
		crc = (crc & 0xff) | (l << 8);
		l >>= 4;
		l ^= (crc >> 8) & 0xff;
		crc <<= 8;
		crc = (crc & 0xff00) | l;
		l = (l << 4) | (l >> 4);
		h = l;
		l = (l << 2) | (l >> 6);
		l &= 0x1f;
		crc = crc ^ (l << 8);
		l = h & 0xf0;
		crc = crc ^ (l << 8);
		l = (h << 1) | (h >> 7);
		l &= 0xe0;
		crc = crc ^ l;
		return (short) crc;
	}

}
