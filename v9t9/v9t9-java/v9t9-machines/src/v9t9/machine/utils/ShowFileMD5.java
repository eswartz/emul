/*
  ShowFileMD5.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import v9t9.common.files.PathFileLocator;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.machine.ti99.memory.TI994AMemoryEntryFactory;

/**
 * @author ejs
 *
 */
public class ShowFileMD5 {

	public static void main(String[] args) throws IOException {
		ShowFileMD5 runner = new ShowFileMD5();
		runner.run(args);
	}

	private Memory memory;
	private PathFileLocator locator;
	private BasicSettingsHandler settings;
	
	/**
	 * 
	 */
	public ShowFileMD5() {
		memory = new Memory();
		locator = new PathFileLocator();
		
		settings = new BasicSettingsHandler();
		
		memory.setMemoryEntryFactory(new TI994AMemoryEntryFactory(memory, settings, locator));
	}
	
	protected void run(String[] args) throws IOException {
		
		for (String arg : args) {
			File file = new File(arg);
			URI uri = file.toURI();
			
			String md5 = locator.getContentMD5(uri);
	
			System.out.println(file + " ==> " + md5);
		}
	}
}
