/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.IOException;

public class FDRFactory {
	/**
	 * Get the FDR accessor for a file on disk.
	 * <p>
	 * This FDR is <b>not validated</b> against the file attributes.
	 * @param file
	 * @return new FDR 
	 * @throws IOException if cannot read file
	 */
    public static FDR createFDR(File file) throws IOException {
        // try TIFILES first, since it has a signature
        try {
        	return TIFILESFDR.readFDR(file);
        } catch (InvalidFDRException e) {
        }
        
        // try V9t9 FDR
        try {
        	return V9t9FDR.readFDR(file);
        } catch (InvalidFDRException e) {
        }
        
        return null;
    }
}
