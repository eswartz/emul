/*
  SwtJavaClient.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.gui.client.swt;

import v9t9.common.client.IClient;
import v9t9.common.machine.IMachine;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtJavaClient extends BaseSwtJavaClient implements IClient {
	public static String ID = "SWT";
	
    public SwtJavaClient(final IMachine machine) {
    	super(machine);
    }

    @Override
    public String getIdentifier() {
    	return ID;
    }
	/**
	 * 
	 */
	protected void setupRenderer() {
		videoRenderer = createSwtVideoRenderer(display);
		keyboardHandler = new SwtKeyboardHandler(
				machine.getKeyboardState(), machine);
	}
    
}

