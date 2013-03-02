/*
  ShowFileMD5.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import v9t9.common.files.PathFileLocator;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryEntryFactory;

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
		
		memory.setMemoryEntryFactory(new MemoryEntryFactory(settings, memory, locator));
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
