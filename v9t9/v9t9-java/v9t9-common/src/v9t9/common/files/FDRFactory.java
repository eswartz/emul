/*
  FDRFactory.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

public class FDRFactory {
	/**
	 * Get the FDR accessor for a file on disk.
	 * <p>
	 * This FDR is <b>not validated</b> against the file attributes.
	 * @param file
	 * @return FDR or null 
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
