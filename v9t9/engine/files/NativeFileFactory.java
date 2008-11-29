/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

public class NativeFileFactory {
	/**
	 * Create a NativeFile accessor for a native file on disk
	 * @param file
	 * @return new NativeFile
	 * @throws IOException if cannot read file
	 */
    public static NativeFile createNativeFile(File file) throws IOException {
        FDR fdr;
        
        // try TIFILES first, since it has a signature
        try {
            fdr = TIFILESFDR.readFDR(file);
            return new NativeFDRFile(file, fdr);
        } catch (InvalidFDRException e) {
        }
        
        // try V9t9 FDR
        try {
            fdr = V9t9FDR.readFDR(file);
            return new NativeFDRFile(file, fdr);
        } catch (InvalidFDRException e) {
        }
        
        return new NativeTextFile(file);
    }
}
