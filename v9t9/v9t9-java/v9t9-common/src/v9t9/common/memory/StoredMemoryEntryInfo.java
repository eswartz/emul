/**
 * 
 */
package v9t9.common.memory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;

/**
 * @author ejs
 *
 */
public class StoredMemoryEntryInfo {
	public final MemoryEntryInfo info;
	public final IPathFileLocator locator;
	public final URI uri;
	public final String name;
	public final int fileoffs;
	public final int filesize;
	public final int size;
	public final IMemory memory;
	public final ISettingsHandler settings;
	
	public StoredMemoryEntryInfo(MemoryEntryInfo info, ISettingsHandler settings, IMemory memory,
			IPathFileLocator locator, URI uri, String name, int fileoffs, int filesize, int size) {
		this.info = info;
		this.settings = settings;
		this.memory = memory;
		this.locator = locator;
		this.uri = uri;
		this.name = name;
		this.fileoffs = fileoffs;
		this.filesize = filesize;
		this.size = size;
	}
	

	/**
	 * @param addr
	 * @param size
	 * @param isStored
	 * @param uri
	 * @param filesize
	 * @return
	 * @throws IOException
	 */
	public static StoredMemoryEntryInfo resolveStoredMemoryEntryInfo(
			IPathFileLocator locator, ISettingsHandler settings, IMemory memory,
			MemoryEntryInfo info,
			String name,
			String filename,
			int fileoffs) throws IOException {
		
		int size = info.getSize();
    	int addr = info.getAddress();
        boolean isStored = info.isStored();
        
		if (size < -IMemoryDomain.PHYSMEMORYSIZE
                || isStored && size <= 0
                || isStored && fileoffs != 0
                ) {
			throw new IOException("size or offset is incompatible with the memory model");
		}
        
		int filesize;
		
		// note: if stored, this finds the user's copy first or the original template
		URI uri = locator.findFile(filename);
		if (uri == null) {
			if (info.isStored()) {
				uri = locator.getWriteURI(filename);
			}
			if (uri == null)
				throw new FileNotFoundException(filename);
		}

		try {
			try {
				filesize = (int) new File(uri).length();
			} catch (IllegalArgumentException e) {
				filesize = locator.getContentLength(uri);
			}
	        
			/* for large files selected, e.g., by accident */
			if (size > 0 && filesize > size) {
				filesize = size;
			} else if (size < 0 && filesize > -size) {
				filesize = -size;
			} else if (filesize + addr > 0x10000) {
				filesize = 0x10000 - addr;
			}
            
            // for files too large
            /*if (filesize > 0x2000 && (filesize & 0x1fff) != 0) {
				filesize &= ~0x1fff;
			}*/
            
        } catch (IOException e) {		// TODO
            if (isStored) {
				filesize = size;	/* not created yet */
			} else {
				throw e;
			}
        }
        

        if (size == 0)
        	size = filesize - fileoffs;

        return new StoredMemoryEntryInfo(info, settings, memory, locator, 
        		uri, name, fileoffs, filesize, size);
	}
    

}
