/**
 * 
 */
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.Catalog;
import v9t9.common.files.IFileHandler;
import v9t9.engine.files.directory.DiskDirectoryMapper;
import v9t9.engine.files.directory.FileDirectory;
import v9t9.engine.files.image.BaseDiskImage;
import v9t9.engine.files.image.DiskImageFactory;

/**
 * @author Ed
 *
 */
public class FileHandler implements IFileHandler {

	private final ISettingsHandler settings;

	public FileHandler(ISettingsHandler settings) {
		this.settings = settings;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileHandler#createCatalog(v9t9.base.properties.IProperty, boolean)
	 */
	@Override
	public Catalog createCatalog(IProperty diskProperty, boolean isDiskImage)
			throws IOException {
		String name = diskProperty.getName();
		File spec = new File(diskProperty.getString());
		if (isDiskImage) {
			BaseDiskImage image = DiskImageFactory.createDiskImage(
					settings, name, spec);
			image.openDiskImage();
			
			Catalog catalog = image.readCatalog();
			
			image.closeDiskImage();
			return catalog;
		} else {
			FileDirectory fileDir = new FileDirectory(spec, DiskDirectoryMapper.INSTANCE);

			Catalog catalog = fileDir.readCatalog();

			return catalog;
		}
	}

}
