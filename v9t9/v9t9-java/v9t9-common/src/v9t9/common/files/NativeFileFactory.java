/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

public class NativeFileFactory {
	/**
	 * Create a NativeFile accessor for a native file on disk.  This does not
	 * validate the FDR for an FDR-based file.
	 * @param file
	 * @return new NativeFile
	 * @throws IOException if cannot read file
	 */
    public static NativeFile createNativeFile(File file) throws IOException {
        FDR fdr = FDRFactory.createFDR(file);
        
        if (fdr != null) {
            return new NativeFDRFile(file, fdr);
        }
        
        return new NativeTextFile(file);
    }
}
