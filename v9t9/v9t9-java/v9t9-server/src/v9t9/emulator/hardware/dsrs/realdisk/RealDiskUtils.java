/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

import java.io.File;
import java.net.URL;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.EmulatorServer;
import v9t9.emulator.clients.builtin.IconSetting;

/**
 * @author ejs
 *
 */
public class RealDiskUtils {

	static final URL diskImageIconPath = EmulatorServer.getDataURL("icons/disk_image.png");
	public static final SettingProperty diskImageDebug = new SettingProperty("DiskImageDebug",
	"Debug Disk Image Support",
	"When set, log disk operation information to the console.",
	Boolean.FALSE);
	public static final SettingProperty diskImageRealTime = new SettingProperty("DiskImageRealTime",
	"Real-Time Disk Images",
	"When set, disk operations on disk images will try to run at a similar speed to the original FDC1771.",
	Boolean.TRUE);
	public static final SettingProperty diskImageDsrEnabled = new IconSetting("DiskImageDSREnabled",
	"Disk Image Support",
	"This implements a drive (like DSK1) in a disk image on your host.\n\n"+
	"Either sector image or track image disks are supported.\n\n"+
	"A track image can support copy-protected disks, while a sector image cannot.",
	Boolean.FALSE, diskImageIconPath);
	public static File defaultDiskRootDir;

	static void dumpBuffer(byte[] buffer, int offs, int len)
	{
		StringBuilder builder = new StringBuilder();
		int rowLength = 32;
		int x;
		if (len > 0)
			builder.append("Buffer contents:\n");
		for (x = offs; len-- > 0; x+=rowLength, len-=rowLength) {
			int         y;
	
			builder.append(HexUtils.toHex4(x));
			builder.append(' ');
			for (y = 0; y < rowLength; y++)
				builder.append(HexUtils.toHex2(buffer[x + y]) + " ");
			builder.append(' ');
			for (y = 0; y < rowLength; y++) {
				byte b = buffer[x+y];
				if (b >= 32 && b < 127)
					builder.append((char) b);
				else
					builder.append('.');
			}
			builder.append('\n');
		}
		BaseDiskImage.info(builder.toString());
	
	}

	public static String getDiskImageSetting(int num) {
		return "DSKImage" + num;
	}

	public static File getDefaultDiskImage(String name) {
		return new File(defaultDiskRootDir, name + ".dsk");
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
