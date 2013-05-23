/*
  NativeFileFactory.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

public class NativeFileFactory implements INativeFileFactory {
	/**
	 * Create a NativeFile accessor for a native file on disk.  This does not
	 * validate the FDR for an FDR-based file.
	 * @param file
	 * @return new NativeFile
	 * @throws IOException if cannot read file
	 */
    public NativeFile createNativeFile(File file) throws IOException {
    	return createNativeFile(null, file);
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.files.INativeFileFactory#createNativeFile(v9t9.common.files.IEmulatedDisk, java.io.File)
     */
    @Override
    public NativeFile createNativeFile(IEmulatedDisk disk, File file)
    		throws IOException {
        FDR fdr = FDRFactory.createFDR(file);
        
        if (fdr != null) {
            return new NativeFDRFile(disk, file, fdr);
        }
        
        return new NativeTextFile(disk, file);
    }
}
