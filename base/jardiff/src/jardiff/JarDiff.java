/**
 * 
 */
package jardiff;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author ejs
 *
 */
public class JarDiff {

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Run as:  JarDiff [old.jar] [new.jar]\n"+
					"\n"+
					"Returns exit code 0 for same, 1 for different\n");
			System.exit(1);
		}
		
		File file1 = new File(args[0]);
		File file2 = new File(args[1]);
		if (!file1.exists() || !file2.exists()) {
			System.exit(1);
		}
		
		byte[] content1 = FileUtils.readInputStreamContentsAndClose(new FileInputStream(file1));
		byte[] content2 = FileUtils.readInputStreamContentsAndClose(new FileInputStream(file2));
		
		if (content1.length != content2.length) {
			System.out.println("File lengths differ");
			//System.exit(1);
		}
		
		// files appear to differ -- but it could just be timestamps
		Map<String, String> file1Hashes = getZipHashes(content1);
		Map<String, String> file2Hashes = getZipHashes(content2);
		
		boolean changed = false;
		for (Map.Entry<String, String> ent1 : file1Hashes.entrySet()) {
			String hash1 = ent1.getValue();
			String hash2 = file2Hashes.get(ent1.getKey());
			if (hash2 == null) {
				System.out.println("added " + ent1.getKey());
				changed = true;
			}
			else if (!hash1.equals(hash2)) {
				System.out.println("changed " + ent1.getKey());
				changed = true;
			}
		}
		
		for (Map.Entry<String, String> ent2 : file2Hashes.entrySet()) {
			String hash1 = file1Hashes.get(ent2.getKey());
			if (hash1 == null) {
				System.out.println("deleted " + ent2.getKey());
				changed = true;
			}
		}

		if (changed)
			System.exit(1);
		
		System.exit(0);
	}

	/**
	 * @param content1
	 * @return
	 * @throws IOException 
	 */
	private static Map<String, String> getZipHashes(byte[] content) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		ZipInputStream zis = new ZipInputStream(bis);
		
		Map<String, String> hashes = new LinkedHashMap<String, String>();
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			if (!entry.isDirectory()) {
				String crc = Long.toString(entry.getCrc());
				hashes.put(entry.getName(), crc);
			}
			zis.closeEntry();
		}
		zis.close();
		bis.close();
		
		return hashes;
	}
}

