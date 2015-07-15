/**
 * 
 */
package v9t9.common.modules;

import java.util.Arrays;
import java.util.Comparator;

import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.utils.FileUtils;
import ejs.base.utils.HexUtils;

/**
 * This class constructs "module MD5s" from module entries,
 * allowing a cheap and portable way of identifying a module,
 * short of encoding all its content.
 * @author ejs
 *
 */
public class ModuleMD5Sums {

	public static String createMD5(IModule module) {
		return createMD5(module.getMemoryEntryInfos());
	}
	
	public static String createMD5(MemoryEntryInfo[] ents) {
		Arrays.sort(ents, new Comparator<MemoryEntryInfo>() {

			@Override
			public int compare(MemoryEntryInfo arg0, MemoryEntryInfo arg1) {
				int diff = arg0.getDomainName().compareTo(arg1.getDomainName());
				if (diff != 0)
					return diff;
				
				diff = arg0.getAddress() - arg1.getAddress();
				if (diff != 0)
					return diff;
				
				diff = arg0.getAddress2() - arg1.getAddress2();
				if (diff != 0)
					return diff;
				
				diff = arg0.getBankSize() - arg1.getBankSize();
				if (diff != 0)
					return diff;
				
				diff = arg0.getSize() - arg1.getSize();
				if (diff != 0)
					return diff;
				
				return arg0.getName().compareTo(arg1.getName());
			}
			
		});
		
		StringBuilder sb = new StringBuilder();
		for (MemoryEntryInfo ent : ents) {
			sb.append(ent.getDomainName());
			sb.append(HexUtils.toHex4(ent.getAddress()));
			if (ent.getAddress2() != 0)
				sb.append(HexUtils.toHex4(ent.getAddress2()));
			if (ent.getFileMD5() != null) {
				sb.append(ent.getFileMD5Algorithm());
				sb.append(ent.getFileMD5());
			}
			if (ent.getFile2MD5() != null) {
				sb.append(ent.getFile2MD5Algorithm());
				sb.append(ent.getFile2MD5());
			}
		}

		return FileUtils.getMD5Hash(sb.toString().getBytes());
	}
}
