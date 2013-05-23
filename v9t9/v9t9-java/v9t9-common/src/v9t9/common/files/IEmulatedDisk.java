/**
 * 
 */
package v9t9.common.files;

import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface IEmulatedDisk extends IEmulatedStorage {

	Catalog readCatalog() throws IOException;

	IEmulatedFile getFile(String name) throws IOException;

	String getPath();
	

	/**
	 * Tell if the disk appears formatted
	 * @return
	 */
	boolean isFormatted();

	/**
	 * Create (deleting any existing) file
	 * @param fileName
	 * @param srcFdr prototype for file 
	 * @return new emulated file
	 * @throws IOException 
	 */
	IEmulatedFile createFile(String fileName, FDR fdr) throws IOException;

}
