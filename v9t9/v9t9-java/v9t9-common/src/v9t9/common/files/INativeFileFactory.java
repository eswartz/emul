/*
  INativeFileFactory.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

/**
 * @author ejs
 *
 */
public interface INativeFileFactory {
	INativeFileFactory INSTANCE = new NativeFileFactory();
	
	/**
	 * Create a NativeFile accessor for a native file on disk.  This does not
	 * validate the FDR for an FDR-based file, but will distinguish between
	 * an FDR-based and a text-based file.
	 * @param file 
	 * @return new NativeFile
	 * @throws IOException if cannot read file
	 */
    NativeFile createNativeFile(File file) throws IOException;
    NativeFile createNativeFile(IEmulatedDisk disk, File file) throws IOException;
}
