/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

/**
 * @author ejs
 *
 */
public class CatalogEntry {

	public final String fileName;
	public final int secs;
	public final String type;
	public final int recordLength;

	/**
	 * @param fileName
	 * @param sz
	 * @param type
	 * @param recordLength
	 */
	public CatalogEntry(String fileName, int sz, String type, int recordLength) {
		this.fileName = fileName;
		this.secs = sz;
		this.type = type;
		this.recordLength = recordLength;
	}

}
