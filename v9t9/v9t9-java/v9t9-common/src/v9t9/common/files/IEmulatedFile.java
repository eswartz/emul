/*
  IEmulatedFile.java

  (c) 2009-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

/**
 * This is the interface to files handled by the emulator (either by emulating a
 * DSR or through direct manipulation for utilities).
 * @author ejs
 *
 */
public interface IEmulatedFile {
	/** Tell whether the file exists */
	boolean exists();
	
	/** Check validity, returning a string describing any problems or <code>null</code> for success */
	String isValid();
	
	/** Get the FDR */
	FDR getFDR();

	
}
